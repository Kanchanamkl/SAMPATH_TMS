package com.epic.pos.util;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    private final String TAG = LogUtils.class.getSimpleName();
    private final String LOG_DIR = "eappa_logs";
    private File logFile;

    public LogUtils(Context context) {
        mLog("LogUtils()");
        File dir = new File(context.getFilesDir(), LOG_DIR);
        if (!dir.exists()) {
            mLog("Creating log dir.");
            dir.mkdirs();
        } else {
            mLog("Log dir exists.");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        logFile = new File(dir, sdf.format(new Date()) + ".log");
        if (!logFile.exists()) {
            mLog("Creating log file: " + logFile.getName());
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mLog("Log file exists: " + logFile.getName());
        }
    }

    /**
     * Write log into file.
     */
    public void log(String txt) {
        try {
            FileWriter writer = new FileWriter(logFile);
            writer.append(txt);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mLog(String msg) {
        AppLog.i(TAG, msg);
    }

}
