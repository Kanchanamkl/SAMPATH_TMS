package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TLE")
public class TLE {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int ID;

    @ColumnInfo(name = "IssuerNumber")
    private int IssuerNumber;

    @ColumnInfo(name = "AID")
    private String AID;

    @ColumnInfo(name = "VSN")
    private String VSN;

    @ColumnInfo(name = "EAlgo")
    private String EAlgo;

    @ColumnInfo(name = "UKID")
    private String UKID;

    @ColumnInfo(name = "MAlgo")
    private String MAlgo;

    @ColumnInfo(name = "DMTR")
    private String DMTR;

    @ColumnInfo(name = "KSize")
    private String KSize;

    @ColumnInfo(name = "SCSLength")
    private String SCSLength;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getIssuerNumber() {
        return IssuerNumber;
    }

    public void setIssuerNumber(int issuerNumber) {
        IssuerNumber = issuerNumber;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getVSN() {
        return VSN;
    }

    public void setVSN(String VSN) {
        this.VSN = VSN;
    }

    public String getEAlgo() {
        return EAlgo;
    }

    public void setEAlgo(String EAlgo) {
        this.EAlgo = EAlgo;
    }

    public String getUKID() {
        return UKID;
    }

    public void setUKID(String UKID) {
        this.UKID = UKID;
    }

    public String getMAlgo() {
        return MAlgo;
    }

    public void setMAlgo(String MAlgo) {
        this.MAlgo = MAlgo;
    }

    public String getDMTR() {
        return DMTR;
    }

    public void setDMTR(String DMTR) {
        this.DMTR = DMTR;
    }

    public String getKSize() {
        return KSize;
    }

    public void setKSize(String KSize) {
        this.KSize = KSize;
    }

    public String getSCSLength() {
        return SCSLength;
    }

    public void setSCSLength(String SCSLength) {
        this.SCSLength = SCSLength;
    }
}
