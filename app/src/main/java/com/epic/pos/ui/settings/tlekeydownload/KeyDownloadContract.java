package com.epic.pos.ui.settings.tlekeydownload;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.ui.BaseView;

public interface KeyDownloadContract {

    interface View extends BaseView {
        void onTxnFailedAndRetry(String msg);
        void onTxnFailed(String msg);
        void onSuccessKeyDownload();
    }

    interface Presenter {
        void selectApp();
        void getSerialNumber();
        void getPinVerificationMode();
        void pinVerification();
        void getPinCounter();
        void getMac();
        String getEncryptedMethod();
        void retryTransaction();
        String getClearKeys(String encryptedMethod, String key);
        void keyDownloadRequest();
        void waitCardInsert(Host selectedHost);
        void onClosing();
    }
}
