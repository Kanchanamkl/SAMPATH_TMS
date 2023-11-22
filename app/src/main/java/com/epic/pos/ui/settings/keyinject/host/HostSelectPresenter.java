package com.epic.pos.ui.settings.keyinject.host;

import static com.epic.pos.ui.settings.keyinject.host.HostSelectActivity.isTLE;

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

    @Inject
    public HostSelectPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    public void getHostList() {
        repository.getHostList(hosts -> {
            hosts.remove(3);
            hosts.remove(2);
            hostList = hosts;
            mView.setUpRecyclerView(hosts);
        });
    }

    public void onListItemClicked(Host item) {
        selectedHost = item;
        for (Host _host : hostList) {
            _host.setSelected(false);
        }
        item.setSelected(true);
        mView.notifyListView();
    }

    public void onClickContinue() {
        if(selectedHost!=null){

            if(isTLE){
                if(selectedHost.getTLEEnabled() == 1) {
                    mView.startPasswordActivity(selectedHost);
                }else{
                    mView.showToastMessage("Please enable TLE");
                }
            }else{
                mView.gotoKeyInjectActivity(selectedHost);
            }
        }
    }
}
