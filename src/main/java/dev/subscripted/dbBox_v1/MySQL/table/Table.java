package dev.subscripted.dbBox_v1.MySQL.table;

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

    private CompletableFuture<Void> findColumns() {
        String query = "SELECT * FROM " + name;
        return databaseManager.executeQuery(query).thenAccept(resultSet -> {
            for(int i = 0; i < resultSet.getColumnCount(); i++) {
                tableColumns.add(new TableColumn(resultSet.getColumnName(i), resultSet.getColumnType(i)));
            }
        });
    }

    private int getColumnAsIndex(String column) {
        for(int i = 0; i < tableColumns.size(); i++) {
            if(tableColumns.get(i).getName().equals(column))
                return i;
        }
        return -1;
    }

    private CompletableFuture<TableCachedEntry> load(String key) {
        String query = "SELECT * FROM " + name + " WHERE " + identifier + " = ?";
        return databaseManager.executeQuery(query, key).thenApply(resultSet -> {
            TableCachedEntry entry = new TableCachedEntry(System.currentTimeMillis());
            try {
                if(resultSet.next()) {
                    for(int i = 0; i < tableColumns.size(); i++) {
                        entry.getEntries().add(new TableEntry(tableColumns.get(i), resultSet.result().getObject(i + 1)));
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
        if(cachedEntries.containsKey(key))
            return CompletableFuture.completedFuture(cachedEntries.get(key).getEntries().get(getColumnAsIndex(column)));
        return load(key).thenApply(cachedEntry -> cachedEntry.getEntries().get(getColumnAsIndex(column)));
    }

    public void set(String key, String column, Object value) {
        if(cachedEntries.containsKey(key)) {
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
        if(index == -1)
            throw new RuntimeException("table '" + name + "' contains no column '" + column + "'");
        String query = "SELECT * FROM " + name;
        return databaseManager.executeQuery(query).thenApply(resultSet -> {
            List<Object> result = new ArrayList<>();
            try {
                while(resultSet.next()) {
                    TableEntry entry = new TableEntry(tableColumns.get(index), resultSet.result().getObject(index));
                    if(entry.compare(value))
                        result.add(resultSet.result().getObject(identifier));
                }
            } catch (SQLException exception) {
                throw new RuntimeException("An error occurred while filtering the table '" + name + "'", exception);
            }
            return result;
        });
    }

    public void update(String key) {
        if(!cachedEntries.containsKey(key))
            throw new RuntimeException("The key '" + key + "' does not exist in the memory");
        StringBuilder query = new StringBuilder("INSERT INTO " + name + " VALUES (");
        for(int i = 0; i < tableColumns.size(); i++) {
            query.append("?");
            if(i < tableColumns.size() - 1)
                query.append(", ");
        }
        query.append(") ON DUPLICATE KEY UPDATE ");
        for(int i = 0; i < tableColumns.size(); i++) {
            query.append(tableColumns.get(i).getName());
            query.append(" = ?");
            if(i < tableColumns.size() - 1)
                query.append(", ");
        }
        Object[] values = new Object[tableColumns.size() - 1];
        for(int i = 0; i < tableColumns.size(); i++) {
            values[i] = cachedEntries.get(key).getEntries().get(i);
        }
        databaseManager.executeUpdate(query.toString(), values);
        unload(key);
    }
}