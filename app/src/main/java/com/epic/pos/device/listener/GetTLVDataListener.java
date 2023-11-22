package com.epic.pos.device.listener;

public interface GetTLVDataListener {
    void onTLVDataReceived(String tlvData, String panSequenceNumber);
}
