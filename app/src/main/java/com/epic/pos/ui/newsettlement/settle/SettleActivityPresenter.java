package com.epic.pos.ui.newsettlement.settle;

import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.ui.newsettlement.SettlementHandler;
import com.epic.pos.device.data.PrintError;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-10-22
 */
public class SettleActivityPresenter extends BasePresenter<SettleActivityContact.View>
        implements SettleActivityContact.Presenter, SettlementHandler.SettlementCallbacks {

    private final String TAG = SettleActivityPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private Host host;
    private Merchant merchant;
    private SettlementHandler settlementHandler;

    @Inject
    public SettleActivityPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void initSettlement() {
        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_INIT_SETTLEMENT, Const.MSG_PLEASE_WAIT);
        }

        repository.saveTransactionOngoing(true);
        settlementHandler = new SettlementHandler(repository, host, merchant, false, 0);
        settlementHandler.setSettlementCallbacks(this);
        settlementHandler.initData(new SettlementHandler.InitListener() {
            @Override
            public void onNoTransactions() {
                if (isViewNotNull()) {
                    mView.showTransactionNotFound();
                    mView.hideLoader();
                }
            }

            @Override
            public void onTransactionsExists(String currency, long txnAmount) {
                if (isViewNotNull()) {
                    mView.showSettlementAmount(currency, txnAmount);
                    mView.hideLoader();
                }
            }
        });
    }

    @Override
    public void onPreAuthDeleteClicked() {
        showSettlementLoading();
        settlementHandler.setPreAuthAction(SettlementHandler.PreAuthAction.DELETE);
    }

    @Override
    public void onPreAuthKeepClicked() {
        showSettlementLoading();
        settlementHandler.setPreAuthAction(SettlementHandler.PreAuthAction.KEEP);
    }

    @Override
    public void onDetailReportPrintClicked() {
        showSettlementLoading();
        settlementHandler.setDetailReportPrintAction(SettlementHandler.DetailReportPrintAction.PRINT);
    }

    @Override
    public void onDoNotPrintDetailReportClicked() {
        showSettlementLoading();
        settlementHandler.setDetailReportPrintAction(SettlementHandler.DetailReportPrintAction.DO_NOT_PRINT);
    }

    @Override
    public void rePrintDetailReport() {
        showSettlementLoading();
        settlementHandler.rePrintDetailReport();
    }

    @Override
    public void rePrintSettlementReport() {
        showSettlementLoading();
        settlementHandler.rePrintSettlementReport();
    }

    @Override
    public void onSettleClicked() {
        showSettlementLoading();
        settlementHandler.startSettlement();
    }

    // <editor-fold defaultstate="collapsed" desc="Settlement Callbacks">
    @Override
    public void showPreAuthDeleteConfirmDialog() {
        if (isViewNotNull()) {
            mView.hideLoader();
            mView.showPreAuthDeleteConfirmationDialog();
        }
    }

    @Override
    public void showDetailReportPrintDialog() {
        if (isViewNotNull()) {
            mView.hideLoader();
            mView.showPrintDetailReportDialog();
        }
    }

    @Override
    public void onSettlementCompleted() {
        settlementHandler.stopSettlementThread();
        if (isViewNotNull()) {
            mView.hideLoader();
            mView.onSettlementCompleted();
        }
    }

    @Override
    public void onSettlementStateUpdate(String msg) {
        if (isViewNotNull()){
            mView.onSettlementStateUpdate(msg);
        }
    }

    @Override
    public void onSettlementFailed(String errorMsg) {
        settlementHandler.stopSettlementThread();
        if (isViewNotNull()) {
            mView.hideLoader();
            mView.onSettlementFailed(errorMsg);
        }
    }

    @Override
    public void onDetailReportPrintError(PrintError printError) {
        if (isViewNotNull()) {
            mView.hideLoader();
            mView.detailReportPrintError(printError.getMsg());
        }
    }

    @Override
    public void onSettlementReportPrintError(PrintError printError) {
        if (isViewNotNull()) {
            mView.hideLoader();
            mView.settlementReportPrintError(printError.getMsg());
        }
    }
    // </editor-fold>

    @Override
    public void setSelectedHost(Host host) {
        this.host = host;
    }

    @Override
    public void setSelectedMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    @Override
    public Host getSelectedHost() {
        return host;
    }

    @Override
    public Merchant getSelectedMerchant() {
        return merchant;
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
    }

    private void showSettlementLoading() {
        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_SETTLEMENT_PROCESSING, Const.MSG_PLEASE_WAIT);
        }
    }

    @Override
    public void onDestroy() {
        settlementHandler.stopSettlementThread();
        mView = null;
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

}