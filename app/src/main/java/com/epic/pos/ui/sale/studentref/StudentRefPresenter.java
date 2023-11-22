package com.epic.pos.ui.sale.studentref;

import android.text.TextUtils;
import com.epic.pos.util.AppLog;

import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-27
 */
public class StudentRefPresenter extends BasePresenter<StudentRefContact.View> implements StudentRefContact.Presenter {

    private final String TAG = StudentRefPresenter.class.getSimpleName();

    private Repository repository;
    private NetworkConnection networkConnection;
    private String studentRef = "";

    @Inject
    public StudentRefPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void setStudentRef(String studentRef) {
        this.studentRef = studentRef;
        if (isViewNotNull()) {
            mView.setContinueBtnEnabled(!TextUtils.isEmpty(studentRef));
        }
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void init() {

    }

    @Override
    public void onSubmit() {
        repository.saveStudentReferenceNo(studentRef);
        if (isViewNotNull()) {
            mView.goToCardScanActivity();
        }
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}