package com.epic.pos.ui.settings.setup;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.ui.BaseView;

public interface SetUpContact {
    interface View extends BaseView {
        void onUpdateUi(boolean printClearIsoPacket);
        void onUpdateEncUi(boolean printEncIsoPacket);
        void onUpdateUiLogEnable(boolean isEnabled);
        void showHostSelectConfirmation(String msg);
    }

    interface Presenter {
        void updateUi();
        void printClearIsoPacket();
        void printEncIsoPacket();
        void onLogEnableClicked();
        void setSelectedHost(Host host);
        void setSelectedMerchant(Merchant merchant);
        void clearBatch();
        void generateConfigMap();
        void exportTransactions();
        void restoreTransactions(String endData);
    }
}
