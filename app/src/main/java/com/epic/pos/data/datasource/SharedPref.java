package com.epic.pos.data.datasource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.epic.pos.iso.modal.response.SaleResponse;
import com.epic.pos.util.Utility;
import com.epic.pos.util.spcrypto.SPEncryptor;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
import com.google.gson.Gson;

import javax.inject.Inject;

public class SharedPref {

    public static final String PREFS_NAME = "com.epic.pos";
    private static final String KEY_PREF = "com.epic.pos.";

    public static final String AES_KEY = KEY_PREF + "p1";
    public static final String AES_INET_VECTOR = KEY_PREF + "p2";
    private static final String TOTAL_AMOUNT = KEY_PREF + "p3";
    private static final String CARD_ACTION = KEY_PREF + "p4";
    private static final String CARD_DATA = KEY_PREF + "p5";
    private static final String SELECTED_CDT = KEY_PREF + "p6";
    private static final String SALE_RESPONSE = KEY_PREF + "p10";
    private static final String SELECTED_TERMINAL_ID = KEY_PREF + "p11";
    private static final String CURRENT_SALE_ID = KEY_PREF + "p12";
    private static final String IS_CARD_PIN_ENTERED = KEY_PREF + "p13";
    private static final String CURRENT_VOID_SALE_ID = KEY_PREF + "p14";
    private static final String IS_MANUAL_SALE_FLOW = KEY_PREF + "p15";
    private static final String IS_OFFLINE_SALE_FLOW = KEY_PREF + "p16";
    private static final String IS_OFFLINE_MANUAL_SALE_FLOW = KEY_PREF + "p17";
    private static final String OFFLINE_APPROVAL_CODE = KEY_PREF + "p18";
    private static final String IS_QR_SALE_FLOW = KEY_PREF + "p19";
    private static final String IS_VOID_SALE_FLOW = KEY_PREF + "p20";
    private static final String IS_SIGNATURE_REQUIRED = KEY_PREF + "p21";
    private static final String IS_PRE_AUTH_SALE = KEY_PREF + "p22";
    private static final String IS_PRE_AUTH_MANUAL_SALE = KEY_PREF + "p23";
    private static final String SHOULD_PRINT_CLEAR_ISO_PACKET = KEY_PREF + "p24";
    private static final String IS_INSTALLMENT_SALE = KEY_PREF + "p25";
    private static final String IS_PER_COMP_SALE = KEY_PREF + "p26";
    private static final String CURRENT_PRE_COMP_SALE_ID = KEY_PREF + "p27";
    private static final String SELECTED_HOST_ID_FOR_VOID = KEY_PREF + "p28";
    private static final String SELECTED_MERCHANT_ID_FOR_VOID = KEY_PREF + "p29";
    private static final String SELECTED_HOST_ID_FOR_PRE_COM = KEY_PREF + "p30";
    private static final String SELECTED_MERCHANT_ID_FOR_PRE_COM = KEY_PREF + "p31";
    private static final String TLE_PWD = KEY_PREF + "p32";
    private static final String TRACE_NUMBER = KEY_PREF + "p33";
    private static final String SELECTED_MERCHANT_GROUP_ID = KEY_PREF + "p34";
    private static final String CARD_INITIATED_SALE = KEY_PREF + "p35";
    private static final String CARD_INITIATED_SALE_PIN_REQUESTED = KEY_PREF + "p36";
    private static final String CARD_INITIATED_IS_ONLINE_PIN = KEY_PREF + "p37";
    private static final String IS_TRANSACTION_ONGOING = KEY_PREF + "p38";
    private static final String CHECK_REMOVE_CARD = KEY_PREF + "p39";
    private static final String START_WITH_AMOUNT_ACTIVITY = KEY_PREF + "p40";
    private static final String FORCE_LOAD_HOME = KEY_PREF + "p41";
    private static final String CARD_INITIATED_SALE_WITH_CHIP = KEY_PREF + "p42";
    private static final String SHOULD_PRINT_ENC_ISO_PACKET = KEY_PREF + "p43";
    private static final String HAS_PENDING_AUTO_SETTLEMENT = KEY_PREF + "p44";
    public static final String HAS_PENDING_PROFILE_UPDATE = KEY_PREF + "p45";
    private static final String PROFILE_UPDATE_DATA = KEY_PREF + "p46";
    private static final String TERMINAL_DISABLED = KEY_PREF + "p47";
    private static final String IS_REFUND_SALE_FLOW = KEY_PREF + "p48";
    private static final String IS_MANUAL_REFUND_SALE_FLOW = KEY_PREF + "p49";
    private static final String IS_CASH_BACK_SALE_FLOW = KEY_PREF + "p50";
    private static final String CASH_BACK_AMOUNT = KEY_PREF + "p51";
    private static final String BASE_AMOUNT = KEY_PREF + "p52";
    private static final String IS_QUASI_CASH = KEY_PREF + "p53";
    private static final String IS_QUASI_CASH_MANUAL = KEY_PREF + "p54";
    private static final String IS_CASH_ADVANCE = KEY_PREF + "p55";
    private static final String IS_ONLINE_PIN_REQUESTED = KEY_PREF + "p56";
    private static final String IS_ECR_INITIATED_SALE = KEY_PREF + "p57";
    private static final String IS_STUDENT_REF_SALE = KEY_PREF + "p58";
    private static final String STUDENT_REFERENCE_NO = KEY_PREF + "p59";
    private static final String CONFIG_MAP_GENERATED = KEY_PREF + "p60"; //todo - update the key if config map should re generate for first time use.
    private static final String IS_LOG_ENABLE = KEY_PREF + "p61";
    private static final String IS_AUTH_ONLY_SALE = KEY_PREF + "p62";
    public static final String HAS_PENDING_APPLICATION_UPDATE = KEY_PREF + "p63";
    public static final String IS_AUTO_SETTLEMENT_COMPLETED = KEY_PREF + "p64";
    public static final String IS_TRAN_SYNC_ENABLE= KEY_PREF +"p64";


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private Context context;

