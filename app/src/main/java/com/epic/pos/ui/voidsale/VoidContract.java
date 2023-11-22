package com.epic.pos.ui.voidsale;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public interface VoidContract {

    interface View extends BaseView {
        void onInvoiceDataReceived(Transaction t, Issuer i, Merchant m, String maskedPan);
        void setConfirmEnabled(boolean isEnabled);
        void invalidInvoiceNumber();
        void onTxnFailed(String msg);
        void gotoVoidReceiptActivity();
        void onErrorAndClear(String title, String msg);
        void onClearVoidUI();
        void onDateReceived(String hostName, String merchantName);
        void showInAppError(String msg);
        void gotoReversalFailedActivity();
        void reversalValidationFailed(String error);
    }

    interface Presenter {
        void clearVoid();
        void resetData();
        void setInvoiceNumber(String invoice);
        void onSubmit();
    }
}
