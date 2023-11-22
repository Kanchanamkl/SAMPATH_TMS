package com.epic.pos.ui.settings.tms;

import com.example.atemhub.control.AppData;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class DeviceOperationData {
    @SerializedName("operationCode")
    private String code;

    @SerializedName("operationMessage")
    private String message;

    @SerializedName("appPackageName")
    private String appPackageName;

    @SerializedName("data")
    private List<AppData> appData;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public List<AppData> getAppData() {
        return appData;
    }

    public void setAppData(List<AppData> appData) {
        this.appData = appData;
    }

    @Override
    public String toString() {
        return "DeviceOperation{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", appPackageName='" + appPackageName + '\'' +
                ", appData=" + appData +
                '}';
    }
}