    private String aesKey;
    private String aesInetVector;

    @SuppressLint("CommitPrefEdits")
    @Inject
    public SharedPref(SharedPreferences sharedPreferences, Context context) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.sharedPreferencesEditor = sharedPreferences.edit();
        aesKey = sharedPreferences.getString(AES_KEY, "");
        aesInetVector = sharedPreferences.getString(AES_INET_VECTOR, "");
    }

    public void setLogEnable(boolean isLogEnabled) {
        putString(IS_LOG_ENABLE, String.valueOf(isLogEnabled));
    }

    public boolean isLogEnabled() {
        String isLogEnabled = getString(IS_LOG_ENABLE);
        if (!TextUtils.isEmpty(isLogEnabled)) {
            return Boolean.parseBoolean(isLogEnabled);
        } else {
            return false;
        }
    }

    public void saveConfigMapGenerated(boolean isGenerated) {
        putString(CONFIG_MAP_GENERATED, String.valueOf(isGenerated));
    }

    public boolean isConfigMapGenerated() {
        String isConfigMapGenerated = getString(CONFIG_MAP_GENERATED);
        if (!TextUtils.isEmpty(isConfigMapGenerated)) {
            return Boolean.parseBoolean(isConfigMapGenerated);
        } else {
            return false;
        }
    }

    public void saveStudentReference(String studentRef) {
        putString(STUDENT_REFERENCE_NO, String.valueOf(studentRef));
    }

    public String getStudentReferenceNo() {
        return getString(STUDENT_REFERENCE_NO);
    }

    public void setStudentRefSale(boolean studentRefSale) {
        putString(IS_STUDENT_REF_SALE, String.valueOf(studentRefSale));
    }

    public boolean isStudentRefSale() {
        String studentRefSale = getString(IS_STUDENT_REF_SALE);
        if (!TextUtils.isEmpty(studentRefSale)) {
            return Boolean.parseBoolean(studentRefSale);
        } else {
            return false;
        }
    }

    public void setEcrInitiatedSale(boolean ecrInitiatedSale) {
        putString(IS_ECR_INITIATED_SALE, String.valueOf(ecrInitiatedSale));
    }

    public boolean isEcrInitiatedSale() {
        String boolString = getString(IS_ECR_INITIATED_SALE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void setOnlinePinRequested(boolean isOnlinePinRequested) {
        putString(IS_ONLINE_PIN_REQUESTED, String.valueOf(isOnlinePinRequested));
    }

    public boolean isOnlinePinRequested() {
        String boolString = getString(IS_ONLINE_PIN_REQUESTED);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveCashAdvance(boolean cashAdvance) {
        putString(IS_CASH_ADVANCE, String.valueOf(cashAdvance));
    }

    public boolean isCashAdvance() {
        String boolString = getString(IS_CASH_ADVANCE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveQuasiCashManualFlow(boolean quasiCashManual) {
        putString(IS_QUASI_CASH_MANUAL, String.valueOf(quasiCashManual));
    }

    public boolean isQuasiCashManualFlow() {
        String boolString = getString(IS_QUASI_CASH_MANUAL);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveQuasiCashFlow(boolean quasiCash) {
        putString(IS_QUASI_CASH, String.valueOf(quasiCash));
    }

    public boolean isQuasiCashFlow() {
        String boolString = getString(IS_QUASI_CASH);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveBaseAmount(String baseAmount) {
        putString(BASE_AMOUNT, String.valueOf(baseAmount));
    }

    public String getBaseAmount() {
        return getString(BASE_AMOUNT);
    }

    public void saveCashbackAmount(String cashBackAmount) {
        putString(CASH_BACK_AMOUNT, String.valueOf(cashBackAmount));
    }

    public String getCashBackAmount() {
        return getString(CASH_BACK_AMOUNT);
    }

    public void saveCashBackSaleFlow(boolean cashbackSale) {
        putString(IS_CASH_BACK_SALE_FLOW, String.valueOf(cashbackSale));
    }

    public boolean isCashBackSaleFlow() {
        String boolString = getString(IS_CASH_BACK_SALE_FLOW);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveShouldDisableTerminal(boolean shouldDisableTerminal) {
        putString(TERMINAL_DISABLED, String.valueOf(shouldDisableTerminal));
    }

    public boolean isTerminalDisabled() {
        String boolString = getString(TERMINAL_DISABLED);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveProfileUpdateData(String profileUpdateData) {
        putString(PROFILE_UPDATE_DATA, profileUpdateData);
    }

    public String getProfileUpdateData() {
        return getString(PROFILE_UPDATE_DATA);
    }

    public void saveHasPendingProfileUpdate(boolean hasAutoSettlement) {
        putString(HAS_PENDING_PROFILE_UPDATE, String.valueOf(hasAutoSettlement));
    }

    public boolean hasPendingProfileUpdate() {
        String boolString = getString(HAS_PENDING_PROFILE_UPDATE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveHasPendingAutoSettlement(boolean hasAutoSettlement) {
        putString(HAS_PENDING_AUTO_SETTLEMENT, String.valueOf(hasAutoSettlement));
    }

    public boolean hasPendingAutoSettlement() {
        String boolString = getString(HAS_PENDING_AUTO_SETTLEMENT);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveCheckRemoveCard(boolean removeCard) {
        putString(CHECK_REMOVE_CARD, String.valueOf(removeCard));
    }

    public boolean isCheckRemoveCard() {
        String boolString = getString(CHECK_REMOVE_CARD);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveCardInitiatedSaleWithChip(boolean isChipSale) {
        putString(CARD_INITIATED_SALE_WITH_CHIP, String.valueOf(isChipSale));
    }

    public boolean isCardInitiatedSaleWithChip() {
        String boolString = getString(CARD_INITIATED_SALE_WITH_CHIP);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveForceLoadHome(boolean forceLoadHome) {
        putString(FORCE_LOAD_HOME, String.valueOf(forceLoadHome));
    }

    public boolean isForceLoadHome() {
        String boolString = getString(FORCE_LOAD_HOME);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveStartWithAmountActivity(boolean startWithAmount) {
        putString(START_WITH_AMOUNT_ACTIVITY, String.valueOf(startWithAmount));
    }

    public boolean isStartWithAmountActivity() {
        String boolString = getString(START_WITH_AMOUNT_ACTIVITY);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveTransactionOngoing(boolean txnOngoing) {
        putString(IS_TRANSACTION_ONGOING, String.valueOf(txnOngoing));
    }

    public boolean isTransactionOngoing() {
        String boolString = getString(IS_TRANSACTION_ONGOING);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveCardInitiatedSaleIsOnlinePin(boolean isPinRequested) {
        putString(CARD_INITIATED_IS_ONLINE_PIN, String.valueOf(isPinRequested));
    }

    public boolean isCardInitiatedSaleOnlinePin() {
        String boolString = getString(CARD_INITIATED_IS_ONLINE_PIN);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveCardInitiatedSalePinRequested(boolean isPinRequested) {
        putString(CARD_INITIATED_SALE_PIN_REQUESTED, String.valueOf(isPinRequested));
    }

    public boolean isCardInitiatedSalePinRequested() {
        String boolString = getString(CARD_INITIATED_SALE_PIN_REQUESTED);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveCardInitiatedSale(boolean isCardInitiatedSale) {
        putString(CARD_INITIATED_SALE, String.valueOf(isCardInitiatedSale));
    }

    public boolean isCardInitiatedSale() {
        String boolString = getString(CARD_INITIATED_SALE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void saveSelectedMerchantGroupId(int id) {
        putString(SELECTED_MERCHANT_GROUP_ID, String.valueOf(id));
    }

    public int getSelectedMerchantGroupId() {
        String idString = getString(SELECTED_MERCHANT_GROUP_ID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveSelectedHostIdForPreComp(int id) {
        putString(SELECTED_HOST_ID_FOR_PRE_COM, String.valueOf(id));
    }

    public int getSelectedHostIdForPreComp() {
        String idString = getString(SELECTED_HOST_ID_FOR_PRE_COM);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveSelectedMerchantIdForPreComp(int id) {
        putString(SELECTED_MERCHANT_ID_FOR_PRE_COM, String.valueOf(id));
    }

    public int getSelectedMerchantIdForPreComp() {
        String idString = getString(SELECTED_MERCHANT_ID_FOR_PRE_COM);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveSelectedMerchantIdForVoid(int id) {
        putString(SELECTED_MERCHANT_ID_FOR_VOID, String.valueOf(id));
    }

    public int getSelectedMerchantIdForVoid() {
        String idString = getString(SELECTED_MERCHANT_ID_FOR_VOID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveSelectedHostIdForVoid(int id) {
        putString(SELECTED_HOST_ID_FOR_VOID, String.valueOf(id));
    }

    public int getSelectedHostIdForVoid() {
        String idString = getString(SELECTED_HOST_ID_FOR_VOID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveCurrentPreCompId(int id) {
        putString(CURRENT_PRE_COMP_SALE_ID, String.valueOf(id));
    }

    public int getCurrentPreCompId() {
        String idString = getString(CURRENT_PRE_COMP_SALE_ID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void setPreCompSale(boolean isPreCompSale) {
        putString(IS_PER_COMP_SALE, String.valueOf(isPreCompSale));
    }

    public boolean isPreCompSale() {
        String isPreCompSale = getString(IS_PER_COMP_SALE);
        if (!TextUtils.isEmpty(isPreCompSale)) {
            return Boolean.parseBoolean(isPreCompSale);
        } else {
            return false;
        }
    }

    public void setInstallmentSale(boolean isInstallmentSale) {
        putString(IS_INSTALLMENT_SALE, String.valueOf(isInstallmentSale));
    }

    public boolean isInstallmentSale() {
        String isInstallmentSale = getString(IS_INSTALLMENT_SALE);
        if (!TextUtils.isEmpty(isInstallmentSale)) {
            return Boolean.parseBoolean(isInstallmentSale);
        } else {
            return false;
        }
    }

    public void setPrintClearISOPacket(boolean printClearISOPacket) {
        putString(SHOULD_PRINT_CLEAR_ISO_PACKET, String.valueOf(printClearISOPacket));
    }

    public void setPrintEncISOPacket(boolean printEncISOPacket) {
        putString(SHOULD_PRINT_ENC_ISO_PACKET, String.valueOf(printEncISOPacket));
    }

    public boolean isPrintClearISOPacket() {
        String isPreAuthManualSale = getString(SHOULD_PRINT_CLEAR_ISO_PACKET);
        if (!TextUtils.isEmpty(isPreAuthManualSale)) {
            return Boolean.parseBoolean(isPreAuthManualSale);
        } else {
            return false;
        }
    }

    public boolean isPrintEncISOPacket() {
        String isPrint = getString(SHOULD_PRINT_ENC_ISO_PACKET);

        if (!TextUtils.isEmpty(isPrint)) {
            return Boolean.parseBoolean(isPrint);
        } else {
            return false;
        }
    }


    public void setPreAuthManualSale(boolean isPreAuthManualSale) {
        putString(IS_PRE_AUTH_MANUAL_SALE, String.valueOf(isPreAuthManualSale));
    }

    public boolean isPreAuthManualSale() {
        String isPreAuthManualSale = getString(IS_PRE_AUTH_MANUAL_SALE);
        if (!TextUtils.isEmpty(isPreAuthManualSale)) {
            return Boolean.parseBoolean(isPreAuthManualSale);
        } else {
            return false;
        }
    }

    public void setPreAuthSale(boolean isPreAuthSale) {
        putString(IS_PRE_AUTH_SALE, String.valueOf(isPreAuthSale));
    }
    public void setAuthOnlySale(boolean isAuthOnlySale) {
        putString(IS_AUTH_ONLY_SALE, String.valueOf(isAuthOnlySale));
    }
    public boolean isPreAuthSale() {
        String isPreAuthSale = getString(IS_PRE_AUTH_SALE);
        if (!TextUtils.isEmpty(isPreAuthSale)) {
            return Boolean.parseBoolean(isPreAuthSale);
        } else {
            return false;
        }
    }
    public boolean isAuthOnlySale() {
        String isAuthOnlySale = getString(IS_AUTH_ONLY_SALE);
        if (!TextUtils.isEmpty(isAuthOnlySale)) {
            return Boolean.parseBoolean(isAuthOnlySale);
        } else {
            return false;
        }
    }
    public void setSignatureRequired(boolean isSignatureRequired) {
        putString(IS_SIGNATURE_REQUIRED, String.valueOf(isSignatureRequired));
    }

    public boolean isSignatureRequired() {
        String isSigRequired = getString(IS_SIGNATURE_REQUIRED);
        if (!TextUtils.isEmpty(isSigRequired)) {
            return Boolean.parseBoolean(isSigRequired);
        } else {
            return false;
        }
    }

    public void setRefundSaleFlow(boolean isRefund) {
        putString(IS_REFUND_SALE_FLOW, String.valueOf(isRefund));
    }

    public boolean isRefundSale() {
        String isRefund = getString(IS_REFUND_SALE_FLOW);
        if (!TextUtils.isEmpty(isRefund)) {
            return Boolean.parseBoolean(isRefund);
        } else {
            return false;
        }
    }

    public void setManualRefundSaleFlow(boolean isRefund) {
        putString(IS_MANUAL_REFUND_SALE_FLOW, String.valueOf(isRefund));
    }

    public boolean isManualRefundSale() {
        String isManualRefund = getString(IS_MANUAL_REFUND_SALE_FLOW);
        if (!TextUtils.isEmpty(isManualRefund)) {
            return Boolean.parseBoolean(isManualRefund);
        } else {
            return false;
        }
    }

    public void setVoidSale(boolean isVoidSale) {
        putString(IS_VOID_SALE_FLOW, String.valueOf(isVoidSale));
    }

    public boolean isVoidSale() {
        String isVoidSale = getString(IS_VOID_SALE_FLOW);
        if (!TextUtils.isEmpty(isVoidSale)) {
            return Boolean.parseBoolean(isVoidSale);
        } else {
            return false;
        }
    }

    public void setQrSale(boolean isQrSale) {
        putString(IS_QR_SALE_FLOW, String.valueOf(isQrSale));
    }

    public boolean isQrSale() {
        String isQrSale = getString(IS_QR_SALE_FLOW);
        if (!TextUtils.isEmpty(isQrSale)) {
            return Boolean.parseBoolean(isQrSale);
        } else {
            return false;
        }
    }

    public void saveOfflineApprovalCode(String approvalCode) {
        putString(OFFLINE_APPROVAL_CODE, approvalCode);
    }

    public String getOfflineApprovalCode() {
        return getString(OFFLINE_APPROVAL_CODE);
    }

    public void setOfflineManualSale(boolean isOfflineManualSale) {
        putString(IS_OFFLINE_MANUAL_SALE_FLOW, String.valueOf(isOfflineManualSale));
    }

    public boolean isOfflineManualSale() {
        String isOfflineManualSale = getString(IS_OFFLINE_MANUAL_SALE_FLOW);
        if (!TextUtils.isEmpty(isOfflineManualSale)) {
            return Boolean.parseBoolean(isOfflineManualSale);
        } else {
            return false;
        }
    }

    public void setOfflineSale(boolean isOfflineSale) {
        putString(IS_OFFLINE_SALE_FLOW, String.valueOf(isOfflineSale));
    }

    public boolean isOfflineSale() {
        String isOfflineSale = getString(IS_OFFLINE_SALE_FLOW);
        if (!TextUtils.isEmpty(isOfflineSale)) {
            return Boolean.parseBoolean(isOfflineSale);
        } else {
            return false;
        }
    }

    public void setManualSale(boolean isManualSale) {
        putString(IS_MANUAL_SALE_FLOW, String.valueOf(isManualSale));
    }

    public boolean isManualSale() {
        String isManualSale = getString(IS_MANUAL_SALE_FLOW);
        if (!TextUtils.isEmpty(isManualSale)) {
            return Boolean.parseBoolean(isManualSale);
        } else {
            return false;
        }
    }

    public void saveCurrentVoidSaleId(int id) {
        putString(CURRENT_VOID_SALE_ID, String.valueOf(id));
    }

    public int getCurrentVoidSaleId() {
        String idString = getString(CURRENT_VOID_SALE_ID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void setIsCardPinEntered(boolean isCardPinEntered) {
        putString(IS_CARD_PIN_ENTERED, String.valueOf(isCardPinEntered));
    }

    public boolean isCardPinEntered() {
        String string = getString(IS_CARD_PIN_ENTERED);
        if (!TextUtils.isEmpty(string)) {
            return Boolean.parseBoolean(string);
        } else {
            return false;
        }
    }

    public void saveCurrentSaleId(int id) {
        putString(CURRENT_SALE_ID, String.valueOf(id));
    }

    public int getCurrentSaleId() {
        String idString = getString(CURRENT_SALE_ID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveSelectedTerminalId(int id) {
        putString(SELECTED_TERMINAL_ID, String.valueOf(id));
    }

    public int getSelectedTerminalId() {
        String idString = getString(SELECTED_TERMINAL_ID);
        if (!TextUtils.isEmpty(idString)) {
            return Integer.parseInt(idString);
        } else {
            return 0;
        }
    }

    public void saveSaleResponse(SaleResponse saleResponse) {
        String json = new Gson().toJson(saleResponse);
        putString(SALE_RESPONSE, json);
    }

    public SaleResponse getSaleResponse() {
        String json = getString(SALE_RESPONSE);
        if (!TextUtils.isEmpty(json)) {
            return new Gson().fromJson(json, SaleResponse.class);
        } else {
            return null;
        }
    }

    public void saveSelectedCardDefinitionId(int id) {
        putString(SELECTED_CDT, String.valueOf(id));
    }

    public int getSelectedCardDefinitionId() {
        String cdtString = getString(SELECTED_CDT);
        if (!TextUtils.isEmpty(cdtString)) {
            return Integer.parseInt(cdtString);
        } else {
            return 0;
        }
    }

    public void saveCardData(CardData cardData) {
        String cardDataJson = new Gson().toJson(cardData);
        putString(CARD_DATA, cardDataJson);
    }

    public CardData getCardData() {
        String cardDataJson = getString(CARD_DATA);
        if (!TextUtils.isEmpty(cardDataJson)) {
            return new Gson().fromJson(cardDataJson, CardData.class);
        } else {
            return null;
        }
    }

    public void saveCardAction(CardAction cardAction) {
        putString(CARD_ACTION, String.valueOf(cardAction.val));
    }

    public CardAction getCardAction() {
        String cA = getString(CARD_ACTION);
        if (!TextUtils.isEmpty(cA)) {
            return CardAction.valueOf(Integer.parseInt(cA));
        } else {
            return null;
        }
    }

    public void saveTotalAmount(String amount) {
        putString(TOTAL_AMOUNT, amount);
    }

    public String getTotalAmount() {
        return getString(TOTAL_AMOUNT);
    }

    public String getString(String key) {
        String encVal = sharedPreferences.getString(key, "");

        if (!TextUtils.isEmpty(encVal)) {
            return SPEncryptor.decrypt(aesKey, aesInetVector, encVal);
        } else {
            return "";
        }
    }

    private void putString(String key, String value) {
        String encVal = SPEncryptor.encrypt(aesKey, aesInetVector, value);
        sharedPreferencesEditor.putString(key, encVal);
        sharedPreferencesEditor.commit();
    }

    public void saveTLEPassword(String pwd) {
        putString(TLE_PWD, pwd);
    }

    public String getTLEPassword() {

        return getString(TLE_PWD);
    }

    public void incrementTraceNumber() {
        String invoiceNumber = getString(TRACE_NUMBER);

        if (TextUtils.isEmpty(invoiceNumber)) {
            invoiceNumber = "000001";
        } else {
            int num = Integer.parseInt(invoiceNumber) + 1;
            invoiceNumber = Utility.padLeftZeros(String.valueOf(num), 6);
        }

        putString(TRACE_NUMBER, invoiceNumber);
    }

    public String getTraceNumber() {
        return getString(TRACE_NUMBER);
    }

    public void saveHasPendingApplicationUpdate(boolean hasAutoSettlement) {
        putString(HAS_PENDING_APPLICATION_UPDATE, String.valueOf(hasAutoSettlement));
    }

    public boolean hasPendingApplicationUpdate() {
        String boolString = getString(HAS_PENDING_APPLICATION_UPDATE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }


    public void saveAutoSettlementCompleted(boolean autoSettleCompleted) {
        putString(IS_AUTO_SETTLEMENT_COMPLETED, String.valueOf(autoSettleCompleted));
    }

    public boolean isAutoSettlementCompleted() {
        String boolString = getString(IS_AUTO_SETTLEMENT_COMPLETED);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }

    public void setIsTranSyncEnable(boolean isTranSyncEnable){
        putString(IS_TRAN_SYNC_ENABLE, String.valueOf(isTranSyncEnable));
    }

    public boolean isTranSyncEnable(){
        String boolString = getString(IS_TRAN_SYNC_ENABLE);
        if (!TextUtils.isEmpty(boolString)) {
            return Boolean.parseBoolean(boolString);
        } else {
            return false;
        }
    }
}
