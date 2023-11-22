package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//Card Definition Table
@Entity(tableName = "CDT")
public class CardDefinition {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    @ColumnInfo(name = "PANLow")
    private String panLow;

    @ColumnInfo(name = "PANHigh")
    private String panHigh;

    @ColumnInfo(name = "CardAbbre")
    private String cardAbbreviation;

    @ColumnInfo(name = "CardLable")
    private String cardLabel;

    @ColumnInfo(name = "TrackRequired")
    private String trackRequired;

    @ColumnInfo(name = "TxnBitmap")
    private String txnBitmap;

    @ColumnInfo(name = "FloorLimit")
    private int floorLimit;

    @ColumnInfo(name = "HostIndex")
    private int hostIndex;

    @ColumnInfo(name = "HostGroup")
    private int hostGroup;

    @ColumnInfo(name = "MinPANDigit")
    private int minPanDigit;

    @ColumnInfo(name = "MaxPANDigit")
    private int maxPanDigit;

    @ColumnInfo(name = "IssuerNumber")
    private int issuerNumber;

    @ColumnInfo(name = "CheckLuhn")
    private int checkLuhn;

    @ColumnInfo(name = "ExpDateRequired")
    private int expDateRequired;

    @ColumnInfo(name = "ManualEntry")
    private int manualEntry;

    @ColumnInfo(name = "ChkSvcCode")
    private int chkSvcCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPanLow() {
        return panLow;
    }

    public void setPanLow(String panLow) {
        this.panLow = panLow;
    }

    public String getPanHigh() {
        return panHigh;
    }

    public void setPanHigh(String panHigh) {
        this.panHigh = panHigh;
    }

    public String getCardAbbreviation() {
        return cardAbbreviation;
    }

    public void setCardAbbreviation(String cardAbbreviation) {
        this.cardAbbreviation = cardAbbreviation;
    }

    public String getCardLabel() {
        return cardLabel;
    }

    public void setCardLabel(String cardLabel) {
        this.cardLabel = cardLabel;
    }

    public String getTrackRequired() {
        return trackRequired;
    }

    public void setTrackRequired(String trackRequired) {
        this.trackRequired = trackRequired;
    }

    public String getTxnBitmap() {
        return txnBitmap;
    }

    public void setTxnBitmap(String txnBitmap) {
        this.txnBitmap = txnBitmap;
    }

    public int getFloorLimit() {
        return floorLimit;
    }

    public void setFloorLimit(int floorLimit) {
        this.floorLimit = floorLimit;
    }

    public int getHostIndex() {
        return hostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.hostIndex = hostIndex;
    }

    public int getHostGroup() {
        return hostGroup;
    }

    public void setHostGroup(int hostGroup) {
        this.hostGroup = hostGroup;
    }

    public int getMinPanDigit() {
        return minPanDigit;
    }

    public void setMinPanDigit(int minPanDigit) {
        this.minPanDigit = minPanDigit;
    }

    public int getMaxPanDigit() {
        return maxPanDigit;
    }

    public void setMaxPanDigit(int maxPanDigit) {
        this.maxPanDigit = maxPanDigit;
    }

    public int getIssuerNumber() {
        return issuerNumber;
    }

    public void setIssuerNumber(int issuerNumber) {
        this.issuerNumber = issuerNumber;
    }

    public int getCheckLuhn() {
        return checkLuhn;
    }

    public void setCheckLuhn(int checkLuhn) {
        this.checkLuhn = checkLuhn;
    }

    public int getExpDateRequired() {
        return expDateRequired;
    }

    public void setExpDateRequired(int expDateRequired) {
        this.expDateRequired = expDateRequired;
    }

    public int getManualEntry() {
        return manualEntry;
    }

    public void setManualEntry(int manualEntry) {
        this.manualEntry = manualEntry;
    }

    public int getChkSvcCode() {
        return chkSvcCode;
    }

    public void setChkSvcCode(int chkSvcCode) {
        this.chkSvcCode = chkSvcCode;
    }

    @Override
    public String toString() {
        return "CDT{" +
                "id=" + id +
                ", panLow='" + panLow + '\'' +
                ", panHigh='" + panHigh + '\'' +
                ", cardAbbreviation='" + cardAbbreviation + '\'' +
                ", cardLabel='" + cardLabel + '\'' +
                ", trackRequired='" + trackRequired + '\'' +
                ", txnBitmap='" + txnBitmap + '\'' +
                ", floorLimit=" + floorLimit +
                ", hostIndex=" + hostIndex +
                ", hostGroup=" + hostGroup +
                ", minPanDigit=" + minPanDigit +
                ", maxPanDigit=" + maxPanDigit +
                ", issuerNumber=" + issuerNumber +
                ", checkLuhn=" + checkLuhn +
                ", expDateRequired=" + expDateRequired +
                ", manualEntry=" + manualEntry +
                ", chkSvcCode=" + chkSvcCode +
                '}';
    }
}
