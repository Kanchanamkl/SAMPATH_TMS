package com.epic.pos.data.db.dccdb.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "BINLIST")
 public class DCCBINNLIST {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    @NotNull
    private int ID;

    @ColumnInfo(name = "PANL")
    @NotNull
    private String PANL;

    @ColumnInfo(name = "PANH")
    @NotNull
    private String PANH;

    @ColumnInfo(name = "CARDTYPE")
    private String CARDTYPE;

    @ColumnInfo(name = "CURRENCY")
    @NotNull
    private int CURRENCY;

    @ColumnInfo(name = "COUNTRY")
    @NotNull
    private int COUNTRY;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    @NotNull
    public String getPANL() {
        return PANL;
    }

    public void setPANL(@NotNull String PANL) {
        this.PANL = PANL;
    }

    @NotNull
    public String getPANH() {
        return PANH;
    }

    public void setPANH(@NotNull String PANH) {
        this.PANH = PANH;
    }

    public String getCARDTYPE() {
        return CARDTYPE;
    }

    public void setCARDTYPE(String CARDTYPE) {
        this.CARDTYPE = CARDTYPE;
    }

    public int getCURRENCY() {
        return CURRENCY;
    }

    public void setCURRENCY(int CURRENCY) {
        this.CURRENCY = CURRENCY;
    }

    public int getCOUNTRY() {
        return COUNTRY;
    }

    public void setCOUNTRY(int COUNTRY) {
        this.COUNTRY = COUNTRY;
    }
}
