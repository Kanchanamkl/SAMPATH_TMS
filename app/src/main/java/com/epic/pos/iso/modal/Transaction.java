package com.epic.pos.iso.modal;

/**
 * Transaction is the parent of iso message building bean.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public class Transaction {

    public static final String POS_TXN                = "60";
    public static final String NII                    = "0011";
    public static final String DCCNII                 = "0060";
    public static final String SECURE_NII             = "0016";
    public static final String TO_NII                 = "0000";
    public static final String SALE_MTI               = "0200";
    public static final String OFFLINE_SALE_MTI       = "0220";
    public static final String OFFLINE_SALE_RES_MTI   = "0230";
    public static final String BATCH_UPLOAD_MTI       = "0320";
    public static final String BATCH_UPLOAD_RES_MTI   = "0330";
    public static final String REVERSAL_MTI           = "0400";
    public static final String REVERSAL_RES_MTI       = "0410";
    public static final String SETTLEMENT_MTI         = "0500";
    public static final String SETTLEMENT_RES_MTI     = "0510";
    public static final String SALE_RES_MTI           = "0210";
    public static final String MTI_KEY_DOWNLOAD       = "0800";
    public static final String QR_MTI                 = "0100";
    public static final String QR_NII                 = "0011";
    public static final String QR_TO_NII              = "0000";
    public static final String PRE_AUTH_MTI           = "0100";
    public static final String PRE_AUTH_MTI_RES       = "0110";
    public static final String AUTH_ONLY_MTI          = "0100";
    public static final String AUTH_ONLY_MTI_RES      = "0110";
    public static final String DCC__MTI               = "0100";
    public static final String MTI_DCC_DOWNLOAD       = "0800";
    //processing codes
    //sale
    public static final String SALE_PROCESSING_CODE             = "000000";
    public static final String SALE_PROCESSING_CODE_AMEX        = "000000";
    //refund
    public static final String REFUND_PROCESSONG_CODE           = "200000";
    public static final String REFUND_PROCESSONG_CODE_AMEX      = "204001";
    //void sale
    public static final String VOID_PROCESSING_CODE             = "024000";
    public static final String VOID_PROCESSING_CODE_AMEX        = "024000";
    //void refund
    public static final String VOID_REFUND_PROCESSING_CODE      = "220000";
    public static final String VOID_REFUND_PROCESSING_CODE_AMEX = "224000";
    //pre auth
    public static final String PRE_AUTH_PROCESSING_CODE         = "300000";
    public static final String PRE_AUTH_PROCESSING_CODE_AMEX    = "304000";
    //pre auth
    public static final String AUTH_ONLY_PROCESSING_CODE         = "300000";
    public static final String AUTH_ONLY_PROCESSING_CODE_AMEX    = "304000";
    //pre comp
    public static final String PRE_COMP_PROCESSING_CODE         = "000000";
    public static final String PRE_COMP_PROCESSING_CODE_AMEX    = "024001";
    //cash back
    public static final String CASH_BACK_PROCESSING_CODE        = "090000";
    public static final String CASH_BACK_PROCESSING_CODE_AMEX   = "090000";
    //quasi cash
    public static final String QUASI_CASH_PROCESSING_CODE       = "110000";
    public static final String QUASI_CASH_PROCESSING_CODE_AMEX  = "110000";
    //cash advance
    public static final String CASH_ADVANCE_PROCESSING_CODE       = "010000";
    public static final String CASH_ADVANCE_PROCESSING_CODE_AMEX  = "010000";

    //DCC
    public static final String DCC_PROCESSING_CODE       = "000000";


    public static final String POS_ENTRY_MODE_MAG_PIN_SUPPORT           = "21";
    public static final String POS_ENTRY_MODE_MAG_PIN_NOT_SUPPORT       = "22";
    public static final String POS_ENTRY_MODE_INSERT_PIN_SUPPORT        = "51";
    public static final String POS_ENTRY_MODE_INSERT_PIN_NOT_SUPPORT    = "52";
    public static final String POS_ENTRY_MODE_MANUAL_PIN_SUPPORT        = "11";
    public static final String POS_ENTRY_MODE_MANUAL_PIN_NOT_SUPPORT    = "12";
    public static final String POS_ENTRY_MODE_TAP_PIN_SUPPORT           = "71";
    public static final String POS_ENTRY_MODE_TAP_PIN_NOT_SUPPORT       = "72";
    public static final String POS_ENTRY_MODE_TAPMAG_PIN_SUPPORT        = "901";
    public static final String POS_ENTRY_MODE_TAPMAG_PIN_NOT_SUPPORT    = "902";
    public static final String POS_ENTRY_MODE_FALLBACK_PIN_SUPPORT      = "801";
    public static final String POS_ENTRY_MODE_FALLBACK_PIN_NOT_SUPPORT  = "802";

    public static String posEntryModeToString(String mode){
        if (mode.equals(POS_ENTRY_MODE_MAG_PIN_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_MAG_PIN_NOT_SUPPORT)){
            return "Swipe";
        }else if (mode.equals(POS_ENTRY_MODE_INSERT_PIN_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_INSERT_PIN_NOT_SUPPORT)){
            return "Chip";
        }else if (mode.equals(POS_ENTRY_MODE_TAP_PIN_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_TAP_PIN_NOT_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_TAPMAG_PIN_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_TAPMAG_PIN_NOT_SUPPORT)){
            return "CTLS";
        }else if (mode.equals(POS_ENTRY_MODE_MANUAL_PIN_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_MANUAL_PIN_NOT_SUPPORT)) {
            return "M";
        }else if (mode.equals(POS_ENTRY_MODE_FALLBACK_PIN_SUPPORT)
                || mode.equals(POS_ENTRY_MODE_FALLBACK_PIN_NOT_SUPPORT)){
            return "F";
        }else {
            return "-";
        }
    }

}
