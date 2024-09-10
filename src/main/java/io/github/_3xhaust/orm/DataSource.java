package io.github._3xhaust.orm;

import io.github._3xhaust.orm.driver.MysqlConnectionOptions;
import io.github._3xhaust.orm.driver.SqliteConnectionOptions;

public class DataSource {
    public DataSource(DataSourceOptions options) {
        if (options.getType() == DataSourceOptions.DatabaseType.SQLITE) {
            SqliteConnectionOptions sqliteOptions = options.getSqliteOptions();
        } else if (options.getType() == DataSourceOptions.DatabaseType.MYSQL) {
            MysqlConnectionOptions mysqlOptions = options.getMysqlOptions();
        } else {
            throw new IllegalArgumentException("지원하지 않는 데이터베이스 유형: " + options.getType());
        }
    }
}