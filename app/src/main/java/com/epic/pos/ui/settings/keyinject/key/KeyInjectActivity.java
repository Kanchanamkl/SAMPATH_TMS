package com.epic.pos.ui.settings.keyinject.key;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.databinding.ActivityKeyInjectBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.device.PosDevice;
import com.epic.pos.util.Utility;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeyInjectActivity extends BaseActivity<KeyInjectPresenter> implements KeyInjectContact.View {

    private final String TAG = KeyInjectActivity.class.getSimpleName();
    private ActivityKeyInjectBinding binding;
    private static final String SELECTED_HOST = "selected_host";

    private Handler handler = new Handler(Looper.getMainLooper());

    public static void startActivity(BaseActivity activity, Host host) {
        Intent i = new Intent(activity, KeyInjectActivity.class);
        i.putExtra(SELECTED_HOST, host);
        activity.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_key_inject);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_key_inject), true);
        mPresenter.setHost(getIntent().getParcelableExtra(SELECTED_HOST));
    }

    @OnClick(R.id.btnTemp)
    void tempKeyInjectClicked(){
        temp();
    }

    private void temp() {
        showLoader("Please wait", "Loading keys");
        new Thread(){
            @Override
            public void run() {
                int slotMasterKey1 = 10;
                int slotWorkKey1 = 11;
                //HNB
//                String masterKey1 = "283fdbb4554769d2aaedd8d472639988";
//                String workKey1 = "6477231FB1F8019A92AC8A8137AF2E8F";

                String samworkkey = Utility.xorHexString("9897C85ED6DAE9C7FD2F1A2CBC4A15FE"," 67ADFBDAD0074931CB3D19F204A2CBCE");
                //SAMPATH
                String masterKey1 = samworkkey;  //FF3A338406DDA0F6361203DEB8E8DE30
                String workKey1 = "EF07F2262AEE7ECCAE4895D8B5F2379E";

                int slotMasterKey2 = 15;
                int slotWorkKey2 = 16;
                String masterKey2 = "174463711EC54E9A1DACA50C5C951D42";
                String workKey2 = "96005720A07B27D7F7F583C1208C0237";

                PosDevice.getInstance().clearWorkerKey(slotWorkKey1);

                PosDevice.getInstance().loadMainKey(slotMasterKey1, masterKey1);
                PosDevice.getInstance().loadWorkKey(slotMasterKey1, slotWorkKey1, workKey1);
                tempSleep();

                PosDevice.getInstance().clearWorkerKey(slotWorkKey2);
                PosDevice.getInstance().loadMainKey(slotMasterKey2, masterKey2);
                PosDevice.getInstance().loadWorkKey(slotMasterKey2, slotWorkKey2, workKey2);
                tempSleep();

                handler.post(() -> {
                    hideLoader();
                    showToastMessage("Success");
                });
            }
        }.start();
    }
    private void tempSleep(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyInjectSuccess() {

    }

    @OnClick(R.id.btnStart)
    void onStartClicked(){
        mPresenter.openSerialPort();
    }

    @Override
    public void setStatusIconVisible(boolean isVisible) {
        if (isVisible){
            binding.ivDone.setVisibility(View.VISIBLE);
        }else {
            binding.ivDone.setVisibility(View.GONE);
        }
    }

    @Override
    public void setKeyStatus(String status) {
        binding.tvStatus.setText(status);
    }

    @Override
    public void setActionEnabled(boolean isEnable) {
        binding.btnStart.setEnabled(isEnable);
    }

    @Override
    public void setActionVisible(boolean isVisible) {
       if (isVisible){
           binding.btnStart.setVisibility(View.VISIBLE);
       }else {
           binding.btnStart.setVisibility(View.GONE);
       }
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }
}