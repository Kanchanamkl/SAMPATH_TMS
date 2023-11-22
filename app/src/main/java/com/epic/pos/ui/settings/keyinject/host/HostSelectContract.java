package com.epic.pos.ui.settings.keyinject.host;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.ui.BaseView;

import java.util.List;

public interface HostSelectContract {
    interface View extends BaseView {
        void setUpRecyclerView(List<Host> hostList);
        void notifyListView();
        void gotoKeyInjectActivity(Host host);
        void startPasswordActivity(Host selectedHost);
    }

    interface Presenter {
    }
}
