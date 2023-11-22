package com.epic.pos.service;

import android.os.Build;

import com.epic.pos.config.AppConst;
import com.epic.pos.config.ServerConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitBuilder {
    private static Retrofit RETROFIT_PLAIN_TEXT;
    private static Retrofit RETROFIT;

    public static Retrofit getRetrofitInstancePlainText() {
        if (RETROFIT_PLAIN_TEXT == null) {


            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(AppConst.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(AppConst.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(AppConst.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(new UserAgentInterceptor(Build.MANUFACTURER + "|" + Build.MODEL + "|" + Build.VERSION.RELEASE))
                    .build();


            RETROFIT_PLAIN_TEXT = new Retrofit.Builder()
                    .baseUrl(ServerConfig.APPLICATION_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build();
        }
        return RETROFIT_PLAIN_TEXT;
    }

    public static Retrofit getRetrofitInstance() {
        if (RETROFIT == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(AppConst.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(AppConst.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(AppConst.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(new UserAgentInterceptor(Build.MANUFACTURER + "|" + Build.MODEL + "|" + Build.VERSION.RELEASE))
                    .build();


            RETROFIT = new Retrofit.Builder()
                    .baseUrl(ServerConfig.APPLICATION_URL)
             //       .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build();
        }
        return RETROFIT;
    }

    public static void clearInstances() {
        RETROFIT = null;
        RETROFIT_PLAIN_TEXT = null;
    }

}
