package com.epic.pos.device;

import android.os.RemoteException;

import com.epic.pos.util.AppLog;

/**
 * Created by Simon on 2019/2/15.
 */
public class SerialCom {
    static final String TAG = "EMVDemo-SerialCom";

//    IExternalSerialPort iExternalSerialPort = null;    // for RS232 in the Base
//    ISerialPort iSerialPort = null;    // for USB cable, the UART port the PC side. need install the driver
//    IUsbSerialPort iUsbSerialPort = null;  // for OTG+USB2Serial

    private int bps;
    private int par;
    private int dbs;

    private boolean isOpend = false;

    public enum SerialType {
        UART,
        RS232,
        OTGSerial
    };

    private SerialType serialType;

    public void initialize(SerialType serialType, int bps, int par, int dbs ) {
        if( isOpend ) {
            if( this.serialType != serialType
                    || this.bps != bps
                    || this.par != par
                    || this.dbs != dbs ) {
                close();
            }
        }
        this.serialType = serialType;
        this.bps = bps;
        this.par = par;
        this.dbs = dbs;
    }

    public boolean open(){
        if( isOpend )
            return true;
        AppLog.d(TAG, "try open" + " serial type:" + serialType.ordinal() );

        boolean ret = false;
        switch ( serialType ){
            case UART:
//                if( null == iSerialPort ) {
//                    iSerialPort = PosDevice.getInstance().getSerialPort();
//                }
//                try {
//                    if( iSerialPort.open() ) {
//                        ret = iSerialPort.init(115200, 0, 8);
//                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }

                break;
            case RS232:
//                if( null == iExternalSerialPort ) {
//                    iExternalSerialPort = PosDevice.getInstance().getExternalSerialPort();
//                }
//                try {
//                    if( ExternalSerialConst.MODE_TRANSPARENT  == iExternalSerialPort.setExtPinpadPortMode( ExternalSerialConst.MODE_TRANSPARENT ) ) {   // normal mode
//                        // set ok
//                        SerialDataControl dataControl = new SerialDataControl(ExternalSerialConst.BD115200, ExternalSerialConst.DATA_8, ExternalSerialConst.DSTOP_1, ExternalSerialConst.DPARITY_N);
//                        ret = iExternalSerialPort.openSerialPort(ExternalSerialConst.PORT_RS232, dataControl );
//                        if( ret ){
//                            ret = iExternalSerialPort.isExternalConnected();
//                        } else {
//                            AppLog.e(TAG, "error while openSerialPort");
//                        }
//                    } else {
//                        AppLog.e(TAG, "error open port");
//                    }
//
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                break;
        }
        if( ret ){
            isOpend = true;
            AppLog.d(TAG, "open success");
        } else {
            AppLog.e(TAG, "open fails");
        }
        return ret;
    }

    public int read( byte[] buffer, int expectLength, int timeout_ms ){
        int ret = 0;
        AppLog.d(TAG, "Try read, expect size:" + expectLength +" , timeout:" + timeout_ms + " ,serial type:" + serialType.ordinal() );
        if( !isOpend ){
            open();
        }
        if( isOpend ){
            // read
            switch ( serialType ){
                case UART:
//                    try {
//                        ret = iSerialPort.read(buffer,expectLength,timeout_ms);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }

                    break;
                case RS232:
//                    try {
//                        ret = iExternalSerialPort.readSerialPort(ExternalSerialConst.PORT_RS232, buffer, expectLength);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                    break;
            }
        }
        AppLog.e(TAG, "return size:" + ret);
        return ret;
    }

    public int write( byte[] buffer, int length ){
        int ret = 0;
        AppLog.d(TAG, "try write size:" + length + " ,serial type:" + serialType.ordinal() );
        if( !isOpend ){
            open();
        }
        if( isOpend ){
            // write
            switch ( serialType ){
                case UART:
//                    try {
//                        ret = iSerialPort.write(buffer, length );
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                    break;
                case RS232:
//                    try {
//                        ret = iExternalSerialPort.writeSerialPort(ExternalSerialConst.PORT_RS232, buffer, length);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                    break;
            }
        }
        AppLog.d(TAG, "write return:" + ret);
        return ret;
    }

    private void close(){
        switch ( serialType ){
            case UART:
//                try {
//                    iSerialPort.close();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                break;
            case RS232:
//                try {
//                    iExternalSerialPort.closeSerialPort( ExternalSerialConst.MODE_TRANSPARENT );
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                break;
            case OTGSerial:
                //
                break;
        }
        isOpend = false;
    }
}