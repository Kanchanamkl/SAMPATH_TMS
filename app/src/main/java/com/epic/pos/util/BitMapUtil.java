package com.epic.pos.util;

import android.text.TextUtils;

public class BitMapUtil {


    public static final int SALE = 0;
    public static final int OFFLINE_SALE = 1;
    public static final int MANUAL_SALE = 2;
    public static final int OFFLINE_MANUAL_SALE = 3;
    public static final int PRE_AUTH = 4;
    public static final int REFUND = 5;
    public static final int PRE_AUTH_MANUAL = 6;
    public static final int REFUND_MANUAL = 7;
    public static final int CASH_ADVANCE = 8;
    public static final int INSTALLMENT = 9;
    public static final int CASH_BACK = 10;
    public static final int QUASI_CASH = 11;
    public static final int QUASI_CASH_MANUAL = 12;
    public static final int STD_REF_SALE = 13;


    /**
     * BIN wise feature status check method
     *
     * @param bitmap  Bit map has 13 digits each digit represents specific feature is enabled or not.
     *                (0 or 1) <br>
     *                01. Sale<br>
     *                02. Offline Sale<br>
     *                03. Manual Sale<br>
     *                04. Offline Manual Sale<br>
     *                05. Pre-Authorization<br>
     *                06. Refund<br>
     *                07. Pre-Auth Manual<br>
     *                08. Refund Manual<br>
     *                09. Cash Advance<br>
     *                10. Instalment<br>
     *                11. Cash Back<br>
     *                12. Quasi Cash<br>
     *                13. Quasi Cash Manual<br>
     *                14. Student Ref Sale<br>
     * @param feature feature index (0 to 12)
     * @return
     */
    public static boolean isFeatureEnabled(String bitmap, int feature) {
        if (!TextUtils.isEmpty(bitmap) && bitmap.length() >= 14 && feature <= 13) {
            String bit = String.valueOf(bitmap.toCharArray()[feature]);
            return bit.equals("1");
        } else {
            return false;
        }
    }


}
