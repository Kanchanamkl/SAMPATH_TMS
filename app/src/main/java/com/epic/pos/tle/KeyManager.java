package com.epic.pos.tle;

import android.os.Environment;
import com.epic.pos.util.ValidatorUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class KeyManager {

    private static String keySet[] = new String[100];

    public static void  setKeyInSlot(int index, String key) {
        keySet[index] = key;
        writeKeyInSecFile(index,key);
    }

    public static String getKeyFromSlot(int index) {
        return keySet[index];
    }

    //the code below will emulate a secure area but its not really a secured area
    private static BufferedReader reader ;
    private static BufferedWriter writer;

    //this is the part that initialize the secure file in the file system
    private static File secKeyFile = null;
    private static final String SEC_FILE = "secFile.dat";

    public static void init() {
        path = Environment.getExternalStorageDirectory() + "/Download/";
        File pathDirs =  new File(path);
        if (!pathDirs.exists())
            pathDirs.mkdirs();

        //read all the keys and store in the object
        if (secKeyFile == null)
            secKeyFile =  new File(path,SEC_FILE);

        secKeyFile.setReadable(true);

        try {
            //read up all the keys within the file
            if (reader == null)
                reader =  new BufferedReader(new FileReader(secKeyFile));

            String line;
            int index;
            String key;
            int offset;

            while ((line = reader.readLine()) != null) {
                //tokenize and store in the array
                //get the index
                offset = line.indexOf("_");
                offset++;
                String sIndex = line.substring(offset,offset + 3);
                index = Integer.valueOf(sIndex);

                //get the key
                offset = line.indexOf("=");
                offset++;
                key = line.substring(offset,line.length());

                //store it
                keySet[index] = key;
            }

            reader.close();
            reader = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String path = Environment.getExternalStorageDirectory() + "/Download/";
    private static void writeKeyInSecFile(int index, String key) {
        boolean result;
        try {
            File pathDirs =  new File(path);
            if (!pathDirs.exists())
                pathDirs.mkdirs();

            secKeyFile = new File(path,SEC_FILE);

            if (writer == null)
                writer = new BufferedWriter(new FileWriter(secKeyFile));

            //update the key array
            keySet[index] = key;
            String line = "";

            //write the entire array in to the file
            for (int i = 0; i < 100; i++) {
                if (keySet[i] == null)
                    continue;

                String sIndex = String.valueOf(i);
                sIndex = ValidatorUtil.getInstance().zeroPadString(sIndex,3,false);
                line = "KEY_" + sIndex + "=" + keySet[i] + "\n";
                writer.write(line);
            }
            writer.flush();
            writer.close();
            writer = null;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}