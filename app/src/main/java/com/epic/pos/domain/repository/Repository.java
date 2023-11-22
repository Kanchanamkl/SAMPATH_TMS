package com.epic.pos.domain.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.data.db.dccdb.model.DCCData;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.ErrorResponse;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.data.model.respone.ServiceResponse;
import com.epic.pos.domain.entity.ColumnInfoEntity;
import com.epic.pos.domain.entity.ConfigMapEntity;
import com.epic.pos.domain.entity.TerminalEntity;
import com.epic.pos.iso.modal.request.BatchUploadRequest;
import com.epic.pos.iso.modal.request.DCCRequest;
import com.epic.pos.iso.modal.request.KeyDownloadTransaction;
import com.epic.pos.iso.modal.request.QrRequest;
import com.epic.pos.iso.modal.request.OfflineSaleRequest;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.SaleRequest;
import com.epic.pos.iso.modal.request.SettlementRequest;
import com.epic.pos.iso.modal.request.VoidRequest;
import com.epic.pos.iso.modal.response.BatchUploadResponse;
import com.epic.pos.iso.modal.response.DCCDownloadResponse;
import com.epic.pos.iso.modal.response.DCCResponse;
import com.epic.pos.iso.modal.response.KeyDownloadResponse;
import com.epic.pos.iso.modal.response.QrResponse;
import com.epic.pos.iso.modal.response.OfflineSaleResponse;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.iso.modal.response.SaleResponse;
import com.epic.pos.iso.modal.response.SettlementResponse;
import com.epic.pos.iso.modal.response.VoidResponse;
import com.epic.pos.tle.TLEData;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;

import java.util.ArrayList;
import java.util.List;

public interface Repository {

    //ISO
    void saleRequest(Issuer issuer, SaleRequest saleRequest, TLEData tleData, SaleTransactionListener listener);

    void reversalRequest(Issuer issuer, ReversalRequest reversalRequest, TLEData tleData, ReversalTransactionListener listener);

    void voidRequest(Issuer issuer, VoidRequest voidRequest, TLEData tleData, VoidRequestListener listener);

    void qrRequest(String ip, String port, QrRequest qrRequest, QrRequestListener listener);

    void settlementRequest(Issuer issuer, SettlementRequest settlementRequest, TLEData tleData, SettlementRequestListener listener);

    void batchUploadRequest(Issuer issuer, BatchUploadRequest batchUploadRequest, TLEData tleData, BatchUploadRequestListener listener);

    void DCCRequest(Issuer issuer, DCCRequest dccRequest, TLEData tleData, DCCRequestListener listener);

    void keyDownloadRequest(Issuer issuer, KeyDownloadTransaction keyDownloadTransaction, KeyDownloadListener listener);

    void DCCDataDownloadRequest(Issuer issuer,Merchant merchant,Terminal terminal,DCCDownloadRequestListener listener);

    void offlineSaleRequest(Issuer issuer, OfflineSaleRequest offlineSaleRequest, TLEData tleData, OfflineSaleListener listener);

    //Shared Pref
    void saveBaseAmount(String amount);

    void saveTotalAmount(String amount);

    void saveCashBackAmount(String amount);

    String getBaseAmount();

    String getTotalAmount();

    String getCashBackAmount();

    void saveCardAction(CardAction cardAction);

    CardAction getCardAction();

    void saveCardData(CardData cardData);

    CardData getCardData();

    void saveSelectedCardDefinitionId(int id);

    int getSelectedCardDefinitionId();

    void getCardDefinitionById(int id, DbHandler.GetCardDefinitionListener listener);

    void saveSelectedMerchantGroupId(int id);

    int getSelectedMerchantGroupId();

    void saveSaleResponse(SaleResponse saleResponse);

    SaleResponse getSaleResponse();

    void saveSelectedTerminalId(int terminalId);

    int getSelectedTerminalId();

    void getTerminalById(int id, DbHandler.GetTerminalListener listener);

    void getTerminalByMerchant(int merchantId, DbHandler.GetTerminalListener listener);

    void saveCurrentSaleId(int id);

    int getCurrentSaleId();

    int getCurrentPreCompSaleId();

    void setIsCardPinEntered(boolean isCardPinEntered);

    boolean isCardPinEntered();

    void saveCurrentVoidSaleId(int id);

    void saveCurrentPreCompSaleId(int id);

    int getCurrentVoidSaleId();

    void setManualSale(boolean isManualSale);

