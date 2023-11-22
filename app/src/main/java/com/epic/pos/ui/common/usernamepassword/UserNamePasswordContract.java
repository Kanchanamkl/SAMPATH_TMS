package com.epic.pos.ui.common.usernamepassword;

import com.epic.pos.ui.BaseView;

public interface UserNamePasswordContract {

    interface View extends BaseView {
        void passwordOk();
        void invalidPassword();
    }

    interface Presenter {
        void initData();
        void onSubmit(String username,String password);
    }
}
