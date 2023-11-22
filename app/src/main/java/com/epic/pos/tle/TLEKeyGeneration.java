package com.epic.pos.tle;

import com.epic.pos.util.AppLog;

import com.epic.pos.util.Formatter;
import com.epic.pos.util.Utility;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class TLEKeyGeneration {

    public static String szKvcForUniKey = "";
    public static String szTLETXN_HEAD = "";
    public static int host = -1;
    public static String tid = "";
    public static String szTHUKID;
    public static String szTHAID;
    public static String szTHVSN;
    public static String szTHEAlgo;
    public static String szTHMAlgo;
    public static String szTHDMTR;


    public static byte[] unKsnCreate() {
        int count = 0;
        String szCount  = "",szEFgyn = "",szKeyid="",szKsn= "";
        byte[] unKsn    = new byte[8];

        szCount         = String.format("%06X",count);
        szEFgyn         = TLEKeyGeneration.szTleEFGyn();
        String str1     = szEFgyn.substring(0,1);
        szCount         = szCount.substring(1, szCount.length());
        szCount         = str1 + szCount;

        szKeyid         = szGetKeyIdGyn();
        szKeyid         = szKeyid.substring(0,6);
        szKsn           = szKeyid + tid + szCount;
        szKsn           = szKsn.substring(4,szKsn.length());
        //SVC_DSP_2_HEX(szKsn+4,(char*)unKsn,16);
        unKsn = Utility.hexStr2Byte(szKsn);
        return unKsn;
    }

    public static String szGetKeyIdGyn() {
        String szKEYIDGyn="";
        short shIndex = 0;
        byte[] unKEYIDGyn;

        switch(host) {
            case 0:
                shIndex=32;
                break;
            case 1:
                shIndex=35;
                break;
            case 2:
                shIndex=38;
                break;
            default:
                break;
        }

        unKEYIDGyn =  unDataFromSecureAria(shIndex);
        szKEYIDGyn = Utility.byte2HexStr(unKEYIDGyn,0,6);
        return szKEYIDGyn;
    }

/*
    public static int inGetHostGroupRef() {
        return host;
    }*/



    public static int inIncTxnCount() {
        long count=0L;
        String szCount="";
        short shIndex = 0;
        byte[] unEFgyn = new byte[1];

        switch(host) {
            case 0:
                shIndex = 31;
                count = inGetCounterGyn(shIndex);
                if(count==0xFFFFF) {
                    count = 0;
                    unEFgyn[0] = 0x0F;
                    shIndex--;
                    inSetEFGyn(unEFgyn,shIndex);
                }
                else
                    count = count + 1;

                szCount = String.format("%06X", count);
                inSetCountGyn(szCount,shIndex);
                break;

            case 1:
                shIndex = 34;
                count =inGetCounterGyn(shIndex);
                if(count==0xFFFFF) {
                    count = 0;
                    unEFgyn[0] = 0x0F;
                    shIndex--;
                    inSetEFGyn(unEFgyn,shIndex);
                }
                else
                    count = count + 1;

                //memset(szCount,0,sizeof(szCount));
                // sprintf(szCount,"%06X",count);
                //szCount[6]='\0';
                szCount = String.format("%06X", count);
                inSetCountGyn(szCount,shIndex);
                break;
        }
        return 0;
    }

    public static int inGetCounterGyn(short shIndex) {
        int inCount = 0;
        byte[] unTemp = new byte[16];
        byte[] unCount = new byte[3];
        String szCount="";
        unTemp  = unDataFromSecureAria(shIndex);
        try {
            szCount = Utility.byte2HexStr(unTemp,0,3);
            inCount = Integer.valueOf(szCount,16);

        }catch (Exception e){

        }
        return inCount;
    }

    public static int inSetEFGyn(byte[] unEFGyn,short shIndex) {
        inStoreSecureAria(unEFGyn,shIndex);
        return 0;
    }

    public static int inSetCountGyn(String szCountGyn, short shIndex) {
        byte[] unCount = new byte[3];

        unCount = Utility.hexStr2Byte(szCountGyn,0,6);
        inStoreSecureAria(unCount,shIndex);

        return 0;
    }

    public static long lnGetHostTxnCount() {
        long count=0L;
        short shIndex=0;

        switch(host) {
            case 0:
                shIndex=31;
                count =inGetCounterGyn(shIndex);
                break;

            case 1:
                shIndex=34;
                count =inGetCounterGyn(shIndex);
                break;

            case 2:
                break;
        }
        return count;
    }

    public static String szTleEFGyn() {
        short shIndex=0;
        String szEF = "";
        switch(host) {
            case 0:
                shIndex=30;
                szEF=szGetEFGyn(shIndex);
                break;

            case 1:
                shIndex=33;
                szEF=szGetEFGyn(shIndex);
                break;

            case 2:
                shIndex=36;
                szEF=szGetEFGyn(shIndex);
                break;

        }
	return szEF;
    }

    public static String szGetEFGyn(short shIndex)
    {
        String szEFGyn = "";

        byte[] unEFGyn = new byte[16];
        unEFGyn = unDataFromSecureAria(shIndex);
        szEFGyn = Utility.byte2HexStr(unEFGyn,0,1);
        szEFGyn = szEFGyn.substring(0,1);

        if (szEFGyn.equals("0"))
            szEFGyn = "E";
        else
            szEFGyn = "F";
        return szEFGyn;

    }

    public static void vdSetHostCountWhenBitCoutTen(long inCnt) {
        String szCount="";
        short shIndex=0;

        switch(host) {
            case 0:
                shIndex = 31;
                szCount = String.format("%06X", inCnt);
                inSetCountGyn(szCount,shIndex);
                break;

            case 1:
                shIndex = 34;
                szCount = String.format("%06X", inCnt);
                inSetCountGyn(szCount,shIndex);
                break;

            case 2:
                shIndex = 37;
                szCount = String.format("%06X", inCnt);
                inSetCountGyn(szCount,shIndex);
                break;
        }
    }


    public  static int inGETFUTUREKEYSLOTINDEX(int inHost) {
        int index = 0;

        if(inHost == 0)
            index = 40;

        else if(inHost == 1)
            index = 60;

        else if(inHost == 2)
            index = 80;

        return index;
    }

    public static byte[] unTripleDesEncpt(byte[] unClearData, byte[] unkey) throws Exception {
        byte [] unEncryptedData = new byte[16];
        SecretKeySpec symKey1 = new SecretKeySpec(unkey ,"DESede");
        Cipher cipher         = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,symKey1);
        unEncryptedData       =  cipher.doFinal(unClearData);
        return unEncryptedData;
    }

    public static byte[] unDesEncpt(byte[] unClearData, byte[] unkey) throws Exception {
        byte [] unEncryptedData;

        SecretKeySpec symKey1 = new SecretKeySpec(unkey ,"DES");

        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,symKey1);
        unEncryptedData =  cipher.doFinal(unClearData);

        return unEncryptedData;
    }

    public static void  inStoreSecureAria(byte[] unData, int inSlot) {
        String keyData = Utility.byte2HexStr(unData);
        KeyManager.setKeyInSlot(inSlot,keyData);
    }


    public static byte[] unDataFromSecureAria(int inSlot) {
        String keyHex = KeyManager.getKeyFromSlot(inSlot);
        byte[] unData = Utility.hexStr2Byte(keyHex);
        return unData;
    }


    public  static String szTLE_Header(String szSmartTid){

        String szKeyId = "",szTxnCount="",szEFgyn = "",szCount = "";
        long inTxnCount = 0;

        if(szTHUKID.equals("03") || szTHUKID.equals("04")) {

            inTxnCount  = lnGetHostTxnCount() - 1;
            szKeyId     = szGetKeyIdGyn();
            szTxnCount  = szKeyId.substring(0,6);
            szTxnCount  = szTxnCount + szSmartTid;
            szCount     = String.format("%05X",inTxnCount);
            szEFgyn     = szTleEFGyn();
            szTxnCount  = szTxnCount + szEFgyn + szCount;
        }


        szTLETXN_HEAD = "";
        szTLETXN_HEAD = szTHAID;
        szKeyId       = szGetKeyIdGyn();
        szTLETXN_HEAD += szKeyId;
        szTLETXN_HEAD += szTHVSN;
        szTLETXN_HEAD += szTHEAlgo;
        szTLETXN_HEAD += szTHUKID;
        szTLETXN_HEAD += szTHMAlgo;
        szTLETXN_HEAD += szTxnCount;
        szTLETXN_HEAD += szTHDMTR;
        szTLETXN_HEAD += inGetTHEMode();

        if(szTHAID.equals("0002")){
           szTLETXN_HEAD  = szTLETXN_HEAD +"08";
           String trail   = Formatter.fillInBack("0", szSmartTid,16 );
           szTLETXN_HEAD  = szTLETXN_HEAD + trail;
        }

        szTLETXN_HEAD = szTLETXN_HEAD +szKvcForUniKey.substring(0,6);
        TLE.tleHeaderLength = szTLETXN_HEAD.length();
        AppLog.i("IIIIIIIIIIIIIIIII ", "szTLETXN_HEAD = "+szTLETXN_HEAD);
        return szTLETXN_HEAD;
    }

    public static String inGetTHEMode() {
        return "01";
    }
}
