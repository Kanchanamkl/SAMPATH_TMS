package com.epic.pos.data.model.respone;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    private String status;
    @SerializedName("status_code") private String statusCode;
    @SerializedName("status_description") private String statusDescription;
    @SerializedName("description_visibility") private boolean descriptionVisibility;

    public String getStatus() {
        return status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public boolean isDescriptionVisibility() {
        return descriptionVisibility;
    }
}
