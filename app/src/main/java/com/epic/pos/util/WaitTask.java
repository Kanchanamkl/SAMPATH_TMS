package com.epic.pos.util;

import android.os.Handler;
import android.os.Looper;

/**
 * WaitTask
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-18
 */
public class WaitTask extends Thread{

    private final String TAG = WaitTask.class.getSimpleName();
    private boolean shouldContinue = true;
    private int millis;
    private Listener listener;

    private Handler handler = new Handler(Looper.getMainLooper());

    public WaitTask(int millis) {
        this.millis = millis;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void stopWaitTask(){
        AppLog.i(TAG, "stopWaitTask()");
        shouldContinue = false;
    }

    @Override
    public void run() {
        while (shouldContinue){
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            AppLog.i(TAG, "doTask()");

            handler.post(() -> {
                if (shouldContinue){
                    listener.doTask();
                }
            });
        }

        AppLog.i(TAG, "finished()");
        handler.post(() -> listener.onFinished());
    }

    public interface Listener{
        void doTask();
        void onFinished();
    }
}
