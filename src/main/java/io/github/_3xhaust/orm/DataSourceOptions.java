package io.github._3xhaust.orm;

import io.github._3xhaust.orm.connections.MysqlConnectionOptions;
import io.github._3xhaust.orm.connections.SqliteConnectionOptions;
import lombok.Getter;

public class DataSourceOptions {
    public enum DatabaseType {
        SQLITE, MYSQL
    }

    @Getter
    private final DatabaseType type;
    private final boolean logging;

    private SqliteConnectionOptions sqliteOptions;
    private MysqlConnectionOptions mysqlOptions;

    public static DataSourceOptions sqlite(String database, boolean logging) {
        return new DataSourceOptions(DatabaseType.SQLITE, new SqliteConnectionOptions(database), logging);
    }

    public static DataSourceOptions mysql(String database, boolean logging, String username, String password) {
        return new DataSourceOptions(DatabaseType.MYSQL, new MysqlConnectionOptions(database, username, password), logging);
    }

    DataSourceOptions(DatabaseType type, SqliteConnectionOptions sqliteOptions, boolean logging) {
        this.type = type;
        this.sqliteOptions = sqliteOptions;
        this.logging = logging;
    }

    private DataSourceOptions(DatabaseType type, MysqlConnectionOptions mysqlOptions, boolean logging) {
        this.type = type;
        this.mysqlOptions = mysqlOptions;
        this.logging = logging;
    }

    public boolean isLoggingEnabled() {
        return logging;
    }

    public SqliteConnectionOptions getSqliteOptions() {
        if (type != DatabaseType.SQLITE) {
            throw new IllegalStateException("SqliteConnectionOptions is only available for SQLITE.");
        }
        return sqliteOptions;
    }

    public MysqlConnectionOptions getMysqlOptions() {
        if (type != DatabaseType.MYSQL) {
            throw new IllegalStateException("MysqlConnectionOptions is only available for MYSQL.");
        }
        return mysqlOptions;
    }
}