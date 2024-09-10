package io.github._3xhaust.orm;

public class AvnoiOrmModule {
    private static DataSourceOptions dataSourceOptions;

    public static void forRoot(DataSourceOptions options) {
        dataSourceOptions = options;
    }

    public static DataSourceOptions getDataSourceOptions() {
        if (dataSourceOptions == null) {
            throw new IllegalStateException("AvnoiOrmModule is not initialized yet. Please call AvnoiOrmModule.forRoot() first.");
        }
        return dataSourceOptions;
    }
}