package com.epic.pos.service;

import static com.epic.pos.common.Const.IS_TLE_ENABLE;
import static com.epic.pos.common.Const.PRINT_ISO_MSG;

import android.content.Context;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;
import com.epic.pos.util.AppLog;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericValidatingPackager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

public class SocketConnectionService  {
    private static DataInputStream datain = null;
    private static DataOutputStream dataout = null;
    private static Socket bankSocket = null;
    private static String XML_FILE_PATH = "iso"+ File.separator + "iso87binaryrec.xml";

    public static String responseTPDU = "";
    private Context context;
    int timeOut = 45;
    public SocketConnectionService(Context context) {
        this.context = context;
    }

    public ISOMsg getServerResponse(String hostIp, String hostPort, String request) throws Exception {

        DbHandler.getInstance().getTCT(tct -> {
            timeOut = tct.getConnectTimeout();
        });

        String retResponce;
        Socket bankSocket = null;
        DataOutputStream dataout = null;
        DataInputStream datain = null;

        String CORE_BANK_SWITCH_IP = hostIp;
        String CORE_BANK_SWITCH_PORT = hostPort;

        byte response[] = new byte[2048];
        byte printreq[] = null;
        byte actualResponse[] = null;
        byte requestBytes[] = null;

        byte originalRes[] = null;

        try {
            System.out.println("REQUEST " + request);
            requestBytes = ISOUtil.hex2byte(request);

            if(IS_TLE_ENABLE) {
                if(Const.PRINT_ENC_ISO_MSG)
                    printISOPacket(requestBytes, 1);

            } else if (PRINT_ISO_MSG){
                printISOPacket(requestBytes, 1);
            }


            timeOut = timeOut * 1000;
            InetAddress anetAdd = InetAddress.getByName(CORE_BANK_SWITCH_IP);

            System.out.println("--------   SOCKET COMMUNICATION DETAILS -----------------");
            System.out.println("------------- IP address   : " + CORE_BANK_SWITCH_IP + " ----------");
            System.out.println("------------  Port Address : " + CORE_BANK_SWITCH_PORT + " ---------");
            System.out.println("socket time out : " + timeOut);
            System.out.println("---------------------------------------------------------");

         //   bankSocket = new Socket(anetAdd, Integer.parseInt(CORE_BANK_SWITCH_PORT));
          //  bankSocket.setSoTimeout(timeOut);

            bankSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(anetAdd, Integer.parseInt(CORE_BANK_SWITCH_PORT));
           // bankSocket.setSoTimeout(timeOut);
            bankSocket.connect(socketAddress, timeOut);


            bankSocket.setSendBufferSize(8192);
            dataout = new DataOutputStream(bankSocket.getOutputStream());
            datain  = new DataInputStream(bankSocket.getInputStream());


            dataout.write(requestBytes);
            dataout.flush();
            System.out.println("RESPONSE ---> SIZE  " + datain.available() + "\n");
            System.out.println("RESPONSE ---> SIZE  " + response.length + "\n");

            int reslent = datain.read(response, 0, response.length);
            byte mHeader[] = new byte[5];

            originalRes  = Arrays.copyOfRange(response, 0, reslent);
            if(originalRes != null) {
                responseTPDU = ISOUtil.hexString(originalRes).substring(0,14);
            }
            System.out.println("originalRes ---> " + ISOUtil.hexString(originalRes));


            if(IS_TLE_ENABLE) {
                if(Const.PRINT_ENC_ISO_MSG)
                    printISOPacket(originalRes, 2);

            }else if (PRINT_ISO_MSG){
                printISOPacket(originalRes, 2);
            }

            InetAddress addr = bankSocket.getInetAddress();
            String ip = addr.getHostAddress();
            System.out.println("Recived massage length  [ " + reslent + " ] from [ " + ip + " ]");

             if (reslent > 0) {
                actualResponse = new byte[reslent - 7];
                printreq = new byte[reslent];
                byte tpdu[] = new byte[5];

                for (int i = 0; i < reslent; i++) {
                    printreq[i] = response[i];
                    if (i > 1) {
                        if (i < 7) {
                            tpdu[i - 2] = response[i];

                        } else {
                            actualResponse[i - 7] = response[i];
                            if (i < 12) {
                                mHeader[i - 7] = response[i];
                            }
                        }
                    }
                }

                System.out.println("RESPONSE ---> HEADER " + ISOUtil.hexString(mHeader) + "\n");
                retResponce = new String(actualResponse);


                ISOMsg m = new ISOMsg();
                InputStream is = context.getAssets().open(XML_FILE_PATH);
                GenericValidatingPackager packager = new GenericValidatingPackager(is);
                m.setPackager(packager);
                m.unpack(actualResponse);

                printPacket(m,"");

                return m;
            }
            throw new Exception();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                bankSocket.close();
                dataout.close();
                datain.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void printISOPacket(byte printBytes[], int type){
        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_ISO);
        p.setIsoSentType(type);
        p.setIsoData(printBytes);
        PosDevice.getInstance().addToPrintQueue(p);
    }

//    private void printReqAndRes(byte[] req, byte[] res){
//        PosDevice.getInstance().printISO(1, req, new PrintListener() {
//            @Override
//            public void onPrintFinished() {
//                PosDevice.getInstance().printISO(2, res, new PrintListener() {
//                    @Override
//                    public void onPrintFinished() {
//
//                    }
//
//                    @Override
//                    public void onPrintError(PrintError printError) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onPrintError(PrintError printError) {
//
//            }
//        });
//    }

    public void printPacket(ISOMsg m, String msgt) throws Exception {

        StringBuffer msg = new StringBuffer();
        msg.append("\n---------------------------------------------------\n");
        msg.append(msgt + "\n");
        msg.append("---------------------------------------------------\n");
        for (int i = 0; i < 128; i++) {
            if (m.hasField(i)) {
                msg.append("Element [" + i + "] " + m.getValue(i).toString() + "\n");

            }
        }
        msg.append("---------------------------------------------------\n");
        AppLog.i("PACKET ",msg.toString());
    }
}
