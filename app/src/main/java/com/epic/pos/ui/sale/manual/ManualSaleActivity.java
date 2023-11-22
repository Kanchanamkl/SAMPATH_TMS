package com.epic.pos.ui.sale.manual;

import android.content.Intent;
import android.mtp.MtpDevice;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.databinding.ActivityManualSaleBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;
import com.epic.pos.util.ExpireDateTextWatcher;
import com.epic.pos.util.UiUtil;
import com.epic.pos.view.CDTListDialog;
import com.epic.pos.view.MultiAppListDialog;

import java.util.List;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class ManualSaleActivity extends BaseActivity<ManualSalePresenter> implements ManualSaleContact.View {

    private final String TAG = ManualSaleActivity.class.getSimpleName();
    private ActivityManualSaleBinding binding;
    private final int FAIL_RES = 150;

    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.select_cdt_title)
    String selectCDT;
    @BindString(R.string.msg_card_not_support)
    String msg_card_not_support;
    @BindString(R.string.select_application)
    String select_application;
    @BindString(R.string.emv_error)
    String emv_error;
    @BindString(R.string.data_error)
    String data_error;
    @BindString(R.string.btn_ok)
    String btn_ok;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, ManualSaleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manual_sale);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
        binding.etExpDate.addTextChangedListener(new ExpireDateTextWatcher(binding.etExpDate){
            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.setExpireDate(s.toString());
            }
        });
        mPresenter.init();
        disableCopyPaste(binding.etCardNo);
        disableCopyPaste(binding.etExpDate);
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

    @Override
    public void showApprovalCode() {
        binding.tilApprovalCode.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCDTError() {
        showDialogError(app_name, msg_card_not_support, this::finish);
    }

    @Override
    public void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList) {
        new CDTListDialog(this).showDialog(selectCDT, cardDefinitionList, cdt -> mPresenter.onCDTSelected(cdt));
    }

    @OnTextChanged(value = R.id.etCardNo, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardNoTextChanged(Editable s) {
        mPresenter.setCardNumber(s.toString());
    }

    @OnTextChanged(value = R.id.etApprovalCode, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onApprovalCodeChanged(Editable s) {
        mPresenter.setApprovalCode(s.toString());
    }

    @Override
    public void gotoNoRetryFailedActivity(String title, String msg) {
        FailedActivity.startActivity(title, msg, btn_ok, this);
        finish();
    }

    @Override
    public void showDataMissingError(String msg) {
        UiUtil.hideKeyboard(this);
        showDialogError(data_error, msg, this::finish);
    }

    @Override
    public void showValidationError(String msg) {
        UiUtil.hideKeyboard(this);
        showDialogError(app_name, msg, null);
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
    void btnContinueClicked(){
        mPresenter.onSubmit();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }



}