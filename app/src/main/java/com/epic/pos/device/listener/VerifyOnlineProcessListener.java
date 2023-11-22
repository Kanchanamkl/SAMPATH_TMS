package com.epic.pos.device.listener;

public interface VerifyOnlineProcessListener {

    void onlineProcessSuccess();
    void onlineProcessRefuse();
    void onlineProcessTerminate();

    void onError(int code);

}
