package com.epic.pos.data.repository;

import android.database.Cursor;

import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.data.db.dccdb.model.DCCData;
import com.epic.pos.iso.modal.request.DCCDownloadTransaction;
import com.epic.pos.iso.modal.request.DCCRequest;
import com.epic.pos.util.AppLog;

import com.epic.pos.data.datasource.AppDataSource;
import com.epic.pos.data.datasource.IsoDataSource;
import com.epic.pos.data.datasource.IsoDataSourceImpl;
import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.ConfigMap;
import com.epic.pos.data.db.dbpos.modal.Currency;
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
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.iso.ISOMsgBuilder;
import com.epic.pos.iso.modal.request.BatchUploadRequest;
import com.epic.pos.iso.modal.request.KeyDownloadTransaction;
import com.epic.pos.iso.modal.request.OfflineSaleRequest;
import com.epic.pos.iso.modal.request.QrRequest;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.SaleRequest;
import com.epic.pos.iso.modal.request.SettlementRequest;
import com.epic.pos.iso.modal.request.VoidRequest;
import com.epic.pos.iso.modal.response.SaleResponse;
import com.epic.pos.tle.TLE;
import com.epic.pos.tle.TLEData;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;

import org.jpos.iso.ISOMsg;

import java.util.ArrayList;
import java.util.List;

public class RepositoryImpl implements Repository {

    private final String TAG = RepositoryImpl.class.getSimpleName();
    private AppDataSource appDataSource;
    private SharedPref sharedPref;
    private IsoDataSource isoDataSource;
    private DbHandler localDataSource;

    public RepositoryImpl(AppDataSource appDataSource,
                          SharedPref sharedPref,
                          IsoDataSourceImpl isoDataSource,
                          DbHandler localDataSource) {
        this.appDataSource = appDataSource;
        this.sharedPref = sharedPref;
        this.isoDataSource = isoDataSource;
        this.localDataSource = localDataSource;
    }

