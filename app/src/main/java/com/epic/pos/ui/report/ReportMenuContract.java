package com.epic.pos.ui.report;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.ui.BaseView;

public interface ReportMenuContract{
    interface View extends BaseView {

        void selectMerchantForAnyReceipt(Host host);
        void selectInvoiceForAnyReceipt(Host host, Merchant merchant);

        void selectMerchantForDetailReport(Host host);

        void selectMerchantForSummaryReport(Host host);

        void selectMerchantForLastSettlementReport(Host host);

        void selectMerchantForHostInfo(Host host);

        void gotoReceiptTypeSelectActivity();
    }

    interface Presenter {

        void setHostForHostInfoReport(Host host);
        void setMerchantForHostInfoReport(Merchant merchant);

        void setHostForLastSettlementReport(Host host);
        void setMerchantForLastSettlementReport(Merchant merchant);

        void setHostForSummaryReport(Host host);
        void setMerchantForSummaryReport(Merchant merchant);

        void setHostForDetailReport(Host host);
        void setMerchantForDetailReport(Merchant merchant);

        void setHostForAnyReceipt(Host host);
        void setMerchantForAnyReceipt(Merchant merchant);
        void setTransactionForAnyReceipt(Transaction transaction);

        void printLastSettlementReport();
        void printLastReceipt();
        void onExitApp();

    }
}
