package com.epic.pos.ui.settings.keyinject.host;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.epic.pos.R;
import com.epic.pos.adapter.HostListAdapter;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.databinding.ActivityHostSelectForDebitKeyInjectBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.password.PasswordActivity;
import com.epic.pos.ui.settings.keyinject.key.KeyInjectActivity;

import java.util.List;

import butterknife.ButterKnife;

public class HostSelectActivity extends BaseActivity<HostSelectPresenter> implements HostSelectContract.View, HostListAdapter.OnClickListener {

    private final String TAG = "HostSelectActivity";
    private ActivityHostSelectForDebitKeyInjectBinding binding;
    private HostListAdapter mAdapter;
    public static boolean isTLE;

    public static void startActivity(BaseActivity activity) {
        isTLE = false;
        activity.startActivity(new Intent(activity, HostSelectActivity.class));
    }

    public static void startActivityForTLE(BaseActivity activity) {
        isTLE = true;
        activity.startActivity(new Intent(activity, HostSelectActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_select_for_debit_key_inject);
        ButterKnife.bind(this);

        if(isTLE){
            setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_tle_key_download), true);
        }else{
            setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.debit_key_inject), true);
        }
        init();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void init() {
        mPresenter.getHostList();
    }

    @Override
    public void setUpRecyclerView(List<Host> hostList) {
        mAdapter = new HostListAdapter(hostList, this);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void notifyListView() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void gotoKeyInjectActivity(Host host) {
        KeyInjectActivity.startActivity(this, host);
        finish();
    }

    @Override
    public void startPasswordActivity(Host selectedHost) {
        PasswordActivity.startActivityForTLE(this, selectedHost);
        finish();
    }

    @Override
    public void onListItemClicked(Host item) {
        mPresenter.onListItemClicked(item);
    }

    public void onClickContinue(View view) {
        mPresenter.onClickContinue();
    }
}