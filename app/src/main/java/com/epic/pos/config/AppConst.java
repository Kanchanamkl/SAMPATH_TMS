package com.epic.pos.config;

public final class AppConst {

    public static final int ACTIVITY_RESULT_REVERSAL_FAILED        = 100;
    public static final int ACTIVITY_RESULT_VOID_PW                = 101;
    public static final int ACTIVITY_RESULT_TLE_PW                 = 102;

    public static final long INACTIVE_TIME_DURATION_IN_MILLS = 3 * 60 * 1000;//(3 * 60 * 1000)mills = 3 minutes
    public static final int IMAGE_DOWNLOAD_REQUEST_TIMEOUT   = 60 * 1000;

    public static final int CONNECT_TIMEOUT_SECONDS = 30;
    public static final int WRITE_TIMEOUT_SECONDS = 30;
    public static final int READ_TIMEOUT_SECONDS = 30;

    public static final int PAGINATION_OFFSET = 10;

    //Extras
    public static final String EXTRA_AMOUNT = "EXTRA_AMOUNT";
    public static final String EXTRA_CURRENT_PIN = "EXTRA_CURRENT_PIN";
    public static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";
    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    public static final String EXTRA_QR_DATA = "EXTRA_QR_DATA";
    public static final String EXTRA_SELECTED_USER = "EXTRA_SELECTED_USER";
    public static final String EXTRA_TRANSACTION_DETAIL = "EXTRA_TRANSACTION_DETAIL";
    public static final String EXTRA_SELECTED_HOST = "EXTRA_SELECTED_HOST";


    public static final String DIALOG_TITLE_SUCCESS = "Success";
    public static final String DIALOG_TITLE_ERROR = "Error";

    public static final String DEFAULT_FONT_PATH = "fonts/Roboto-Regular.ttf";


}
