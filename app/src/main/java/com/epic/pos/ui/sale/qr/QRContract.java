package com.epic.pos.ui.sale.qr;

import android.content.Context;

import com.epic.pos.ui.BaseView;

/**
 * @author buddhika_j
 * @version 1.0
 * @since 2023-09-11
 */
public interface QRContract {

    interface View extends BaseView {

        void loadqrcode(String qrstring);

        void onTxnFailed(String errorMsg,String msg);

        void onTxnStillPending(String errorMsg,String msg);

        void gotoReceiptActivity();
    }

    interface Presenter {
        void initData(Context c);
    }
}
