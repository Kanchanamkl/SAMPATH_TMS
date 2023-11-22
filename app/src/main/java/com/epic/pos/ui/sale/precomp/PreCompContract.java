package com.epic.pos.ui.sale.precomp;

import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public interface PreCompContract {

    interface View extends BaseView {
        void onInvoiceDataReceived(Transaction t, Issuer i, Merchant m, String maskedPan);
        void setConfirmEnabled(boolean isEnabled);
        void onShowError(String error);
        void onClearVoidUI();
        void gotoAmountActivity();
        void onDateReceived(String hostName, String merchantName);
    }

    interface Presenter {
        String getTitle();
        void clearVoid();
        void resetData();
        void setInvoiceNumber(String invoice);
        void onSubmit();
        void closeButtonPressed();
    }
}
