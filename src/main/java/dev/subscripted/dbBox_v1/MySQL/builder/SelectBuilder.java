package dev.subscripted.dbBox_v1.MySQL.builder;

import dev.subscripted.dbBox_v1.MySQL.src.DatabaseResultMySQL;
import dev.subscripted.dbBox_v1.MySQL.src.DatasourceManagerMySQL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SelectBuilder {

    private final DatasourceManagerMySQL dbManager;
    private final String tableName;
    private final List<String> columns = new ArrayList<>();
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();

    public SelectBuilder(DatasourceManagerMySQL dbManager, String tableName) {
        this.dbManager = dbManager;
        this.tableName = tableName;
    }

    /**
     * Gibt die zu selektierenden Spalten an.
     * Wird keine Spalte angegeben, wird "*" verwendet.
     */
    public SelectBuilder columns(String... cols) {
        for (String col : cols) {
            columns.add(col);
        }
        return this;
    }

    /**
     * Fügt eine WHERE-Bedingung hinzu, z. B. where("id", 42) ergibt "WHERE id = ?"
     */
    public SelectBuilder where(String column, Object value) {
        conditions.add(column + " = ?");
        parameters.add(value);
        return this;
    }

    /**
     * Ergänzende Methode, falls mehrere Bedingungen verknüpft werden sollen.
     */
    public SelectBuilder and(String column, Object value) {
        return where(column, value);
    }

    /**
     * Führt die SELECT-Abfrage asynchron aus.
     */
    public CompletableFuture<DatabaseResultMySQL> execute() {
        StringBuilder query = new StringBuilder("SELECT ");
        if (columns.isEmpty()) {
            query.append("*");
        } else {
            for (int i = 0; i < columns.size(); i++) {
                query.append(columns.get(i));
                if (i < columns.size() - 1) {
                    query.append(", ");
                }
            }
        }
        query.append(" FROM ").append(tableName);
        if (!conditions.isEmpty()) {
            query.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                query.append(conditions.get(i));
                if (i < conditions.size() - 1) {
                    query.append(" AND ");
                }
            }
        }
        return dbManager.executeQuery(query.toString(), parameters.toArray());
    }
}
