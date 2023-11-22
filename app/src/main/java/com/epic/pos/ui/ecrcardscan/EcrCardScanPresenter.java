package com.epic.pos.ui.ecrcardscan;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
import com.epic.pos.device.listener.PosCheckCardListener;
import com.epic.pos.device.serial.ECRCom;
import com.epic.pos.device.serial.EcrReq;
import com.epic.pos.device.serial.EcrRes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-09-08
 */
public class EcrCardScanPresenter extends BasePresenter<EcrCardScanContact.View>
        implements EcrCardScanContact.Presenter, ECRCom.ECRCommandListener {

    private final String TAG = EcrCardScanPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private boolean isMultiApplicationDialogLaunched = false;
    private boolean isFallback = false;
    private boolean isPause = false;

    private CardData cardData = null;
    private CardAction cardAction = null;
    private List<Merchant> merchantGroup = new ArrayList<>();

    @Inject
    public EcrCardScanPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void checkCard() {
        log("checkCard()");
        mView.setState(Const.TXT_CARD_STATE_1);

        if (PosDevice.getInstance().isCardChecking()) {
            log("card is checking.");
            return;
        }

        repository.getTCT(tct -> {
            int checkCardTimeOut = isFallback ? Integer.parseInt(tct.getFallBackTime()) : Const.CHECK_CARD_TIMEOUT;

            PosDevice.getInstance().checkCard(
                    true,
                    true,
                    true,
                    true,
                    checkCardTimeOut,
                    new PosCheckCardListener() {
                        @Override
                        public void onCardInserted() {
                            log("onCardInserted()");
                        }

                        @Override
                        public void onCheckCardError(int error, String errorMsg) {
                            log("onCheckCardError() error: " + error + " msg: " + errorMsg);
                            if (isPause) {
                                log("Activity paused!");
                                return;
                            }

                            if (isViewNotNull()) {
                                mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, errorMsg);
                            }
                        }

                        @Override
                        public void onEmvError(int result, Bundle data, String msg) {
                            log("onEmvError()");
                        }

                        @Override
                        public void onFallback() {
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
                                    mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_CARD_READ_ERROR);
                                }
                            }
                        }

                        @Override
                        public void onTimeOut() {
                            log("onTimeOut");
                            if (isPause) {
                                log("Activity paused!");
                                return;
                            }
                            if (isViewNotNull()) {
                                mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_TXN_TIME_OUT);
                            }
                        }

                        @Override
                        public void onCardData(CardAction cardAction, CardData cardData) {
                            log("onCardData()");
                            if (isFallback) {
                                if (cardAction == CardAction.SWIPE) {
                                    cardAction = CardAction.FALLBACK;
                                }
                            }

                            EcrCardScanPresenter.this.cardAction = cardAction;
                            EcrCardScanPresenter.this.cardData = cardData;
                            log(cardData.toString());
                            checkCDTByPan(cardData.getPan());
                        }

                        @Override
                        public void onSelectApplication(List<String> applications) {
                            log("onSelectApplication: isMultiApplicationDialogLaunched - " + isMultiApplicationDialogLaunched);
                            if (isViewNotNull()) {
                                if (!isMultiApplicationDialogLaunched) {
                                    mView.onMultiApplicationCard(applications);
                                }
                                isMultiApplicationDialogLaunched = true;
                            }
                        }

                        @Override
                        public void onPinRequested(boolean isOnlinePin, int retryTimes) {
                            log("onPinRequested() ");
                        }

                        @Override
                        public void onRequestOnlineProcess() {
                            log("onRequestOnlineProcess()");
                        }
                        @Override
                        public void onOfflineapprove() {
                            log("onRequestOnlineProcess()");
                        }
                    });
        });
    }

    @Override
    public void onCardApplicationSelected(int index) {
        PosDevice.getInstance().selectApplication(index + 1);
    }

    private void checkCDTByPan(String pan) {
        repository.getCdtListByPan(pan, cdts -> {
            if (cdts.size() == 0) {
                //invalid card
                if (isViewNotNull()) {
                    mView.onCDTError();
                }
            } else if (cdts.size() == 1) {
                //auto select card type
                onCDTSelected(cdts.get(0));
            } else {
                //let user to select card type
                if (isViewNotNull()) {
                    mView.onMultipleCDTReceived(cdts);
                }
            }
        });
    }

    @Override
    public void onCDTSelected(CardDefinition cardDefinition) {
        log("onCDTSelected()");
        if (repository.getCardAction() == CardAction.SWIPE) {
            if (cardDefinition.getChkSvcCode() == 0) {
                log("check svc disabled");
                proceedCDT(cardDefinition);
            } else {
                log("check svc enabled");
                CardData cardData = repository.getCardData();
                if (!TextUtils.isEmpty(cardData.getServiceCode())
                        && (cardData.getServiceCode().startsWith("2") || cardData.getServiceCode().startsWith("6"))) {
                    log("chip card swiped");
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_PLEASE_INSERT_THE_CARD);
                        mView.finishCardScanActivity();
                    }
                } else {
                    log("mag card swiped");
                    proceedCDT(cardDefinition);
                }
            }
        } else {
            proceedCDT(cardDefinition);
        }
    }

    private void proceedCDT(CardDefinition cardDefinition) {
        if (!isCardExpired(cardDefinition)) {
            repository.saveSelectedCardDefinitionId(cardDefinition.getId());
            validateAndProceed();
        } else {
            PosDevice.getInstance().stopEmvFlow();
            if (isViewNotNull()) {
                mView.gotoNoRetryFailedActivity(Const.MSG_TXN_FAILED, Const.MSG_CARD_EXPIRED);
            }
        }
    }

    private CardDefinition cardDefinition;
    private Issuer issuer;
    private Host host;
    private Merchant merchant;
    private Terminal terminal;

    private void validateAndProceed() {
        int selectedMerchantGroup = repository.getSelectedMerchantGroupId();
        repository.getEnabledMerchantsFromGroupId(selectedMerchantGroup, merchants -> {
            merchantGroup = merchants;
            //Get CardDefinition - CDT
            repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
                this.cardDefinition = cardDefinition;
                //Get Issuer - IIT
                repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                    if (issuer != null) {
                        this.issuer = issuer;
                        //Get Host - IHT
                        repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                            if (host != null) {
                                this.host = host;
                                //Check must settle status on host
                                if (host.getMustSettleFlag() == 0) {
                                    //Check host support for selected merchant group
                                    Merchant merchant = getHostSupportedMerchantFromGroup(host);
                                    if (merchant != null) {
                                        this.merchant = merchant;
                                        //Card host support for selected merchant
                                        //Get Terminal for merchant - TMIF
                                        repository.getTerminalByMerchant(merchant.getMerchantNumber(), terminal -> {
                                            if (terminal != null) {
                                                this.terminal = terminal;
                                                PosDevice.getInstance().stopEmvFlow();
                                                PosDevice.getInstance().setTerminalID(terminal.getTerminalID());
                                                repository.saveSelectedTerminalId(terminal.getID());
                                                repository.saveTransactionOngoing(true);
                                                repository.saveCardInitiatedSale(true);
                                                repository.saveCardAction(cardAction);
                                                repository.saveCardData(cardData);
                                                sendEcrData();
                                            } else {
                                                mView.showToastMessage(Const.MSG_TERMINAL_NOT_FOUND_FOR_MERCHANT);
                                                checkCard();
                                            }
                                        });
                                    } else {
                                        mView.showToastMessage(Const.MSG_HOST_NOT_SUPPORT_FOR_MERCHANT);
                                        checkCard();
                                    }
                                } else {
                                    mView.showToastMessage(Const.MSG_SETTLEMENT_PENDING);
                                    checkCard();
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showDataMissingError(Const.MSG_HOST_NOT_FOUND);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showDataMissingError(Const.MSG_ISSUER_NOT_FOUND);
                        }
                    }
                });
            });
        });
    }


    private void sendEcrData() {
        EcrReq e = new EcrReq();
        e.setTerminalId(terminal.getTerminalID());
        e.setMid(merchant.getMerchantID());
        e.setHostName(host.getHostName());
        e.setCardNoFirst6(cardData.getPan().substring(0, 6));
        e.setCardNoLast4(cardData.getPan().substring(cardData.getPan().length() - 4));
        e.setCardType(issuer.getIssuerLable());
        e.setName(cardData.getCardHolderName());
        String ecrMsg = e.toEcrReq();
        log("ECR_CMD: " + ecrMsg);
        PosDevice.getInstance().getEcrCom().writeMsg(ecrMsg);
    }

    private boolean isCardExpired(CardDefinition cardDefinition) {
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
            //check card expiration
            return isCardExpired;
        } else {
            //card expiration check not required
            if (isCardExpired) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_PROCEED_WITH_EXPIRED_CARD);
                }
            }
            return false;
        }
    }

    @Override
    public void initEcr() {
        repository.setEcrInitiatedSale(true);
        PosDevice.getInstance().setEcrInitListener(() ->
                PosDevice.getInstance().getEcrCom().setListener(EcrCardScanPresenter.this));
    }

    @Override
    public void onECRCommandReceived(ECRCom.EcrCommand cmd, EcrRes res) {
        if (cmd == ECRCom.EcrCommand.ECR_CMD_SALE_ONLINE) {
            //Online sale
            String amount = Utility.getFormattedAmount(Long.parseLong(res.getAmount()));
            long longAmount = Long.parseLong(amount.replaceAll(",", "")
                    .replaceAll("\\.", ""));
            log("formatted amount: " + amount);

            repository.getTCT(tct -> {
                if (longAmount > Long.parseLong(tct.getMaxTxnAmount())){
                    mView.showToastMessage(Const.MSG_MAX_AMOUNT_EXCEEDED);
                    return;
                }

                repository.saveBaseAmount(amount);
                repository.saveTotalAmount(amount);

                PosDevice.getInstance().setTxnAmount(longAmount);
                PosDevice.getInstance().setMerchantName(merchant.getMerchantName());
                PosDevice.getInstance().setMerchantId(merchant.getMerchantID());
                if (cardData.getAid() != null) {
                    PosDevice.getInstance().setTransactionAid(cardData.getAid());
                } else {
                    PosDevice.getInstance().setTransactionAid("");
                }

                mView.gotoCardScanActivity();
            });
        }
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
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    private Merchant getHostSupportedMerchantFromGroup(Host host) {
        for (Merchant m : merchantGroup) {
            if (m.getHostId() == host.getHostID()) {
                return m;
            }
        }

        return null;
    }

    private String getClearAmount(String amount) {
        return amount.replace(",", "");
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }


}