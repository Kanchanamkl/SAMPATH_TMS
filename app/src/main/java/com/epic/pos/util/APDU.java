package com.epic.pos.util;

/**
 * Created by harshana_m on 12/19/2018.
 */

public class APDU
{
    public static byte[] ContructAPDU(String command, String data)
    {
        String sLen =  "";

        if (data == null) {
            sLen = "00";
            data = "";
        }
        else
        {
            int len =  data.length();
            len /= 2;                   //actual byte length of the command
            sLen = Integer.toHexString(len);     //convert the value to hex
        }

        if (sLen.length()  == 1)
            sLen = "0" + sLen;

        //construct the command
        String apdu = command + sLen + data;
        return Utility.hexStr2Byte(apdu);
    }

    public static byte[] ContructAPDUWithString(String command, String data)
    {
        String sLen =  "";

        if (data == null)
            sLen = "00";
        else
        {
            int len =  data.length();
            sLen = Integer.toHexString(len);     //convert the value to hex
        }

        if (sLen.length()  == 1)
            sLen = "0" + sLen;

        //construct the command
        String apdu = command + sLen + data;
        return Utility.hexStr2Byte(apdu);
    }


}