    //ISO
    @Override
    public void saleRequest(Issuer issuer, SaleRequest saleRequest, TLEData tleData, SaleTransactionListener listener) {
        try {
            ISOMsgBuilder.getInstance().saleISOMessage(saleRequest, tleData, new TLE.GetEncryptedFiled() {
                @Override
                public void onReceived(String isoMessage) {
                    AppLog.i(TAG, "SALE_REQ: " + isoMessage);
                    isoDataSource.saleRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.SaleTransactionListener() {
                        @Override
                        public void onReceived(ISOMsg reversalResObj) {
                            listener.onReceived(ISOMsgBuilder.getInstance().getSaleTransactionResponseEntity(reversalResObj));
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            listener.onError(throwable);
                        }

                        @Override
                        public void onTLEError(String errorMsg) {
                            listener.TLEError(errorMsg);
                        }

                        @Override
                        public void onCompleted() {
                            listener.onCompleted();
                        }
                    });
                }

                @Override
                public void onTLEError(String errorMsg) {
                    listener.TLEError(errorMsg);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            listener.onError(ex);
        }
    }

    @Override
    public void reversalRequest(Issuer issuer, ReversalRequest reversalRequest, TLEData tleData, ReversalTransactionListener listener) {
        ISOMsgBuilder.getInstance().reversalISOMessage(reversalRequest, tleData, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String isoMessage) {
                AppLog.i(TAG, "REVERSAL_REQ: " + isoMessage);
                isoDataSource.reversalRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.ReversalTransactionListener() {
                    @Override
                    public void onReceived(ISOMsg reversalResObj) {
                        listener.onReceived(ISOMsgBuilder.getInstance().getReversalTransactionResponseEntity(reversalResObj));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    @Override
                    public void onTLEError(String errorMsg) {
                        listener.TLEError(errorMsg);
                    }

                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }
        });
    }

    @Override
    public void voidRequest(Issuer issuer, VoidRequest voidRequest, TLEData tleData, VoidRequestListener listener) {
        ISOMsgBuilder.getInstance().voidISOMessage(voidRequest, tleData, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String isoMessage) {
                AppLog.i(TAG, "VOID_REQ: " + isoMessage);
                isoDataSource.voidRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.VoidTransactionListener() {
                    @Override
                    public void onReceived(ISOMsg voidResObj) {
                        listener.onReceived(ISOMsgBuilder.getInstance().getVoidTransactionResponseEntity(voidResObj));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    @Override
                    public void onTLEError(String errorMsg) {
                        listener.TLEError(errorMsg);
                    }

                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }
        });
    }

    @Override
    public void qrRequest(String ip, String port, QrRequest qrRequest, QrRequestListener listener) {
        String isoMessage = ISOMsgBuilder.getInstance().qrRequestIsoMessage(qrRequest);
        AppLog.i(TAG, "QR_REQ: " + isoMessage);

        isoDataSource.qrRequest(ip, port, isoMessage, new IsoDataSource.QrRequestTransactionListener() {
            @Override
            public void onReceived(ISOMsg qrResObj) {
                listener.onReceived(ISOMsgBuilder.getInstance().getQrRequestResponseEntity(qrResObj));
            }

            @Override
            public void onError(Throwable throwable) {
                listener.onError(throwable);
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }

            @Override
            public void onCompleted() {
                listener.onCompleted();
            }
        });
    }

    @Override
    public void settlementRequest(Issuer issuer, SettlementRequest settlementRequest, TLEData tleData, SettlementRequestListener listener) {
        ISOMsgBuilder.getInstance().settlementISOMessage(settlementRequest, tleData, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String isoMessage) {
                AppLog.i(TAG, "SETTLEMENT_REQ: " + isoMessage);
                AppLog.i(TAG, "onReceived: IP 2 : " + issuer.getIP() + "| Issuer Name : " + issuer.getIssuerLable());
                isoDataSource.settlementRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.SettlementTransactionListener() {
                    @Override
                    public void onReceived(ISOMsg settlementResObj) {
                        listener.onReceived(ISOMsgBuilder.getInstance().getSettlementTransactionResponseEntity(settlementResObj));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    @Override
                    public void onTLEError(String errorMsg) {
                        listener.TLEError(errorMsg);
                    }

                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }
        });
    }

    @Override
    public void batchUploadRequest(Issuer issuer, BatchUploadRequest batchUploadRequest, TLEData tleData, BatchUploadRequestListener listener) {
        ISOMsgBuilder.getInstance().batchUploadISOMessage(batchUploadRequest, tleData, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String isoMessage) {
                AppLog.i(TAG, "BATCH_UPLOAD_REQ: " + isoMessage);
                isoDataSource.batchUploadRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.BatchUploadTransactionListener() {
                    @Override
                    public void onReceived(ISOMsg batchUploadResObj) {
                        listener.onReceived(ISOMsgBuilder.getInstance().getBatchUploadResponseEntity(batchUploadResObj));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    @Override
                    public void onTLEError(String errorMsg) {
                        listener.TLEError(errorMsg);
                    }

                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }
        });
    }

    @Override
    public void DCCRequest(Issuer issuer, DCCRequest dccRequest, TLEData tleData, DCCRequestListener listener) {
        ISOMsgBuilder.getInstance().DccISOMessage(dccRequest, tleData, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String isoMessage) {
                AppLog.i(TAG, "DCC_REQ: " + isoMessage);
                isoDataSource.DCCRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.DCCTransactionListener() {
                    @Override
                    public void onReceived(ISOMsg dccResObj) {
                        listener.onReceived(ISOMsgBuilder.getInstance().getdccResponseEntity(dccResObj));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    @Override
                    public void onTLEError(String errorMsg) {
                        listener.TLEError(errorMsg);
                    }

                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }
        });
    }

    @Override
    public void keyDownloadRequest(Issuer issuer, KeyDownloadTransaction keyDownloadTransaction, KeyDownloadListener listener) {
        String isoMessage = ISOMsgBuilder.getInstance().keyDownloadISOMessage(keyDownloadTransaction);
        isoDataSource.keyDownloadRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.KeyDownloadListener() {
            @Override
            public void onReceived(ISOMsg keyResObj) {
                listener.onReceived(ISOMsgBuilder.getInstance().getKeyDownloadTransactionResponseEntity(keyResObj));
            }

            @Override
            public void onError(Throwable throwable) {
                listener.onError(throwable);
            }

            @Override
            public void onCompleted() {
                listener.onCompleted();
            }
        });
        AppLog.i(TAG, "MSG: " + isoMessage);
    }
    @Override
    public void  DCCDataDownloadRequest(Issuer issuer, Merchant merchants,Terminal terminal, DCCDownloadRequestListener listener){

        DCCDownloadTransaction dcctran = new DCCDownloadTransaction();
        getTCT(tct -> {
            dcctran.setNii(tct.getDCCNII());
        });
        dcctran.setTraceNumber("000000");
        dcctran.setPosConditionCode("00");
       // dcctran.setTpdu("6008020000");
        dcctran.setProcessingCode("970000");
        dcctran.setTid(terminal.getTerminalID());
        dcctran.setMid(merchants.getMerchantID());


        String isoMessage = ISOMsgBuilder.getInstance().DCCDownloadISOMessage(dcctran);
        isoDataSource.DCCDownloadRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.DCCDownloadTransactionListener() {
            @Override
            public void onReceived(ISOMsg dccResObj) {
                listener.onReceived(ISOMsgBuilder.getInstance().getDCCDownloadResponce(dccResObj));
            }
            @Override
            public void onError(Throwable throwable) {
                listener.onError(throwable);
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.onError(errorMsg);
            }

            @Override
            public void onCompleted() {
                listener.onCompleted();
            }
        });

    }
    @Override
    public void offlineSaleRequest(Issuer issuer, OfflineSaleRequest offlineSaleRequest, TLEData tleData, OfflineSaleListener listener) {
        ISOMsgBuilder.getInstance().offlineSaleISOMessage(offlineSaleRequest, tleData, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String isoMessage) {
                AppLog.i(TAG, "OFFLINE_SALE_REQ: " + isoMessage);
                isoDataSource.batchUploadRequest(issuer.getIP(), String.valueOf(issuer.getPort()), isoMessage, new IsoDataSource.BatchUploadTransactionListener() {
                    @Override
                    public void onReceived(ISOMsg batchUploadResObj) {
                        listener.onReceived(ISOMsgBuilder.getInstance().getOfflineSaleResponseEntity(batchUploadResObj));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    @Override
                    public void onTLEError(String errorMsg) {
                        listener.TLEError(errorMsg);
                    }

                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.TLEError(errorMsg);
            }
        });
    }


    //Shared Pref
    @Override
    public void saveTotalAmount(String amount) {
        sharedPref.saveTotalAmount(amount);
    }

    @Override
    public String getTotalAmount() {
        return sharedPref.getTotalAmount();
    }

    @Override
    public void saveBaseAmount(String amount) {
        sharedPref.saveBaseAmount(amount);
    }

    @Override
    public String getBaseAmount() {
        return sharedPref.getBaseAmount();
    }

    @Override
    public void saveCashBackAmount(String amount) {
        sharedPref.saveCashbackAmount(amount);
    }

    @Override
    public String getCashBackAmount() {
        return sharedPref.getCashBackAmount();
    }

    @Override
    public void saveCardAction(CardAction cardAction) {
        sharedPref.saveCardAction(cardAction);
    }

    @Override
    public CardAction getCardAction() {
        return sharedPref.getCardAction();
    }

    @Override
    public void saveCardData(CardData cardData) {
        sharedPref.saveCardData(cardData);
    }

    @Override
    public CardData getCardData() {
        return sharedPref.getCardData();
    }

    @Override
    public void saveSelectedCardDefinitionId(int id) {
        sharedPref.saveSelectedCardDefinitionId(id);
    }

    @Override
    public int getSelectedCardDefinitionId() {
        return sharedPref.getSelectedCardDefinitionId();
    }

    @Override
    public void saveSaleResponse(SaleResponse saleResponse) {
        sharedPref.saveSaleResponse(saleResponse);
    }

    @Override
    public SaleResponse getSaleResponse() {
        return sharedPref.getSaleResponse();
    }

    @Override
    public void saveSelectedTerminalId(int terminalId) {
        sharedPref.saveSelectedTerminalId(terminalId);
    }

    @Override
    public int getSelectedTerminalId() {
        return sharedPref.getSelectedTerminalId();
    }

    @Override
    public void saveCurrentSaleId(int id) {
        sharedPref.saveCurrentSaleId(id);
    }

    @Override
    public int getCurrentSaleId() {
        return sharedPref.getCurrentSaleId();
    }

    @Override
    public void setIsCardPinEntered(boolean isCardPinEntered) {
        sharedPref.setIsCardPinEntered(isCardPinEntered);
    }

    @Override
    public boolean isCardPinEntered() {
        return sharedPref.isCardPinEntered();
    }

    @Override
    public void saveCurrentVoidSaleId(int id) {
        sharedPref.saveCurrentVoidSaleId(id);
    }

    @Override
    public int getCurrentVoidSaleId() {
        return sharedPref.getCurrentVoidSaleId();
    }

    @Override
    public void setManualSale(boolean isManualSale) {
        sharedPref.setManualSale(isManualSale);
    }

    @Override
    public void setOfflineSale(boolean isOfflineSale) {
        sharedPref.setOfflineSale(isOfflineSale);
    }

    @Override
    public void setOfflineManualSale(boolean isOfflineManualSale) {
        sharedPref.setOfflineManualSale(isOfflineManualSale);
    }

    @Override
    public boolean isManualSale() {
        return sharedPref.isManualSale();
    }

    @Override
    public boolean isOfflineSale() {
        return sharedPref.isOfflineSale();
    }

    @Override
    public boolean isOfflineManualSale() {
        return sharedPref.isOfflineManualSale();
    }

    @Override
    public void saveOfflineApprovalCode(String approvalCode) {
        sharedPref.saveOfflineApprovalCode(approvalCode);
    }

    @Override
    public String getOfflineApprovalCode() {
        return sharedPref.getOfflineApprovalCode();
    }

    @Override
    public void setVoidSale(boolean isVoidSale) {
        sharedPref.setVoidSale(isVoidSale);
    }

    @Override
    public boolean isVoidSale() {
        return sharedPref.isVoidSale();
    }

    @Override
    public void setQrSale(boolean isQrSale) {
        sharedPref.setQrSale(isQrSale);
    }

    @Override
    public boolean isQrSale() {
        return sharedPref.isQrSale();
    }

    @Override
    public void setPreAuthSale(boolean isPreAuth) {
        sharedPref.setPreAuthSale(isPreAuth);
    }
    @Override
    public void setAuthOnlySale(boolean isuthonly) {
        sharedPref.setAuthOnlySale(isuthonly);
    }
    @Override
    public boolean isPreAuthSale() {
        return sharedPref.isPreAuthSale();
    }
    @Override
    public boolean isAuthOnlySale() {
        return sharedPref.isAuthOnlySale();
    }
    @Override
    public void setPreAuthManualSale(boolean isPreAuthManual) {
        sharedPref.setPreAuthManualSale(isPreAuthManual);
    }

    @Override
    public boolean isPreAuthManualSale() {
        return sharedPref.isPreAuthManualSale();
    }

    @Override
    public void setSignatureRequired(boolean isRequired) {
        sharedPref.setSignatureRequired(isRequired);
    }

    @Override
    public boolean isSignatureRequired() {
        return sharedPref.isSignatureRequired();
    }

    @Override
    public boolean shouldPrintClearISOPacket() {
        return sharedPref.isPrintClearISOPacket();
    }

    @Override
    public boolean shouldPrintEncryptedISOPacket() {
        return sharedPref.isPrintEncISOPacket();
    }

    @Override
    public void setPrintClearISOPacket(boolean printClearISOPacket) {
        sharedPref.setPrintClearISOPacket(printClearISOPacket);
    }

    @Override
    public void setPrintEncryptedISOPacket(boolean printEncISOPacket) {
        sharedPref.setPrintEncISOPacket(printEncISOPacket);
    }

    @Override
    public void setInstallmentSale(boolean isInstallment) {
        sharedPref.setInstallmentSale(isInstallment);
    }

    @Override
    public boolean isInstallmentSale() {
        return sharedPref.isInstallmentSale();
    }

    @Override
    public void setPreCompSale(boolean isPreCompSale) {
        sharedPref.setPreCompSale(isPreCompSale);
    }

    @Override
    public boolean isPreCompSale() {
        return sharedPref.isPreCompSale();
    }

    @Override
    public void setRefundSale(boolean isRefund) {
        sharedPref.setRefundSaleFlow(isRefund);
    }

    @Override
    public boolean isRefundSale() {
        return sharedPref.isRefundSale();
    }

    @Override
    public void setRefundManualSale(boolean isManualRefund) {
        sharedPref.setManualRefundSaleFlow(isManualRefund);
    }

    @Override
    public boolean isRefundManualSale() {
        return sharedPref.isManualRefundSale();
    }

    @Override
    public void setCashBackSale(boolean cashBackSale) {
        sharedPref.saveCashBackSaleFlow(cashBackSale);
    }

    @Override
    public boolean isCashBackSale() {
        return sharedPref.isCashBackSaleFlow();
    }

    @Override
    public int getCurrentPreCompSaleId() {
        return sharedPref.getCurrentPreCompId();
    }

    @Override
    public void saveCurrentPreCompSaleId(int id) {
        sharedPref.saveCurrentPreCompId(id);
    }

    @Override
    public void saveSelectedHostIdForVoid(int id) {
        sharedPref.saveSelectedHostIdForVoid(id);
    }

    @Override
    public int getSelectedHostIdForVoid() {
        return sharedPref.getSelectedHostIdForVoid();
    }

    @Override
    public void saveSelectedMerchantIdForVoid(int id) {
        sharedPref.saveSelectedMerchantIdForVoid(id);
    }

    @Override
    public int getSelectedMerchantIdForVoid() {
        return sharedPref.getSelectedMerchantIdForVoid();
    }

    @Override
    public void saveSelectedHostIdForPreComp(int id) {
        sharedPref.saveSelectedHostIdForPreComp(id);
    }

    @Override
    public int getSelectedHostIdForPreComp() {
        return sharedPref.getSelectedHostIdForPreComp();
    }

    @Override
    public void saveSelectedMerchantIdForPreComp(int id) {
        sharedPref.saveSelectedMerchantIdForPreComp(id);
    }

    @Override
    public int getSelectedMerchantIdForPreComp() {
        return sharedPref.getSelectedMerchantIdForPreComp();
    }

    @Override
    public void saveSelectedMerchantGroupId(int id) {
        sharedPref.saveSelectedMerchantGroupId(id);
    }

    @Override
    public void saveTLEPwd(String pwd) {
        sharedPref.saveTLEPassword(pwd);
    }

    @Override
    public String getTLEPwd() {
        return sharedPref.getTLEPassword();
    }

    @Override
    public void incrementTraceNumber() {
        sharedPref.incrementTraceNumber();
    }

    @Override
    public String getTraceNumber() {
        return sharedPref.getTraceNumber();
    }

    @Override
    public int getSelectedMerchantGroupId() {
        return sharedPref.getSelectedMerchantGroupId();
    }

    @Override
    public void saveCardInitiatedSale(boolean cardInitiatedSale) {
        sharedPref.saveCardInitiatedSale(cardInitiatedSale);
    }

    @Override
    public boolean isCardInitiatedSale() {
        return sharedPref.isCardInitiatedSale();
    }

    @Override
    public void saveCardInitiatedSalePinRequested(boolean pinRequested) {
        sharedPref.saveCardInitiatedSalePinRequested(pinRequested);
    }

    @Override
    public boolean isCardInitiatedSalePinRequested() {
        return sharedPref.isCardInitiatedSalePinRequested();
    }

    @Override
    public void saveCardInitiatedSaleIsOnlinePin(boolean isOnlinePin) {
        sharedPref.saveCardInitiatedSaleIsOnlinePin(isOnlinePin);
    }

    @Override
    public boolean isCardInitiatedSaleOnlinePin() {
        return sharedPref.isCardInitiatedSaleOnlinePin();
    }

    @Override
    public void saveTransactionOngoing(boolean isTxnOngoing) {
        sharedPref.saveTransactionOngoing(isTxnOngoing);
    }

    @Override
    public boolean isTransactionOngoing() {
        return sharedPref.isTransactionOngoing();
    }

    @Override
    public void saveStartWithAmountActivity(boolean startWithAmount) {
        sharedPref.saveStartWithAmountActivity(startWithAmount);
    }

    @Override
    public boolean isStartWithAmountActivity() {
        return sharedPref.isStartWithAmountActivity();
    }

    @Override
    public void saveForceLoadHome(boolean forceLoadHome) {
        sharedPref.saveForceLoadHome(forceLoadHome);
    }

    @Override
    public boolean isForceLoadHome() {
        return sharedPref.isForceLoadHome();
    }

    @Override
    public void saveCardInitiatedSaleWithChip(boolean isChipSale) {
        sharedPref.saveCardInitiatedSaleWithChip(isChipSale);
    }

    @Override
    public boolean isCardInitiatedSaleWithChip() {
        return sharedPref.isCardInitiatedSaleWithChip();
    }

    @Override
    public void saveCheckRemoveCard(boolean removeCad) {
        sharedPref.saveCheckRemoveCard(removeCad);
    }

    @Override
    public boolean isCheckRemoveCard() {
        return sharedPref.isCheckRemoveCard();
    }

    @Override
    public void saveHasPendingAutoSettlement(boolean hasAutoSettlement) {
        sharedPref.saveHasPendingAutoSettlement(hasAutoSettlement);
    }

    @Override
    public boolean hasPendingAutoSettlement() {
        return sharedPref.hasPendingAutoSettlement();
    }

    @Override
    public void saveHasPendingProfileUpdate(boolean hasProfileUpdate) {
        sharedPref.saveHasPendingProfileUpdate(hasProfileUpdate);
    }

    @Override
    public boolean hasPendingProfileUpdate() {
        return sharedPref.hasPendingProfileUpdate();
    }

    @Override
    public void saveProfileUpdateData(String profileUpdateData) {
        sharedPref.saveProfileUpdateData(profileUpdateData);
    }

    @Override
    public String getProfileUpdateData() {
        return sharedPref.getProfileUpdateData();
    }

    @Override
    public void saveShouldDisableTerminal(boolean shouldDisableTerminal) {
        sharedPref.saveShouldDisableTerminal(shouldDisableTerminal);
    }

    @Override
    public boolean isTerminalDisabled() {
        return sharedPref.isTerminalDisabled();
    }

    @Override
    public void setQuasiCashManualFlow(boolean quasiCashManual) {
        sharedPref.saveQuasiCashManualFlow(quasiCashManual);
    }

    @Override
    public void setLogEnabled(boolean isLogEnabled) {
        sharedPref.setLogEnable(isLogEnabled);
    }

    @Override
    public boolean isLogEnabled() {
        return sharedPref.isLogEnabled();
    }

    @Override
    public boolean isQuasiCashManualFlow() {
        return sharedPref.isQuasiCashManualFlow();
    }

    @Override
    public void setQuasiCashFlow(boolean quasiCash) {
        sharedPref.saveQuasiCashFlow(quasiCash);
    }

    @Override
    public boolean isQuasiCashFlow() {
        return sharedPref.isQuasiCashFlow();
    }

    @Override
    public void setCashAdvance(boolean cashAdvance) {
        sharedPref.saveCashAdvance(cashAdvance);
    }

    @Override
    public boolean isCashAdvance() {
        return sharedPref.isCashAdvance();
    }

    @Override
    public void setOnlinePinRequested(boolean isOnlinePinRequested) {
        sharedPref.setOnlinePinRequested(isOnlinePinRequested);
    }

    @Override
    public boolean isOnlinePinRequested() {
        return sharedPref.isOnlinePinRequested();
    }

    @Override
    public void setEcrInitiatedSale(boolean ecrInitiatedSale) {
        sharedPref.setEcrInitiatedSale(ecrInitiatedSale);
    }

    @Override
    public boolean isEcrInitiatedSale() {
        return sharedPref.isEcrInitiatedSale();
    }

    @Override
    public void saveConfigMapGenerated(boolean isGenerated) {
        sharedPref.saveConfigMapGenerated(isGenerated);
    }

    @Override
    public boolean isConfigMapGenerated() {
        return sharedPref.isConfigMapGenerated();
    }

    @Override
    public void setStudentRefSale(boolean isStudentRefSale) {
        sharedPref.setStudentRefSale(isStudentRefSale);
    }

    @Override
    public boolean isStudentRefSale() {
        return sharedPref.isStudentRefSale();
    }

    @Override
    public void saveStudentReferenceNo(String referenceNo) {
        sharedPref.saveStudentReference(referenceNo);
    }

    @Override
    public String getStudentReferenceNo() {
        return sharedPref.getStudentReferenceNo();
    }

    //Database
    @Override
    public void getAllCDTs(DbHandler.GetAllCDTListener listener) {
        DbHandler.getInstance().getAllCDTs(listener);
    }

    @Override
    public void getCdtListByPan(String pan, DbHandler.GetCDTByPanListener listener) {
        DbHandler.getInstance().getCdtByPan(pan, listener);
    }

    @Override
    public void getIssuerById(int issuerId, DbHandler.GetIssuerByIdListener listener) {
        DbHandler.getInstance().getIssuerById(issuerId, listener);
    }

    @Override
    public void getAidByIssuer(int issuerId, DbHandler.AidListener listener) {
        DbHandler.getInstance().getAidByIssuer(issuerId, listener);
    }

    @Override
    public void getIssuerContainsHost(int issuerId, DbHandler.GetIssuerListener listener) {
        DbHandler.getInstance().getIssuerContainsHost(issuerId, listener);
    }

    @Override
    public void getHostByHostId(int hostId, GetHostByHostIdListener listener) {
        localDataSource.getHostByHostId(hostId, listener::onReceived);
    }

    @Override
    public void updateMustSettleFlagByHostId(int hostId, int mustSettleFlag, UpdateMustSettleFlagByHostIdListener listener) {
        localDataSource.updateMustSettleFlagByHostId(hostId, mustSettleFlag, listener::onReceived);
    }

    @Override
    public void getTerminalById(int id, DbHandler.GetTerminalListener listener) {
        DbHandler.getInstance().getTerminalById(id, listener);
    }

    @Override
    public void getTerminalByMerchant(int merchantId, DbHandler.GetTerminalListener listener) {
        DbHandler.getInstance().getTerminalByMerchant(merchantId, listener);
    }

    @Override
    public void getAllTerminalsByHost(int hostID, DbHandler.GetTerminalsListener listener) {
        DbHandler.getInstance().getAllTerminalsByHost(hostID, listener);
    }

    @Override
    public void updateTerminal(Terminal terminal, DbHandler.UpdateListener listener) {
        DbHandler.getInstance().updateTerminal(terminal, listener);
    }

    @Override
    public void getTerminals(GetTerminalEntityListener listener) {
        List<TerminalEntity> ts = new ArrayList<>();

        DbHandler.getInstance().getTerminals(terminals -> {
            for (Terminal t : terminals) {
                ts.add(new TerminalEntity(t));
            }
            listener.onReceived(ts);
        });
    }

    @Override
    public void getCardDefinitionById(int id, DbHandler.GetCardDefinitionListener listener) {
        DbHandler.getInstance().getCardDefinitionById(id, listener);
    }

    @Override
    public void getMerchantById(int id, DbHandler.GetMerchantListener listener) {
        DbHandler.getInstance().getMerchantById(id, listener);
    }

    @Override
    public void getEnableMerchantById(int id, DbHandler.GetMerchantListener listener) {
        DbHandler.getInstance().getEnableMerchantById(id, listener);
    }

    @Override
    public void getEnabledMerchants(boolean hasInstallmentSupport, DbHandler.GetMerchantListListener listener) {
        DbHandler.getInstance().getEnabledMerchants(hasInstallmentSupport, listener);
    }

    @Override
    public void getEnabledMerchantsByHost(int hostId, DbHandler.GetMerchantListListener listener) {
        DbHandler.getInstance().getEnableMerchantsByHost(hostId, listener);
    }

    @Override
    public void getSaleSupportMerchantsByHost(int HostId, DbHandler.GetMerchantListListener listener) {
        DbHandler.getInstance().getSaleSupportMerchantsByHost(HostId, listener);
    }

    @Override
    public void getEnabledMerchantsFromGroupId(int groupId, DbHandler.GetMerchantListListener listener) {
        DbHandler.getInstance().getEnableMerchantsFromGroupId(groupId, listener);
    }

    @Override
    public void updateMerchant(Merchant merchant, DbHandler.UpdateListener listener) {
        DbHandler.getInstance().updateMerchant(merchant, listener);
    }

    @Override
    public void getCurrencyByMerchantId(int merchantId, DbHandler.GetCurrencyListener listener) {
        DbHandler.getInstance().getCurrencyByMerchantId(merchantId, listener);
    }

    @Override
    public void updateCurrency(Currency currency, DbHandler.UpdateListener listener) {
        DbHandler.getInstance().updateCurrency(currency, listener);
    }

    @Override
    public void insertReversal(Reversal reversal, DbHandler.InsertListener listener) {
        DbHandler.getInstance().insertReversal(reversal, listener);
    }
    @Override
    public void savedccdata(DCCData dccData, DbHandler.InsertListener listener) {
        DbHandler.getInstance().insertdccdata(dccData, listener);
    }
    @Override
    public void getcursymbycode(int ccode,DbHandler.getcursymbycode listener) {
        DbHandler.getInstance().getcursymbycode(listener,ccode);
    }
    @Override
    public void deleteReversal(Reversal reversal, DbHandler.DeleteListener listener) {
        DbHandler.getInstance().deleteReversal(reversal, listener);
    }

    @Override
    public void getReversals(DbHandler.GetReversalsListener listener) {
        DbHandler.getInstance().getReversals(listener);
    }

    @Override
    public void getReversalsByHost(int host, DbHandler.GetReversalsListener listener) {
        DbHandler.getInstance().getReversalsByHost(host, listener);
    }

    @Override
    public void insertTransaction(Transaction transaction, DbHandler.InsertListener listener) {
        DbHandler.getInstance().insertTransaction(transaction, listener);
    }

    @Override
    public void updateTransaction(Transaction transaction, DbHandler.UpdateListener listener) {
        DbHandler.getInstance().updateTransaction(transaction, listener);
    }

    @Override
    public void getTransactionById(int id, DbHandler.GetTransactionListener listener) {
        DbHandler.getInstance().getTransactionById(id, listener);
    }

    @Override
    public void getAllTransactions(DbHandler.GetTransactionsListener listener) {
        DbHandler.getInstance().getTransactions(transactions -> listener.onReceived(transactions));
    }

    @Override
    public void deleteAllTransactions(DbHandler.DeleteListener listener) {
        DbHandler.getInstance().deleteAllTransactions(listener);
    }

    @Override
    public void deleteAllReversals(DbHandler.DeleteListener listener) {
        DbHandler.getInstance().deleteAllReversals(listener);
    }

    @Override
    public void insertAllTransactions(List<Transaction> transactions, DbHandler.InsertListener listener) {
        DbHandler.getInstance().insertAllTransactions(transactions, listener);
    }

    @Override
    public void insertAllReversals(List<Reversal> reversals, DbHandler.InsertListener listener) {
        DbHandler.getInstance().insertAllReversals(reversals, listener);
    }

    @Override
    public void getLastTransaction(DbHandler.GetTransactionListener listener) {
        DbHandler.getInstance().getLastTransaction(listener);
    }

    @Override
    public void updateTableInfo(String tableName, List<ColumnInfoEntity> entities, DbHandler.UpdateTableInfoListener listener) {
        DbHandler.getInstance().updateTableInfo(tableName, entities, listener);
    }

    @Override
    public void getTransaction(int hostId, int merchantNo, String invoiceNo, DbHandler.GetTransactionListener listener) {
        DbHandler.getInstance().getTransaction(hostId, merchantNo, invoiceNo, listener);
    }

    @Override
    public void getTransactionByMerchantAndHost(String merchantId, int hostId, GetTransactionByInvoiceNoListener listener) {
        DbHandler.getInstance().getTransactionByMerchantAndHost(merchantId, hostId,
                transactions -> listener.onReceived(new ArrayList<>(transactions)));
    }

    @Override
    public void deleteTransactionByMerchantAndHostAndTxnCodes(String merchantId, int hostId, List<Integer> txnCodeList, DeleteTransactionByInvoiceNoListener listener) {
        DbHandler.getInstance().deleteTransactionByMerchantAndHostAndTxnCodes(merchantId, hostId, txnCodeList, listener::onReceived);
    }

    @Override
    public void deleteTransactions(int merchantNo, int hostId, DbHandler.DeleteListener listener) {
        DbHandler.getInstance().deleteTransactions(merchantNo, hostId, listener);
    }

    @Override
    public void getTCT(DbHandler.GetTCTListener listener) {
        DbHandler.getInstance().getTCT(listener);
    }

    @Override
    public void updateAutoSettleDate(String autoSettleDate, DbHandler.UpdateTctListener listener) {
        DbHandler.getInstance().updateAutoSettleDate(autoSettleDate, listener);
    }

    @Override
    public void getTransactionCount(DbHandler.GetCountListener listener) {
        DbHandler.getInstance().getTransactionCount(listener);
    }

    @Override
    public void getTransactionCountByMerchant(int merchantNo, DbHandler.GetCountListener listener) {
        DbHandler.getInstance().getTransactionCountByMerchant(merchantNo, listener);
    }

    @Override
    public void updateAutoSettleDate(String nextSettlementDate) {

    }


    @Override
    public void getHostList(GetHostListener listener) {
        localDataSource.getHosts(listener::onReceived);
    }

    @Override
    public void getMerchantList(GetMerchantListListener listener) {
        localDataSource.getMerchants(listener::onReceived);
    }


    @Override
    public void getMerchantsByMerchantNumbers(List<Integer> merchantNumbers, GetMerchantListListener listener) {
        localDataSource.getMerchantsByMerchantNumbers(merchantNumbers, listener::onReceived);
    }

    @Override
    public void getTLEData(int issuerNo, DbHandler.GetTLEListener listener) {
        DbHandler.getInstance().getTLEData(issuerNo, listener);
    }

    @Override
    public void getAllFeatures(DbHandler.GetFeaturesListener listener) {
        DbHandler.getInstance().getAllFeatures(listener);
    }

    @Override
    public void getFeature(int id, DbHandler.GetFeatureListener listener) {
        DbHandler.getInstance().getFeature(id, listener);
    }

    @Override
    public ConfigMapEntity getConfigData(String paramName) {
        ConfigMap configMap = localDataSource.getConfigDataByParamName(paramName);
        if (configMap != null) {
            return new ConfigMapEntity(
                    configMap.getTableName(),
                    configMap.getColumnName(),
                    configMap.getRowIndex());
        } else {
            return null;
        }
    }

    @Override
    public String getTablePrimaryColumn(String tableName) {
        return localDataSource.getTablePrimaryColumnName(tableName);
    }

    @Override
    public void getTableColumns(String tableName, GetTableColumnsListener listener) {
        localDataSource.getTableColumns(tableName, listener::onReceived);
    }

    @Override
    public void getTableDataCursor(String tableName, int rowIndex, DbHandler.GetTableDataCursorListener listener) {
        DbHandler.getInstance().getTableDataCursor(tableName, rowIndex, listener);
    }

    @Override
    public void getTableColumnInfo(String tableName, DbHandler.GetTableColumnInfoListener listener) {
        DbHandler.getInstance().getTableColumnInfo(tableName, listener);
    }

    @Override
    public long updateProfileData(
            String tableName,
            String columnName,
            String value,
            String primaryColName,
            String rowID) {
        return localDataSource.updateProfileData(tableName, columnName, value, primaryColName, rowID);
    }

    @Override
    public void deleteConfigMapByTableName(String tableName) {
        localDataSource.deleteConfigMapByTableName(tableName);
    }

    @Override
    public void  getuuserbyusernameandpassword(String uname, String password, DbHandler.GetTableDataCursorListener listener) {

        DbHandler.getInstance().getuserbyusernameandpassword(uname,password,listener);
    }

    @Override
    public void insertConfigMap(String parameterName, String tableName, String columnName, int rowIndex, String value) {
        localDataSource.insertConfigMap(parameterName, tableName, columnName, rowIndex, value);
    }

    @Override
    public Cursor getTableData(String tableName) {
        return localDataSource.getTableData(tableName);
    }
    @Override
    public void  getdccdataauronet(DbHandler.GetDCCDataCursorListener listener,String ccode) {
        DbHandler.getInstance().getDCCDataByCcode(listener,ccode);
    }

    @Override
    public void  checkdccbin(DbHandler.CheckDCCBINListener listener,String pan) {
        DbHandler.getInstance().checkdccbin(listener,pan);
    }

    @Override
    public void  insertdccbinlist(DbHandler.UPDATEDCCBINListener listener,  List <DCCBINNLIST>  values) {
        DbHandler.getInstance().insertdccbinlist(listener , values);
    }
    @Override
    public void getTMIFByHostAndMerchant(int hostID, int merchantID, GetTMIFByHostAndMerchantListener listener) {
        localDataSource.getTMIFByHostAndMerchant(hostID, merchantID, listener::onReceived);
    }

    @Override
    public void getIssuerContainsHostRaw(int issuerNo, DbHandler.GetIssuerHostListener listener) {
        localDataSource.getIssuerContainsHostRaw(issuerNo, listener);
    }

    @Override
    public void getAllIssuers(DbHandler.GetIssuersListener listener) {
        localDataSource.getAllIssuers(listener);
    }

    @Override
    public void getTerminalsByHost(int hostID, DbHandler.GetTerminalListener listener) {
        localDataSource.getTerminalByHost(hostID, listener);
    }


    @Override
    public void updateBatchIdByMerchantId(int merchantId, String batchNumber, UpdateBatchIdByMerchantIdListener listener) {
        localDataSource.updateBatchIdByMerchantId(merchantId, batchNumber, listener::onReceived);
    }

    @Override
    public void login(LoginRequest loginRequest, Repository.LoginCallback loginCallback) {
        appDataSource.login(loginRequest, new AppDataSource.LoginCallback() {
            @Override
            public void onSuccessful(ServiceResponse<LoginResponse> serviceResponse) {
                loginCallback.onSuccessful(serviceResponse);
            }

            @Override
            public void onFailed(ErrorResponse errorResponse) {
                loginCallback.onFailed(errorResponse);
            }

            @Override
            public void onFailed(String message) {
                loginCallback.onFailed(message);
            }

            @Override
            public void onCompleted() {
                loginCallback.onCompleted();
            }
        });
    }

    @Override
    public void saveHasPendingApplicationUpdate(boolean hasProfileUpdate) {
        sharedPref.saveHasPendingApplicationUpdate(hasProfileUpdate);
    }


    @Override
    public boolean isAutoSettlementCompleted() {
        return sharedPref.isAutoSettlementCompleted();
    }

    @Override
    public void setAutoSettlementCompleted(boolean isAutoSettlementCompleted) {
        sharedPref.saveAutoSettlementCompleted(isAutoSettlementCompleted);
    }

    @Override
    public boolean isTranSyncEnable() {
        return sharedPref.isTranSyncEnable();
    }

    @Override
    public void setTranSyncEnable(boolean tranSyncEnable) {
        sharedPref.setIsTranSyncEnable(tranSyncEnable);
    }
}
