package com.epic.pos.ui;

import static com.epic.pos.config.AppConst.DEFAULT_FONT_PATH;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.epic.pos.R;
import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.device.PosDevice;
import com.epic.pos.ui.login.LoginActivity;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.UiUtil;
import com.epic.pos.view.CustomProgressDialog;

import java.lang.reflect.Method;

import javax.inject.Inject;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity implements BaseView {

    private final String TAG = BaseActivity.class.getSimpleName();

    @Inject
    protected T mPresenter;
    protected LoginResponse loginData;
    private DialogFragment dialog;
    private Toast toast = null;

    private int batteryLevel = 0;
    private boolean isCharging = false;
    private boolean isloadervisible;

    //Status bar hide
    private boolean currentFocus;
    private boolean isPaused;
    private Handler collapseNotificationHandler;

    private BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getAction())
                    && intent.getAction().equals(Const.BROADCAST_ACTION_NOTIFY)) {
                if (intent.hasExtra(Const.BROADCAST_EXTRA_ACTION)) {
                    String action = intent.getStringExtra(Const.BROADCAST_EXTRA_ACTION);
                    switch (action) {
                        case Const.BROADCAST_EXTRA_AUTO_SETTLEMENT:
                            onAutoSettlementNotify();
                            break;
                        case Const.BROADCAST_EXTRA_PROFILE_DOWNLOAD:
                            onProfileUpdateNotify();
                            break;
                        case Const.BROADCAST_EXTRA_TERMINAL_DISABLE:
                            onTerminalDisableNotify();
                            break;
                        case Const.BROADCAST_EXTRA_GENERATE_CONFIG_MAP:
                            onGenerateConfigMapNotify();
                            break;
                    }
                }
            }
        }
    };

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            onBatteryLevelChanged(batteryLevel);
        }
    };

    private BroadcastReceiver chargerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                onChargerStatesChanged(true);
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                onChargerStatesChanged(false);
            }
        }
    };


    protected void onChargerStatesChanged(boolean isCharging) {
        AppLog.i(TAG, "onChargerStatesChanged() isConnected: " + isCharging);
        if (isCharging) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void onBatteryLevelChanged(int batteryLevel) {
        AppLog.i(TAG, "onBatteryLevelChanged() " + batteryLevel);
    }

    protected void onProfileUpdateNotify() {
        AppLog.i(TAG, "onProfileUpdateNotify()");
        mPresenter.hasPendingProfileUpdate();
    }

    protected void onAutoSettlementNotify() {
        AppLog.i(TAG, "onNotifyAutoSettlement()");
        mPresenter.hasPendingSettlement();
    }

    protected void onTerminalDisableNotify() {
        AppLog.i(TAG, "onTerminalDisableNotify()");
    }

    protected void onGenerateConfigMapNotify() {
        AppLog.i(TAG, "onGenerateConfigMapNotify()");
    }

    protected void disableCopyPaste(EditText editText) {
        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PosDevice.getInstance().setActivity(this);
        } catch (Exception ex) {
        }

        initDependencies(((MyApp) getApplication()).getAppComponent());
        loginData = ((MyApp) getApplication()).getLoginData();
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath(DEFAULT_FONT_PATH)
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

//        View decorView = getWindow().getDecorView();  //BBB25082023
//        decorView.setOnSystemUiVisibilityChangeListener
//                (visibility -> {
//                    // Note that system bars will only be "visible" if none of the
//                    // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
//                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//                        // The system bars are visible.
//                        Log.i(TAG, "system bars are visible.");
//                    } else {
//                        // The system bars are NOT visible.
//                        Log.i(TAG, "The system bars are NOT visible.");
//                    }
//                });


    }

    public void collapseNow() {
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }

        if (!currentFocus && !isPaused) {
            collapseNotificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    @SuppressLint("WrongConstant")
                    Object statusBarService = getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Method collapseStatusBar = null;

                    try {
                        collapseStatusBar = statusBarManager .getMethod("collapsePanels");
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }
                }
            }, 100L);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.getInstance().setAppRunning(true);

        //profile update receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.BROADCAST_ACTION_NOTIFY);
        registerReceiver(profileUpdateReceiver, filter);

        //battery level receiver
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(mBatInfoReceiver, batteryIntentFilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        onChargerStatesChanged(isCharging);

        //charger status receiver
        IntentFilter chargerIntentFilter = new IntentFilter();
        chargerIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        chargerIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(chargerStatusReceiver, chargerIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.profileUpdateReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyApp.getInstance().setAppRunning(false);
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter=null;
        }

        unregisterReceiver(mBatInfoReceiver);
        unregisterReceiver(chargerStatusReceiver);
        super.onDestroy();
    }

    protected abstract void initDependencies(AppComponent appComponent);

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }

        return super.onCreateView(parent, name, context, attrs);
    }

    public void setUpToolbarDefault(Toolbar mToolbar, String title, boolean backButtonEnable) {
        ((TextView) mToolbar.findViewById(R.id.tvTitle)).setText(title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        if (backButtonEnable) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
//            mToolbar.setNavigationIcon(R.drawable.ic_left_arrow);
            mToolbar.setNavigationIcon(R.drawable.ic_close);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // close this activity and return to preview activity (if there is any)
            onCloseButtonPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCloseButtonPressed() {
        //
    }

    @Override
    public void showToastMessage(String message) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void showDialogMessage(String title, String message) {
        UiUtil.showDialogMessage(this, title, message);
    }

    @Override
    public void showDialogMessage(String title, String message, UiUtil.SuccessDialogListener listener) {
        UiUtil.showDialogMessage(this, title, message, listener);
    }

    @Override
    public void showDialogError(String title, String message, UiUtil.ErrorDialogListener listener) {
        UiUtil.showErrorDialog(this, title, message, listener);
    }

    public void showDialogError(String title, String message, String posBtn, String negBtn, UiUtil.OptionDialogListener listener) {
        UiUtil.showErrorDialog(this, title, message, posBtn, negBtn, listener);
    }

    @Override
    public void showNoInternetAlert() {
        UiUtil.noInternetAlert(this);
    }

    @Override
    public void showLoader(@NonNull String title, @NonNull String message) {
        if (isloadervisible) return;
        isloadervisible = true;
        if (dialog == null) {
            dialog = CustomProgressDialog.newInstance(title, message);
        }
        if (!dialog.isAdded()) {
            dialog.show(getSupportFragmentManager(), "CustomProgressDialog");
        }
    }

    protected void showMsgDialog(String msg, DialogListener listener) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    dialog.dismiss();
                    listener.onPositiveClicked();
                }).show();
    }

    public boolean isAutomaticTimeZoneEnabled() {
        Log.i(TAG, "isAutomaticTimeZoneEnabled()");
        try {
            int autoTime = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME);
            int autoTimeZone = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE);
            Log.i(TAG, "autoTime: " + autoTime);
            Log.i(TAG, "autoTimeZone: " + autoTimeZone);
            return autoTimeZone == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public void hideLoader() {
        if (!isloadervisible) return;
        isloadervisible = false;
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            dialog = null;
        }
    }

    public interface DialogListener {
        void onPositiveClicked();
    }

    @Override
    public void launchLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    public static class customViewGroup extends ViewGroup {

        public customViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("customViewGroup", "**********Intercepted");
            return true;
        }
    }


}
