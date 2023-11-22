package com.epic.pos.device.listener;

import com.epic.pos.device.data.PrintError;

public interface PrintListener {

    void onPrintFinished();
    void onPrintError(PrintError printError);
}
