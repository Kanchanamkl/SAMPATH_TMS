package com.epic.pos.ui.common.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.epic.pos.R;
import com.epic.pos.adapter.MerchantListAdapter;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.databinding.ActivityMerchantSelectBinding;
import com.epic.pos.ui.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class MerchantSelectActivity extends BaseActivity<MerchantSelectPresenter> implements MerchantSelectContract.View, MerchantListAdapter.OnClickListener {

    private final String TAG = "MerchantSelectActivity";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_SELECTED_HOST = "EXTRA_SELECTED_HOST";
    private static final String EXTRA_MERCHANT_TYPE = "EXTRA_MERCHANT_TYPE";
    public static final String RESULT_MERCHANT = "RESULT_MERCHANT";

    private ActivityMerchantSelectBinding binding;
    private MerchantListAdapter mAdapter;

    public static void startActivityForResult(BaseActivity activity, Host host, String title, MerchantType merchantType, int requestCode) {
        Intent i = new Intent(activity, MerchantSelectActivity.class);
        i.putExtra(EXTRA_SELECTED_HOST, host);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_MERCHANT_TYPE, merchantType.getVal());
        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_merchant_select);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getIntent().getStringExtra(EXTRA_TITLE), true);
        getExtras();
        init();
    }

    private void getExtras() {
        mPresenter.setHost(getIntent().getParcelableExtra(EXTRA_SELECTED_HOST));
        int mTypeVal = getIntent().getIntExtra(EXTRA_MERCHANT_TYPE, MerchantType.ALL.getVal());
        mPresenter.setMerchantType(MerchantType.valueOf(mTypeVal));
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void init() {
        binding.etHost.setText(mPresenter.getHost().getHostName());
        mPresenter.initMerchantList();
        mPresenter.initData();
    }

    public void onClickContinue(View view) {
        mPresenter.onClickContinue();
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
    public void setUpRecyclerView(List<Merchant> merchantList) {
        mAdapter = new MerchantListAdapter(merchantList, this);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onMerchantSelected(Merchant selectedMerchant) {
        Intent intent = getIntent();
        intent.putExtra(RESULT_MERCHANT, selectedMerchant);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void onListItemClicked(Merchant item) {
        mPresenter.onListItemClicked(item);
    }

    @Override
    public void notifyListView() {
        mAdapter.notifyDataSetChanged();
    }

    @OnTextChanged(value = R.id.etSearch,
            callback = OnTextChanged.Callback.TEXT_CHANGED)
    protected void afterSearchChanged(CharSequence s, int start, int before, int count) {
        if (mAdapter != null)
            mAdapter.getFilter().filter(s);
    }
}