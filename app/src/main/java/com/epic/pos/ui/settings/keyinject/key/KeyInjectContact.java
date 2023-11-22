package com.epic.pos.ui.settings.keyinject.key;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.ui.BaseView;

public interface KeyInjectContact {
    interface View extends BaseView {
        void setKeyStatus(String status);
        void setActionEnabled(boolean isEnable);
        void setActionVisible(boolean isVisible);
        void setStatusIconVisible(boolean isVisible);
        void keyInjectSuccess();
    }

    interface Presenter {
        void setHost(Host host);
        void openSerialPort();
        void tempKeyInject(String key);
    }
}
