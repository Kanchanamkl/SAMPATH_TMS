package com.epic.pos.dagger;


import com.epic.pos.data.datasource.AppDataSource;
import com.epic.pos.data.datasource.IsoDataSourceImpl;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.repository.RepositoryImpl;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.data.datasource.SharedPref;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    @Provides
    @Singleton
    Repository providesRepository(AppDataSource appDataSource,
                                  SharedPref sharedPref,
                                  IsoDataSourceImpl isoDataSource,
                                  DbHandler localDataSource) {
        return new RepositoryImpl(appDataSource, sharedPref, isoDataSource, localDataSource);
    }
}