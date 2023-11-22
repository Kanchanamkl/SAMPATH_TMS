package com.epic.pos.util.spcrypto;

import com.epic.pos.util.Utility;
import com.epic.pos.util.ValidatorUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



/**
 * Created by harshana_m on 12/21/2018.
 */

public class DESCrypto
{
    public static byte[] encrypt3Des(String clearData, String key) throws Exception
    {
        final byte[] keydata1 = Utility.hexStr2Byte(key);
        SecretKeySpec symKey1 = new SecretKeySpec(keydata1 ,"DESede");

        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,symKey1);

        byte []encrypted =  cipher.doFinal(Utility.hexStr2Byte(clearData));

        return encrypted;
    }

    public static byte[] encrypt3DesBytes(byte[] data, String key) throws Exception
    {
        final byte[] keydata1 = Utility.hexStr2Byte(key);
        SecretKeySpec symKey1 = new SecretKeySpec(keydata1 ,"DESede");

        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,symKey1);

        byte []encrypted =  cipher.doFinal(data);

        return encrypted;
    }





    public static byte[] encrypt3DesWithCBCTLEPadding(String clearData, String key) throws Exception
    {
        byte [] encrypted = null;
        final byte[] keydata1 = Utility.hexStr2Byte(key);
        SecretKeySpec symKey1 = new SecretKeySpec(keydata1 ,"DESede");

        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        byte[] x = new byte[8];
        IvParameterSpec iv = new IvParameterSpec(x);
        cipher.init(Cipher.ENCRYPT_MODE,symKey1,iv);

        if (clearData.length() % 2 != 0)
            clearData += "0";

        encrypted = Utility.hexStr2Byte(clearData);

        int numBytes = encrypted.length;
        int numBytesToBePadded = 8  - (numBytes % 8);

        String padString = "";
        numBytesToBePadded *= 2;

        if (numBytesToBePadded > 0) {
            for (int i  = 0 ; i < numBytesToBePadded - 2; i++)
                padString += "0";

            String howManyPadded = Integer.toHexString(numBytesToBePadded / 2);
            howManyPadded = ValidatorUtil.getInstance().zeroPadString(howManyPadded,2);
            padString += howManyPadded;
            clearData += padString;
        }

        encrypted = Utility.hexStr2Byte(clearData);
        encrypted = cipher.doFinal(encrypted);

        return encrypted;
    }

    public static byte[] decrypt3DesCBCTLENoPaddingOrgAndroid5(String clearData, String key) throws Exception {
        final byte[] keydata1 = Utility.hexStr2Byte(key);
        SecretKeySpec symKey1 = new SecretKeySpec(keydata1 ,"DESede");

        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,symKey1);

        byte []decrypted =  cipher.doFinal(Utility.hexStr2Byte(clearData));

        return decrypted;
    }

    public static byte[] decrypt3DesCBCTLENoPadding(String clearData, String key) throws Exception {
        String iv = "00000000";
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        byte[] keydata1 = Utility.hexStr2Byte(key);

        if(keydata1.length == 16) {
            byte[] tempkey = new byte[24];
            System.arraycopy(keydata1, 0, tempkey, 0, 16);
            System.arraycopy(keydata1, 0, tempkey, 16, 8);
            keydata1 = tempkey;
        }

        SecretKeySpec symKey1 = new SecretKeySpec(keydata1 ,"DESede");

        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,symKey1,ivSpec);

        byte []decrypted =  cipher.doFinal(Utility.hexStr2Byte(clearData));
        return decrypted;
    }
}