package com.epic.pos.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class HomeMenuBean implements Parcelable {

    public static final int TYPE_SALE = 1;
    public static final int TYPE_OFFLINE_SALE = 2;
    public static final int TYPE_MANUAL_SALE = 3;
    public static final int TYPE_OFFLINE_MANUAL_SALE = 4;
    public static final int TYPE_SETTLEMENT = 5;
    public static final int TYPE_VOID = 6;
    public static final int TYPE_PRE_AUTH = 7;
    public static final int TYPE_REFUND = 8;
    public static final int TYPE_PRE_AUTH_MANUAL = 9;
    public static final int TYPE_REFUND_MANUAL = 10;
    public static final int TYPE_PRE_COM = 11;
    public static final int TYPE_CASH_ADVANCE = 12;
    public static final int TYPE_INSTALMENT = 13;
    public static final int TYPE_CASH_BACK = 14;
    public static final int TYPE_CLEAR_REVERSAL = 15;
    public static final int TYPE_QR_VERIFY = 16;
    public static final int TYPE_QUASI_CASH = 17;
    public static final int TYPE_QUASI_CASH_MANUAL = 18;
    public static final int TYPE_DETAIL_REPORT = 19;
    public static final int TYPE_PRINT_LAST_RECEIPT = 20;
    public static final int TYPE_LAST_SETTLEMENT_RECEIPT = 21;
    public static final int TYPE_PRINT_ANY_RECEIPT = 22;
    public static final int TYPE_CHECK_REVERSAL = 23;
    public static final int TYPE_STUDENT_REF_SALE = 24;
    public static final int TYPE_AUTH_ONLY = 25;
    public static final int TYPE_QR_SALE= 26;


    private String name;
    private int icon;
    private int type;

    public HomeMenuBean(String name, int icon, int type) {
        this.name = name;
        this.icon = icon;
        this.type = type;
    }

    protected HomeMenuBean(Parcel in) {
        name = in.readString();
        icon = in.readInt();
        type = in.readInt();
    }

    public static final Creator<HomeMenuBean> CREATOR = new Creator<HomeMenuBean>() {
        @Override
        public HomeMenuBean createFromParcel(Parcel in) {
            return new HomeMenuBean(in);
        }

        @Override
        public HomeMenuBean[] newArray(int size) {
            return new HomeMenuBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(icon);
        parcel.writeInt(type);
    }
}
