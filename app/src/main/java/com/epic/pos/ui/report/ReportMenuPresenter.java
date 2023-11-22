package com.epic.pos.ui.report;

import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION;
import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION_MANUAL;

import android.graphics.Bitmap;

import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintDataBuilder;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.listener.PrintListener;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ReportMenuPresenter extends BasePresenter<ReportMenuContract.View> implements ReportMenuContract.Presenter {

    private final String TAG = ReportMenuPresenter.class.getSimpleName();

    private Repository repository;
    private NetworkConnection networkConnection;

    private Host hostForHostInfo;
    private Merchant merchantForHostInfo;

    private Host hostForLastSettlement;
    private Merchant merchantForLastSettlement;

    private Host hostForSummary;
    private Merchant merchantForSummary;

    private Host hostForAnyReceipt;
    private Merchant merchantForAnyReceipt;

    private Host hostForDetailReport;
    private Merchant merchantForDetailReport;

    @Inject
    public ReportMenuPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void setHostForSummaryReport(Host host) {
        hostForSummary = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setMerchantForSummaryReport(merchants.get(0));
            } else {
                mView.selectMerchantForSummaryReport(host);
            }
        });
    }

    @Override
    public void setMerchantForSummaryReport(Merchant merchant) {
        merchantForSummary = merchant;
        generateSummaryReport();
    }

    @Override
    public void setHostForDetailReport(Host host) {
        hostForDetailReport = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setMerchantForDetailReport(merchants.get(0));
            } else {
                mView.selectMerchantForDetailReport(host);
            }
        });
    }

    @Override
    public void setMerchantForDetailReport(Merchant merchant) {
        merchantForDetailReport = merchant;
        prepareDetailReport();
    }

    @Override
    public void setHostForAnyReceipt(Host host) {
        hostForAnyReceipt = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setMerchantForAnyReceipt(merchants.get(0));
            } else {
                mView.selectMerchantForAnyReceipt(host);
            }
        });
    }

    @Override
    public void setMerchantForAnyReceipt(Merchant merchant) {
        merchantForAnyReceipt = merchant;
        mView.selectInvoiceForAnyReceipt(hostForAnyReceipt, merchantForAnyReceipt);
    }

    @Override
    public void setTransactionForAnyReceipt(Transaction transaction) {
        hostForAnyReceipt = null;
        merchantForAnyReceipt = null;
        printDuplicateTxnCopy(transaction,true);
    }

    @Override
    public void printLastReceipt() {
        repository.getLastTransaction(transaction -> {
            if (transaction != null) {
                mView.gotoReceiptTypeSelectActivity();
              //  printDuplicateTxnCopy(transaction,true);
            } else {
                mView.showToastMessage(Const.MSG_TXN_EMPTY);
            }
        });
    }


    private void generateSummaryReport() {
        repository.getTransactionByMerchantAndHost(merchantForSummary.getMerchantID(), hostForSummary.getHostID(), transactions -> {
            if (!transactions.isEmpty()) {
                PosDevice.getInstance().startPrinting();
                mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
                repository.getTerminalByMerchant(merchantForSummary.getMerchantNumber(), terminal -> {
                    repository.getCurrencyByMerchantId(merchantForSummary.getMerchantNumber(), currency -> {
                        MyApp.getInstance().getAppReceipts().generateSummaryReport(hostForSummary, merchantForSummary, terminal, currency,
                                transactions, new AppReceipts.GenerateReceiptListener() {
                                    @Override
                                    public void onReceived(Bitmap bitmap) {
                                        Print p = new Print();
                                        p.setPrintType(Print.PRINT_TYPE_IMAGE);
                                        p.setBitmap(bitmap);
                                        p.setPrintListener(new PrintListener() {
                                            @Override
                                            public void onPrintFinished() {
                                                mView.hideLoader();
                                                bitmap.recycle();
                                                PosDevice.getInstance().stopPrinting();
                                            }

                                            @Override
                                            public void onPrintError(PrintError printError) {
                                                mView.hideLoader();
                                                mView.showToastMessage(printError.getMsg());
                                                PosDevice.getInstance().stopPrinting();

                                            }
                                        });

                                        PosDevice.getInstance().addToPrintQueue(p);
                                    }

                                    @Override
                                    public void onGenerateReceiptFailed() {
                                        mView.hideLoader();
                                        PosDevice.getInstance().stopPrinting();
                                    }
                                });
                    });
                });
            } else {
                mView.showToastMessage(Const.MSG_TXN_EMPTY);
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Host Info Report">
    @Override
    public void setHostForHostInfoReport(Host host) {
        hostForHostInfo = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setMerchantForHostInfoReport(merchants.get(0));
            } else {
                mView.selectMerchantForHostInfo(host);
            }
        });
    }

    @Override
    public void setMerchantForHostInfoReport(Merchant merchant) {
        merchantForHostInfo = merchant;
        printHostInfo();
    }

    private void printHostInfo() {
        mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
        PosDevice.getInstance().startPrinting();
        repository.getTerminalByMerchant(merchantForHostInfo.getMerchantNumber(), terminal -> {
            MyApp.getInstance().getAppReceipts().generateHostInfoReport(hostForHostInfo, merchantForHostInfo, terminal, new AppReceipts.GenerateReceiptListener() {
                @Override
                public void onReceived(Bitmap bitmap) {
                    Print p = new Print();
                    p.setPrintType(Print.PRINT_TYPE_IMAGE);
                    p.setBitmap(bitmap);
                    p.setPrintListener(new PrintListener() {
                        @Override
                        public void onPrintFinished() {
                            mView.hideLoader();
                            bitmap.recycle();
                            PosDevice.getInstance().stopPrinting();
                        }

                        @Override
                        public void onPrintError(PrintError printError) {
                            mView.hideLoader();
                            mView.showToastMessage(printError.getMsg());
                            PosDevice.getInstance().stopPrinting();

                        }
                    });
                    PosDevice.getInstance().addToPrintQueue(p);
                }

                @Override
                public void onGenerateReceiptFailed() {
                    mView.hideLoader();
                    PosDevice.getInstance().stopPrinting();
                }
            });
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Last settlement receipt">
    @Override
    public void setHostForLastSettlementReport(Host host) {
        hostForLastSettlement = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setMerchantForLastSettlementReport(merchants.get(0));
            } else {
                mView.selectMerchantForLastSettlementReport(host);
            }
        });
    }

    @Override
    public void setMerchantForLastSettlementReport(Merchant merchant) {
        merchantForLastSettlement = merchant;
        printLastSettlementReport();
    }

    @Override
    public void printLastSettlementReport() {
        Bitmap settlementReceipt = ImageUtils.getInstance()
                .getCustomerSettlementReceipt(hostForLastSettlement.getHostName(), merchantForLastSettlement.getMerchantID());

        hostForLastSettlement = null;
        merchantForLastSettlement = null;

        if (settlementReceipt != null) {
            mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
            PosDevice.getInstance().startPrinting();

            Print p = new Print();
            p.setPrintType(Print.PRINT_TYPE_IMAGE);
            p.setBitmap(settlementReceipt);
            p.setPrintListener(new PrintListener() {
                @Override
                public void onPrintFinished() {
                    mView.hideLoader();
                }

                @Override
                public void onPrintError(PrintError printError) {
                    mView.showToastMessage("Printer Error: " + printError.getMsg());
                    mView.hideLoader();
                }
            });
            PosDevice.getInstance().addToPrintQueue(p);
        } else {
            mView.showToastMessage(Const.MSG_LAST_SETTLEMENT_NOT_FOUND);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Print duplicate copy">
    private void printDuplicateTxnCopy(Transaction transaction,Boolean ismerchantcopy) {
        mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
        PosDevice.getInstance().startPrinting();
        repository.getIssuerById(transaction.getIssuer_number(), issuer ->
                repository.getMerchantById(transaction.getMerchant_no(), merchant ->
                        MyApp.getInstance().getAppReceipts().generateTxnDuplicateReceipt(transaction, merchant, issuer,ismerchantcopy,
                                new AppReceipts.DuplicateReceiptListener() {
                                    @Override
                                    public void onReceiptGenerated(Bitmap bitmap) {
                                        Print p = new Print();
                                        p.setPrintType(Print.PRINT_TYPE_IMAGE);
                                        p.setBitmap(bitmap);
                                        p.setPrintListener(new PrintListener() {
                                            @Override
                                            public void onPrintFinished() {
                                                mView.hideLoader();
                                                bitmap.recycle();
                                                PosDevice.getInstance().stopPrinting();
                                            }

                                            @Override
                                            public void onPrintError(PrintError printError) {
                                                mView.hideLoader();
                                                mView.showToastMessage(printError.getMsg());
                                                PosDevice.getInstance().stopPrinting();
                                            }
                                        });
                                        PosDevice.getInstance().addToPrintQueue(p);
                                    }

                                    @Override
                                    public void onReceiptFailed() {
                                        mView.hideLoader();
                                        PosDevice.getInstance().stopPrinting();
                                    }
                                })));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Detail Report and Summary Report">
    private List<PrintDataBuilder> printDataBuilders;
    private int printIndex;
    private String printErrorMsg = "";

    private void prepareDetailReport() {
        log("prepareDetailReport");
        mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
        log("MID : " + merchantForDetailReport.getMerchantID());
        log("HOST : " + hostForDetailReport.getHostID());
        repository.getTransactionByMerchantAndHost(merchantForDetailReport.getMerchantID(), hostForDetailReport.getHostID(), allTransactions -> {
            ArrayList<Transaction> detailReportTransactions = new ArrayList<>();
            for (Transaction t : allTransactions) {
                if (!isPreAuthTxn(t.getTransaction_code())) {
                    detailReportTransactions.add(t);
                }
            }

            log("transaction count : " + detailReportTransactions.size());
            if (detailReportTransactions.size() >= 1) {
                PosDevice.getInstance().startPrinting();
                repository.getTerminalByMerchant(merchantForDetailReport.getMerchantNumber(), terminal -> {
                    log("terminal " + terminal.getTerminalID());
                    repository.getCurrencyByMerchantId(merchantForDetailReport.getMerchantNumber(), currency -> {
                        log("currency " + currency.getCurrencySymbol());
                        repository.getAllIssuers(issuers -> {
                            log("Issuers received count " + issuers.size());
                            MyApp.getInstance().getAppReceipts()
                                    .generateDetailReportReceiptArvin(hostForDetailReport,
                                            merchantForDetailReport, terminal, currency, issuers,
                                            detailReportTransactions, new AppReceipts.ReceiptBuilderListener() {
                                                @Override
                                                public void onReceiptGenerated(List<PrintDataBuilder> pdbs) {
                                                    printDataBuilders = pdbs;
                                                    printIndex = 0;
                                                    startDetailReportPrint();
                                                }

                                                @Override
                                                public void onReceiptGenerationFailed() {
                                                    mView.hideLoader();
                                                    mView.showToastMessage("Receipt Error: " + printErrorMsg);
                                                }
                                            });
                        });
                    });
                });
            } else {
                mView.hideLoader();
                mView.showToastMessage(Const.MSG_TXN_EMPTY);
            }
        });
    }

    private void startDetailReportPrint() {
        PrintDataBuilder p = printDataBuilders.get(printIndex);

        Print print = new Print();
        print.setPrintType(Print.PRINT_DATA_BUILDER);
        print.setPrintDataBuilder(p);
        print.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                if ((printIndex + 1) == printDataBuilders.size()) {
                    printIndex = 0;
                    printDataBuilders.clear();
                    log("detail report print completed");
                    PosDevice.getInstance().stopPrinting();
                    mView.hideLoader();
                } else {
                    printIndex += 1;
                    startDetailReportPrint();
                }
            }

            @Override
            public void onPrintError(PrintError printError) {
                PosDevice.getInstance().clearPrintQueue();
                PosDevice.getInstance().stopPrinting();
                mView.hideLoader();
                printErrorMsg = printError.getMsg();
                mView.hideLoader();
                mView.showToastMessage("Printer Error: " + printErrorMsg);
                log("detail report print error");
                PosDevice.getInstance().stopPrinting();
            }
        });

        PosDevice.getInstance().addToPrintQueue(print);
    }
    // </editor-fold>

    private boolean isPreAuthTxn(int txnCode) {
        return (txnCode == SALE_PRE_AUTHORIZATION
                || txnCode == SALE_PRE_AUTHORIZATION_MANUAL);
    }

    @Override
    public void onExitApp() {
        new PosDevice.Builder().clearAll();
        repository.saveForceLoadHome(false);
       // PosDevice.getInstance().platform.disableUsbCdc();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}