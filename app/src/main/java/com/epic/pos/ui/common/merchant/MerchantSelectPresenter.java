package com.epic.pos.ui.common.merchant;

import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.entity.TerminalEntity;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MerchantSelectPresenter extends BasePresenter<MerchantSelectContract.View> implements MerchantSelectContract.Presenter {
    private Repository repository;
    private NetworkConnection networkConnection;
    private Host host;
    private List<Merchant> merchantList;
    private Merchant selectedMerchant;
    private MerchantType merchantType;


    @Inject
    public MerchantSelectPresenter(Repository repository, NetworkConnection networkConnection) {
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
    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public void setMerchantType(MerchantType merchantType) {
        this.merchantType = merchantType;
    }

    public Host getHost() {
        return host;
    }

    public void onListItemClicked(Merchant item) {
        selectedMerchant = item;
        for (Merchant _merchant : merchantList) {
            _merchant.setSelected(false);
        }
        item.setSelected(true);
        mView.notifyListView();
    }

    public void onClickContinue() {
        if (selectedMerchant != null)
            mView.onMerchantSelected(selectedMerchant);
    }

    private void onMerchantsSelected(List<Merchant> merchants){
        MerchantSelectPresenter.this.merchantList = merchants;
        mView.setUpRecyclerView(merchantList);
    }

    public void initMerchantList() {
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            List<Merchant> ms = new ArrayList<>();

            if (merchantType == MerchantType.ALL){
                onMerchantsSelected(merchants);
            }else if (merchantType == MerchantType.SALE_SUPPORT_MERCHANTS){
                for(Merchant m : merchants){
                    if (m.getIsInstallment() == 0){
                        ms.add(m);
                    }
                }

                onMerchantsSelected(ms);
            }else if (merchantType == MerchantType.INSTALLMENT_SUPPORT_MERCHANTS){
                for(Merchant m : merchants){
                    if (m.getIsInstallment() == 1){
                        ms.add(m);
                    }
                }
                onMerchantsSelected(ms);
            }
        });
    }
}