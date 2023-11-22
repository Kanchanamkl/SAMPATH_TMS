package com.epic.pos.ui.sale.detail;

import android.os.CountDownTimer;
import android.util.Log;

import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.common.ErrorMsg;
import com.epic.pos.common.TranTypes;
import com.epic.pos.data.db.dbpos.modal.Aid;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.iso.modal.Transaction;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.SaleRequest;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.iso.modal.response.SaleResponse;
import com.epic.pos.tle.TLEData;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CVMResult;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
import com.epic.pos.device.data.CardType;
import com.epic.pos.device.listener.VerifyOnlineProcessListener;
import com.epic.pos.device.serial.EcrReq;

import java.util.Date;

import javax.inject.Inject;

/**
 * TransactionDetailsPresenter
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-04-02
 */
public class TransactionDetailsPresenter extends BasePresenter<TransactionDetailsContract.View> implements TransactionDetailsContract.Presenter {


    private String TAG = "TxnDetailPresenter";
    private Repository repository;
    private NetworkConnection networkConnection;

    public String baseAmount;
    public String totalAmount;
    public String trancurrancy;
    public String cashBackAmount;
    private CardData cardData;
    private String pinBlock;
    private CardAction cardAction;

    private CardDefinition cardDefinition;
    private Issuer issuer;
    private Host host;
    private Aid aid;
    private TCT tct;

    private Terminal terminal;
    private Merchant merchant;
    private Currency currency;

    private Reversal reversal;
    private SaleRequest saleRequest;
    private SaleResponse saleResponse;

    //pending reversal
    private Reversal pendingReversal;
    private ReversalRequest pendingRReq;
    private ReversalResponse pendingRRes;

    private String invoiceNo;
    private String traceNo;

    //for pre completion
    private com.epic.pos.data.db.dbtxn.modal.Transaction preCompTxn;

    private Date transactionDate = null;
    private String errorMsg = "";

    @Inject
    public TransactionDetailsPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    private CountDownTimer homeIdleCountDown = null;
    private boolean isPause = true;

    @Override
    public void onResume() {
        isPause = false;
        startIdleCountDown();
    }

    @Override
    public void onPause() {
        isPause = true;
        cancelIdleCountDown();
    }

    @Override
    public void onUserInteraction() {
        startIdleCountDown();
    }

