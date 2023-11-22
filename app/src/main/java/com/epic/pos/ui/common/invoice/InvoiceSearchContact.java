package com.epic.pos.ui.common.invoice;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-07-05
 */
public interface InvoiceSearchContact {

    interface View extends BaseView {
        void onInvoiceDataReceived(Transaction t, Issuer i, Merchant m, String maskedPan);
        void setConfirmEnabled(boolean isEnabled);
        void onShowError(String error);
        void onClearVoidUI();
        void gotoAmountActivity();
        void onDateReceived(String hostName, String merchantName);
        void onTxnSelected(Transaction transaction);
    }

    interface Presenter {
        void setHost(Host host);
        void setMerchant(Merchant merchant);
        void setTitle(String title);
        String getTitle();
        void clearVoid();
        void resetData();
        void setInvoiceNumber(String invoice);
        void onSubmit();
        void closeButtonPressed();
    }
}
