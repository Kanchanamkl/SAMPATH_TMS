package com.epic.pos.dagger;

import android.content.Context;

import com.epic.pos.service.ApiInterface;
import com.epic.pos.service.RetrofitBuilder;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.helper.NetworkConnectionImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    ApiInterface provideApiInterface(){
        return RetrofitBuilder.getRetrofitInstance().create(ApiInterface.class);
    }

    @Provides
    @Singleton
    NetworkConnection provideNetworkConnection(Context context){
        return new NetworkConnectionImpl(context);
    }
}
