package com.epic.pos.ui.sale.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.common.Const;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityTransactionDetailsBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.receipt.ReceiptActivity;
import com.epic.pos.ui.sale.signature.SignatureActivity;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionDetailsActivity extends BaseActivity<TransactionDetailsPresenter>
        implements TransactionDetailsContract.View {

    private ActivityTransactionDetailsBinding binding;

    @BindString(R.string.msg_txn_failed)
    String msg_txn_failed;
    @BindString(R.string.msg_processing)
    String msg_processing;
    @BindString(R.string.msg_txn_processing_msg)
    String msg_txn_processing_msg;
    @BindString(R.string.btn_ok)
    String btn_ok;
    @BindString(R.string.msg_reversal_failed)
    String msg_reversal_failed;
    @BindString(R.string.msg_reversal_failed_msg)
    String msg_reversal_failed_msg;

    public static void startActivity(BaseActivity activity, String pinBlock) {
        Intent intent = new Intent(activity, TransactionDetailsActivity.class);
        intent.putExtra(Const.KEY_PIN_BLOCK, pinBlock);
        activity.startActivity(intent);
    }

    public static void startActivity(BaseActivity activity) {
        Intent intent = new Intent(activity, TransactionDetailsActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_details);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), false);
        getExtras();
        mPresenter.initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void onCloseButtonPressed() {
        mPresenter.closeButtonPressed();
    }

    @Override
    public void finishTxnDetails() {
        mPresenter.closeButtonPressed();
        finish();
    }

    @Override
    public void onBackPressed() {
        mPresenter.closeButtonPressed();
        super.onBackPressed();
    }

    @Override
    public void gotoReversalFailedActivity() {
        FailedActivity.startActivity(msg_reversal_failed, msg_reversal_failed_msg, btn_ok, this);
        finish();
    }

    @Override
    public void reversalValidationFailed(String errMsg) {
        FailedActivity.startActivity(msg_reversal_failed, errMsg, btn_ok, this);
        finish();
    }

    @Override
    public void onTxnFailed(String msg) {
        FailedActivity.startActivity(msg_txn_failed, msg, btn_ok, this);
        finish();
    }

    @Override
    public void gotoReceiptActivity() {
        startActivity(new Intent(this, ReceiptActivity.class));
        finish();
    }

    @Override
    public void gotoSignaturePad() {
        SignatureActivity.startActivity(this);
        finish();
    }

    @Override
    public void showTxnProgress() {
        showLoader(msg_processing, msg_txn_processing_msg);
    }

    @Override
    public void hideTxnProgress() {
        hideLoader();
    }

    private void getExtras() {
        mPresenter.setPinBlock(getIntent().getStringExtra(Const.KEY_PIN_BLOCK));
    }

    @Override
    public void onUpdateUi(String currency, String amount, String cardNo, String cardType, String expDate) {
        binding.tvAmountTitle.setText("Amount (" + currency + ")");
        binding.tvAmount.setText(amount);
        binding.tvCardNum.setText(cardNo);
        binding.tvCardType.setText(cardType);
        binding.tvExpDate.setText(expDate);
    }

    @Override
    public void setSubmitButtonEnabled(boolean isEnabled) {
        binding.btnContinue.setEnabled(isEnabled);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        mPresenter.onUserInteraction();
    }

    @OnClick(R.id.btnContinue)
    void onContinueClicked(View view){
        mPresenter.saleRequest();
    }

    @OnClick(R.id.btnCancel)
    void onCancelClicked(View view){
        mPresenter.closeButtonPressed();
        finish();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }
}