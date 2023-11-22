package com.epic.pos.device.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PrintDataBuilder
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-06-21
 */
public class PrintDataBuilder {

    private List<PrintItem> printItems = new ArrayList<>();
    private PrintItem.FontSize fontSize;

    public PrintDataBuilder(){
        super();
        fontSize = PrintItem.FontSize.SIZE_16;
    }

    public void setFontSize(PrintItem.FontSize fontSize) {
        this.fontSize = fontSize;
    }

    public void addTextLeft(String leftText){
        PrintItem p = new PrintItem(PrintItem.ItemType.TEXT_LINE);
        p.setFontSize(fontSize);
        p.setLeftText(leftText);
        p.setMiddleText("");
        p.setRightText("");
        printItems.add(p);
    }

    public void addTextMiddle(String middleText){
        PrintItem p = new PrintItem(PrintItem.ItemType.TEXT_LINE);
        p.setFontSize(fontSize);
        p.setLeftText("");
        p.setMiddleText(middleText);
        p.setRightText("");
        printItems.add(p);
    }

    public void addTextRight(String rightText){
        PrintItem p = new PrintItem(PrintItem.ItemType.TEXT_LINE);
        p.setFontSize(fontSize);
        p.setLeftText("");
        p.setMiddleText("");
        p.setRightText(rightText);
        printItems.add(p);
    }

    public void addText(String left, String middle, String right){
        PrintItem p = new PrintItem(PrintItem.ItemType.TEXT_LINE);
        p.setFontSize(fontSize);
        p.setLeftText(left);
        p.setMiddleText(middle);
        p.setRightText(right);
        printItems.add(p);
    }

    public void addDotLine(){
        PrintItem p = new PrintItem(PrintItem.ItemType.DOT_LINE);
        p.setFontSize(PrintItem.FontSize.SIZE_16);
        p.setLeftText("--------------------------------------");
        printItems.add(p);
    }

    public void addImage(InputStream is){
        PrintItem p = new PrintItem(PrintItem.ItemType.BANK_LOGO);
        p.setImgInputStream(is);
        printItems.add(p);
    }

    public void addSpace(int lines){
        PrintItem p = new PrintItem(PrintItem.ItemType.SPACE);
        p.setSpaceLines(lines);
        printItems.add(p);
    }

    public List<PrintItem> getPrintItems() {
        return printItems;
    }
}
