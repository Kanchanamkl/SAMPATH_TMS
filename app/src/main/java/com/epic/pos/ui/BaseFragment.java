package com.epic.pos.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.View;

import com.epic.pos.config.AppMessages;
import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.ui.login.LoginActivity;
import com.epic.pos.util.UiUtil;
import com.epic.pos.view.CustomProgressDialog;

import javax.inject.Inject;

public abstract class BaseFragment<T extends BasePresenter> extends Fragment implements BaseView {
    @Inject protected T mPresenter;
    protected LoginResponse loginData;
    private DialogFragment dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDependencies(((MyApp) getActivity().getApplication()).getAppComponent());
    }


    protected abstract void initDependencies(AppComponent appComponent);


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
    }

    @Override
    public void onDestroy() {

        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void showToastMessage(String message) {
        UiUtil.showToastMessage(getContext(), message);
    }

    @Override
    public void showDialogMessage(String title, String message) {
        UiUtil.showDialogMessage(getContext(), title, message);
    }

    @Override
    public void showDialogMessage(String title, String message, UiUtil.SuccessDialogListener listener) {
        UiUtil.showDialogMessage(getContext(), title, message, listener);
    }

    @Override
    public void showNoInternetAlert() {
        UiUtil.noInternetAlert(getContext());
    }

    @Override
    public void showLoader(@NonNull String title, @NonNull String message) {
        if (dialog == null)
            dialog = CustomProgressDialog.newInstance(title, message);
        dialog.show(getActivity().getSupportFragmentManager(), "CustomProgressDialog");
    }

    @Override
    public void hideLoader() {
        if (dialog != null) dialog.dismiss();
    }

    @Override
    public void launchLogin() {
        startActivity(new Intent(getActivity(), LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void showDialogError(String title, String message, UiUtil.ErrorDialogListener listener) {
        UiUtil.showErrorDialog(getContext(), title, message, listener);
    }


}

