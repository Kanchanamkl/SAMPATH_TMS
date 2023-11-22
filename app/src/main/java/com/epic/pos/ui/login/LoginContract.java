package com.epic.pos.ui.login;

import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.ui.BaseView;

public interface LoginContract {
    interface View extends BaseView{
        void launchDashboard();
        void launchChangePin(String userName, String token);
        void clearData();
        void saveLoginData(LoginResponse data);
        void setUsername(String username);
    }

    interface Presenter {
        void login(LoginRequest loginRequest);
        void setUsername();
    }
}