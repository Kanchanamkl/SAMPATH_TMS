package com.epic.pos.ui.newsettlement.settle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.databinding.ActivitySettleBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.util.Utility;
import com.epic.pos.view.CustomDialogFragment;
import com.epic.pos.device.PosDevice;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.Gravity.CENTER;
import static android.view.View.GONE;

public class SettleActivity extends BaseActivity<SettleActivityPresenter> implements SettleActivityContact.View {

    private ActivitySettleBinding binding;
    private static final String EXTRA_SELECTED_HOST = "EXTRA_SELECTED_HOST";
    private static final String EXTRA_SELECTED_MERCHANT = "EXTRA_SELECTED_MERCHANT";
    private static final int DETAIL_REPORT_PRINT_ERROR = 101;
    private static final int SETTLEMENT_REPORT_PRINT_ERROR = 102;

    @BindString(R.string.toolbar_settlement)
    String toolbar_settlement;
    @BindString(R.string.activity_settlement_details_no_txn)
    String no_txn_found;
    @BindString(R.string.activity_settlement_details_report_msg_delete_pre_auth_title)
    String detail_report_msg_delete_pre_auth_title;
    @BindString(R.string.activity_settlement_details_report_msg_delete_pre_auth_message)
    String detail_report_msg_delete_pre_auth_message;
    @BindString(R.string.activity_settlement_details_report_msg_delete_pre_auth_left_btn)
    String detail_report_msg_delete_pre_auth_left_btn;
    @BindString(R.string.activity_settlement_details_report_msg_delete_pre_auth_right_btn)
    String detail_report_msg_delete_pre_auth_right_btn;
    @BindString(R.string.activity_settlement_details_report_msg_title)
    String detail_report_msg_title;
    @BindString(R.string.activity_settlement_details_report_msg_message)
    String detail_report_msg_message;
    @BindString(R.string.activity_settlement_details_report_msg_left_btn)
    String detail_report_msg_left_btn;
    @BindString(R.string.activity_settlement_details_report_msg_right_btn)
    String detail_report_msg_right_btn;
    @BindString(R.string.msg_detail_report_error)
    String msg_detail_report_error;
    @BindString(R.string.msg_settlement_report_error)
    String msg_settlement_report_error;
    @BindString(R.string.msg_settlement_success)
    String msg_settlement_success;
    @BindString(R.string.msg_settlement_failed)
    String msg_settlement_failed;
    @BindString(R.string.btn_ok)
    String btn_ok;

    Handler handler;
    private CustomDialogFragment preAuthDeleteConfDialog;
    private CustomDialogFragment printDetailReportConfDialog;

