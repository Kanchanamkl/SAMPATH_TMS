package com.epic.pos.data.db.dbpos.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TCT")
public class TCT {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int ID;

    @ColumnInfo(name = "ManagerPassword")
    private String ManagerPassword;

    @ColumnInfo(name = "SuperPassword")
    private String SuperPassword;

    @ColumnInfo(name = "DateFormat")
    private String DateFormat;

    @ColumnInfo(name = "FallBackTime")
    private String FallBackTime;

    @ColumnInfo(name = "TipPercent")
    private int TipPercent;

    @ColumnInfo(name = "TipProcessing")
    private int TipProcessing;

    @ColumnInfo(name = "ConfirmLast4")
    private int ConfirmLast4;

    @ColumnInfo(name = "IdleTimeOut")
    private int IdleTimeOut;

    @ColumnInfo(name = "AutoSettleEnable")
    private int AutoSettleEnable;

    @ColumnInfo(name = "LanguageIndex")
    private int LanguageIndex;

    @ColumnInfo(name = "CTLSEnable")
    private int CTLSEnable;

    @ColumnInfo(name = "URL")
    private String URL;

    @ColumnInfo(name = "InstituteName")
    private String InstituteName;

    @ColumnInfo(name = "DCCEnable")
    private int DCCEnable;

    @ColumnInfo(name = "InstallmentEnable")
    private int InstallmentEnable;

    @ColumnInfo(name = "LKR_USDEnabled")
    private int LKR_USDEnabled;

    @ColumnInfo(name = "ReversalTimeout")
    private int ReversalTimeout;

    @ColumnInfo(name = "ConnectTimeout")
    private int ConnectTimeout;

    @ColumnInfo(name = "PinEntryTimeout")
    private int PinEntryTimeout;

    @ColumnInfo(name = "HttpConTimeOut")
    private String HttpConTimeOut;

    @ColumnInfo(name = "HttpRedTimeOut")
    private String HttpRedTimeOut;

    @ColumnInfo(name = "EchoTimeOut")
    private String EchoTimeOut;

    @ColumnInfo(name = "PinEncryption")
    private int PinEncryption;

    @ColumnInfo(name = "ROMFolderName")
    private String ROMFolderName;

    @ColumnInfo(name = "BankName")
    private String BankName;

    @ColumnInfo(name = "BulkCount")
    private int BulkCount;

    @ColumnInfo(name = "MonitorBattery")
    private int MonitorBattery;

    @ColumnInfo(name = "SpeechEnabled")
    private int SpeechEnabled;

    @ColumnInfo(name = "ThemeEnabled")
    private int ThemeEnabled;

    @ColumnInfo(name = "AutoSettTime")
    private String AutoSettTime;

    @ColumnInfo(name = "AutoSettDate")
    private String AutoSettDate;

    @ColumnInfo(name = "FallbackEnable")
    private int FallbackEnable;

    @ColumnInfo(name = "IP_TMS")
    private String IP_TMS;

    @ColumnInfo(name = "PORT_TMS")
    private int PORT_TMS;

    @ColumnInfo(name = "ClearReversalPassword")
    private String ClearReversalPassword;

    @ColumnInfo(name = "ExitPassword")
    private String ExitPassword;

    @ColumnInfo(name = "StartWithAmountEnter")
    private int StartWithAmountEnter;

    @ColumnInfo(name = "StartAmountHomeIdleTimeout")
    private int StartAmountHomeIdleTimeout;

    @ColumnInfo(name = "TxnDetailIdleTimeout")
    private int TxnDetailIdleTimeout;

    @ColumnInfo(name = "MaxTxnAmountLen")
    private int MaxTxnAmountLen;

    @ColumnInfo(name = "MaxTxnAmount")
    private String MaxTxnAmount;

    @ColumnInfo(name = "MaxTxnCount")
    private int MaxTxnCount;

    @ColumnInfo(name = "CriticalBatteryLevel")
    private int CriticalBatteryLevel;

    @ColumnInfo(name = "BatteryLowLevel")
    private int BatteryLowLevel;

    @ColumnInfo(name = "CashBackPercentage")
    private String CashBackPercentage;

