package com.epic.pos.device.serial;

import android.text.TextUtils;

import com.epic.pos.util.ValidatorUtil;

public class EcrReq {

    String invoiceNo = "000000";        //len 6
    String batchNo = "000000";          //len 6
    String terminalId = "        ";     //len 8
    String mid = "               ";     //len 15
    String hostName = "      ";         //len 6
    String approveCode = "000000";      //len 6
    String responseCode = "00";         //len 2 (sale response)
    String refNo = "000000000000";      //len 12
    String cardNoFirst6 = "000000";     //len 6
    String cardNoLast4 = "0000";        //len 4
    String cardType = "            ";   //len 12
    String amount = "000000000000";     //len 12
    String name = "                              "; //len 30

    public String toEcrReq() {
        return invoiceNo
                + batchNo
                + terminalId
                + mid
                + hostName
                + approveCode
                + responseCode
                + refNo
                + cardNoFirst6
                + cardNoLast4
                + cardType
                + amount
                + name;
    }

    public void setInvoiceNo(String invoiceNo) {
        if (!TextUtils.isEmpty(invoiceNo) && invoiceNo.length() == 6) {
            this.invoiceNo = invoiceNo;
        }
    }

    public void setBatchNo(String batchNo) {
        if (!TextUtils.isEmpty(batchNo) && batchNo.length() == 6) {
            this.batchNo = batchNo;
        }
    }

    public void setTerminalId(String terminalId) {
        if (!TextUtils.isEmpty(terminalId)) {
            if (terminalId.length() == 8) {
                this.terminalId = terminalId;
            } else if (terminalId.length() <= 7) {
                this.terminalId = ValidatorUtil.getInstance().anyCharPadString(terminalId, 8, " ", true);
            }
        }
    }

    public void setMid(String mid) {
        if (!TextUtils.isEmpty(mid)) {
            if (mid.length() == 15) {
                this.mid = mid;
            } else if (mid.length() <= 14) {
                this.mid = ValidatorUtil.getInstance().anyCharPadString(mid, 15, " ", true);
            }
        }
    }

    public void setHostName(String hostName) {
        if (!TextUtils.isEmpty(hostName)) {
            if (hostName.length() == 6) {
                this.hostName = hostName;
            } else if (hostName.length() <= 5) {
                this.hostName = ValidatorUtil.getInstance().anyCharPadString(hostName, 6, " ", true);
            } else {
                this.hostName = hostName.substring(0, 6);
            }
        }
    }

    public void setApproveCode(String approveCode) {
        if (!TextUtils.isEmpty(approveCode) && approveCode.length() == 6) {
            this.approveCode = approveCode;
        }
    }

    public void setResponseCode(String responseCode) {
        if (!TextUtils.isEmpty(responseCode) && responseCode.length() == 2) {
            this.responseCode = responseCode;
        }
    }

    public void setRefNo(String refNo) {
        if (!TextUtils.isEmpty(refNo)) {
            if (refNo.length() == 12) {
                this.refNo = refNo;
            } else if (refNo.length() <= 11) {
                this.refNo = ValidatorUtil.getInstance().anyCharPadString(refNo, 12, " ", true);
            }
        }
    }

    public void setCardNoFirst6(String cardNoFirst6) {
        if (!TextUtils.isEmpty(cardNoFirst6) && cardNoFirst6.length() == 6) {
            this.cardNoFirst6 = cardNoFirst6;
        }
    }

    public void setCardNoLast4(String cardNoLast4) {
        if (!TextUtils.isEmpty(cardNoLast4) && cardNoLast4.length() == 4) {
            this.cardNoLast4 = cardNoLast4;
        }
    }

    public void setCardType(String cardType) {
        if (!TextUtils.isEmpty(cardType)) {
            cardType = cardType.trim();
            if (cardType.length() == 12) {
                this.cardType = cardType;
            } else if (cardType.length() <= 11) {
                this.cardType = ValidatorUtil.getInstance().anyCharPadString(cardType, 12, " ", true);
            } else {
                this.cardType = cardType.substring(0, 12);
            }
        }
    }

    public void setAmount(String amount) {
        if (!TextUtils.isEmpty(amount)) {
            String aReplaced = amount.replaceAll(",", "").replaceAll("\\.", "");
            this.amount = ValidatorUtil.getInstance().anyCharPadString(aReplaced, 12, " ", false);
        }
    }

    public void setName(String name) {
        if (!TextUtils.isEmpty(name)) {
            name = name.trim();
            if (name.length() == 30) {
                this.name = name;
            } else if (name.length() <= 29) {
                this.name = ValidatorUtil.getInstance().anyCharPadString(name, 30, " ", true);
            } else {
                this.name = name.substring(0, 30);
            }
        }
    }
}
