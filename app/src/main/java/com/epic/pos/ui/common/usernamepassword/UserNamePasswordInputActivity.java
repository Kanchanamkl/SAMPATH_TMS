package com.epic.pos.ui.common.usernamepassword;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityUserNamePasswordInputBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.home.HomeActivity;
import com.epic.pos.util.UiUtil;

import butterknife.BindString;
import butterknife.ButterKnife;

public class UserNamePasswordInputActivity  extends BaseActivity<UserNamePasswordPresenter> implements  UserNamePasswordContract.View {

    private final String TAG = "UserNamePasswordActivity";
    public static String TRANTYPE = "TRANTYPE";
    ActivityUserNamePasswordInputBinding binding;

    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.msg_invalid_pw_username)
    String msg_invalid_pw_un;
    @BindString(R.string.text_cancel)
    String text_cancel;
    @BindString(R.string.btn_retry)
    String btn_retry;


    public static void startActivityForResult(HomeActivity homeActivity, int trantype,int activity_result_manualsale_pw) {

        Intent i = new Intent(homeActivity, UserNamePasswordInputActivity.class);
        i.putExtra(TRANTYPE, trantype);
        homeActivity.startActivityForResult(i, activity_result_manualsale_pw);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_name_password_input);

        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, "", true);

        mPresenter.initData();

    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
       appComponent.inject(this);
    }


    @Override
    public void passwordOk() {

        int tran = getIntent().getIntExtra(TRANTYPE, 0);
        Intent intent = getIntent();
        intent.putExtra(TRANTYPE, tran);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void invalidPassword() {
        showDialogError(app_name, msg_invalid_pw_un, btn_retry, text_cancel, new UiUtil.OptionDialogListener() {
            @Override
            public void onOkClicked() {
                clearPwText();
            }

            @Override
            public void onCancelClicked() {
                setResult(RESULT_CANCELED, new Intent());
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, new Intent());
        super.onBackPressed();
    }

    public void btnconfirmuserandpassword(View view) {


        Editable username = (Editable) binding.etUsernamemanualsale.getText();
        Editable password = (Editable) binding.etPasswordmanualsale.getText();
        mPresenter.onSubmit(username.toString(),password.toString());


    }

    private void clearPwText() {
        binding.etPasswordmanualsale.setText("");
    }

}