package com.epic.pos.ui;

import com.epic.pos.util.UiUtil;

public interface BaseView {
    void showToastMessage(String message);
    void showDialogMessage(String title, String message);
    void showDialogMessage(String title, String message, UiUtil.SuccessDialogListener listener);
    void showDialogError(String title, String message, UiUtil.ErrorDialogListener listener);
    void showNoInternetAlert();
    void showLoader(String title, String message);
    void hideLoader();
    void launchLogin();

}
