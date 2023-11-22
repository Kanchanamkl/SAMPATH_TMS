package com.epic.pos.ui.sale.cardscan;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.DccData;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.databinding.ActivityCardScanBinding;
import com.epic.pos.device.PosDevice;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.approvalcode.ApprovalCodeActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;
import com.epic.pos.view.CDTListDialog;
import com.epic.pos.view.DCCSelectionDialog;
import com.epic.pos.view.MultiAppListDialog;

import java.util.List;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CardScanActivity extends BaseActivity<CardScanPresenter> implements CardScanContract.View {

  //  private String TAG = CardScanActivity.class.getSimpleName();
    private ActivityCardScanBinding binding;
    private final int FAIL_RES = 150;

    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.select_cdt_title)
    String selectCDT;
    @BindString(R.string.msg_card_not_support)
    String msg_card_not_support;
    @BindString(R.string.select_application)
    String select_application;
    @BindString(R.string.data_error)
    String data_error;
    @BindString(R.string.btn_ok)
    String btn_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_card_scan);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), false);
        mPresenter.resetData();
        mPresenter.checkCard();
        mPresenter.updateUi();
        mPresenter.startProcessThread();


    }

    @Override
    protected void onCloseButtonPressed() {
        mPresenter.closeButtonPressed();
        finish();
    }

    @Override
    public void onUpdateUi(String currency, String amount, String checkCardStatus) {
        binding.tvCurrency.setText(currency);
        binding.tvAmount.setText(amount);
        binding.tvStatus.setText(checkCardStatus);
    }

    @Override
    public void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList) {
        new CDTListDialog(this).showDialog(selectCDT, cardDefinitionList, cdt -> mPresenter.onCDTSelected(cdt));
    }
    @Override
    public void onDCCDataSELECT(List<DccData> dccData) {
        new DCCSelectionDialog(this).showDialogDCC( dccData, dcc -> {
            mPresenter.onDCCselected(dcc);
        });
    }
    @Override
    public void onMultiApplicationCard(List<String> applicationList) {
        new MultiAppListDialog(this).showDialog(select_application, applicationList,
                (name, index) -> mPresenter.onCardApplicationSelected(index));
    }

    @Override
    public void finishCardScanActivity() {
        finish();
    }

    @Override
    public void showDataMissingError(String msg) {
        showDialogError(data_error, msg, this::finish);
    }

    @Override
    public void gotoApprovalCodeActivity() {
        ApprovalCodeActivity.startActivity(this);
        finish();
    }

    @Override
    public void gotoNoRetryFailedActivity(String title, String msg) {
        FailedActivity.startActivity(title, msg, btn_ok, this);
        finish();
    }

    @Override
    public void gotoTxnDetailActivity() {
        TransactionDetailsActivity.startActivity(this, mPresenter.getPinBlock());
        finish();
    }

    @Override
    public void onCDTError() {
        showDialogError(app_name, msg_card_not_support, () -> {
            mPresenter.closeButtonPressed();
        });
        finish();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @OnClick(R.id.btnCancel)
    public void onClickCancel() {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        mPresenter.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mPresenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(binding!=null){
        binding.unbind();
        binding=null;}
        if(mPresenter!=null){
        mPresenter.detachView();
        mPresenter=null;}
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        PosDevice.getInstance().stopEmvFlow();
        mPresenter.closeButtonPressed();
        super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int result, @Nullable Intent data) {
        super.onActivityResult(requestCode, result, data);
        if (requestCode == FAIL_RES) {
            if (result == RESULT_OK) {
                mPresenter.checkCard();
            } else {
                finish();
            }
        }
    }



}