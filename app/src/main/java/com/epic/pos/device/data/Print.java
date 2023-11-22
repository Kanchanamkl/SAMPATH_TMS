package com.epic.pos.device.data;

import android.graphics.Bitmap;

import com.epic.pos.device.listener.PrintListener;

/**
 * CardType
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-04
 */
public class Print {

    public static final int PRINT_TYPE_ISO = 1;
    public static final int PRINT_TYPE_IMAGE = 2;
    public static final int PRINT_DATA_BUILDER = 3;

    //iso print data
    private int isoSentType;
    private byte[] isoData;
    //bitmap print
    private Bitmap bitmap;
    //common data
    private int printType;
    private PrintListener printListener;
    //print data list
    private PrintDataBuilder printDataBuilder;

    public int getPrintType() {
        return printType;
    }

    public void setPrintType(int printType) {
        this.printType = printType;
    }

    public int getIsoSentType() {
        return isoSentType;
    }

    public void setIsoSentType(int isoSentType) {
        this.isoSentType = isoSentType;
    }

    public byte[] getIsoData() {
        return isoData;
    }

    public void setIsoData(byte[] isoData) {
        this.isoData = isoData;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public PrintListener getPrintListener() {
        return printListener;
    }

    public void setPrintListener(PrintListener printListener) {
        this.printListener = printListener;
    }

    public PrintDataBuilder getPrintDataBuilder() {
        return printDataBuilder;
    }

    public void setPrintDataBuilder(PrintDataBuilder printDataBuilder) {
        this.printDataBuilder = printDataBuilder;
    }

    @Override
    public String toString() {
        return "Print{" +
                "isoSentType=" + isoSentType +
                ", bitmap=" + bitmap +
                ", printType=" + printType +
                ", printListener=" + printListener +
                '}';
    }
}
