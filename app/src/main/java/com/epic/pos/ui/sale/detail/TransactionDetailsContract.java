package com.epic.pos.ui.sale.detail;

import com.epic.pos.ui.BaseView;

/**
 * TransactionDetailsContract
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-30
 */
public interface TransactionDetailsContract {
    interface View extends BaseView {
        void onUpdateUi(String currency, String amount, String cardNo, String cardType, String expDate);
        void gotoSignaturePad();
        void gotoReceiptActivity();
        void onTxnFailed(String msg);
        void showTxnProgress();
        void hideTxnProgress();
        void setSubmitButtonEnabled(boolean isEnabled);

        void gotoReversalFailedActivity();
        void reversalValidationFailed(String errMsg);
        void finishTxnDetails();
    }

    interface Presenter {
        String getTitle();
        void initData();
        void saleRequest();
        void closeButtonPressed();
        void onResume();
        void onPause();
        void onUserInteraction();
    }
}
