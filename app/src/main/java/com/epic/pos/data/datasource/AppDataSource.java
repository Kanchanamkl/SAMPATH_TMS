package com.epic.pos.data.datasource;

import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.ErrorResponse;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.data.model.respone.ServiceResponse;

public interface AppDataSource {

    interface LoginCallback {
        void onSuccessful(ServiceResponse<LoginResponse> serviceResponse);

        void onFailed(ErrorResponse errorResponse);

        void onFailed(String message);

        void onCompleted();
    }

    void login(LoginRequest loginRequest, LoginCallback loginCallback);

}
