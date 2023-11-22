package com.epic.pos.ui.sale.signature;

import android.graphics.Bitmap;

import com.epic.pos.ui.BaseView;

public interface SignatureContact {

    interface View extends BaseView {
        void gotoReceiptActivity();
        void updateUi(String currency, String amount);
        void setConfirmBtnEnabled(boolean isEnabled);
    }

    interface Presenter {
        void initData();
        void saveSignature(Bitmap bitmap);
    }

}
