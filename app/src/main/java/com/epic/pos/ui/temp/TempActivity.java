package com.epic.pos.ui.temp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import com.epic.pos.util.AppLog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.epic.pos.R;
import com.epic.pos.view.SurfaceDrawCanvas;


public class TempActivity extends AppCompatActivity {

    private final String TAG = "TempActivity";

    private SurfaceDrawCanvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {
        //Display display = this.getWindowManager().getDefaultDisplay();
        //int screenWidth = display.getWidth();
        //Log.d("", "sw" + screenWidth + " " + "sh" + display.getHeight());

        //int height = display.getHeight() - dip2px(this, 70);

        //Log.d("", "w " + height * 3 + " " + "h " + height);


        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int width  = layout.getMeasuredWidth();
                int height = layout.getMeasuredHeight();
                setSize(width, height);
            }
        });









    }

    private void setSize(int w, int h){
        LinearLayout layout = (LinearLayout) this.findViewById(R.id.layout);
        canvas = new SurfaceDrawCanvas(this, w, h);

        LinearLayout.LayoutParams canvasLayout = new LinearLayout.LayoutParams(w, h);
        canvas.setLayoutParams(canvasLayout);
        layout.addView(canvas);
        AppLog.d("", "lw" + layout.getWidth() + " " + "lh" + layout.getHeight());
        canvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        Button confirm = (Button) this.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Bitmap result = canvas.saveCanvas();

                if (result == null) {
                    Toast.makeText(TempActivity.this, "Signature empty", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Toast.makeText(TempActivity.this, "Signature exists", Toast.LENGTH_SHORT).show();
                }

                AppLog.d("", "w:" + result.getWidth());
                AppLog.d("", "h:" + result.getHeight());
            }
        });

        // 取消
        Button reset = (Button) this.findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.resetCanvas();
            }
        });
    }

    private void finishActivity() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        AppLog.e("dip2px", (int) (dipValue * scale + 0.5f) + "");
        return (int) (dipValue * scale + 0.5f);
    }


    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}