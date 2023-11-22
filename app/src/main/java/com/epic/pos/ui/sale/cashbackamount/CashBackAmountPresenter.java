package com.epic.pos.ui.sale.cashbackamount;

import android.text.TextUtils;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-07-21
 */
public class CashBackAmountPresenter extends BasePresenter<CashbackAmountContract.View> implements CashbackAmountContract.Presenter {

    private final String TAG = CashBackAmountPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private String amount = "";
    private boolean hasValidAmount = false;
    private String pinBlock = null;

    private Merchant merchant;
    private Currency currency;
    private boolean startWithAmountActivity = false;
    private TCT tct;

    @Inject
    public CashBackAmountPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
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
        startWithAmountActivity = repository.isStartWithAmountActivity();
        int selectedMerchantGroup = repository.getSelectedMerchantGroupId();
        repository.getEnabledMerchantsFromGroupId(selectedMerchantGroup, merchants -> {
            //select first merchant object from the group
            CashBackAmountPresenter.this.merchant = merchants.get(0);
            //get currency fro selected merchant
            repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                if (currency != null) {
                    CashBackAmountPresenter.this.currency = currency;
                    mView.setMerchantCurrency(currency.getCurrencySymbol());
                } else {
                    mView.showDataMissingError(Const.MSG_CURRENCY_NOT_FOUND);
                }
            });
        });

        repository.getTCT(tct -> {
            CashBackAmountPresenter.this.tct = tct;
            int len = addAmountSeparatorsLength(tct.getMaxTxnAmountLen());
            mView.setMaxLength(len);
        });
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


    public double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }

    @Override
    public void submitAmount() {
        long longAmount = Long.parseLong(amount.replaceAll(",", "")
                .replaceAll("\\.", ""));

        if (longAmount > Long.parseLong(tct.getMaxTxnAmount())) {
            mView.showToastMessage(Const.MSG_MAX_AMOUNT_EXCEEDED);
            return;
        }

        double cashBackPercentage = Double.parseDouble(tct.getCashBackPercentage());
        double saleAmount = Double.parseDouble(repository.getBaseAmount().replaceAll(",", ""));
        double maxCashBackAmount = (saleAmount / 100 * cashBackPercentage);
        double cashBackAmount = Double.parseDouble(amount.replaceAll(",", ""));

        if (maxCashBackAmount < cashBackAmount){
            String msg = Const.MSG_CASH_BACK_AMOUNT_EXCEEDED.replace("#percentage#", tct.getCashBackPercentage());
            mView.showToastMessage(msg);
            return;
        }

        mView.setActionButtonEnabled(false);

        double totalAmountDouble = saleAmount + cashBackAmount;
        String totalAmount = String.format("%.2f", totalAmountDouble);
        long totalLong = Long.parseLong(totalAmount.replace(".", ""));
        String formattedTotalAmount = Utility.getFormattedAmount(totalLong);

        repository.saveCashBackAmount(amount);
        repository.saveTotalAmount(formattedTotalAmount);


        PosDevice.getInstance().setCashBackAmount(longAmount);

        mView.goToCardScanActivity();
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
                    mView.setActionButtonEnabled(true);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        hasValidAmount = false;
        mView.setActionButtonEnabled(false);
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


}