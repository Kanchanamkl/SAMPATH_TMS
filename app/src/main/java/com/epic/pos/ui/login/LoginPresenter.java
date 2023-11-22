package com.epic.pos.ui.login;

import com.epic.pos.config.AppConst;
import com.epic.pos.config.ErrorMessages;
import com.epic.pos.config.ResponseCodes;
import com.epic.pos.data.model.request.LoginRequest;
import com.epic.pos.data.model.respone.ErrorResponse;
import com.epic.pos.data.model.respone.LoginResponse;
import com.epic.pos.data.model.respone.ServiceResponse;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPresenter extends BasePresenter<LoginContract.View> implements LoginContract.Presenter {
    private Repository repository;
    private NetworkConnection networkConnection;

    @Inject
    public LoginPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void login(LoginRequest loginRequest) {
        if (loginRequest.isValidRequest()) {
            if (networkConnection.checkNetworkConnection()) {
//                mView.showLoader();
                repository.login(loginRequest, new Repository.LoginCallback() {
                    @Override
                    public void onSuccessful(ServiceResponse<LoginResponse> serviceResponse) {

                        if (serviceResponse.getStatusCode().equalsIgnoreCase(ResponseCodes.SUCCESS)) {
                            mView.saveLoginData(serviceResponse.getData());
                            //repository.saveUsername(loginRequest.getUserName());
                            mView.launchDashboard();

                        } else if (serviceResponse.getStatusCode().equalsIgnoreCase(ResponseCodes.REDIRECT_TO_CHANGE_PIN)) {
                            //repository.saveUsername(loginRequest.getUserName());
                            mView.launchChangePin(loginRequest.getUserName(), serviceResponse.getData().getMerchanttoken());

                        } else if (serviceResponse.isDescriptionVisibility())
                            mView.showDialogMessage(AppConst.DIALOG_TITLE_ERROR, serviceResponse.getStatusDescription());

                        else
                            mView.showDialogMessage(AppConst.DIALOG_TITLE_ERROR, ErrorMessages.ERROR_MESSAGE_DEFAULT);
                    }

                    @Override
                    public void onFailed(ErrorResponse errorResponse) {
                        if (errorResponse.isDescriptionVisibility())
                            mView.showDialogMessage(AppConst.DIALOG_TITLE_ERROR, errorResponse.getStatusDescription());
                        else
                            mView.showDialogMessage(AppConst.DIALOG_TITLE_ERROR, ErrorMessages.ERROR_MESSAGE_DEFAULT);
                    }

                    @Override
                    public void onFailed(String message) {
                        mView.hideLoader();
                        mView.showDialogMessage(AppConst.DIALOG_TITLE_ERROR, message);
                    }

                    @Override
                    public void onCompleted() {
                        mView.hideLoader();
                        clearData();
                    }
                });
            } else {
                mView.showNoInternetAlert();
            }
        } else {
            mView.showDialogMessage(AppConst.DIALOG_TITLE_ERROR, loginRequest.getErrorMessage());
        }
    }

    private void clearData() {
//        if(repository.getUsername().isEmpty())
//            mView.clearData();
//        else
//            mView.setUsername(repository.getUsername());
    }

    @Override
    public void setUsername() {
//        if(!repository.getUsername().isEmpty())
//            mView.setUsername(repository.getUsername());
    }
}
