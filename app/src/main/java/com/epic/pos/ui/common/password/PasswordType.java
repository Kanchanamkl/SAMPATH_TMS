package com.epic.pos.ui.common.password;

public enum PasswordType {

    MANAGER(0), SUPER(1), CLEAR_REVERSAL(2), EXIT_PASSWORD(3), EDIT_TABLE_PASSWORD(4);

    public int val;

    PasswordType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static PasswordType valueOf(int val) {
        if (val == MANAGER.getVal()) {
            return MANAGER;
        } else if (val == SUPER.getVal()) {
            return SUPER;
        } else if (val == CLEAR_REVERSAL.getVal()) {
            return CLEAR_REVERSAL;
        } else if (val == EDIT_TABLE_PASSWORD.getVal()) {
            return EDIT_TABLE_PASSWORD;
        } else {
            return EXIT_PASSWORD;
        }
    }
}
