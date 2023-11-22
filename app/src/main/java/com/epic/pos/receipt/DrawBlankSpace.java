package com.epic.pos.receipt;

import android.graphics.Canvas;
import android.graphics.Paint;


public class DrawBlankSpace implements IDrawItem {

    private int blankSpace;

    public DrawBlankSpace(int blankSpace) {
        this.blankSpace = blankSpace;
    }

    @Override
    public void drawOnCanvas(Canvas canvas, float x, float y) {
    }

    @Override
    public int getHeight() {
        return blankSpace;
    }

    @Override
    public Paint.Align getAlign() {
        return null;
    }
}