    private void startIdleCountDown() {
        if (homeIdleCountDown != null) {
            log("cancel idle count down");
            homeIdleCountDown.cancel();
        }

        repository.getTCT(tct -> {
            homeIdleCountDown = new CountDownTimer(
                    1000 * tct.getTxnDetailIdleTimeout(),
                    1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    log("txn detail idle countdown finished");
                    if (isPause) return;
                    if (isViewNotNull()) {
                        mView.finishTxnDetails();
                    }
                }
            };

            homeIdleCountDown.start();
            log("txn detail idle count down started");
        });
    }

    private void cancelIdleCountDown() {
        if (homeIdleCountDown != null) {
            log("txn detail idle count down cancel");
            homeIdleCountDown.cancel();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Init data and update UI">
    @Override
    public void initData() {
        log("initData()");
        PosDevice.getInstance().clearPrintQueue();
        PosDevice.getInstance().startPrinting();

        baseAmount = repository.getBaseAmount();
        totalAmount = repository.getTotalAmount();


        if (repository.isCashBackSale()) {
            cashBackAmount = repository.getCashBackAmount();
        }

        repository.getTCT(tct -> TransactionDetailsPresenter.this.tct = tct);

        if (repository.isPreCompSale()) {
            //pre comp sale
            int saleId = repository.getCurrentPreCompSaleId();
            repository.getTransactionById(saleId, transaction -> {
                TransactionDetailsPresenter.this.preCompTxn = transaction;
                repository.getIssuerById(transaction.getIssuer_number(), issuer -> {
                    TransactionDetailsPresenter.this.issuer = issuer;
                    repository.getAidByIssuer(issuer.getIssuerNumber(), aid -> {
                        TransactionDetailsPresenter.this.aid = aid;
                        repository.getHostByHostId(transaction.getHost(), host -> {
                            TransactionDetailsPresenter.this.host = host;
                            getTerminalRelatedData();
                        });
                    });
                });
            });
        } else {
            //not pre comp sale
            cardData = repository.getCardData();
            cardAction = repository.getCardAction();

            repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
                TransactionDetailsPresenter.this.cardDefinition = cardDefinition;
                repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                    TransactionDetailsPresenter.this.issuer = issuer;
                    repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                        TransactionDetailsPresenter.this.host = host;
                        repository.getAidByIssuer(issuer.getIssuerNumber(), aid -> {
                            TransactionDetailsPresenter.this.aid = aid;
                        });
                    });
                });
            });

            getTerminalRelatedData();
        }
    }

    private void getTerminalRelatedData() {
        int selectedTerminalId = repository.getSelectedTerminalId();
        repository.getTerminalById(selectedTerminalId, terminal -> {
            TransactionDetailsPresenter.this.terminal = terminal;
            repository.getMerchantById(terminal.getMerchantNumber(), merchant -> {
                TransactionDetailsPresenter.this.merchant = merchant;
                repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                    TransactionDetailsPresenter.this.currency = currency;
                    updateUi();
                });
            });
        });
    }

    private void updateUi() {
        log("updateUi()");
        String maskingFormat = issuer.getMaskDisplay();
        String pan = "";
        String cardLabel = "";
        String expireDate = "";

        if (repository.isPreCompSale()) {
            //pre comp sale
            if (preCompTxn.getPan().length() != 16) {
                maskingFormat = Utility.getMaskingFormat(preCompTxn.getPan());
            }

            pan = preCompTxn.getPan();
            cardLabel = preCompTxn.getCard_label();
            expireDate = preCompTxn.getExp_date();
        } else {
            //not pre comp sale
            if (cardData.getPan().length() != 16) {
                maskingFormat = Utility.getMaskingFormat(cardData.getPan());
            }

            pan = cardData.getPan();
            expireDate = cardData.getExpiryDate();
            cardLabel = getCardLabel(cardAction, cardDefinition);
        }

        if (isViewNotNull()) {
            mView.onUpdateUi(currency.getCurrencySymbol(),
                    totalAmount,
                    Utility.maskCardNumber(pan, maskingFormat),
                    cardLabel,
                    Utility.maskCardNumber(expireDate, issuer.getMaskExpireDate()));

            if (tct.getBypassTxnConf() == 1){
                log("Auto bypass transaction confirmation screen.");
                saleRequest();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sale request functions">
    @Override
    public void saleRequest() {
        log("saleRequest()");
        repository.getTransactionCountByMerchant(merchant.getMerchantNumber(), count -> {
            if (count < tct.getMaxTxnCount() || repository.isPreCompSale()) {
                if (repository.isOfflineSale() || repository.isOfflineManualSale() || repository.isPreCompSale()) {
                    log("offline sale or pre comp");
                    cancelIdleCountDown();
                    proceedSaleRequest();
                } else {
                    log("online sale");
                    if (networkConnection.checkNetworkConnection()) {
                        cancelIdleCountDown();
                        log("connection available");
                        log("check reversals");
                        repository.getReversalsByHost(host.getHostID(), reversals -> {
                            if (reversals.size() == 0) {
                                log("No reversals exists");
                                proceedSaleRequest();
                            } else if (reversals.size() == 1) {
                                log("Reversal exists");
                                pendingReversal = reversals.get(0);
                                pendingRReq = createReversalRequest(pendingReversal);
                                sendPendingReversalRequest();
                            }
                        });
                    } else {
                        log(Const.MSG_CHECK_CONNECTION);
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                        }
                    }
                }
            } else {
                log(Const.MSG_MAX_TXN_LIMIT_REACHED);
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_MAX_TXN_LIMIT_REACHED);
                }
            }
        });
    }

    private void proceedSaleRequest() {
        log("proceedSaleRequest()");
        if (isViewNotNull()) {
            mView.setSubmitButtonEnabled(false);
        }

        if (repository.isPreCompSale()) {
            log("pre-comp sale");
            long baseAmount = Long.parseLong(totalAmount.replaceAll(",", "")
                    .replaceAll("\\.", ""));

            preCompTxn.setTransaction_code(TranTypes.SALE_PRE_COMPLETION);
            preCompTxn.setTotal_amount(String.valueOf(baseAmount));
            preCompTxn.setBase_transaction_amount(String.valueOf(baseAmount));

            if (isAmexHost()) {
                preCompTxn.setProcessing_code(Transaction.PRE_COMP_PROCESSING_CODE_AMEX);
            } else {
                preCompTxn.setProcessing_code(Transaction.PRE_COMP_PROCESSING_CODE);
            }

            repository.updateTransaction(preCompTxn, () -> mView.gotoSignaturePad());
        } else {
            log("Not pre comp sale");
            if (isViewNotNull()) {
                mView.showTxnProgress();
            }

            log("Increment invoice number and trace number for next txn");
            merchant.setInvNumber(String.valueOf(Integer.parseInt(merchant.getInvNumber()) + 1));
            merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
            invoiceNo = AppUtil.toInvoiceNumber(Integer.parseInt(merchant.getInvNumber()));
            traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));
            repository.updateMerchant(merchant, null);

            transactionDate = new Date();
            String txnDateString = AppUtil.toTransactionDate(transactionDate);
            String txnTimeString = AppUtil.toTransactionTime(transactionDate);

            saleRequest = new SaleRequest();
            saleRequest.setNii(terminal.getNII());
            saleRequest.setSecureNii(terminal.getSecureNII());
            saleRequest.setTpdu(terminal.getTPDU());
            saleRequest.setInvoiceNumber(invoiceNo);
            saleRequest.setTraceNumber(traceNo);
            saleRequest.setMid(merchant.getMerchantID());
            saleRequest.setTid(terminal.getTerminalID());
            saleRequest.setTxnDate(txnDateString);
            saleRequest.setTxnTime(txnTimeString);
            saleRequest.setOnlinePinRequested(repository.isOnlinePinRequested());

            //add student ref if exists
            if (repository.isStudentRefSale()) {
                saleRequest.setStudentRefNo(repository.getStudentReferenceNo());
            }

            saleRequest.setTotalAmount(totalAmount);
            saleRequest.setBaseAmount(baseAmount);
            saleRequest.setCardAction(cardAction.val);

            if (!repository.isManualSale()
                    && !repository.isOfflineManualSale()
                    && !repository.isPreAuthManualSale()
                    && !repository.isRefundManualSale()
                    && !repository.isQuasiCashManualFlow()) {
                log("Not manual sale");
                saleRequest.setPinBlock(pinBlock);
                saleRequest.setTrack2Data(cardData.getTrack2());
            } else {
                log("Manual sale");
                saleRequest.setPan(cardData.getPan());
                saleRequest.setExpDate(cardData.getExpiryDate());
            }

            log("Set processing code");
            if (repository.isRefundSale() || repository.isRefundManualSale()) {
                log("Refund sale");
                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.REFUND_PROCESSONG_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.REFUND_PROCESSONG_CODE);
                }
            } else if (repository.isPreAuthSale() || repository.isPreAuthManualSale()) {
                log("Pre-Auth sale");
                saleRequest.setMti(Transaction.PRE_AUTH_MTI);
                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.PRE_AUTH_PROCESSING_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.PRE_AUTH_PROCESSING_CODE);
                }
            } else if (repository.isAuthOnlySale()) {
                log("Auth-Only");
                saleRequest.setMti(Transaction.AUTH_ONLY_MTI);
                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.AUTH_ONLY_PROCESSING_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.AUTH_ONLY_PROCESSING_CODE);
                }
            } else if (repository.isCashBackSale()) {
                log("Cash back sale");
                saleRequest.setCashBackAmount(cashBackAmount);

                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.CASH_BACK_PROCESSING_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.CASH_BACK_PROCESSING_CODE);
                }
            } else if (repository.isQuasiCashFlow() || repository.isQuasiCashManualFlow()) {
                log("Quasi cash");
                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.QUASI_CASH_PROCESSING_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.QUASI_CASH_PROCESSING_CODE);
                }
            } else if (repository.isCashAdvance()) {
                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.CASH_ADVANCE_PROCESSING_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.CASH_ADVANCE_PROCESSING_CODE);
                }
            } else {
                log("Sale");
                if (isAmexHost()) {
                    saleRequest.setProcessingCode(Transaction.SALE_PROCESSING_CODE_AMEX);
                } else {
                    saleRequest.setProcessingCode(Transaction.SALE_PROCESSING_CODE);
                }
            }

            if (cardAction == CardAction.SWIPE) {
                if (aid.getPinSupport() == 1) {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_MAG_PIN_SUPPORT);
                } else {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_MAG_PIN_NOT_SUPPORT);
                }

                initiateSale();
            } else if (cardAction == CardAction.INSERT) {
                if (aid.getPinSupport() == 1) {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_INSERT_PIN_SUPPORT);
                } else {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_INSERT_PIN_NOT_SUPPORT);
                }

                getEmvData();
            } else if (cardAction == CardAction.TAP) {
                if (aid.getCTLSPinSupport() == 1) {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_TAP_PIN_SUPPORT);
                } else {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_TAP_PIN_NOT_SUPPORT);
                }
                getEmvData();
            } else if (cardAction == CardAction.MANUAL) {
                if (aid.getPinSupport() == 1) {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_MANUAL_PIN_SUPPORT);
                } else {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_MANUAL_PIN_NOT_SUPPORT);
                }

                initiateSale();
            } else if (cardAction == CardAction.FALLBACK) {
                if (aid.getPinSupport() == 1) {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_FALLBACK_PIN_SUPPORT);
                } else {
                    saleRequest.setPosEntryMode(Transaction.POS_ENTRY_MODE_FALLBACK_PIN_NOT_SUPPORT);
                }

                initiateSale();
            }
            log(saleRequest.toString());
        }
    }

    private boolean isAmexHost() {
        return host.getHostID() == 2;
    }

    private void getEmvData() {
        CardType cardType = CardType.valueOf(cardDefinition.getCardLabel().toUpperCase());
        PosDevice.getInstance().getTLVFromTags(cardType, (tlvData, panSequenceNumber) -> {
            saleRequest.setEmvData(tlvData);
            saleRequest.setPanSequenceNumber(panSequenceNumber);
            if(PosDevice.getInstance().isIsamexmsd()){
            saleRequest.setPosEntryMode("911");}

            initiateSale();
        });
    }


    private void initiateSale() {
        log("initiateSale()");
        if (repository.isOfflineManualSale() || repository.isOfflineSale()) {
            log("Offline sale");
            com.epic.pos.data.db.dbtxn.modal.Transaction t = createTransactionObj();
            repository.insertTransaction(t, saleId -> {
                repository.saveCurrentSaleId((int) saleId);
                if (isViewNotNull()) {
                    mView.gotoSignaturePad();
                }
            });
        } else {
            log("Normal sale");
            log("Insert sale txn to reversal table");
            reversal = createReversalObj();
            repository.insertReversal(reversal, reversalId -> {
                reversal.setId((int) reversalId);
                       sendSaleRequest();
            });
        }
    }

    private void sendSaleRequest() {
        log("sendSaleRequest()");
        TLEData tleData = new TLEData();
        tleData.setChipStatus(Integer.parseInt(saleRequest.getPosEntryMode()));
        tleData.setHostId(host.getHostID());
        tleData.setIssuerId(issuer.getIssuerNumber());
        tleData.setPan(saleRequest.getPan());
        tleData.setTrack2(saleRequest.getTrack2Data());
        tleData.setTleEnable(host.getTLEEnabled() == 1);

        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_PROCESSING, Const.MSG_PROCESSING_DESC);
        }

        log("Send sale request");
        repository.saleRequest(issuer, saleRequest, tleData, new Repository.SaleTransactionListener() {
            @Override
            public void onReceived(SaleResponse saleResponse) {
                log("SALE RES CODE: " + saleResponse.getResponseCode());

                TransactionDetailsPresenter.this.saleResponse = saleResponse;

                if (repository.isEcrInitiatedSale()) {
                    EcrReq ecrReq = new EcrReq();
                    ecrReq.setInvoiceNo(invoiceNo);
                    ecrReq.setBatchNo(merchant.getBatchNumber());
                    ecrReq.setTerminalId(terminal.getTerminalID());
                    ecrReq.setMid(merchant.getMerchantID());
                    ecrReq.setHostName(host.getHostName());
                    ecrReq.setApproveCode(saleResponse.getApprovalCode());
                    ecrReq.setResponseCode(saleResponse.getResponseCode());
                    ecrReq.setRefNo(saleResponse.getRrn());
                    ecrReq.setCardNoFirst6(cardData.getPan().substring(0, 6));
                    ecrReq.setCardNoLast4(cardData.getPan().substring(cardData.getPan().length() - 4));
                    ecrReq.setCardType(issuer.getIssuerLable());
                    ecrReq.setName(cardData.getCardHolderName());
                    String ecrCmd = ecrReq.toEcrReq();
                    log("ECR_CMD: " + ecrCmd);
                    PosDevice.getInstance().getEcrCom().writeMsg(ecrCmd);
                }

                if (validateSale()) {
                    log("Sale validated.");
                    com.epic.pos.data.db.dbtxn.modal.Transaction t = createTransactionObj();
                        if (saleResponse.getResponseCode().equals(SaleResponse.RES_CODE_SUCCESS)) {
                            log("Sale response code validation success.");

                            if(!repository.isAuthOnlySale()){
                         ///   repository.insertTransaction(t, saleId -> {
                            //    repository.saveCurrentSaleId((int) saleId);
                                log("Sale record created.");
                                if (cardAction == CardAction.SWIPE) {
                                    log("Card swiped, goto signature pad.");
                                    repository.deleteReversal(reversal, () -> {
                                        log("Sale reversal deleted.");
                                    repository.insertTransaction(t, saleId -> {
                                    repository.saveCurrentSaleId((int) saleId);

                                    if (isViewNotNull()) {
                                        mView.setSubmitButtonEnabled(true);
                                        mView.hideTxnProgress();
                                        mView.gotoSignaturePad();
                                    }
                                    });  });
                                } else if (cardAction == CardAction.FALLBACK) {
                                    log("Fallback, goto signature pad.");
                                    repository.deleteReversal(reversal, () -> {
                                        log("Sale reversal deleted.");
                                        repository.insertTransaction(t, saleId -> {
                                            repository.saveCurrentSaleId((int) saleId);
                                    if (isViewNotNull()) {
                                        mView.setSubmitButtonEnabled(true);
                                        mView.hideTxnProgress();
                                        mView.gotoSignaturePad();
                                    }
                                        });  });
                                } else if (cardAction == CardAction.INSERT) {
                                    log("Card inserted, validate emv data.");
                                    validateEmvData(saleResponse,t);
                                } else {
                                    log("Card Tapped - validate cmv result and proceed");
                                    repository.deleteReversal(reversal, () -> {
                                        log("Sale reversal deleted.");
                                        repository.insertTransaction(t, saleId -> {

                                                    repository.saveCurrentSaleId((int) saleId);
                                                validateCVMResultAndProceed();
                                        });
                                    });
                                }
                          //  });
                            }else {

                                if (cardAction == CardAction.SWIPE) {
                                    log("Card swiped, goto signature pad.");
                                    repository.deleteReversal(reversal, () -> {
                                        log("Sale reversal deleted.");
                                    if (isViewNotNull()) {
                                        mView.setSubmitButtonEnabled(true);
                                        mView.hideTxnProgress();
                                        mView.gotoSignaturePad();
                                    }
                                    });
                                } else if (cardAction == CardAction.FALLBACK) {
                                    log("Fallback, goto signature pad.");
                                    repository.deleteReversal(reversal, () -> {
                                        log("Sale reversal deleted.");
                                    if (isViewNotNull()) {
                                        mView.setSubmitButtonEnabled(true);
                                        mView.hideTxnProgress();
                                        mView.gotoSignaturePad();
                                    }
                                });
                                } else if (cardAction == CardAction.INSERT) {
                                    log("Card inserted, validate emv data.");
                                    validateEmvData(saleResponse, t);
                                } else {
                                    repository.deleteReversal(reversal, () -> {
                                        log("Sale reversal deleted.");
                                    log("Card Tapped - validate cmv result and proceed");
                                    validateCVMResultAndProceed();
                                    });
                                }
                            }
                        }
                        else {
                            log("Sale response code validation failed.");
                          //  if (cardAction == CardAction.INSERT) {
                        //    saleResponse.setResponseCode("Z3");  // TEST CASE
                           //     validateEmvData(saleResponse, t);
                           // } else {
                            repository.deleteReversal(reversal, () -> {
                                errorMsg = ErrorMsg.getErrorMsg("", saleResponse.getResponseCode());
                                if (isViewNotNull()) {
                                    mView.setSubmitButtonEnabled(true);
                                    mView.hideTxnProgress();
                                    mView.onTxnFailed(errorMsg);
                                }
                           //}
                            });
                        }
                  //  });
                } else {
                    log("Sale validation failed.");
                    if (isViewNotNull()) {
                        mView.setSubmitButtonEnabled(true);
                        mView.hideTxnProgress();
                        mView.onTxnFailed(errorMsg);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log("SaleRequest - onError()");
                throwable.printStackTrace();

                if (isViewNotNull()) {
                    mView.setSubmitButtonEnabled(true);
                    mView.hideTxnProgress();
                    mView.onTxnFailed(Const.MSG_TXN_REQUEST_ERROR);
                }

            }

            @Override
            public void TLEError(String error) {
                log("Sale request - TLEError");
                repository.deleteReversal(reversal, () -> {
                if (isViewNotNull()) {
                    mView.setSubmitButtonEnabled(true);
                    mView.hideTxnProgress();
                    mView.onTxnFailed(Const.MSG_PLEASE_DOWNLOAD_TLE_KEY);
                }
                });
            }

            @Override
            public void onCompleted() {

            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Pending Reversal Functions">
    private void sendPendingReversalRequest() {
        log("sendPendingReversalRequest()");
        TLEData tleData = new TLEData();
        tleData.setChipStatus(Integer.parseInt(pendingRReq.getPosEntryMode()));
        tleData.setHostId(host.getHostID());
        tleData.setIssuerId(issuer.getIssuerNumber());
        tleData.setPan(pendingRReq.getPan());
        tleData.setTrack2(pendingRReq.getTrack2Data());
        tleData.setTleEnable(host.getTLEEnabled() == 1);

        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_REVERSAL_REQUEST, Const.MSG_PLEASE_WAIT);
        }

        repository.reversalRequest(issuer, pendingRReq, tleData, new Repository.ReversalTransactionListener() {
            @Override
            public void onReceived(ReversalResponse reversalResponse) {
                AppLog.i(TAG, "REVERSAL RES CODE: " + reversalResponse.getResponseCode());
                if (isViewNotNull()) {
                    mView.hideLoader();
                }

                TransactionDetailsPresenter.this.pendingRRes = reversalResponse;
                validateReversal(pendingRReq, pendingRRes, (isValid, error) -> {
                    if (isValid) {
                        log("Reversal response validated.");
                        repository.deleteReversal(pendingReversal, () -> {
                            log("Reversal successfully cleared.");
                            proceedSaleRequest();
                        });
                    } else {
                        log("Reversal validation failed.");
                        if (isViewNotNull()) {
                            mView.reversalValidationFailed(error);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                log("Reversal error.");
                throwable.printStackTrace();
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.gotoReversalFailedActivity();
                }
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void TLEError(String error) {
                log("Reversal request - TLE error.");
                if (isViewNotNull()) {
                    mView.gotoReversalFailedActivity();
                    mView.hideLoader();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Create reversal object for current sale">
    private Reversal createReversalObj() {
        log("createReversalObj()");
        Reversal r = new Reversal();
        r.setInvoice_no(invoiceNo);
        r.setTrace_no(traceNo);
        r.setTxn_date(saleRequest.getTxnDate());
        r.setTxn_time(saleRequest.getTxnTime());
        r.setHost(host.getHostID());
        r.setMerchant_no(merchant.getMerchantNumber());
        r.setMti(saleRequest.getMti());
        r.setProcessing_code(saleRequest.getProcessingCode());
        r.setTransaction_code(TranTypes.SALE);

        if (repository.isManualSale()) {
            r.setTransaction_code(TranTypes.SALE_MANUAL);
        }

        if (repository.isOfflineSale()) {
            r.setTransaction_code(TranTypes.SALE_OFFLINE);
        }

        if (repository.isOfflineManualSale()) {
            r.setTransaction_code(TranTypes.SALE_OFFLINE_MANUAL);
        }

        if (repository.isPreAuthSale()) {
            r.setTransaction_code(TranTypes.SALE_PRE_AUTHORIZATION);
        }
        if (repository.isAuthOnlySale()) {
            r.setTransaction_code(TranTypes.AUTH_ONLY);
        }
        if (repository.isPreAuthManualSale()) {
            r.setTransaction_code(TranTypes.SALE_PRE_AUTHORIZATION_MANUAL);
        }

        if (repository.isInstallmentSale()) {
            r.setTransaction_code(TranTypes.SALE_INSTALLMENT);
        }

        if (repository.isRefundSale()) {
            r.setTransaction_code(TranTypes.SALE_REFUND);
        }

        if (repository.isRefundManualSale()) {
            r.setTransaction_code(TranTypes.SALE_REFUND_MANUAL);
        }

        if (repository.isCashBackSale()) {
            r.setTransaction_code(TranTypes.CASH_BACK);
        }

        if (repository.isQuasiCashFlow()) {
            r.setTransaction_code(TranTypes.QUASI_CASH);
        }

        if (repository.isQuasiCashManualFlow()) {
            r.setTransaction_code(TranTypes.QUASI_CASH_MANUAL);
        }

        if (repository.isCashAdvance()) {
            r.setTransaction_code(TranTypes.CASH_ADVANCE);
        }

        if (repository.isStudentRefSale()) {
            r.setStd_ref_no(repository.getStudentReferenceNo());
        }

        if (repository.isCashBackSale()) {
            long cashBackAmountLong = Long.parseLong(cashBackAmount.replaceAll(",", "")
                    .replaceAll("\\.", ""));
            r.setCash_back_amount(String.valueOf(cashBackAmountLong));
        }

        long totalAmountLong = Long.parseLong(totalAmount.replaceAll(",", "")
                .replaceAll("\\.", ""));
        long baseAmountLong = Long.parseLong(baseAmount.replaceAll(",", "")
                .replaceAll("\\.", ""));

        r.setBase_transaction_amount(String.valueOf(baseAmountLong));
        r.setTotal_amount(String.valueOf(totalAmountLong));
        r.setChip_status(Integer.parseInt(saleRequest.getPosEntryMode()));
        r.setPan(cardData.getPan());
        r.setCard_serial_number("");

        if (!repository.isManualSale()
                && !repository.isOfflineManualSale()
                && !repository.isPreAuthManualSale()
                && !repository.isRefundManualSale()
                && !repository.isQuasiCashManualFlow()) {
            r.setTrack2(cardData.getTrack2());
            r.setSvc_code(cardData.getServiceCode());
        }

        r.setExp_date(cardData.getExpiryDate());
        r.setTerminal_id(saleRequest.getTid());
        r.setTerminal_no(terminal.getID());
        r.setMerchant_id(saleRequest.getMid());
        r.setMerchant_name(merchant.getMerchantName());
        r.setNii(saleRequest.getNii());
        r.setSecure_nii(saleRequest.getSecureNii());
        r.setTpdu(saleRequest.getTpdu());

        if (saleRequest.getCardAction() == CardAction.TAP.val
                || saleRequest.getCardAction() == CardAction.INSERT.val) {
            r.setEmv_field_55(saleRequest.getEmvData());
        }

        r.setResponse_code("00");
        r.setCdt_index(cardDefinition.getId());
        r.setIssuer_number(issuer.getIssuerNumber());
        r.setCard_label(getCardLabel(cardAction, cardDefinition));
        r.setCurrency_symbol(currency.getCurrencySymbol());
        return r;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Create transaction object for current sale">
    private com.epic.pos.data.db.dbtxn.modal.Transaction createTransactionObj() {
        log("createTransactionObj()");
        com.epic.pos.data.db.dbtxn.modal.Transaction t =
                new com.epic.pos.data.db.dbtxn.modal.Transaction();

        t.setInvoice_no(invoiceNo);
        t.setTrace_no(traceNo);
        t.setHost(host.getHostID());
        t.setMerchant_no(merchant.getMerchantNumber());
        t.setTransaction_code(TranTypes.SALE);
        t.setTerminal_no(terminal.getID());
        t.setCard_holder_name(cardData.getCardHolderName());

        if (repository.isManualSale()) {
            t.setTransaction_code(TranTypes.SALE_MANUAL);
        }

        if (repository.isOfflineSale()) {
            t.setTransaction_code(TranTypes.SALE_OFFLINE);
        }

        if (repository.isOfflineManualSale()) {
            t.setTransaction_code(TranTypes.SALE_OFFLINE_MANUAL);
        }

        if (repository.isPreAuthSale()) {
            t.setTransaction_code(TranTypes.SALE_PRE_AUTHORIZATION);
        }
        if (repository.isAuthOnlySale()) {
            t.setTransaction_code(TranTypes.AUTH_ONLY);
        }
        if (repository.isAuthOnlySale()) {
            t.setTransaction_code(TranTypes.SALE_PRE_AUTHORIZATION);
        }
        if (repository.isPreAuthManualSale()) {
            t.setTransaction_code(TranTypes.SALE_PRE_AUTHORIZATION_MANUAL);
        }

        if (repository.isInstallmentSale()) {
            t.setTransaction_code(TranTypes.SALE_INSTALLMENT);
        }

        if (repository.isRefundSale()) {
            t.setTransaction_code(TranTypes.SALE_REFUND);
        }

        if (repository.isRefundManualSale()) {
            t.setTransaction_code(TranTypes.SALE_REFUND_MANUAL);
        }

        if (repository.isCashBackSale()) {
            t.setTransaction_code(TranTypes.CASH_BACK);
        }

        if (repository.isQuasiCashFlow()) {
            t.setTransaction_code(TranTypes.QUASI_CASH);
        }

        if (repository.isQuasiCashManualFlow()) {
            t.setTransaction_code(TranTypes.QUASI_CASH_MANUAL);
        }

        if (repository.isCashAdvance()) {
            t.setTransaction_code(TranTypes.CASH_ADVANCE);
        }

        if (repository.isStudentRefSale()) {
            t.setStd_ref_no(repository.getStudentReferenceNo());
        }

        if (repository.isOfflineManualSale() || repository.isOfflineSale()) {
            //offline manual sale
            t.setApprove_code(repository.getOfflineApprovalCode());
            t.setTxn_date(saleRequest.getTxnDate());
            t.setTxn_time(saleRequest.getTxnTime());
            t.setMti(saleRequest.getMti());
            t.setProcessing_code(saleRequest.getProcessingCode());
            t.setTerminal_id(saleRequest.getTid());
        } else {
            //normal sale
            t.setApprove_code(saleResponse.getApprovalCode());
            t.setTxn_date(saleResponse.getDate());
            t.setTxn_time(saleResponse.getTime());
            t.setRrn(saleResponse.getRrn());
            t.setMti(reversal.getMti());
            t.setProcessing_code(saleResponse.getProcessingCode());
            t.setTerminal_id(saleResponse.getTid());
        }

        if (repository.isCashBackSale()) {
            long cashBackAmountLong = Long.parseLong(cashBackAmount.replaceAll(",", "")
                    .replaceAll("\\.", ""));
            t.setCash_back_amount(String.valueOf(cashBackAmountLong));
        }

        long totalAmountLong = Long.parseLong(totalAmount.replaceAll(",", "")
                .replaceAll("\\.", ""));
        long baseAmountLong = Long.parseLong(baseAmount.replaceAll(",", "")
                .replaceAll("\\.", ""));

        t.setBase_transaction_amount(String.valueOf(baseAmountLong));
        t.setTotal_amount(String.valueOf(totalAmountLong));
        t.setChip_status(Integer.parseInt(saleRequest.getPosEntryMode()));
        t.setPan(cardData.getPan());
        t.setCard_serial_number("");

        if (!repository.isManualSale()
                && !repository.isOfflineManualSale()
                && !repository.isPreAuthManualSale()
                && !repository.isRefundManualSale()
                && !repository.isQuasiCashManualFlow()) {
            t.setTrack2(cardData.getTrack2());
            t.setSvc_code(cardData.getServiceCode());
        }

        t.setExp_date(cardData.getExpiryDate());
        t.setMerchant_id(saleRequest.getMid());
        t.setMerchant_name(merchant.getMerchantName());
        t.setNii(saleRequest.getNii());
        t.setSecure_nii(saleRequest.getSecureNii());
        t.setTpdu(saleRequest.getTpdu());

        if (saleRequest.getCardAction() == CardAction.TAP.val
                || saleRequest.getCardAction() == CardAction.INSERT.val) {
            t.setEmv_field_55(saleRequest.getEmvData());
        }

        t.setResponse_code("00");
        t.setCdt_index(cardDefinition.getId());
        t.setIssuer_number(issuer.getIssuerNumber());
        t.setVoided(0);
        t.setCard_label(getCardLabel(cardAction, cardDefinition));
        t.setCurrency_code(Integer.parseInt(currency.getCurrencyCode()));
        t.setCurrency_symbol(currency.getCurrencySymbol());

        return t;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sale validations">
    private void validateEmvData(SaleResponse saleResponse, com.epic.pos.data.db.dbtxn.modal.Transaction t) {
        log("validateEmvData()");
        PosDevice.getInstance().checkPrinterStatus(() -> {
            PosDevice.getInstance().verifyOnlineProcess(
                    saleResponse.getResponseCode(),
                    saleResponse.getApprovalCode(),
                    saleResponse.getEmvData(), new VerifyOnlineProcessListener() {
                        @Override
                        public void onlineProcessSuccess() {
                            log("onlineProcessSuccess()");
                            log("Insert - validate cmv result and proceed");
                            repository.deleteReversal(reversal, () -> {
                                log("Sale reversal deleted.");
                                repository.insertTransaction(t, saleId -> {
                                    repository.saveCurrentSaleId((int) saleId);
                                        validateCVMResultAndProceed();
                                });
                            });
                        }

                        @Override
                        public void onlineProcessRefuse() {
                            log("onlineProcessRefuse()");
                            if (isViewNotNull()) {
                                mView.setSubmitButtonEnabled(true);
                                mView.hideTxnProgress();

                                String err = ErrorMsg.getErrorMsg("", saleResponse.getResponseCode());
                                if (err.endsWith("Response error occurred")) {
                                    err = "Transaction declined by chip.";
                                }
                                mView.onTxnFailed(err);
                            }
                        }

                        @Override
                        public void onlineProcessTerminate() {
                            log("onlineProcessTerminate()");
                            if (isViewNotNull()) {
                                mView.setSubmitButtonEnabled(true);
                                mView.hideTxnProgress();

                                String err = ErrorMsg.getErrorMsg("", saleResponse.getResponseCode());
                                if (err.endsWith("Response error occurred")) {
                                    err = "Online Failure, Terminate.";
                                }

                                mView.onTxnFailed(err);
                            }
                        }

                        @Override
                        public void onError(int code) {
                            log("onError() code: " + code);
                            if (isViewNotNull()) {
                                mView.setSubmitButtonEnabled(true);
                                mView.hideTxnProgress();

                                String err = ErrorMsg.getErrorMsg("", saleResponse.getResponseCode());
                                mView.onTxnFailed(err);
                            }
                        }
                    });
        });
    }

    private void validateCVMResultAndProceed() {
        log("validateCVMResultAndProceed()");
        CVMResult cvmResult = PosDevice.getInstance().analyseCVMResult(cardAction == CardAction.TAP);
        if (cvmResult == CVMResult.SIGNATURE
                || cvmResult == CVMResult.PLAIN_PIN_ICC_AND_SIGNATURE
                || cvmResult == CVMResult.ENCRYPTED_PIN_BY_ICC_ABD_SIGNATURE
                || cvmResult == CVMResult.UNKNOWN) {
            if (isViewNotNull()) {
                mView.gotoSignaturePad();
            }
        } else {
            if (isViewNotNull()) {
                mView.gotoReceiptActivity();
            }
        }
    }

    private boolean validateSale() {
        log("validateSale()");
        String mti = Transaction.SALE_RES_MTI;

        if (repository.isPreAuthSale() || repository.isPreAuthManualSale()) {
            mti = Transaction.PRE_AUTH_MTI_RES;
        }
        if (repository.isAuthOnlySale()) {
            mti = Transaction.AUTH_ONLY_MTI_RES;
        }

        if (saleResponse.getMti().equals(mti)) {
            log("MTI matched.");
            if (saleResponse.getProcessingCode().equals(saleRequest.getProcessingCode())) {
                log("Processing code matched.");
                if (saleResponse.getTraceNumber().equals(saleRequest.getTraceNumber())) {
                    log("Trace number matched.");
                    if (saleResponse.getTid().equals(saleRequest.getTid())) {
                        log("TID matched.");
                        if (saleResponse.getResponseCode() != null
                                && !saleResponse.getResponseCode().isEmpty()) {
                            log("Response code exists.");
                            return true;
                        } else {
                            errorMsg = "Invalid sale response";
                        }
                    } else {
                        errorMsg = "Terminal ID mismatch";
                    }
                } else {
                    errorMsg = "Trace number mismatch";
                }
            } else {
                errorMsg = "Processing code mismatch";
            }
        } else {
            errorMsg = "MTI mismatch";
        }

        return false;
    }
    // </editor-fold>

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }
}