    @ColumnInfo(name = "EditTablePassword")
    private String EditTablePassword;

    @ColumnInfo(name = "StartUpAutoRun")
    private int StartUpAutoRun;

    @ColumnInfo(name = "AutoSettlementTryCount")
    private int AutoSettlementTryCount;

    @ColumnInfo(name = "EcrEnabled")
    private int EcrEnabled;

    @ColumnInfo(name = "ErrorPageCountdown")
    private int ErrorPageCountdown;

    @ColumnInfo(name = "BypassTxnConf")
    private int BypassTxnConf;

    @ColumnInfo(name = "SkipReceipt")
    private int SkipReceipt;

    @ColumnInfo(name = "OfflineTranLimit")
    private String OfflineTranLimit;

    @ColumnInfo(name = "ManualSalePassword")
    private int ManualSalePassword;

    @ColumnInfo(name = "ECRWithourReceipt")
    private int ECRWithourReceipt;

    @ColumnInfo(name = "DCCNII")
    private String DCCNII;

    @ColumnInfo(name = "IsAuronetDCC")
    private int IsAuronetDCC;

    @ColumnInfo(name = "MinDCCAmount")
    private int MinDCCAmount;

    @ColumnInfo(name = "APPCODE")
    private String appcode;

    @ColumnInfo(name = "MaxQRAmount")
    private String maxqramount;

    @ColumnInfo(name = "MinQRAmount")
    private String minqramount;

    public String getMaxqramount() {
        return maxqramount;
    }

    public void setMaxqramount(String maxqramount) {
        this.maxqramount = maxqramount;
    }

    public String getMinqramount() {
        return minqramount;
    }

    public void setMinqramount(String minqramount) {
        this.minqramount = minqramount;
    }

    public int getMinDCCAmount() {
        return MinDCCAmount;
    }

