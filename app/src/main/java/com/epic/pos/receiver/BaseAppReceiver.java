package com.epic.pos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.data.db.dbpos.EpicAPOSDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BaseAppReceiver extends BroadcastReceiver {

    private final String TAG = BaseAppReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLog.i(TAG, "onReceive()");
        if (intent.hasExtra("cmd")) {
            String cmd = intent.getStringExtra("cmd");
            if (cmd.equals("auto_settlement")) {
                log("auto settlement request.");
                updateAutoSettleData(context);
            } else if (cmd.equals("profile_download")) {
                log("profile download request");
                downloadProfileData(context);
            } else if (cmd.equals("terminal_disable")) {
                log("terminal disable operation.");
                disableTerminal(context);
            } else if (cmd.equals("terminal_enable")) {
                log("terminal enable operation.");
                enableTerminal(context);
            }
        }
    }

    private void enableTerminal(Context context) {
        new Thread() {
            @Override
            public void run() {
                SharedPref prefs = prefs(context);
                prefs.saveShouldDisableTerminal(false);
                notifyEnableTerminalComplete(context);
            }
        }.start();
    }


    private void disableTerminal(Context context) {
        new Thread() {
            @Override
            public void run() {
                SharedPref prefs = prefs(context);
                prefs.saveShouldDisableTerminal(true);
                notifyDisableTerminalComplete(context);
            }
        }.start();
    }

    private void downloadProfileData(Context context) {
        String profileData = getProfileDownloadData(context);
        SharedPref s = prefs(context);
        s.saveProfileUpdateData(profileData);
        s.saveHasPendingProfileUpdate(true);
    }


    public static void notifyEnableTerminalComplete(Context context) {
        try {
            Uri myUri = Uri.parse("content://" + Const.EPIC_BASE_APP_DATA_PROVIDER + "/terminal_enable_complete");
            Cursor cursor = context.getContentResolver().query(myUri, null, null, null, null);
            cursor.moveToFirst();
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void notifyDisableTerminalComplete(Context context) {
        try {
            Uri myUri = Uri.parse("content://" + Const.EPIC_BASE_APP_DATA_PROVIDER + "/terminal_disable_complete");
            Cursor cursor = context.getContentResolver().query(myUri, null, null, null, null);
            cursor.moveToFirst();
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void notifyProfileUpdateComplete(Context context) {
        try {
            Uri myUri = Uri.parse("content://" + Const.EPIC_BASE_APP_DATA_PROVIDER + "/profile_update_complete");
            Cursor cursor = context.getContentResolver().query(myUri, null, null, null, null);
            cursor.moveToFirst();
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isTMSConnected(Context context) {
        try {
            Uri myUri = Uri.parse("content://" + Const.EPIC_BASE_APP_DATA_PROVIDER + "/tms_connected");
            Cursor cursor = context.getContentResolver().query(myUri, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex("tms_connected");
            int tmsStatus = cursor.getInt(idx);
            cursor.close();
            return tmsStatus == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String getProfileDownloadData(Context context) {
        try {
            Uri myUri = Uri.parse("content://" + Const.EPIC_BASE_APP_DATA_PROVIDER + "/profile_data");
            Cursor cursor = context.getContentResolver().query(myUri, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex("profile_data");
            String profileData = cursor.getString(idx);
            cursor.close();
            return profileData;
        } catch (Exception ex) {
            log("get profile data error : " + ex.getMessage());
        }
        return null;
    }

    private void updateAutoSettleData(Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat(Const.AUTO_SETTLE_DATE_FORMAT).format(c.getTime());

        new Thread() {
            @Override
            public void run() {
                EpicAPOSDatabase db = EpicAPOSDatabase.getInstance(context);
                db.tctDao().updateAutoSettleDate(yesterday);
                db.tctDao().updateAutoSettleEnable(1);
            }
        }.start();
    }

    private SharedPref prefs(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SharedPref.PREFS_NAME, Context.MODE_PRIVATE);
        return new SharedPref(sharedPref, context);
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

}
