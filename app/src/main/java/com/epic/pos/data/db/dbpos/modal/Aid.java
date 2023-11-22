package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AID")
public class Aid {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "IssuerID")
    private int IssuerID;

    @ColumnInfo(name = "IssuerName")
    private String IssuerName;

    @ColumnInfo(name = "FLAmt")
    private String FLAmt;

    @ColumnInfo(name = "TxnAmt")
    private String TxnAmt;

    @ColumnInfo(name = "CVMLAmt")
    private String CVMLAmt;

    //Contact less pin support or not
    @ColumnInfo(name = "CTLSPinSupport")
    private int CTLSPinSupport;

    //Contact less pin support value for 9F3303
    @ColumnInfo(name = "CTLSPinSupportValue")
    private String CTLSPinSupportValue;

    //Contact pin support or not
    @ColumnInfo(name = "PinSupport")
    private int PinSupport;

    //Contact pin support value for 9F3303
    @ColumnInfo(name = "PinSupportValue")
    private String PinSupportValue;

    //Contact Less Terminal Transaction Qualifier Value 9F6604
    @ColumnInfo(name = "TTQCTLSValue")
    private String TTQCTLSValue;

    //Contact Terminal Transaction Qualifier Value 9F6604
    @ColumnInfo(name = "TTQValue")
    private String TTQValue;

    public int getIssuerID() {
        return IssuerID;
    }

    public void setIssuerID(int issuerID) {
        IssuerID = issuerID;
    }

    public String getIssuerName() {
        return IssuerName;
    }

    public void setIssuerName(String issuerName) {
        IssuerName = issuerName;
    }

    public String getFLAmt() {
        return FLAmt;
    }

    public void setFLAmt(String FLAmt) {
        this.FLAmt = FLAmt;
    }

    public String getTxnAmt() {
        return TxnAmt;
    }

    public void setTxnAmt(String txnAmt) {
        TxnAmt = txnAmt;
    }

    public String getCVMLAmt() {
        return CVMLAmt;
    }

    public void setCVMLAmt(String CVMLAmt) {
        this.CVMLAmt = CVMLAmt;
    }

    public int getCTLSPinSupport() {
        return CTLSPinSupport;
    }

    public void setCTLSPinSupport(int CTLSPinSupport) {
        this.CTLSPinSupport = CTLSPinSupport;
    }

    public String getCTLSPinSupportValue() {
        return CTLSPinSupportValue;
    }

    public void setCTLSPinSupportValue(String CTLSPinSupportValue) {
        this.CTLSPinSupportValue = CTLSPinSupportValue;
    }

    public int getPinSupport() {
        return PinSupport;
    }

    public void setPinSupport(int pinSupport) {
        PinSupport = pinSupport;
    }

    public String getPinSupportValue() {
        return PinSupportValue;
    }

    public void setPinSupportValue(String pinSupportValue) {
        PinSupportValue = pinSupportValue;
    }

    public String getTTQCTLSValue() {
        return TTQCTLSValue;
    }

    public void setTTQCTLSValue(String TTQCTLSValue) {
        this.TTQCTLSValue = TTQCTLSValue;
    }

    public String getTTQValue() {
        return TTQValue;
    }

    public void setTTQValue(String TTQValue) {
        this.TTQValue = TTQValue;
    }
}
