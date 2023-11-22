package com.epic.pos.service;

import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.data.model.respone.ServiceResponse;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface ApiInterface {

    @POST("Login")
    Observable<Response<ServiceResponse<LoginResponse>>> login(@Body LoginRequest loginRequest);

}
