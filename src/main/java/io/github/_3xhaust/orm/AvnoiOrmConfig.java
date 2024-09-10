package io.github._3xhaust.orm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvnoiOrmConfig extends DataSourceOptions{
    private int retryAttempts = 10;
    private int retryDelay = 3000;
    private boolean autoLoadEntities = false;
    @Deprecated
    private boolean keepConnectionAlive = false;
    private boolean verboseRetryLog = false;
    private boolean manualInitialization = false;
    private final DataSourceOptions options;

    public AvnoiOrmConfig(DataSourceOptions options) {
        super(options.getType(), options.getSqliteOptions(), options.isLoggingEnabled());
        this.options = options;
    }
}