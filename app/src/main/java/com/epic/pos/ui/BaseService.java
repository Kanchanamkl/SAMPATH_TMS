package com.epic.pos.ui;

import android.app.Service;

import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;

public abstract class BaseService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        initDependencies(((MyApp) getApplication()).getAppComponent());
    }

    protected abstract void initDependencies(AppComponent appComponent);
}
