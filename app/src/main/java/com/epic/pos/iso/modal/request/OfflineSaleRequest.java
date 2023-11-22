package com.epic.pos.iso.modal.request;


import com.epic.pos.data.db.dbtxn.modal.Transaction;

import static com.epic.pos.iso.modal.Transaction.NII;
import static com.epic.pos.iso.modal.Transaction.OFFLINE_SALE_MTI;
import static com.epic.pos.iso.modal.Transaction.POS_TXN;
import static com.epic.pos.iso.modal.Transaction.TO_NII;

/**
 * OfflineSaleRequest
 *
 * @author Sameera Jayarathna.
 * @version 1.0
 * @since 18 May, 2021
 */
public class OfflineSaleRequest extends Transaction {
    private String mti = OFFLINE_SALE_MTI;
    private String tpdu = POS_TXN + NII + TO_NII;

    private String pan;
    private String processingCode;
    private String amount;
    private String traceNumber;
    private String time;
    private String date;
    private String expDate;
    private String posEntryMode;
    private String panSeq;
    private String nii = NII;
    private String secureNii;
    private String posConditionCode;
    private String authCode;
    private String tid;
    private String mid;
    private String emv;
    private String invoice;

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public void setPosEntryMode(String posEntryMode) {
        this.posEntryMode = posEntryMode;
    }

    public void setPanSeq(String panSeq) {
        this.panSeq = panSeq;
    }

    public void setPosConditionCode(String posConditionCode) {
        this.posConditionCode = posConditionCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public void setEmv(String emv) {
        this.emv = emv;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getMti() {
        return mti;
    }

    public String getTpdu() {
        return tpdu;
    }

    public String getPan() {
        return pan;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public String getAmount() {
        return amount;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getExpDate() {
        return expDate;
    }

    public String getPosEntryMode() {
        return posEntryMode;
    }

    public String getPanSeq() {
        return panSeq;
    }

    public String getNii() {
        return nii;
    }

    public String getPosConditionCode() {
        return posConditionCode;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getTid() {
        return tid;
    }

    public String getMid() {
        return mid;
    }

    public String getEmv() {
        return emv;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getSecureNii() {
        return secureNii;
    }

    public void setSecureNii(String secureNii) {
        this.secureNii = secureNii;
    }
}
