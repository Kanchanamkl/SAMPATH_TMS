package com.epic.pos.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class LinearLayoutOneToOne extends LinearLayout {
    public LinearLayoutOneToOne(Context context) {
        super(context);
    }

    public LinearLayoutOneToOne(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutOneToOne(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
