package com.epic.pos.ui.sale.manual;

import android.text.TextUtils;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public class ManualSalePresenter extends BasePresenter<ManualSaleContact.View> implements ManualSaleContact.Presenter {

    private final String TAG = ManualSalePresenter.class.getSimpleName();

    private Repository repository;
    private NetworkConnection networkConnection;
    private boolean hasValidExpDate = false;
    private boolean hasValidCardNo = false;
    private boolean hasValidApprovalCode = false;
    private Date expireDateObj;
    private String expireDate;
    private String cardNo;
    private String approvalCode;

    private List<Merchant> merchantGroup = new ArrayList<>();

    @Inject
    public ManualSalePresenter(Repository repository, NetworkConnection networkConnection) {
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

    @Override
    public void init() {
        int selectedMerchantGroup = repository.getSelectedMerchantGroupId();
        repository.getEnabledMerchantsFromGroupId(selectedMerchantGroup, merchants -> {
            merchantGroup = merchants;
        });

        if (repository.isOfflineManualSale()) {
            mView.showApprovalCode();
        }
    }

    @Override
    public void onSubmit() {
        checkCDTByPan(cardNo);
    }

    private void checkCDTByPan(String pan) {
        repository.getCdtListByPan(pan, cdts -> {
            if (cdts.size() == 0) {
                //invalid card
                mView.onCDTError();
            } else if (cdts.size() == 1) {
                //auto select card type
                onCDTSelected(cdts.get(0));
            } else {
                //let user to select card type
                mView.onMultipleCDTReceived(cdts);
            }
        });
    }


    @Override
    public void onCDTSelected(CardDefinition cardDefinition) {
        repository.saveSelectedCardDefinitionId(cardDefinition.getId());
        validateData();
    }

    private void validateData() {
        //Get CardDefinition - CDT
        repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
            //check card expire status
            if (!isCardExpired(cardDefinition)) {
                //Validate card number min pan and max pan length
                if (cardNo.length() >= cardDefinition.getMinPanDigit()
                        && cardNo.length() <= cardDefinition.getMaxPanDigit()) {
                    //Get Issuer - IIT
                    repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                        if (issuer != null) {
                            //Get Host - IHT
                            repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                                if (host != null) {
                                    //Check must settle status on host
                                    if (host.getMustSettleFlag() == 0) {
                                        //Check host support for selected merchant group
                                        Merchant merchant = getHostSupportedMerchantFromGroup(host);
                                        if (merchant != null) {
                                            //Card host support for selected merchant
                                            //Get Terminal for merchant - TMIF
                                            repository.getTerminalByMerchant(merchant.getMerchantNumber(), terminal -> {
                                                if (terminal != null) {
                                                    repository.saveSelectedTerminalId(terminal.getID());
                                                    //proceed manual sale
                                                    CardData c = new CardData();
                                                    c.setPan(cardNo);
                                                    c.setExpiryDate(convertExpDate());
                                                    repository.saveCardData(c);
                                                    repository.saveCardAction(CardAction.MANUAL);

                                                    if (repository.isOfflineManualSale()) {
                                                        repository.saveOfflineApprovalCode(approvalCode);
                                                    }

                                                    mView.gotoTxnDetailActivity();
                                                } else {
                                                    mView.showDataMissingError(Const.MSG_TERMINAL_NOT_FOUND_FOR_MERCHANT);
                                                }
                                            });
                                        } else {
                                            mView.showDataMissingError(Const.MSG_HOST_NOT_SUPPORT_FOR_MERCHANT);
                                        }
                                    } else {
                                        mView.gotoNoRetryFailedActivity(Const.MSG_SETTLEMENT_PENDING, Const.MSG_MUST_SETTLE);
                                    }
                                } else {
                                    mView.showDataMissingError(Const.MSG_HOST_NOT_FOUND);
                                }
                            });
                        } else {
                            mView.showDataMissingError(Const.MSG_ISSUER_NOT_FOUND);
                        }
                    });
                } else {
                    mView.showValidationError(Const.MSG_PLEASE_CHECK_THE_CARD_NO);
                }
            } else {
                mView.showValidationError(Const.MSG_CARD_EXPIRED);
            }
        });
    }


    @Override
    public void setApprovalCode(String approvalCode) {
        hasValidApprovalCode = validateApprovalCode(approvalCode);
        if (hasValidApprovalCode) {
            ManualSalePresenter.this.approvalCode = approvalCode;
        }
        validateActionBtn();
    }


    @Override
    public void setExpireDate(String expireDate) {
        hasValidExpDate = isValidDate(expireDate);
        if (hasValidExpDate) {
            ManualSalePresenter.this.expireDate = expireDate;
        }
        validateActionBtn();
    }

    @Override
    public void setCardNumber(String cardNumber) {
        hasValidCardNo = isValidCardNumber(cardNumber);
        if (hasValidCardNo) {
            ManualSalePresenter.this.cardNo = cardNumber;
        }
        validateActionBtn();
    }

    private boolean isCardExpired(CardDefinition cardDefinition) {
        boolean isCardExpired = true;
        try {
            Calendar expCalendar = Calendar.getInstance();
            expCalendar.setTime(expireDateObj);
            expCalendar.add(Calendar.MONTH, 1);
            isCardExpired = expCalendar.getTime().before(new Date());
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }

        if (cardDefinition.getExpDateRequired() == 1) {
            //check card expiration
            return isCardExpired;
        } else {
            //card expiration check not required
            if (isCardExpired) {
                mView.showToastMessage(Const.MSG_PROCEED_WITH_EXPIRED_CARD);
            }
            return false;
        }
    }

    private void validateActionBtn() {
        if (repository.isOfflineManualSale()) {
            mView.setActionBtnEnabled(hasValidCardNo && hasValidExpDate && hasValidApprovalCode);
        } else {
            mView.setActionBtnEnabled(hasValidCardNo && hasValidExpDate);
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.length() >= 9;
    }

    private boolean validateApprovalCode(String approvalCode) {
        return approvalCode.length() == 6;
    }

    private String convertExpDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(Const.CARD_EXP_DATE_FORMAT, Locale.ENGLISH);
        return sdf.format(expireDateObj);
    }

    private boolean isValidDate(String expireDate) {
        try {
            if (!TextUtils.isEmpty(expireDate) && expireDate.length() == 5) {
                SimpleDateFormat sdf = new SimpleDateFormat(Const.CARD_DISPLAY_EXP_DATE_FORMAT, Locale.ENGLISH);
                sdf.setLenient(false);

                String cMonth = expireDate.substring(0, 2);
                String cYear = expireDate.substring(3, 5);
                log("Year: " + cYear);
                log("Month: " + cMonth);

                Calendar cardCalendar = Calendar.getInstance();
                cardCalendar.set(Calendar.YEAR, 2000 + Integer.parseInt(cYear));
                cardCalendar.set(Calendar.MONTH, Integer.parseInt(cMonth) - 1);
                log("Card expire: " + new SimpleDateFormat("yyyy-MM").format(cardCalendar.getTime()));
                expireDateObj = cardCalendar.getTime();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private Merchant getHostSupportedMerchantFromGroup(Host host) {
        for (Merchant m : merchantGroup) {
            if (m.getHostId() == host.getHostID()) {
                return m;
            }
        }

        return null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}