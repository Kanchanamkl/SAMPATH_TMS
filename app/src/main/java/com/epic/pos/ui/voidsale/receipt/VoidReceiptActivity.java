package com.epic.pos.ui.voidsale.receipt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityVoidReceiptBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.device.PosDevice;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public class VoidReceiptActivity extends BaseActivity<VoidReceiptPresenter> implements VoidReceiptContact.View {

    private ActivityVoidReceiptBinding binding;
    private int MERCHANT_COPY_RES = 150;
    private int CUSTOMER_COPY_RES = 151;

    @BindString(R.string.msg_mer_copy_print_error)
    String msg_mer_copy_print_error;
    @BindString(R.string.msg_cus_copy_print_error)
    String msg_cus_copy_print_error;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, VoidReceiptActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_void_receipt);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_void), true);
        mPresenter.initData();
    }

    @Override
    public void onCustomerReceiptGenerated(Bitmap bitmap) {
        binding.ivPreview.setImageBitmap(bitmap);
    }

    @Override
    public void setPrintButtonEnabled(boolean isEnabled) {
        binding.btnPrint.setEnabled(isEnabled);
    }

    @OnClick(R.id.btnPrint)
    void onPrintClicked(View view) {
        mPresenter.printCustomerCopy();
    }

    @OnClick(R.id.btnCancel)
    void onCancelClicked(View view) {
        finish();
    }

    @Override
    public void onMerchantCopyPrintError(String msg) {
        FailedActivity.startActivity(msg_mer_copy_print_error, msg,
                false, false, this, MERCHANT_COPY_RES);
    }

    @Override
    public void onCustomerCopyPrintError(String msg) {
        FailedActivity.startActivity(msg_cus_copy_print_error, msg,
                false, false, this, CUSTOMER_COPY_RES);
    }

    @Override
    protected void onActivityResult(int requestCode, int result, @Nullable Intent data) {
        super.onActivityResult(requestCode, result, data);
        if (requestCode == MERCHANT_COPY_RES) {
            if (result == RESULT_OK) {
                mPresenter.retryToPrintMerchantCopy();
            }
        } else if (requestCode == CUSTOMER_COPY_RES) {
            if (result == RESULT_OK) {
                mPresenter.retryToPrintCustomerCopy();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PosDevice.getInstance().stopPrinting();
    }

    @Override
    public void onCustomerReceiptPrinted() {
        finish();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

}