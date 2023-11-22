package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TCP")
public class TCP {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int ID;

    @ColumnInfo(name = "PrimaryHostIP")
    private String PrimaryHostIP;

    @ColumnInfo(name = "SecondaryHostIP")
    private String SecondaryHostIP;

    @ColumnInfo(name = "PrimaryPort")
    private int PrimaryPort;

    @ColumnInfo(name = "SecondaryPort")
    private int SecondaryPort;

    @ColumnInfo(name = "IpTries")
    private int IpTries;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getPrimaryHostIP() {
        return PrimaryHostIP;
    }

    public void setPrimaryHostIP(String primaryHostIP) {
        PrimaryHostIP = primaryHostIP;
    }

    public String getSecondaryHostIP() {
        return SecondaryHostIP;
    }

    public void setSecondaryHostIP(String secondaryHostIP) {
        SecondaryHostIP = secondaryHostIP;
    }

    public int getPrimaryPort() {
        return PrimaryPort;
    }

    public void setPrimaryPort(int primaryPort) {
        PrimaryPort = primaryPort;
    }

    public int getSecondaryPort() {
        return SecondaryPort;
    }

    public void setSecondaryPort(int secondaryPort) {
        SecondaryPort = secondaryPort;
    }

    public int getIpTries() {
        return IpTries;
    }

    public void setIpTries(int ipTries) {
        IpTries = ipTries;
    }
}
