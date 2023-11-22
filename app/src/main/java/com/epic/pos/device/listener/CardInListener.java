package com.epic.pos.device.listener;

public interface CardInListener {

    void cardIsStillIn();

    void onCardRemoved();

    void onError(Exception ex);

}
