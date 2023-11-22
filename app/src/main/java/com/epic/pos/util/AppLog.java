package com.epic.pos.util;

import android.os.Environment;
import android.util.Log;

import com.epic.pos.BuildConfig;
import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppLog {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }

        if (Const.IS_LOG_WRITE_ON_FILE) {
            writeToFile("I", tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }

        if (Const.IS_LOG_WRITE_ON_FILE) {
            writeToFile("D", tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }

        if (Const.IS_LOG_WRITE_ON_FILE) {
            writeToFile("E", tag, msg);
        }
    }

    private static void writeToFile(String type, String tag, String msg) {
        try {
            File logDir = new File(Environment.getExternalStorageDirectory().getPath(), "eappa_logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            String logLine = sdf.format(new Date()) + " | " + type + " | " + tag + " | " + msg + "\r\n";

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(logFile, true);
                fileOutputStream.write(logLine.getBytes());
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
