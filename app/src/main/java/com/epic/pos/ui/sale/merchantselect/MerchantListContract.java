package com.epic.pos.ui.sale.merchantselect;

import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.entity.TerminalEntity;
import com.epic.pos.ui.BaseView;

import java.util.List;

public interface MerchantListContract {
    interface View extends BaseView {
        void setUpRecyclerView(List<Merchant> merchants);
        void gotoAmountActivity();
        void returnResult();
    }

    interface Presenter {
        void initData();
        void getMerchants();
        void onMerchantClicked(Merchant merchant);
        String getTitle();
        void setReturnResult(boolean isReturnResult);
        void closeButtonPressed();
    }
}
