package com.epic.pos.receipt.modal;

import android.text.TextUtils;

/**
 * The VoidReceipt class is the data holder of void sale receipt.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public class VoidReceipt {

    private String bankLogo = "img/boc_680.gif";
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    private String dateTime;
    private String merchantId;
    private String terminalId;
    private String batchNo;
    private String invoiceNo;

    private String cardNo;
    private String expireDate;
    private String cardType;
    private String apprCode;
    private String refNo;
    private String currency;
    private String amount;
    private String merchantNo;
    private String studentRef;

    private boolean isCustomerCopy;
    private boolean isMerchantCopy;
    private boolean shouldSave;
    private boolean isOfflineSale;
    private boolean isPreCompTxn;
    private boolean isInstallmentTxn;
    private boolean isRefundSale;
    private boolean isQuasiCash;

    public void toUpperCase() {
        if (!TextUtils.isEmpty(addressLine1))
            addressLine1 = addressLine1.toUpperCase();

        if (!TextUtils.isEmpty(addressLine2))
            addressLine2 = addressLine2.toUpperCase();

        if (!TextUtils.isEmpty(addressLine3))
            addressLine3 = addressLine3.toUpperCase();

        if (!TextUtils.isEmpty(dateTime))
            dateTime = dateTime.toUpperCase();

        if (!TextUtils.isEmpty(merchantId))
            merchantId = merchantId.toUpperCase();

        if (!TextUtils.isEmpty(terminalId))
            terminalId = terminalId.toUpperCase();

        if (!TextUtils.isEmpty(batchNo))
            batchNo = batchNo.toUpperCase();

        if (!TextUtils.isEmpty(invoiceNo))
            invoiceNo = invoiceNo.toUpperCase();

        if (!TextUtils.isEmpty(cardNo))
            cardNo = cardNo.toUpperCase();

        if (!TextUtils.isEmpty(expireDate))
            expireDate = expireDate.toUpperCase();

        if (!TextUtils.isEmpty(cardType))
            cardType = cardType.toUpperCase();

        if (!TextUtils.isEmpty(apprCode))
            apprCode = apprCode.toUpperCase();

        if (!TextUtils.isEmpty(refNo))
            refNo = refNo.toUpperCase();

        if (!TextUtils.isEmpty(currency))
            currency = currency.toUpperCase();

        if (!TextUtils.isEmpty(amount))
            amount = amount.toUpperCase();

        if (!TextUtils.isEmpty(studentRef))
            studentRef = studentRef.toUpperCase();
    }

    public String getBankLogo() {
        return bankLogo;
    }

    public void setBankLogo(String bankLogo) {
        this.bankLogo = bankLogo;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getApprCode() {
        return apprCode;
    }

    public void setApprCode(String apprCode) {
        this.apprCode = apprCode;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isCustomerCopy() {
        return isCustomerCopy;
    }

    public void setCustomerCopy(boolean customerCopy) {
        isCustomerCopy = customerCopy;
    }

    public boolean isMerchantCopy() {
        return isMerchantCopy;
    }

    public void setMerchantCopy(boolean merchantCopy) {
        isMerchantCopy = merchantCopy;
    }

    public boolean isShouldSave() {
        return shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public boolean isOfflineSale() {
        return isOfflineSale;
    }

    public void setOfflineSale(boolean offlineSale) {
        isOfflineSale = offlineSale;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public boolean isPreCompTxn() {
        return isPreCompTxn;
    }

    public void setPreCompTxn(boolean preCompTxn) {
        isPreCompTxn = preCompTxn;
    }

    public boolean isInstallmentTxn() {
        return isInstallmentTxn;
    }

    public void setInstallmentTxn(boolean installmentTxn) {
        isInstallmentTxn = installmentTxn;
    }

    public boolean isRefundSale() {
        return isRefundSale;
    }

    public void setRefundSale(boolean refundSale) {
        isRefundSale = refundSale;
    }

    public boolean isQuasiCash() {
        return isQuasiCash;
    }

    public void setQuasiCash(boolean quasiCash) {
        isQuasiCash = quasiCash;
    }

    public String getStudentRef() {
        return studentRef;
    }

    public void setStudentRef(String studentRef) {
        this.studentRef = studentRef;
    }

    @Override
    public String toString() {
        return "VoidReceipt{" +
                "bankLogo='" + bankLogo + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", addressLine3='" + addressLine3 + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", expireDate='" + expireDate + '\'' +
                ", cardType='" + cardType + '\'' +
                ", apprCode='" + apprCode + '\'' +
                ", refNo='" + refNo + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                ", merchantNo='" + merchantNo + '\'' +
                ", studentRef='" + studentRef + '\'' +
                ", isCustomerCopy=" + isCustomerCopy +
                ", isMerchantCopy=" + isMerchantCopy +
                ", shouldSave=" + shouldSave +
                ", isOfflineSale=" + isOfflineSale +
                ", isPreCompTxn=" + isPreCompTxn +
                ", isInstallmentTxn=" + isInstallmentTxn +
                ", isRefundSale=" + isRefundSale +
                ", isQuasiCash=" + isQuasiCash +
                '}';
    }
}
