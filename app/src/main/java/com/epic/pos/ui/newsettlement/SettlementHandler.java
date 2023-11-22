package com.epic.pos.ui.newsettlement;

import static com.epic.pos.common.TranTypes.CASH_ADVANCE;
import static com.epic.pos.common.TranTypes.CASH_BACK;
import static com.epic.pos.common.TranTypes.QR_SALE;
import static com.epic.pos.common.TranTypes.QUASI_CASH;
import static com.epic.pos.common.TranTypes.QUASI_CASH_MANUAL;
import static com.epic.pos.common.TranTypes.SALE;
import static com.epic.pos.common.TranTypes.SALE_INSTALLMENT;
import static com.epic.pos.common.TranTypes.SALE_MANUAL;
import static com.epic.pos.common.TranTypes.SALE_OFFLINE;
import static com.epic.pos.common.TranTypes.SALE_OFFLINE_MANUAL;
import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION;
import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION_MANUAL;
import static com.epic.pos.common.TranTypes.SALE_PRE_COMPLETION;
import static com.epic.pos.common.TranTypes.SALE_REFUND;
import static com.epic.pos.common.TranTypes.SALE_REFUND_MANUAL;
import static com.epic.pos.iso.modal.response.SettlementResponse.RES_CODE_BATCH_UPLOAD;
import static com.epic.pos.iso.modal.response.SettlementResponse.RES_CODE_SUCCESS;

import android.graphics.Bitmap;

import com.epic.pos.common.Const;
import com.epic.pos.common.ErrorMsg;
import com.epic.pos.common.MustSettleTypes;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintDataBuilder;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.listener.PrintListener;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.iso.modal.request.BatchUploadRequest;
import com.epic.pos.iso.modal.request.OfflineSaleRequest;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.SettlementRequest;
import com.epic.pos.iso.modal.response.BatchUploadResponse;
import com.epic.pos.iso.modal.response.OfflineSaleResponse;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.iso.modal.response.SettlementResponse;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.receipt.modal.SettlementReceipt;
import com.epic.pos.tle.TLEData;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.ImageUtils;
import com.epic.pos.util.ValidatorUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Settlement handler
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-10-29
 */
public class SettlementHandler extends BasePresenter {

    private final String TAG = SettlementHandler.class.getSimpleName();
    private final int SETTLEMENT_RETRY_INTERVAL = 2000;

    //init data
    private Repository repository;
    private Host host;
    private Merchant merchant;
    private boolean isAutoSettlement;
    private int settlementRetryCount;
    private SettlementCallbacks settlementCallbacks;
    //other related data
    private boolean startSettlement;
    private boolean isPreAuthTxnAvailable;
    private Terminal terminal;
    private Currency currency;
    private List<Issuer> allIssuers;
    //All transactions
    private ArrayList<Transaction> allTransactions;
    //Transaction categories
    public ArrayList<Transaction> saleTransactions = new ArrayList<>();
    public ArrayList<Transaction> QRTransactions = new ArrayList<>();
    public ArrayList<Transaction> offlineSaleTransactions = new ArrayList<>();
    public ArrayList<Transaction> offlineSaleTransactionstoupload = new ArrayList<>();
    public ArrayList<Transaction> voidTransactions = new ArrayList<>();
    //Transaction batch
    public ArrayList<Transaction> transactionBatch = new ArrayList<>();
    //Transaction batch
    public ArrayList<Transaction> batchuploadingtrans = new ArrayList<>();
    //Settlement and batch uplaod related
    private SettlementRequest settlementRequest = null;
    private SettlementResponse settlementResponse = null;
    private SettlementReceipt settlementReceipt = null;

    //Settlement process
    public enum PreAuthAction {
        DEFAULT, DELETE, KEEP
    }

    public enum DetailReportPrintAction {
        DEFAULT, PRINT, DO_NOT_PRINT
    }

    private enum State {
        ENABLE_MUST_SETTLE_FLAG,
        CHECK_PRE_AUTH_TXN,
        PRE_AUTH_TXN_ACTION,
        DETAIL_REPORT_PRINT_ACTION,
        DETAIL_REPORT_PRINT_RESUME,
        CHECK_REVERSALS,
        CHECK_OFFLINE_SALES,
        SETTLEMENT_REQUEST,
        BATCH_UPLOAD,
        PRINT_SETTLEMENT_REPORT,
        UPDATE_TABLES
    }

    private State state = State.ENABLE_MUST_SETTLE_FLAG;
    private PreAuthAction preAuthAction = PreAuthAction.DEFAULT;
    private DetailReportPrintAction detailReportPrintAction = DetailReportPrintAction.DEFAULT;

