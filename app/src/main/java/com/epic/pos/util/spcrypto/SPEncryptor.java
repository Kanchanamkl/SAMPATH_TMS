package com.epic.pos.util.spcrypto;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Shared Pref SPEncryptor Class
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-25
 */
public class SPEncryptor {

    private static final String TAG = "Encryptor";

    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv      = new IvParameterSpec(Base64.decode(initVector, Base64.DEFAULT));
            SecretKeySpec skeySpec  = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES");
            Cipher cipher           = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted        = cipher.doFinal(value.getBytes());
            String encryptedString  = Base64.encodeToString(encrypted, Base64.DEFAULT);
            return encryptedString;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String encrypt(String key, String value) {
        try {
            String initVector       = "s2iD06R4e20PnqTg";
            IvParameterSpec iv      = new IvParameterSpec(initVector.getBytes("UTF-8"));
            byte[] keyBytes         = Base64.decode(key, Base64.DEFAULT);
            SecretKeySpec skeySpec  = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher           = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted        = cipher.doFinal(value.getBytes());
            String encryptedString  = Base64.encodeToString(encrypted, Base64.DEFAULT);

            return encryptedString;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String encrypted) {
        try {
            String initVector       = "s2iD06R4e20PnqTg";
            IvParameterSpec iv      = new IvParameterSpec(initVector.getBytes("UTF-8"));
            byte[] keyBytes        = Base64.decode(key, Base64.DEFAULT);
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher           = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original     = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv      = new IvParameterSpec(Base64.decode(initVector, Base64.DEFAULT));
            SecretKeySpec skeySpec  = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES");
            Cipher cipher           = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

}
