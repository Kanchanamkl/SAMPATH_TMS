package com.epic.pos.ui.settings.keyinject.key;

import android.os.Handler;
import android.os.Looper;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.SerialCom;

import javax.inject.Inject;

public class KeyInjectPresenter extends BasePresenter<KeyInjectContact.View> implements KeyInjectContact.Presenter {

    private String TAG = KeyInjectPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private Handler handler = new Handler(Looper.getMainLooper());
    private SerialCom serialCom;
    private Host host;

    private String M1 = "Press \"Start Key Inject\"";
    private String M2 = "Listening to key";
    private String M3 = "Key inject completed";
    private String M4 = "Key inject failed";

    @Inject
    public KeyInjectPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void setHost(Host host) {
        this.host = host;
        PosDevice.getInstance().clearMasterKey(host.getMasterKeyId());
        PosDevice.getInstance().clearWorkerKey(host.getWorkKeyId());
    }

    @Override
    public void tempKeyInject(String key) {
//        PosDevice.getInstance().clearWorkerKey();
//        PosDevice.getInstance().loadMasterKey(key);
//        PosDevice.getInstance().loadWorkKey(repository.getWorkKey());
//        mView.setKeyStatus(M3);
//        mView.setActionVisible(false);
//        mView.setStatusIconVisible(true);
    }

    @Override
    public void openSerialPort() {
        log("openSerialPort");
        new Thread() {
            @Override
            public void run() {
                PosDevice.getInstance().clearWorkerKey(host.getWorkKeyId());

                serialCom = new SerialCom();
                serialCom.initialize(SerialCom.SerialType.UART, 115200, 0, 8);
                serialCom.open();

                handler.post(() -> {
                    if (mView != null){
                        mView.setKeyStatus(M2);
                        mView.setActionEnabled(false);
                    }
                });

                if (serialCom.open()) {
                    byte[] buffer = new byte[1024];
                    int len = serialCom.read(buffer, 36, Const.SERIAL_PORT_TIMEOUT);

                    if (len == 0) {
                        log("Serial data not received!");

                        handler.post(() -> {
                            if (mView != null){
                                mView.setKeyStatus(M1);
                                mView.setActionEnabled(true);
                            }
                        });
                    } else {
                        try {
                            log("Serial data received.");
                            String masterKey = Utility.asciiToString(Utility.byte2HexStr(buffer, 0, len));
                            AppLog.i(TAG, "MASTER_KEY: " + masterKey);
                            masterKey = masterKey.substring(4);

                            PosDevice.getInstance().loadMainKey(host.getMasterKeyId(), masterKey);
                            PosDevice.getInstance().loadWorkKey(host.getMasterKeyId(), host.getWorkKeyId(), host.getWorkKey());
                            log("Key inject completed");

                            handler.post(() -> {
                                if (mView != null){
                                    mView.setKeyStatus(M3);
                                    mView.setActionVisible(false);
                                    mView.setStatusIconVisible(true);
                                    mView.keyInjectSuccess();
                                    PosDevice.getInstance().beep(300);
                                }
                            });
                        }catch (Exception ex){
                            ex.printStackTrace();
                            handler.post(() -> {
                                if (mView != null){
                                    mView.setKeyStatus(M1);
                                    mView.setActionEnabled(true);
                                    mView.showToastMessage(M4);
                                }
                            });
                        }
                    }
                } else {
                    handler.post(() -> {
                        if (mView != null){
                            mView.setActionEnabled(true);
                        }
                    });
                }
            }
        }.start();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}
