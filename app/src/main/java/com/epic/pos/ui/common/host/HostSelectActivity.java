package com.epic.pos.ui.common.host;

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
import com.epic.pos.databinding.ActivityHostSelectBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.merchant.MerchantSelectActivity;

import java.util.List;

import butterknife.ButterKnife;

public class HostSelectActivity extends BaseActivity<HostSelectPresenter> implements HostSelectContract.View, HostListAdapter.OnClickListener {

    private final String TAG = HostSelectActivity.class.getSimpleName();
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String RESULT_HOST = "RESULT_HOST";

    private ActivityHostSelectBinding binding;
    private HostListAdapter mAdapter;

    public static void startActivityForResult(BaseActivity activity, String title, int requestCode) {
        Intent i = new Intent(activity, HostSelectActivity.class);
        i.putExtra(EXTRA_TITLE, title);
        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_select);
        ButterKnife.bind(this);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        setUpToolbarDefault((Toolbar) binding.toolbar, title, true);
        init();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void init() {
        mPresenter.getHostList();
        mPresenter.initData();
    }

    @Override
    protected void onCloseButtonPressed() {
        mPresenter.closeButtonPressed();
    }

    @Override
    public void onBackPressed() {
        mPresenter.closeButtonPressed();
        super.onBackPressed();
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
    public void hostSelected(Host selectedHost) {
        Intent intent = getIntent();
        intent.putExtra(RESULT_HOST, selectedHost);
        setResult(RESULT_OK, intent);
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