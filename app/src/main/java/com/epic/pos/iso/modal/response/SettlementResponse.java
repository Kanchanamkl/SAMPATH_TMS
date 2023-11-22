package com.epic.pos.iso.modal.response;

import android.os.Parcel;
import android.os.Parcelable;

public class SettlementResponse implements Parcelable {
    public static final String RES_CODE_SUCCESS = "00";
    public static final String RES_CODE_BATCH_UPLOAD = "95";

    private String mti;
    private String processingCode;
    private String traceNumber;
    private String time; //HHMMSS
    private String date; //MMDD
    private String nii;
    private String rrn;
    private String authCode;
    private String tid;
    private String responseCode;

    public SettlementResponse() {
    }

    protected SettlementResponse(Parcel in) {
        mti = in.readString();
        processingCode = in.readString();
        traceNumber = in.readString();
        time = in.readString();
        date = in.readString();
        nii = in.readString();
        rrn = in.readString();
        authCode = in.readString();
        tid = in.readString();
        responseCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mti);
        dest.writeString(processingCode);
        dest.writeString(traceNumber);
        dest.writeString(time);
        dest.writeString(date);
        dest.writeString(nii);
        dest.writeString(rrn);
        dest.writeString(authCode);
        dest.writeString(tid);
        dest.writeString(responseCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SettlementResponse> CREATOR = new Creator<SettlementResponse>() {
        @Override
        public SettlementResponse createFromParcel(Parcel in) {
            return new SettlementResponse(in);
        }

        @Override
        public SettlementResponse[] newArray(int size) {
            return new SettlementResponse[size];
        }
    };

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
}
