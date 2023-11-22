package com.epic.pos.device.listener;

public interface PosPinListener {

    void onKeyPress(int len, int key);
    void onConfirm(byte[] data, boolean isNonePin);
    void onCancel();
    void onError(int errorCode);
    void onpinbypass();

}
