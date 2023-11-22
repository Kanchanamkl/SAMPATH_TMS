package com.epic.pos.device.listener;

import android.os.Bundle;

import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;

import java.util.List;

/**
 * The PosCheckCardListener interface is used to manage check card callbacks.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-10
 */
public interface PosCheckCardListener {

    void onCardInserted();

    void onCheckCardError(int error, String msg);

    void onEmvError(int result, Bundle data, String msg);

    void onFallback();

    void onTimeOut();

    void onCardData(CardAction cardAction, CardData cardData);

    void onSelectApplication(List<String> applications);

    void onPinRequested(boolean isOnlinePin, int retryTimes);

    void onRequestOnlineProcess();

    void onOfflineapprove();

}
