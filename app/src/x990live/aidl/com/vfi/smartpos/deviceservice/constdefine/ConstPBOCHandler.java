package com.vfi.smartpos.deviceservice.constdefine;

/**
 * Created by Simon on 2018/8/21.
 */

public class ConstPBOCHandler {
    public class onTransactionResult{
        public class result {
            public static final int AARESULT_TC = 0;    // TC on action analysis
            public static final int AARESULT_AAC = 1;   // refuse on action analysis
            public static final int EMV_NO_APP = 8;   // emv no application(aid param)
            public static final int EMV_COMPLETE = 9;   // emv complete
            public static final int EMV_OTHER_ERROR = 11;       // emv other error,transaction abort</li>
            public static final int EMV_FALLBACK = 12 ;         // FALLBACK </li>
            public static final int EMV_DATA_AUTH_FAIL = 13;    // - emv data auth failed
            public static final int EMV_APP_BLOCKED = 14;       // - emv app blocked
            public static final int EMV_NOT_ECCARD = 15;        // not an ecc card
            public static final int EMV_UNSUPPORT_ECCARD = 16;  // emv un supported card
            public static final int EMV_AMOUNT_EXCEED_ON_PURELYEC = 17;     //emv amount exceeded
            public static final int EMV_SET_PARAM_ERROR = 18;               //parameter setting error
            public static final int EMV_PAN_NOT_MATCH_TRACK2 = 19;          //pan not matching with the track
            public static final int EMV_CARD_HOLDER_VALIDATE_ERROR = 20;    //card holder validation error
            public static final int EMV_PURELYEC_REJECT = 21 ;              //pure lyce reject
            public static final int EMV_BALANCE_INSUFFICIENT = 22;          //emv balance insufficient
            public static final int EMV_AMOUNT_EXCEED_ON_RFLIMIT_CHECK = 23; //amount exceeded on relimit check
            public static final int EMV_CARD_BIN_CHECK_FAIL = 24; // read cardData fail
            public static final int EMV_CARD_BLOCKED = 25;                  //emv card blocked
            public static final int EMV_MULTI_CARD_ERROR = 26;  // multi cards
            public static final int EMV_BALANCE_EXCEED = 27;  // emv balance exceed
            public static final int EMV_GACERR_GACCMD = 28;  // emv balance exceed
            public static final int EMV_TIMEOUT_TRY_AGAIN = 29;  // emv balance exceed
            public static final int EMV_RFCARD_PASS_FAIL = 60;  // tap card failure
            public static final int EMV_IN_QPBOC_PROCESS = 99;  // qPBOC is processing
            public static final int EMV_SEE_PHONE = 150;                       //paypass result, please check the result on phone
            public static final int QPBOC_AAC = 202;            //refuse on qPBOC
            public static final int QPBOC_ERROR = 203;                       //error on qPBOC
            public static final int QPBOC_TC = 204;                       //TC on qPBOC
            public static final int QPBOC_CONT = 205;                       //need contact
            public static final int QPBOC_NO_APP = 206;                       //result of qPBOC, no application (UP Card maybe available)
            public static final int QPBOC_NOT_CPU_CARD = 207;                       //not a cpu card
            public static final int QPBOC_ABORT = 208;                       //Transation abort
            public static final int PAYPASS_COMPLETE = 301;                       //paypass complete
            public static final int PAYPASS_EMV_TC = 304;                       //paypass交易结果
            public static final int PAYPASS_EMV_AAC = 305;                       //ppaypass result, refuse
            public static final int PAYPASS_EMV_ERROR = 306;                       //paypass交易结果，交易失败
            public static final int PAYPASS_END_APP = 307;                       //paypass交易结果，交易终止
            public static final int PAYPASS_TRYOTHER = 308;                       //paypass completepaypass result, try other (contact, magnetic card)
        }
        public class data {
            public static final String KEY_TC_DATA_String       = "TC_DATA";
            public static final String KEY_REVERSAL_DATA_String = "REVERSAL_DATA"; // (String) - the string of reversal data</li>
            public static final String KEY_ERROR_String         = "ERROR"; // * <li>(String) - the error description ( from the result of PBOC) </li>
        }
    }

    public class onConfirmCardInfo {
        public class info {
            public static final String KEY_PAN_String = "PAN"; // (String) the PAN </li>
            public static final String KEY_TRACK2_String = "TRACK2"; // (String) track 2</li>
            public static final String KEY_CARD_SN_String = "CARD_SN"; // (String) card serial number</li>
            public static final String KEY_SERVICE_CODE_String = "SERVICE_CODE"; // (String) service code</li>
            public static final String KEY_EXPIRED_DATE_String = "EXPIRED_DATE"; //(String) expired date</li>
            public static final String KEY_CARD_TYPE_String = "CARD_TYPE";
        }
    }

    public class onRequestOnlineProcess {
        public class aaResult {
            public static final String KEY_CTLS_CVMR_int = "CTLS_CVMR";
            public static final int VALUE_CTLS_CVMR_NO_CVM = 0;   //) - QPBOC_ARQC, online request, part of PBOC standard<br>
            public static final int VALUE_CTLS_CVMR_CVM_PIN = 1;  //, the action analysis result<br>
            public static final int VALUE_CTLS_CVMR_CVM_SIGN = 2; // -the mode of magnetic card on paypass request<br>
            public static final int VALUE_CTLS_CVMR_CVM_CDCVM = 3; //- the mode of EMV on paypass request<br>

            public static final String KEY_RESULT_int = "RESULT";
            public static final int VALUE_RESULT_QPBOC_ARQC = 201;   //) - QPBOC_ARQC, online request, part of PBOC standard<br>
            public static final int VALUE_RESULT_AARESULT_ARQC = 2;  //, the action analysis result<br>
            public static final int VALUE_RESULT_PAYPASS_MAG_ARQC = 302; // -the mode of magnetic card on paypass request<br>
            public static final int VALUE_RESULT_PAYPASS_EMV_ARQC = 303; //- the mode of EMV on paypass request<br>

            public static final String KEY_ARQC_DATA_String = "ARQC_DATA";

            public static final String KEY_REVERSAL_DATA_String = "REVERSAL_DATA";

        }
    }
}
