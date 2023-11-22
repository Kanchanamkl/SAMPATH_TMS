package com.epic.pos.ui.common.password;

import com.epic.pos.ui.BaseView;

public interface PasswordContract {
    interface View extends BaseView {
        void passwordOk();
        void passwordTLEOk();
        void invalidPassword();
    }

    interface Presenter {
        void initData();
        void closeButtonPressed();
        void onSubmit(String password, boolean isTLE);
        void setPasswordType(PasswordType passwordType);
    }
}