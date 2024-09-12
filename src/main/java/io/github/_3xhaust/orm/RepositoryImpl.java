package io.github._3xhaust.orm;

import io.github._3xhaust.orm.driver.MysqlConnectionOptions;
import io.github._3xhaust.orm.driver.SqliteConnectionOptions;
import io.github._3xhaust.annotations.orm.Column;
import io.github._3xhaust.annotations.orm.PrimaryGeneratedColumn;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class RepositoryImpl<T> implements Repository<T> {
    private final Class<T> entityClass;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private Field primaryKeyField;

    public RepositoryImpl(Class<T> entityClass, SqliteConnectionOptions connectionOptions) {
        this.entityClass = entityClass;
        this.jdbcUrl = "jdbc:sqlite:" + connectionOptions.database;
        this.username = null;
        this.password = null;
        findPrimaryKeyField();
    }

    public RepositoryImpl(Class<T> entityClass, MysqlConnectionOptions connectionOptions) {
        this.entityClass = entityClass;
        this.jdbcUrl = "jdbc:mysql://" + connectionOptions.host + ":" + connectionOptions.port + "/" + connectionOptions.database;
        this.username = connectionOptions.username;
        this.password = connectionOptions.password;
        findPrimaryKeyField();
    }

    private void findPrimaryKeyField() {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryGeneratedColumn.class)) {
                this.primaryKeyField = field;
                this.primaryKeyField.setAccessible(true);
                return;
            }
        }
        // Primary Key 필드를 찾지 못했지만, 오류를 발생시키지 않습니다.
    }

    @Override
    public T save(T entity) {
        if (primaryKeyField == null) {
            insertWithoutId(entity);
            return entity;
        }

        if (getId(entity) == null) {
            insert(entity);
        } else {
            update(entity);
        }
        return entity;
    }

    private Long getId(T entity) {
        try {
            return (Long) primaryKeyField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get ID from entity: " + e.getMessage(), e);
        }
    }

    private void insert(T entity) {
        String tableName = getTableName(entityClass);

        if (!tableExists(tableName)) {
            createTable(tableName, entity.getClass());
        }

        if (hasUniqueConstraintViolation(entity)) {
            throw new RuntimeException("Unique constraint violation!");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");

        List<Object> values = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();

        for (Field field : getFieldsWithoutPrimaryKey(entityClass)) {
            field.setAccessible(true);
            try {
                sql.append(field.getName()).append(", ");
                values.add(field.get(entity));
                placeholders.append("?, ");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + e.getMessage(), e);
            }
        }

        sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ") VALUES (" + placeholders.substring(0, placeholders.length() - 2) + ")");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {

            setParameters(statement, values);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    setPrimaryKeyField(entity, generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert entity: " + e.getMessage(), e);
        }
    }

    private void insertWithoutId(T entity) {
        String tableName = getTableName(entityClass);

        if (!tableExists(tableName)) {
            createTable(tableName, entity.getClass());
        }

        if (hasUniqueConstraintViolation(entity)) {
            throw new RuntimeException("Unique constraint violation!");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");

        List<Object> values = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();

        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                sql.append(field.getName()).append(", ");
                values.add(field.get(entity));
                placeholders.append("?, ");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + e.getMessage(), e);
            }
        }

        sql = new StringBuilder(sql.substring(0, sql.length() - 2) + ") VALUES (" + placeholders.substring(0, placeholders.length() - 2) + ")");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, values);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert entity: " + e.getMessage(), e);
        }
    }

    private void update(T entity) {
        String tableName = getTableName(entityClass);

        if (hasUniqueConstraintViolation(entity)) {
            throw new RuntimeException("Unique constraint violation!");
        }

        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");

        List<Object> values = new ArrayList<>();
        for (Field field : getFieldsWithoutPrimaryKey(entityClass)) {
            field.setAccessible(true);
            try {
                sql.append(field.getName()).append(" = ?, ");
                values.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + e.getMessage(), e);
            }
        }

        sql = new StringBuilder(sql.substring(0, sql.length() - 2) + " WHERE " + primaryKeyField.getName() + " = ?");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, values);
            statement.setLong(values.size() + 1, getId(entity));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update entity: " + e.getMessage(), e);
        }
    }

    @Override
    public List<T> findOne(Map<String, Object> where) {
        String tableName = getTableName(entityClass);
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
        if (primaryKeyField == null) {
            throw new RuntimeException("Entity Class '" + entityClass.getSimpleName() + "'에 Primary Key 필드가 없습니다.");
        }

        String tableName = getTableName(entityClass);
        String sql = "DELETE FROM " + tableName + " WHERE " + primaryKeyField.getName() + " = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, getId(entity));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entity: " + e.getMessage(), e);
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
                if (field.getType() == Long.class) {
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

    private void setPrimaryKeyField(T entity, Long id) {
        try {
            primaryKeyField.set(entity, id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID field: " + e.getMessage(), e);
        }
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

        Field[] fields = entityClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            sql.append(field.getName()).append(" ").append(getSqlType(field.getType()));

            if (field.isAnnotationPresent(PrimaryGeneratedColumn.class)) {
                sql.append(" PRIMARY KEY AUTOINCREMENT");
            } else if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation.unique()) {
                    sql.append(" UNIQUE");
                }
            }

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

    private String getTableName(Class<?> entityClass) {
        return entityClass.getSimpleName().toLowerCase() + "s";
    }

    private List<Field> getFieldsWithoutPrimaryKey(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(PrimaryGeneratedColumn.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private void setParameters(PreparedStatement statement, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
        }
    }

    private boolean hasUniqueConstraintViolation(T entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation.unique()) {
                    try {
                        field.setAccessible(true);
                        Object fieldValue = field.get(entity);
                        if (fieldValue != null && !isUniqueValue(getTableName(entityClass), field.getName(), fieldValue, getId(entity))) {
                            return true;
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access field: " + e.getMessage(), e);
                    }
                }
            }
        }
        return false;
    }

    private boolean isUniqueValue(String tableName, String columnName, Object value, Long id) {
        String sql;
        if (primaryKeyField != null) {
            sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ? AND " + primaryKeyField.getName() + " != ?";
        } else {
            sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, value);
            if (primaryKeyField != null) {
                statement.setLong(2, id == null ? -1 : id);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check unique constraint: " + e.getMessage(), e);
        }
    }
}