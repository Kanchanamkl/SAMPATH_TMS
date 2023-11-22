package com.epic.pos.ui.settings.setup;

import android.os.Environment;

import com.epic.pos.config.MyApp;
import com.epic.pos.domain.entity.TxnData;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.ConfigMapTableHelper;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.spcrypto.SPEncryptor;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

public class SetUpPresenter extends BasePresenter<SetUpContact.View> implements SetUpContact.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;
    private Host selectedHost;
    private Merchant selectedMerchant;

    @Inject
    public SetUpPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void restoreTransactions(String encData) {
        try {
            String jsonData = SPEncryptor.decrypt(Const.DATA_1, Const.DATA_2, encData);
            TxnData td = new Gson().fromJson(jsonData, TxnData.class);

            repository.deleteAllTransactions(() -> {
                repository.deleteAllReversals(() -> {
                    repository.insertAllTransactions(td.getTransactions(), id -> {
                        repository.insertAllReversals(td.getReversals(), id1 -> {
                            mView.showToastMessage("File restore completed.");
                        });
                    });
                });
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            mView.showToastMessage("Unable to restore file.");
        }
    }

    @Override
    public void exportTransactions() {
        repository.getReversals(reversals -> {
            repository.getAllTransactions(transactions -> {
                if (reversals.isEmpty() && transactions.isEmpty()) {
                    if (isViewNotNull()) {
                        mView.showToastMessage("No transactions or reversals found.");
                    }
                } else {
                    TxnData txnData = new TxnData();
                    txnData.setTransactions(transactions);
                    txnData.setReversals(reversals);

                    String encData = SPEncryptor.encrypt(Const.DATA_1, Const.DATA_2, new Gson().toJson(txnData));

                    File backupDir = new File(Environment.getExternalStorageDirectory().getPath(), "eappa_backup");
                    if (!backupDir.exists()) {
                        backupDir.mkdirs();
                    }

                    File backupFile = new File(backupDir, "eappa_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".bkp");

                    FileOutputStream fileOutputStream = null;
                    try {
                        backupFile.createNewFile();
                        fileOutputStream = new FileOutputStream(backupFile);
                        fileOutputStream.write(encData.getBytes());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (isViewNotNull()) {
                        mView.showToastMessage("Transaction backup completed.");
                    }
                }
            });
        });
    }

    @Override
    public void onLogEnableClicked() {
        boolean isLogEnabled = !repository.isLogEnabled();
        repository.setLogEnabled(isLogEnabled);
        Const.IS_LOG_WRITE_ON_FILE = isLogEnabled;
        updateUi();
    }

    @Override
    public void printClearIsoPacket() {
        boolean shouldPrintClearISOPacket = !repository.shouldPrintClearISOPacket();
        repository.setPrintClearISOPacket(shouldPrintClearISOPacket);
        Const.PRINT_ISO_MSG = shouldPrintClearISOPacket;

        if (repository.shouldPrintClearISOPacket() && repository.shouldPrintEncryptedISOPacket()) {
            repository.setPrintEncryptedISOPacket(false);
            Const.PRINT_ENC_ISO_MSG = false;
        }
        updateUi();
    }

    @Override
    public void generateConfigMap() {
        if (isViewNotNull()) {
            mView.showLoader("EAPPA", "Generating config map.");
        }

        new ConfigMapTableHelper(repository).clearConfigMap(repository, Const.TABLES, () -> {
            new ConfigMapTableHelper(repository).insertDataToTable(repository, Const.TABLES, () -> {
                if (isViewNotNull()) {
                    mView.hideLoader();
                }
            });
        });
    }

    @Override
    public void printEncIsoPacket() {
        boolean shouldPrintEncISOPacket = !repository.shouldPrintEncryptedISOPacket();
        repository.setPrintEncryptedISOPacket(shouldPrintEncISOPacket);
        Const.PRINT_ENC_ISO_MSG = shouldPrintEncISOPacket;

        if (repository.shouldPrintClearISOPacket() && repository.shouldPrintEncryptedISOPacket()) {
            repository.setPrintClearISOPacket(false);
            Const.PRINT_ISO_MSG = false;
        }
        updateUi();
    }

    @Override
    public void setSelectedHost(Host host) {
        this.selectedHost = host;
    }

    @Override
    public void setSelectedMerchant(Merchant merchant) {
        this.selectedMerchant = merchant;
        if (merchant != null) {
            String msg = Const.MSG_CLEAR_TXN_CONF
                    .replace("#h#", selectedHost.getHostName())
                    .replace("#m#", selectedMerchant.getMerchantName());
            mView.showHostSelectConfirmation(msg);
        }
    }

    @Override
    public void clearBatch() {
        //delete transactions
        repository.deleteTransactions(selectedMerchant.getMerchantNumber(), selectedHost.getHostID(), () -> {
            //update must settle flag
            selectedHost.setMustSettleFlag(0);
            repository.updateMustSettleFlagByHostId(selectedHost.getHostID(), 0, () -> {
                //must settle flag updated
                mView.showToastMessage(Const.MSG_TXN_BATCH_CLEAR);
            });
        });
    }

    @Override
    public void updateUi() {
        if (isViewNotNull()){
            mView.onUpdateUi(repository.shouldPrintClearISOPacket());
            mView.onUpdateEncUi(repository.shouldPrintEncryptedISOPacket());
            mView.onUpdateUiLogEnable(repository.isLogEnabled());
        }
    }

    private boolean isViewNotNull() {
        return mView != null;
    }
}