package com.epic.pos.data.db.dbpos.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "CST")
public class Currency implements Parcelable {

    public Currency() {
    }

    @PrimaryKey()
    @ColumnInfo(name = "MerchantNumber")
    @NotNull
    private int MerchantNumber;//REFERENCES "MIT"("MerchantNumber")

    @ColumnInfo(name = "CurrencySymbol")
    private String CurrencySymbol;

    @ColumnInfo(name = "CurrencyCode")
    private String CurrencyCode;

    @ColumnInfo(name = "CountryCode")
    private String CountryCode;

    protected Currency(Parcel in) {
        MerchantNumber = in.readInt();
        CurrencySymbol = in.readString();
        CurrencyCode = in.readString();
        CountryCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(MerchantNumber);
        dest.writeString(CurrencySymbol);
        dest.writeString(CurrencyCode);
        dest.writeString(CountryCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel in) {
            return new Currency(in);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };

    public int getMerchantNumber() {
        return MerchantNumber;
    }

    public void setMerchantNumber(int merchantNumber) {
        MerchantNumber = merchantNumber;
    }

    public String getCurrencySymbol() {
        return CurrencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        CurrencySymbol = currencySymbol;
    }

    public String getCurrencyCode() {
        return CurrencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        CurrencyCode = currencyCode;
    }

    public String getCountryCode() {
        return CountryCode;
    }

    public void setCountryCode(String countryCode) {
        CountryCode = countryCode;
    }
}