    void setOfflineSale(boolean isOfflineSale);

    void setOfflineManualSale(boolean isOfflineManualSale);

    void setVoidSale(boolean isVoidSale);

    void setQrSale(boolean isQrSale);

    void setPreAuthSale(boolean isPreAuth);

    void setAuthOnlySale(boolean isPreAuth);

    void setPreAuthManualSale(boolean isPreAuthManual);

    void setInstallmentSale(boolean isInstallment);

    void setPreCompSale(boolean isPreCompSale);

    void setRefundSale(boolean isRefund);

    void setRefundManualSale(boolean isManualRefund);

    void setCashBackSale(boolean cashBackSale);

    void setStudentRefSale(boolean isStudentRefSale);

    void saveStudentReferenceNo(String referenceNo);

    boolean isManualSale();

    boolean isOfflineSale();

    boolean isOfflineManualSale();

    boolean isVoidSale();

    boolean isQrSale();

    boolean isPreAuthSale();

    boolean isAuthOnlySale();

    boolean isPreAuthManualSale();

    boolean isInstallmentSale();

    boolean isPreCompSale();

    boolean isRefundSale();

    boolean isRefundManualSale();

    boolean isCashBackSale();

    boolean isStudentRefSale();

    String getStudentReferenceNo();

    void saveOfflineApprovalCode(String approvalCode);

    String getOfflineApprovalCode();

    void setSignatureRequired(boolean isRequired);

    boolean isSignatureRequired();

    boolean shouldPrintClearISOPacket();

    boolean shouldPrintEncryptedISOPacket();

    void setPrintClearISOPacket(boolean printClearISOPacket);

    void setPrintEncryptedISOPacket(boolean printEncISOPacket);

    void saveSelectedHostIdForVoid(int id);

    int getSelectedHostIdForVoid();

    void saveSelectedMerchantIdForVoid(int id);

    int getSelectedMerchantIdForVoid();

    void saveSelectedHostIdForPreComp(int id);

    int getSelectedHostIdForPreComp();

    void saveSelectedMerchantIdForPreComp(int id);

    int getSelectedMerchantIdForPreComp();

    String getTraceNumber();

    void incrementTraceNumber();

    void saveCardInitiatedSale(boolean cardInitiatedSale);

    boolean isCardInitiatedSale();

    void saveCardInitiatedSalePinRequested(boolean pinRequested);

    boolean isCardInitiatedSalePinRequested();

    void saveCardInitiatedSaleIsOnlinePin(boolean isOnlinePin);

    boolean isCardInitiatedSaleOnlinePin();

    void saveTransactionOngoing(boolean isTxnOngoing);

    boolean isTransactionOngoing();

    void saveStartWithAmountActivity(boolean startWithAmount);

    boolean isStartWithAmountActivity();

    void saveForceLoadHome(boolean forceLoadHome);

    boolean isForceLoadHome();

    void saveCardInitiatedSaleWithChip(boolean isChipSale);

    boolean isCardInitiatedSaleWithChip();

    void saveCheckRemoveCard(boolean removeCad);

    boolean isCheckRemoveCard();

    void saveHasPendingAutoSettlement(boolean hasAutoSettlement);

    boolean hasPendingAutoSettlement();

    void saveHasPendingProfileUpdate(boolean hasProfileUpdate);

    boolean hasPendingProfileUpdate();

    void saveProfileUpdateData(String profileUpdateData);

    String getProfileUpdateData();

    void saveShouldDisableTerminal(boolean shouldDisableTerminal);

    boolean isTerminalDisabled();

    void setQuasiCashManualFlow(boolean quasiCashManual);

    boolean isQuasiCashManualFlow();

    void setQuasiCashFlow(boolean quasiCash);

    boolean isQuasiCashFlow();

    void setCashAdvance(boolean cashAdvance);

    boolean isCashAdvance();

    void setOnlinePinRequested(boolean isOnlinePinRequested);

    boolean isOnlinePinRequested();

    void setEcrInitiatedSale(boolean ecrInitiatedSale);

    boolean isEcrInitiatedSale();

    void saveConfigMapGenerated(boolean isGenerated);

    boolean isConfigMapGenerated();

    void setLogEnabled(boolean isLogEnabled);

    boolean isLogEnabled();

    //Database
    void getAllCDTs(DbHandler.GetAllCDTListener listener);

    void getCdtListByPan(String pan, DbHandler.GetCDTByPanListener listener);

