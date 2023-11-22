package com.epic.pos.device.data;

import com.google.gson.annotations.SerializedName;

/**
 * The CardData class contains the card data.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-10
 */
public class CardData {

    @SerializedName("pan")
    private String pan;

    @SerializedName("track1")
    private String track1;

    @SerializedName("track2")
    private String track2;

    @SerializedName("track3")
    private String track3;

    @SerializedName("serviceCode")
    private String serviceCode;

    @SerializedName("expiryDate")
    private String expiryDate;

    @SerializedName("cardHolderName")
    private String cardHolderName;

    @SerializedName("aid")
    private String aid;

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getTrack1() {
        return track1;
    }

    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getTrack3() {
        return track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName.trim();
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    @Override
    public String toString() {
        return "CardData{" +
                "pan='" + pan + '\'' +
                ", track1='" + track1 + '\'' +
                ", track2='" + track2 + '\'' +
                ", track3='" + track3 + '\'' +
                ", serviceCode='" + serviceCode + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", cardHolderName='" + getCardHolderName() + '\'' +
                ", aid='" + aid + '\'' +
                '}';
    }
}