    private boolean enableMustSettleStarted = false;
    private boolean checkPreAuthTxnStarted = false;
    private boolean preAuthDeletingStarted = false;
    private boolean generateDetailReportStarted = false;
    private boolean checkReversalsStarted = false;
    private boolean checkOfflineSaleStarted = false;
    private boolean settlementRequestStarted = false;
    private boolean batchUploadStarted = false;
    private boolean settlementReportPrintStarted = false;
    private boolean updateTablesStarted = false;
    private boolean resumeDetailReportPrintStarted = false;

    /**
     * Create the object of SettlementHandler
     *
     * @param repository           repository
     * @param host                 host object
     * @param merchant             merchant object
     * @param isAutoSettlement     is auto settlement or not
     * @param settlementRetryCount this value only used in auto settlement
     */
    public SettlementHandler(Repository repository, Host host, Merchant merchant, boolean isAutoSettlement, int settlementRetryCount) {
        this.repository = repository;
        this.host = host;
        this.merchant = merchant;
        this.isAutoSettlement = isAutoSettlement;
        this.settlementRetryCount = settlementRetryCount;
    }

    /**
     * Init settlement with data
     *
     * @param listener
     */
    public void initData(InitListener listener) {
        repository.getTransactionByMerchantAndHost(merchant.getMerchantID(), host.getHostID(), transactions -> {
            log("Transactions received: count = " + transactions.size());
            allTransactions = transactions;
            for (Transaction transaction : transactions) {
                int txnCode = transaction.getTransaction_code();
                log("Transactions received: txnCode = " + txnCode);
                if (isOfflineTxn(txnCode)) {
                    log("Transactions received: ISOFFLINE = " + txnCode);
                    //Offline Sale, Offline Manual and Pre Comp
                    if (transaction.getVoided() == 0) {
                        log("Transactions received: NOT VOIDED = " + txnCode);
                        offlineSaleTransactions.add(transaction);
                        offlineSaleTransactionstoupload.add(transaction);

                    } else {
                        voidTransactions.add(transaction);
                    }
                } else if (isSaleTxn(txnCode)) {
                    if (transaction.getVoided() == 0) {
                        saleTransactions.add(transaction);
                    } else {
                        voidTransactions.add(transaction);
                    }
                }
                else if (txnCode == QR_SALE) {
                     QRTransactions.add(transaction);
                }

                if (isPreAuthTxn(txnCode)) {
                    isPreAuthTxnAvailable = true;
                    log("Pre auth txn exists");
                }
            }

            if(host.getHostID()==4){
            transactionBatch.addAll(QRTransactions);}
            transactionBatch.addAll(saleTransactions);
            transactionBatch.addAll(offlineSaleTransactions);

            log("Get all issues.");
            repository.getAllIssuers(issuers -> {
                SettlementHandler.this.allIssuers = issuers;
                log("Get terminal by host and merchant.");
                repository.getTMIFByHostAndMerchant(host.getHostID(), merchant.getMerchantNumber(), terminal -> {
                    SettlementHandler.this.terminal = terminal;
                    log("Get currency by merchant.");
                    repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                        SettlementHandler.this.currency = currency;
                        if (transactionBatch.isEmpty() && voidTransactions.isEmpty()) {
                            log("No transactions found.");
                            if (listener != null) {
                                listener.onNoTransactions();
                            }
                        } else {
                            log("Transactions exists");
                            long total = calcTotalAmount();
                            if (listener != null) {
                                listener.onTransactionsExists(currency.getCurrencySymbol(), total);
                            }
                        }
                    });
                });
            });
        });
    }

    /**
     * Start settlement thread
     */
    public void startSettlement() {
        log("startSettlement()");
        if (!startSettlement) {
            startSettlement = true;
            new Thread() {
                @Override
                public void run() {
                    while (startSettlement) {
                        if (state == SettlementHandler.State.ENABLE_MUST_SETTLE_FLAG) {
                            if (!enableMustSettleStarted) {
                                settlementCallbacks.onSettlementStateUpdate("Updating must settle flag.");
                                log("Turn on must settle flag...");
                                repository.updateMustSettleFlagByHostId(host.getHostID(), MustSettleTypes.MUST_SETTLE_ON, () -> {
                                    log("Must settle flag updated.");
                                    if (isAutoSettlement) {
                                        log("Auto settlement");
                                        detailReportPrintAction = DetailReportPrintAction.PRINT;
                                        state = SettlementHandler.State.DETAIL_REPORT_PRINT_ACTION;
                                    } else {
                                        log("No an auto settlement.");
                                        state = SettlementHandler.State.CHECK_PRE_AUTH_TXN;
                                    }
                                });
                            }
                            enableMustSettleStarted = true;
                        } else if (state == SettlementHandler.State.CHECK_PRE_AUTH_TXN) {
                            if (!checkPreAuthTxnStarted) {
                                log("Check pre-auth transactions.");
                                if (isPreAuthTxnAvailable) {
                                    log("Pre auth txn exists. Show Pre auth delete confirmation dialog.");
                                    settlementCallbacks.showPreAuthDeleteConfirmDialog();
                                    state = SettlementHandler.State.PRE_AUTH_TXN_ACTION;
                                } else {
                                    log("Pre auth txn not exists.");
                                    state = SettlementHandler.State.DETAIL_REPORT_PRINT_ACTION;
                                    log("Show detail report print dialog.");
                                    settlementCallbacks.showDetailReportPrintDialog();
                                }
                            }
                            checkPreAuthTxnStarted = true;
                        } else if (state == SettlementHandler.State.PRE_AUTH_TXN_ACTION) {
                            if (preAuthAction == PreAuthAction.DELETE) {
                                if (!preAuthDeletingStarted) {
                                    log("Deleting pre-auth transactions.");
                                    settlementCallbacks.onSettlementStateUpdate("Deleting pre-auth transactions.");
                                    repository.deleteTransactionByMerchantAndHostAndTxnCodes(
                                            merchant.getMerchantID(), host.getHostID(), getPreAuthTxnTypes(), () -> {
                                                log("Pre auth transactions are deleted.");
                                                state = SettlementHandler.State.DETAIL_REPORT_PRINT_ACTION;
                                                log("Show detail report print dialog.");
                                                settlementCallbacks.showDetailReportPrintDialog();
                                            });
                                }
                                preAuthDeletingStarted = true;
                            } else if (preAuthAction == PreAuthAction.KEEP) {
                                log("Keep pre-auth transactions.");
                                state = SettlementHandler.State.DETAIL_REPORT_PRINT_ACTION;
                                log("Show detail report print dialog.");
                                settlementCallbacks.showDetailReportPrintDialog();
                            }
                        } else if (state == SettlementHandler.State.DETAIL_REPORT_PRINT_ACTION) {
                            if (detailReportPrintAction == DetailReportPrintAction.PRINT) {
                                log("Print detail report clicked.");
                                if (!generateDetailReportStarted) {
                                    generateDetailReport();
                                }
                                generateDetailReportStarted = true;
                            } else if (detailReportPrintAction == DetailReportPrintAction.DO_NOT_PRINT) {
                                log("Do not print detail report.");
                                state = SettlementHandler.State.CHECK_REVERSALS;
                            }
                        } else if (state == SettlementHandler.State.DETAIL_REPORT_PRINT_RESUME) {
                            if (!resumeDetailReportPrintStarted) {
                                resumePrintDetailReport();
                            }
                            resumeDetailReportPrintStarted = true;
                        } else if (state == SettlementHandler.State.CHECK_REVERSALS) {
                            if (!checkReversalsStarted) {
                                log("Start to check reversals.");
                                checkReversals();
                            }
                            checkReversalsStarted = true;
                        } else if (state == SettlementHandler.State.CHECK_OFFLINE_SALES) {
                            if (!checkOfflineSaleStarted) {
                                log("Start check offline sales.");
                                checkOfflineSales();
                            }
                            checkOfflineSaleStarted = true;
                        } else if (state == SettlementHandler.State.SETTLEMENT_REQUEST) {
                            if (!settlementRequestStarted) {
                                log("Send settlement request.");
                                if(host.getHostID()==4){
                                    state=SettlementHandler.State.PRINT_SETTLEMENT_REPORT;
                                }else{
                                sendSettlementRequest();}
                            }
                            settlementRequestStarted = true;
                        } else if (state == SettlementHandler.State.BATCH_UPLOAD) {
                            if (!batchUploadStarted) {
                                log("Start batch upload.");

                                batchuploadingtrans.addAll(saleTransactions);
                                batchuploadingtrans.addAll(offlineSaleTransactions);
                                uploadTransactionBatch();
                            }
                            batchUploadStarted = true;
                        } else if (state == SettlementHandler.State.PRINT_SETTLEMENT_REPORT) {
                            if (!settlementReportPrintStarted) {
                                generateSettlementReport();
                            }
                            settlementReportPrintStarted = true;
                        } else if (state == SettlementHandler.State.UPDATE_TABLES) {
                            if (!updateTablesStarted) {
                                updateDb();
                            }
                            updateTablesStarted = true;
                        }

                        try {
                            Thread.sleep(500);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    log("Settlement thread stopped.");
                }
            }.start();
            log("Settlement thread started.");
        } else {
            log("Settlement thread already started.");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Update database">
    private void updateDb() {
        log("updateDb()");
        settlementCallbacks.onSettlementStateUpdate("Updating database.");
        String batchNo = String.valueOf(Integer.parseInt(merchant.getBatchNumber()) + 1);
        int mNumber = merchant.getMerchantNumber();
        String mId = merchant.getMerchantID();
        int hId = host.getHostID();

        repository.updateBatchIdByMerchantId(mNumber, batchNo, () -> {
            log("Batch number updated: merchant_no = " + mNumber + " | batch = " + batchNo);
            repository.deleteTransactionByMerchantAndHostAndTxnCodes(mId, hId, getDeleteTxnTypes(), () -> {
                log("Transactions deleted: mid = " + mId + " host = " + hId);
                repository.updateMustSettleFlagByHostId(hId, MustSettleTypes.MUST_SETTLE_OFF, () -> {
                    log("Turn off must settle flag.");
                    log("Settlement completed.");
                    settlementCallbacks.onSettlementCompleted();
                });
            });
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generate Settlement Report">
    private void generateSettlementReport() {
        log("generateSettlementReport()");
        settlementCallbacks.onSettlementStateUpdate("Generating settlement report.");
        settlementReceipt = new SettlementReceipt();
        settlementReceipt.setAddressLine1(merchant.getRctHdr1());
        settlementReceipt.setAddressLine2(merchant.getRctHdr2());
        settlementReceipt.setAddressLine3(merchant.getRctHdr3());

        try {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);

            Date txnDateTime = new SimpleDateFormat(Const.TXN_DATE_TIME, Locale.ENGLISH)
                    .parse(year + settlementResponse.getDate() + " " + settlementResponse.getTime());
            String receiptDateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH)
                    .format(txnDateTime);
            settlementReceipt.setDateTime(receiptDateTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        settlementReceipt.setMerchantId(merchant.getMerchantID());
        settlementReceipt.setTerminalId(terminal.getTerminalID());
        settlementReceipt.setBatchNo(merchant.getBatchNumber());
        settlementReceipt.setHost(host.getHostName());

        ArrayList<Transaction> settlementReportTransactions = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (!isPreAuthTxn(t.getTransaction_code())) {
                settlementReportTransactions.add(t);
            }
        }

        settlementReceipt.setTransactionList(settlementReportTransactions);
        settlementReceipt.setCurrency(currency.getCurrencySymbol());

        MyApp.getInstance().getAppReceipts().generateSettlementReceipt(settlementReceipt, new AppReceipts.ReceiptListener() {
            @Override
            public void onReceiptGenerated() {
                log("Settlement report generated.");
                printSettlementReport();
            }

            @Override
            public void onReceiptGenerationFailed() {
                log("Settlement report generation failed.");
                settlementCallbacks.onSettlementFailed(Const.MSG_SETTLEMENT_REPORT_ERROR);
            }
        });
    }

    private void printSettlementReport() {
        log("printSettlementReport()");
        settlementCallbacks.onSettlementStateUpdate("Printing settlement report.");
        Bitmap bitmap = ImageUtils.getInstance().getCustomerSettlementReceipt(settlementReceipt.getHost(), settlementReceipt.getMerchantId());
        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_IMAGE);
        p.setBitmap(bitmap);
        p.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                log("Settlement report print finished.");
                bitmap.recycle();
                state = State.UPDATE_TABLES;
            }

            @Override
            public void onPrintError(PrintError printError) {
                log("Settlement report print error: " + printError.getMsg());
                settlementCallbacks.onSettlementReportPrintError(printError);
            }
        });
        PosDevice.getInstance().addToPrintQueue(p);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Upload transactions batch">
    private void uploadTransactionBatch() {
        log("Upload transactions batch.");
        if (batchuploadingtrans.isEmpty()) {
            log("Transaction batch is empty. Batch upload completed.");
            // Set settlement processing code to 960000 after batch upload.
            settlementRequest.setProcessingCode(settlementRequest.getPostSettlementProcessingCode());
            settlementRequestStarted = false;
            state = State.SETTLEMENT_REQUEST;
        } else {
            Transaction txn = batchuploadingtrans.get(0);
            log("Transaction exists: Invoice = " + txn.getInvoice_no());

            log("Increment merchant trace number.");
            merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
            String traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));
            log("traceNo: " + traceNo);
            repository.updateMerchant(merchant, null);

            BatchUploadRequest request = createBatchUploadRequest(traceNo, txn);

            Issuer issuer = getHostContainsIssuer();

            TLEData tleData = new TLEData();
            tleData.setChipStatus(request.getChip_status());
            tleData.setHostId(host.getHostID());
            tleData.setIssuerId(issuer.getIssuerNumber());
            tleData.setPan(request.getPan());
            tleData.setTrack2(request.getTrack2());
            tleData.setTleEnable(host.getTLEEnabled() == 1);

            settlementCallbacks.onSettlementStateUpdate("Uploading transaction batch. (" + txn.getInvoice_no() + ")");
            log("Sending batch upload request.");
            repository.batchUploadRequest(issuer, request, tleData, new Repository.BatchUploadRequestListener() {
                @Override
                public void onReceived(BatchUploadResponse response) {
                    validateBatchUploadResponse(request, response, (isValid, error) -> {
                        if (isValid) {
                            log("Batch upload response validated.");
                            batchuploadingtrans.remove(0);
                            uploadTransactionBatch();
                        } else {
                            log("Batch upload response validation failed.");
                            settlementCallbacks.onSettlementFailed(error);
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    log("Batch upload request error. " + throwable.getMessage());
                    if (isAutoSettlement) {
                        if (settlementRetryCount > 1) {
                            log("Retry count = " + settlementRetryCount);
                            log("Retry batch upload request.");
                            uploadTransactionBatch();
                            settlementRetryCount -= 1;
                        } else {
                            settlementCallbacks.onSettlementFailed(Const.MSG_BATCH_UPLOAD_ERROR);
                        }
                    } else {
                        settlementCallbacks.onSettlementFailed(Const.MSG_BATCH_UPLOAD_ERROR);
                    }
                }

                @Override
                public void TLEError(String error) {
                    log("Batch upload request error. TLEError = " + error);
                    settlementCallbacks.onSettlementFailed(Const.MSG_PLEASE_DOWNLOAD_TLE_KEY);
                }

                @Override
                public void onCompleted() {
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Settlement request">
    private void sendSettlementRequest() {
        log("sendSettlementRequest()");
        log("Increment merchant trace number.");
        merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
        String traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));
        repository.updateMerchant(merchant, null);

        Issuer issuer = getHostContainsIssuer();
        long totalAmount = calcTotalAmount();
        int batchSize = transactionBatch.size();
        log("Total amount: " + totalAmount);
        log("Batch size: " + batchSize);

        if (settlementRequest == null) {
            settlementRequest = new SettlementRequest();
        }

        settlementRequest.setTraceNumber(traceNo);
        settlementRequest.setTid(terminal.getTerminalID());
        settlementRequest.setMid(merchant.getMerchantID());
        settlementRequest.setBatchNo(ValidatorUtil.getInstance().zeroPadString(merchant.getBatchNumber(), 6));
        settlementRequest.setTxnCountAndAmount(
                ValidatorUtil.getInstance().zeroPadString(String.valueOf(batchSize), 3)
                        + ValidatorUtil.getInstance().zeroPadString(String.valueOf(totalAmount), 12)
                        + ValidatorUtil.getInstance().zeroPadString("", 75)
        );
        settlementRequest.setNii(terminal.getNII());
        settlementRequest.setSecureNii(terminal.getSecureNII());
        settlementRequest.setTpdu(terminal.getTPDU());

        TLEData tleData = new TLEData();
        tleData.setHostId(host.getHostID());
        tleData.setIssuerId(issuer.getIssuerNumber());
        tleData.setTleEnable(host.getTLEEnabled() == 1);

        settlementCallbacks.onSettlementStateUpdate("Sending settlement request.");
        log("Starting settlement request.");
        repository.settlementRequest(issuer, settlementRequest, tleData, new Repository.SettlementRequestListener() {
            @Override
            public void onReceived(SettlementResponse response) {
                settlementResponse = response;
                log("Settlement response received. code = " + response.getResponseCode());
                validateSettlementResponse(settlementRequest, response, (isValid, error) -> {
                    if (isValid) {
                        log("Settlement request validated.");
                        if (response.getResponseCode().equals(RES_CODE_SUCCESS)) {
                            log("Settlement Success");
                            state = State.PRINT_SETTLEMENT_REPORT;
                        } else if (response.getResponseCode().equals(RES_CODE_BATCH_UPLOAD)) {
                            log("Settlement response batch upload.");
                            batchUploadStarted = false;
                            state = State.BATCH_UPLOAD;
                        } else {
                            String errorMsg = ErrorMsg.getErrorMsg("Settlement", response.getResponseCode());
                            settlementCallbacks.onSettlementFailed(errorMsg);
                        }
                    } else {
                        log("Invalid settlement response: " + error);
                        settlementCallbacks.onSettlementFailed("Settlement response validation failed.");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                log("Settlement request error. " + throwable.getMessage());
                if (isAutoSettlement) {
                    if (settlementRetryCount > 1) {
                        log("Retry count = " + settlementRetryCount);
                        log("Retry settlement request.");
                        sendSettlementRequest();
                        settlementRetryCount -= 1;
                    } else {
                        settlementCallbacks.onSettlementFailed(Const.MSG_SETTLEMENT_ERROR);
                    }
                } else {
                    settlementCallbacks.onSettlementFailed(Const.MSG_SETTLEMENT_ERROR);
                }
            }

            @Override
            public void TLEError(String error) {
                log("Settlement request TLE error");
                settlementCallbacks.onSettlementFailed(Const.MSG_PLEASE_DOWNLOAD_TLE_KEY);
            }

            @Override
            public void onCompleted() {
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Check offline sales and send">
    private void checkOfflineSales() {
        if (offlineSaleTransactionstoupload.isEmpty()) {
            log("Offline sales are empty.");
            settlementRequestStarted = false;
            state = State.SETTLEMENT_REQUEST;
        } else {
            log("Offline sale transactions are exists. Count = " + offlineSaleTransactionstoupload.size());
            sendOfflineSalesRequests();
        }
    }

    private void sendOfflineSalesRequests() {
        log("sendOfflineSalesRequests()");
        if (!offlineSaleTransactionstoupload.isEmpty()) {
            Transaction txn = offlineSaleTransactionstoupload.get(0);
            String msg = "Sending offline sale request. (" + txn.getInvoice_no() + ")";
            log(msg);
            settlementCallbacks.onSettlementStateUpdate(msg);

            Issuer issuer = getHostContainsIssuer();

            TLEData tleData = new TLEData();
            tleData.setChipStatus(txn.getChip_status());
            tleData.setHostId(host.getHostID());
            tleData.setIssuerId(issuer.getIssuerNumber());
            tleData.setPan(txn.getPan());
            tleData.setTrack2(txn.getTrack2());
            tleData.setTleEnable(host.getTLEEnabled() == 1);

            OfflineSaleRequest request = createOfflineSaleRequest(txn);
            repository.offlineSaleRequest(issuer, request, tleData, new Repository.OfflineSaleListener() {
                @Override
                public void onReceived(OfflineSaleResponse response) {
                    log("Offline sale requested");
                    validateOfflineSale(request, response, (isValid, error) -> {
                        if (isValid) {
                            log("Offline sale response validated.");
                            offlineSaleTransactionstoupload.remove(0);
                            sendOfflineSalesRequests();
                        } else {
                            log("Offline sale response validation failed.");
                            settlementCallbacks.onSettlementFailed("Invalid offline sale response.");
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    log("onError() Offline sale error: " + throwable.getMessage());
                    if (isAutoSettlement) {
                        if (settlementRetryCount > 1) {
                            log("Retry count = " + settlementRetryCount);
                            log("Retry offline sale request.");
                            sendOfflineSalesRequests();
                            settlementRetryCount -= 1;
                        } else {
                            settlementCallbacks.onSettlementFailed(Const.MSG_OFFLINE_SALE_ERROR);
                        }
                    } else {
                        settlementCallbacks.onSettlementFailed(Const.MSG_OFFLINE_SALE_ERROR);
                    }
                }

                @Override
                public void TLEError(String error) {
                    log("TLEError() Offline sale TLE Error");
                    settlementCallbacks.onSettlementFailed(Const.MSG_PLEASE_DOWNLOAD_TLE_KEY);
                }

                @Override
                public void onCompleted() {
                }
            });
        } else {
            log("Offline sale transactions are empty.");
            settlementRequestStarted = false;
            state = State.SETTLEMENT_REQUEST;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Check reversals and send request">
    private void checkReversals() {
        repository.getReversalsByHost(host.getHostID(), reversals -> {
            if (reversals.isEmpty()) {
                onNoReversalExists();
            } else {
                log("Reversals exists. Send reversal request.");
                sendReversal(reversals.get(0));
            }
        });
    }

    private void onNoReversalExists() {
        log("onNoReversalExists()");
        state = State.CHECK_OFFLINE_SALES;
    }

    private void sendReversal(Reversal reversal) {
        settlementCallbacks.onSettlementStateUpdate("Sending reversal request.");
        log("Get reversal issuer.");
        repository.getIssuerById(reversal.getIssuer_number(), issuer -> {
            log("Issuer received.");
            ReversalRequest pendingRReq = createReversalRequest(reversal);
            TLEData tleData = new TLEData();
            tleData.setChipStatus(Integer.parseInt(pendingRReq.getPosEntryMode()));
            tleData.setHostId(host.getHostID());
            tleData.setIssuerId(issuer.getIssuerNumber());
            tleData.setPan(pendingRReq.getPan());
            tleData.setTrack2(pendingRReq.getTrack2Data());
            tleData.setTleEnable(host.getTLEEnabled() == 1);

            log("Sending reversal request.");
            repository.reversalRequest(issuer, pendingRReq, tleData, new Repository.ReversalTransactionListener() {
                @Override
                public void onReceived(ReversalResponse reversalResponse) {
                    log("Reversal response.");
                    validateReversal(pendingRReq, reversalResponse, (isValid, error) -> {
                        if (isValid) {
                            log("Reversal validated. Delete reversal.");
                            repository.deleteReversal(reversal, () -> onNoReversalExists());
                        } else {
                            log("Reversal validation failed.");
                            settlementCallbacks.onSettlementFailed("Reversal response validation failed.");
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    log("Reversal failed.");
                    throwable.printStackTrace();
                    String msg = "Unable to process pending reversal request.";
                    if (isAutoSettlement) {
                        if (settlementRetryCount > 1) {
                            log("Retry count = " + settlementRetryCount);
                            log("Retry reversal request.");
                            sendReversal(reversal);
                            settlementRetryCount -= 1;
                        } else {
                            settlementCallbacks.onSettlementFailed(msg);
                        }
                    } else {
                        settlementCallbacks.onSettlementFailed(msg);
                    }
                }

                @Override
                public void onCompleted() {
                }

                @Override
                public void TLEError(String error) {
                    log("Reversal failed: TLEError = " + error);
                    settlementCallbacks.onSettlementFailed("Unable to process pending reversal request.");
                }
            });
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Detail report print">
    private int detailReportPrintIndex = 0;
    private List<PrintDataBuilder> detailReportPrintBuilders;

    public void generateDetailReport() {
        log("generateDetailReport()");
        settlementCallbacks.onSettlementStateUpdate("Generating detail report.");

        ArrayList<Transaction> detailReportTransactions = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (!isPreAuthTxn(t.getTransaction_code())) {
                detailReportTransactions.add(t);
            }
        }

        PosDevice.getInstance().startPrinting();
        MyApp.getInstance().getAppReceipts().generateDetailReportReceiptArvin(
                host, merchant, terminal, currency, allIssuers, detailReportTransactions,
                new AppReceipts.ReceiptBuilderListener() {
                    @Override
                    public void onReceiptGenerated(List<PrintDataBuilder> builders) {
                        log("Detail report generated: builders = " + builders.size());
                        detailReportPrintBuilders = builders;
                        detailReportPrintIndex = 0;
                        startPrintingDetailReport();
                    }

                    @Override
                    public void onReceiptGenerationFailed() {
                        log("Detail report generation failed.");
                        settlementCallbacks.onSettlementFailed("Detail report generation failed.");
                    }
                });
    }

    private void resumePrintDetailReport() {
        log("resumePrintDetailReport()");
        startPrintingDetailReport();
    }

    private void startPrintingDetailReport() {
        settlementCallbacks.onSettlementStateUpdate("Printing detail report.");
        log("Start printing detail report. builder index = " + detailReportPrintIndex);
        PrintDataBuilder p = detailReportPrintBuilders.get(detailReportPrintIndex);

        Print print = new Print();
        print.setPrintType(Print.PRINT_DATA_BUILDER);
        print.setPrintDataBuilder(p);
        print.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                if ((detailReportPrintIndex + 1) == detailReportPrintBuilders.size()) {
                    log("Detail report print completed.");
                    detailReportPrintIndex = 0;
                    detailReportPrintBuilders.clear();
                    state = State.CHECK_REVERSALS;
                } else {
                    detailReportPrintIndex += 1;
                    startPrintingDetailReport();
                }
            }

            @Override
            public void onPrintError(PrintError printError) {
                log("Print error: print builder index = " + detailReportPrintIndex);
                settlementCallbacks.onDetailReportPrintError(printError);
            }
        });

        PosDevice.getInstance().addToPrintQueue(print);
    }
    // </editor-fold>

    /**
     * Re print detail report
     */
    public void rePrintDetailReport() {
        state = State.DETAIL_REPORT_PRINT_RESUME;
    }

    /**
     * Re print settlement report
     */
    public void rePrintSettlementReport() {
        settlementReportPrintStarted = false;
        state = State.PRINT_SETTLEMENT_REPORT;
    }

    /**
     * Stop settlement thread
     */
    public void stopSettlementThread() {
        log("stopSettlementThread()");
        startSettlement = false;
    }

    /**
     * Set settlement callbacks
     *
     * @param settlementCallbacks
     */
    public void setSettlementCallbacks(SettlementCallbacks settlementCallbacks) {
        this.settlementCallbacks = settlementCallbacks;
    }

    /**
     * Set pre auth delete dialog action
     *
     * @param preAuthAction
     */
    public void setPreAuthAction(PreAuthAction preAuthAction) {
        this.preAuthAction = preAuthAction;
    }

    /**
     * SEt detail report print dialog action
     *
     * @param detailReportPrintAction
     */
    public void setDetailReportPrintAction(DetailReportPrintAction detailReportPrintAction) {
        this.detailReportPrintAction = detailReportPrintAction;
    }

    public interface SettlementCallbacks {
        void showPreAuthDeleteConfirmDialog();

        void showDetailReportPrintDialog();

        void onSettlementCompleted();

        void onSettlementStateUpdate(String msg);

        void onSettlementFailed(String errorMsg);

        void onDetailReportPrintError(PrintError printError);

        void onSettlementReportPrintError(PrintError printError);
    }

    public interface InitListener {
        void onNoTransactions();

        void onTransactionsExists(String currency, long txnAmount);
    }

    // <editor-fold defaultstate="collapsed" desc="Common Functions">
    private Issuer getHostContainsIssuer() {
        String[] issuerIds = host.getIssuerList().split(",");
        int issuerId = Integer.parseInt(issuerIds[0]);
        for (Issuer i : allIssuers) {
            if (i.getIssuerNumber() == issuerId) {
                return i;
            }
        }
        return null;
    }

    private long calcTotalAmount() {
        log("calcTotalAmount()");
        long totalAmount = 0;
        for (Transaction transaction : transactionBatch) {
            if ((transaction.getTransaction_code()== SALE_REFUND) || (transaction.getTransaction_code()== SALE_REFUND_MANUAL) ){
                totalAmount -= Long.parseLong(transaction.getTotal_amount());
            }
            else{
                totalAmount += Long.parseLong(transaction.getTotal_amount());
            }
        }
        log("Total amount: " + totalAmount);
        return totalAmount;
    }

    private List<Integer> getPreAuthTxnTypes() {
        List<Integer> txnCodeList = new ArrayList<>();
        txnCodeList.add(SALE_PRE_AUTHORIZATION);
        txnCodeList.add(SALE_PRE_AUTHORIZATION_MANUAL);
        return txnCodeList;
    }

    private boolean isOfflineTxn(int txnCode) {
        return (txnCode == SALE_OFFLINE
                || txnCode == SALE_OFFLINE_MANUAL
                || txnCode == SALE_PRE_COMPLETION);
    }

    private boolean isSaleTxn(int txnCode) {
        return (txnCode == SALE
                || txnCode == SALE_MANUAL
                || txnCode == CASH_BACK
                || txnCode == SALE_REFUND
                || txnCode == SALE_REFUND_MANUAL
                || txnCode == QUASI_CASH
                || txnCode == QUASI_CASH_MANUAL
                || txnCode == CASH_ADVANCE
                || txnCode == SALE_INSTALLMENT);
    }

    private List<Integer> getDeleteTxnTypes() {
        List<Integer> txnCodeList = new ArrayList<>();
        txnCodeList.add(SALE);
        txnCodeList.add(SALE_MANUAL);
        txnCodeList.add(SALE_OFFLINE);
        txnCodeList.add(SALE_OFFLINE_MANUAL);
        txnCodeList.add(CASH_BACK);
        txnCodeList.add(SALE_REFUND);
        txnCodeList.add(SALE_REFUND_MANUAL);
        txnCodeList.add(CASH_ADVANCE);
        txnCodeList.add(QUASI_CASH);
        txnCodeList.add(QUASI_CASH_MANUAL);
        txnCodeList.add(SALE_INSTALLMENT);
        txnCodeList.add(SALE_PRE_COMPLETION);
        txnCodeList.add(QR_SALE);
        return txnCodeList;
    }

    private boolean isPreAuthTxn(int txnCode) {
        return (txnCode == SALE_PRE_AUTHORIZATION
                || txnCode == SALE_PRE_AUTHORIZATION_MANUAL);
    }


    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
    // </editor-fold>


}
