package com.epic.pos.iso.modal.request;

import com.epic.pos.iso.modal.Transaction;

/**
 * SaleTransaction is the data holder bean for sale transaction iso message.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public class SaleRequest extends Transaction {

    private String tpdu;
    private String mti = SALE_MTI;
    private String processingCode = SALE_PROCESSING_CODE;
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
    private String pinBlock;
    private String invoiceNumber;
    private String pan;
    private int cardAction;
    private String txnDate;
    private String txnTime;
    private String expDate;
    private int issuerNo;
    private int hostId;
    private String panSequenceNumber;
    private boolean onlinePinRequested;
    private String studentRefNo;
    private String currencycode;

    public String getTpdu() {
        return tpdu;
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

    public String getPinBlock() {
        return pinBlock;
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
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

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
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

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public int getIssuerNo() {
        return issuerNo;
    }

    public void setIssuerNo(int issuerNo) {
        this.issuerNo = issuerNo;
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

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
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

    public boolean isOnlinePinRequested() {
        return onlinePinRequested;
    }

    public void setOnlinePinRequested(boolean onlinePinRequested) {
        this.onlinePinRequested = onlinePinRequested;
    }

    public String getStudentRefNo() {
        return studentRefNo;
    }

    public void setStudentRefNo(String studentRefNo) {
        this.studentRefNo = studentRefNo;
    }

    public String getCurrencycode() {
        return currencycode;
    }

    public void setCurrencycode(String currencycode) {
        this.currencycode = currencycode;
    }

    @Override
    public String toString() {
        return "SaleRequest{" +
                "tpdu='" + tpdu + '\'' +
                ", mti='" + mti + '\'' +
                ", processingCode='" + processingCode + '\'' +
                ", baseAmount='" + baseAmount + '\'' +
                ", cashBackAmount='" + cashBackAmount + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", traceNumber='" + traceNumber + '\'' +
                ", posEntryMode='" + posEntryMode + '\'' +
                ", nii='" + nii + '\'' +
                ", secureNii='" + secureNii + '\'' +
                ", posConditionCode='" + posConditionCode + '\'' +
                ", track2Data='" + track2Data + '\'' +
                ", emvData='" + emvData + '\'' +
                ", mid='" + mid + '\'' +
                ", tid='" + tid + '\'' +
                ", pinBlock='" + pinBlock + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", pan='" + pan + '\'' +
                ", cardAction=" + cardAction +
                ", txnDate='" + txnDate + '\'' +
                ", txnTime='" + txnTime + '\'' +
                ", expDate='" + expDate + '\'' +
                ", issuerNo=" + issuerNo +
                ", hostId=" + hostId +
                ", panSequenceNumber='" + panSequenceNumber + '\'' +
                ", onlinePinRequested=" + onlinePinRequested +
                ", studentRefNo='" + studentRefNo + '\'' +
                '}';
    }
}