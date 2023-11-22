package com.epic.pos.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivitySettingsBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.password.PasswordActivity;
import com.epic.pos.ui.common.password.PasswordType;
import com.epic.pos.ui.settings.edittable.EditTableActivity;
import com.epic.pos.ui.settings.keyinject.host.HostSelectActivity;
import com.epic.pos.ui.settings.setup.SetUpActivity;
import com.epic.pos.device.PosDevice;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity<SettingsPresenter> implements SettingsContract.View {

    private final String TAG = SettingsActivity.class.getSimpleName();
    private ActivitySettingsBinding binding;
    private final int ACTIVITY_RESULT_KEY_INJECT_PW = 100;
    private final int ACTIVITY_RESULT_EDIT_TABLE_PW = 101;
    private final int ACTIVITY_RESULT_SETUP_PW = 102;
    private final int ACTIVITY_RESULT_TLE_LEY_PW = 103;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, SettingsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_settings), true);
        init();
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void init() {
    }

    @OnClick(R.id.tvSetup)
    public void onClickSetup(View view) {
        PasswordActivity.startActivityForResult(this, PasswordType.MANAGER, ACTIVITY_RESULT_SETUP_PW);
    }

    @OnClick(R.id.tvKeyInject)
    public void onClickKeyInject(View view) {
        PasswordActivity.startActivityForResult(this, PasswordType.MANAGER, ACTIVITY_RESULT_KEY_INJECT_PW);
    }

    @OnClick(R.id.tvEditTable)
    public void onClickEditTable(View view) {
        PasswordActivity.startActivityForResult(this, PasswordType.EDIT_TABLE_PASSWORD, ACTIVITY_RESULT_EDIT_TABLE_PW);
    }

    @OnClick(R.id.tvTLEKey)
    public void onClickTLEKey(View view) {
        PasswordActivity.startActivityForResult(this, PasswordType.MANAGER, ACTIVITY_RESULT_TLE_LEY_PW);
    }

   /* @OnClick(R.id.tvTMSParameters)
    public void onClickTMSParameters(View view) {

    }*/

    @OnClick(R.id.tvHostParameters)
    public void onClickHostParameters(View view) {

    }

    @OnClick(R.id.tvDiagnosticReports)
    public void onClickDiagnosticReports(View view) {
        //check whether the file is exist
        File pFile = getFilesDir();
        String path = pFile.getPath();
        path += "/Secured";

        mPresenter.printDiagnosticReport(new File(path, PosDevice.EMV_TAG_FILE));
    }

    @Override
    protected void onActivityResult(int requestCode, int result, @Nullable Intent data) {
        super.onActivityResult(requestCode, result, data);
        if (requestCode == ACTIVITY_RESULT_KEY_INJECT_PW) {
            if (result == RESULT_OK) {
                HostSelectActivity.startActivity(this);
                finish();
            }
        }else if (requestCode == ACTIVITY_RESULT_EDIT_TABLE_PW){
            if (result == RESULT_OK) {
                EditTableActivity.startActivity(this);
                finish();
            }
        }else if (requestCode == ACTIVITY_RESULT_SETUP_PW) {
            if (result == RESULT_OK) {
                SetUpActivity.startActivity(this);
                finish();
            }
        }else if (requestCode == ACTIVITY_RESULT_TLE_LEY_PW){
            if (result == RESULT_OK){
                HostSelectActivity.startActivityForTLE(this);
                finish();
            }
        }
    }
}