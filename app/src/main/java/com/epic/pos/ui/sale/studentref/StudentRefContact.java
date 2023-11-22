package com.epic.pos.ui.sale.studentref;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.ui.BaseView;

import java.util.List;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-05
 */
public interface StudentRefContact {

    interface View extends BaseView {
        void setContinueBtnEnabled(boolean isEnabled);
        void goToCardScanActivity();
    }

    interface Presenter {
        String getTitle();

        void init();

        void onSubmit();

        void closeButtonPressed();

        void setStudentRef(String studentRef);
    }
}
