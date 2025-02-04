package dev.subscripted.dbBox_v1.MySQL.table;

public enum TableDataType {
    STRING(String.class, "VARCHAR", ""),
    INT(Integer.class, "INT", 0),
    LONG(Long.class, "BIGINT", 0L),
    FLOAT(Float.class, "FLOAT", 0.0f),
    DOUBLE(Double.class, "DOUBLE", 0.0d),
    BOOLEAN(Boolean.class, "BOOLEAN", false);

    private final Class javaType;
    private final String mysqlType;
    private final Object defaultValue;

    TableDataType(Class javaType, String mysqlType, Object defaultValue) {
        this.javaType = javaType;
        this.mysqlType = mysqlType;
        this.defaultValue = defaultValue;
    }

    public Class getJavaType() {
        return javaType;
    }

    public String getMysqlType() {
        return mysqlType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
