package dev.subscripted.dbBox_v1.MySQL.table;

public class TableEntry {

    private final TableDataType dataType;
    private Object value;

    public TableEntry(TableColumn column, Object value) {
        this.dataType = column.getDataType();
        this.value = value;
    }

    public boolean compare(Object other) {
        if (dataType.getJavaType().equals(other.getClass())) {
            return value.equals(other);
        }
        return false;
    }

    public void update(Object value) {
        if (value == null) {
            throw new RuntimeException("the table entry cannot be null");
        }
        if (!dataType.getJavaType().equals(value.getClass())) {
            throw new RuntimeException("can not set " + value.getClass().getName() + " to " + dataType.getJavaType().getName() + " entry");
        }
        this.value = value;
    }

    public boolean isString() {
        return dataType.equals(TableDataType.STRING);
    }

    public String asString() {
        if (!isString()) {
            throw new RuntimeException("failed get non string entry as string");
        }
        return (String) value;
    }

    public boolean isInt() {
        return dataType.equals(TableDataType.INT);
    }

    public int asInt() {
        if (!isInt()) {
            throw new RuntimeException("failed get non integer entry as integer");
        }
        return (int) value;
    }

    public boolean isLong() {
        return dataType.equals(TableDataType.LONG);
    }

    public long asLong() {
        if (!isLong()) {
            throw new RuntimeException("failed get non long entry as long");
        }
        return (long) value;
    }

    public boolean isFloat() {
        return dataType.equals(TableDataType.FLOAT);
    }

    public float asFloat() {
        if (!isFloat()) {
            throw new RuntimeException("failed get non float entry as float");
        }
        return (float) value;
    }

    public boolean isDouble() {
        return dataType.equals(TableDataType.DOUBLE);
    }

    public double asDouble() {
        if (!isDouble()) {
            throw new RuntimeException("failed get non double entry as double");
        }
        return (double) value;
    }

    public boolean isBoolean() {
        return dataType.equals(TableDataType.BOOLEAN);
    }

    public boolean asBoolean() {
        if (!isBoolean()) {
            throw new RuntimeException("failed get non boolean entry as boolean");
        }
        return (boolean) value;
    }
}