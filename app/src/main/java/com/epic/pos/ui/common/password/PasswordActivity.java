package com.epic.pos.ui.common.password;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.config.AppConst;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.databinding.ActivityPasswordBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.merchant.MerchantType;
import com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity;
import com.epic.pos.util.UiUtil;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.EXTRA_SELECTED_HOST;

public class PasswordActivity extends BaseActivity<PasswordPresenter> implements PasswordContract.View, View.OnClickListener {

    private final String TAG = "PasswordActivity";
    private ActivityPasswordBinding binding;
    public static final String EXTRAS_PWD = "EXTRAS_PWD";
    private static boolean isTLERequest = false;
    public static final String EXTRA_PW_TYPE = "EXTRA_PW_TYPE";

    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.msg_invalid_pw)
    String msg_invalid_pw;
    @BindString(R.string.text_cancel)
    String text_cancel;
    @BindString(R.string.btn_retry)
    String btn_retry;

    public static void startActivityForResult(BaseActivity activity, PasswordType passwordType, int resCode) {
        isTLERequest = false;
        Intent i = new Intent(activity, PasswordActivity.class);
        i.putExtra(EXTRA_PW_TYPE, passwordType.getVal());
        activity.startActivityForResult(i, resCode);
    }

    public static void startActivityForTLE(BaseActivity activity, Host selectedHost) {
        isTLERequest = true;
        activity.startActivity(new Intent(activity, PasswordActivity.class).
                putExtra(AppConst.EXTRA_SELECTED_HOST, selectedHost));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_password);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, "", true);
        if(isTLERequest){
            binding.textView5.setText(getString(R.string.toolbar_enter_pin));
        }
        getExtras();
        initViews();
        mPresenter.initData();
    }

    @Override
    protected void onCloseButtonPressed() {
        mPresenter.closeButtonPressed();
    }

    private void getExtras(){
        int pwTypeVal = getIntent().getIntExtra(EXTRA_PW_TYPE, PasswordType.EXIT_PASSWORD.getVal());
        mPresenter.setPasswordType(PasswordType.valueOf(pwTypeVal));
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null && "number_button".equals(view.getTag())) {
            binding.tvPassword.append(((TextView) view).getText());

        } else {
            switch (view.getId()) {
                case R.id.ivReload: { // handle clear button
                    clearPwText();
                    break;
                }
                case R.id.ivDelete: { // handle backspace button
                    // delete one character
                    Editable editable = (Editable) binding.tvPassword.getText();
                    int charCount = editable.length();
                    if (charCount > 0) {
                        editable.delete(charCount - 1, charCount);
                    }
                    break;
                }
            }
        }
    }

    private void initViews() {
        $(R.id.tv0).setOnClickListener(this);
        $(R.id.tv1).setOnClickListener(this);
        $(R.id.tv2).setOnClickListener(this);
        $(R.id.tv3).setOnClickListener(this);
        $(R.id.tv4).setOnClickListener(this);
        $(R.id.tv5).setOnClickListener(this);
        $(R.id.tv6).setOnClickListener(this);
        $(R.id.tv7).setOnClickListener(this);
        $(R.id.tv8).setOnClickListener(this);
        $(R.id.tv9).setOnClickListener(this);
        $(R.id.ivReload).setOnClickListener(this);
        $(R.id.ivDelete).setOnClickListener(this);
    }

    protected <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }

    @OnClick(R.id.btnConfirm)
    public void onClickConfirm(View view) {
        mPresenter.onSubmit(binding.tvPassword.getText().toString(), isTLERequest);
    }

    @Override
    public void passwordOk() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    @Override
    public void passwordTLEOk() {
        KeyDownloadActivity.startActivity(this, getIntent().getParcelableExtra(EXTRA_SELECTED_HOST));
    }

    @Override
    public void invalidPassword() {
        showDialogError(app_name, msg_invalid_pw, btn_retry, text_cancel, new UiUtil.OptionDialogListener() {
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

    private void clearPwText() {
        binding.tvPassword.setText("");
    }

    @Override
    public void onBackPressed() {
        mPresenter.closeButtonPressed();
        setResult(RESULT_CANCELED, new Intent());
        super.onBackPressed();
    }
}