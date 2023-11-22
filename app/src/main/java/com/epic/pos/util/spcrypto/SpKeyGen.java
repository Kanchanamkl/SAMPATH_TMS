package com.epic.pos.util.spcrypto;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


/**
 * Values
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-25
 */
public class SpKeyGen {

    /**
     * Generate AES 128bit key
     *
     * @return
     */
    public static String generateAESKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecretKey skey = kgen.generateKey();
            return Base64.encodeToString(skey.getEncoded(), Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Generaet AES 16 bytes IV Inet Vector
     *
     * @return
     */
    public static String generateAESInitVector() {
        byte[] iv = new byte[128 / 8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return Base64.encodeToString(iv, Base64.DEFAULT);
    }


}
