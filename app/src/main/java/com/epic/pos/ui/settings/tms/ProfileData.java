package com.epic.pos.ui.settings.tms;

import com.epic.pos.domain.entity.ProfileDataEntity;
import com.google.gson.annotations.SerializedName;

public class ProfileData {
    @SerializedName("operationCode")
    private String operationCode;

    @SerializedName("operationMessage")
    private String operationMessage;

    @SerializedName("appPackageName")
    private String appPackageName;

    @SerializedName("data")
    private ProfileDataEntity profileDataEntity;

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getOperationMessage() {
        return operationMessage;
    }

    public void setOperationMessage(String operationMessage) {
        this.operationMessage = operationMessage;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public ProfileDataEntity getProfileDataEntity() {
        return profileDataEntity;
    }

    public void setProfileDataEntity(ProfileDataEntity profileDataEntity) {
        this.profileDataEntity = profileDataEntity;
    }

}
