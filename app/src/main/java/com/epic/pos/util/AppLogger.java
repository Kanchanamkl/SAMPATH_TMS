package com.epic.pos.util;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by harshana_m on 12/3/2018.
 */

public class AppLogger {
    private String filename = null;
    private FileWriter writer = null;

    private boolean noLog = false;


    public AppLogger(String fname)
    {
       filename  = fname;
    }

    public boolean initialize() {
        File file = null;
        String path = null;
        path = Environment.getExternalStorageDirectory() + File.separator  +  filename;

        if (!isExternalStorageWritable())
            return false;

        try {
            file = new File(path);

            if (file.exists())
                file.delete();

            if (!file.createNewFile())
                return false;

            writer = new FileWriter(file);
        } catch (IOException io) {
            return false;
        }
        return true;
    }

    public void Log(String tag, String data) {
        if(noLog)
            return ;

        String line  = tag + "  " + data;
        try {
            if(writer == null) {
                return;
            }

            writer.write(line + "\n");
            writer.flush();

        } catch ( IOException io) {
            io.printStackTrace();
        }
    }

    public void deInit() {
        try {
            writer.flush();
            writer.close();
        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}