package com.epic.pos.ui.sale.qr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityQrBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.sale.receipt.ReceiptActivity;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import butterknife.BindString;
import butterknife.ButterKnife;
/**
 * @author buddhika_j
 * @version 1.0
 * @since 2023-09-11
 */
public class QrActivity extends BaseActivity<QrPresenter> implements QRContract.View {

    private final String TAG = QrActivity.class.getSimpleName();
    private ActivityQrBinding binding;

    @BindString(R.string.msg_txn_failed)
    String msg_txn_failed;
    @BindString(R.string.msg_processing)
    String msg_processing;
    @BindString(R.string.msg_txn_processing_msg)
    String msg_txn_processing_msg;
    @BindString(R.string.btn_ok)
    String btn_ok;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, QrActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.qr_sale), true);
        mPresenter.initData(getApplicationContext());
    }

    @Override
    public void loadqrcode(String qrstring) {
     //   qrstring = "000201010212263200281627800000000000030050060000520448145303144540520.005802LK5915WPOS QR-SUNTECH6007COLOMBO6104000062040500630424A6";
        QRGEncoder qrgEncoder = new QRGEncoder(qrstring, null, QRGContents.Type.TEXT, 600);
        try {
            Bitmap bitmap = qrgEncoder.getBitmap();
            binding.ivQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onTxnFailed(String msg,String msgcus) {
        FailedActivity.startActivity(msg, msgcus, btn_ok, this);
        finish();
    }
    int trantime =0;
    @Override
    public void onTxnStillPending(String msg , String msgdetail){
        showToastMessage("Transaction Still Pending ");
        binding.btnContinue.setEnabled(true);
    }

    @Override
    public void gotoReceiptActivity() {
     startActivity(new Intent(this, ReceiptActivity.class));
     finish();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


    public void btnverifypayment(View view) {
        binding.btnContinue.setEnabled(false);
        mPresenter.sendqrvalidaterequest();
    }


    public void btnretryclick(View view) {
        binding.btnContinue.setEnabled(false);
        mPresenter.sendqrvalidaterequest();
    }

    public void btncloseclick(View view) {
        finish();
    }
}