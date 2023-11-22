package com.epic.pos.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class ImageUtils {

    private String DIR_IMAGES = "images";
    private String DIR_SIGNATURES = "signatures";
    private String DIR_SALE_RECEIPTS_CUS = "cus_sale_receipts";
    private String DIR_SALE_RECEIPTS_MER = "mer_sale_receipts";
    private String DIR_SETTLEMENT_RECEIPTS = "settlement_receipts";
    private String DIR_VOID_SALE_RECEIPTS_CUS = "cus_void_receipts";
    private String DIR_VOID_SALE_RECEIPTS_MER = "mer_void_receipts";


    private Handler handler = new Handler(Looper.getMainLooper());
    private static ImageUtils instance;
    private Context context;

    /**
     * Save void sale receipt image in application cache dir.
     *
     * @param bitmap
     * @param invoiceNo
     * @return
     * @throws Exception
     */
    public File saveMerchantVoidSaleReceipt(Bitmap bitmap, String merchantNo, String invoiceNo) throws Exception {
        File dir = new File(context.getCacheDir()
                + File.separator + DIR_IMAGES + File.separator + DIR_VOID_SALE_RECEIPTS_MER);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir + File.separator + merchantNo + "_" + invoiceNo + ".jpg");
        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        return file;
    }

    /**
     * Save void sale receipt image in application cache dir.
     *
     * @param bitmap
     * @param invoiceNo
     * @return
     * @throws Exception
     */
    public File saveCustomerVoidSaleReceipt(Bitmap bitmap, String merchantNo, String invoiceNo) throws Exception {
        File dir = new File(context.getCacheDir()
                + File.separator + DIR_IMAGES + File.separator + DIR_VOID_SALE_RECEIPTS_CUS);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir + File.separator + merchantNo + "_" + invoiceNo + ".jpg");
        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        return file;
    }


    /**
     * Save sale receipt image in application cache dir.
     *
     * @param bitmap
     * @param invoiceNo
     * @return
     * @throws Exception
     */
    public File saveCustomerSaleReceipt(Bitmap bitmap, String merchantNo, String invoiceNo) throws Exception {
        File dir = new File(context.getCacheDir()
                + File.separator + DIR_IMAGES + File.separator + DIR_SALE_RECEIPTS_CUS);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir + File.separator + merchantNo + "_" + invoiceNo + ".jpg");
        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        return file;
    }

    /**
     * Save sale receipt image in application cache dir.
     *
     * @param bitmap
     * @param invoiceNo
     * @return
     * @throws Exception
     */
    public File saveMerchantSaleReceipt(Bitmap bitmap, String merchantNo, String invoiceNo) throws Exception {
        File dir = new File(context.getCacheDir()
                + File.separator + DIR_IMAGES + File.separator + DIR_SALE_RECEIPTS_MER);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir + File.separator + merchantNo + "_" + invoiceNo + ".jpg");
        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        return file;
    }

    /**
     * Save sale receipt image in application cache dir.
     *
     * @param bitmap
     * @return
     * @throws Exception
     */
    public File saveSettlementReceipt(String hostName, String mid, Bitmap bitmap) throws Exception {
        File dir = new File(context.getCacheDir()
                + File.separator + DIR_IMAGES + File.separator + DIR_SETTLEMENT_RECEIPTS);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir + File.separator + "settlement_" + hostName + "_" + mid + ".jpg");
        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        return file;
    }

    public Bitmap getCustomerVoidSaleReceipt(String merchantNo, String invoiceNo) {
        File file = new File(context.getCacheDir() + File.separator
                + DIR_IMAGES + File.separator
                + DIR_VOID_SALE_RECEIPTS_CUS + File.separator
                + merchantNo + "_" + invoiceNo + ".jpg");
        return toBitmap(file);
    }

    public Bitmap getMerchantVoidSaleReceipt(String merchantNo, String invoiceNo) {
        File file = new File(context.getCacheDir() + File.separator
                + DIR_IMAGES + File.separator
                + DIR_VOID_SALE_RECEIPTS_MER + File.separator
                + merchantNo + "_" + invoiceNo + ".jpg");
        return toBitmap(file);
    }

    public Bitmap getCustomerSettlementReceipt(String hostName, String mid) {
        File file = new File(context.getCacheDir() + File.separator
                + DIR_IMAGES + File.separator
                + DIR_SETTLEMENT_RECEIPTS + File.separator
                + "settlement_" + hostName + "_" + mid + ".jpg");
        return toBitmap(file);
    }

    public Bitmap getCustomerSaleReceipt(String merchantNo, String invoiceNo) {
        File file = new File(context.getCacheDir() + File.separator
                + DIR_IMAGES + File.separator
                + DIR_SALE_RECEIPTS_CUS + File.separator
                + merchantNo + "_" + invoiceNo + ".jpg");
        return toBitmap(file);
    }

    public Bitmap getMerchantSaleReceipt(String merchantNo, String invoiceNo) {
        File file = new File(context.getCacheDir() + File.separator
                + DIR_IMAGES + File.separator
                + DIR_SALE_RECEIPTS_MER + File.separator
                + merchantNo + "_" + invoiceNo + ".jpg");
        return toBitmap(file);
    }

    /**
     * Get signature by invoice number
     *
     * @param invoiceNum
     * @return Bitmap
     */
    public Bitmap getSignature(String merchantNo, String invoiceNum) {
        try {
            File img = new File(context.getCacheDir() + File.separator
                    + DIR_IMAGES + File.separator
                    + DIR_SIGNATURES + File.separator
                    + merchantNo + "_" + invoiceNum + ".png");
            return toBitmap(img);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Save signature image in application cache dir.
     *
     * @param bitmap
     * @param merchantNo
     * @param invoiceNo
     * @param listener
     */
    public void saveSignature(Bitmap bitmap, String merchantNo, String invoiceNo, SaveSignatureListener listener) {
        originalSignature = bitmap;
        trimBitmap(trimBitmap -> {
            new Thread() {
                @Override
                public void run() {
                    try {
                        originalSignature.recycle();
                        File dir = new File(context.getCacheDir() + File.separator
                                + DIR_IMAGES + File.separator + DIR_SIGNATURES);

                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File file = new File(dir + File.separator + merchantNo + "_" + invoiceNo + ".png");
                        file.createNewFile();

                        FileOutputStream stream = new FileOutputStream(file);
                        trimBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();

                        handler.post(() -> listener.onSaved(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.post(() -> listener.onError(e));
                    }
                }
            }.start();
        });
    }

    /**
     * File to Bitmap
     *
     * @param file
     * @return
     */
    public Bitmap toBitmap(File file) {
        if (file != null && file.exists()) {
            return BitmapFactory.decodeFile(file.toString());
        } else {
            return null;
        }
    }

    //trim parems
    private Bitmap originalSignature = null;

    private boolean leftTrimCompleted = false;
    private boolean rightTrimCompleted = false;
    private boolean topTrimCompleted = false;
    private boolean bottomTrimCompleted = false;

    private int startWidth = 0;
    private int endWidth = 0;
    private int startHeight = 0;
    private int endHeight = 0;

    /**
     * This method is used to remove white spaces account the image.
     *
     * @param listener
     */
    public void trimBitmap(TrimBitmapListener listener) {
        leftTrimCompleted = false;
        rightTrimCompleted = false;
        topTrimCompleted = false;
        bottomTrimCompleted = false;
        startWidth = 0;
        endWidth = 0;
        startHeight = 0;
        endHeight = 0;

        int imgHeight = originalSignature.getHeight();
        int imgWidth = originalSignature.getWidth();

        //TRIM WIDTH - LEFT
        new Thread() {
            @Override
            public void run() {
                for (int x = 0; x < imgWidth; x++) {
                    if (startWidth == 0) {
                        for (int y = 0; y < imgHeight; y++) {
                            if (originalSignature.getPixel(x, y) != Color.WHITE) {
                                startWidth = x;
                                break;
                            }
                        }
                    } else break;
                }
                leftTrimCompleted = true;
                checkTrimBitmap(listener);
            }
        }.start();

        //TRIM WIDTH - RIGHT
        new Thread() {
            @Override
            public void run() {
                for (int x = imgWidth - 1; x >= 0; x--) {
                    if (endWidth == 0) {
                        for (int y = 0; y < imgHeight; y++) {
                            if (originalSignature.getPixel(x, y) != Color.WHITE) {
                                endWidth = x;
                                break;
                            }
                        }
                    } else break;
                }
                rightTrimCompleted = true;
                checkTrimBitmap(listener);
            }
        }.start();

        //TRIM HEIGHT - TOP
        new Thread() {
            @Override
            public void run() {
                for (int y = 0; y < imgHeight; y++) {
                    if (startHeight == 0) {
                        for (int x = 0; x < imgWidth; x++) {
                            if (originalSignature.getPixel(x, y) != Color.WHITE) {
                                startHeight = y;
                                break;
                            }
                        }
                    } else break;
                }
                topTrimCompleted = true;
                checkTrimBitmap(listener);
            }
        }.start();

        //TRIM HEIGHT - BOTTOM
        new Thread() {
            @Override
            public void run() {
                for (int y = imgHeight - 1; y >= 0; y--) {
                    if (endHeight == 0) {
                        for (int x = 0; x < imgWidth; x++) {
                            if (originalSignature.getPixel(x, y) != Color.WHITE) {
                                endHeight = y;
                                break;
                            }
                        }
                    } else break;
                }
                bottomTrimCompleted = true;
                checkTrimBitmap(listener);
            }
        }.start();
    }

    private void checkTrimBitmap(TrimBitmapListener listener) {
        if (leftTrimCompleted && rightTrimCompleted && topTrimCompleted && bottomTrimCompleted) {
            new Thread() {
                @Override
                public void run() {
                    Bitmap trimBitmap = Bitmap.createBitmap(
                            originalSignature,
                            startWidth,
                            startHeight,
                            endWidth - startWidth,
                            endHeight - startHeight
                    );
                    listener.onReceived(trimBitmap);
                }
            }.start();
        }
    }

    public interface TrimBitmapListener {
        void onReceived(Bitmap bitmap);
    }

    public interface GetSignatureListener {
        void onSignature(Bitmap bitmap);

        void onError(Exception ex);
    }

    public interface SaveReceiptListener {
        void onSaved(File path);

        void onError(Exception ex);
    }

    public interface SaveSignatureListener {
        void onSaved(File path);

        void onError(Exception ex);
    }


    private ImageUtils() {
        super();
        instance = this;
    }

    private void init(Context appContext) {
        context = appContext;
    }

    public static ImageUtils getInstance() {
        if (instance != null) {
            return instance;
        }

        throw new RuntimeException("ImageUtils class not correctly instantiated. " +
                "Please call ImageUtils.Builder().setContext(context).build();" +
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
                throw new RuntimeException("Context not set, please set context before building the ImageUtils instance.");
            }

            new ImageUtils().init(mContext);
        }
    }
}
