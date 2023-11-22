package com.epic.pos.iso.modal.request;

import com.epic.pos.data.db.dbtxn.modal.Transaction;

import static com.epic.pos.iso.modal.Transaction.BATCH_UPLOAD_MTI;
import static com.epic.pos.iso.modal.Transaction.NII;
import static com.epic.pos.iso.modal.Transaction.POS_TXN;
import static com.epic.pos.iso.modal.Transaction.TO_NII;
/**
 * BatchUploadRequest is the data holder bean for sale transaction iso message.
 *
 * @author Sameera Jayarathna.
 * @version 1.0
 * @since 2021-05-06
 */
public class BatchUploadRequest extends Transaction {
    private String mti = BATCH_UPLOAD_MTI;
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
    private String rrn;
    private String authCode;
    private String response;
    private String tid;
    private String mid;
    private String emv;
    private String originalTxnData;
    private String invoice;

    public String getMti() {
        return mti;
    }

    public String getTpdu() {
        return tpdu;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getPosEntryMode() {
        return posEntryMode;
    }

    public void setPosEntryMode(String posEntryMode) {
        this.posEntryMode = posEntryMode;
    }

    public String getPanSeq() {
        return panSeq;
    }

    public void setPanSeq(String panSeq) {
        this.panSeq = panSeq;
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

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
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

    public String getEmv() {
        return emv;
    }

    public void setEmv(String emv) {
        this.emv = emv;
    }

    public String getOriginalTxnData() {
        return originalTxnData;
    }

    public void setOriginalTxnData(String originalTxnData) {
        this.originalTxnData = originalTxnData;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getSecureNii() {
        return secureNii;
    }

    public void setSecureNii(String secureNii) {
        this.secureNii = secureNii;
    }
}
