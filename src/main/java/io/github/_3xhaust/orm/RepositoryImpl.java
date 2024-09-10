package io.github._3xhaust.orm;

import io.github._3xhaust.orm.driver.MysqlConnectionOptions;
import io.github._3xhaust.orm.driver.SqliteConnectionOptions;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class RepositoryImpl<T> implements Repository<T> {
    private final Map<Long, T> entities = new HashMap<>();
    private long nextId = 1;
    private final Class<T> entityClass;
    private final DataSourceOptions.DatabaseType databaseType;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public RepositoryImpl(Class<T> entityClass, SqliteConnectionOptions connectionOptions) {
        this.entityClass = entityClass;
        this.databaseType = DataSourceOptions.DatabaseType.SQLITE;
        this.jdbcUrl = "jdbc:sqlite:" + connectionOptions.database;
        this.username = null;
        this.password = null;
    }

    public RepositoryImpl(Class<T> entityClass, MysqlConnectionOptions connectionOptions) {
        this.entityClass = entityClass;
        this.databaseType = DataSourceOptions.DatabaseType.MYSQL;
        this.jdbcUrl = "jdbc:mysql://" + connectionOptions.host + ":" + connectionOptions.port + "/" + connectionOptions.database;
        this.username = connectionOptions.username;
        this.password = connectionOptions.password;
    }

    @Override
    public T save(T entity) {
        String tableName = entity.getClass().getSimpleName().toLowerCase() + "s";

        if (!tableExists(tableName)) {
            createTable(tableName, entity.getClass());
        }

        try {
            Field idField = getIdField(entity.getClass());
            idField.setAccessible(true);
            Long id = (Long) idField.get(entity);

            if (id == null) { // 새 엔티티 저장
                StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");

                Field[] fields = entity.getClass().getDeclaredFields();
                List<Object> values = new ArrayList<>();
                for (int i = 1; i < fields.length; i++) {  // id 필드 제외
                    Field field = fields[i];
                    field.setAccessible(true);
                    try {
                        sql.append(field.getName());
                        values.add(field.get(entity));
                        if (i < fields.length - 1) {
                            sql.append(", ");
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access field: " + e.getMessage(), e);
                    }
                }
                sql.append(") VALUES (");
                for (int i = 0; i < values.size(); i++) {
                    sql.append("?");
                    if (i < values.size() - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(")");

                try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                     PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {

                    for (int i = 0; i < values.size(); i++) {
                        statement.setObject(i + 1, values.get(i));
                    }

                    statement.executeUpdate();

                    if (databaseType == DataSourceOptions.DatabaseType.MYSQL) {
                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                Long generatedId = generatedKeys.getLong(1);
                                setIdField(entity, generatedId);
                            }
                        }
                    } else if (databaseType == DataSourceOptions.DatabaseType.SQLITE) {
                        // SQLite의 경우 마지막으로 삽입된 행의 ID를 가져옴
                        try (Statement stmt = connection.createStatement()) {
                            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                            if (rs.next()) {
                                Long generatedId = rs.getLong(1);
                                setIdField(entity, generatedId);
                            }
                        }
                    }
                }
            } else {  // 기존 엔티티 업데이트
                update(entity);
            }

        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to save entity: " + e.getMessage(), e);
        }
        return entity;
    }


    private boolean tableExists(String tableName) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check table existence: " + e.getMessage(), e);
        }
    }

    private void createTable(String tableName, Class<?> entityClass) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

        Field[] fields = entityClass.getDeclaredFields();
        for (int i = 1; i < fields.length; i++) {
            Field field = fields[i];
            sql.append(field.getName()).append(" ").append(getSqlType(field.getType()));
            if (i < fields.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute(sql.toString());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: " + e.getMessage(), e);
        }
    }

    private String getSqlType(Class<?> type) {
        if (type == String.class) {
            return "TEXT";
        } else if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
            return "INTEGER";
        } else if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
            return "REAL";
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type.getName());
        }
    }

    @Override
    public List<T> findOne(Map<String, Object> where) {
        String tableName = entityClass.getSimpleName().toLowerCase() + "s";
        String whereClause = buildWhereClause(where);
        String sql = "SELECT * FROM " + tableName + " " + whereClause;

        List<T> results = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setWhereParameters(statement, where);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(createEntityFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find entities: " + e.getMessage(), e);
        }
        return results;
    }

    @Override
    public List<T> find() {
        return findOne(null);
    }

    @Override
    public void delete(T entity) {
        String tableName = entityClass.getSimpleName().toLowerCase() + "s";
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            Field idField = getIdField(entity.getClass());
            idField.setAccessible(true);
            Long id = (Long) idField.get(entity);
            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (NoSuchFieldException | IllegalAccessException | SQLException e) {
            throw new RuntimeException("Failed to delete entity: " + e.getMessage(), e);
        }
    }


    @Override
    public void update(T entity) {
        String tableName = entityClass.getSimpleName().toLowerCase() + "s";
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");

        Field[] fields = entity.getClass().getDeclaredFields();
        List<Object> values = new ArrayList<>();
        for (int i = 1; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                sql.append(field.getName()).append(" = ?");
                values.add(field.get(entity));
                if (i < fields.length - 1) {
                    sql.append(", ");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + e.getMessage(), e);
            }
        }

        sql.append(" WHERE id = ?");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            for (Object value : values) {
                statement.setObject(paramIndex++, value);
            }

            Field idField = getIdField(entity.getClass());
            idField.setAccessible(true);
            Long id = (Long) idField.get(entity);
            statement.setLong(paramIndex, id);

            statement.executeUpdate();

        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to update entity: " + e.getMessage(), e);
        }
    }

    @Override
    public T create(Object dto) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            for (Field dtoField : dto.getClass().getDeclaredFields()) {
                dtoField.setAccessible(true);
                for (Field entityField : entityClass.getDeclaredFields()) {
                    entityField.setAccessible(true);
                    if (dtoField.getName().equals(entityField.getName()) && dtoField.getType() == entityField.getType()) {
                        entityField.set(entity, dtoField.get(dto));
                        break;
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity instance: " + e.getMessage(), e);
        }
    }

    private T createEntityFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if ("id".equals(fieldName) && field.getType() == Long.class) {
                    field.set(entity, resultSet.getLong(fieldName));
                } else {
                    field.set(entity, resultSet.getObject(fieldName));
                }
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity from ResultSet: " + e.getMessage(), e);
        }
    }

    private void setIdField(T entity, Long id) {
        try {
            Field idField = getIdField(entity.getClass());
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID field: " + e.getMessage(), e);
        }
    }

    private Field getIdField(Class<?> clazz) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("id")) {
                return field;
            }
        }
        throw new NoSuchFieldException("No 'id' field found");
    }


    private String buildWhereClause(Map<String, Object> where) {
        if (where == null || where.isEmpty()) {
            return "";
        }

        List<String> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : where.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Collection) {
                Collection<?> values = (Collection<?>) value;
                if (!values.isEmpty()) {
                    List<String> placeholders = new ArrayList<>();
                    for (Object v : values) {
                        placeholders.add("?");
                    }
                    conditions.add(key + " IN (" + String.join(", ", placeholders) + ")");
                }
            } else {
                conditions.add(key + " = ?");
            }
        }

        return "WHERE " + String.join(" AND ", conditions);
    }

    private void setWhereParameters(PreparedStatement statement, Map<String, Object> where) throws SQLException {
        if (where != null) {
            int paramIndex = 1;
            for (Object value : where.values()) {
                if (value instanceof Collection) {
                    for (Object v : (Collection<?>) value) {
                        statement.setObject(paramIndex++, v);
                    }
                } else {
                    statement.setObject(paramIndex++, value);
                }
            }
        }
    }
}