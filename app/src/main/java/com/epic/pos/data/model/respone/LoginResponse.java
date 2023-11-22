package com.epic.pos.data.model.respone;

public class LoginResponse {
    private String merchanttoken, merchantname, address, mid, tid, currency;

    public String getMerchanttoken() {
        return merchanttoken;
    }

    public String getMerchantname() {
        return merchantname;
    }

    public String getAddress() {
        return address;
    }

    public String getMid() {
        return mid;
    }

    public String getTid() {
        return tid;
    }

    public String getCurrency() {
        return currency;
    }
}
