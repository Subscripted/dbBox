package dev.subscripted.dbBox_v1.MySQL.table;

import dev.subscripted.dbBox_v1.MySQL.src.DatasourceManagerMySQL;
import dev.subscripted.dbBox_v1.MySQL.exception.DatabaseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Hilfsklasse zum Erstellen von Tabellen.
 */
public class TableBuilder {

    private final DatasourceManagerMySQL databaseManager;
    private final String name;
    private final List<TableColumn> tableColumns;

    public TableBuilder(DatasourceManagerMySQL databaseManager, String name) {
        this.databaseManager = databaseManager;
        if (!name.matches("^[a-zA-Z0-9_]+$")) {
            throw new DatabaseException("Invalid table name: " + name);
        }
        this.name = name;
        this.tableColumns = new ArrayList<>();
    }

    public TableBuilder addString(String name) {
        tableColumns.add(new TableColumn(name, TableDataType.STRING));
        return this;
    }

    public TableBuilder addInt(String name) {
        tableColumns.add(new TableColumn(name, TableDataType.INT));
        return this;
    }

    public TableBuilder addLong(String name) {
        tableColumns.add(new TableColumn(name, TableDataType.LONG));
        return this;
    }

    public TableBuilder addFloat(String name) {
        tableColumns.add(new TableColumn(name, TableDataType.FLOAT));
        return this;
    }

    public TableBuilder addDouble(String name) {
        tableColumns.add(new TableColumn(name, TableDataType.DOUBLE));
        return this;
    }

    public TableBuilder addBoolean(String name) {
        tableColumns.add(new TableColumn(name, TableDataType.BOOLEAN));
        return this;
    }

    /**
     * Erstellt die Tabelle in der Datenbank.
     */
    public void create() {
        // Da SQL-Platzhalter für Tabellennamen nicht funktionieren,
        // wird hier der Tabellenname direkt in den Query eingebaut.
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS `")
                .append(name)
                .append("` (");

        for (int i = 0; i < tableColumns.size(); i++) {
            TableColumn column = tableColumns.get(i);
            query.append("`").append(column.getName()).append("` ")
                    .append(column.getDataType().getMysqlType());
            if (i < tableColumns.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");


        databaseManager.executeUpdate(query.toString())
                .exceptionally(ex -> {
                    throw new DatabaseException("Error creating table " + name, ex);
                });
    }
}
