package com.epic.pos.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originRequest = chain.request();
        Request requestWithUserAgent = originRequest.newBuilder()
                .header("User-Agent", userAgent)
                .header("OS", "ANDROID")
//                .header("Version-Code", String.valueOf(BuildConfig.VERSION_CODE))
                .build();
        return chain.proceed(requestWithUserAgent);
    }
}

