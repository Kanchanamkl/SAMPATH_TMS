package com.epic.pos.ui.sale.amount;

import android.text.TextUtils;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.ConfigMapTableHelper;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.helper.ProfileUpdateHelper;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.device.PosDevice;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-10
 */
public class AmountPresenter extends BasePresenter<AmountContract.View> implements AmountContract.Presenter {

    private final String TAG = AmountPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private String amount = "";
    private boolean hasValidAmount = false;
    private String pinBlock = null;
    private boolean isPaused = false;

    private Merchant merchant;
    private Currency currency;
    private boolean startWithAmountActivity = false;
    private TCT tct;
    private int batteryLevel = -1;

    @Inject
    public AmountPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void setAmount(String amount) {
        this.amount = amount;
        validate();
    }

    @Override
    public void initData() {
        PosDevice.getInstance().setTxnAmount(0);
        PosDevice.getInstance().setCashBackAmount(0);

        if (repository.isCashAdvance()) {
            PosDevice.getInstance().setTransProcessCode((byte) 0x01);
        } else {
            PosDevice.getInstance().setTransProcessCode((byte) 0x00);
        }

        startWithAmountActivity = repository.isStartWithAmountActivity();
        int selectedMerchantGroup = repository.getSelectedMerchantGroupId();

        if (repository.isQrSale()) {
            //get currency fro selected merchant
            repository.getCurrencyByMerchantId(4, currency -> {
                if (currency != null) {
                    AmountPresenter.this.currency = currency;
                    if (isViewNotNull()) {
                        mView.setMerchantCurrency(currency.getCurrencySymbol());
                    }
                } else {
                    if (isViewNotNull()) {
                        mView.showDataMissingError(Const.MSG_CURRENCY_NOT_FOUND);
                    }
                }
            });
        }else {
            repository.getEnabledMerchantsFromGroupId(selectedMerchantGroup, merchants -> {
                //select first merchant object from the group
                AmountPresenter.this.merchant = merchants.get(0);
                //get currency fro selected merchant
                repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                    if (currency != null) {
                        AmountPresenter.this.currency = currency;
                        if (isViewNotNull()) {
                            mView.setMerchantCurrency(currency.getCurrencySymbol());
                        }
                    } else {
                        if (isViewNotNull()) {
                            mView.showDataMissingError(Const.MSG_CURRENCY_NOT_FOUND);
                        }
                    }
                });
            });
        }
        repository.getTCT(tct -> {
            AmountPresenter.this.tct = tct;
            int len = addAmountSeparatorsLength(tct.getMaxTxnAmountLen());
            if (isViewNotNull()) {
                mView.setMaxLength(len);
            }
        });
    }

    @Override
    public void startProfileDownload() {
        log("startProfileDownload()");
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            repository.getTransactionCount(count -> {
                log("txn count: " + count);
                if (count >= 1) {
                    log("goto auto settle");
                    if (isViewNotNull()) {
                        mView.gotoAutoSettlementActivity();
                    }
                } else {
                    log("updating profile");
                    String profileData = repository.getProfileUpdateData();
                    if (!TextUtils.isEmpty(profileData)) {
                        log("profile data is exists");
                        ProfileUpdateHelper p = new ProfileUpdateHelper(repository);
                        if (isViewNotNull()) {
                            mView.showLoader("Please wait", "Updating profile");
                        }
                        p.setUpdateCompleteListener(new ProfileUpdateHelper.UpdateCompleteListener() {
                            @Override
                            public void onCompleted() {
                                log("profile update completed");
                                MyApp.getInstance().getBgThread().setProfileUpdateOngoing(false);
                                repository.saveHasPendingProfileUpdate(false);
                                repository.saveProfileUpdateData("");
                                if (isViewNotNull()) {
                                    mView.hideLoader();
                                    mView.onProfileUpdateCompleted();
                                }
                            }

                            @Override
                            public void onError() {
                                log("profile update error");
                                if (isViewNotNull()){
                                    mView.hideLoader();
                                    mView.showToastMessage("Profile update error!");
                                }
                            }
                        });
                        p.update(profileData);
                    } else {
                        log("profileData is null");
                    }
                }
            });
        } else {
            log("profile download cancelled - battery level critical: " + batteryLevel + "%");
        }
    }

    @Override
    public void generateConfigMap() {
        MyApp.getInstance().getBgThread().setGeneratingConfigMap(true);
        if (isViewNotNull()) {
            mView.showLoader("Sampath Bank", "Generating config map\nfor first time use.");
        }

        new ConfigMapTableHelper(repository).clearConfigMap(repository, Const.TABLES, () -> {
            new ConfigMapTableHelper(repository).insertDataToTable(repository, Const.TABLES, () -> {
                MyApp.getInstance().getBgThread().setGeneratingConfigMap(false);
                repository.saveConfigMapGenerated(true);
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.showToastMessage("ConfigMap generation completed.");
                }
            });
        });
    }

    @Override
    public void tryToAutoSettle() {
        log("tryToAutoSettle()");
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            repository.getTransactionCount(count -> {
                log("txn count: " + count);
                if (count >= 1) {
                    log("goto auto settle");
                    if (isViewNotNull()) {
                        mView.gotoAutoSettlementActivity();
                    }
                } else {
                    log("auto settle cancelled - not txn exists");
                    incrementAutoSettlementDate(nextSettlementDate ->
                            log("next settlement date: " + nextSettlementDate));
                    repository.saveHasPendingAutoSettlement(false);
                }
            });
        } else {
            log("auto settle cancelled - battery level critical: " + batteryLevel + "%");
            incrementAutoSettlementDate(nextSettlementDate ->
                    log("next settlement date: " + nextSettlementDate));
            repository.saveHasPendingAutoSettlement(false);
        }
    }

    private int addAmountSeparatorsLength(int len) {
        if (len == 12) {
            return len + 4;
        } else if (len == 11 || len == 10 || len == 9) {
            return len + 3;
        } else if (len == 8 || len == 7 || len == 6) {
            return len + 2;
        } else {
            return len + 1;
        }
    }

    @Override
    public void closeButtonPressed() {
        repository.saveForceLoadHome(true);
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void submitAmount() {
        if (!PosDevice.getInstance().isPaperExistsInPrinter()){
            mView.feedPaperIntoPinter();
            return;
        }

        long longAmount = Long.parseLong(amount.replaceAll(",", "")
                .replaceAll("\\.", ""));

        if (longAmount > Long.parseLong(tct.getMaxTxnAmount())) {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_MAX_AMOUNT_EXCEEDED);
            }
            return;
        }
        if (repository.isOfflineSale()) {
        if (longAmount > Long.parseLong(tct.getOfflineTranLimit())) {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_MAX_OFFLINE_AMOUNT_EXCEEDED);
            }
            return;
        }
        }

        if (repository.isQrSale()) {
            if (longAmount > Long.parseLong(tct.getMaxqramount())) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_MAX_QR_AMOUNT_EXCEEDED);
                }
                return;
            }
            if (longAmount < Long.parseLong(tct.getMinqramount())) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_MIN_QR_AMOUNT_EXCEEDED);
                }
                return;
            }
        }

        if (isViewNotNull()) {
            mView.setActionButtonEnabled(false);
        }
        repository.saveBaseAmount(amount);
        repository.saveTotalAmount(amount);

        if (repository.isCashBackSale()) {
            setPosDeviceData(longAmount);
            if (isViewNotNull()) {
                mView.gotoCashBackAmountActivity();
            }
        } else if (repository.isPreCompSale()) {
            if (isViewNotNull()) {
                mView.gotoTxnDetailActivity();
            }
        } else if (repository.isQrSale()) {
            if (isViewNotNull()) {
                mView.gotoQrSaleActivity();
            }
        } else if (repository.isStudentRefSale()) {
            setPosDeviceData(longAmount);
            if (isViewNotNull()) {
                mView.gotoStudentRefActivity();
            }
        } else {
            setPosDeviceData(longAmount);
            if (repository.isCardInitiatedSale()) {
                //sale is card initiated sale
                if (repository.isCardInitiatedSaleWithChip()) {
                    //chip
                    if (isViewNotNull()) {
                        mView.goToCardScanActivity();
                    }
                } else {
                    //swipe
                    if (isViewNotNull()) {
                        mView.gotoTxnDetailActivity();
                    }
                }
            } else {
                //sale is initiated from button
                if (repository.isManualSale()
                        || repository.isOfflineManualSale()
                        || repository.isPreAuthManualSale()
                        || repository.isRefundManualSale()
                        || repository.isQuasiCashManualFlow()) {
                    if (isViewNotNull()) {
                        mView.gotoManualSaleActivity();
                    }
                } else {
                    if (isViewNotNull()) {
                        mView.goToCardScanActivity();
                    }
                }
            }
        }
    }

    private void setPosDeviceData(long longAmount) {
        PosDevice.getInstance().setTxnAmount(longAmount);
        PosDevice.getInstance().setMerchantName(merchant.getMerchantName());
        PosDevice.getInstance().setMerchantId(merchant.getMerchantID());
        PosDevice.getInstance().setTransactionAid("");

        repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
            if (currency != null) {
                PosDevice.getInstance().setCurrencyCode(currency.getCurrencyCode());
            }
        });
    }

    private String getClearAmount() {
        return amount.replace(",", "");
    }

    private void validate() {
        if (!TextUtils.isEmpty(amount)) {
            try {
                double amount = Double.parseDouble(getClearAmount());
                if (amount > 0) {
                    hasValidAmount = true;
                    if (isViewNotNull()) {
                        mView.setActionButtonEnabled(true);
                    }
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        hasValidAmount = false;
        if (isViewNotNull()) {
            mView.setActionButtonEnabled(false);
        }
    }


    @Override
    public String getPinBlock() {
        return pinBlock;
    }

    @Override
    public boolean isMaxLenReach() {
        if (hasValidAmount) {
            int maxLen = addAmountSeparatorsLength(tct.getMaxTxnAmountLen());
            return amount.length() > maxLen;
        }
        return false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }


}