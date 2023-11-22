package com.epic.pos.ui.sale.cardscan;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.epic.pos.data.DccData;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.iso.modal.request.DCCRequest;
import com.epic.pos.iso.modal.response.BatchUploadResponse;
import com.epic.pos.iso.modal.response.DCCResponse;
import com.epic.pos.tle.TLEData;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.BitMapUtil;
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
import com.epic.pos.device.listener.PosCheckCardListener;
import com.epic.pos.device.listener.PosPinListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public class CardScanPresenter extends BasePresenter<CardScanContract.View> implements CardScanContract.Presenter {

    private final String TAG = CardScanPresenter.class.getSimpleName();

    private Repository repository;
    private NetworkConnection networkConnection;

    private Handler handler = new Handler(Looper.getMainLooper());
    private TCT tct;
    //Preload data
    private List<Merchant> merchantGroup = new ArrayList<>();
    //Activity state
    private boolean isPause = false;
    //Check card status
    private boolean checkContactLess = true;
    private boolean checkSmartCard = true;
    private boolean checkMagneticCard = true;
    //Pin pad data
    private boolean isCardPinRequested = false;
    private boolean isOnlinePin = false;
    private int retryTimes = 0;
    private String pinBlock;
    //final flow
    private boolean onRequestOnlineProcess = false;
    //process thread
    protected boolean processThreadStart = true;
    //Card related data
    private boolean isFallback = false;
    private CardData cardData = null;
    private CardAction cardAction = null;
    private Issuer issuer = null;
    private Host host = null;
    private Merchant merchant = null;
    private Terminal terminal = null;
    //Thread states
    private Step step = Step.DEFAULT;
    //After validation timer
    private CountDownTimer countDownTimer;

    private enum Step {
        DEFAULT,
        CARD_DATA_RECEIVED,
        CARD_DATA_VALIDATING,
        CARD_DATA_VALIDATED,
        PIN_PAD_LAUNCHED
    }


    @Inject
    public CardScanPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
        repository.getTCT(t -> {
            tct = t;
        });
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void resetData() {
        repository.setIsCardPinEntered(false);
    }

    @Override
    public void updateUi() {
        int selectedMerchantGroup = repository.getSelectedMerchantGroupId();
        repository.getEnabledMerchantsFromGroupId(selectedMerchantGroup, merchants -> {
            merchantGroup = merchants;
            //select first merchant object from the group
            repository.getCurrencyByMerchantId(merchants.get(0).getMerchantNumber(), currency -> {
                String checkCardStatus = "";
                if (checkSmartCard && checkMagneticCard && checkContactLess) {
                    checkCardStatus = Const.TXT_CARD_STATE_1;
                } else if (checkSmartCard && checkMagneticCard) {
                    checkCardStatus = Const.TXT_CARD_STATE_2;
                } else if (checkSmartCard && checkContactLess) {
                    checkCardStatus = Const.TXT_CARD_STATE_3;
                } else if (checkMagneticCard && checkContactLess) {
                    checkCardStatus = Const.TXT_CARD_STATE_4;
                } else if (checkMagneticCard) {
                    checkCardStatus = Const.TXT_CARD_STATE_5;
                } else if (checkSmartCard) {
                    checkCardStatus = Const.TXT_CARD_STATE_6;
                } else if (checkContactLess) {
                    checkCardStatus = Const.TXT_CARD_STATE_7;
                }

                if (isViewNotNull()) {
                    mView.onUpdateUi(currency.getCurrencySymbol(), repository.getTotalAmount(), checkCardStatus);
                }
            });
        });
    }

    @Override
    public void startAfterScanCountDown() {
        log("startAfterScanCountDown()");
        if (countDownTimer == null) {
            int countDown = (Const.PIN_ENTER_TIMEOUT + 3) * 1000;
            countDownTimer = new CountDownTimer(countDown, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    log("After scan countdown finished.");
                    processThreadStart = false;

                    if (isViewNotNull()) {
                        mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_TXN_ONLINE_PROCESS_TIMEOUT);
                    }

                    log("Emv floe stop");
                    PosDevice.getInstance().stopEmvFlow();
                }
            };
            countDownTimer.start();
        }
    }

    @Override
    public void checkCard() {
        log("checkCard()");

        if (PosDevice.getInstance().isCardChecking()) {
            log("card is checking.");
            return;
        }

        if (repository.isOfflineSale()) {
            checkContactLess = false;
        }

        if (isFallback) {
            checkContactLess = false;
            checkSmartCard = false;
            updateUi();
        }

        repository.getTCT(tct -> {
            int checkCardTimeOut = isFallback ? Integer.parseInt(tct.getFallBackTime()) : Const.CHECK_CARD_TIMEOUT;

            log("checkCardTimeOut: " + checkCardTimeOut);

            PosDevice.getInstance().checkCard(
                    checkContactLess,
                    checkSmartCard,
                    checkMagneticCard,
                    true,
                    checkCardTimeOut,
                    new PosCheckCardListener() {
                        @Override
                        public void onCardInserted() {
                            log("onCardInserted()");
                        }

                        @Override
                        public void onCheckCardError(int error, String msg) {
                            log("onCheckCardError() error: " + error + "| msg: " + msg);
                            if (isPause) {
                                log("Activity paused!");
                                return;
                            }

                            if (error == 3 && tct.getFallbackEnable() == 1) {
                                log("Fallback enabled.");
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_MSG_FALLBACK);
                                }
                                isFallback = true;
                                checkCard();
                            } else {
                                if (isViewNotNull()) {
                                    mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_CARD_READ_ERROR);
                                }
                            }
                        }

                        @Override
                        public void onEmvError(int result, Bundle data, String msg) {
                            log("onEmvError");
                            if (isPause) {
                                log("Activity paused!");
                                return;
                            }

                            if (isViewNotNull()) {
                                mView.gotoNoRetryFailedActivity(Const.MSG_EMV_ERROR, msg);
                            }
                        }

                        @Override
                        public void onTimeOut() {
                            log("onTimeOut");
                            if (isViewNotNull()) {
                                mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_TXN_TIME_OUT);
                            }
                        }

                        @Override
                        public void onFallback() {
                            log("onFallback");
                            if (isPause) {
                                log("Activity paused!");
                                return;
                            }
                            if (tct.getFallbackEnable() == 1) {
                                log("fallback enabled");
                                isFallback = true;
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_MSG_FALLBACK);
                                }
                                checkCard();
                            } else {
                                log("fallback disabled");
                                if (isViewNotNull()) {
                                    mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_FALLBACK_DISABLED);
                                }
                            }
                        }

                        @Override
                        public void onSelectApplication(List<String> applications) {
                            log("onSelectApplication()");
                            if (applications != null) {
                                log("Multi application size: " + applications.size());
                                for (String app : applications) {
                                    log("Application: " + app);
                                }
                            } else {
                                log("Multi applications are null");
                            }

                            if (isViewNotNull()) {
                                mView.onMultiApplicationCard(applications);
                            }
                        }

                        @Override
                        public void onCardData(CardAction cardAction, CardData cardData) {
                            log("onCardData() cardAction:" + cardAction.val + "| cardData: " + cardData);
                            if (isFallback) {
                                if (cardAction == CardAction.SWIPE) {
                                    cardAction = CardAction.FALLBACK;
                                }
                            }

                            CardScanPresenter.this.cardData = cardData;
                            CardScanPresenter.this.cardAction = cardAction;
                            CardScanPresenter.this.step = Step.CARD_DATA_RECEIVED;
                            repository.saveCardAction(cardAction);
                            repository.saveCardData(cardData);
                        }

                        @Override
                        public void onPinRequested(boolean isOnlinePin, int retryTimes) {
                            log("onPinRequested()");
                            repository.setOnlinePinRequested(isOnlinePin);
                            CardScanPresenter.this.isCardPinRequested = true;
                            CardScanPresenter.this.isOnlinePin = isOnlinePin;
                            CardScanPresenter.this.retryTimes = retryTimes;
                        }

                        @Override
                        public void onRequestOnlineProcess() {
                            log("onRequestOnlineProcess()");
                            onRequestOnlineProcess = true;
                        }

                        @Override
                        public void onOfflineapprove() {
                            log("onRequestOnlineProcess()");
                            repository.setOfflineSale(true);
                            onRequestOnlineProcess = true;
                        }

                    });
        });
    }

    @Override
    public void startProcessThread() {
        new Thread() {
            private String TAG = "ProcessThread";

            @Override
            public void run() {
                AppLog.i(TAG,"Process thread running.");

                while (processThreadStart) {
                    if (step == Step.CARD_DATA_RECEIVED) {
                        AppLog.i(TAG, "Card data received. Start validate database records");
                        step = Step.CARD_DATA_VALIDATING;
                        handler.post(() -> checkCDTRecord());
                    } else if (step == Step.CARD_DATA_VALIDATED) {
                        AppLog.i(TAG, "Card data validated.");
                        if (cardAction == CardAction.SWIPE || cardAction == CardAction.FALLBACK ) {
                            AppLog.i(TAG,"Card swiped or fallbacked.");
                            processThreadStart = false;
                            if (repository.isOfflineSale()) {
                                AppLog.i(TAG,"Offline sale - Goto approval code.");
                                handler.post(() -> gotoApprovalCodeActivity());
                            } else {
                                AppLog.i(TAG,"Goto txn detail.");
                                handler.post(() -> gotoTxnDetailActivity());
                            }
                        } else {
                            AppLog.i(TAG, "Card inserted or tapped.");
                            if (repository.isOfflineSale()) {
                                AppLog.i(TAG, "Offline sale - goto approval code");
                                processThreadStart = false;
                                handler.post(() -> gotoApprovalCodeActivity());
                            } else {
                                AppLog.i(TAG, "Not offline sale");
                                if (isCardPinRequested) {
                                    AppLog.i(TAG, "Launch pin pad");
                                    step = Step.PIN_PAD_LAUNCHED;
                                    handler.post(() -> launchPinPad());
                                } else {
                                    AppLog.i(TAG, "Card pin not requested.");
                                    if (onRequestOnlineProcess) {
                                        AppLog.i(TAG, "Request online process.");
                                        processThreadStart = false;
                                        handler.post(() -> gotoTxnDetailActivity());
                                    }
                                }
                            }
                        }
                    } else if (step == Step.PIN_PAD_LAUNCHED) {
                        AppLog.i(TAG, "Pin pad launched.");
                        if (onRequestOnlineProcess) {
                            AppLog.i(TAG, "Request online process after pin enter.");
                            processThreadStart = false;
                            handler.post(() -> gotoTxnDetailActivity());
                        }
                    }

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                AppLog.i(TAG, "Process thread stopped");
            }
        }.start();
    }

    private void gotoTxnDetailActivity() {
        log("gotoTxnDetailActivity()");
        if (isViewNotNull()) {
            mView.gotoTxnDetailActivity();
        }
    }

    private void gotoApprovalCodeActivity() {
        log("gotoApprovalCodeActivity()");
        if (isViewNotNull()) {
            mView.gotoApprovalCodeActivity();
        }
    }


    private void launchPinPad() {
        log("launchPinPad()");
        log("launchPinPad() host: " + host.getHostName() + " | issuer: " + issuer.getIssuerLable());
        boolean isPinBypass = (issuer.getPINBypass() == 1);
        PosDevice.getInstance().launchPinPad(host.getWorkKeyId(), isOnlinePin, isPinBypass, cardData.getPan(), Const.PIN_PAD_TITLE, new PosPinListener() {
            @Override
            public void onKeyPress(int len, int key) {
                AppLog.i(TAG, "onKeyPress");
            }

            @Override
            public void onConfirm(byte[] data, boolean isNonePin) {
                log("onConfirm() PinPad onConfirm data = " + Utility.byte2HexStr(data));
                repository.setIsCardPinEntered(true);
                pinBlock = Utility.byte2HexStr(data);
            }

            @Override
            public void onCancel() {
                log("onCancel()");
                isPause = false;
            }

            @Override
            public void onError(int errorCode) {
                log("onError()");
                if (isViewNotNull()) {
                    mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_PIN_PAD_ERROR);
                }
            }
            @Override
            public void onpinbypass() {
                repository.setIsCardPinEntered(false);
            }

        });
    }

    private void checkCDTRecord() {
        log("validateCardData()");
        repository.getCdtListByPan(cardData.getPan(), cdts -> {
            log("CDT list received: " + cdts.size());
            if (cdts.size() == 0) {
                log("Invalid card");
                if (isViewNotNull()) {
                    mView.onCDTError();
                }
            } else if (cdts.size() == 1) {
                log("Auto select CDT (card type)");
                onCDTSelected(cdts.get(0));
            } else {
                log("Multiple CDT exists (Open user select flow)");
                if (isViewNotNull()) {
                    mView.onMultipleCDTReceived(cdts);
                }
            }
        });
    }

    @Override
    public void onCDTSelected(CardDefinition cardDefinition) {
        log("onCDTSelected()");
        if (cardAction == CardAction.SWIPE) {
            if (cardDefinition.getChkSvcCode() == 0) {
                log("check svc disabled");
                validateDBData(cardDefinition);
            } else {
                log("check svc enabled");
                if (!TextUtils.isEmpty(cardData.getServiceCode())
                        && (cardData.getServiceCode().startsWith("2") || cardData.getServiceCode().startsWith("6"))) {
                    log("chip card swiped");
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_PLEASE_INSERT_THE_CARD);
                        mView.finishCardScanActivity();
                    }
                } else {
                    log("mag card swiped");
                    validateDBData(cardDefinition);
                }
            }
        } else {
            log("card not swiped");
            validateDBData(cardDefinition);
        }
    }
    @Override
    public void onDCCselected(DccData dccData) {
        repository.saveTotalAmount(dccData.getDccamount());

        log("onDCCselected()");
        if (isNexgo()) {
            PosDevice.getInstance().confirmCardNumber();
        }
        step = Step.CARD_DATA_VALIDATED;
        startAfterScanCountDown();
    }
    @SuppressLint("Range")
    private void validateDBData(CardDefinition cardDefinition) {
        log("validateDBData()");
        if (!isCardExpired(cardDefinition)) {
            String bmp = cardDefinition.getTxnBitmap();
            int idx = getFeatureBitmapIndex(repository);
            log("Feature bitmap [ " + bmp + " | index: " + idx + " ]");
            if (BitMapUtil.isFeatureEnabled(bmp, idx)) {
                log("feature enable for this bin.");
                repository.saveSelectedCardDefinitionId(cardDefinition.getId());
                repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                    CardScanPresenter.this.issuer = issuer;
                    log("Issuer received: " + issuer);
                    if (issuer != null) {
                        repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                            CardScanPresenter.this.host = host;
                            log("Host received: " + host);
                            if (host != null) {
                                log("Check must settle status on host: " + host.getMustSettleFlag());
                                if (host.getMustSettleFlag() == 0) {
                                    log("Check host support for selected merchant group");
                                    CardScanPresenter.this.merchant = getHostSupportedMerchantFromGroup(host);
                                    if (CardScanPresenter.this.merchant != null) {
                                        log("Get Terminal for merchant - TMIF");
                                        repository.getTerminalByMerchant(merchant.getMerchantNumber(), terminal -> {
                                            if (terminal != null) {
                                                log("All data validated.");
                                                CardScanPresenter.this.terminal = terminal;
                                                repository.saveSelectedTerminalId(terminal.getID());
                                                PosDevice.getInstance().setTerminalID(terminal.getTerminalID());

                                                if(tct.getDCCEnable()==1) {

                                                    if(tct.getIsAuronetDCC()==1){

                                                        if(tct.getMinDCCAmount()<Long.parseLong(repository.getBaseAmount().replace(".","").replace(",","")))
                                                        {
                                                            repository.checkdccbin(new DbHandler.CheckDCCBINListener() {
                                                                @Override
                                                                public void onDCCBIN(List<DCCBINNLIST> binlist) {


                                                                    log("DCC BIN " + binlist.get(0).getCURRENCY());
                                                                    log("DCC BIN " + binlist.get(0).getPANL());
                                                                    log("DCC BIN " + binlist.get(0).getPANH());
                                                                    log("DCC BIN MERCHANT No " +merchant.getMerchantNumber());
                                                                    final String[] lcurrencycode = {""};
                                                                    final String[] lcurrencysymbal = {""};
                                                                    repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), new DbHandler.GetCurrencyListener() {
                                                                        @Override
                                                                        public void onReceived(Currency currency) {
                                                                            log("DCC Local Currency " +currency.getCurrencyCode() );
                                                                            lcurrencycode[0] =currency.getCurrencyCode();
                                                                            lcurrencysymbal[0] =currency.getCurrencySymbol();
                                                                        }
                                                                    });

                                                                    if(!lcurrencycode[0].equals(binlist.get(0).getCURRENCY())){
                                                                        repository.getdccdataauronet(new DbHandler.GetDCCDataCursorListener() {
                                                                            @Override
                                                                            public void onReceived(Cursor cursor) {
                                                                                if (cursor.getCount() > 0) {
                                                                                    List<DccData> dccDatalist = new ArrayList<>();

                                                                                    while (cursor.moveToNext()) {
                                                                                        // for (int i = 0; i < cursor.getColumnCount(); i++) {
                                                                                        //  String columnName = cursor.getColumnName(i);
                                                                                        String cCode = cursor.getString(cursor.getColumnIndex("CSymbol"));
                                                                                        String dccamount = cursor.getString(cursor.getColumnIndex("ConversionRate"));


                                                                                        DccData dccData = new DccData();
                                                                                        dccData.setDccamount(dccamount);
                                                                                        dccData.setBaseamount(repository.getBaseAmount());
                                                                                        dccData.setCurrancysymbol(cCode);
                                                                                        dccDatalist.add(dccData);

                                                                                        //  }

                                                                                    }


                                                                                    DccData localcurdata = new DccData();
                                                                                    localcurdata.setDccamount(repository.getBaseAmount());
                                                                                    localcurdata.setBaseamount(repository.getBaseAmount());
                                                                                    localcurdata.setCurrancysymbol(lcurrencysymbal[0]);

                                                                                    dccDatalist.add(localcurdata);

                                                                                    mView.onDCCDataSELECT(dccDatalist);
                                                                                } else {
                                                                                    log("DCC Data Not Downloaded");
                                                                                    if (isNexgo()) {
                                                                                        PosDevice.getInstance().confirmCardNumber();
                                                                                    }
                                                                                    step = Step.CARD_DATA_VALIDATED;
                                                                                    startAfterScanCountDown();
                                                                                }
                                                                            }
                                                                        }, String.valueOf(binlist.get(0).getCURRENCY()));
                                                                    }
                                                                }

                                                                @Override
                                                                public void NotDCCBIN(String message) {
                                                                    log("NOT A DCC BIN");
                                                                    if (isNexgo()) {
                                                                        PosDevice.getInstance().confirmCardNumber();
                                                                    }
                                                                    step = Step.CARD_DATA_VALIDATED;
                                                                    startAfterScanCountDown();
                                                                }
                                                            },repository.getCardData().getPan());
                                                        }
                                                        else{
                                                            log("Below DCC AMOUNT");
                                                            if (isNexgo()) {
                                                                PosDevice.getInstance().confirmCardNumber();
                                                            }
                                                            step = Step.CARD_DATA_VALIDATED;
                                                            startAfterScanCountDown();
                                                        }
                                                    }
                                                    else{
                                                    TLEData tleData = new TLEData();
                                                    //  tleData.setChipStatus(Integer.parseInt(saleRequest.getPosEntryMode()));
                                                    tleData.setHostId(host.getHostID());
                                                    tleData.setIssuerId(issuer.getIssuerNumber());
                                                    //  tleData.setPan(saleRequest.getPan());
                                                    //  tleData.setTrack2(saleRequest.getTrack2Data());
                                                    tleData.setTleEnable(host.getTLEEnabled() == 1);


                                                    log("Increment invoice number and trace number for next txn");
                                                    merchant.setInvNumber(String.valueOf(Integer.parseInt(merchant.getInvNumber()) + 1));
                                                    merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
                                                    String invoiceNo = AppUtil.toInvoiceNumber(Integer.parseInt(merchant.getInvNumber()));
                                                    String traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));
                                                    repository.updateMerchant(merchant, null);

                                                    Date transactionDate = new Date();
                                                    String txnDateString = AppUtil.toTransactionDate(transactionDate);
                                                    String txnTimeString = AppUtil.toTransactionTime(transactionDate);


                                                    DCCRequest dccrequest = new DCCRequest();
                                                    dccrequest.setPan(cardData.getPan());
                                                    dccrequest.setNii(tct.getDCCNII());
                                                    // dccrequest.setSecureNii(tct.getDCCNII());
                                                    //  dccrequest.setTpdu(terminal.getTPDU());
                                                    dccrequest.setInvoice(invoiceNo);
                                                    dccrequest.setTraceNumber(traceNo);
                                                    dccrequest.setMid(merchant.getMerchantID());
                                                    dccrequest.setTid(terminal.getTerminalID());
                                                    dccrequest.setDate(txnDateString);
                                                    dccrequest.setTime(txnTimeString);

                                                    dccrequest.setAmount(repository.getTotalAmount());

                                                    repository.DCCRequest(issuer, dccrequest, tleData, new Repository.DCCRequestListener() {

                                                        @Override
                                                        public void onReceived(DCCResponse dccResponse) {
                                                            if (dccResponse.getResponseCode().equals(DCCResponse.RES_CODE_SUCCESS)) {

                                                                if (dccResponse.getDccdata() != null) {

                                                                    List<DccData> dccDatalist = new ArrayList<>();

                                                                    DccData dccData = new DccData();
                                                                    dccData.setDccamount(repository.getTotalAmount());
                                                                    dccData.setCurrancysymbol("LKR");
                                                                    dccDatalist.add(dccData);

                                                                    DccData dccData2 = new DccData();
                                                                    dccData2.setDccamount("000000000254");
                                                                    dccData2.setCurrancysymbol("USD");
                                                                    dccDatalist.add(dccData2);

                                                                    DccData dccData3 = new DccData();
                                                                    dccData3.setDccamount("000000005654");
                                                                    dccData3.setCurrancysymbol("GBP");
                                                                    dccDatalist.add(dccData3);

                                                                    mView.onDCCDataSELECT(dccDatalist);

                                                                } else {
                                                                    log("DCC Data Not Received");
                                                                    if (isNexgo()) {
                                                                        PosDevice.getInstance().confirmCardNumber();
                                                                    }
                                                                    step = Step.CARD_DATA_VALIDATED;
                                                                    startAfterScanCountDown();
                                                                }
                                                            } else {
                                                                log("DCC Response Not Success");
                                                                if (isViewNotNull()) {
                                                                    mView.showDataMissingError(Const.MSG_DCC_ERRO + " : " + dccResponse.getResponseCode());
                                                                }


                                                            }

                                                        }

                                                        @Override
                                                        public void onError(Throwable throwable) {
                                                            log("onError");
                                                            if (isViewNotNull()) {
                                                                mView.showDataMissingError(Const.MSG_DCC_Failed);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCompleted() {

                                                        }

                                                        @Override
                                                        public void TLEError(String error) {
                                                            log("TLEError");
                                                            if (isViewNotNull()) {
                                                                mView.showDataMissingError(Const.MSG_DCC_ERRO + " TLE : " + error);
                                                            }
                                                        }

                                                    });

                                                }

                                                }
                                                else {
                                                    log("DCC Disabled");
                                                    if (isNexgo()) {
                                                        PosDevice.getInstance().confirmCardNumber();
                                                    }
                                                    step = Step.CARD_DATA_VALIDATED;
                                                    startAfterScanCountDown();
                                                }
                                            } else {
                                                log("Terminal not found");
                                                if (isViewNotNull()) {
                                                    mView.showDataMissingError(Const.MSG_TERMINAL_NOT_FOUND_FOR_MERCHANT);
                                                }
                                            }
                                        });
                                    } else {
                                        log("Host not support for merchant.");
                                        if (isViewNotNull()) {
                                            mView.showDataMissingError(Const.MSG_HOST_NOT_SUPPORT_FOR_MERCHANT);
                                        }
                                    }
                                } else {
                                    log("Settlement pending.");
                                    if (isViewNotNull()) {
                                        mView.gotoNoRetryFailedActivity(Const.MSG_SETTLEMENT_PENDING, Const.MSG_MUST_SETTLE);
                                    }
                                }
                            } else {
                                log("Host not found.");
                                if (isViewNotNull()) {
                                    mView.showDataMissingError(Const.MSG_HOST_NOT_FOUND);
                                }
                            }
                        });
                    } else {
                        log("Issuer not found.");
                        if (isViewNotNull()) {
                            mView.showDataMissingError(Const.MSG_ISSUER_NOT_FOUND);
                        }
                    }
                });
            } else {
                log("feature disabled for this bin.");
                if (isViewNotNull()){
                    mView.showDataMissingError(Const.MSG_FEATURE_DISABLED_FOR_BIN);
                }
            }
        } else {
            log("Card expired");
            PosDevice.getInstance().stopEmvFlow();
            if (isViewNotNull()) {
                mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_CARD_EXPIRED);
            }
        }
    }

    @Override
    public void onCardApplicationSelected(int index) {
        PosDevice.getInstance().selectApplication(index + 1);
    }

    private boolean isCardExpired(CardDefinition cardDefinition) {
        log("isCardExpired()");
        CardData cardData = repository.getCardData();
        boolean isCardExpired = true;
        try {
            String cYear = cardData.getExpiryDate().substring(0, 2);
            String cMonth = cardData.getExpiryDate().substring(2, 4);
            log("Year: " + cYear);
            log("Month: " + cMonth);

            Calendar cardCalendar = Calendar.getInstance();
            cardCalendar.set(Calendar.YEAR, 2000 + Integer.parseInt(cYear));
            cardCalendar.set(Calendar.MONTH, Integer.parseInt(cMonth) - 1);
            log("Card expire: " + new SimpleDateFormat("yyyy-MM").format(cardCalendar.getTime()));
            Date expiry = cardCalendar.getTime();

            Calendar expCalendar = Calendar.getInstance();
            expCalendar.setTime(expiry);
            expCalendar.add(Calendar.MONTH, 1);
            isCardExpired = expCalendar.getTime().before(new Date());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (cardDefinition.getExpDateRequired() == 1) {
            log("Card expiration check required.");
            return isCardExpired;
        } else {
            log("Card expiration check not required.");
            if (isCardExpired) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_PROCEED_WITH_EXPIRED_CARD);
                }
            }
            return false;
        }
    }

    public String getPinBlock() {
        return pinBlock;
    }

    @Override
    public void onResume() {
        isPause = false;
    }

    @Override
    public void onPause() {
        isPause = true;
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        processThreadStart = false;
    }

    private Merchant getHostSupportedMerchantFromGroup(Host host) {
        for (Merchant m : merchantGroup) {
            if (m.getHostId() == host.getHostID()) {
                return m;
            }
        }

        return null;
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}

