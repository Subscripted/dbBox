package dev.subscripted.dbBox_v1.MySQL.table;

import dev.subscripted.dbBox_v1.MySQL.builder.SelectBuilder;
import dev.subscripted.dbBox_v1.MySQL.builder.UpdateBuilder;
import dev.subscripted.dbBox_v1.MySQL.src.DatabaseResultMySQL;
import dev.subscripted.dbBox_v1.MySQL.src.DatasourceManagerMySQL;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Table {

    private final DatasourceManagerMySQL databaseManager;
    private final String name;
    private final String identifier;

    private final List<TableColumn> tableColumns;
    private final Map<String, TableCachedEntry> cachedEntries;

    public Table(DatasourceManagerMySQL databaseManager, String name, String identifier) {
        this.databaseManager = databaseManager;
        this.name = name;
        this.identifier = identifier;
        this.tableColumns = new ArrayList<>();
        this.cachedEntries = new HashMap<>();
        findColumns();
    }


    /**
     * Führt einen SELECT-Query als prepared statement aus.
     * Es werden die übergebenen Spalten, die Tabelle (muss mit dem aktuellen Tabellenname übereinstimmen),
     * die WHERE-Spalte und der Parameterwert verwendet.
     *
     * Beispiel:
     *   table.pselect(data, "kunden", "land", "Deutschland")
     *
     * @param data        Array mit den zu selektierenden Spalten
     * @param tableName      Der Name der Tabelle (muss mit this.name übereinstimmen)
     * @param conditionCol   Die Spalte für die WHERE-Bedingung
     * @param conditionValue Der Wert, der als Parameter übergeben wird
     * @return CompletableFuture mit dem DatabaseResultMySQL
     */
    public CompletableFuture<DatabaseResultMySQL> pselect(String[] data, String tableName, String conditionCol, Object conditionValue) {
        if (!this.name.equals(tableName)) {
            throw new IllegalArgumentException("Der übergebene Tabellenname stimmt nicht mit diesem Table-Objekt überein.");
        }
        // Erstelle einen SelectBuilder, setze die Spalten und füge die WHERE-Bedingung hinzu.
        return new SelectBuilder(databaseManager, name)
                .columns(data)
                .where(conditionCol, conditionValue)
                .execute();
    }

    /**
     * Führt einen SELECT-Query ohne prepared statement aus, indem der Parameterwert direkt in den SQL-String eingebettet wird.
     * Achtung: Hierbei erfolgt keine automatische Escape‑Behandlung – dies ist nur für den internen Gebrauch
     * bzw. bei vertrauenswürdigen Werten zu empfehlen.
     *
     * Beispiel:
     *   table.select(data, "kunden", "land", "Deutschland")
     *
     * @param columns        Array mit den zu selektierenden Spalten
     * @param tableName      Der Name der Tabelle (muss mit this.name übereinstimmen)
     * @param conditionCol   Die Spalte für die WHERE-Bedingung
     * @param conditionValue Der Wert, der direkt in den Query-String eingebettet wird
     * @return CompletableFuture mit dem DatabaseResultMySQL
     */
    public CompletableFuture<DatabaseResultMySQL> select(String[] columns, String tableName, String conditionCol, Object conditionValue) {
        if (!this.name.equals(tableName)) {
            throw new IllegalArgumentException("Der übergebene Tabellenname stimmt nicht mit diesem Table-Objekt überein.");
        }
        StringBuilder query = new StringBuilder("SELECT ");
        if (columns == null || columns.length == 0) {
            query.append("*");
        } else {
            for (int i = 0; i < columns.length; i++) {
                query.append(columns[i]);
                if (i < columns.length - 1) {
                    query.append(", ");
                }
            }
        }
        query.append(" FROM ").append(name)
                .append(" WHERE ").append(conditionCol).append(" = ");
        // Wenn der conditionValue ein String ist, fügen wir Anführungszeichen hinzu.
        if (conditionValue instanceof String) {
            query.append("'").append(conditionValue).append("'");
        } else {
            query.append(conditionValue);
        }
        return databaseManager.executeQuery(query.toString());
    }

    private CompletableFuture<Void> findColumns() {
        String query = "SELECT * FROM " + name;
        return databaseManager.executeQuery(query).thenAccept(resultSet -> {
            for (int i = 0; i < resultSet.getColumnCount(); i++) {
                tableColumns.add(new TableColumn(resultSet.getColumnName(i), resultSet.getColumnType(i)));
            }
        });
    }

    private int getColumnAsIndex(String column) {
        for (int i = 0; i < tableColumns.size(); i++) {
            if (tableColumns.get(i).getName().equals(column))
                return i;
        }
        return -1;
    }

    private CompletableFuture<TableCachedEntry> load(String key) {
        String query = "SELECT * FROM " + name + " WHERE " + identifier + " = ?";
        return databaseManager.executeQuery(query, key).thenApply(resultSet -> {
            TableCachedEntry entry = new TableCachedEntry(System.currentTimeMillis());
            try {
                if (resultSet.next()) {
                    for (int i = 0; i < tableColumns.size(); i++) {
                        // Verwende die neue Methode getResultSet() anstelle von resultSet.result()
                        Object value = resultSet.getResultSet().getObject(i + 1);
                        entry.getEntries().add(new TableEntry(tableColumns.get(i), value));
                    }
                }
            } catch (SQLException exception) {
                throw new CompletionException("Error loading entry with key '" + key + "'", exception);
            }
            cachedEntries.put(key, entry);
            return entry;
        });
    }


    private void unload(String key) {
        cachedEntries.remove(key);
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<TableColumn> getColumns() {
        return tableColumns;
    }

    public boolean isLoaded(String key) {
        return cachedEntries.containsKey(key);
    }

    public CompletableFuture<Boolean> exists(String key) {
        String query = "SELECT * FROM " + name + " WHERE " + identifier + " = ?";
        return databaseManager.executeQuery(query, key).thenApply(DatabaseResultMySQL::next);
    }

    public CompletableFuture<TableEntry> get(String key, String column) {
        if (cachedEntries.containsKey(key))
            return CompletableFuture.completedFuture(cachedEntries.get(key).getEntries().get(getColumnAsIndex(column)));
        return load(key).thenApply(cachedEntry -> cachedEntry.getEntries().get(getColumnAsIndex(column)));
    }

    public void set(String key, String column, Object value) {
        if (cachedEntries.containsKey(key)) {
            cachedEntries.get(key).getEntries().get(getColumnAsIndex(column)).update(value);
            return;
        }
        load(key).thenAccept(cachedEntry -> cachedEntry.getEntries().get(getColumnAsIndex(column)).update(value));
    }

    public void delete(String key) {
        try {
            String query = "DELETE FROM " + name + " WHERE " + identifier + " = ?";
            databaseManager.executeUpdate(query, key);
            cachedEntries.remove(key);
        } catch (Exception exception) {
            throw new RuntimeException("Error occurred while deleting entry with key '" + key + "' from table '" + name + "'", exception);
        }
    }

    public CompletableFuture<List<Object>> filter(String column, Object value) {
        int index = getColumnAsIndex(column);
        if (index == -1)
            throw new RuntimeException("table '" + name + "' contains no column '" + column + "'");
        String query = "SELECT * FROM " + name;
        return databaseManager.executeQuery(query).thenApply(databaseResult -> {
            List<Object> result = new ArrayList<>();
            try {
                while (databaseResult.next()) {
                    // Hinweis: Die Indizierung der Spalten in JDBC beginnt bei 1.
                    // Falls tableColumns.get(index) einem 0-basierten Index entspricht, solltest Du ggf. index+1 verwenden.
                    TableEntry entry = new TableEntry(tableColumns.get(index), databaseResult.getResultSet().getObject(index + 1));
                    if (entry.compare(value))
                        result.add(databaseResult.getResultSet().getObject(identifier));
                }
            } catch (SQLException exception) {
                throw new RuntimeException("An error occurred while filtering the table '" + name + "'", exception);
            }
            return result;
        });
    }


    /**
     * Aktualisiert einen Eintrag in der Datenbank basierend auf dem Cache.
     */
    public void update(String key) {
        if (!cachedEntries.containsKey(key))
            throw new RuntimeException("The key '" + key + "' does not exist in the memory");
        StringBuilder query = new StringBuilder("INSERT INTO " + name + " VALUES (");
        for (int i = 0; i < tableColumns.size(); i++) {
            query.append("?");
            if (i < tableColumns.size() - 1)
                query.append(", ");
        }
        query.append(") ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < tableColumns.size(); i++) {
            query.append(tableColumns.get(i).getName());
            query.append(" = ?");
            if (i < tableColumns.size() - 1)
                query.append(", ");
        }
        Object[] values = new Object[tableColumns.size()];
        for (int i = 0; i < tableColumns.size(); i++) {
            values[i] = cachedEntries.get(key).getEntries().get(i);
        }
        databaseManager.executeUpdate(query.toString(), values);
        unload(key);
    }

    /**
     * Erzeugt einen neuen SelectBuilder für SELECT-Abfragen.
     */
    public SelectBuilder select() {
        return new SelectBuilder(databaseManager, name);
    }

    /**
     * Erzeugt einen neuen UpdateBuilder für UPDATE-Abfragen.
     */
    public UpdateBuilder updateBuilder() {
        return new UpdateBuilder(databaseManager, name);
    }
}
