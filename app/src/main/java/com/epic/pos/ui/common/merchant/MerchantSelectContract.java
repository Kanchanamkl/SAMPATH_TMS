package com.epic.pos.ui.common.merchant;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.ui.BaseView;

import java.util.List;

public interface MerchantSelectContract {
    interface View extends BaseView {
        public void notifyListView();

        void setUpRecyclerView(List<Merchant> merchantList);

        void onMerchantSelected(Merchant selectedMerchant);

        void finishActivity();
    }

    interface Presenter {

        void setHost(Host host);
        void setMerchantType(MerchantType merchantType);
        void initData();
        void closeButtonPressed();
    }
}
