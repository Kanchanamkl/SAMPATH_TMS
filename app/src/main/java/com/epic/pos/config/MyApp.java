package com.epic.pos.config;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;

import com.epic.pos.util.AppLog;

import com.epic.pos.dagger.AppComponent;
import com.epic.pos.dagger.AppModule;
import com.epic.pos.dagger.DaggerAppComponent;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Aid;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.iso.ISOMsgBuilder;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.util.BackgroundThread;
import com.epic.pos.util.FileUtils;
import com.epic.pos.util.ImageUtils;
import com.epic.pos.device.PosDevice;

public class MyApp extends Application {

    private final String TAG = MyApp.class.getSimpleName();
    private AppComponent appComponent;
    private AppReceipts appReceipts;
    private LoginResponse loginData;
    private BackgroundThread bgThread;
    private boolean isAppRunning;
    //location
    public Location currentLocation = null;
    public Location previousLocation = null;
    //Charger status and battery level
    public boolean isCharging = false;
    public int batteryLevel = 0;

    private static MyApp mInstance;

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public static MyApp getInstance() {
        return mInstance;
    }

    protected AppComponent initDagger(MyApp application) {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(application))
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppLog.i(TAG, "onCreate()");
        mInstance = this;
        appComponent = initDagger(this);
        appReceipts = new AppReceipts(getApplicationContext());

        new DbHandler.Builder()
                .setContext(getApplicationContext())
                .build();

        new ISOMsgBuilder.Builder()
                .setContext(getApplicationContext())
                .build();

        new ImageUtils.Builder()
                .setContext(getApplicationContext())
                .build();

        new FileUtils.Builder()
                .setContext(getApplicationContext())
                .build();

        try {
            FileUtils.getInstance().copyFontsIfNotExists();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

       // startClientApp();

        bgThread = new BackgroundThread();
        bgThread.setContext(getApplicationContext());
        bgThread.start();

        initBatteryLevel();
    }

    public void initPosDevice() {
        AppLog.i(TAG, "initPosDevice()");
        DbHandler.getInstance().getAllAids(aids -> {
            AppLog.i(TAG, "all aids received");
            Aid visAid = null;
            Aid masterAid = null;
            Aid cupAid = null;
            Aid amexAid = null;
            Aid dinersAid = null;
            Aid jcbAid = null;

            for (Aid a : aids) {
                if (a.getIssuerID() == 1) {//visa aid
                    visAid = a;
                } else if (a.getIssuerID() == 2) {//master aid
                    masterAid = a;
                } else if (a.getIssuerID() == 3) { //cup aid
                    cupAid = a;
                } else if (a.getIssuerID() == 4) {//amex
                    amexAid = a;
                } else if (a.getIssuerID() == 5) {//diners
                    dinersAid = a;
                } else if (a.getIssuerID() == 6) { //jcb
                    jcbAid = a;
                }
            }

            new PosDevice.Builder()
                    .setContext(getApplicationContext())
                    .setVisa(visAid)
                    .setMaster(masterAid)
                    .setCup(cupAid)
                    .setAmex(amexAid)
                    .setDiners(dinersAid)
                    .setJCB(jcbAid)
                    .build();

            PosDevice.getInstance().setInitListener(() -> {
                appReceipts.setSerialNo(PosDevice.getInstance().getSerialNo());
            });
        });
    }

    private void initBatteryLevel() {
        //battery level receiver
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(mBatInfoReceiver, batteryIntentFilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);

        //charger status receiver
        IntentFilter chargerIntentFilter = new IntentFilter();
        chargerIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        chargerIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(chargerStatusReceiver, chargerIntentFilter);
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
    };

    private BroadcastReceiver chargerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                isCharging = true;
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                isCharging = false;
            }
        }
    };

    /**
     * Is pos moved in 10 meters.
     *
     */
    public boolean isPosMoved() {
        if (previousLocation != null && currentLocation != null) {
            float distanceInMeters = currentLocation.distanceTo(previousLocation);
            return (distanceInMeters >= 25);
        }
        return false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public LoginResponse getLoginData() {
        return loginData;
    }

    public BackgroundThread getBgThread() {
        return bgThread;
    }

    public void setLoginData(LoginResponse data) {
        this.loginData = data;
    }

    public AppReceipts getAppReceipts() {
        return appReceipts;
    }

    public void startClientApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.epic.eatmca");
        if (intent != null)
            startActivity(intent);
    }

    public boolean isAppRunning() {
        return isAppRunning;
    }

    public void setAppRunning(boolean appRunning) {
        isAppRunning = appRunning;
    }



}
