package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "ConfigMap")
public class ConfigMap {

    public ConfigMap() {
    }

    public ConfigMap(@NotNull String parameterName, String tableName, String columnName, int rowIndex, String value) {
        ParameterName = parameterName;
        TableName = tableName;
        ColumnName = columnName;
        RowIndex = rowIndex;
        Value = value;
    }

    @PrimaryKey()
    @ColumnInfo(name = "ParameterName")
    @NotNull
    private String ParameterName;

    @ColumnInfo(name = "TableName")
    private String TableName;

    @ColumnInfo(name = "ColumnName")
    private String ColumnName;

    @ColumnInfo(name = "RowIndex")
    private int RowIndex;

    @ColumnInfo(name = "Value")
    private String Value;

    public String getParameterName() {
        return ParameterName;
    }

    public void setParameterName(String parameterName) {
        ParameterName = parameterName;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }

    public String getColumnName() {
        return ColumnName;
    }

    public void setColumnName(String columnName) {
        ColumnName = columnName;
    }

    public int getRowIndex() {
        return RowIndex;
    }

    public void setRowIndex(int rowIndex) {
        RowIndex = rowIndex;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
