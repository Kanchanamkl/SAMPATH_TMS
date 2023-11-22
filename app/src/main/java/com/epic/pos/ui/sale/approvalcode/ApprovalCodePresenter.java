package com.epic.pos.ui.sale.approvalcode;

import com.epic.pos.common.Const;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-10
 */
public class ApprovalCodePresenter extends BasePresenter<ApprovalCodeContact.View> implements ApprovalCodeContact.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;

    private boolean hasValidApprovalCode = false;
    private String approvalCode;

    @Inject
    public ApprovalCodePresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void onSubmit() {
        repository.saveOfflineApprovalCode(approvalCode);
        mView.gotoTxnDetailActivity();
    }

    @Override
    public void setApprovalCode(String approvalCode) {
        hasValidApprovalCode = validateApprovalCode(approvalCode);
        if (hasValidApprovalCode){
            ApprovalCodePresenter.this.approvalCode = approvalCode;
        }
        validateActionBtn();
    }

    private void validateActionBtn() {
        mView.setActionBtnEnabled(hasValidApprovalCode);
    }

    private boolean validateApprovalCode(String approvalCode) {
        return approvalCode.length() == Const.APPROVAL_CODE_LEN;
    }

}