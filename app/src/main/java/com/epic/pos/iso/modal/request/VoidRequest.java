package com.epic.pos.iso.modal.request;


import com.epic.pos.data.db.dbtxn.modal.Transaction;

import static com.epic.pos.iso.modal.Transaction.NII;
import static com.epic.pos.iso.modal.Transaction.POS_TXN;
import static com.epic.pos.iso.modal.Transaction.SALE_MTI;
import static com.epic.pos.iso.modal.Transaction.TO_NII;
import static com.epic.pos.iso.modal.Transaction.VOID_PROCESSING_CODE;

/**
 * SaleTransaction is the data holder bean for sale transaction iso message.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public class VoidRequest {

    private String tpdu;
    private String mti = SALE_MTI;
    private String processingCode;
    private String amount;
    private String cashBackAmount;
    private String txnDate;
    private String txnTime;
    private String traceNumber;
    private String posEntryMode;
    private String nii;
    private String secureNii;
    private String posConditionCode = "00";
    private String pan;
    private String emvData;
    private String mid;
    private String tid;
    private String invoiceNumber;
    private String expDate;
    private String approvalCode; //38
    private String rrn; //37
    private String studentRefNo; //63

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

    public String getPosConditionCode() {
        return posConditionCode;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getEmvData() {
        return emvData;
    }

    public void setEmvData(String emvData) {
        this.emvData = emvData;
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

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
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

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getSecureNii() {
        return secureNii;
    }

    public void setSecureNii(String secureNii) {
        this.secureNii = secureNii;
    }

    public String getCashBackAmount() {
        return cashBackAmount;
    }

    public void setCashBackAmount(String cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getStudentRefNo() {
        return studentRefNo;
    }

    public void setStudentRefNo(String studentRefNo) {
        this.studentRefNo = studentRefNo;
    }
}
