package com.epic.pos.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Hash {
    public static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }

    public static String getSHA256Hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return bin2hex(hash); // make it printable
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String mpiMd5(String value) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(value.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6);
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
