package com.epic.pos.device.listener;

/**
 * Created by dhanushi_s  on 3/31/2021
 */

public interface SmartCardListener {

    void onAPDUSuccess(String result);
    void onCheckCardError(String errorMsg);

}
