package com.epic.pos.receipt.modal;

import android.text.TextUtils;

/**
 * The SaleReceipt class is the data holder of sale receipt.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-23
 */
public class SaleReceipt {

    public enum SignatureType {
        SIGNATURE, SIGNATURE_NOT_REQUIRED, PIN_VERIFIED, NO_CVM_REQUIRED
    }

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
    private String aid;
    private String apprCode;
    private String refNo;
    private String currency;
    private String totalAmount;
    private String baseAmount;
    private String cashBackAmount;
    private String merchantNo;
    private String studentRefNo;

    private String cardHolderName;
    private SignatureType signatureType;
    private boolean isCustomerCopy;
    private boolean isMerchantCopy;
    private boolean shouldSave;
    private boolean isOfflineSale;
    private boolean isPreAuth;
    private boolean isInstallment;
    private boolean isPreComp;
    private boolean isRefund;
    private boolean isCashBack;
    private boolean isQuasiCash;
    private boolean isCashAdvance;
    private boolean isQrSale;
    private boolean isAuthOnly;
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

        if (!TextUtils.isEmpty(totalAmount))
            totalAmount = totalAmount.toUpperCase();

        if (!TextUtils.isEmpty(cashBackAmount))
            cashBackAmount = cashBackAmount.toUpperCase();

        if (!TextUtils.isEmpty(cardHolderName))
            cardHolderName = cardHolderName.toUpperCase();

        if (!TextUtils.isEmpty(studentRefNo))
            studentRefNo = studentRefNo.toUpperCase();
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

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public SignatureType getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(SignatureType signatureType) {
        this.signatureType = signatureType;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardHolderName() {
        return cardHolderName;
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

    public boolean isPreAuth() {
        return isPreAuth;
    }
    public boolean isAuthOnly() {
        return isAuthOnly;
    }
    public void setPreAuth(boolean preAuth) {
        isPreAuth = preAuth;
    }
    public void setAuthOnly(boolean authonly) {
        isAuthOnly = authonly;
    }
    public boolean isInstallment() {
        return isInstallment;
    }

    public void setInstallment(boolean installment) {
        isInstallment = installment;
    }

    public boolean isPreComp() {
        return isPreComp;
    }

    public void setPreComp(boolean preComp) {
        isPreComp = preComp;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public boolean isRefund() {
        return isRefund;
    }

    public void setRefund(boolean refund) {
        isRefund = refund;
    }

    public void setCashBack(boolean cashBack) {
        isCashBack = cashBack;
    }

    public boolean isCashBack() {
        return isCashBack;
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

    public boolean isQuasiCash() {
        return isQuasiCash;
    }

    public void setQuasiCash(boolean quasiCash) {
        isQuasiCash = quasiCash;
    }

    public boolean isCashAdvance() {
        return isCashAdvance;
    }

    public void setCashAdvance(boolean cashAdvance) {
        isCashAdvance = cashAdvance;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getStudentRefNo() {
        return studentRefNo;
    }

    public void setStudentRefNo(String studentRefNo) {
        this.studentRefNo = studentRefNo;
    }

    public boolean isQrSale() {
        return isQrSale;
    }

    public void setQrSale(boolean qrSale) {
        isQrSale = qrSale;
    }

    @Override
    public String toString() {
        return "SaleReceipt{" +
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
                ", aid='" + aid + '\'' +
                ", apprCode='" + apprCode + '\'' +
                ", refNo='" + refNo + '\'' +
                ", currency='" + currency + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", baseAmount='" + baseAmount + '\'' +
                ", cashBackAmount='" + cashBackAmount + '\'' +
                ", merchantNo='" + merchantNo + '\'' +
                ", studentRefNo='" + studentRefNo + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", signatureType=" + signatureType +
                ", isCustomerCopy=" + isCustomerCopy +
                ", isMerchantCopy=" + isMerchantCopy +
                ", shouldSave=" + shouldSave +
                ", isOfflineSale=" + isOfflineSale +
                ", isPreAuth=" + isPreAuth +
                ", isInstallment=" + isInstallment +
                ", isPreComp=" + isPreComp +
                ", isRefund=" + isRefund +
                ", isCashBack=" + isCashBack +
                ", isQuasiCash=" + isQuasiCash +
                ", isCashAdvance=" + isCashAdvance +
                ", isAuthOnly=" + isAuthOnly +
                '}';
    }

}
