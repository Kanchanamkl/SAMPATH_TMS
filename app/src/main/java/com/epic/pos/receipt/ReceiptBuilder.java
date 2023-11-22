package com.epic.pos.receipt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;


public class ReceiptBuilder {
    private List<IDrawItem> listItems = new ArrayList<>();
    private int backgroundColor = Color.WHITE;
    private float textSize;
    private int color = Color.BLACK;
    private int width;
    private int marginTop, marginBottom, marginLeft, marginRight;
    private Typeface typeface;
    private Paint.Align align = Paint.Align.LEFT;
    private int leftWidth = 19;
    private int rightWidth = 18;

    public ReceiptBuilder(int width) {
        this.width = width;
        setMargin(8);
    }

    public ReceiptBuilder setTextSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    public ReceiptBuilder setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ReceiptBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public ReceiptBuilder setDefaultTypeface() {
        typeface = null;
        return this;
    }

    public ReceiptBuilder setAlign(Paint.Align align) {
        this.align = align;
        return this;
    }

    public ReceiptBuilder setMargin(int margin) {
        this.marginLeft   = margin;
        this.marginRight  = margin;
        this.marginTop    = margin;
        this.marginBottom = margin;
        return this;
    }

    public ReceiptBuilder setMargin(int marginTopBottom, int marginLeftRight) {
        this.marginLeft = marginLeftRight;
        this.marginRight = marginLeftRight;
        this.marginTop = marginTopBottom;
        this.marginBottom = marginTopBottom;
        return this;
    }

    public ReceiptBuilder setMarginLeft(int margin) {
        this.marginLeft = margin;
        return this;
    }

    public ReceiptBuilder setMarginRight(int margin) {
        this.marginRight = margin;
        return this;
    }

    public ReceiptBuilder setMarginTop(int margin) {
        this.marginTop = margin;
        return this;
    }

    public ReceiptBuilder setMarginBottom(int margin) {
        this.marginBottom = margin;
        return this;
    }

    public ReceiptBuilder addText(String text) {
        return addText(text, true);
    }


    public ReceiptBuilder addText(String text, Boolean newLine) {
        DrawText drawerText = new DrawText(text);
        drawerText.setTextSize(this.textSize);
        drawerText.setColor(this.color);
        drawerText.setNewLine(newLine);
        if (typeface != null) {
            drawerText.setTypeface(typeface);
        }
        if (align != null) {
            drawerText.setAlign(align);
        }
        listItems.add(drawerText);
        return this;
    }


    public ReceiptBuilder addTable(String textLeft, String txtRight) {
        return addTable(textLeft,txtRight, true);
    }

    public ReceiptBuilder addTable(String textLeft, String txtRight, Boolean newLine) {
        align = Paint.Align.LEFT;
        boolean lengthExceed = false;

        if(textLeft.length()> leftWidth) {
            addText(textLeft.substring(0,leftWidth), false);
            lengthExceed = true;

        }else{
            addText(textLeft, false);
        }

        align = Paint.Align.RIGHT;
        if(txtRight.length()> rightWidth) {
            addText(txtRight.substring(0,rightWidth), true);

            if(lengthExceed){
                align = Paint.Align.LEFT;
                addText(textLeft.substring(leftWidth), false);
            }
            align = Paint.Align.RIGHT;
            addText(txtRight.substring(rightWidth), true);

        }else{
            addText(txtRight, true);
            if(lengthExceed){
                align = Paint.Align.LEFT;
                addText(textLeft.substring(leftWidth), true);
            }
        }

        if(newLine) {
            align = Paint.Align.LEFT;
            addText("", true);
        }
        return this;
    }


    public ReceiptBuilder addTable3Columns(String textLeft,String txtCenter, String txtRight) {
        align = Paint.Align.LEFT;
        addText(textLeft, false);

        align = Paint.Align.CENTER;
        addText(txtCenter, false);

        align = Paint.Align.RIGHT;
        addText(txtRight, true);

        align = Paint.Align.LEFT;
        addText("", true);

        return this;
    }

    public ReceiptBuilder addImage(Bitmap bitmap) {
        DrawImage drawerImage = new DrawImage(bitmap);
        if (align != null) {
            drawerImage.setAlign(align);
        }
        listItems.add(drawerImage);
        return this;
    }

    public ReceiptBuilder addItem(IDrawItem item) {
        listItems.add(item);
        return this;
    }

    public ReceiptBuilder addBlankSpace(int heigth) {
        listItems.add(new DrawBlankSpace(heigth));
        return this;
    }

    public ReceiptBuilder addParagraph() {
        listItems.add(new DrawBlankSpace((int) textSize));
        return this;
    }

    public ReceiptBuilder addLine() {
        return addLine(width - marginRight - marginLeft, null);
    }

    public ReceiptBuilder addDashLine() {
        return addLine(width - marginRight - marginLeft, Paint.Style.STROKE);
    }

    public ReceiptBuilder setTypeface(Context context, String typefacePath) {
        typeface = Typeface.createFromAsset(context.getAssets(), typefacePath);
        return this;
    }

    public ReceiptBuilder addLine(int size, Paint.Style style) {
        DrawLine line = new DrawLine(size);
        line.setAlign(align);
        line.setColor(color);
        line.setStyle(Paint.Style.FILL_AND_STROKE);
        listItems.add(line);
        return this;
    }

    private int getHeight() {
        int height = 5 + marginTop + marginBottom;
        for (IDrawItem item : listItems) {
            height += item.getHeight();
        }
        return height;
    }


    public Bitmap build() {
        Bitmap image  = Bitmap.createBitmap(width, getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint   = new Paint();
        canvas.drawColor(backgroundColor);
        canvas.drawBitmap(drawImage(), marginLeft, 0, paint);
        return image;
    }

    private Bitmap drawImage() {
        Bitmap image = Bitmap.createBitmap(width - marginRight - marginLeft, getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(backgroundColor);
        float size = marginTop;
        for (IDrawItem item : listItems) {
            item.drawOnCanvas(canvas, 0, size);
            size += item.getHeight();
        }
        return image;
    }
}
