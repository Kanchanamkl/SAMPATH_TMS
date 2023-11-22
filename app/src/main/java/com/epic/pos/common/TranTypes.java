package com.epic.pos.common;

public class TranTypes {

    public static final int SALE = 1;
    public static final int SALE_MANUAL = 2;
    public static final int SALE_OFFLINE = 3;
    public static final int SALE_OFFLINE_MANUAL = 4;
    public static final int SALE_PRE_AUTHORIZATION = 5;
    public static final int SALE_PRE_AUTHORIZATION_MANUAL = 6;
    public static final int SALE_INSTALLMENT = 7;
    public static final int SALE_PRE_COMPLETION = 8;
    public static final int SALE_REFUND = 9;
    public static final int SALE_REFUND_MANUAL = 10;
    public static final int CASH_BACK = 11;
    public static final int QUASI_CASH = 12;
    public static final int QUASI_CASH_MANUAL = 13;
    public static final int CASH_ADVANCE = 14;
    public static final int STUDENT_REF = 15;
    public static final int AUTH_ONLY = 16;
    public static final int QR_SALE = 17;
    public static String toTitle(int code, boolean voided) {
        if (code == SALE || code == SALE_MANUAL) {
            if (voided) {
                return Const.RECEIPT_VOID_SALE;
            } else {
                return Const.RECEIPT_SALE;
            }
        } else if (code == SALE_OFFLINE || code == SALE_OFFLINE_MANUAL) {
            if (voided) {
                return Const.RECEIPT_VOID_OFFLINE_SALE;
            } else {
                return Const.RECEIPT_OFFLINE_SALE;
            }
        } else if (code == SALE_PRE_AUTHORIZATION
                || code == SALE_PRE_AUTHORIZATION_MANUAL) {
            return Const.RECEIPT_PRE_AUTH;
        } else if (code == SALE_INSTALLMENT) {
            if (voided) {
                return Const.RECEIPT_VOID_INSTALLMENT;
            } else {
                return Const.RECEIPT_INSTALLMENT;
            }
        } else if (code == SALE_PRE_COMPLETION) {
            if (voided) {
                return Const.RECEIPT_VOID_PRE_COMP;
            } else {
                return Const.RECEIPT_COMPLETION;
            }
        } else if (code == SALE_REFUND || code == SALE_REFUND_MANUAL) {
            if (voided) {
                return Const.RECEIPT_VOID_REFUND;
            } else {
                return Const.RECEIPT_REFUND;
            }
        } else if (code == QUASI_CASH || code == QUASI_CASH_MANUAL) {
            if (voided) {
                return Const.RECEIPT_VOID_QUASI_CASH;
            } else {
                return Const.RECEIPT_QUASI_CASH;
            }
        } else if (code == CASH_ADVANCE) {
            if (voided) {
                return Const.RECEIPT_VOID_CASH_ADVANCE;
            } else {
                return Const.RECEIPT_CASH_ADVANCE;
            }
        } else if (code == STUDENT_REF) {
            if (voided) {
                return Const.RECEIPT_VOID_SALE;
            } else {
                return Const.RECEIPT_SALE;
            }
        }
        else if (code == AUTH_ONLY) {
            if (voided) {
                return Const.RECEIPT_AUTHONLY_VOID;
            } else {
                return Const.RECEIPT_AUTHONLY_SALE;
            }
        }
        else if (code == QR_SALE) {
            if (voided) {
                return Const.RECEIPT_QR_VOID;
            } else {
                return Const.RECEIPT_QR_SALE;
            }
        }
        return "";
    }

}
