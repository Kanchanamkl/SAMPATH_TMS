package com.epic.pos.ui.sale.cashbackamount;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import com.epic.pos.util.AppLog;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityCashbackAmountBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.sale.cardscan.CardScanActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;
import com.epic.pos.ui.sale.manual.ManualSaleActivity;
import com.epic.pos.ui.sale.qr.QrActivity;
import com.epic.pos.util.EditTextWatcher;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CashbackAmountActivity extends BaseActivity<CashBackAmountPresenter> implements CashbackAmountContract.View, View.OnClickListener {

    private static final String TAG = CashbackAmountActivity.class.getSimpleName();
    private ActivityCashbackAmountBinding binding;

    @BindString(R.string.data_error)
    String data_error;
    @BindString(R.string.btn_ok)
    String btn_ok;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, CashbackAmountActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cashback_amount);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
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
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void setMaxLength(int length) {
        log("setMaxLength() length:" + length);
        binding.tvAmount.addTextChangedListener(new EditTextWatcher(binding.tvAmount, length));
        initViews();
    }

    @Override
    public void showDataMissingError(String msg) {
        showDialogError(data_error, msg, this::finish);
    }

    @Override
    public void setActionButtonEnabled(boolean isEnabled) {
        binding.btnContinue.setEnabled(isEnabled);
    }



    public String getInputText() {
        return binding.tvAmount.getText().toString();
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null && "number_button".equals(view.getTag())) {
            if (!mPresenter.isMaxLenReach()) {
                binding.tvAmount.append(((TextView) view).getText());
            } else {
                return;
            }
        } else {
            switch (view.getId()) {
                case R.id.ivReload: { // handle clear button
                    binding.tvAmount.setText("0.00");
                    break;
                }
                case R.id.ivDelete: { // handle backspace button
                    // delete one character
                    Editable editable = (Editable) binding.tvAmount.getText();
                    int charCount = editable.length();
                    if (charCount > 0) {
                        editable.delete(charCount - 1, charCount);
                    }
                    break;
                }
            }
        }

        mPresenter.setAmount(getInputText());
    }

    @Override
    public void setMerchantCurrency(String merchantCurrency) {
        binding.tvCurrency.setText(merchantCurrency);
    }

    @Override
    public void gotoManualSaleActivity() {
        startActivity(new Intent(this, ManualSaleActivity.class));
        finish();
    }

    @Override
    public void goToCardScanActivity() {
        startActivity(new Intent(this, CardScanActivity.class));
        finish();
    }

    @Override
    public void gotoQrSaleActivity() {
        startActivity(new Intent(this, QrActivity.class));
        finish();
    }

    @Override
    public void gotoTxnDetailActivity() {
        TransactionDetailsActivity.startActivity(this, mPresenter.getPinBlock());
        finish();
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue(View view) {
        mPresenter.submitAmount();
    }

    private void initViews() {
        $(R.id.tv0).setOnClickListener(this);
        $(R.id.tv1).setOnClickListener(this);
        $(R.id.tv2).setOnClickListener(this);
        $(R.id.tv3).setOnClickListener(this);
        $(R.id.tv4).setOnClickListener(this);
        $(R.id.tv5).setOnClickListener(this);
        $(R.id.tv6).setOnClickListener(this);
        $(R.id.tv7).setOnClickListener(this);
        $(R.id.tv8).setOnClickListener(this);
        $(R.id.tv9).setOnClickListener(this);
        $(R.id.ivReload).setOnClickListener(this);
        $(R.id.ivDelete).setOnClickListener(this);
    }

    protected <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }

    private void log(String msg){
        AppLog.i(TAG, msg);
    }

}