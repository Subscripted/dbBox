package dev.subscripted.dbBox_v1.MySQL.src;

import dev.subscripted.dbBox_v1.MySQL.exception.DatabaseException;
import dev.subscripted.dbBox_v1.MySQL.table.Table;
import dev.subscripted.dbBox_v1.MySQL.table.TableBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verwaltet Datenbankverbindungen, führt Operationen aus und bietet asynchrone APIs.
 */
public class DatasourceManagerMySQL {

    private final int MAX_CONNECTIONS = 10;
    private final int MAX_ATTEMPTS = 3;
    private final DatasourceMySQL info;
    private final BlockingQueue<Connection> connectionPool;
    private final List<Table> tables;

    // Eigener ExecutorService für asynchrone Operationen
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS);

    private static final Logger LOGGER = Logger.getLogger(DatasourceManagerMySQL.class.getName());

    public DatasourceManagerMySQL(DatasourceMySQL info) {
        this.info = info;
        this.connectionPool = new ArrayBlockingQueue<>(MAX_CONNECTIONS);
        for (int i = 1; i < MAX_CONNECTIONS; i++) {
            connectionPool.add(openConnection());
        }
        this.tables = new ArrayList<>();
    }

    /**
     * Öffnet eine neue Datenbankverbindung.
     */
    private Connection openConnection() {
        try {
            return DriverManager.getConnection(info.getUrl(), info.getUser(), info.getPassword());
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to create a database connection.", exception);
        }
    }

    /**
     * Holt eine Verbindung aus dem Pool.
     */
    private Connection getConnection() throws InterruptedException {
        return connectionPool.take();
    }

    /**
     * Gibt eine Verbindung zurück in den Pool.
     */
    private void releaseConnection(Connection connection) {
        connectionPool.add(connection);
    }

    /**
     * Führt eine Operation sicher mit mehrfachen Versuchen aus.
     *
     * @param operation Die Datenbankoperation, die ausgeführt werden soll.
     */
    private void runSecureOperation(DatabaseOperationMySQL operation) {
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Connection connection = null;
            try {
                connection = getConnection();
                operation.executeOperation(connection);
                releaseConnection(connection);
                return;
            } catch (SQLException | InterruptedException exception) {
                LOGGER.log(Level.WARNING, "MySQL operation failed at attempt " + i + " of " + MAX_ATTEMPTS + " with " + exception.getClass().getSimpleName(), exception);
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error closing connection.", e);
                    }
                    releaseConnection(openConnection());
                }
            }
        }
        throw new DatabaseException("MySQL operation failed in all " + MAX_ATTEMPTS + " attempts.");
    }

    /**
     * Setzt die Parameter eines PreparedStatements.
     */
    private void setStatementParameters(PreparedStatement statement, Object... values) {
        for (int i = 0; i < values.length; i++) {
            try {
                statement.setObject(i + 1, values[i]);
            } catch (SQLException exception) {
                throw new DatabaseException("An error occurred while setting the parameters for the prepared statement.", exception);
            }
        }
    }

    /**
     * Erzeugt eine aussagekräftige Fehlermeldung.
     */
    private String getErrorMessage(String query, Object... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if (i < values.length - 1)
                builder.append(", ");
        }
        return String.format("An error occurred while executing the query '%s' with parameters '%s'", query, builder);
    }

    public TableBuilder createTable(String name) {
        return new TableBuilder(this, name);
    }

    public Table getTable(String name, String identifier) {
        for (Table table : tables) {
            if (table.getName().equals(name))
                return table;
        }
        Table newTable = new Table(this, name, identifier);
        tables.add(newTable);
        return newTable;
    }

    /**
     * Führt eine Abfrage asynchron aus.
     */
    public CompletableFuture<DatabaseResultMySQL> executeQuery(String query, Object... values) {
        if (values == null)
            throw new IllegalArgumentException("The values array cannot be null");
        return CompletableFuture.supplyAsync(() -> {
            final DatabaseResultMySQL[] result = new DatabaseResultMySQL[1];
            try {
                runSecureOperation(connection -> {
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        setStatementParameters(statement, values);
                        try (ResultSet resultSet = statement.executeQuery()) {
                            result[0] = new DatabaseResultMySQL(resultSet);
                        }
                    }
                });
            } catch (Exception exception) {
                throw new CompletionException(getErrorMessage(query, values), exception);
            }
            return result[0];
        }, executor);
    }

    /**
     * Führt ein Update asynchron aus.
     */
    public CompletableFuture<Void> executeUpdate(String query, Object... values) {
        if (values == null)
            throw new IllegalArgumentException("The values array cannot be null");
        return CompletableFuture.runAsync(() -> {
            try {
                runSecureOperation(connection -> {
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        setStatementParameters(statement, values);
                        statement.executeUpdate();
                    }
                });
            } catch (Exception exception) {
                throw new CompletionException(getErrorMessage(query, values), exception);
            }
        }, executor);
    }

    /**
     * Führt mehrere Operationen in einer Transaktion aus.
     *
     * @param operations Eine Operation, die mehrere Datenbankzugriffe kapselt.
     */
    public void executeTransaction(DatabaseOperationMySQL operations) {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);
            operations.executeOperation(connection);
            connection.commit();
        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Rollback failed.", e);
                }
            }
            throw new DatabaseException("Transaction failed.", exception);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection after transaction.", e);
                }
            }
        }
    }

    /**
     * Schließt alle Verbindungen und den Executor.
     */
    public void shutdown() {
        try {
            for (Connection connection : connectionPool) {
                connection.close();
            }
            connectionPool.clear();
            executor.shutdown();
        } catch (SQLException exception) {
            throw new DatabaseException("Error while closing database connections during shutdown.", exception);
        }
    }
}
