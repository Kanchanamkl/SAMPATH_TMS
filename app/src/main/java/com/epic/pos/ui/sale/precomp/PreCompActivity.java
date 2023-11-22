package com.epic.pos.ui.sale.precomp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.databinding.ActivityPreCompBinding;
import com.epic.pos.databinding.ActivityVoidDetailsBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.amount.AmountActivity;
import com.epic.pos.ui.voidsale.VoidContract;
import com.epic.pos.ui.voidsale.VoidPresenter;
import com.epic.pos.ui.voidsale.receipt.VoidReceiptActivity;
import com.epic.pos.util.InvoiceNumberTextWatcher;
import com.epic.pos.util.UiUtil;
import com.epic.pos.util.Utility;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public class PreCompActivity extends BaseActivity<PreCompPresenter> implements PreCompContract.View {

    private final String TAG = PreCompActivity.class.getSimpleName();
    private ActivityPreCompBinding binding;

    @BindString(R.string.btn_confirm)
    String btn_confirm;
    @BindString(R.string.btn_continue)
    String btn_continue;
    @BindString(R.string.amount_with_currency)
    String amount_with_currency;
    @BindString(R.string.msg_void_failed)
    String msg_void_failed;
    @BindString(R.string.btn_ok)
    String btn_ok;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, PreCompActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pre_comp);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
        mPresenter.resetData();
        binding.etInvoiceNo.addTextChangedListener(new InvoiceNumberTextWatcher(binding.etInvoiceNo, 6) {
            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.setInvoiceNumber(binding.etInvoiceNo.getText().toString());
            }
        });
        disableCopyPaste(binding.etInvoiceNo);
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
    public void onDateReceived(String hostName, String merchantName) {
        binding.etHost.setText(hostName);
        binding.etMerchant.setText(merchantName);
    }

    @Override
    public void onClearVoidUI() {
        binding.ivClear.setVisibility(View.GONE);
        binding.btnContinue.setVisibility(View.VISIBLE);
        binding.btnContinue.setText(btn_continue);
        binding.btnContinue.setEnabled(false);
        binding.etInvoiceNo.setEnabled(true);
        binding.etInvoiceNo.setText("");
        binding.etInvoiceNo.requestFocusFromTouch();
        binding.cvInvoiceData.setVisibility(View.GONE);
        binding.cvErrorMsg.setVisibility(View.GONE);
    }

    @Override
    public void setConfirmEnabled(boolean isEnabled) {
        binding.btnContinue.setEnabled(isEnabled);
    }

    @OnClick(R.id.btnContinue)
    void btnContinueClicked() {
        UiUtil.hideKeyboard(this);
        mPresenter.onSubmit();
    }

    @OnClick(R.id.ivClear)
    void onClearClicked() {
        mPresenter.clearVoid();
    }

    @Override
    public void onInvoiceDataReceived(Transaction t, Issuer i, Merchant m, String maskedPan) {
        binding.btnContinue.setText(btn_confirm);
        binding.etInvoiceNo.setEnabled(false);
        binding.cvInvoiceData.setVisibility(View.VISIBLE);

        binding.tvHost.setText(i.getIssuerLable());
        binding.tvMerchant.setText(m.getMerchantName());

        binding.tvCardNo.setText(maskedPan);

        String tvAmountTitle = amount_with_currency.replace("#currency#", t.getCurrency_symbol());
        binding.tvAmountTitle.setText(tvAmountTitle);

        String amount = Utility.getFormattedAmount(Long.parseLong(t.getBase_transaction_amount()));
        binding.tvAmount.setText(amount);

        if (t.getVoided() == 1) {
            binding.tvAlreadyVoided.setVisibility(View.VISIBLE);
            binding.btnContinue.setVisibility(View.GONE);
        } else {
            binding.tvAlreadyVoided.setVisibility(View.GONE);
        }

        binding.ivClear.setVisibility(View.VISIBLE);
    }

    @Override
    public void gotoAmountActivity() {
        AmountActivity.startActivity(this);
        finish();
    }

    @Override
    public void onShowError(String error) {
        binding.incorrectInvoice.setText(error);
        binding.btnContinue.setVisibility(View.GONE);
        binding.etInvoiceNo.setEnabled(false);
        binding.cvErrorMsg.setVisibility(View.VISIBLE);
        binding.ivClear.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


}