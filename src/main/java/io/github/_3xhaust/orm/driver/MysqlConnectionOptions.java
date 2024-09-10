package io.github._3xhaust.orm.driver;

public class MysqlConnectionOptions {
    public String host = "localhost";
    public int port = 3306;
    public String username;
    public String password;
    public String database;

    public MysqlConnectionOptions(String database, String username, String password) {
        this.database = database;
        this.username = username;
        this.password = password;
    }
}