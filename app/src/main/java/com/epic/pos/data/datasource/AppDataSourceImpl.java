package com.epic.pos.data.datasource;

import com.epic.pos.config.ErrorMessages;
import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.ErrorResponse;
import com.epic.pos.service.ApiInterface;
import com.google.gson.Gson;

import java.net.SocketTimeoutException;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AppDataSourceImpl implements AppDataSource {

    private ApiInterface apiInterface;

    public AppDataSourceImpl(ApiInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    @Override
    public void login(LoginRequest loginRequest, LoginCallback loginCallback) {
        apiInterface.login(loginRequest).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {

            if (response.code() == 200) {
                loginCallback.onSuccessful(response.body());

            } else if (response.code() == 404) {
                loginCallback.onFailed(ErrorMessages.ERROR_MESSAGE_404);

            } else {
                try {
                    ErrorResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), ErrorResponse.class);
                    loginCallback.onFailed(errorResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    loginCallback.onFailed(ErrorMessages.ERROR_MESSAGE_DEFAULT);
                }
            }
        }, throwable -> {
            if (throwable instanceof SocketTimeoutException) {
                loginCallback.onFailed(ErrorMessages.ERROR_MESSAGE_TIME_OUT);
            } else {
                loginCallback.onFailed(ErrorMessages.ERROR_MESSAGE_DEFAULT);
            }
        }, loginCallback::onCompleted);
    }
}

