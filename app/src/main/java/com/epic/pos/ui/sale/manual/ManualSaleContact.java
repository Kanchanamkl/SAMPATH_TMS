package com.epic.pos.ui.sale.manual;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.ui.BaseView;

import java.util.List;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-05
 */
public interface ManualSaleContact {

    interface View extends BaseView {
        void setActionBtnEnabled(boolean isEnabled);
        void onCDTError();
        void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList);
        void showDataMissingError(String msg);
        void showValidationError(String msg);
        void gotoNoRetryFailedActivity(String title, String msg);
        void gotoTxnDetailActivity();
        void showApprovalCode();
    }

    interface Presenter {
        String getTitle();
        void init();
        void setExpireDate(String expireDate);
        void setCardNumber(String cardNumber);
        void setApprovalCode(String approvalCode);
        void onCDTSelected(CardDefinition cardDefinition);
        void onSubmit();
        void closeButtonPressed();
    }
}
