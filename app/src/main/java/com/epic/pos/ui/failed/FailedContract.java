package com.epic.pos.ui.failed;

import com.epic.pos.ui.BaseView;

public interface FailedContract {
    interface View extends BaseView {
        void onCountDownFinished();
    }

    interface Presenter {
        void closeButtonPressed();
        void startCountDown();
    }
}
