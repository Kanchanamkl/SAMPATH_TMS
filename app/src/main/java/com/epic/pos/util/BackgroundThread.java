package com.epic.pos.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;

import com.epic.pos.common.Const;
import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.domain.repository.Repository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

public class BackgroundThread extends Thread {

    private final String TAG = BackgroundThread.class.getSimpleName();
    private final int SLEEP_TIME = 1000 * 30;
    private boolean shouldRun = true;
    private boolean isSettlementOngoing = false;
    private boolean isProfileUpdateOngoing = false;
    private boolean isGeneratingConfigMap = false;
    private int tmsConIteration = 0;
    private Context context;
    private Handler handler;

    @Inject
    protected Repository repository;

    public void setContext(Context context) {
        this.context = context;
        handler = new Handler(context.getMainLooper());
    }

    public void setSettlementOngoing(boolean isSettlementOngoing) {
        this.isSettlementOngoing = isSettlementOngoing;
    }

    public void setProfileUpdateOngoing(boolean isProfileUpdateOngoing) {
        this.isProfileUpdateOngoing = isProfileUpdateOngoing;
    }

    public void setGeneratingConfigMap(boolean generatingConfigMap) {
        isGeneratingConfigMap = generatingConfigMap;
    }

    private SharedPref prefs() {
        SharedPreferences sharedPref = context.getSharedPreferences(SharedPref.PREFS_NAME, Context.MODE_PRIVATE);
        return new SharedPref(sharedPref, context);
    }

    @Override
    public void run() {
        log("=== BackgroundThread Start ===");
        while (shouldRun) {
            SharedPref prefs = prefs();

            //CHECK FOR SETTLEMENT
            if (!isSettlementOngoing) {
                log("no settlement is ongoing.");
                if (prefs.hasPendingAutoSettlement()) {
                    log("has pending auto settlement.");
                    turnOnScreen();
                    notifyAutoSettlement();
                } else {
                    log("no pending auto settlement.");
                    DbHandler.getInstance().getTCT(tct -> {
                        log("tct received.");
                        log("is auto settle enabled status: " + tct.getAutoSettleEnable());
                        if (tct.getAutoSettleEnable() == 1) {
                            if (shouldStartAutoSettle(tct)) {
                                log("auto settle date ok.");
                                DbHandler.getInstance().getTransactionCount(count -> {
                                    log("transaction count: " + count);
                                    if (count >= 1) {
                                        turnOnScreen();
                                        notifyAutoSettlement();
                                    } else {
                                        log("increment next settlement date.");
                                        incrementAutoSettlementDate(nextSettlementDate -> {
                                            log("next settlement date: " + nextSettlementDate);
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            } else {
                log("settlement is ongoing...");
            }

            //profile update
            if (!isProfileUpdateOngoing) {
                if (prefs.hasPendingProfileUpdate()) {
                    log("has pending profile update.");
                    handler.post(() -> {
                        turnOnScreen();
                        notifyProfileUpdate();
                    });
                } else {
                    log("no pending profile update.");
                }
            } else {
                log("profile update is ongoing...");
            }

            //terminal disable operation
            if (prefs.isTerminalDisabled()) {
                log("notify terminal disable operation");
                notifyTerminalDisableOperation();
            }

            //generate config map
            if (!prefs.isConfigMapGenerated()) {
                if (!isGeneratingConfigMap) {
                    log("notify config map generation");
                    handler.post(() -> {
                        turnOnScreen();
                        notifyGenerateConfigMap();
                    });
                } else {
                    log("config map generation ongoing...");
                }
            }


//            //tms connection
//            if (AppUtil.isTmsClientAppExists(context)) {
//                log("tms client exists.");
//                if (!BaseAppReceiver.isTMSConnected(context)) {
//                    tmsConIteration++;
//                    log("tms not connected: " + tmsConIteration);
//                    if (tmsConIteration >= 5) {
//                        log("kill tms app.");
//                        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
//                        am.killBackgroundProcesses("com.epic.eatmca");
//                        tmsConIteration = 0;
//                    }
//                } else {
//                    log("tms connected.");
//                    tmsConIteration = 0;
//                }
//            } else {
//                log("tms client not exists.");
//            }


            try {
                Thread.sleep(SLEEP_TIME);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void incrementAutoSettlementDate(IncrementAutoSettlementListener listener) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, 1);

            String nextSettlementDate = new SimpleDateFormat(Const.AUTO_SETTLE_DATE_FORMAT).format(c.getTime());
            DbHandler.getInstance().updateAutoSettleDate(nextSettlementDate,
                    () -> listener.onCompleted(nextSettlementDate));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected interface IncrementAutoSettlementListener {
        void onCompleted(String nextSettlementDate);
    }

    private void notifyTerminalDisableOperation() {
        if (context != null) {
            Intent intent = new Intent(Const.BROADCAST_ACTION_NOTIFY);
            intent.putExtra(Const.BROADCAST_EXTRA_ACTION, Const.BROADCAST_EXTRA_TERMINAL_DISABLE);
            context.sendBroadcast(intent);
        }
    }

    private void notifyProfileUpdate() {
        try {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        handler.post(() -> {
                            if (context != null) {
                                Intent intent = new Intent(Const.BROADCAST_ACTION_NOTIFY);
                                intent.putExtra(Const.BROADCAST_EXTRA_ACTION, Const.BROADCAST_EXTRA_PROFILE_DOWNLOAD);
                                context.sendBroadcast(intent);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyGenerateConfigMap() {
        try {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(7000);
                        handler.post(() -> {
                            if (context != null) {
                                Intent intent = new Intent(Const.BROADCAST_ACTION_NOTIFY);
                                intent.putExtra(Const.BROADCAST_EXTRA_ACTION, Const.BROADCAST_EXTRA_GENERATE_CONFIG_MAP);
                                context.sendBroadcast(intent);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyAutoSettlement() {
        try {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        handler.post(() -> {
                            if (context != null) {
                                Intent intent = new Intent(Const.BROADCAST_ACTION_NOTIFY);
                                intent.putExtra(Const.BROADCAST_EXTRA_ACTION, Const.BROADCAST_EXTRA_AUTO_SETTLEMENT);
                                context.sendBroadcast(intent);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean shouldStartAutoSettle(TCT tct) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Const.AUTO_SETTLE_DATE_FORMAT + " " + Const.AUTO_SETTLE_TIME_FORMAT);
            Date settlementDate = dateFormat.parse(tct.getAutoSettDate() + " " + tct.getAutoSettTime());
            return new Date().after(settlementDate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void turnOnScreen() {
        try {
            if (context != null) {
                PowerManager.WakeLock screenLock = ((PowerManager)
                        context.getSystemService(Context.POWER_SERVICE))
                        .newWakeLock(268435466, TAG);
                screenLock.acquire();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopBackgroundThread() {
        shouldRun = false;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}
