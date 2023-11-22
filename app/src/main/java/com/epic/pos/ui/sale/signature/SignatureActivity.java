package com.epic.pos.ui.sale.signature;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivitySignatureBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.sale.receipt.ReceiptActivity;
import com.epic.pos.view.SurfaceDrawCanvas;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignatureActivity extends BaseActivity<SignaturePresenter> implements SignatureContact.View {

    private ActivitySignatureBinding binding;

    @BindString(R.string.msg_signature_is_empty)
    String msg_signature_is_empty;

    private SurfaceDrawCanvas canvas;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, SignatureActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signature);
        ButterKnife.bind(this);
        binding.signaturePad.setClipToOutline(true);
        mPresenter.initData();
    }

    @Override
    public void onAttachedToWindow() {
        ViewTreeObserver vto = binding.signaturePad.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.signaturePad.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = binding.signaturePad.getMeasuredWidth();
                int height = binding.signaturePad.getMeasuredHeight();
                addSignaturePadToLayout(width, height);
            }
        });
    }

    private void addSignaturePadToLayout(int width, int height) {
        canvas = new SurfaceDrawCanvas(this, width, height);
        LinearLayout.LayoutParams canvasLayout = new LinearLayout.LayoutParams(width, height);
        canvas.setLayoutParams(canvasLayout);
        binding.signaturePad.addView(canvas);
    }

    @Override
    public void updateUi(String currency, String amount) {
        binding.tvCurrency.setText(currency);
        binding.tvAmount.setText(amount);
    }

    @Override
    public void gotoReceiptActivity() {
        ReceiptActivity.startActivity(this);
        finish();
    }

    @OnClick(R.id.layoutClear)
    void onClearSignatureClicked(View view) {
        canvas.resetCanvas();
    }

    @OnClick(R.id.btnConfirm)
    void onBtnConfirmClicked(View view) {
        Bitmap bitmap = canvas.saveCanvas();
        if (bitmap != null) {
            mPresenter.saveSignature(bitmap);
        } else {
            showToastMessage(msg_signature_is_empty);
        }
    }

    @Override
    public void setConfirmBtnEnabled(boolean isEnabled) {
        binding.btnConfirm.setEnabled(isEnabled);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }
}