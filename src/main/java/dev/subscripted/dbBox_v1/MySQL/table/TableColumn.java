package dev.subscripted.dbBox_v1.MySQL.table;

public class TableColumn {

    private final String name;
    private final TableDataType dataType;

    public TableColumn(String name, String dataType) {
        this.name = name;
        this.dataType = TableDataType.valueOf(dataType);
    }

    public TableColumn(String name, TableDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public TableDataType getDataType() {
        return dataType;
    }
}