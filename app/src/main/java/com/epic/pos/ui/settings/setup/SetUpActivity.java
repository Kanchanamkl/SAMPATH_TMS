package com.epic.pos.ui.settings.setup;

import static com.epic.pos.ui.common.host.HostSelectActivity.RESULT_HOST;
import static com.epic.pos.ui.common.merchant.MerchantSelectActivity.RESULT_MERCHANT;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.common.Const;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.databinding.ActivitySetupBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.host.HostSelectActivity;
import com.epic.pos.ui.common.merchant.MerchantSelectActivity;
import com.epic.pos.ui.common.merchant.MerchantType;
import com.epic.pos.util.UiUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import abhishekti7.unicorn.filepicker.UnicornFilePicker;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetUpActivity extends BaseActivity<SetUpPresenter> implements SetUpContact.View {

    private final String TAG = SetUpActivity.class.getSimpleName();
    private final int HOST_SELECT_FOR_CLEAT_BATCH = 100;
    private final int MERCHANT_SELECT_FOR_CLEAT_BATCH = 101;
    private final int PICK_BACKUP_FILE = 102;

    private ActivitySetupBinding binding;

    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.btn_yes)
    String btn_yes;
    @BindString(R.string.btn_no)
    String btn_no;


    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, SetUpActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_setup), true);
        mPresenter.updateUi();
    }

    @Override
    public void onUpdateUi(boolean printClearIsoPacket) {
        binding.swIsoClear.setChecked(printClearIsoPacket);

        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            binding.tvAboutTitle.setText("Version Name: " + info.versionName);
            binding.tvAboutDesc.setText("Build: " + info.versionCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onUpdateEncUi(boolean printEncIsoPacket) {
        binding.swIsoEncrypted.setChecked(printEncIsoPacket);
    }

    @OnClick(R.id.layoutTMSParams)
    void onTmsParamsClicked() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.epic.eatmca", "com.epic.eatmca.TMSParamActivity"));
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
            showToastMessage("Please update the base app.");
        }
    }


    @OnClick(R.id.layoutIsoClear)
    void onPrintIsoClearOptionClicked() {
        mPresenter.printClearIsoPacket();
    }

    @OnClick(R.id.layoutIsoEncrypted)
    void onPrintIsoEncOptionClicked() {
        mPresenter.printEncIsoPacket();
    }

    @OnClick(R.id.layoutLogEnable)
    void onLogEnableClicked() {
        mPresenter.onLogEnableClicked();
    }

    @OnClick(R.id.layoutClearBatch)
    void onClearBatchClicked() {
        HostSelectActivity.startActivityForResult(this, Const.TITLE_CLEAR_BATCH, HOST_SELECT_FOR_CLEAT_BATCH);
    }

    @OnClick(R.id.layoutExportTxn)
    void onExportTransactionsClicked() {
        showDialogError(app_name, Const.MSG_EXPORT_CONFIRM, "YES", "NO", new UiUtil.OptionDialogListener() {
            @Override
            public void onOkClicked() {
                mPresenter.exportTransactions();
            }

            @Override
            public void onCancelClicked() {

            }
        });
    }

    @OnClick(R.id.layoutImportTransactions)
    void onImportTransactionsClicked() {
        UnicornFilePicker.from(this)
                .addConfigBuilder()
                .selectMultipleFiles(false)
                .showOnlyDirectory(false)
                .setRootDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                .showHiddenFiles(false)
                .setFilters(new String[]{"bkp"})
                .addItemDivider(true)
                .theme(R.style.MyFilePicker)
                .build()
                .forResult(PICK_BACKUP_FILE);
    }

    @OnClick(R.id.layoutConfigMap)
    void onConfigMapClicked() {
        showDialogError(app_name, Const.MSG_CONFIG_MAP_CONFIRMATION, "YES", "NO", new UiUtil.OptionDialogListener() {
            @Override
            public void onOkClicked() {
                mPresenter.generateConfigMap();
            }

            @Override
            public void onCancelClicked() {

            }
        });
    }


    @Override
    protected void onActivityResult(int request, int result, @Nullable Intent data) {
        super.onActivityResult(request, result, data);
        if (request == HOST_SELECT_FOR_CLEAT_BATCH) {
            if (result == RESULT_OK) {
                Host host = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setSelectedHost(host);
                MerchantSelectActivity.startActivityForResult(this, host, Const.TITLE_CLEAR_BATCH, MerchantType.ALL, MERCHANT_SELECT_FOR_CLEAT_BATCH);
            }
        } else if (request == MERCHANT_SELECT_FOR_CLEAT_BATCH) {
            if (result == RESULT_OK) {
                Merchant selectedMerchant = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setSelectedMerchant(selectedMerchant);
            }
        } else if (request == PICK_BACKUP_FILE) {
            if (result == RESULT_OK) {
                ArrayList<String> files = data.getStringArrayListExtra("filePaths");
                if (files != null && !files.isEmpty()){
                    String file = files.get(0);
                    restoreBackup(file);
                }else {
                    showToastMessage("No backup file selected.");
                }
            }
        }
    }

    private void restoreBackup(String backupFile) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(backupFile));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            String encData = text.toString();

            showDialogError(app_name, Const.MSG_RESTORE_CONFIRMATION, "YES", "NO", new UiUtil.OptionDialogListener() {
                @Override
                public void onOkClicked() {
                    mPresenter.restoreTransactions(encData);
                }

                @Override
                public void onCancelClicked() {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showToastMessage("Unable to restore backup file.");
        }
    }

    @Override
    public void onUpdateUiLogEnable(boolean isEnabled) {
        binding.swWriteLogFiles.setChecked(isEnabled);
    }

    @Override
    public void showHostSelectConfirmation(String msg) {
        showDialogError(app_name, msg, btn_yes, btn_no, new UiUtil.OptionDialogListener() {
            @Override
            public void onOkClicked() {
                mPresenter.clearBatch();
            }

            @Override
            public void onCancelClicked() {
                mPresenter.setSelectedMerchant(null);
                mPresenter.setSelectedHost(null);
            }
        });
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


}