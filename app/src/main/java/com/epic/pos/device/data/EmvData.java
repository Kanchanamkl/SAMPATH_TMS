package com.epic.pos.device.data;

public class EmvData {

    public static int VISA_TAG_LIST[] = {
            0x82, 0x84, 0x95, 0x9A, 0x9B, 0x9C, 0x5F2A, 0x9F03, 0x9F09, 0x9F1E, 0x9F34, 0x9F35, 0x9F41, 0x9F53, 0x5F25, 0x5F34,
            0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x9F02, 0x9F1A, 0x9F33, 0x9F6E
    };

    public static int MASTER_TAG_LIST[] = {
            0x82, 0x84, 0x87, 0x95, 0x9A, 0x9C, 0x5F2A, 0x9F03, 0x9F09, 0x9F1E, 0x9F34, 0x9F35, 0x9F41, 0x5F34,
            0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x9F02, 0x9F1A, 0x9F33, 0x9F1D
//            0x9F53  0x9F6E
    };

    public static int AMEX_TAG_LIST[] = {
//            0x9F53,0x9F6E
            0x82,0x84,0x95,0x9A,0x9C,0x5F2A,0x5F34,0x9F02,0x9F03,0x9F09,0x9F10,0x9F1A,0x9F1E,0x9F26,0x9F27
            ,0x9F33,0x9F34,0x9F35,0x9F36,0x9F37,0x9F41,0x5F25
    };

    public static int CUP_TAG_LIST[] = {
            0x82, 0x95, 0x9A, 0x9C, 0x5F2A, 0x9F03, 0x9F10, 0x9F37, 0x9F36, 0x9F02, 0x9F26, 0x9F27,
            0x9F33, 0x9F1A, 0x84, 0x9F09, 0x9F1E, 0x9F34, 0x9F41, 0x5F34, 0x50
    };


    public static String VISA_TAG_LIST_NEXGO[] = {
//            "82", "84", "95", "9a", "9b", "9c", "5f2a", "9f03", "9f09", "9f1e",
//            "9f34", "9f35", "9f41", "9f53", "5f25", "5f34", "9f26", "9f27",
//            "9f10", "9f37", "9f36", "9f02", "9f1A", "9f33", "9f6e"
            "82", "84", "95", "9a", "9c", "5f2a", "9F02", "9F03",
            "9F09", "9F10", "9F1A", "9F1E", "9F26", "9F27", "9F33", "9F34",
            "9F35", "9F36", "9F37", "9F41", "9F53","5F34"
    };

    public static String MASTER_TAG_LIST_STRING[] = {
//            "82", "84", "95", "9a", "9c", "5f2a", "9f03", "9f09", "9f1e",
//            "9f34", "9f35", "9f41", "5f34", "9f26", "9f27", "9f10",
//            "9f37", "9f36", "9f02", "9f1A", "9f33",  "9f1d"
//            //, "87"
            "82", "84", "95", "9a", "9c", "5f2a", "9F02", "9F03",
            "9F09", "9F10", "9F1A", "9F1E", "9F26", "9F27", "9F33", "9F34",
            "9F35", "9F36", "9F37", "9F41","5F34","9F53"
    };

    public static String AMEX_TAG_LIST_STRING[] = {
            "82", "84", "95", "9a", "9c", "5f2a", "5F34", "9F02", "9F03",
            "9F09", "9F10", "9F1A", "9F1E", "9F26", "9F27", "9F33", "9F34",
            "9F35", "9F36", "9F37", "9F41", "5F25"
    };

    public static String CUP_TAG_LIST_STRING[] = {
            "82", "95", "9a", "9c", "5f2a", "9f03", "9f10", "9f37", "9f36",
            "9f02", "9f26", "9f27", "9f33", "9f1a", "84", "9f09", "9f1e", "9f34",
            "9f41", "5f34"
    };
    public static String JCB_TAG_LIST_STRING[] = {
            "82", "84", "95", "9a", "9b", "9c", "5f2a", "9F02", "9F03",
            "9F09", "9F10", "9F1A", "9F1E", "9F26", "9F27", "9F33", "9F34",
            "9F35", "9F36", "9F37", "9F41", "5F34"
    };
}
