package com.epic.pos.data.db.dbpos.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "IIT")
public class Issuer implements Parcelable {

    public Issuer() {
    }

    @PrimaryKey()
    @ColumnInfo(name = "IssuerNumber")
    private int IssuerNumber;

    @ColumnInfo(name = "IssuerAbbrev")
    private String IssuerAbbrev;

    @ColumnInfo(name = "IssuerLable")
    private String IssuerLable;

    @ColumnInfo(name = "MaskCustomerCopy")
    private String MaskCustomerCopy;

    @ColumnInfo(name = "MaskMerchantCopy")
    private String MaskMerchantCopy;

    @ColumnInfo(name = "MaskExpireDate")
    private String MaskExpireDate;

    @ColumnInfo(name = "MaskDisplay")
    private String MaskDisplay;

    @ColumnInfo(name = "PanFormat")
    private String PanFormat;

    @ColumnInfo(name = "IP")
    private String IP;

    @ColumnInfo(name = "Port")
    private int Port;

    @ColumnInfo(name = "PINBypass")
    private int PINBypass;

    protected Issuer(Parcel in) {
        IssuerNumber = in.readInt();
        IssuerAbbrev = in.readString();
        IssuerLable = in.readString();
        MaskCustomerCopy = in.readString();
        MaskMerchantCopy = in.readString();
        MaskExpireDate = in.readString();
        MaskDisplay = in.readString();
        PanFormat = in.readString();
        IP = in.readString();
        Port = in.readInt();
        PINBypass = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(IssuerNumber);
        dest.writeString(IssuerAbbrev);
        dest.writeString(IssuerLable);
        dest.writeString(MaskCustomerCopy);
        dest.writeString(MaskMerchantCopy);
        dest.writeString(MaskExpireDate);
        dest.writeString(MaskDisplay);
        dest.writeString(PanFormat);
        dest.writeString(IP);
        dest.writeInt(Port);
        dest.writeInt(PINBypass);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Issuer> CREATOR = new Creator<Issuer>() {
        @Override
        public Issuer createFromParcel(Parcel in) {
            return new Issuer(in);
        }

        @Override
        public Issuer[] newArray(int size) {
            return new Issuer[size];
        }
    };

    public int getIssuerNumber() {
        return IssuerNumber;
    }

    public void setIssuerNumber(int issuerNumber) {
        IssuerNumber = issuerNumber;
    }

    public String getIssuerAbbrev() {
        return IssuerAbbrev;
    }

    public void setIssuerAbbrev(String issuerAbbrev) {
        IssuerAbbrev = issuerAbbrev;
    }

    public String getIssuerLable() {
        return IssuerLable;
    }

    public void setIssuerLable(String issuerLable) {
        IssuerLable = issuerLable;
    }

    public String getMaskCustomerCopy() {
        return MaskCustomerCopy;
    }

    public void setMaskCustomerCopy(String maskCustomerCopy) {
        MaskCustomerCopy = maskCustomerCopy;
    }

    public String getMaskMerchantCopy() {
        return MaskMerchantCopy;
    }

    public void setMaskMerchantCopy(String maskMerchantCopy) {
        MaskMerchantCopy = maskMerchantCopy;
    }

    public String getMaskExpireDate() {
        return MaskExpireDate;
    }

    public void setMaskExpireDate(String maskExpireDate) {
        MaskExpireDate = maskExpireDate;
    }

    public String getMaskDisplay() {
        return MaskDisplay;
    }

    public void setMaskDisplay(String maskDisplay) {
        MaskDisplay = maskDisplay;
    }

    public String getPanFormat() {
        return PanFormat;
    }

    public void setPanFormat(String panFormat) {
        PanFormat = panFormat;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }

    public int getPINBypass() {
        return PINBypass;
    }

    public void setPINBypass(int PINBypass) {
        this.PINBypass = PINBypass;
    }
}
