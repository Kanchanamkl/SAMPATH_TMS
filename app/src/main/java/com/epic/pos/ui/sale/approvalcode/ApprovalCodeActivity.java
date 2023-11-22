package com.epic.pos.ui.sale.approvalcode;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityApprovalCodeBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class ApprovalCodeActivity extends BaseActivity<ApprovalCodePresenter> implements ApprovalCodeContact.View {

    private final String TAG = ApprovalCodeActivity.class.getSimpleName();
    private ActivityApprovalCodeBinding binding;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, ApprovalCodeActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_approval_code);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
        disableCopyPaste(binding.etApprovalCode);
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

    @OnTextChanged(value = R.id.etApprovalCode, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onApprovalCodeChanged(Editable s) {
        mPresenter.setApprovalCode(s.toString());
    }

    @Override
    public void gotoTxnDetailActivity() {
        TransactionDetailsActivity.startActivity(this);
        finish();
    }

    @Override
    public void setActionBtnEnabled(boolean isEnabled) {
        binding.btnContinue.setEnabled(isEnabled);
    }

    @OnClick(R.id.btnContinue)
    void btnContinueClicked() {
        mPresenter.onSubmit();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


}