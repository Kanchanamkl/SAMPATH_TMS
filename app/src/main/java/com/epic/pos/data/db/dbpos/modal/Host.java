package com.epic.pos.data.db.dbpos.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "IHT")

    public class Host implements Parcelable {

    public Host() {
    }

    @Ignore
    private boolean isSelected;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "HostID")
    private int HostID;

    @ColumnInfo(name = "HostName")
    private String HostName;

    @ColumnInfo(name = "IssuerList")
    private String IssuerList;

    @ColumnInfo(name = "BaseIssuer")
    private int BaseIssuer;

    @ColumnInfo(name = "MustSettleFlag")
    private int MustSettleFlag;

    @ColumnInfo(name = "TLEEnabled")
    private int TLEEnabled;

    @ColumnInfo(name = "MasterKeyId")
    private int MasterKeyId;

    @ColumnInfo(name = "WorkKeyId")
    private int WorkKeyId;

    @ColumnInfo(name = "WorkKey")
    private String WorkKey;

    protected Host(Parcel in) {
        HostID = in.readInt();
        HostName = in.readString();
        IssuerList = in.readString();
        BaseIssuer = in.readInt();
        MustSettleFlag = in.readInt();
        TLEEnabled = in.readInt();
        MasterKeyId = in.readInt();
        WorkKeyId = in.readInt();
        WorkKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(HostID);
        dest.writeString(HostName);
        dest.writeString(IssuerList);
        dest.writeInt(BaseIssuer);
        dest.writeInt(MustSettleFlag);
        dest.writeInt(TLEEnabled);
        dest.writeInt(MasterKeyId);
        dest.writeInt(WorkKeyId);
        dest.writeString(WorkKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Host> CREATOR = new Creator<Host>() {
        @Override
        public Host createFromParcel(Parcel in) {
            return new Host(in);
        }

        @Override
        public Host[] newArray(int size) {
            return new Host[size];
        }
    };

    public int getHostID() {
        return HostID;
    }

    public void setHostID(int hostID) {
        HostID = hostID;
    }

    public String getHostName() {
        return HostName;
    }

    public void setHostName(String hostName) {
        HostName = hostName;
    }

    public String getIssuerList() {
        return IssuerList;
    }

    public void setIssuerList(String issuerList) {
        IssuerList = issuerList;
    }

    public int getBaseIssuer() {
        return BaseIssuer;
    }

    public void setBaseIssuer(int baseIssuer) {
        BaseIssuer = baseIssuer;
    }

    public int getMustSettleFlag() {
        return MustSettleFlag;
    }

    public void setMustSettleFlag(int mustSettleFlag) {
        MustSettleFlag = mustSettleFlag;
    }

    public int getTLEEnabled() {
        return TLEEnabled;
    }

    public void setTLEEnabled(int TLEEnabled) {
        this.TLEEnabled = TLEEnabled;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getMasterKeyId() {
        return MasterKeyId;
    }

    public void setMasterKeyId(int masterKeyId) {
        MasterKeyId = masterKeyId;
    }

    public int getWorkKeyId() {
        return WorkKeyId;
    }

    public void setWorkKeyId(int workKeyId) {
        WorkKeyId = workKeyId;
    }

    public String getWorkKey() {
        return WorkKey;
    }

    public void setWorkKey(String workKey) {
        WorkKey = workKey;
    }

    @Override
    public String toString() {
        return "Host{" +
                "isSelected=" + isSelected +
                ", HostID=" + HostID +
                ", HostName='" + HostName + '\'' +
                ", IssuerList='" + IssuerList + '\'' +
                ", BaseIssuer=" + BaseIssuer +
                ", MustSettleFlag=" + MustSettleFlag +
                ", TLEEnabled=" + TLEEnabled +
                ", MasterKeyId=" + MasterKeyId +
                ", WorkKeyId=" + WorkKeyId +
                '}';
    }
}
