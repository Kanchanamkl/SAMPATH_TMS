package com.epic.pos.ui.login;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.epic.pos.R;
import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.databinding.ActivityLoginBinding;
import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.tle.KeyManager;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.UiUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadPresenter.VISA_MASTER_TMK_INDEX;

public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginContract.View {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, getString(R.string.toolbar_login), false);
        init();
        mPresenter.setUsername();

    }

    private void init() {
    }

    @OnTouch(R.id.root)
    boolean onTouchView() {
        UiUtil.hideKeyboard(this);
        return true;
    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }


    @Override
    public void launchDashboard() {
//        Intent intent = new Intent(this, DashboardActivity.class);
//        startActivity(intent);
//        finish();
    }

    @Override
    public void launchChangePin(String userName, String token) {
//        Intent intent = new Intent(this, CurrentPasscodeActivity.class);
//        intent.putExtra(AppConst.EXTRA_TOKEN, token);
//        intent.putExtra(AppConst.EXTRA_USER_NAME, userName);
//        startActivity(intent);
    }

    @Override
    public void clearData() {
        binding.etUsername.setText("");
        binding.etPassword.setText("");
    }

    @Override
    public void saveLoginData(LoginResponse data) {
        ((MyApp) (getApplication())).setLoginData(data);
    }

    @Override
    public void setUsername(String username) {
        binding.etUsername.setText(username);
        binding.etPassword.setText("");
        binding.etUsername.setFocusable(false);
        binding.etUsername.setFocusableInTouchMode(false);
        binding.etUsername.setClickable(false);
    }

    @OnClick(R.id.btnLogin)
    public void OnClickLogin(View view) {
        UiUtil.hideKeyboard(this);
        mPresenter.login(new LoginRequest(binding.etUsername.getText().toString().trim(),
                binding.etPassword.getText().toString().trim(),
                "FirebaseInstanceId.getInstance().getToken()",
                AppUtil.getAndroidId(this)));
    }

    @OnClick(R.id.tvForgotPassword)
    public void OnClickForgotPassword(View view) {
        //TODO
    }

    @OnTextChanged(value = R.id.etUsername, callback = OnTextChanged.Callback.TEXT_CHANGED)
    protected void afterUsernameChanged(CharSequence s, int start, int before, int count) {
        changeLoginBtnColor(s.toString(), binding.etPassword.getText().toString().trim());
    }

    @OnTextChanged(value = R.id.etPassword, callback = OnTextChanged.Callback.TEXT_CHANGED)
    protected void afterPasswordChanged(CharSequence s, int start, int before, int count) {
        changeLoginBtnColor(s.toString(), binding.etUsername.getText().toString().trim());
    }

    private void changeLoginBtnColor(String text1, String text2) {
        if (!TextUtils.isEmpty(text1.toString()) && !TextUtils.isEmpty(text2)) {
            binding.btnLogin.setBackgroundResource(R.drawable.rounded_primary_background);
            binding.btnLogin.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            binding.btnLogin.setBackgroundResource(R.drawable.rounded_primary_border);
            binding.btnLogin.setTextColor(ContextCompat.getColor(this, R.color.gray));
        }
    }

}