    void getIssuerById(int issuerId, DbHandler.GetIssuerByIdListener listener);

    void getAidByIssuer(int issuerId, DbHandler.AidListener listener);

    void getIssuerContainsHost(int issuerId, DbHandler.GetIssuerListener listener);

    void getHostByHostId(int hostId, GetHostByHostIdListener listener);

    void updateMustSettleFlagByHostId(int hostId, int mustSettleFlag, UpdateMustSettleFlagByHostIdListener listener);

    void getTerminals(GetTerminalEntityListener listener);

    void getMerchantById(int id, DbHandler.GetMerchantListener listener);

    void getEnableMerchantById(int id, DbHandler.GetMerchantListener listener);

    void getEnabledMerchants(boolean hasInstallmentSupport, DbHandler.GetMerchantListListener listener);

    void getEnabledMerchantsByHost(int HostId, DbHandler.GetMerchantListListener listener);

    void getSaleSupportMerchantsByHost(int HostId, DbHandler.GetMerchantListListener listener);

    void getEnabledMerchantsFromGroupId(int groupId, DbHandler.GetMerchantListListener listener);

    void updateMerchant(Merchant merchant, DbHandler.UpdateListener listener);

    void getCurrencyByMerchantId(int merchantId, DbHandler.GetCurrencyListener listener);

    void updateCurrency(Currency currency, DbHandler.UpdateListener listener);

    void insertReversal(Reversal reversal, DbHandler.InsertListener listener);

    void savedccdata(DCCData dccData, DbHandler.InsertListener listener);

    void getcursymbycode(int ccode,DbHandler.getcursymbycode listener);

    void deleteReversal(Reversal reversal, DbHandler.DeleteListener listener);

    void getReversals(DbHandler.GetReversalsListener listener);

    void getReversalsByHost(int host, DbHandler.GetReversalsListener listener);

    void insertTransaction(Transaction transaction, DbHandler.InsertListener listener);

    void updateTransaction(Transaction transaction, DbHandler.UpdateListener listener);

    void getTransactionById(int id, DbHandler.GetTransactionListener listener);

    void deleteAllTransactions(DbHandler.DeleteListener listener);

    void deleteAllReversals(DbHandler.DeleteListener listener);

    void insertAllTransactions(List<Transaction> transactions, DbHandler.InsertListener listener);

    void insertAllReversals(List<Reversal> reversals, DbHandler.InsertListener listener);

    void getAllTransactions(DbHandler.GetTransactionsListener listener);

    void getLastTransaction(DbHandler.GetTransactionListener listener);

    void getTransaction(int hostId, int merchantNo, String invoiceNo, DbHandler.GetTransactionListener listener);

    void getTransactionByMerchantAndHost(String merchantId, int hostId, GetTransactionByInvoiceNoListener listener);

    void deleteTransactionByMerchantAndHostAndTxnCodes(String merchantId, int hostId, List<Integer> txnCodeList, DeleteTransactionByInvoiceNoListener listener);

    void deleteTransactions(int merchantNo, int hostId, DbHandler.DeleteListener listener);

    void getTCT(DbHandler.GetTCTListener listener);

    void updateAutoSettleDate(String autoSettleDate, DbHandler.UpdateTctListener listener);

    void getHostList(GetHostListener listener);

    void getMerchantList(GetMerchantListListener listener);

    void getMerchantsByMerchantNumbers(List<Integer> merchantNumbers, GetMerchantListListener listener);

    ConfigMapEntity getConfigData(String paramName);

    String getTablePrimaryColumn(String tableName);

    void getTableColumns(String tableName, GetTableColumnsListener listener);

    void getTableColumnInfo(String tableName, DbHandler.GetTableColumnInfoListener listener);

    void updateTableInfo(String tableName, List<ColumnInfoEntity> entities, DbHandler.UpdateTableInfoListener listener);

    void getTableDataCursor(String tableName, int rowIndex, DbHandler.GetTableDataCursorListener listener);

    void  checkdccbin(DbHandler.CheckDCCBINListener listener,String pan);

    void  insertdccbinlist(DbHandler.UPDATEDCCBINListener listener,  List <DCCBINNLIST>  values);


    void getTMIFByHostAndMerchant(int hostID, int merchantID, GetTMIFByHostAndMerchantListener listener);

    void getIssuerContainsHostRaw(int issuerNo, DbHandler.GetIssuerHostListener listener);

