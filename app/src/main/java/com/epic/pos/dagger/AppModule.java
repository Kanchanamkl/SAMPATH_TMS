package com.epic.pos.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.util.spcrypto.SpKeyGen;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return application;
    }

    @Provides
    @Singleton
    SharedPreferences provideDefaultSharedPreference(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SharedPref.PREFS_NAME, Context.MODE_PRIVATE);

        if (!sharedPref.contains(SharedPref.AES_KEY)){
            String aesKey = SpKeyGen.generateAESKey();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(SharedPref.AES_KEY, aesKey);
            editor.apply();
        }

        if (!sharedPref.contains(SharedPref.AES_INET_VECTOR)){
            String initVector = SpKeyGen.generateAESInitVector();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(SharedPref.AES_INET_VECTOR, initVector);
            editor.apply();
        }

        return sharedPref;
    }

    @Provides
    @Singleton
    SharedPreferences.Editor provideSharedPreferencesEditor(SharedPreferences preferences){
        return preferences.edit();
    }
}
