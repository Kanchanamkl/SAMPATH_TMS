package com.epic.pos.ui.failed;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityFailedBinding;
import com.epic.pos.ui.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FailedActivity extends BaseActivity<FailedPresenter> implements FailedContract.View {

    private ActivityFailedBinding binding;
    private static final String EXTRAS_TITLE = "EXTRAS_TITLE";
    private static final String EXTRAS_MESSAGE = "EXTRAS_MESSAGE";
    private static final String EXTRAS_RETRY_ENABLED = "EXTRAS_RETRY_ENABLED";
    private static final String EXTRAS_CANCEL_ENABLED = "EXTRAS_CANCEL_ENABLED";
    private static final String EXTRAS_CANCEL_BTN_TXT = "EXTRAS_CANCEL_BTN_TXT";
    private static final String EXTRAS_TIMEOUT_ENABLED = "EXTRAS_TIMEOUT_ENABLED";

    private boolean retryEnabled = false; //btn visibility
    private boolean cancelEnabled = true; //btn visibility
    private boolean isTimeOutEnabled = true; //auto close timeout
    private String cancelBtnTxt = ""; //cancel button text

    public static void startActivity(String title, String message, boolean isCancelEnabled, boolean isTimeOutEnabled, BaseActivity activity, int requestCode) {
        activity.startActivityForResult(
                new Intent(activity, FailedActivity.class)
                        .putExtra(EXTRAS_TITLE, title)
                        .putExtra(EXTRAS_MESSAGE, message)
                        .putExtra(EXTRAS_CANCEL_ENABLED, isCancelEnabled)
                        .putExtra(EXTRAS_TIMEOUT_ENABLED, isTimeOutEnabled)
                        .putExtra(EXTRAS_RETRY_ENABLED, true), requestCode);
    }

    public static void startActivity(String title, String message, BaseActivity activity, int requestCode) {
        activity.startActivityForResult(
                new Intent(activity, FailedActivity.class)
                        .putExtra(EXTRAS_TITLE, title)
                        .putExtra(EXTRAS_MESSAGE, message)
                        .putExtra(EXTRAS_RETRY_ENABLED, false)
                        .putExtra(EXTRAS_TIMEOUT_ENABLED, true), requestCode);
    }

    public static void startActivity(String title, String message, String cancelBtnTxt, BaseActivity activity) {
        activity.startActivity(
                new Intent(activity, FailedActivity.class)
                        .putExtra(EXTRAS_TITLE, title)
                        .putExtra(EXTRAS_MESSAGE, message)
                        .putExtra(EXTRAS_CANCEL_BTN_TXT, cancelBtnTxt)
                        .putExtra(EXTRAS_RETRY_ENABLED, false)
                        .putExtra(EXTRAS_TIMEOUT_ENABLED, true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_failed);
        ButterKnife.bind(this);
        getExtras();
        Drawable d = binding.ivFailImage.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animation = (AnimatedVectorDrawable) d;
            animation.start();
        }

        if (!retryEnabled) {
            binding.btnTryAgain.setVisibility(View.GONE);
        }

        if (!cancelEnabled) {
            binding.btnCancel.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(cancelBtnTxt)) {
            binding.btnCancel.setText(cancelBtnTxt);
        }

        if (isTimeOutEnabled){
            mPresenter.startCountDown();
        }
    }

    private void getExtras() {
        binding.tvTitle.setText(getIntent().getStringExtra(EXTRAS_TITLE));
        binding.tvMessage.setText(getIntent().getStringExtra(EXTRAS_MESSAGE));
        retryEnabled = getIntent().getBooleanExtra(EXTRAS_RETRY_ENABLED, false);
        cancelEnabled = getIntent().getBooleanExtra(EXTRAS_CANCEL_ENABLED, true);
        cancelBtnTxt = getIntent().getStringExtra(EXTRAS_CANCEL_BTN_TXT);
        isTimeOutEnabled = getIntent().getBooleanExtra(EXTRAS_TIMEOUT_ENABLED, true);
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onCountDownFinished() {
        onCancelClicked();
    }

    @OnClick(R.id.btnCancel)
    public void onCancelClicked() {
        mPresenter.closeButtonPressed();

        if (retryEnabled) {
            setResult(RESULT_CANCELED, new Intent());
        }

        finish();
    }

    @OnClick(R.id.btnTryAgain)
    public void onClickTryAgain() {
        if (retryEnabled) {
            setResult(RESULT_OK, new Intent());
        }

        finish();
    }
}