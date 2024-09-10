package io.github._3xhaust.orm;

import io.github._3xhaust.orm.driver.MysqlConnectionOptions;
import io.github._3xhaust.orm.driver.SqliteConnectionOptions;

import java.util.HashMap;
import java.util.Map;

public class RepositoryFactoryImpl implements RepositoryFactory {
    private final Map<Class<?>, Repository<?>> repositories = new HashMap<>();
    private final DataSourceOptions dataSourceOptions;

    public RepositoryFactoryImpl(DataSourceOptions dataSourceOptions) {
        this.dataSourceOptions = dataSourceOptions;
    }

    @Override
    public <T> Repository<T> getRepository(Class<T> entityClass) {
        if (!repositories.containsKey(entityClass)) {
            Repository<?> repository = switch (dataSourceOptions.getType()) {
                case SQLITE -> {
                    SqliteConnectionOptions sqliteOptions = dataSourceOptions.getSqliteOptions();
                    yield new RepositoryImpl<>(entityClass, sqliteOptions);
                }
                case MYSQL -> {
                    MysqlConnectionOptions mysqlOptions = dataSourceOptions.getMysqlOptions();
                    yield new RepositoryImpl<>(entityClass, mysqlOptions);
                }
                default ->
                        throw new IllegalArgumentException("Unsupported database type: " + dataSourceOptions.getType());
            };
            repositories.put(entityClass, repository);
        }
        return (Repository<T>) repositories.get(entityClass);
    }
}