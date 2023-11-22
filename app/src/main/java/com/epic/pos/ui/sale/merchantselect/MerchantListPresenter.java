package com.epic.pos.ui.sale.merchantselect;

import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class MerchantListPresenter extends BasePresenter<MerchantListContract.View> implements MerchantListContract.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;
    private boolean isReturnResult =false;

    @Inject
    public MerchantListPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void initData() {
        repository.saveTransactionOngoing(true);
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void setReturnResult(boolean isReturnResult) {
        this.isReturnResult = isReturnResult;
    }

    @Override
    public void getMerchants() {
        repository.getEnabledMerchants(repository.isInstallmentSale(), merchants -> mView.setUpRecyclerView(merchants));
    }

    @Override
    public void onMerchantClicked(Merchant merchant) {
        repository.saveSelectedMerchantGroupId(merchant.getGroupId());

        if (isReturnResult){
            mView.returnResult();
        }else {
            mView.gotoAmountActivity();
        }
    }

}