package com.epic.pos.data.model.request;

import android.os.Build;
import android.text.TextUtils;

import com.epic.pos.BuildConfig;
import com.epic.pos.util.Hash;

public class LoginRequest {
    private String user_name, pin, app_version, os_type, push_id, model, uuid, brand, os_version,
            device_type, push_sha;

    public LoginRequest(String user_name, String pin, String pushId, String UUID) {
        this.user_name = user_name;
        this.pin = Hash.mpiMd5(pin);
        this.app_version = String.valueOf(BuildConfig.VERSION_CODE);
        this.os_type = "Android";
        this.push_id = pushId;
        this.model = android.os.Build.MODEL;
        this.uuid = UUID;
        this.brand = android.os.Build.PRODUCT;
        this.os_version = System.getProperty("os.version");
        this.device_type = Build.TYPE;
        this.push_sha = Hash.getSHA256Hash(pushId);
    }

    public String getUserName() {
        return user_name;
    }

    public boolean isValidRequest() {
        if (TextUtils.isEmpty(user_name) && TextUtils.isEmpty(pin)) {
            return false;
        }

        //check username is empty
        if (TextUtils.isEmpty(user_name)) {
            return false;
        }

        //check pin is empty
        if (TextUtils.isEmpty(pin)) {
            return false;
        }

        return true;
    }


    public String getErrorMessage() {
        if (TextUtils.isEmpty(user_name) && TextUtils.isEmpty(pin)) {
            return "Please enter a username";
        }

        //check username is empty
        if (TextUtils.isEmpty(user_name)) {
            return "Please enter a username";
        }

        //check pin is empty
        if (TextUtils.isEmpty(pin)) {
            return "Please enter a pin";
        }

        return "Oops! something went wrong!";
    }
}
