package com.epic.pos.ui.sale.studentref;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.databinding.ActivityManualSaleBinding;
import com.epic.pos.databinding.ActivityStudentRefBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.cardscan.CardScanActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;
import com.epic.pos.ui.sale.manual.ManualSaleContact;
import com.epic.pos.ui.sale.manual.ManualSalePresenter;
import com.epic.pos.util.ExpireDateTextWatcher;
import com.epic.pos.util.UiUtil;
import com.epic.pos.view.CDTListDialog;

import java.util.List;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class StudentRefActivity extends BaseActivity<StudentRefPresenter> implements StudentRefContact.View {

    private final String TAG = StudentRefActivity.class.getSimpleName();
    private ActivityStudentRefBinding binding;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, StudentRefActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_student_ref);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
        mPresenter.init();
    }

    @Override
    public void goToCardScanActivity() {
        startActivity(new Intent(this, CardScanActivity.class));
        finish();
    }

    @Override
    public void setContinueBtnEnabled(boolean isEnabled) {
        binding.btnContinue.setEnabled(isEnabled);
    }

    @OnTextChanged(value = R.id.etStudentRef, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onApprovalCodeChanged(Editable s) {
        mPresenter.setStudentRef(s.toString());
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

    @OnClick(R.id.btnContinue)
    void btnContinueClicked() {
        mPresenter.onSubmit();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


}