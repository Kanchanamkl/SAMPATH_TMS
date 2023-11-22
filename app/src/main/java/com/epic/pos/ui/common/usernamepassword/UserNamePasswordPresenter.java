package com.epic.pos.ui.common.usernamepassword;

import android.database.Cursor;

import com.epic.pos.data.db.DbHandler;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;


public class UserNamePasswordPresenter  extends BasePresenter<UserNamePasswordContract.View> implements UserNamePasswordContract.Presenter {

    private Repository repository;

    @Inject
    public UserNamePasswordPresenter(Repository repository ) {
        this.repository = repository;
    }

    @Override
    public void initData() {
    repository.saveTransactionOngoing(true);

    }

    @Override
    public void onSubmit(String username,String password) {

        repository.getuuserbyusernameandpassword(username,password, new DbHandler.GetTableDataCursorListener() {

            @Override
            public void onReceived(Cursor cursor) {
                    if(cursor==null){
                        mView.invalidPassword();
                    }else{
                    if(cursor.getCount()>0){
                    mView.passwordOk();
                    cursor.close();
                    }
                    else{
                        mView.invalidPassword();
                    }
                    }

            }
        });

    }
}
