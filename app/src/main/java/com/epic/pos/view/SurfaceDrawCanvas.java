package com.epic.pos.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

import com.epic.pos.BuildConfig;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.AppUtil;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;

/**
 * This class is use as a signature pad
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-09-14
 */
public class SurfaceDrawCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private Paint paint, paintText;
    private Canvas canvas;
    private Bitmap bitmap;
    // private Bitmap originBitmap;
    private int mov_x;
    private int mov_y;
    final static int BUFFER_SIZE = 10000;
    public byte[] top;
    private int width, height;
    SurfaceHolder holder;
    // private Rect textRect;
    private int textW = 0;
    private int textH = 0;

    public SurfaceDrawCanvas(Context context, int width, int height) {
        super(context);
        holder = this.getHolder();
        holder.addCallback(this);
        this.width = width;
        this.height = height;
        AppLog.d("", "width:" + width);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setStyle(Style.FILL);
        if (AppUtil.isNexgoBuild()){
            paint.setStrokeWidth(3);
        }else {
            paint.setStrokeWidth(5);
        }
        //paint.setFakeBoldText(true);
        paint.setLinearText(true);
        // paint.setSubpixelText(true);
        // paint.setFilterBitmap(true);
        paint.setStrokeCap(Cap.ROUND);
        paint.setDither(true);
        paint.setAntiAlias(true);

        paintText = new Paint();
        String familyName = "Times New Roman";
        Typeface font = Typeface.create(familyName, Typeface.NORMAL);
        paintText.setTypeface(font);
        paintText.setTextSize(3);
        paintText.setAntiAlias(true);
        paintText.setColor(Color.BLACK);
        // getResources().getColor(R.color.gray)
        paintText.setStyle(Style.FILL);
        // paintText.setStrokeWidth(4* zoomMultiples);

        bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
        AppLog.d("Surface", "w:" + bitmap.getWidth());
        AppLog.d("Surface", "h:" + bitmap.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new MyThread().start();
        threadFlag = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        threadFlag = false;
    }

    boolean threadFlag = false;

    class MyThread extends Thread {
        @Override
        public void run() {
            while (threadFlag) {
                long startTime = System.currentTimeMillis();

                myDraw(canvas);
                long endTime = System.currentTimeMillis();
                if (endTime - startTime < 30) {
                    try {
                        Thread.sleep(30 - (endTime - startTime));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void resetCanvas() {
        isOnTouch = false;
        isCoverTrait = false;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        canvas.setBitmap(bitmap);
    }

    public Bitmap saveCanvas() {
        if (isOnTouch) {
            //saveCroppedImage(bitmap);
            Bitmap newbmp = compress(bitmap);
//            bitmap  = BitmapFactory.decodeFile("/sdcard/Pictures/temp_cropped.jpg");
            return newbmp;
        } else {
            return null;
        }

    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * �ȱ�ѹ��ͼƬ
     */
    private Bitmap compress(Bitmap bitmap) {
        Matrix matrix = new Matrix();

        matrix.postScale((float) 800 / bitmap.getWidth(), (float) 600 / bitmap.getHeight());
        // matrix.postRotate(180);
        Bitmap compressBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return compressBitmap;
    }

    protected void myDraw(Canvas canvas) {
        canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawBitmap(bitmap, 0, 0, paint);
        holder.unlockCanvasAndPost(canvas);
    }

    private boolean isOnTouch = false;
    private boolean isCoverTrait = false;
    private int down_x = 0;
    private int down_y = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // canvas.drawPoint(mov_x, mov_y, paint);
            if (!isOnTouch) {
                down_x = (int) event.getX();
                down_y = (int) event.getY();
            }
            // Log.d("", "dx:" + down_x + " dy:" + down_y);
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            canvas.drawLine(mov_x, mov_y, event.getX(), event.getY(), paint);
            if (!isOnTouch && (Math.abs(down_x - mov_x) > 100 || Math.abs(down_y - mov_y) > 50)) {
                isOnTouch = true;
            }
            // Log.d("", "ml:" + textRect.left + " mr:" + textRect.right +
            // " mt:"
            // + textRect.top + " mb:" + textRect.bottom + "isCoverTrait:"
            // + isCoverTrait);
            // if (!isCoverTrait && textRect.contains(mov_x, mov_y)) {
            //    isCoverTrait = true;
            // }
            // Log.d("", "mx:" + mov_x + " my:" + mov_y + "isOnTouch:" +
            // isOnTouch);

        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mov_x = (int) event.getX();
            mov_y = (int) event.getY();
        }
        mov_x = (int) event.getX();
        mov_y = (int) event.getY();
        return true;
    }
}
