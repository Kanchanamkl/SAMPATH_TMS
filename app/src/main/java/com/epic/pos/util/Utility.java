package com.epic.pos.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by on 10/19/2018.
 */

public class Utility {

    public static String getPanSequenceNumber(String tlv) {
        String panSequenceNumber = "";
        if (tlv.toUpperCase().contains("5F3401")) {
            int startIdx = tlv.indexOf("5F3401") + 6;
            int endIdx = startIdx + 2;
            panSequenceNumber = tlv.substring(startIdx, endIdx);
        }
        return panSequenceNumber;
    }

    public static String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public static String byte2HexStr(byte[] var0, int offset, int length) {

        int var3 = 0;
        try {
            if (var0 == null) {
                return "";
            } else {
                String var1 = "";
                StringBuilder var2 = new StringBuilder("");

                for (var3 = offset; var3 < (offset + length); ++var3) {
                    var1 = Integer.toHexString(var0[var3] & 255);
                    var2.append(var1.length() == 1 ? "0" + var1 : var1);
                }

                return var2.toString().toUpperCase().trim();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public static String byte2HexStr(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            String var1 = "";
            StringBuilder var2 = new StringBuilder("");

            for (int var3 = 0; var3 < var0.length; ++var3) {
                var1 = Integer.toHexString(var0[var3] & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }


    public static byte[] hexStr2Byte(String hexString) {
//        Log.d(TAG, "hexStr2Byte:" + hexString);
        if (hexString == null || hexString.length() == 0) {
            return new byte[]{0};
        }
        String hexStrTrimed = hexString.replace(" ", "");
//        Log.d(TAG, "hexStr2Byte:" + hexStrTrimed);
        {
            String hexStr = hexStrTrimed;
            int len = hexStrTrimed.length();
            if ((len % 2) == 1) {
                hexStr = hexStrTrimed + "0";
                ++len;
            }
            char highChar, lowChar;
            int high, low;
            byte result[] = new byte[len / 2];
            String s;
            for (int i = 0; i < hexStr.length(); i++) {
                // read 2 chars to convert to byte
//                s = hexStr.substring(i,i+2);
//                int v = Integer.parseInt(s, 16);
//
//                result[i/2] = (byte) v;
//                i++;
                // read high byte and low byte to convert
                highChar = hexStr.charAt(i);
                lowChar = hexStr.charAt(i + 1);
                high = CHAR2INT(highChar);
                low = CHAR2INT(lowChar);
                result[i / 2] = (byte) (high * 16 + low);
                i++;
            }
            return result;

        }
    }

    public static byte[] hexStr2Byte(String hexString, int beginIndex, int length) {
        if (hexString == null || hexString.length() == 0) {
            return new byte[]{0};
        }
        {
            if (length > hexString.length()) {
                length = hexString.length();
            }
            String hexStr = hexString;
            int len = length;
            if ((len % 2) == 1) {
                hexStr = hexString + "0";
                ++len;
            }
            byte result[] = new byte[len / 2];
            String s;
            for (int i = beginIndex; i < len; i++) {
                s = hexStr.substring(i, i + 2);
                int v = Integer.parseInt(s, 16);

                result[i / 2] = (byte) v;
                i++;
            }
            return result;

        }
    }

    public static byte HEX2DEC(int hex) {
        return (byte) ((hex / 10) * 16 + hex % 10);
    }

    public static int DEC2INT(byte dec) {
        int high = ((0x007F & dec) >> 4);
        if (0 != (0x0080 & dec)) {
            high += 8;
        }
        return (high) * 10 + (dec & 0x0F);
    }

    public static int CHAR2INT(char c) {
        if (
                (c >= '0' && c <= '9')
                        || (c == '=')
        ) {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else {
            return 0;
        }

    }


    public static String getValueofTag(String TLVString, String tag) {
        String data = "";
        int index = TLVString.indexOf(tag);
        if (index > 0) {
            //found the tag
            //get the next byte for the length
            String tagLenString = TLVString.substring(index + tag.length(), index + tag.length() + 2);
            int inLen = Integer.valueOf(tagLenString);
            int index_to_data = index + tag.length() + tagLenString.length();

            //extract the data portion from the tlv
            data = TLVString.substring(index_to_data, index_to_data + (inLen * 2));
            return data;
        }

        return null;
    }

    public static String getCurrentTime() {
        SimpleDateFormat dt = new SimpleDateFormat("HH/mm/ss");
        String currentTime = dt.format(new Date());
        return currentTime;
    }

    public static String getCurrentDate() {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yy");
        String currentDate = dt.format(new Date());
        //currentDate = currentDate.substring(2,currentDate.length());
        return currentDate;
    }

    public static String getCurrentDateandTime() {
     //   SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yy");
     //   String currentDate = dt.format(new Date());
        //currentDate = currentDate.substring(2,currentDate.length());
        return String.valueOf(new Date());
    }
    public static String getCurrentDateTimeInISO8601() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        TimeZone customTimeZone = TimeZone.getTimeZone("GMT+05:30");
        dateFormat.setTimeZone(customTimeZone);
        Date currentDate = new Date();
        // Format the date and time as a string
        String formattedDate = dateFormat.format(currentDate);

        return formattedDate;
    }
    public static String getCurrentYearMonth() {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yy");
        String currentDate = dt.format(new Date());
        currentDate = currentDate.substring(2, currentDate.length());
        return currentDate;
    }

    public static String asciiToString(String asciiString) {
        int start = 0;
        int end = 2;

        String char_value = "";
        String str = "";

        while (end <= asciiString.length()) {
            char_value = asciiString.substring(start, end);
            int val = Integer.valueOf(char_value, 16);
            str += Character.toString((char) val);
            start = end;
            end += 2;
        }

        return str;
    }

    public static String stringToAscii(String string) {
        int char_value = 0;
        String asciiStr = "";

        for (int i = 0; i < string.length(); i++) {
            char_value = (int) string.charAt(i);
            asciiStr += String.format("%02X", (int) char_value);
        }
        return asciiStr;
    }


    public static byte[] xorArrays(byte[] a, byte[] b) {
        byte[] xor = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            xor[i] = (byte) (a[i] ^ b[i]);
        }

        return xor;
    }

    public static String xorHexString(String a, String b) {
        byte[] xor = xorArrays(hexStr2Byte(a), hexStr2Byte(b));
        String xorStr = byte2HexStr(xor);
        return xorStr;
    }

    public static String convertHexToBinary(String hex) {

        StringBuilder binStrBuilder = new StringBuilder();
        int c = 1;
        for (int i = 0; i < hex.length() - 1; i += 2) {

            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);

            String binStr = Integer.toBinaryString(decimal);
            int len = binStr.length();
            StringBuilder sbf = new StringBuilder();

            if (len < 8) {

                for (int k = 0; k < (8 - len); k++) {
                    sbf.append("0");
                }
                sbf.append(binStr);
            } else {
                sbf.append(binStr);
            }

            c++;
            binStrBuilder.append(sbf.toString());
        }

        return binStrBuilder.toString();
    }

    /**
     * Get formatted amount
     *
     * @param amount
     * @return
     */
    public static String getFormattedAmount(long amount) {
        String cleanString = String.valueOf(amount);
        String formatted;

        if (amount != 0) {
            if ((cleanString.charAt(cleanString.length() - 1) == '0') && (cleanString.charAt(cleanString.length() - 2) == '0')) {
                double parsed = Double.parseDouble(cleanString);
                NumberFormat numberFormat = new DecimalFormat("#,###.##");
                formatted = numberFormat.format(parsed / 100) + ".00";
            } else if (cleanString.charAt(cleanString.length() - 1) == '0') {
                double parsed = Double.parseDouble(cleanString);
                NumberFormat numberFormat = new DecimalFormat("#,###.##");
                formatted = numberFormat.format(parsed / 100) + "0";
            } else {
                double parsed = Double.parseDouble(cleanString);
                NumberFormat numberFormat = new DecimalFormat("#,###.##");
                formatted = numberFormat.format(parsed / 100);
            }
        } else formatted = "0.00";

        return formatted;
    }
    public static String getFormattedAmountDCCRate(long amount) {
        String cleanString = String.valueOf(amount);
        String formatted;

        if (amount != 0) {
            if ((cleanString.charAt(cleanString.length() - 1) == '0') && (cleanString.charAt(cleanString.length() - 2) == '0')) {
                double parsed = Double.parseDouble(cleanString);
                NumberFormat numberFormat = new DecimalFormat("#,###.##");
                formatted = numberFormat.format(parsed / 100) + ".00";
            } else if (cleanString.charAt(cleanString.length() - 1) == '0') {
                double parsed = Double.parseDouble(cleanString);
                NumberFormat numberFormat = new DecimalFormat("#,###.##");
                formatted = numberFormat.format(parsed / 100) + "0";
            } else {
                double parsed = Double.parseDouble(cleanString);
                NumberFormat numberFormat = new DecimalFormat("#,###.##");
                formatted = numberFormat.format(parsed / 100);
            }
        } else formatted = "0.00";

        return formatted;
    }
    /**
     * Get formatted date
     * Works for short dates such as 05/13
     *
     * @param date
     * @return
     */
    public static String getFormattedDate(String date) {
        if (date.length() == 4)
            return date.substring(0, 2) + "/" + date.substring(2, 4);
        else
            throw new NumberFormatException("Date length should be 4.");
    }

    public static String getdateformatedmmdd() {

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        return  formattedDate;

    }

    public static String gettimeformatedHHmmss() {

        // Get the current time
        Date currentTime = new Date();
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        // Format the time as a string
        String formattedTime = timeFormat.format(currentTime);
        return  formattedTime;

    }

    /**
     * Get formatted time
     *
     * @param time
     * @return
     */
    public static String getFormattedTime(String time) {
        if (time.length() == 6)
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        else
            throw new NumberFormatException("Time should be in hhmmss (length should be 6).");
    }

    /**
     * Applies the specified mask to the card number.
     *
     * @param cardNumber The card number in plain format
     * @param mask       The number mask pattern. Use N to include a digit from the
     *                   card number at that position, use * to skip the digit at that position
     * @return The masked card number
     */
    public static String maskCardNumber(String cardNumber, String mask) {

        // format the number
        int index = 0;
        StringBuilder maskedNumber = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == 'N') {
                maskedNumber.append(cardNumber.charAt(index));
                index++;
            } else if (c == '*') {
                maskedNumber.append(c);
                index++;
            } else {
                maskedNumber.append(c);
            }
        }

        // return the masked number
        return maskedNumber.toString();
    }

    public static String getMaskingFormat(String cardNumber) {
        switch (cardNumber.length()) {
            case 9:
                return "**** *NNNN";
            case 10:
                return "**** **NNNN";
            case 11:
                return "**** ***NN NN";
            case 12:
                return "**** **** NNNN";
            case 13:
                return "**** ***** NNNN";
            case 14:
                return "**** ****** NNNN";
            case 15:
                return "**** ****** *NNNN";
            case 17:
                return "**** **** **** *NNN N";
            case 18:
                return "**** **** **** **NN NN";
            case 19:
                return "**** **** **** ***N NNN";
            default:
                return "**** **** **** NNNN";
        }
    }


}
