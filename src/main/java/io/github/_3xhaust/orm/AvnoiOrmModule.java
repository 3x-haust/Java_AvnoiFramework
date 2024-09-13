package io.github._3xhaust.orm;

import io.github._3xhaust.Avnoi;

public class AvnoiOrmModule {
    private static DataSourceOptions dataSourceOptions;

    public static void forRoot(DataSourceOptions options) {
        dataSourceOptions = options;
        Avnoi.isOrmInitialized = true;
    }

    public static DataSourceOptions getDataSourceOptions() {
        if (dataSourceOptions == null) {
            throw new RuntimeException("ORM is not initialized. If you need to use ORM features, please import AvnoiOrmModule in your AppModule and call AvnoiOrmModule.forRoot() before running the application.");
        }
        return dataSourceOptions;
    }
}