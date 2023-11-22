package com.epic.pos.ui.sale.receipt;

import android.graphics.Bitmap;

import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-12
 */
public interface ReceiptContact {

    interface View extends BaseView {
        void onCustomerReceiptGenerated(Bitmap bitmap, Transaction txn, boolean isTxnDisabled);

        void onCustomerReceiptPrinted();

        void onMerchantCopyPrintError(String msg);

        void onCustomerCopyPrintError(String msg);

        void setActionButtonEnabled(boolean isEnabled);

        void onReceiptGenerationError(String msg);

        void setUiVisible();

        void onfinish();
    }

    interface Presenter {
        String getTitle();

        void initData();

        void printCustomerCopy();

        void retryToPrintMerchantCopy();

        void retryToPrintCustomerCopy();

        void closeButtonPressed();

        void CheckReceiptPrintWithECR();
    }

}
