package com.epic.pos.ui.voidsale.receipt;

import android.graphics.Bitmap;

import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public interface VoidReceiptContact {

    interface View extends BaseView {
        void onCustomerReceiptGenerated(Bitmap bitmap);
        void onCustomerReceiptPrinted();
        void onMerchantCopyPrintError(String msg);
        void onCustomerCopyPrintError(String msg);
        void setPrintButtonEnabled(boolean isEnabled);
    }

    interface Presenter {
        void initData();
        void printCustomerCopy();
        void retryToPrintMerchantCopy();
        void retryToPrintCustomerCopy();
    }

}
