package com.epic.pos.ui.ecrcardscan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.databinding.ActivityApprovalCodeBinding;
import com.epic.pos.databinding.ActivityEcrCardScanBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.approvalcode.ApprovalCodeContact;
import com.epic.pos.ui.sale.approvalcode.ApprovalCodePresenter;
import com.epic.pos.ui.sale.cardscan.CardScanActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;
import com.epic.pos.view.CDTListDialog;
import com.epic.pos.view.MultiAppListDialog;

import java.util.List;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class EcrCardScanActivity extends BaseActivity<EcrCardScanPresenter> implements EcrCardScanContact.View {

    private final String TAG = EcrCardScanActivity.class.getSimpleName();
    private ActivityEcrCardScanBinding binding;

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

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, EcrCardScanActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ecr_card_scan);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_card_scan), true);
        mPresenter.checkCard();
        mPresenter.initEcr();
    }

    @Override
    public void setState(String state) {
        binding.tvStatus.setText(state);
    }

    @Override
    public void onCDTError() {
        showDialogError(app_name, msg_card_not_support, () -> {
            mPresenter.closeButtonPressed();
            finish();
        });
    }

    @Override
    public void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList) {
        new CDTListDialog(this).showDialog(selectCDT, cardDefinitionList, cdt -> mPresenter.onCDTSelected(cdt));
    }

    @Override
    public void onMultiApplicationCard(List<String> applicationList) {
        new MultiAppListDialog(this).showDialog(select_application, applicationList,
                (name, index) -> mPresenter.onCardApplicationSelected(index));
    }

    @Override
    public void gotoNoRetryFailedActivity(String title, String msg) {
        FailedActivity.startActivity(title, msg, btn_ok, this);
        finish();
    }

    @Override
    public void gotoCardScanActivity() {
        startActivity(new Intent(this, CardScanActivity.class));
        finish();
    }

    @Override
    protected void onCloseButtonPressed() {
        mPresenter.closeButtonPressed();
    }

    @Override
    @OnClick(R.id.btnCancel)
    public void onBackPressed() {
        mPresenter.closeButtonPressed();
        super.onBackPressed();
    }

    @Override
    public void showDataMissingError(String msg) {
        showDialogError(data_error, msg, this::finish);
    }

    @Override
    public void finishCardScanActivity() {
        finish();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }



}