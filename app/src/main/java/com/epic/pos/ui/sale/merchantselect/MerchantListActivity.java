package com.epic.pos.ui.sale.merchantselect;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.content.Intent;
import android.os.Bundle;

import com.epic.pos.R;
import com.epic.pos.adapter.MerchantAdapter;
import com.epic.pos.adapter.MerchantListAdapter;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.databinding.ActivityMerchantListBinding;
import com.epic.pos.domain.entity.TerminalEntity;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.sale.amount.AmountActivity;

import java.util.List;

import butterknife.ButterKnife;

public class MerchantListActivity extends BaseActivity<MerchantListPresenter> implements MerchantListContract.View, MerchantAdapter.OnClickListener {

    private final String TAG = MerchantListActivity.class.getSimpleName();
    public static final String RESULT_HOST = "RESULT_HOST";
    public static final String EXTRA_RETURN_RESULT = "RESULT_RETURN_RESULT";
    private ActivityMerchantListBinding binding;
    private boolean shouldReturnResult = false;

    public static void startActivity(BaseActivity activity) {
        Intent i = new Intent(activity, MerchantListActivity.class);
        i.putExtra(EXTRA_RETURN_RESULT, false);
        activity.startActivity(i);
    }

    public static void startActivityForResult(BaseActivity activity, int requestCode) {
        Intent i = new Intent(activity, MerchantListActivity.class);
        i.putExtra(EXTRA_RETURN_RESULT, true);
        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_merchant_list);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
        mPresenter.setReturnResult(getIntent().getBooleanExtra(EXTRA_RETURN_RESULT, false));
        mPresenter.initData();
        mPresenter.getMerchants();
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
    public void setUpRecyclerView(List<Merchant> merchants) {
        MerchantAdapter mAdapter = new MerchantAdapter(merchants,this);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onTerminalClicked(Merchant merchant) {
        binding.recyclerView.setClickable(false);
        mPresenter.onMerchantClicked(merchant);
    }

    @Override
    public void returnResult() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void gotoAmountActivity() {
        AmountActivity.startActivity(this);
        finish();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

}