package com.epic.pos.ui.sale.amount;

import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-10
 */
public interface AmountContract {

    interface View extends BaseView {
        void setMerchantCurrency(String merchantCurrency);
        void setMaxLength(int length);
        void setActionButtonEnabled(boolean isEnabled);
        void goToCardScanActivity();
        void gotoManualSaleActivity();
        void gotoQrSaleActivity();
        void gotoTxnDetailActivity();
        void gotoCashBackAmountActivity();
        void gotoStudentRefActivity();
        void gotoAutoSettlementActivity();
        void showDataMissingError(String msg);
        void onProfileUpdateCompleted();
        void feedPaperIntoPinter();
        void restartActivity();
    }

    interface Presenter {
        void setAmount(String amount);
        void initData();
        boolean isMaxLenReach();
        void submitAmount();
        String getTitle();
        String getPinBlock();
        void closeButtonPressed();
        void tryToAutoSettle();
        void startProfileDownload();
        void generateConfigMap();
    }
}
