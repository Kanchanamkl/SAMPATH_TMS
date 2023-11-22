package com.epic.pos.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.epic.pos.util.AppLog;

import com.epic.pos.data.db.DbHandler;
import com.epic.pos.ui.home.HomeActivity;

public class NotifyingDailyService extends Service {

    private final String TAG = NotifyingDailyService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind()");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand()");
        DbHandler.getInstance().getTCT(tct -> {
            log("tct received: " + tct);
            if (tct.getStartUpAutoRun() == 1){
                log("auto startup enabled");
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }else {
                log("auto startup disabled");
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void log(String msg){
        AppLog.i(TAG, msg);
    }
}