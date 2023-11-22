package com.epic.pos.ui.sale.cardscan;

import android.widget.LinearLayout;

import com.epic.pos.data.DccData;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.ui.BaseView;

import java.util.List;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-10
 */
public interface CardScanContract {

    interface View extends BaseView {
        void onUpdateUi(String currency, String amount, String checkCardStatus);
        void gotoNoRetryFailedActivity(String title, String msg);
        void gotoTxnDetailActivity();
        void gotoApprovalCodeActivity();
        void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList);
        void onDCCDataSELECT(List<DccData> dccdata);
        void onMultiApplicationCard(List<String> applicationList);
        void onCDTError();
        void showDataMissingError(String msg);
        void finishCardScanActivity();

    }

    interface Presenter {
        String getTitle();
        void resetData();
        void updateUi();
        void checkCard();
        void onCDTSelected(CardDefinition cardDefinition);
        void onDCCselected(DccData dccData);
        void onCardApplicationSelected(int index);
        void closeButtonPressed();
        void onResume();
        void onPause();
        void onDestroy();
        void startProcessThread();
        void startAfterScanCountDown();
    }
}
