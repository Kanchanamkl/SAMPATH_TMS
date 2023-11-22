package com.epic.pos.domain.entity;

public class ConfigMapEntity {

    private String tableName;
    private String columnName;
    private int rowIndex;

    public ConfigMapEntity(String tableName, String columnName, int rowIndex) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.rowIndex = rowIndex;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
