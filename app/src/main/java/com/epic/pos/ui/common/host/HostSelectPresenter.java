package com.epic.pos.ui.common.host;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import java.util.List;

import javax.inject.Inject;

public class HostSelectPresenter extends BasePresenter<HostSelectContract.View> implements HostSelectContract.Presenter {
    private Repository repository;
    private NetworkConnection networkConnection;
    private List<Host> hostList;
    private Host selectedHost;
    private boolean isListItemClicked = false;

    @Inject
    public HostSelectPresenter(Repository repository, NetworkConnection networkConnection) {
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

    public void getHostList() {
        repository.getHostList(hosts -> {
            if(repository.isVoidSale() || repository.isPreCompSale() ){
                hosts.remove(3);
            }
            hostList = hosts;
            mView.setUpRecyclerView(hosts);
        });
    }

    public void onListItemClicked(Host item) {
        if (isListItemClicked) return;
        isListItemClicked = true;

        selectedHost = item;
        for (Host _host : hostList) {
            _host.setSelected(false);
        }
        item.setSelected(true);
        mView.hostSelected(selectedHost);
    }

    public void onClickContinue() {
        if(selectedHost!=null){
            mView.hostSelected(selectedHost);
        }
    }
}