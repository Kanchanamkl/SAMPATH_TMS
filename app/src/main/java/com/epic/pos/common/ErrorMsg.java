package com.epic.pos.common;

public class ErrorMsg {

    public static String getErrorMsg(String errorPref, String code) {
        switch (code) {
            case "01":
                return code+" : PLEASE CALL";
            case "02":
                return code+" : "+ "REFERRAL";
            case "03":
                return code+" : "+ "ERROR-CALL HELP SN";
            case "04":
                return code+" : "+ "PLS. PICK UP CARD";
            case "05":
                return code+" : "+ "DO NOT HONOUR";
            case "06":
                return code+" : "+ "Error";
            case "07":
                return code+" : "+ "Pickup Card, Spl Cond";
            case "08":
                return code+" : "+ "VERIFY ID AND SIGN";
            case "09":
                return code+" : "+ "ACCEPTED";
            case "10":
                return code+" : "+ "Appvd for Partial Amt";
            case "11":
                return code+" : "+ "Approved (VIP)";
            case "12":
                return code+" : "+ "ERROR-CALL HELP ";
            case "13":
                return code+" : "+ "ERROR - INVALID AMT";
            case "14":
                return code+" : "+ "ERROR - INVALID CARD";
            case "15":
                return code+" : "+ "No such Issuer";
            case "16":
                return code+" : "+ "Approved, Update Tk 3";
            case "17":
                return code+" : "+ "Customer Cancellation";
            case "18":
                return code+" : "+ "Customer Dispute";
            case "19":
                return code+" : "+ "RE-ENTER TRANSACTION";
            case "20":
                return code+" : "+ "Invalid Response";
            case "21":
                return code+" : "+ "NO TRANSACTIONS";
            case "22":
                return code+" : "+ "Suspected Malfunction";
            case "23":
                return code+" : "+ "Unaccepted Trans Fee";
            case "24":
                return code+" : "+ "File Upd not Suported";
            case "25":
                return code+" : "+ "ERROR-TERM. INACTIVE";
            case "26":
                return code+" : "+ "Dup Rec,Old Rec Rplcd";
            case "27":
                return code+" : "+ "Field Edit Error";
            case "28":
                return code+" : "+ "File Locked out";
            case "29":
                return code+" : "+ "File Update Error";
            case "30":
                return code+" : "+ "ERROR - INVLD FORMAT";
            case "31":
                return code+" : "+ "ERROR - NS";
            case "32":
                return code+" : "+ "Completed Partially";
            case "33":
                return code+" : "+ "Expired Card";
            case "34":
                return code+" : "+ "Suspected Fraud";
            case "35":
                return code+" : "+ "Contact Acquirer";
            case "36":
                return code+" : "+ "Restricted Card";
            case "37":
                return code+" : "+ "Call Acq. Security";
            case "38":
                return code+" : "+ "PIN tries Exceeded";
            case "39":
                return code+" : "+ "No Credit Account";
            case "40":
                return code+" : "+ "Func. not Supported";
            case "41":
                return code+" : "+ "PLEASE CALL - LC";
            case "42":
                return code+" : "+ "No Universal Account";
            case "43":
                return code+" : "+ "PLEASE CALL - SC";
            case "44":
                return code+" : "+ "No Investment Account";
            case "45":
                return code+" : "+ "ISO error #45";
            case "46":
                return code+" : "+ "ISO error #46";
            case "47":
                return code+" : "+ "ISO error #47";
            case "48":
                return code+" : "+ "Refer to issuer";
            case "49":
                return code+" : "+ "ISO error #49";
            case "50":
                return code+" : "+ "ISO error #50";
            case "51":
                return code+" : "+ "DECLINED";
            case "52":
                return code+" : "+ "No Checking Account";
            case "53":
                return code+" : "+ "No Savings Account";
            case "54":
                return code+" : "+ "EXPIRED CARD";
            case "55":
                return code+" : "+ "Incorrect PIN";
            case "56":
                return code+" : "+ "No Card Record";
            case "57":
                return code+" : "+ "Txn not Permtd-card";
            case "58":
                return code+" : "+ "INVALID TRANSACTION";
            case "59":
                return code+" : "+ "Suspected Fraud";
            case "60":
                return code+" : "+ "Contact Acquirer";
            case "61":
                return code+" : "+ "Exceeds Amount Limit";
            case "62":
                return code+" : "+ "Restricted Card";
            case "63":
                return code+" : "+ "Security Violation";
            case "64":
                return code+" : "+ "Org Amount Incorrect";
            case "65":
                return code+" : "+ "Freq. Limit Exceed";
            case "66":
                return code+" : "+ "Call Acq's Security";
            case "67":
                return code+" : "+ "Hard Capture";
            case "68":
                return code+" : "+ "Resp Recvd too Late";
            case "69":
                return code+" : "+ "ISO error #69 ";
            case "70":
                return code+" : "+ "ISO error #70 ";
            case "71":
                return code+" : "+ "ISO error #71 ";
            case "72":
                return code+" : "+ "ISO error #72 ";
            case "73":
                return code+" : "+ "ISO error #73 ";
            case "74":
                return code+" : "+ "ISO error #74 ";
            case "75":
                return code+" : "+ "PIN Tries Exceeded";
            case "76":
                return code+" : "+ "ERROR - DESCRIPTOR";
            case "77":
                return code+" : "+ "RECONCILE ERROR";
            case "78":
                return code+" : "+ "OLD ROC NOT FOUND";
            case "79":
                return code+" : "+ "BATCH ALREADY OPEN";
            case "80":
                return code+" : "+ "BAD BATCH NUMBER";
            case "81":
                return code+" : "+ "Private error #81";
            case "82":
                return code+" : "+ "Private error #82";
            case "83":
                return code+" : "+ "Private error #83";
            case "84":
                return code+" : "+ "Private error #84";
            case "85":
                return code+" : "+ "BATCH NOT FOUND";
            case "86":
                return code+" : "+ "Private error #86";
            case "87":
                return code+" : "+ "Private error #87";
            case "88":
                return code+" : "+ "HAVE CM CALL AMEX";
            case "89":
                return code+" : "+ "BAD TERMINAL ID";
            case "90":
                return code+" : "+ "Cutoff in Process";
            case "91":
                return code+" : "+ "ISSUER UNAVAILABLE";
            case "92":
                return code+" : "+ "Trans can't be Routed";
            case "93":
                return code+" : "+ "Txn cant be Completed";
            case "94":
                return code+" : "+ "ERROR - SEQUENCE NO";
            case "95":
                return code+" : "+ "RECONCILE ERROR";
            case "96":
                return code+" : "+ "ERROR - INVALID MSG";
            case "97":
                return code+" : "+ "INVALID TENURE";
            case "98":
                return code+" : "+ "Resvd. for Nat. use";
            case "99":
                return code+" : "+ "Resvd. for Nat. use";
            case "A1":
                return code+" : "+ "Undefined MTI or Processing code";
            case "A2":
                return code+" : "+ "Undefined LISTENER";
            case "A3":
                return code+" : "+ "Previous Transaction is in a undefined status";
            case "A4":
                return code+" : "+ "Error Response from Server";
            case "A5":
                return code+" : "+ "Txn is in Processing status";
            case "A6":
                return code+" : "+ "Txn is in reversal pending status";
            case "A7":
                return code+" : "+ "Txn is reversed";
            case "A8":
                return code+" : "+ "Reversal is failed";
            case "A9":
                return code+" : "+ "Previous Txn not found for void txn";
            case "AA":
                return code+" : "+ "Void is failed";
            case "AB":
                return code+" : "+ "Invalid Merchant Customer";
            case "AC":
                return code+" : "+ "Txn type blocked to the merchant customer";
            case "AD":
                return code+" : "+ "Currency blocked to the merchant customer";
            case "AE":
                return code+" : "+ "Invalid Merchant";
            case "AF":
                return code+" : "+ "Txn type blocked to the merchant";
            case "B1":
                return code+" : "+ "Currency blocked to the merchant";
            case "B2":
                return code+" : "+ "Merchant Risk profile not found";
            case "B3":
                return code+" : "+ "Amount is less than the minimum amount for Merchant";
            case "B4":
                return code+" : "+ "Amount is higher than the maximum amount for Merchant";
            case "B5":
                return code+" : "+ "Invalid Terminal";
            case "B6":
                return code+" : "+ "Txn type blocked to the terminal";
            case "B7":
                return code+" : "+ "Terminal Risk profile not found";
            case "B8":
                return code+" : "+ "Amount is less than the minimum amount for terminal";
            case "B9":
                return code+" : "+ "Amount is higher than the maximum amount for terminal";
            case "BA":
                return code+" : "+ "Txn is in QR generated status";
            case "BB":
                return code+" : "+ "Previous Txn not found for Wallet txn";
            case "BC":
                return code+" : "+ "Daily Txn Count/Amount exceeded for Terminal";
            case "BD":
                return code+" : "+ "BD Daily Txn Count/Amount exceeded for Merchant";
            case "BE":
                return code+" : "+ "BE Transaction Cancelled";
            case "BF":
                return code+" : "+ "BF Transaction Already Completed";
            case "C1":
                return code+" : "+ "Channel not found";
            case "C2":
                return code+" : "+ "Invalid terminal";
            case "C3":
                return code+" : "+ "Inactive terminal";
            case "C4":
                return code+" : "+ "Routing not allowed";
            case "C5":
                return code+" : "+ "Requires encpt level should be fully";
            case "C6":
                return code+" : "+ "Requires encpt level should be Mac only";
            case "C7":
                return code+" : "+ "Requires encryption level should be clear";
            case "C8":
                return code+" : "+ "Incorrect key check value";
            case "C9":
                return code+" : "+ "Incorrect officer PIN";
            case "D1":
                return code+" : "+ "Incorrect key id";
            case "D2":
                return code+" : "+ "Incorrect tle header length";
            case "D3":
                return code+" : "+ "Not found tle header details from request";
            case "D4":
                return code+" : "+ "Routing disabled by tle administrator";
            case "D5":
                return code+" : "+ "Requesting algorithm notenabled";
            case "D6":
                return code+" : "+ "Invalid encryption level";
            case "D7":
                return code+" : "+ "Request mac alg no enble";
            case "D8":
                return code+" : "+ "Mapping NII not found";
            case "D9":
                return code+" : "+ "NII group not found";
            case "E1":
                return code+" : "+ "MAC failed";
            case "E2":
                return code+" : "+ "Channel error";
            case "E3":
                return code+" : "+ "Requesting NII not registered";
            case "E4":
                return code+" : "+ "Invalid data in request";
            case "E5":
                return code+" : "+ "Requesting key size not enabled";
            case "E6":
                return code+" : "+ "Invalid card holder";
            case "E7":
                return code+" : "+ "Inactive card holder";
            case "E8":
                return code+" : "+ "PIN tries exceed";
            case "E9":
                return code+" : "+ "Key downloading Limit exceed";
            case "F1":
                return code+" : "+ "Need on-line PIN  verification";
            case "F2":
                return code+" : "+ "Need off-line PIN verification";
            case "F3":
                return code+" : "+ "TLE Server Busy";
            case "F4":
                return code+" : "+ "Smart Card Blocked";
            case "F5":
                return code+" : "+ "Channel Inactive";
            case "F6":
                return code+" : "+ "MAC value not found in the request";
            case "F7":
                return code+" : "+ "Requesting key type not enabled";
            case "F8":
                return code+" : "+ "Invalid BDK";
            case "F9":
                return code+" : "+ "Invalid KSN";
            case "H0":
                return code+" : "+ "DB pool busy";
            case "H1":
                return code+" : "+ "Invalid header length";
            case "H2":
                return code+" : "+ "Invalid txntype";
            case "H3":
                return code+" : "+ "Invalid data";
            case "H4":
                return code+" : "+ "Timeout";
            case "H5":
                return code+" : "+ "Database error found";
            case "H6":
                return code+" : "+ "Original txnnot found";
            case "H7":
                return code+" : "+ "TSP error found";
            case "H8":
                return code+" : "+ "Undefined MTI";
            case "H9":
                return code+" : "+ "Undefined processing code";
            case "I0":
                return code+" : "+ "Authorization failed";
            case "I1":
                return code+" : "+ "Detokenization failed";
            case "I2":
                return code+" : "+ "Mobile number not found";
            case "I3":
                return code+" : "+ "Per txn amt less minimum amt";
            case "I4":
                return code+" : "+ "Per txn amt greater minimum amt";
            case "I5":
                return code+" : "+ "Per day total txn count exceed";
            case "I6":
                return code+" : "+ "Per day total txn amount exceed";
            case "I7":
                return code+" : "+ "POS Per txn amount less minimum amt";
            case "I8":
                return code+" : "+ "POS Per txn amt greater maximum amt";
            case "I9":
                return code+" : "+ "Wallet Per day total txn count exceed";
            case "J0":
                return code+" : "+ "Wallet Per day total txn amount exceed";
            case "J1":
                return code+" : "+ "P2P Per txn amt less than minimum amount";
            case "J2":
                return code+" : "+ "P2P Per txn amt greater than maximum amount";
            case "J3":
                return code+" : "+ "P2P Per day total txn count exceed";
            case "J4":
                return code+" : "+ "P2P Per day total txn amt exceed";
            case "G1":
                return code+" : "+ "Host timeout";
            case "G2":
                return code+" : "+ "HSM Error found";
            default:
                return code+" : "+ errorPref + " - Response error occurred";
        }


    }

}
