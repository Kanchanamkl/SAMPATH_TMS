package com.epic.pos.ui.settings.tlekeydownload;

import android.content.Intent;
import android.os.Bundle;
import com.epic.pos.util.AppLog;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.databinding.ActivityKeyDownloadBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardData;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class KeyDownloadActivity extends BaseActivity<KeyDownloadPresenter> implements KeyDownloadContract.View {

    private ActivityKeyDownloadBinding binding;
    private final String TAG = "KeyDownloadActivity";
    private CardData cardData = null;

    public static final String EXTRA_SELECTED_HOST  = "EXTRA_SELECTED_HOST";

    public static String APDU_SELECT_APP            = "00A40400";
    public static String TLE_AID                    = "0102030405060708090001";
    public static String APDU_GET_SERIAL            = "0C010000";
    public static String APDU_GET_PIN_VERIF_MODE    = "0C020000";
    public static String APDU_GET_PIN_VERFICATION   = "0C020100";
    public static String APDU_GET_ENC_METHOD        = "0C030000";
    public static String APDU_GET_COUNTER           = "0C030100";
    public static String APDU_GET_MAC               = "0C060000";
    public static String APDU_3DES_DEC              = "0C040000";
    public static String APDU_RSA_DEC               = "0C050000";

    public static String mac = "";
    @BindString(R.string.msg_txn_failed)
    String msg_txn_failed;

    private final int FAIL_RES = 150;

    public static void startActivity(BaseActivity activity, Host selectedHost) {
        activity.startActivity(new Intent(activity, KeyDownloadActivity.class).
                putExtra(EXTRA_SELECTED_HOST, selectedHost));
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_key_download);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_tle_key_download), false);
        mPresenter.initializeData(getIntent().getParcelableExtra(EXTRA_SELECTED_HOST));

        PosDevice.getInstance().setApduPowerOn(false);
        PosDevice.getInstance().clearPrintQueue();
        PosDevice.getInstance().startPrinting();
    }


    @OnClick(R.id.btnContinue)
    void onContinueClicked(View view){
        mPresenter.selectApp();
    }

    @Override
    public void onBackPressed() {
        mPresenter.onClosing();
        super.onBackPressed();
    }

    public void onClickCancel() {
        mPresenter.onClosing();
        finish();
    }


    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


    private final void log(String msg) {
        AppLog.i(TAG, msg);
    }

    @Override
    public void onTxnFailedAndRetry(String msg) {
        FailedActivity.startActivity(msg_txn_failed, msg, this, FAIL_RES);
    }

    @Override
    public void onTxnFailed(String msg) {
        FailedActivity.startActivity(msg_txn_failed, msg, this,0);
        finish();
    }

    @Override
    public void onSuccessKeyDownload() {
        mPresenter.onClosing();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int result, @Nullable Intent data) {
        super.onActivityResult(requestCode, result, data);
        if (requestCode == FAIL_RES) {
            if (result == RESULT_OK) {
                mPresenter.retryTransaction();
            } else {
                mPresenter.onClosing();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PosDevice.getInstance().stopPrinting();
    }
}