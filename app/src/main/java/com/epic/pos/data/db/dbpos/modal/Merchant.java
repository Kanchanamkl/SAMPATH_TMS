package com.epic.pos.data.db.dbpos.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "MIT")
public class Merchant implements Parcelable {

    public Merchant() {
    }

    @Ignore
    private boolean isSelected;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "MerchantNumber")
    private int MerchantNumber;

    @ColumnInfo(name = "MerchantName")
    private String MerchantName;

    @ColumnInfo(name = "RctHdr1")
    private String RctHdr1;

    @ColumnInfo(name = "RctHdr2")
    private String RctHdr2;

    @ColumnInfo(name = "RctHdr3")
    private String RctHdr3;

    @ColumnInfo(name = "MerchantPassword")
    private String MerchantPassword;

    @ColumnInfo(name = "InvNumber")
    private String InvNumber;

    @ColumnInfo(name = "STAN")
    private String STAN;

    @ColumnInfo(name = "MerchantID")
    private String MerchantID;

    @ColumnInfo(name = "BatchNumber")
    private String BatchNumber;

    @ColumnInfo(name = "MobileNumber")
    private String MobileNumber;

    @ColumnInfo(name = "NIC")
    private String NIC;

    @ColumnInfo(name = "MCC")
    private String MCC;

    @ColumnInfo(name = "ContactNumber")
    private String ContactNumber;

    @ColumnInfo(name = "Country")
    private String Country;

    @ColumnInfo(name = "Province")
    private String Province;

    @ColumnInfo(name = "District")
    private String District;

    @ColumnInfo(name = "City")
    private String City;

    @ColumnInfo(name = "Email")
    private String Email;

    @ColumnInfo(name = "Fax")
    private String Fax;

    @ColumnInfo(name = "Remark")
    private String Remark;

    @ColumnInfo(name = "GroupId")
    private int GroupId;

    @ColumnInfo(name = "HostId")
    private int HostId;

    @ColumnInfo(name = "IsInstallment")
    private int IsInstallment;

    @ColumnInfo(name = "IsEnabled")
    private int IsEnabled;

    protected Merchant(Parcel in) {
        isSelected = in.readByte() != 0;
        MerchantNumber = in.readInt();
        MerchantName = in.readString();
        RctHdr1 = in.readString();
        RctHdr2 = in.readString();
        RctHdr3 = in.readString();
        MerchantPassword = in.readString();
        InvNumber = in.readString();
        STAN = in.readString();
        MerchantID = in.readString();
        BatchNumber = in.readString();
        MobileNumber = in.readString();
        NIC = in.readString();
        MCC = in.readString();
        ContactNumber = in.readString();
        Country = in.readString();
        Province = in.readString();
        District = in.readString();
        City = in.readString();
        Email = in.readString();
        Fax = in.readString();
        Remark = in.readString();
        GroupId = in.readInt();
        HostId = in.readInt();
        IsInstallment = in.readInt();
        IsEnabled = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeInt(MerchantNumber);
        dest.writeString(MerchantName);
        dest.writeString(RctHdr1);
        dest.writeString(RctHdr2);
        dest.writeString(RctHdr3);
        dest.writeString(MerchantPassword);
        dest.writeString(InvNumber);
        dest.writeString(STAN);
        dest.writeString(MerchantID);
        dest.writeString(BatchNumber);
        dest.writeString(MobileNumber);
        dest.writeString(NIC);
        dest.writeString(MCC);
        dest.writeString(ContactNumber);
        dest.writeString(Country);
        dest.writeString(Province);
        dest.writeString(District);
        dest.writeString(City);
        dest.writeString(Email);
        dest.writeString(Fax);
        dest.writeString(Remark);
        dest.writeInt(GroupId);
        dest.writeInt(HostId);
        dest.writeInt(IsInstallment);
        dest.writeInt(IsEnabled);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Merchant> CREATOR = new Creator<Merchant>() {
        @Override
        public Merchant createFromParcel(Parcel in) {
            return new Merchant(in);
        }

        @Override
        public Merchant[] newArray(int size) {
            return new Merchant[size];
        }
    };

    public int getMerchantNumber() {
        return MerchantNumber;
    }

    public void setMerchantNumber(int merchantNumber) {
        MerchantNumber = merchantNumber;
    }

    public String getMerchantName() {
        return MerchantName;
    }

    public void setMerchantName(String merchantName) {
        MerchantName = merchantName;
    }

    public String getRctHdr1() {
        return RctHdr1;
    }

    public void setRctHdr1(String rctHdr1) {
        RctHdr1 = rctHdr1;
    }

    public String getRctHdr2() {
        return RctHdr2;
    }

    public void setRctHdr2(String rctHdr2) {
        RctHdr2 = rctHdr2;
    }

    public String getRctHdr3() {
        return RctHdr3;
    }

    public void setRctHdr3(String rctHdr3) {
        RctHdr3 = rctHdr3;
    }

    public String getMerchantPassword() {
        return MerchantPassword;
    }

    public void setMerchantPassword(String merchantPassword) {
        MerchantPassword = merchantPassword;
    }

    public String getInvNumber() {
        return InvNumber;
    }

    public void setInvNumber(String invNumber) {
        InvNumber = invNumber;
    }

    public String getSTAN() {
        return STAN;
    }

    public void setSTAN(String STAN) {
        this.STAN = STAN;
    }

    public String getMerchantID() {
        return MerchantID;
    }

    public void setMerchantID(String merchantID) {
        MerchantID = merchantID;
    }

    public String getBatchNumber() {
        return BatchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        BatchNumber = batchNumber;
    }

    public String getMobileNumber() {
        return MobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        MobileNumber = mobileNumber;
    }

    public String getNIC() {
        return NIC;
    }

    public void setNIC(String NIC) {
        this.NIC = NIC;
    }

    public String getMCC() {
        return MCC;
    }

    public void setMCC(String MCC) {
        this.MCC = MCC;
    }

    public String getContactNumber() {
        return ContactNumber;
    }

    public void setContactNumber(String contactNumber) {
        ContactNumber = contactNumber;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getDistrict() {
        return District;
    }

    public void setDistrict(String district) {
        District = district;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFax() {
        return Fax;
    }

    public void setFax(String fax) {
        Fax = fax;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getGroupId() {
        return GroupId;
    }

    public void setGroupId(int groupId) {
        GroupId = groupId;
    }

    public int getHostId() {
        return HostId;
    }

    public void setHostId(int hostId) {
        HostId = hostId;
    }

    public int getIsInstallment() {
        return IsInstallment;
    }

    public void setIsInstallment(int isInstallment) {
        IsInstallment = isInstallment;
    }

    public int getIsEnabled() {
        return IsEnabled;
    }

    public void setIsEnabled(int isEnabled) {
        IsEnabled = isEnabled;
    }
}
