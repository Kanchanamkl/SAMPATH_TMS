package com.epic.pos.ui.newsettlement.auto;

import com.epic.pos.device.listener.AutoSettlementListener;
import com.epic.pos.util.AppLog;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.ui.newsettlement.SettlementHandler;
import com.epic.pos.device.data.PrintError;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-11-01
 */
public class AutoSettlePresenter extends BasePresenter<AutoSettleContact.View> implements AutoSettleContact.Presenter {

    private final String TAG = AutoSettlePresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private TCT tct;
    private SettlementHandler settlementHandler;
    //Settlement related data
    private List<Host> allHosts;

    @Inject
    public AutoSettlePresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void initAutoSettlement() {
        repository.saveTransactionOngoing(true);
        repository.getTCT(t -> {
            tct = t;
            repository.getHostList(hosts -> {
                log("Hosts received: count = " + hosts.size());
                allHosts = hosts;
                checkHosts();
            });
        });
    }

    private void onAutoSettlementCompleted() {
        log("onAutoSettlementCompleted()");
        repository.saveHasPendingAutoSettlement(false);
        repository.setAutoSettlementCompleted(true);
        incrementAutoSettlementDate(nextSettlementDate -> {
            log("Next auto settlement date updated: " + nextSettlementDate);
            if (isViewNotNull()) {
                mView.onAutoSettlementCompleted();
            }
        });
    }

    private void checkHosts() {
        log("checkMerchants()");
        if (allHosts.size() >= 1) {
            log("Hosts exists.");
            Host host = allHosts.get(0);
            repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
                if (merchants.size() >= 1) {
                    log("Merchants exists for host: " + merchants.size());
                    checkMerchants(host, merchants);
                } else {
                    log("No merchants exists for host: " + host.getHostName());
                    allHosts.remove(0);
                    checkHosts();
                }
            });
        } else {
            log("All hosts are empty.");
            onAutoSettlementCompleted();
        }
    }

    private void checkMerchants(Host host, List<Merchant> merchants) {
        if (merchants.size() >= 1) {
            //Start settlement
            Merchant merchant = merchants.get(0);
            String hostAndMerchant = "[ Host = " + host.getHostName() + " | Merchant = " + merchant.getMerchantName() + " ]";
            log("=========================================");
            log("Start settlement for " + hostAndMerchant);
            //Settlement Handler
            settlementHandler = new SettlementHandler(repository, host, merchant, true, tct.getAutoSettlementTryCount());
            settlementHandler.initData(new SettlementHandler.InitListener() {
                @Override
                public void onNoTransactions() {
                    log("No transactions exists.");
                    merchants.remove(0);
                    checkMerchants(host, merchants);
                }

                @Override
                public void onTransactionsExists(String currency, long txnAmount) {
                    log("Transactions exists");
                    settlementHandler.setSettlementCallbacks(new SettlementHandler.SettlementCallbacks() {
                        @Override
                        public void showPreAuthDeleteConfirmDialog() {
                            //not used for auto settlement
                        }

                        @Override
                        public void showDetailReportPrintDialog() {
                            //not used for auto settlement
                        }

                        @Override
                        public void onSettlementCompleted() {
                            log("Auto settlement completed.");
                            settlementHandler.stopSettlementThread();
                            merchants.remove(0);
                            checkMerchants(host, merchants);
                        }

                        @Override
                        public void onSettlementFailed(String errorMsg) {
                            settlementHandler.stopSettlementThread();
                            log("Increment auto settlement date.");
                            repository.saveHasPendingAutoSettlement(false);
                            incrementAutoSettlementDate(nextSettlementDate -> {
                                log("Next settlement date is: " + nextSettlementDate);
                                if (isViewNotNull()) {
                                    mView.onSettlementFailed(errorMsg);
                                }
                            });
                        }

                        @Override
                        public void onSettlementStateUpdate(String msg) {
                            if (isViewNotNull()) {
                                mView.onSettlementStateUpdate(msg);
                            }
                        }

                        @Override
                        public void onDetailReportPrintError(PrintError printError) {
                            if (isViewNotNull()) {
                                mView.onDetailReportPrintError(printError.getMsg());
                            }
                        }

                        @Override
                        public void onSettlementReportPrintError(PrintError printError) {
                            if (isViewNotNull()) {
                                mView.onSettlementReportPrintError(printError.getMsg());
                            }
                        }
                    });
                    settlementHandler.startSettlement();
                }
            });
        } else {
            log("Merchants are empty for host " + host.getHostName());
            allHosts.remove(0);
            checkHosts();
        }
    }

    @Override
    public void rePrintDetailReport() {
        if (settlementHandler != null) {
            settlementHandler.rePrintDetailReport();
        }
    }

    @Override
    public void rePrintSettlementReport() {
        if (settlementHandler != null) {
            settlementHandler.rePrintSettlementReport();
        }
    }

    @Override
    public void closeButtonPressed() {
        settlementHandler.stopSettlementThread();
        repository.saveTransactionOngoing(false);
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

}