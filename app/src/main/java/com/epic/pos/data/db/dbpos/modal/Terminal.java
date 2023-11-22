package com.epic.pos.data.db.dbpos.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "TMIF")
public class Terminal implements Parcelable {

    public Terminal(){
        super();
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int ID;

    @ColumnInfo(name = "TerminalID")
    private String TerminalID;

    @ColumnInfo(name = "NII")
    private String NII;

    @ColumnInfo(name = "TPDU")
    private String TPDU;

    @ColumnInfo(name = "SecureNII")
    private String SecureNII;

    @ColumnInfo(name = "IssuerNumber")
    private int IssuerNumber;

    @ColumnInfo(name = "HostId")
    private int HostId;

    @ColumnInfo(name = "MerchantNumber")
    private int MerchantNumber;

    @ColumnInfo(name = "IsQrSupport")
    private int IsQrSupport;

    protected Terminal(Parcel in) {
        ID = in.readInt();
        TerminalID = in.readString();
        NII = in.readString();
        TPDU = in.readString();
        SecureNII = in.readString();
        IssuerNumber = in.readInt();
        HostId = in.readInt();
        MerchantNumber = in.readInt();
        IsQrSupport = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(TerminalID);
        dest.writeString(NII);
        dest.writeString(TPDU);
        dest.writeString(SecureNII);
        dest.writeInt(IssuerNumber);
        dest.writeInt(HostId);
        dest.writeInt(MerchantNumber);
        dest.writeInt(IsQrSupport);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Terminal> CREATOR = new Creator<Terminal>() {
        @Override
        public Terminal createFromParcel(Parcel in) {
            return new Terminal(in);
        }

        @Override
        public Terminal[] newArray(int size) {
            return new Terminal[size];
        }
    };

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTerminalID() {
        return TerminalID;
    }

    public void setTerminalID(String terminalID) {
        TerminalID = terminalID;
    }

    public String getNII() {
        return NII;
    }

    public void setNII(String NII) {
        this.NII = NII;
    }

    public String getTPDU() {
        return TPDU;
    }

    public void setTPDU(String TPDU) {
        this.TPDU = TPDU;
    }

    public String getSecureNII() {
        return SecureNII;
    }

    public void setSecureNII(String secureNII) {
        SecureNII = secureNII;
    }

    public int getIssuerNumber() {
        return IssuerNumber;
    }

    public void setIssuerNumber(int issuerNumber) {
        IssuerNumber = issuerNumber;
    }

    public int getHostId() {
        return HostId;
    }

    public void setHostId(int hostId) {
        HostId = hostId;
    }

    public int getMerchantNumber() {
        return MerchantNumber;
    }

    public void setMerchantNumber(int merchantNumber) {
        MerchantNumber = merchantNumber;
    }

    public int getIsQrSupport() {
        return IsQrSupport;
    }

    public void setIsQrSupport(int isQrSupport) {
        IsQrSupport = isQrSupport;
    }


}
