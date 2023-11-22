package com.epic.pos.device.data;

/**
 * CardType
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-11
 */
public enum CardType {

    VISA("VISA"),
    MASTER("MASTER"),
    AMEX("AMEX"),
    UNIONPAY("UNIONPAY"),
    JCB("JCB"),
    DINERS("DINERS"),
    AGODA("AGODA");

    private String val;

    CardType(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }

    //    public static CardType convert(String val) {
//        for (CardType c : values()) {
//            if (c.val.equals(val)) {
//                return c;
//            }
//        }
//        return null;
//    }
}
