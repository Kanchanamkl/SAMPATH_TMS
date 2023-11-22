package com.epic.pos.ui.sale.cashbackamount;

import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-07-21
 */
public interface CashbackAmountContract {

    interface View extends BaseView {
        void setMerchantCurrency(String merchantCurrency);
        void setMaxLength(int length);
        void setActionButtonEnabled(boolean isEnabled);
        void goToCardScanActivity();
        void gotoManualSaleActivity();
        void gotoQrSaleActivity();
        void gotoTxnDetailActivity();
        void showDataMissingError(String msg);
    }

    interface Presenter {
        void setAmount(String amount);
        void initData();
        boolean isMaxLenReach();
        void submitAmount();
        String getTitle();
        String getPinBlock();
        void closeButtonPressed();
    }
}
