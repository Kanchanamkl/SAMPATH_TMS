package com.epic.pos.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Implemented by Sameera Jayarathna
 * https://www.logicchip.com/qr-code-with-logo/
 */
public class QRUtil {

    public static Bitmap createQRCode(String qrCodeData, String charset, Map hintMap, int qrCodeHeight,
                                      int qrCodewidth, Resources resources, int overlayResourceId, BarcodeFormat barcodeFormat) throws Exception {
        //generating qr code in bitmatrix type
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
                barcodeFormat, qrCodewidth, qrCodeHeight, hintMap);
        //converting bitmatrix to bitmap

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                //pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //setting bitmap to image view

//            Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.cardlogo);

        Bitmap overlay = BitmapFactory.decodeResource(resources, overlayResourceId);

        return (barcodeFormat.equals(BarcodeFormat.CODE_39) ? bitmap : mergeBitmaps(overlay, bitmap));
    }


    private static Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap) {

        overlay = Bitmap.createScaledBitmap(overlay, 30, 30, true);

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth - overlay.getWidth()) / 2;
        int centreY = (canvasHeight - overlay.getHeight()) / 2;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }
}
