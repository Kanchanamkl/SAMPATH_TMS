package com.epic.pos.util;


public class ErrorCodeManager {

    private static ErrorCodeManager errorCodeManager;

    private ErrorCodeManager() { }

    /**
     * get common info class instance
     * @return - CommonIfo instance
     */
    public static synchronized ErrorCodeManager getInstance(){
        if (errorCodeManager == null) {
            errorCodeManager = new ErrorCodeManager();
        }
        return errorCodeManager;
    }


    public static enum ErrorCodes {

        C1, C2, C3, C4, C5, C6, C7, C8,C9,
        D1, D2, D3, D4, D5, D6, D7, D8,D9,
        E1, E2, E3, E4, E5, E6, E7, E8,E9,
        F1, F2, F3, F4, F5, F6, F7, F8,F9,
        H0,H1, H2, H3, H4, H5, H6, H7, H8,H9,
        I0,I1, I2, I3, I4, I5, I6, I7, I8,I9,
        J0,J1, J2, J3, J4,
        G1,G2

    }

    public String returnErrorCodes(String code) {
        String retVal = "Unknown error occurred";

        ErrorCodes Codes = ErrorCodes.valueOf(code);
        switch (Codes) {
            case C1: retVal = "Channel not found"; break;
            case C2: retVal = "Invalid terminal"; break;
            case C3: retVal = "Inactive terminal"; break;
            case C4: retVal = "Routing not allowed"; break;
            case C5: retVal = "Requires encpt level should be fully"; break;
            case C6: retVal = "Requires encpt level should be Mac only"; break;
            case C7: retVal = "Requires encryption level should be clear"; break;
            case C8: retVal = "Incorrect key check value"; break;
            case C9: retVal = "Incorrect officer PIN"; break;

            case D1: retVal = "Incorrect key id"  ; break;
            case D2: retVal = "Incorrect tle header length"; break;
            case D3: retVal = "Not found tle header details from request"; break;
            case D4: retVal = "Routing disabled by tle administrator"; break;
            case D5: retVal = "Requesting algorithm not enabled"; break;
            case D6: retVal = "Invalid encryption level"; break;
            case D7: retVal = "Request mac alg no enble"; break;
            case D8: retVal = "Mapping NII not found"; break;
            case D9: retVal = "NII group not found"; break;

            case E1: retVal = "MAC failed"  ; break;
            case E2: retVal = "Channel error"; break;
            case E3: retVal = "Requesting NII not registered"; break;
            case E4: retVal = "Invalid data in request"; break;
            case E5: retVal = "Requesting key size not enabled"; break;
            case E6: retVal = "Invalid card holder"; break;
            case E7: retVal = "Inactive card holder"; break;
            case E8: retVal = "PIN tries exceed"; break;
            case E9: retVal = "Key downloading Limit exceed"; break;

            case F1: retVal = "Need on-line PIN  verification"  ; break;
            case F2: retVal = "Requesting NII not registered"; break;
            case F3: retVal = "TLE Server Busy"; break;
            case F4: retVal = "Smart Card Blocked"; break;
            case F5: retVal = "Channel Inactive"; break;
            case F6: retVal = "MAC value not found in the request"; break;
            case F7: retVal = "Requesting key type not enabled"; break;
            case F8: retVal = "Invalid BDK"; break;
            case F9: retVal = "Invalid KSN"; break;

            case H0: retVal = "DB pool busy"  ; break;
            case H1: retVal = "Invalid header length"; break;
            case H2: retVal = "Invalid transaction type"; break;
            case H3: retVal = "Invalid data"; break;
            case H4: retVal = "Timeout"; break;
            case H5: retVal = "Database error found"; break;
            case H6: retVal = "Original transaction not found"; break;
            case H7: retVal = "TSP error found"; break;
            case H8: retVal = "Undefined MTI"; break;
            case H9: retVal = "Undefined processing code"; break;

            case I0: retVal = "Authorization failed"  ; break;
            case I1: retVal = "Detokenization failed "; break;
            case I2: retVal = "Mobile number not found "; break;
            case I3: retVal = "Per txn amt less minimum amount"; break;
            case I4: retVal = "Per txn amount greater minimum amount"; break;
            case I5: retVal = "Per day total txn count exceed"; break;
            case I6: retVal = "Per day total txn amount exceed"; break;
            case I7: retVal = "POS Per txn amount less minimum amount"; break;
            case I8: retVal = "POS Per txn amt greater maximum amount "; break;
            case I9: retVal = "Wallet Per day total txn count exceed"; break;

            case J0: retVal = "Wallet Per day total txn amount exceed"; break;
            case J1: retVal = "P2P Per txn amt less than minimum amount"; break;
            case J2: retVal = "P2P Per txn amt greater than maximum amount"; break;
            case J3: retVal = "P2P Per day total txn count exceed"; break;
            case J4: retVal = "P2P Per day total txn amt exceed"; break;

            case G1: retVal = "Host timeout"; break;
            case G2: retVal = "HSM Error found "; break;

            default:
                retVal = "Unknown error" ;break;
        }
        return retVal;
    }


    public String getCodeMessage(String errorCode){
        String errorMsg = errorCode.trim();

        try {
            errorMsg =  returnErrorCodes(errorCode);

        } catch (NumberFormatException e) {
            try {
                errorMsg =  returnErrorCodes(errorCode);
            } catch (Exception e1) {
                e1.printStackTrace();
                errorMsg =  "Error code " + errorCode;
            }
        }
        return errorMsg;
    }
}