    void getAllIssuers(DbHandler.GetIssuersListener listener);

    void getTerminalsByHost(int hostID, DbHandler.GetTerminalListener listener);

    void getAllTerminalsByHost(int hostID, DbHandler.GetTerminalsListener listener);

    void updateTerminal(Terminal terminal, DbHandler.UpdateListener listener);

    void updateBatchIdByMerchantId(int merchantId, String batchNumber, UpdateBatchIdByMerchantIdListener listener);

    public long updateProfileData(String tableName,
                                  String columnName,
                                  String value,
                                  String primaryColName,
                                  String rowID);

    void deleteConfigMapByTableName(String tableName);

    void  getuuserbyusernameandpassword(String uname,String password,DbHandler.GetTableDataCursorListener listener);

    void  getdccdataauronet(DbHandler.GetDCCDataCursorListener listener,String ccode);

    void insertConfigMap(String parameterName, String tableName, String columnName, int rowIndex, String value);

    Cursor getTableData(String tableName);

    void getTLEData(int issuerNo, DbHandler.GetTLEListener listener);

    void saveTLEPwd(String pwd);

    String getTLEPwd();

    void getAllFeatures(DbHandler.GetFeaturesListener listener);

    void getFeature(int id, DbHandler.GetFeatureListener listener);

    void getTransactionCount(DbHandler.GetCountListener listener);

    void getTransactionCountByMerchant(int merchantNo, DbHandler.GetCountListener listener);

    void updateAutoSettleDate(String nextSettlementDate);

    boolean isAutoSettlementCompleted();

    void setAutoSettlementCompleted(boolean autoSettlementCompleted);

    boolean isTranSyncEnable();

    void setTranSyncEnable(boolean tranSyncEnable);

    public interface GetHostByHostIdListener {
        void onReceived(Host host);
    }

    public interface UpdateMustSettleFlagByHostIdListener {
        void onReceived();
    }

    public interface GetTransactionByInvoiceNoListener {
        void onReceived(ArrayList<Transaction> transactions);
    }

    public interface DeleteTransactionByInvoiceNoListener {
        void onReceived();
    }

    public interface GetHostListener {
        void onReceived(List<Host> hosts);
    }

    public interface GetMerchantListListener {
        void onReceived(List<Merchant> merchantEntityList);
    }

    public interface GetTerminalByHostListener {
        void onReceived(Terminal terminal);
    }

    public interface GetTMIFByHostAndMerchantListener {
        void onReceived(Terminal terminalId);
    }

    public interface UpdateBatchIdByMerchantIdListener {
        void onReceived();
    }

    interface GetTerminalEntityListener {
        void onReceived(List<TerminalEntity> entities);
    }

    interface GetAllTerminalData {
        void onReceived(String data);
    }

    interface SaleTransactionListener {
        void onReceived(SaleResponse saleResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }

    interface ReversalTransactionListener {
        void onReceived(ReversalResponse reversalResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }

    interface QrRequestListener {
        void onReceived(QrResponse qrResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }

    interface VoidRequestListener {
        void onReceived(VoidResponse voidResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }

    interface SettlementRequestListener {
        void onReceived(SettlementResponse settlementResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }


    interface BatchUploadRequestListener {
        void onReceived(BatchUploadResponse batchUploadResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }

    interface DCCRequestListener {
        void onReceived(DCCResponse batchUploadResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }
    interface KeyDownloadListener {
        void onReceived(KeyDownloadResponse keyDownloadResponse);

        void onError(String error);

        void onError(Throwable throwable);

        void onCompleted();
    }

    interface DCCDownloadRequestListener {
        void onReceived(DCCDownloadResponse dccresponce);

        void onError(String error);

        void onError(Throwable throwable);

        void onCompleted();
    }

    interface OfflineSaleListener {
        void onReceived(OfflineSaleResponse offlineSaleResponse);

        void onError(Throwable throwable);

        void onCompleted();

        void TLEError(String error);
    }

    interface LoginCallback {
        void onSuccessful(ServiceResponse<LoginResponse> serviceResponse);

        void onFailed(ErrorResponse errorResponse);

        void onFailed(String message);

        void onCompleted();
    }

    void login(LoginRequest loginRequest, Repository.LoginCallback loginCallback);

    public interface GetTableColumnsListener {
        void onReceived(List<String> columns);
    }

    void saveHasPendingApplicationUpdate(boolean b);
}
