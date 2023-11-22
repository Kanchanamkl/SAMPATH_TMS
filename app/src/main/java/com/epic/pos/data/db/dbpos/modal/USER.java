package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "USER")

public class USER  {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int ID;

    @ColumnInfo(name = "UserName")
    private String UserName;

    @ColumnInfo(name = "Password")
    private String Password;

    @ColumnInfo(name = "Status")
    private String Status;

    @ColumnInfo(name = "ManualSale")
    private int ManualSale;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public int getManualSale() {
        return ManualSale;
    }

    public void setManualSale(int manualSale) {
        ManualSale = manualSale;
    }
}
