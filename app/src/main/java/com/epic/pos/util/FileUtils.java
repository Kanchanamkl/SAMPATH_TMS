package com.epic.pos.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private final String TAG = FileUtils.class.getSimpleName();

    private String DIR_FONTS = "fonts";
    private String FONT_NORMAL = "IBMPlexMono-Medium.ttf";
    private String FONT_BOLD = "IBMPlexMono-Bold.ttf";

    private Handler handler = new Handler(Looper.getMainLooper());
    private static FileUtils instance;
    private Context context;

    /**
     * Get font normal
     *
     * @return
     */
    public File getFontNormal() {
        return new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + DIR_FONTS
                + File.separator + FONT_NORMAL);
    }

    /**
     * get font bold
     *
     * @return
     */
    public File getFontBold() {
        return new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + DIR_FONTS
                + File.separator + FONT_BOLD);
    }


    /**
     * Copy asset fonts in to cache dir
     *
     * @throws Exception
     */
    public void copyFontsIfNotExists() throws Exception {
        log("copyFontsIfNotExists()");

        File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + DIR_FONTS);

        if (!dir.exists()) {
            log("Fonts folder not exists. Create folder.");
            dir.mkdirs();
        } else {
            log("Font folder exists");
        }

        //Copy normal font file
        File fontNormalFile = new File(dir + File.separator + FONT_NORMAL);
        boolean shouldCopyNormalFont = false;
        if (fontNormalFile.exists()) {
            log("Normal font file already exists.");
            if (fontNormalFile.length() <= 0) {
                log("Normal font file size is 0");
                shouldCopyNormalFont = true;
            }
        } else {
            log("Normal font file not exists.");
            shouldCopyNormalFont = true;
        }

        if (shouldCopyNormalFont) {
            fontNormalFile.createNewFile();
            InputStream is = context.getAssets().open(DIR_FONTS + File.separator + FONT_NORMAL);
            copy(is, fontNormalFile);
            log("Normal font file coped.");
        }

        //Copy bold font file
        File fontBoldFile = new File(dir + File.separator + FONT_BOLD);
        boolean shouldCopyBoldFont = false;
        if (fontBoldFile.exists()) {
            log("Bold font file already exists.");
            if (fontBoldFile.length() <= 0) {
                log("Bold font file size is 0");
                shouldCopyBoldFont = true;
            }
        } else {
            log("Bold font file not exists.");
            shouldCopyBoldFont = true;
        }

        if (shouldCopyBoldFont) {
            fontBoldFile.createNewFile();
            InputStream is = context.getAssets().open(DIR_FONTS + File.separator + FONT_BOLD);
            copy(is, fontBoldFile);
            log("bold font coped");
        }
    }

    private void copy(InputStream is, File sourceFile) throws Exception {
        try {
            try (OutputStream output = new FileOutputStream(sourceFile)) {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = is.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }

                output.flush();
            }
        } finally {
            is.close();
        }
    }


    private void log(String msg){
        AppLog.i(TAG, msg);
    }

    private FileUtils() {
        super();
        instance = this;
    }

    private void init(Context appContext) {
        context = appContext;
    }

    public static FileUtils getInstance() {
        if (instance != null) {
            return instance;
        }

        throw new RuntimeException("ImageUtils class not correctly instantiated. " +
                "Please call FileUtils.Builder().setContext(context).build();" +
                " in the Application class onCreate.");
    }


    public final static class Builder {

        private Context mContext;

        public Builder setContext(final Context context) {
            mContext = context;
            return this;
        }

        public void build() {
            if (mContext == null) {
                throw new RuntimeException("Context not set, please set context before building the FileUtils instance.");
            }

            new FileUtils().init(mContext);
        }
    }
}
