package com.epic.pos.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by harshana_m on 10/23/2018.
 */

public class DataPacket {
    private Attrib fieldAtributes[];
    private Map<Integer, String> data8583 ;
    //private OnPacketLoadListener packetLoadListener = null;
    //public ISO_TXRX isotxrx = new ISO_TXRX();


    public DataPacket() {
      //  curTransaction = transaction;
        data8583 =  new HashMap<Integer, String>();
    }

    enum AttribFormat {
        ALPHA_NEUMERIC,
        OTHERS;
    }

    enum LenType {
        FIXED,
        VARIABLE;
    }

    class Attrib {
        int id;
        int length;
        AttribFormat format;
        LenType lenType;

        private Attrib(int i, int l,AttribFormat f,LenType ml) {
            id = i;
            length = l;
            format = f;
            lenType = ml;
        }
    }

    private void initFieldList() {
       Attrib attr;

       //initialize for all fields
       attr = new Attrib(2,19, AttribFormat.OTHERS, LenType.VARIABLE);
       fieldAtributes[1] = attr;

    }

    private boolean validateCallbackoutput(int field, String data) {
        return true;
    }

    private String tpdu;

    public void setPacketTPDU(String packetTpdu) {
        tpdu = packetTpdu;
    }

    public String injectStepSpaces(int stepSize, String str) {
        String strsub = "";
        String strext = "";

        for (int i = 0 ; i < str.length(); i++) {
            if (i + stepSize <= str.length())
                strext = str.substring(i, i + stepSize);
            else
                strext = str.substring(i, str.length());

            strsub += strext ;
            strsub += " ";

            i += (stepSize - 1);
        }
        return strsub.trim();
    }

   /* public void setOnPacketLoadListener(OnPacketLoadListener listener) {
        packetLoadListener = listener;
    }
    */

    private String padZerosInfront(String data, int numZeros) {
        String s = "";

        for (int i  = 0 ; i < numZeros; i++) {
            s += "0";
        }

        return s + data;
    }


    //custom exception to be thrown
    public class InvalidData extends Exception {
        int fieldID;
        String cause;

        InvalidData(int field, String msg) {
            fieldID = field;
            cause = msg;
        }
        @Override
        public String getMessage() {
           return cause + " Field : " + fieldID;
        }
    }

  /*  //get the row packet
    public byte[] getRawDataPacket() {
        return getRawDataPacketWithoutMac();
    }*/

    public byte[] getRawDataPacketWithMac() {
        ISO8583u iso8583u = new ISO8583u();
        iso8583u.setHeader("6000110000");
        byte[] packet       =  iso8583u.makePacket( data8583, ISO8583.PACKET_TYPE.PACKET_TYPE_HEXLEN_BUF );
        String rawHexPacket = Utility.byte2HexStr(packet);

        String packetSize = rawHexPacket.substring(0,4);
        String data       = rawHexPacket.substring(14,rawHexPacket.length());
        rawHexPacket      = packetSize + tpdu + data;
        packet            = Utility.hexStr2Byte(rawHexPacket);

        return packet;
    }

    public  byte[] getRawDataPacketWithMap(Map<Integer, String> mm) {
        ISO8583u iso8583u = new ISO8583u();
        iso8583u.setHeader("6000110000");
        data8583 = mm;
        byte[] packet =  iso8583u.makePacket( data8583, ISO8583.PACKET_TYPE.PACKET_TYPE_HEXLEN_BUF );
        String rawHexPacket = Utility.byte2HexStr(packet);


        String packetSize = rawHexPacket.substring(0,4);
        String data = rawHexPacket.substring(14,rawHexPacket.length());
        rawHexPacket = packetSize + "6000110000" + data;
        packet = Utility.hexStr2Byte(rawHexPacket);

        return packet;
    }

   /* private byte[] getRawDataPacketWithoutMac() {
        ISO8583u iso8583u = new ISO8583u();
        iso8583u.setHeader("6000110000");
        byte[] packet =  iso8583u.makePacket( data8583, ISO8583.PACKET_TYPE.PACKET_TYPE_HEXLEN_BUF );
        String rawHexPacket = Utility.byte2HexStr(packet);


        if (!iso8583u.unpack(packet))
            return null;

        String packetSize = rawHexPacket.substring(0,4);

        if(iso8583u.unpackValidField[64]) //there is valid 64 content so we do not strip it
        {
            String data = rawHexPacket.substring(14,rawHexPacket.length());
            rawHexPacket = packetSize + tpdu + data;
            packet = Utility.hexStr2Byte(rawHexPacket);
            return packet;
        }

        //recalculate the size of the packet without the mac
        int sizeWithoutMac = Integer.parseInt(packetSize,16);
        sizeWithoutMac -= 9; //remove the mac from the packet

        packetSize = Integer.toHexString(sizeWithoutMac);
        packetSize = Formatter.fillInFront("0",packetSize,4);
        String data = rawHexPacket.substring(14,rawHexPacket.length() - 18);
        rawHexPacket = packetSize + tpdu + data;
        packet = Utility.hexStr2Byte(rawHexPacket);

        return packet;
    }*/
}

