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
 * @since 2023-01-03
 */
public interface PosCheckCardListener2 {

    void onCardData(CardAction cardAction, CardData cardData);

    void onCheckCardError(int error, String msg);

    void onTimeOut();



}
