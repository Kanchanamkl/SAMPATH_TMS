package com.epic.pos.data.db.dccdb.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "DCCData")
 public class DCCData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    @NotNull
    private int ID;

    @ColumnInfo(name = "CCode")
    @NotNull
    private int CCode;

    @ColumnInfo(name = "CSymbol")
    @NotNull
    private String CSymbol;

    @ColumnInfo(name = "EffectiveDate")
    private String EffectiveDate;

    @ColumnInfo(name = "ConversionRate")
    @NotNull
    private String ConversionRate;

    @ColumnInfo(name = "BCCode")
    @NotNull
    private int BCCode;

    @ColumnInfo(name = "Flag")
    private String Flag;

    public int getCCode() {
        return CCode;
    }

    public void setCCode(int CCode) {
        this.CCode = CCode;
    }

    public String getCSymbol() {
        return CSymbol;
    }

    public void setCSymbol(String CSymbol) {
        this.CSymbol = CSymbol;
    }

    public String getEffectiveDate() {
        return EffectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        EffectiveDate = effectiveDate;
    }

    public String getConversionRate() {
        return ConversionRate;
    }

    public void setConversionRate(String conversionRate) {
        ConversionRate = conversionRate;
    }

    public int getBCCode() {
        return BCCode;
    }

    public void setBCCode(int BCCode) {
        this.BCCode = BCCode;
    }

    public String getFlag() {
        return Flag;
    }

    public void setFlag(String flag) {
        Flag = flag;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
