package dev.subscripted.dbBox_v1.MySQL.builder;

import dev.subscripted.dbBox_v1.MySQL.src.DatasourceManagerMySQL;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UpdateBuilder {

    private final DatasourceManagerMySQL dbManager;
    private final String tableName;
    private final Map<String, Object> setValues = new LinkedHashMap<>();
    private String whereClause = "";
    private final Map<Integer, Object> whereParameters = new LinkedHashMap<>();

    public UpdateBuilder(DatasourceManagerMySQL dbManager, String tableName) {
        this.dbManager = dbManager;
        this.tableName = tableName;
    }

    /**
     * Definiert einen Spaltenwert, der aktualisiert werden soll.
     */
    public UpdateBuilder set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }

    /**
     * Fügt eine WHERE-Bedingung hinzu, z. B. where("id = ?", 42)
     */
    public UpdateBuilder where(String clause, Object... params) {
        if (!whereClause.isEmpty()) {
            whereClause += " AND " + clause;
        } else {
            whereClause = clause;
        }
        for (Object param : params) {
            whereParameters.put(whereParameters.size() + 1, param);
        }
        return this;
    }

    /**
     * Führt die UPDATE-Abfrage asynchron aus.
     */
    public CompletableFuture<Void> execute() {
        if (setValues.isEmpty()) {
            throw new IllegalStateException("Keine Spalten zum Aktualisieren angegeben.");
        }
        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        Object[] parameters = new Object[setValues.size() + whereParameters.size()];
        int idx = 0;
        int count = 0;
        for (Map.Entry<String, Object> entry : setValues.entrySet()) {
            query.append(entry.getKey()).append(" = ?");
            parameters[idx++] = entry.getValue();
            count++;
            if (count < setValues.size()) {
                query.append(", ");
            }
        }
        if (!whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
            for (int i = 1; i <= whereParameters.size(); i++) {
                parameters[idx++] = whereParameters.get(i);
            }
        }
        return dbManager.executeUpdate(query.toString(), parameters);
    }
}
