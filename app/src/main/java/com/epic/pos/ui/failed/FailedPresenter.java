package com.epic.pos.ui.failed;

import android.os.CountDownTimer;
import com.epic.pos.util.AppLog;

import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;

public class FailedPresenter extends BasePresenter<FailedContract.View> implements FailedContract.Presenter {

    private final String TAG = FailedPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    @Inject
    public FailedPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void startCountDown() {
        log("startCountDown()");
        repository.getTCT(tct -> {
            int countDown = tct.getErrorPageCountdown() * 1000;
            log("Countdown milliseconds: " + countDown);
            new CountDownTimer(countDown, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if (mView != null){
                        mView.onCountDownFinished();
                    }
                }
            }.start();
        });
    }

    private void log(String msg){
        AppLog.i(TAG, msg);
    }
}

