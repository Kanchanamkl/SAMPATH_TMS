package com.epic.pos.ui.common.password;

import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;

public class PasswordPresenter extends BasePresenter<PasswordContract.View> implements PasswordContract.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;
    private TCT tct;
    private PasswordType passwordType;

    @Inject
    public PasswordPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }


    @Override
    public void setPasswordType(PasswordType passwordType) {
        this.passwordType = passwordType;
    }

    @Override
    public void initData() {
        repository.saveTransactionOngoing(true);
        repository.getTCT(tct -> PasswordPresenter.this.tct = tct);
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void onSubmit(String password, boolean isTLE) {
        if (isTLE) {
            if (!password.equals("")) {
                repository.saveTLEPwd(password);
                mView.passwordTLEOk();
            } else {
                mView.invalidPassword();
            }
        } else {
            String checkPassword = tct.getExitPassword();

            if (passwordType == PasswordType.MANAGER) {
                checkPassword = tct.getManagerPassword();
            } else if (passwordType == PasswordType.SUPER) {
                checkPassword = tct.getSuperPassword();
            } else if (passwordType == PasswordType.CLEAR_REVERSAL) {
                checkPassword = tct.getClearReversalPassword();
            } else if (passwordType == PasswordType.EDIT_TABLE_PASSWORD) {
                checkPassword = tct.getEditTablePassword();
            }

            if (checkPassword.equals(password)) {
                mView.passwordOk();
            } else {
                mView.invalidPassword();
            }
        }
    }
}