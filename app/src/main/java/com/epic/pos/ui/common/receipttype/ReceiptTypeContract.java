package com.epic.pos.ui.common.receipttype;

import com.epic.pos.ui.BaseView;

public interface ReceiptTypeContract {
    interface View extends BaseView {

    }

    interface Presenter {
        void initData();
        void closeButtonPressed();
        void customercopyclicked();
        void merchantcopyclicked();
    }
}
