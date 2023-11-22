package com.epic.pos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.epic.pos.data.db.DbHandler;
import com.epic.pos.ui.home.HomeActivity;
import com.epic.pos.util.AppLog;

import com.epic.pos.service.NotifyingDailyService;

public class BootCompletedReceiver extends BroadcastReceiver {

    private final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent arg1) {
        //context.startService(new Intent(context, NotifyingDailyService.class));
        try {
            System.out.println("BootCompletedReceiver: onReceive()");
            System.out.println("BootCompletedReceiver: Boot Complete");

            DbHandler.getInstance().getTCT(tct -> {
                try {
                    System.out.println("BootCompletedReceiver: tct received: " + tct);
                    if (tct.getStartUpAutoRun() == 1){
                        System.out.println("BootCompletedReceiver: auto startup enabled");
                        Intent intent = new Intent(context, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }else {
                        System.out.println("BootCompletedReceiver: auto startup disabled");
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
