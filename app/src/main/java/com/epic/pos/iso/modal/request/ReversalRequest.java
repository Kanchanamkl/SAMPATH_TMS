package com.epic.pos.iso.modal.request;

import com.epic.pos.iso.modal.Transaction;

/**
 * ReversalRequest is the data holder bean for reversal transaction iso message.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public class ReversalRequest extends Transaction {

    private String tpdu;
    private String mti = REVERSAL_MTI;
    private String processingCode;
    private String baseAmount;
    private String cashBackAmount;
    private String totalAmount;
    private String traceNumber;
    private String posEntryMode;
    private String nii;
    private String secureNii;
    private String posConditionCode = "00";
    private String track2Data;
    private String emvData;
    private String mid;
    private String tid;
    private String invoiceNumber;
    private String pan; //only available if isManualSale true
    private String expDate;
    private String txnDate;
    private String txnTime;
    private String rrn;
    private int cardAction;
    private boolean isManualSale;
    private boolean isVoidSale;
    private String panSequenceNumber;
    private String studentRefNo;

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public int getCardAction() {
        return cardAction;
    }

    public void setCardAction(int cardAction) {
        this.cardAction = cardAction;
    }

    public String getEmvData() {
        return emvData;
    }

    public void setEmvData(String emvData) {
        this.emvData = emvData;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public boolean isManualSale() {
        return isManualSale;
    }

    public void setManualSale(boolean manualSale) {
        isManualSale = manualSale;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public boolean isVoidSale() {
        return isVoidSale;
    }

    public void setVoidSale(boolean voidSale) {
        isVoidSale = voidSale;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getSecureNii() {
        return secureNii;
    }

    public void setSecureNii(String secureNii) {
        this.secureNii = secureNii;
    }

    public String getPanSequenceNumber() {
        return panSequenceNumber;
    }

    public void setPanSequenceNumber(String panSequenceNumber) {
        this.panSequenceNumber = panSequenceNumber;
    }

    public String getCashBackAmount() {
        return cashBackAmount;
    }

    public void setCashBackAmount(String cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(String baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getStudentRefNo() {
        return studentRefNo;
    }

    public void setStudentRefNo(String studentRefNo) {
        this.studentRefNo = studentRefNo;
    }
}
