package com.epic.pos.tlv;

/**
 * Created by harshana_m on 10/19/2018.
 */


import com.epic.pos.util.AppLog;


public class EMVParamKeyCaseA extends EMVParamKey {
    private static final String TAG = "EMVParamKeyCaseA";
    @Override
    public String append(int tag, String value) {
        int fixedTag = tag;
        String fixedValue = value;
        // THIS IS THE DEMO CODE !!!!, PLEASE FIX IT Refer YOUR Specification.
        if( tag == 0x9F23 ) {
            // fix the tag 0x9F23 to 0x9F22
            fixedTag = TAG_Index_9F22;
            AppLog.d(TAG, "reset tag " + Integer.toHexString(tag) + " -> " + Integer.toHexString(fixedTag));
        }
        return super.append(fixedTag, fixedValue);
    }
}
