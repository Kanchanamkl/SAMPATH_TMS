package com.epic.pos.ui.newsettlement.settle;

import android.graphics.Bitmap;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-10-22
 */
public interface SettleActivityContact {

    interface View extends BaseView {
        void showTransactionNotFound();

        void showSettlementAmount(String currencySymbol, long totalSaleAmount);

        void showPrintDetailReportDialog();

        void showPreAuthDeleteConfirmationDialog();

        void onSettlementCompleted();

        void onSettlementFailed(String msg);

        void detailReportPrintError(String error);

        void settlementReportPrintError(String error);

        void onSettlementStateUpdate(String msg);

    }

    interface Presenter {

        void initSettlement();

        void onPreAuthDeleteClicked();

        void onPreAuthKeepClicked();

        void onDetailReportPrintClicked();

        void onDoNotPrintDetailReportClicked();

        void rePrintDetailReport();

        void rePrintSettlementReport();

        void onSettleClicked();


        void setSelectedHost(Host host);

        void setSelectedMerchant(Merchant merchant);

        Host getSelectedHost();

        Merchant getSelectedMerchant();

        void closeButtonPressed();

        void onDestroy();
    }

}
