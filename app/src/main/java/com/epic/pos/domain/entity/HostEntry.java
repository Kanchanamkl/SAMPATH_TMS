package com.epic.pos.domain.entity;

public class HostEntry {

    //merchant
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String contactNumber;
    private String country;
    private String district;
    private String email;
    private String fax;
    private String mcc;
    private String mid;
    private String mobileNumber;
    private String province;
    private String remarks;
    //currency
    private String countryCode;
    private String currencyCode;
    private String currencySymbol;
    //terminal
    private String tid;

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public String getCity() {
        return city;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public String getDistrict() {
        return district;
    }

    public String getEmail() {
        return email;
    }

    public String getFax() {
        return fax;
    }

    public String getMcc() {
        return mcc;
    }

    public String getMid() {
        return mid;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getProvince() {
        return province;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getTid() {
        return tid;
    }
}
