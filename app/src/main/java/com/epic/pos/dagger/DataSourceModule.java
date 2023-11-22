package com.epic.pos.dagger;

import android.content.Context;

import com.epic.pos.data.datasource.AppDataSource;
import com.epic.pos.data.datasource.AppDataSourceImpl;
import com.epic.pos.data.datasource.IsoDataSourceImpl;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.service.SocketConnectionService;
import com.epic.pos.service.ApiInterface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DataSourceModule {

    @Provides
    @Singleton
    AppDataSource providesEventDataSource(ApiInterface apiInterface) {
        return new AppDataSourceImpl(apiInterface);
    }

    @Provides
    @Singleton
    SocketConnectionService providesSocketConnectionService(Context context) {
        return new SocketConnectionService(context);
    }

    @Provides
    @Singleton
    IsoDataSourceImpl providesIsoDataSource(SocketConnectionService socketConnectionService) {
        return new IsoDataSourceImpl(socketConnectionService);
    }

    @Provides
    @Singleton
    DbHandler providesLocalDbSource(Context context) {
        new DbHandler.Builder()
                .setContext(context)
                .build();
        return DbHandler.getInstance();
    }
}
