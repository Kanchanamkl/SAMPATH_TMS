package com.epic.pos.domain.entity;

public class ColumnInfoEntity {

    private int columnId;
    private String table;
    private String columnName;
    private String type;
    private boolean isNotNull;
    private boolean isPrimaryKey;
    private String value;

    public ColumnInfoEntity(){
        super();
    }

    public ColumnInfoEntity(int columnId, String columnName, String type, boolean isNotNull, boolean isPrimaryKey) {
        this.columnId = columnId;
        this.columnName = columnName;
        this.type = type;
        this.isNotNull = isNotNull;
        this.isPrimaryKey = isPrimaryKey;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public void setNotNull(boolean notNull) {
        isNotNull = notNull;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "ColumnInfoEntity{" +
                "columnId=" + columnId +
                ", columnName='" + columnName + '\'' +
                ", type='" + type + '\'' +
                ", isNotNull=" + isNotNull +
                ", isPrimaryKey=" + isPrimaryKey +
                ", value='" + value + '\'' +
                '}';
    }
}
