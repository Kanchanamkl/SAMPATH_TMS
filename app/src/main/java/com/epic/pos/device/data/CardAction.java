package com.epic.pos.device.data;

/**
 * The CardAction enum contains the card action types
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-10
 */
public enum CardAction {

    SWIPE(1),
    INSERT(2),
    TAP(3),
    MANUAL(4),
    FALLBACK(5);

    public int val;

    private CardAction(int val) {
        this.val = val;
    }

    public static CardAction valueOf(int val) {
        for (CardAction e : values()) {
            if (e.val == val) {
                return e;
            }
        }
        return null;
    }

}
