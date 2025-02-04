package dev.subscripted.dbBox_v1.MySQL.src;

public class DatasourceMySQL {

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    public DatasourceMySQL(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
