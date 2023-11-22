package com.epic.pos.ui.newsettlement.auto;

import com.epic.pos.device.listener.AutoSettlementListener;
import com.epic.pos.ui.BaseView;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-11-01
 */
public interface AutoSettleContact {

    interface View extends BaseView {
        void onDetailReportPrintError(String error);

        void onSettlementReportPrintError(String error);

        void onSettlementFailed(String error);

        void onAutoSettlementCompleted();

        void onSettlementStateUpdate(String msg);
    }

    interface Presenter {

        void initAutoSettlement();

        void closeButtonPressed();

        void rePrintDetailReport();

        void rePrintSettlementReport();
    }

}
