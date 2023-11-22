package com.epic.pos.iso.modal.request;


import com.epic.pos.data.db.dbtxn.modal.Transaction;

import static com.epic.pos.iso.modal.Transaction.NII;
import static com.epic.pos.iso.modal.Transaction.POS_TXN;
import static com.epic.pos.iso.modal.Transaction.SETTLEMENT_MTI;
import static com.epic.pos.iso.modal.Transaction.TO_NII;

public class SettlementRequest extends Transaction {
    private String mti = SETTLEMENT_MTI;
    private String processingCode = "920000"; // After batch upload this will be replace with postSettlementProcessingCode(960000)
    private String postSettlementProcessingCode = "960000";
    private String traceNumber;
    private String nii = NII;
    private String secureNii;
    private String mid;
    private String tid;
    private String batchNo;
    private String txnCountAndAmount;
    private String tpdu = POS_TXN + NII + TO_NII;

    public String getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getTxnCountAndAmount() {
        return txnCountAndAmount;
    }

    public void setTxnCountAndAmount(String txnCountAndAmount) {
        this.txnCountAndAmount = txnCountAndAmount;
    }

    public String getMti() {
        return mti;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public String getNii() {
        return nii;
    }

    public String getTpdu() {
        return tpdu;
    }

    public String getPostSettlementProcessingCode() {
        return postSettlementProcessingCode;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public void setSecureNii(String secureNii) {
        this.secureNii = secureNii;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getSecureNii() {
        return secureNii;
    }
}
