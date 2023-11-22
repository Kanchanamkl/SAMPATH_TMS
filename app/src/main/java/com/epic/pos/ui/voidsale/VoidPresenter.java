package com.epic.pos.ui.voidsale;

import static com.epic.pos.common.TranTypes.CASH_ADVANCE;
import static com.epic.pos.common.TranTypes.CASH_BACK;
import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION;
import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION_MANUAL;

import android.text.TextUtils;

import com.epic.pos.common.Const;
import com.epic.pos.common.ErrorMsg;
import com.epic.pos.common.TranTypes;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.device.PosDevice;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.iso.modal.Transaction;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.VoidRequest;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.iso.modal.response.SaleResponse;
import com.epic.pos.iso.modal.response.VoidResponse;
import com.epic.pos.tle.TLEData;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.Utility;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public class VoidPresenter extends BasePresenter<VoidContract.View> implements VoidContract.Presenter {

    private final String TAG = VoidPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private enum State {
        SEARCH_INVOICE, SUBMIT_VOID_REQUEST
    }

    private Host host;
    private Merchant merchant;

    private String traceNo;
    private String invoiceNo;
    private State state = State.SEARCH_INVOICE;
    private com.epic.pos.data.db.dbtxn.modal.Transaction transaction;
    private Issuer issuer;
    private VoidRequest request;
    private VoidResponse response;
    private String errorMsg = "";

    private Reversal reversal;

    //pending reversal
    private Reversal pendingReversal;
    private ReversalRequest pendingRReq;
    private ReversalResponse pendingRRes;

    @Inject
    public VoidPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void resetData() {
        state = State.SEARCH_INVOICE;
        invoiceNo = "";
        transaction = null;
        issuer = null;
        merchant = null;
        repository.getHostByHostId(repository.getSelectedHostIdForVoid(), host -> {
            VoidPresenter.this.host = host;
            repository.getMerchantById(repository.getSelectedMerchantIdForVoid(), merchant -> {
                VoidPresenter.this.merchant = merchant;
                if (isViewNotNull()) {
                    mView.onDateReceived(host.getHostName(), merchant.getMerchantName());
                }
            });
        });
    }

    @Override
    public void clearVoid() {
        resetData();
        if (isViewNotNull()) {
            mView.onClearVoidUI();
        }
    }


    @Override
    public void onSubmit() {
        if (state == State.SEARCH_INVOICE) {
            if (isViewNotNull()) {
                mView.setConfirmEnabled(false);
            }

            repository.getTransaction(host.getHostID(), merchant.getMerchantNumber(), invoiceNo, transaction -> {
                if (transaction != null) {
                    if (transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION
                            || transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION_MANUAL) {
                        //pre auth
                        if (isViewNotNull()) {
                            mView.showInAppError(Const.MSG_PRE_AUTH);
                        }
                    } else if (transaction.getTransaction_code() == CASH_BACK) {
                        //cash back
                        if (isViewNotNull()) {
                            mView.showInAppError(Const.MSG_CASH_BACK_VOID_ERROR);
                        }
                    } else if (transaction.getTransaction_code() == CASH_ADVANCE) {
                        //cash advance
                        if (isViewNotNull()) {
                            mView.showInAppError(Const.MSG_CASH_ADVANCE_ERROR);
                        }
                    } else {
                        VoidPresenter.this.transaction = transaction;
                        repository.getIssuerById(transaction.getIssuer_number(), issuer -> {
                            VoidPresenter.this.issuer = issuer;
                            repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                                VoidPresenter.this.host = host;
                                if (host.getMustSettleFlag() == 0) {
                                    repository.getMerchantById(transaction.getMerchant_no(), merchant -> {
                                        String maskingFormat = issuer.getMaskDisplay();

                                        if (transaction.getPan().length() != 16) {
                                            maskingFormat = Utility.getMaskingFormat(transaction.getPan());
                                        }

                                        VoidPresenter.this.merchant = merchant;
                                        state = State.SUBMIT_VOID_REQUEST;
                                        if (isViewNotNull()) {
                                            mView.onInvoiceDataReceived(transaction, issuer, merchant, Utility.maskCardNumber(transaction.getPan(), maskingFormat));
                                            mView.setConfirmEnabled(true);
                                        }
                                    });
                                } else {
                                    //pre auth txn
                                    if (isViewNotNull()) {
                                        mView.onErrorAndClear(Const.MSG_SETTLEMENT_PENDING, Const.MSG_MUST_SETTLE);
                                    }
                                }
                            });
                        });
                    }
                } else {
                    if (isViewNotNull()) {
                        mView.invalidInvoiceNumber();
                    }
                }
            });
        } else if (state == State.SUBMIT_VOID_REQUEST) {
            if (!transaction.isOfflineTransaction()
                    && transaction.getTransaction_code() != TranTypes.SALE_PRE_COMPLETION) {
                if (!networkConnection.checkNetworkConnection()) {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                    }
                    return;
                }
            }

            PosDevice.getInstance().clearPrintQueue();
            PosDevice.getInstance().startPrinting();

            if (transaction.isOfflineTransaction() || transaction.isPreCompTransaction()) {
                //proceed offline and pre comp transactions without checking reversal
                processVoidTransaction();
            } else {
                //check reversals
                repository.getReversalsByHost(host.getHostID(), reversals -> {
                    if (reversals.size() == 0) {
                        //no reversal exists
                        processVoidTransaction();
                    } else {
                        //reversal exists
                        pendingReversal = reversals.get(0);
                        pendingRReq = createReversalRequest(pendingReversal);
                        sendPendingReversalRequest();
                    }
                });
            }
        }
    }

    private void processVoidTransaction() {
        if (transaction.isOfflineTransaction() || transaction.isPreCompTransaction()) {
            sendVoidRequest();
        } else {
            reversal = createReversalObject();
            repository.insertReversal(reversal, id -> {
                reversal.setId((int) id);
                sendVoidRequest();
            });
        }
    }

    private Reversal createReversalObject() {
        //Increment trace number
        traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));
        merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
        repository.updateMerchant(merchant, null);

        Reversal r = new Reversal();
        r.setInvoice_no(transaction.getInvoice_no());
        r.setTrace_no(traceNo);
        r.setTxn_date(transaction.getTxn_date());
        r.setTxn_time(transaction.getTxn_time());
        r.setHost(transaction.getHost());
        r.setMerchant_no(transaction.getMerchant_no());
        r.setMti(transaction.getMti());

        if (isRefundSale(transaction)) {
            if (isAmexHost(transaction)) {
                r.setProcessing_code(Transaction.VOID_REFUND_PROCESSING_CODE_AMEX);
            } else {
                r.setProcessing_code(Transaction.VOID_REFUND_PROCESSING_CODE);
            }
        } else {
            if (isAmexHost(transaction)) {
                r.setProcessing_code(Transaction.VOID_PROCESSING_CODE_AMEX);
            } else {
                r.setProcessing_code(Transaction.VOID_PROCESSING_CODE);
            }
        }

        r.setTransaction_code(transaction.getTransaction_code());
        r.setChip_status(transaction.getChip_status());
        r.setBase_transaction_amount(transaction.getBase_transaction_amount());
        r.setTotal_amount(transaction.getTotal_amount());
        r.setCash_back_amount(transaction.getCash_back_amount());
        r.setPan(transaction.getPan());
        r.setCard_serial_number(transaction.getCard_serial_number());
        r.setTrack2(transaction.getTrack2());
        r.setSvc_code(transaction.getSvc_code());
        r.setExp_date(transaction.getExp_date());
        r.setTerminal_id(transaction.getTerminal_id());
        r.setTerminal_no(transaction.getTerminal_no());
        r.setMerchant_id(transaction.getMerchant_id());
        r.setMerchant_name(merchant.getMerchantName());
        r.setNii(transaction.getNii());
        r.setSecure_nii(transaction.getSecure_nii());
        r.setTpdu(transaction.getTpdu());
        r.setEmv_field_55(transaction.getEmv_field_55());
        r.setResponse_code(transaction.getResponse_code());
        r.setCdt_index(transaction.getCdt_index());
        r.setIssuer_number(transaction.getIssuer_number());
        return r;
    }

    private boolean isRefundSale(com.epic.pos.data.db.dbtxn.modal.Transaction transaction) {
        return transaction.getTransaction_code() == TranTypes.SALE_REFUND
                || transaction.getTransaction_code() == TranTypes.SALE_REFUND_MANUAL;
    }

    private boolean isAmexHost(com.epic.pos.data.db.dbtxn.modal.Transaction transaction) {
        return transaction.getHost() == 2;
    }

    private void sendVoidRequest() {
        if (isViewNotNull()) {
            mView.setConfirmEnabled(false);
        }

        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_VOID_REQUEST, Const.MSG_PLEASE_WAIT);
        }

        if (transaction.isOfflineTransaction()
                || transaction.getTransaction_code() == TranTypes.SALE_PRE_COMPLETION) {
            transaction.setVoided(1);
            repository.updateTransaction(transaction, () -> {
                repository.saveCurrentVoidSaleId(transaction.getId());
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.gotoVoidReceiptActivity();
                }
            });
        } else {
            request = new VoidRequest();
            request.setNii(transaction.getNii());
            request.setSecureNii(transaction.getSecure_nii());
            request.setTpdu(transaction.getTpdu());
            request.setAmount(String.valueOf(transaction.getTotal_amount()));

            if (transaction.getTransaction_code() == TranTypes.CASH_BACK) {
                request.setCashBackAmount(String.valueOf(transaction.getCash_back_amount()));
            }


          //  merchant.setSTAN(String.valueOf(Integer.parseInt(merchant.getSTAN()) + 1));
            String traceNo = AppUtil.toTraceNumber(Integer.parseInt(merchant.getSTAN()));

            repository.updateMerchant(merchant, null);

          // request.setTraceNumber(reversal.getTrace_no());
            request.setTraceNumber(traceNo);
            request.setTxnTime(transaction.getTxn_time());
            request.setTxnDate(transaction.getTxn_date());
            request.setExpDate(transaction.getExp_date());
            request.setPosEntryMode(String.valueOf(transaction.getChip_status()));
            request.setRrn(transaction.getRrn());
            request.setApprovalCode(transaction.getApprove_code());
            request.setTid(transaction.getTerminal_id());
            request.setMid(transaction.getMerchant_id());
            request.setInvoiceNumber(transaction.getInvoice_no());
            request.setPan(transaction.getPan());
            request.setEmvData(transaction.getEmv_field_55());

            if (!TextUtils.isEmpty(transaction.getStd_ref_no())) {
                request.setStudentRefNo(transaction.getStd_ref_no());
            }

            if (isRefundSale(transaction)) {
                if (isAmexHost(transaction)) {
                    request.setProcessingCode(Transaction.VOID_REFUND_PROCESSING_CODE_AMEX);
                } else {
                    request.setProcessingCode(Transaction.VOID_REFUND_PROCESSING_CODE);
                }
            } else {
                if (isAmexHost(transaction)) {
                    request.setProcessingCode(Transaction.VOID_PROCESSING_CODE_AMEX);
                } else {
                    request.setProcessingCode(Transaction.VOID_PROCESSING_CODE);
                }
            }

            request.setSecureNii(transaction.getSecure_nii());

            repository.voidRequest(issuer, request, getTleData(), new Repository.VoidRequestListener() {
                @Override
                public void onReceived(VoidResponse voidResponse) {
                    VoidPresenter.this.response = voidResponse;
                    if (validateRes()) {
                        repository.deleteReversal(reversal, () -> {
                            transaction.setVoided(1);
                            transaction.setTrace_no(request.getTraceNumber());
                            repository.updateTransaction(transaction, () -> {
                                repository.saveCurrentVoidSaleId(transaction.getId());
                                if (isViewNotNull()) {
                                    mView.hideLoader();
                                    mView.gotoVoidReceiptActivity();
                                }
                            });
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.hideLoader();
                            mView.setConfirmEnabled(true);
                            mView.onTxnFailed(errorMsg);
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (isViewNotNull()) {
                        mView.hideLoader();
                        mView.setConfirmEnabled(true);
                        mView.onTxnFailed(Const.MSG_TXN_REQUEST_ERROR);
                    }
                }

                @Override
                public void TLEError(String error) {
                    mView.hideLoader();
                    mView.onTxnFailed(Const.MSG_PLEASE_DOWNLOAD_TLE_KEY);
                }

                @Override
                public void onCompleted() {

                }
            });
        }
    }

    private TLEData getTleData() {
        TLEData tleData = new TLEData();
        tleData.setChipStatus(transaction.getChip_status());
        tleData.setHostId(host.getHostID());
        tleData.setIssuerId(issuer.getIssuerNumber());
        tleData.setPan(transaction.getPan());
        tleData.setTrack2(transaction.getTrack2());
        tleData.setTleEnable(host.getTLEEnabled() == 1);

        return tleData;
    }


    private boolean validateRes() {
        if (response.getMti().equals(Transaction.SALE_RES_MTI)) {
            if (response.getProcessingCode().equals(request.getProcessingCode())) {
                if (response.getTraceNumber().equals(request.getTraceNumber())) {
                    if (response.getTid().equals(request.getTid())) {
                        if (response.getResponseCode().equals(SaleResponse.RES_CODE_SUCCESS)) {
                            return true;
                        } else {
                            errorMsg = ErrorMsg.getErrorMsg("Void", response.getResponseCode());
                        }
                    } else {
                        errorMsg = "Terminal ID mismatch";
                    }
                } else {
                    errorMsg = "Trace number mismatch";
                }
            } else {
                errorMsg = "Processing code mismatch";
            }
        } else {
            errorMsg = "MTI mismatch";
        }

        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Pending Reversal Functions">
    private void sendPendingReversalRequest() {
        TLEData tleData = new TLEData();
        tleData.setChipStatus(Integer.parseInt(pendingRReq.getPosEntryMode()));
        tleData.setHostId(host.getHostID());
        tleData.setIssuerId(issuer.getIssuerNumber());
        tleData.setPan(pendingRReq.getPan());
        tleData.setTrack2(pendingRReq.getTrack2Data());
        tleData.setTleEnable(host.getTLEEnabled() == 1);

        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_REVERSAL_REQUEST, Const.MSG_PLEASE_WAIT);
        }

        repository.reversalRequest(issuer, pendingRReq, tleData, new Repository.ReversalTransactionListener() {
            @Override
            public void onReceived(ReversalResponse reversalResponse) {
                AppLog.i(TAG, "REVERSAL RES: " + reversalResponse.toString());
                if (isViewNotNull()) {
                    mView.hideLoader();
                }

                VoidPresenter.this.pendingRRes = reversalResponse;
                validateReversal(pendingRReq, pendingRRes, (isValid, error) -> {
                    if (isValid) {
                        repository.deleteReversal(pendingReversal, () -> {
                            processVoidTransaction();
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.reversalValidationFailed(error);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.gotoReversalFailedActivity();
                }
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void TLEError(String error) {
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.reversalValidationFailed(Const.MSG_PLEASE_DOWNLOAD_TLE_KEY);
                }
            }
        });
    }
    // </editor-fold>


    @Override
    public void setInvoiceNumber(String invoice) {
        VoidPresenter.this.invoiceNo = invoice;
        if (isViewNotNull()) {
            mView.setConfirmEnabled(invoice.length() == Const.INVOICE_NO_MAX_LEN);
        }
    }

    private boolean isViewNotNull() {
        return mView != null;
    }
}