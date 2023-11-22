package com.epic.pos.device.data;

import java.io.InputStream;

/**
 * PrintItem
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-06-21
 */
public class PrintItem {

    public enum FontSize{
        SIZE_16, SIZE_16_BOLD, SIZE_24, SIZE_24_BOLD, SIZE_32, SIZE_32_BOLD
    }

    public enum ItemType {
        TEXT_LINE, DOT_LINE, BANK_LOGO, SPACE
    }

    private ItemType itemType;
    private PrintItem printItem;
    private FontSize fontSize;
    private String leftText;
    private String middleText;
    private String rightText;
    private InputStream imgInputStream;
    private int spaceLines;

    public PrintItem() {
        super();
    }

    public PrintItem(ItemType itemType) {
        this.itemType = itemType;
    }

    public PrintItem getPrintItem() {
        return printItem;
    }

    public void setPrintItem(PrintItem printItem) {
        this.printItem = printItem;
    }

    public FontSize getFontSize() {
        return fontSize;
    }

    public void setFontSize(FontSize fontSize) {
        this.fontSize = fontSize;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getMiddleText() {
        return middleText;
    }

    public void setMiddleText(String middleText) {
        this.middleText = middleText;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public InputStream getImgInputStream() {
        return imgInputStream;
    }

    public void setImgInputStream(InputStream imgInputStream) {
        this.imgInputStream = imgInputStream;
    }

    public int getSpaceLines() {
        return spaceLines;
    }

    public void setSpaceLines(int spaceLines) {
        this.spaceLines = spaceLines;
    }
}
