package com.epic.pos.iso.modal.request;


import com.epic.pos.data.db.dbtxn.modal.Transaction;

import static com.epic.pos.iso.modal.Transaction.POS_ENTRY_MODE_MAG_PIN_NOT_SUPPORT;
import static com.epic.pos.iso.modal.Transaction.POS_TXN;
import static com.epic.pos.iso.modal.Transaction.QR_MTI;
import static com.epic.pos.iso.modal.Transaction.QR_NII;
import static com.epic.pos.iso.modal.Transaction.QR_TO_NII;

public class QrRequest extends Transaction {

    private String tpdu = POS_TXN + QR_NII + QR_TO_NII;
    private String mti = QR_MTI;
    private String processingCode = "030000";                   //3
    private String amount;                                      //4
    private String traceNumber;                                 //11
    private String posEntryMode = POS_ENTRY_MODE_MAG_PIN_NOT_SUPPORT; //22
    private String nii = QR_NII;                                //24
    private String posConditionCode = "00";                     //25
    private String tid;                                         //41
    private String mid;                                         //42
    private String currencyCode;                                //49


    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getPosEntryMode() {
        return posEntryMode;
    }

    public void setPosEntryMode(String posEntryMode) {
        this.posEntryMode = posEntryMode;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getPosConditionCode() {
        return posConditionCode;
    }

    public void setPosConditionCode(String posConditionCode) {
        this.posConditionCode = posConditionCode;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
