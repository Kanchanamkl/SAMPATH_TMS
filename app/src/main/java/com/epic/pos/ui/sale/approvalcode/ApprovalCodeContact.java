package com.epic.pos.ui.sale.approvalcode;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.ui.BaseView;

import java.util.List;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-04-02
 */
public interface ApprovalCodeContact {

    interface View extends BaseView {
        void setActionBtnEnabled(boolean isEnabled);
        void gotoTxnDetailActivity();
    }

    interface Presenter {
        String getTitle();
        void setApprovalCode(String approvalCode);
        void onSubmit();
        void closeButtonPressed();
    }
}