    public static void startActivity(BaseActivity activity, Host selectedHost, Merchant selectedMerchant) {
        activity.startActivity(new Intent(activity, SettleActivity.class)
                .putExtra(EXTRA_SELECTED_HOST, selectedHost)
                .putExtra(EXTRA_SELECTED_MERCHANT, selectedMerchant));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settle);
        handler = new Handler(getMainLooper());
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, toolbar_settlement, true);
        //Get extras
        mPresenter.setSelectedHost(getIntent().getParcelableExtra(EXTRA_SELECTED_HOST));
        mPresenter.setSelectedMerchant(getIntent().getParcelableExtra(EXTRA_SELECTED_MERCHANT));
        init();
        initProgress();
    }

    @SuppressLint("NewApi")
    private void initProgress() {
        Drawable d = binding.ivProgressImage.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animation = (AnimatedVectorDrawable) d;
            animation.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    animation.start();
                }
            });
            animation.start();
        }
    }

    @Override
    public void onSettlementStateUpdate(String msg) {
        handler.post(() -> binding.tvProgressDesc.setText(msg));

    }

    @Override
    public void showLoader(@NonNull String title, @NonNull String message) {
        handler.post(() -> {
            binding.tvProgressTitle.setText(title);
            binding.tvProgressDesc.setText(message);
            binding.layoutProgress.setVisibility(View.VISIBLE);
            binding.layoutContent.setVisibility(GONE);
        });
    }

    @Override
    public void hideLoader() {
        handler.post(() -> {
            binding.layoutProgress.setVisibility(GONE);
            binding.layoutContent.setVisibility(View.VISIBLE);
        });

    }

    private void init() {
        binding.etHost.setText(mPresenter.getSelectedHost().getHostName());
        binding.etMerchant.setText(mPresenter.getSelectedMerchant().getMerchantName());
        mPresenter.initSettlement();
    }

    @Override
    public void showPreAuthDeleteConfirmationDialog() {
        handler.post(() -> {
            preAuthDeleteConfDialog = new CustomDialogFragment.CustomDialogBuilder(
                    detail_report_msg_delete_pre_auth_title,
                    detail_report_msg_delete_pre_auth_message)
                    .setLeftBtnText(detail_report_msg_delete_pre_auth_left_btn, () -> {
                        preAuthDeleteConfDialog.dismiss();
                        mPresenter.onPreAuthDeleteClicked();
                    })
                    .setRightBtnText(detail_report_msg_delete_pre_auth_right_btn, () -> {
                        preAuthDeleteConfDialog.dismiss();
                        mPresenter.onPreAuthKeepClicked();
                    }).build();
            preAuthDeleteConfDialog.setCancelable(false);
            preAuthDeleteConfDialog.show(getSupportFragmentManager(), "CustomDialogFragment");
        });
    }

    @Override
    public void showPrintDetailReportDialog() {
        handler.post(() -> {
            printDetailReportConfDialog = new CustomDialogFragment.CustomDialogBuilder(
                    detail_report_msg_title,
                    detail_report_msg_message)
                    .setLeftBtnText(detail_report_msg_left_btn, () -> {
                        printDetailReportConfDialog.dismiss();
                        mPresenter.onDetailReportPrintClicked();
                    })
                    .setRightBtnText(detail_report_msg_right_btn, () -> {
                        printDetailReportConfDialog.dismiss();
                        mPresenter.onDoNotPrintDetailReportClicked();
                    }).build();
            printDetailReportConfDialog.setCancelable(false);
            printDetailReportConfDialog.show(getSupportFragmentManager(), "CustomDialogFragment");
        });
    }

    @Override
    public void onSettlementCompleted() {
        handler.post(() -> {
            showToastMessage(msg_settlement_success);
        });

        finish();
    }

    @Override
    public void onSettlementFailed(String msg) {
        handler.post(() -> {
            FailedActivity.startActivity(msg_settlement_failed, msg, btn_ok, SettleActivity.this);
        });
        finish();
    }

    @Override
    public void detailReportPrintError(String error) {
        handler.post(() -> {
            FailedActivity.startActivity(msg_detail_report_error, error,
                    false, false, SettleActivity.this, DETAIL_REPORT_PRINT_ERROR);
        });

    }

    @Override
    public void settlementReportPrintError(String error) {
        handler.post(() -> {
            FailedActivity.startActivity(msg_settlement_report_error, error,
                    false, false, SettleActivity.this, SETTLEMENT_REPORT_PRINT_ERROR);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_REPORT_PRINT_ERROR) {
            if (resultCode == RESULT_OK) {
                mPresenter.rePrintDetailReport();
            }
        } else if (requestCode == SETTLEMENT_REPORT_PRINT_ERROR) {
            if (resultCode == RESULT_OK) {
                mPresenter.rePrintSettlementReport();
            }
        }
    }

    @OnClick(R.id.btnSettle)
    public void onSettleClicked(View view) {
        PosDevice.getInstance().clearPrintQueue();
        PosDevice.getInstance().startPrinting();
        mPresenter.onSettleClicked();
    }

    @Override
    public void showSettlementAmount(String currencySymbol, long totalSaleAmount) {
        binding.tvSettlementAmount.setText(currencySymbol + " " + Utility.getFormattedAmount(totalSaleAmount));
    }

    @Override
    public void showTransactionNotFound() {
        binding.tvSettlementAmount.setText(no_txn_found);
        binding.tvSettlementAmount.setGravity(CENTER);
        binding.tvSettlement.setVisibility(GONE);
        binding.btnSettle.setVisibility(GONE);
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
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

}