package com.epic.pos.receipt;

import android.graphics.Canvas;
import android.graphics.Paint;


public interface IDrawItem {
    void drawOnCanvas(Canvas canvas, float x, float y);

    int getHeight();

    Paint.Align getAlign();
}