    public void setMinDCCAmount(int minDCCAmount) {
        MinDCCAmount = minDCCAmount;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getManagerPassword() {
        return ManagerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        ManagerPassword = managerPassword;
    }

    public String getSuperPassword() {
        return SuperPassword;
    }

    public void setSuperPassword(String superPassword) {
        SuperPassword = superPassword;
    }

    public String getDateFormat() {
        return DateFormat;
    }

    public void setDateFormat(String dateFormat) {
        DateFormat = dateFormat;
    }

    public String getFallBackTime() {
        return FallBackTime;
    }

    public void setFallBackTime(String fallBackTime) {
        FallBackTime = fallBackTime;
    }

    public int getTipPercent() {
        return TipPercent;
    }

    public void setTipPercent(int tipPercent) {
        TipPercent = tipPercent;
    }

    public int getTipProcessing() {
        return TipProcessing;
    }

    public void setTipProcessing(int tipProcessing) {
        TipProcessing = tipProcessing;
    }

    public int getConfirmLast4() {
        return ConfirmLast4;
    }

    public void setConfirmLast4(int confirmLast4) {
        ConfirmLast4 = confirmLast4;
    }

    public int getIdleTimeOut() {
        return IdleTimeOut;
    }

    public void setIdleTimeOut(int idleTimeOut) {
        IdleTimeOut = idleTimeOut;
    }

    public int getAutoSettleEnable() {
        return AutoSettleEnable;
    }

    public void setAutoSettleEnable(int autoSettleEnable) {
        AutoSettleEnable = autoSettleEnable;
    }

    public int getLanguageIndex() {
        return LanguageIndex;
    }

    public void setLanguageIndex(int languageIndex) {
        LanguageIndex = languageIndex;
    }

    public int getCTLSEnable() {
        return CTLSEnable;
    }

    public void setCTLSEnable(int CTLSEnable) {
        this.CTLSEnable = CTLSEnable;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getInstituteName() {
        return InstituteName;
    }

    public void setInstituteName(String instituteName) {
        InstituteName = instituteName;
    }

    public int getDCCEnable() {
        return DCCEnable;
    }

    public void setDCCEnable(int DCCEnable) {
        this.DCCEnable = DCCEnable;
    }

    public int getInstallmentEnable() {
        return InstallmentEnable;
    }

    public void setInstallmentEnable(int installmentEnable) {
        InstallmentEnable = installmentEnable;
    }

    public int getLKR_USDEnabled() {
        return LKR_USDEnabled;
    }

    public void setLKR_USDEnabled(int LKR_USDEnabled) {
        this.LKR_USDEnabled = LKR_USDEnabled;
    }

    public int getReversalTimeout() {
        return ReversalTimeout;
    }

    public void setReversalTimeout(int reversalTimeout) {
        ReversalTimeout = reversalTimeout;
    }

    public int getConnectTimeout() {
        return ConnectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        ConnectTimeout = connectTimeout;
    }

    public int getPinEntryTimeout() {
        return PinEntryTimeout;
    }

    public void setPinEntryTimeout(int pinEntryTimeout) {
        PinEntryTimeout = pinEntryTimeout;
    }

    public String getHttpConTimeOut() {
        return HttpConTimeOut;
    }

    public void setHttpConTimeOut(String httpConTimeOut) {
        HttpConTimeOut = httpConTimeOut;
    }

    public String getHttpRedTimeOut() {
        return HttpRedTimeOut;
    }

    public void setHttpRedTimeOut(String httpRedTimeOut) {
        HttpRedTimeOut = httpRedTimeOut;
    }

    public String getEchoTimeOut() {
        return EchoTimeOut;
    }

    public void setEchoTimeOut(String echoTimeOut) {
        EchoTimeOut = echoTimeOut;
    }

    public int getPinEncryption() {
        return PinEncryption;
    }

    public void setPinEncryption(int pinEncryption) {
        PinEncryption = pinEncryption;
    }

    public String getROMFolderName() {
        return ROMFolderName;
    }

    public void setROMFolderName(String ROMFolderName) {
        this.ROMFolderName = ROMFolderName;
    }

    public String getBankName() {
        return BankName;
    }

    public void setBankName(String bankName) {
        BankName = bankName;
    }

    public int getBulkCount() {
        return BulkCount;
    }

    public void setBulkCount(int bulkCount) {
        BulkCount = bulkCount;
    }

    public int getMonitorBattery() {
        return MonitorBattery;
    }

    public void setMonitorBattery(int monitorBattery) {
        MonitorBattery = monitorBattery;
    }

    public int getSpeechEnabled() {
        return SpeechEnabled;
    }

    public void setSpeechEnabled(int speechEnabled) {
        SpeechEnabled = speechEnabled;
    }

    public int getThemeEnabled() {
        return ThemeEnabled;
    }

    public void setThemeEnabled(int themeEnabled) {
        ThemeEnabled = themeEnabled;
    }

    public String getAutoSettTime() {
        return AutoSettTime;
    }

    public void setAutoSettTime(String autoSettTime) {
        AutoSettTime = autoSettTime;
    }

    public String getAutoSettDate() {
        return AutoSettDate;
    }

    public void setAutoSettDate(String autoSettDate) {
        AutoSettDate = autoSettDate;
    }

    public int getFallbackEnable() {
        return FallbackEnable;
    }

    public void setFallbackEnable(int fallbackEnable) {
        FallbackEnable = fallbackEnable;
    }

    public String getIP_TMS() {
        return IP_TMS;
    }

    public void setIP_TMS(String IP_TMS) {
        this.IP_TMS = IP_TMS;
    }

    public int getPORT_TMS() {
        return PORT_TMS;
    }

    public void setPORT_TMS(int PORT_TMS) {
        this.PORT_TMS = PORT_TMS;
    }

    public String getClearReversalPassword() {
        return ClearReversalPassword;
    }

    public void setClearReversalPassword(String clearReversalPassword) {
        ClearReversalPassword = clearReversalPassword;
    }

    public String getExitPassword() {
        return ExitPassword;
    }

    public void setExitPassword(String exitPassword) {
        ExitPassword = exitPassword;
    }

    public int getStartWithAmountEnter() {
        return StartWithAmountEnter;
    }

    public void setStartWithAmountEnter(int startWithAmountEnter) {
        StartWithAmountEnter = startWithAmountEnter;
    }

    public int getStartAmountHomeIdleTimeout() {
        return StartAmountHomeIdleTimeout;
    }

    public void setStartAmountHomeIdleTimeout(int startAmountHomeIdleTimeout) {
        StartAmountHomeIdleTimeout = startAmountHomeIdleTimeout;
    }

    public int getTxnDetailIdleTimeout() {
        return TxnDetailIdleTimeout;
    }

    public void setTxnDetailIdleTimeout(int txnDetailIdleTimeout) {
        TxnDetailIdleTimeout = txnDetailIdleTimeout;
    }

    public int getMaxTxnAmountLen() {
        return MaxTxnAmountLen;
    }

    public void setMaxTxnAmountLen(int maxTxnAmountLen) {
        MaxTxnAmountLen = maxTxnAmountLen;
    }

    public String getMaxTxnAmount() {
        return MaxTxnAmount;
    }

    public void setMaxTxnAmount(String maxTxnAmount) {
        MaxTxnAmount = maxTxnAmount;
    }

    public int getMaxTxnCount() {
        return MaxTxnCount;
    }

    public void setMaxTxnCount(int maxTxnCount) {
        MaxTxnCount = maxTxnCount;
    }

    public int getCriticalBatteryLevel() {
        return CriticalBatteryLevel;
    }

    public void setCriticalBatteryLevel(int criticalBatteryLevel) {
        CriticalBatteryLevel = criticalBatteryLevel;
    }

    public int getBatteryLowLevel() {
        return BatteryLowLevel;
    }

    public void setBatteryLowLevel(int batteryLowLevel) {
        BatteryLowLevel = batteryLowLevel;
    }

    public String getCashBackPercentage() {
        return CashBackPercentage;
    }

    public void setCashBackPercentage(String cashBackPercentage) {
        CashBackPercentage = cashBackPercentage;
    }

    public String getEditTablePassword() {
        return EditTablePassword;
    }

    public void setEditTablePassword(String editTablePassword) {
        EditTablePassword = editTablePassword;
    }

    public int getStartUpAutoRun() {
        return StartUpAutoRun;
    }

    public void setStartUpAutoRun(int startUpAutoRun) {
        StartUpAutoRun = startUpAutoRun;
    }

    public int getAutoSettlementTryCount() {
        return AutoSettlementTryCount;
    }

    public void setAutoSettlementTryCount(int autoSettlementTryCount) {
        AutoSettlementTryCount = autoSettlementTryCount;
    }

    public int getEcrEnabled() {
        return EcrEnabled;
    }

    public void setEcrEnabled(int ecrEnabled) {
        EcrEnabled = ecrEnabled;
    }

    public int getErrorPageCountdown() {
        return ErrorPageCountdown;
    }

    public void setErrorPageCountdown(int errorPageCountdown) {
        ErrorPageCountdown = errorPageCountdown;
    }

    public void setBypassTxnConf(int BypassTxnConf) {
        this.BypassTxnConf = BypassTxnConf;
    }

    public int getBypassTxnConf() {
        return BypassTxnConf;
    }

    public void setSkipReceipt(int SkipReceipt) {
        this.SkipReceipt = SkipReceipt;
    }

    public int getSkipReceipt() {
        return SkipReceipt;
    }

    public String getOfflineTranLimit() {
        return OfflineTranLimit;
    }

    public void setOfflineTranLimit(String offlineTranLimit) {
        OfflineTranLimit = offlineTranLimit;
    }
    public int getManualSalePassword() {
        return ManualSalePassword;
    }

    public void setManualSalePassword(int manualSalePassword) {
        ManualSalePassword = manualSalePassword;
    }

    public int getECRWithourReceipt() {
        return ECRWithourReceipt;
    }

    public void setECRWithourReceipt(int ECRWithourReceipt) {
        this.ECRWithourReceipt = ECRWithourReceipt;
    }

    public String getDCCNII() {
        return DCCNII;
    }

    public void setDCCNII(String DCCNII) {
        this.DCCNII = DCCNII;
    }


    public int getIsAuronetDCC() {
        return IsAuronetDCC;
    }

    public void setIsAuronetDCC(int isAuronetDCC) {
        IsAuronetDCC = isAuronetDCC;
    }

    public String getAppcode() {
        return appcode;
    }

    public void setAppcode(String appcode) {
        this.appcode = appcode;
    }
}
