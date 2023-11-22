package com.epic.pos.ui.sale.receipt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.BuildConfig;
import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.databinding.ActivityReceiptBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.device.PosDevice;
import com.epic.pos.ui.home.HomeActivity;
import com.epic.pos.ui.settings.tms.TMSTxn;
import com.epic.pos.util.AppLog;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReceiptActivity extends BaseActivity<ReceiptPresenter> implements ReceiptContact.View {
    private final String TAG = "ReceiptActivity";
    private ActivityReceiptBinding binding;
    private int MERCHANT_COPY_RES = 150;
    private int CUSTOMER_COPY_RES = 151;
    private TMSTxn transaction;

    @BindString(R.string.msg_mer_copy_print_error)
    String msg_mer_copy_print_error;
    @BindString(R.string.msg_cus_copy_print_error)
    String msg_cus_copy_print_error;
    @BindString(R.string.msg_receipt_error)
    String msg_receipt_error;
    @BindString(R.string.btn_ok)
    String btn_ok;
    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.msg_feed_paper)
    String msg_feed_paper;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, ReceiptActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_receipt);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, mPresenter.getTitle(), true);
        Log.d("Buddhika","CheckReceiptPrintWithECR");
       // mPresenter.CheckReceiptPrintWithECR();
        mPresenter.initData();

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
    public void onCustomerReceiptGenerated(Bitmap bitmap, Transaction txn, boolean isTxnEnable) {
        binding.ivPreview.setImageBitmap(bitmap);

        log("isTxnEnable: " + isTxnEnable);
        if(isTxnEnable){
            transaction = new TMSTxn();
            transaction.setTransactionId(String.valueOf(txn.getId()));
            // transaction.setTransactionType(String.valueOf(txn.getTransaction_code()));
            transaction.setTransactionType(getPrintStatus(String.valueOf(txn.getTransaction_code())));
            transaction.setInvoiceNo(txn.getInvoice_no());
            transaction.setAmount(txn.getTotal_amount());
            transaction.setTerminalId(txn.getTerminal_id());
            transaction.setMerchantId(txn.getMerchant_id());
            transaction.setApproveCode(txn.getApprove_code());
            transaction.setRetrivelReffrenceNumber(txn.getRrn());
            transaction.setTraceNumber(txn.getTrace_no());
            transaction.setResponseCode(txn.getResponse_code());
            transaction.setTxnTime(txn.getTxn_time());
            transaction.setBatchId("0001");
            transaction.setStatus("TCMP");
            transaction.setTxnDate(txn.getTxn_date());
            transaction.setMti(txn.getMti());
            transaction.setProcessingCode(txn.getProcessing_code());
            transaction.setNii(txn.getNii());
            transaction.setCardNo(txn.getPan());
            transaction.setTleStatus("OPEN");
            transaction.setSettlmentStatus("TEST");
            transaction.setSettlmentTime("2023-08-03 08:00:00");
            transaction.setBankCode("7083");
            transaction.setCardType(txn.getCard_label());
            transaction.setBin("1234567890123456");
            transaction.setAppPackageName(getApplicationContext().getPackageName());
            transaction.setAppVersion(BuildConfig.VERSION_NAME);

            String tranData = new Gson().toJson(transaction);
            log("TranData_sync: "+ tranData);
            HomeActivity.getInstance().emitTranData(tranData);

            /////////////////////send receipt/////////////////////////////
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();

            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String receiptData = encoded  + "|" + txn.getId() ;
            log("TranData_Receipt_sync: "+  txn.getId());
            HomeActivity.getInstance().sendTxnReceiptData(receiptData);
        }else {
            log("TranData sync disable ");
        }
    }

    @Override
    public void setActionButtonEnabled(boolean isEnabled) {
        binding.btnPrint.setEnabled(isEnabled);
    }



    @OnClick(R.id.btnPrint)
    void onPrintClicked(View view) {
        mPresenter.printCustomerCopy();
    }

    @OnClick(R.id.btnCancel)
    void onCancelClicked(View view) {
        mPresenter.closeButtonPressed();
        finish();
    }

    @Override
    public void onMerchantCopyPrintError(String msg) {
        FailedActivity.startActivity(msg_mer_copy_print_error, msg, false, false, this, MERCHANT_COPY_RES);
    }

    @Override
    public void onCustomerCopyPrintError(String msg) {
        FailedActivity.startActivity(msg_cus_copy_print_error, msg, false, false, this, CUSTOMER_COPY_RES);
    }

    @Override
    public void onReceiptGenerationError(String msg) {
        FailedActivity.startActivity(msg_receipt_error, msg, btn_ok, this);
        finish();
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
    public void onfinish() {
        mPresenter.closeButtonPressed();
        finish();
    }
    @Override
    public void onCustomerReceiptPrinted() {
        mPresenter.closeButtonPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PosDevice.getInstance().stopPrinting();
    }

    @Override
    public void setUiVisible() {
        binding.root.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

    public String getPrintStatus(String status) {
        switch (status) {
//            case "1":
//                return "SALE";
            case "2":
                return "SALE_MANUAL";
            case "3":
                return "SALE_OFFLINE";
            case "4":
                return "SALE_OFFLINE_MANUAL";
            case "5":
                return "SALE_PRE_AUTHORIZATION";
            case "6":
                return "SALE_PRE_AUTHORIZATION_MANUAL";
            case "7":
                return "SALE_INSTALLMENT";
            case "8":
                return "SALE_PRE_COMPLETION";
            case "9":
                return "SALE_REFUND";
            case "10":
                return "SALE_REFUND_MANUAL";
            case "11":
                return "CASH_BACK";
            case "12":
                return "QUASI_CASH";
            case "13":
                return "QUASI_CASH_MANUAL";
            case "14":
                return "CASH_ADVANCE";
            case "15":
                return "STUDENT_REF";
            case "16":
                return "AUTH_ONLY";
            case "17":
                return "QR_SALE";
            default:
                log("SALE TXN");
                return "SALE";
        }
    }


}