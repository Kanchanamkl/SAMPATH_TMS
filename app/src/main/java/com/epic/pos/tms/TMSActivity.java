package com.epic.pos.tms;

import static com.epic.pos.data.datasource.SharedPref.HAS_PENDING_PROFILE_UPDATE;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.epic.pos.common.Const;
import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.device.PosDevice;
import com.epic.pos.ui.home.HomeActivity;
import com.epic.pos.ui.settings.tms.DebitKeyData;
import com.epic.pos.util.Utility;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

//import EPIC_TMS.tmsTerminalCom;


public class TMSActivity {
    public static boolean DEBITKEYDOWNLOADSUCCES = false;

    public static void disableTerminal(Context context) {
        new Thread() {
            @Override
            public void run() {
                SharedPref prefs = prefs(context);
                prefs.saveShouldDisableTerminal(true);
             //   notifyDisableTerminalComplete(context);
            }
        }.start();

    }
    public static void enableTerminal(Context context) {
        new Thread() {
            @Override
            public void run() {
                SharedPref prefs = prefs(context);
                prefs.saveShouldDisableTerminal(false);
               // notifyEnableTerminalComplete(context);
            }
        }.start();
    }
    public static void downloaddebitkey(String keyData,Context context){

        Gson gson = new Gson();
        DebitKeyData debitKeyData = gson.fromJson(keyData, DebitKeyData.class);
        Log.d("debitKeyData : ", debitKeyData.getAppPackageName());

        ArrayList<DebitKeyDetails> debitKeyDetails = (debitKeyData.getDebitKey().getIssuerKeyProfiles());
        int noOfhosts = debitKeyDetails.size();
        DebitKeyDetails visaDetails = null;

        for(int i = 0; i < noOfhosts; i++) {
            if(debitKeyDetails.get(i).getHostName().equals("VISA")) {
                visaDetails = debitKeyDetails.get(i);
                break;
            }
        }
        if(visaDetails != null) {
            String masterKey = getMasterKey(visaDetails.getHostKeys());
            Log.i("MASTERKEY : ",masterKey);
            String workKey2 = "492B274F0AB4DBCE492B274F0AB4DBCE";
            PosDevice.getInstance().clearWorkerKey(11);

            PosDevice.getInstance().loadMainKey(11, masterKey);
            PosDevice.getInstance().loadWorkKey(11, 11, workKey2);

            DEBITKEYDOWNLOADSUCCES=true;
            HomeActivity.getInstance().emitDebitKeyDownload();
        }
    }
    private static String getMasterKey(ArrayList<String> hostKeys) {
        int size = hostKeys.size();
        String firstKey = hostKeys.get(0);
        for(int i = 0; i < size-1; i++) {
            firstKey = Utility.xorHexString(firstKey,hostKeys.get(i+1));
        }
        return firstKey;
    }
    public static boolean isterminalenabled(Context context) {

        SharedPref prefs = prefs(context);
        return  prefs.isTerminalDisabled();

    }
    public static boolean hasPendingProfileUpdate(Context context) {
        SharedPref prefs = prefs(context);

        String boolString = prefs.getString(HAS_PENDING_PROFILE_UPDATE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
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
    public static void downloadProfileData(Context context, String data) {
      //  String profileData = getProfileDownloadData(context);
        SharedPref s = prefs(context);
        s.saveProfileUpdateData(data);
        s.saveHasPendingProfileUpdate(true);
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
    private static SharedPref prefs(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SharedPref.PREFS_NAME, Context.MODE_PRIVATE);
        return new SharedPref(sharedPref, context);
    }

    public static void installapplication(Context context) {
        Log.d("APPLICATIONINSTALL","1");
        try {
            String APP_NAME = "X990.apk";
            path = Environment.getExternalStorageDirectory() + "/Download/";
            String PATH = path;

            String downloadedapkpath="";
            File f = new File(PATH + APP_NAME);
            if (f.exists()) {
                downloadedapkpath = PATH + APP_NAME;
            }
            else{
            }
            Log.d("APPLICATIONINSTALL : ",downloadedapkpath);
            PosDevice.getInstance().installapplication(downloadedapkpath);

//            AsyncUpdate update = new AsyncUpdate(context);
//            //update.execute();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//                update.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            else
//                update.execute();


//            SharedPref s = prefs(context);
//            s.saveHasPendingApplicationUpdate(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static String path = "";
    public static class AsyncUpdate extends AsyncTask<String,Integer,Boolean> {
        private final static String APP_NAME = "X990.apk";
        String PATH = path;
        ProgressDialog installdialog;
        Context c;
        public AsyncUpdate(Context context) {
            this.c=context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            String msg = "";

            if (result)
                msg = "Download Finished";
            else
                msg = "Error Please Try Again..";

            if (!result)
                return;

            String downloadedapkpath="";
            File f = new File(PATH + APP_NAME);
            if (f.exists()) {
               downloadedapkpath = PATH + APP_NAME;
            }
            PosDevice.getInstance().installapplication(downloadedapkpath);

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return true;
        }

    }
}
