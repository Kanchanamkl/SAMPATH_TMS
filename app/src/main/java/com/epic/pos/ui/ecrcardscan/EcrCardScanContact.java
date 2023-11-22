package com.epic.pos.ui.ecrcardscan;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.ui.BaseView;

import java.util.List;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-09-08
 */
public interface EcrCardScanContact {

    interface View extends BaseView {
        void gotoNoRetryFailedActivity(String title, String msg);
        void onMultiApplicationCard(List<String> applicationList);
        void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList);
        void onCDTError();
        void showDataMissingError(String msg);
        void finishCardScanActivity();
        void setState(String state);
        void gotoCardScanActivity();
    }

    interface Presenter {
        void initEcr();
        void checkCard();
        void onCardApplicationSelected(int index);
        void onCDTSelected(CardDefinition cardDefinition);

        void onResume();
        void onPause();
        void closeButtonPressed();
    }
}
