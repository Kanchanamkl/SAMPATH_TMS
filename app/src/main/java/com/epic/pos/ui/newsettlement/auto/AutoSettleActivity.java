package com.epic.pos.ui.newsettlement.auto;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityAutoSettleBinding;
import com.epic.pos.device.listener.AutoSettlementListener;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.device.PosDevice;
import com.epic.pos.ui.home.HomePresenter;

import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-11-01
 */
public class AutoSettleActivity extends BaseActivity<AutoSettlePresenter> implements AutoSettleContact.View {

    private ActivityAutoSettleBinding binding;
    private static final int DETAIL_REPORT_PRINT_ERROR = 101;
    private static final int SETTLEMENT_REPORT_PRINT_ERROR = 102;

    @BindString(R.string.msg_detail_report_error)
    String msg_detail_report_error;
    @BindString(R.string.msg_settlement_report_error)
    String msg_settlement_report_error;
    @BindString(R.string.msg_settlement_failed)
    String msg_settlement_failed;
    @BindString(R.string.btn_ok)
    String btn_ok;
    @BindString(R.string.msg_auto_settle_completed)
    String msg_auto_settle_completed;

    private Handler handler;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, AutoSettleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auto_settle);
        handler = new Handler(getMainLooper());
        ButterKnife.bind(this);
        setLoadingAnimation();
        PosDevice.getInstance().startPrinting();
        mPresenter.initAutoSettlement();
        MyApp.getInstance().getBgThread().setSettlementOngoing(true);
    }

    @Override
    public void onDetailReportPrintError(String error) {
        handler.post(() -> {
            FailedActivity.startActivity(msg_detail_report_error, error,
                    false, false, AutoSettleActivity.this, DETAIL_REPORT_PRINT_ERROR);
        });

    }

    @Override
    public void onSettlementReportPrintError(String error) {
        handler.post(() -> {
            FailedActivity.startActivity(msg_settlement_report_error, error,
                    false, false, AutoSettleActivity.this, SETTLEMENT_REPORT_PRINT_ERROR);
        });
    }

    @Override
    public void onSettlementFailed(String error) {
        handler.post(() -> {
            FailedActivity.startActivity(msg_settlement_failed, error, btn_ok, AutoSettleActivity.this);
            finish();
        });
    }

    @Override
    public void onSettlementStateUpdate(String msg) {
        handler.post(() -> {
            binding.tvMessage.setText(msg);
        });
    }

    @Override
    public void onAutoSettlementCompleted() {
        handler.post(() -> {
            showToastMessage(msg_auto_settle_completed);
            finish();
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

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PosDevice.getInstance().stopPrinting();
        MyApp.getInstance().getBgThread().setSettlementOngoing(false);
    }

    @Override
    public void onBackPressed() {
        //back press disabled
    }

    private void setLoadingAnimation() {
        Drawable d = binding.ivProgressImage2.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animation = (AnimatedVectorDrawable) d;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animation.registerAnimationCallback(new Animatable2.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        animation.start();
                    }
                });
            }
            animation.start();
        }
    }
}