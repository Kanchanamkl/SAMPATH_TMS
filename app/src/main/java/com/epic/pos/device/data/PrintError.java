package com.epic.pos.device.data;

/**
 * PrintError
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-04-09
 */
public enum PrintError {

    PAPER_OUT(240, "Paper out"),
    NO_CONTACT(241, "No content"),
    PRINTER_ERROR(242, "Printer error"),
    OVER_HEAT(243, "Over heat"),
    NO_BLACK_MARK(246, "No black mark"),
    PRINTER_BUSY(247, "Printer is busy"),
    MOTOR_ERROR(251, "Moto error"),
    BATTERY_LOW(14, "Battery low"),
    NO_TTF(226, "No ttf"),
    BITMAP_TOOWIDE(227, "Width of bitmap too wide");

    private int status;
    private String msg;

    PrintError(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public static PrintError convert(int status) {
        for (PrintError p : values()) {
            if (p.status == status){
                return p;
            }
        }
        return null;
    }
}
