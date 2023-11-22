package com.epic.pos.iso.modal.response;

public class CommonResponse {
    private String responseCode;
    private String ErrorMsg;
    private boolean isSuccess;

    public String getResponseCode() {
        return responseCode;
    }

    public String getErrorMsg() {
        return ErrorMsg;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setErrorMsg(String errorMsg) {
        ErrorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return responseCode.equals("00");
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
