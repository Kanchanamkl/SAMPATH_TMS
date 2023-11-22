package com.epic.pos.ui;

import static com.epic.pos.iso.modal.Transaction.BATCH_UPLOAD_RES_MTI;
import static com.epic.pos.iso.modal.Transaction.OFFLINE_SALE_RES_MTI;
import static com.epic.pos.iso.modal.Transaction.SETTLEMENT_RES_MTI;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.epic.pos.BuildConfig;
import com.epic.pos.common.Const;
import com.epic.pos.common.ErrorMsg;
import com.epic.pos.common.TranTypes;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.data.db.dccdb.model.DCCData;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.iso.modal.Transaction;
import com.epic.pos.iso.modal.request.BatchUploadRequest;
import com.epic.pos.iso.modal.request.OfflineSaleRequest;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.SettlementRequest;
import com.epic.pos.iso.modal.response.BatchUploadResponse;
import com.epic.pos.iso.modal.response.DCCDownloadResponse;
import com.epic.pos.iso.modal.response.OfflineSaleResponse;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.iso.modal.response.SettlementResponse;
import com.epic.pos.util.BitMapUtil;
import com.epic.pos.util.Utility;
import com.epic.pos.util.ValidatorUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class BasePresenter<T extends BaseView> {

    private final String TAG = BasePresenter.class.getSimpleName();
    public T mView;

    public void attachView(T view) {
        this.mView = view;
    }

    public void detachView() {
        if (mView != null) {
            mView = null;
        }
    }

    @Inject
    protected Repository repository;

    public void hasPendingProfileUpdate(){

    }
    public void downloaddccdata(){
        mView.showLoader("DCC","Please Wait Downloading DCC Data");

        repository.getIssuerById(1, issuer -> {
        repository.getEnabledMerchantsByHost(1, merchants -> {
            repository.getTerminalById(1, terminal -> {
            repository.DCCDataDownloadRequest(issuer,merchants.get(0),terminal,new Repository.DCCDownloadRequestListener() {
                @Override
                public void onReceived(DCCDownloadResponse dccDownloadResponse) {

                    mView.hideLoader();
                    Log.d("DCC","DATA Downloaded");
                    Log.d("DCC","DATA : " +dccDownloadResponse.getDccdata());
                    String dccresponce =dccDownloadResponse.getDccdata();// "CU1449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   000000000499202021449800001400512589BOC_DCC   00000000049920202";
                    dccresponce =dccresponce.substring(2);
                    Log.d("DCC","RCU "+dccresponce);

                    String [] dccdatas = dccresponce.split("  ");

                    Log.d("DCC","Len "+dccdatas.length);

                    int x= 0;
                    for (int i=1;dccdatas.length>i;i++) {
                        Log.d("DCC", "0 value " + dccdatas[i]);
                      //  String selecteddata = dccresponce;//dccdatas[i];
                        String basecurrancy = dccresponce.substring(x, x+3);
                        String currancy = dccresponce.substring(x+3, x+6);
                        String rate = dccresponce.substring(x+6, x+18);
                        Log.d("DCC", "basecurrancy " + basecurrancy);
                        Log.d("DCC", "currancy " + currancy);
                        Log.d("DCC", "rate " + rate);

                        DCCData dccdata = new DCCData();
                        dccdata.setCCode(Integer.parseInt(currancy));
                        dccdata.setBCCode(Integer.parseInt(basecurrancy));


                        repository.getcursymbycode(Integer.parseInt(currancy),new DbHandler.getcursymbycode() {

                                    @Override
                                    public void onReceived(Cursor cursor) {
                                        cursor.moveToFirst();
                                        @SuppressLint("Range") String  cSymbol= cursor.getString(cursor.getColumnIndex("Sym"));
                                        dccdata.setCSymbol(cSymbol);
                                        dccdata.setConversionRate(rate);

                                        repository.savedccdata(dccdata, new DbHandler.InsertListener() {
                                            @Override
                                            public void onSuccess(long id) {
                                                Log.d("DCC", "Data Saved to Database");
                                            }
                                        });
                                    }
                                });


                        x= i*46;
                    }
                }

                @Override
                public void onError(String error) {
                    mView.hideLoader();
                }

                @Override
                public void onError(Throwable throwable) {
                    mView.hideLoader();
                }

                @Override
                public void onCompleted() {
                    mView.hideLoader();
                }
            });
            });
        });
        });

    }

    public void insertdccbinlist( List<DCCBINNLIST> values){
        //  mView.showLoader("DCC","Please Wait Updating DCC BIN List");
        repository.insertdccbinlist(new DbHandler.UPDATEDCCBINListener() {

            @Override
            public void OnUpdated() {
                // mView.hideLoader();
            }

            @Override
            public void OnError(String message) {
                // mView.hideLoader();
            }
        },values);
    }

    protected boolean isNexgo(){
        return BuildConfig.FLAVOR.equals("nexgodev") || BuildConfig.FLAVOR.equals("nexgolive");
    }

    public void hasPendingSettlement() {
        repository.saveHasPendingAutoSettlement(true);
    }

    protected void incrementAutoSettlementDate(IncrementAutoSettlementListener listener) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, 1);

            String nextSettlementDate = new SimpleDateFormat(Const.AUTO_SETTLE_DATE_FORMAT).format(c.getTime());
            repository.updateAutoSettleDate(nextSettlementDate, () -> listener.onCompleted(nextSettlementDate));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected interface IncrementAutoSettlementListener {
        void onCompleted(String nextSettlementDate);
    }

    /**
     * Get card label with emv and card definition
     *
     * @param cardAction
     * @param cardDefinition
     * @return
     */
    protected String getCardLabel(CardAction cardAction, CardDefinition cardDefinition) {
        String cardLabel = "";

        if (cardAction == CardAction.INSERT || cardAction == CardAction.TAP) {
            cardLabel = PosDevice.getInstance().getTagDataToDisplay("9F12");

            if (TextUtils.isEmpty(cardLabel)) {
                cardLabel = PosDevice.getInstance().getTagDataToDisplay("50");
            }
        }

        if (TextUtils.isEmpty(cardLabel)) {
            cardLabel = cardDefinition.getCardLabel();
        }

        return cardLabel;
    }

    /**
     * Get card label with emv and transaction object
     *
     * @param cardAction
     * @param txn
     * @return
     */
    protected String getCardLabel(CardAction cardAction, com.epic.pos.data.db.dbtxn.modal.Transaction txn) {
        String cardLabel = "";

        if (cardAction == CardAction.INSERT || cardAction == CardAction.TAP) {
            cardLabel = PosDevice.getInstance().getTagDataToDisplay("9F12");

            if (TextUtils.isEmpty(cardLabel)) {
                cardLabel = PosDevice.getInstance().getTagDataToDisplay("9F50");
            }
        }

        if (TextUtils.isEmpty(cardLabel)) {
            cardLabel = txn.getCard_label();
        }

        return cardLabel;
    }

    protected void validateBatchUploadResponse(BatchUploadRequest request, BatchUploadResponse response, ValidateBatchUploadListener listener) {
        String error = "";
        boolean isValid = false;
        if (response.getMti().equals(BATCH_UPLOAD_RES_MTI)) {
            if (request.getProcessingCode().equals(response.getProcessingCode())) {
                if (request.getTraceNumber().equals(response.getTraceNumber())) {
                    if (request.getTid().equals(response.getTid())) {
                        if (response.getResponseCode() != null && !response.getResponseCode().isEmpty()) {
                            if (response.getResponseCode().equals(BatchUploadResponse.RES_CODE_SUCCESS)) {
                                isValid = true;
                            } else {
                                error = ErrorMsg.getErrorMsg("Batch Upload", response.getResponseCode());
                            }
                        } else {
                            error = "Invalid settlement response";
                        }
                    } else {
                        error = "Terminal ID mismatch";
                    }
                } else {
                    error = "Trace number mismatch";
                }
            } else {
                error = "Processing code mismatch";
            }
        } else {
            error = "MTI mismatch";
        }

        listener.onResult(isValid, error);
    }

    protected interface ValidateBatchUploadListener {
        void onResult(boolean isValid, String error);
    }

    protected void validateSettlementResponse(SettlementRequest request, SettlementResponse response, ValidateSettlementListener listener) {
        String error = "";
        boolean isValid = false;
        if (response.getMti().equals(SETTLEMENT_RES_MTI)) {
            if (request.getProcessingCode().equals(response.getProcessingCode())) {
                if (request.getTraceNumber().equals(response.getTraceNumber())) {
                    if (request.getTid().equals(response.getTid())) {
                        if (response.getResponseCode() != null && !response.getResponseCode().isEmpty()) {
                            isValid = true;
                        } else {
                            error = "Invalid settlement response";
                        }
                    } else {
                        error = "Terminal ID mismatch";
                    }
                } else {
                    error = "Trace number mismatch";
                }
            } else {
                error = "Processing code mismatch";
            }
        } else {
            error = "MTI mismatch";
        }

        listener.onResult(isValid, error);
    }

    protected interface ValidateSettlementListener {
        void onResult(boolean isValid, String error);
    }

    protected void validateOfflineSale(OfflineSaleRequest request, OfflineSaleResponse response, ValidateOfflineSaleListener listener) {
        String error = "";
        boolean isValid = false;

        if (response.getMti().equals(OFFLINE_SALE_RES_MTI)) {
            if (request.getProcessingCode().equals(response.getProcessingCode())) {
                if (request.getTraceNumber().equals(response.getTraceNumber())) {
                    if (request.getTid().equals(response.getTid())) {
                        if (response.getResponseCode() != null
                                && !response.getResponseCode().isEmpty()) {
                            isValid = true;
                        } else {
                            error = "Invalid offline sale response";
                        }
                    } else {
                        error = "Terminal ID mismatch";
                    }
                } else {
                    error = "Trace number mismatch";
                }
            } else {
                error = "Processing code mismatch";
            }
        } else {
            error = "MTI mismatch";
        }

        listener.onResult(isValid, error);
    }

    protected interface ValidateOfflineSaleListener {
        void onResult(boolean isValid, String error);
    }


    protected void validateReversal(ReversalRequest reversalRequest, ReversalResponse reversalResponse, ValidateReversalListener listener) {
        String error = "";
        boolean isValid = false;

        if (reversalResponse.getMti().equals(com.epic.pos.iso.modal.Transaction.REVERSAL_RES_MTI)) {
            if (reversalResponse.getProcessingCode().equals(reversalRequest.getProcessingCode())) {
                if (reversalResponse.getTraceNumber().equals(reversalRequest.getTraceNumber())) {
                    if (reversalResponse.getTid().equals(reversalRequest.getTid())) {
                        if (reversalResponse.getResponseCode().equals(ReversalResponse.RES_CODE_SUCCESS)) {
                            isValid = true;
                        } else {
                            error = ErrorMsg.getErrorMsg("Reversal", reversalResponse.getResponseCode());
                        }
                    } else {
                        error = "Terminal ID mismatch";
                    }
                } else {
                    error = "Trace number mismatch";
                }
            } else {
                error = "Processing code mismatch";
            }
        } else {
            error = "MTI mismatch";
        }

        listener.onResult(isValid, error);
    }

    protected interface ValidateReversalListener {
        void onResult(boolean isValid, String error);
    }

    /**
     * Create BatchUploadRequest object by transaction.
     *
     * @param txn
     * @return
     */
    protected BatchUploadRequest createBatchUploadRequest(String traceNo, com.epic.pos.data.db.dbtxn.modal.Transaction txn) {
        BatchUploadRequest batchUploadRequest = new BatchUploadRequest();
        batchUploadRequest.setPan(txn.getPan());
        // Processing code incremented by 1 (6 digits zero pad)
        batchUploadRequest.setProcessingCode(
                ValidatorUtil.getInstance().zeroPadString(
                        String.valueOf(Integer.parseInt(txn.getProcessing_code()) + 1), 6));
        batchUploadRequest.setAmount(String.valueOf(txn.getBase_transaction_amount()));
        //batchUploadRequest.setTraceNumber(txn.getTrace_no());
        batchUploadRequest.setTraceNumber(traceNo);
        batchUploadRequest.setTime(txn.getTxn_time());
        batchUploadRequest.setDate(txn.getTxn_date());
        batchUploadRequest.setExpDate(txn.getExp_date());
        batchUploadRequest.setPosEntryMode(String.valueOf(txn.getChip_status()));
        //batchUploadRequest.setPanSeq(txn.getPan()); // With TLE
        batchUploadRequest.setNii(txn.getNii());
        batchUploadRequest.setSecureNii(txn.getSecure_nii());
        batchUploadRequest.setTpdu(txn.getTpdu());
        batchUploadRequest.setPosConditionCode("00"); // Temporary hardcoded
        batchUploadRequest.setRrn(txn.getRrn());
        batchUploadRequest.setAuthCode(txn.getApprove_code());
        batchUploadRequest.setResponse(txn.getResponse_code());
        batchUploadRequest.setTid(txn.getTerminal_id());
        batchUploadRequest.setMid(txn.getMerchant_id());
        batchUploadRequest.setEmv(txn.getEmv_field_55());
        batchUploadRequest.setOriginalTxnData(txn.getMti() + txn.getTrace_no() + txn.getRrn()); // OriginalMTI+Trace No +RRN
        batchUploadRequest.setInvoice(txn.getInvoice_no());
        return batchUploadRequest;
    }

    /**
     * Create OfflineSaleRequest from transaction object.
     *
     * @param txn
     * @return
     */
    protected OfflineSaleRequest createOfflineSaleRequest(com.epic.pos.data.db.dbtxn.modal.Transaction txn) {
        OfflineSaleRequest offlineSaleReq = new OfflineSaleRequest();
        offlineSaleReq.setPan(txn.getPan());
        offlineSaleReq.setProcessingCode(txn.getProcessing_code());
        offlineSaleReq.setAmount(String.valueOf(txn.getBase_transaction_amount()));
        offlineSaleReq.setTraceNumber(txn.getTrace_no());
        offlineSaleReq.setTime(txn.getTxn_time());
        offlineSaleReq.setDate(txn.getTxn_date());
        offlineSaleReq.setExpDate(txn.getExp_date());
        offlineSaleReq.setNii(txn.getNii());
        offlineSaleReq.setSecureNii(txn.getSecure_nii());
        offlineSaleReq.setTpdu(txn.getTpdu());
        offlineSaleReq.setPosEntryMode(String.valueOf(txn.getChip_status()));
        offlineSaleReq.setPosConditionCode("00"); // Temporary hardcoded
        offlineSaleReq.setAuthCode(txn.getApprove_code());
        offlineSaleReq.setTid(txn.getTerminal_id());
        offlineSaleReq.setMid(txn.getMerchant_id());
        offlineSaleReq.setEmv(txn.getEmv_field_55());
        offlineSaleReq.setInvoice(txn.getInvoice_no());
        return offlineSaleReq;
    }

    /**
     * Create reversal request object by reversal db object
     *
     * @param pendingReversal
     * @return
     */
    protected ReversalRequest createReversalRequest(Reversal pendingReversal) {
        ReversalRequest pendingRReq = new ReversalRequest();
        pendingRReq.setTpdu(pendingReversal.getTpdu());
        pendingRReq.setProcessingCode(pendingReversal.getProcessing_code());
        pendingRReq.setNii(pendingReversal.getNii());
        pendingRReq.setSecureNii(pendingReversal.getSecure_nii());
        pendingRReq.setTraceNumber(pendingReversal.getTrace_no());
        pendingRReq.setInvoiceNumber(pendingReversal.getInvoice_no());
        pendingRReq.setMid(pendingReversal.getMerchant_id());
        pendingRReq.setTid(pendingReversal.getTerminal_id());
        pendingRReq.setTrack2Data(pendingReversal.getTrack2());
        pendingRReq.setExpDate(pendingReversal.getExp_date());
        pendingRReq.setBaseAmount(String.valueOf(pendingReversal.getBase_transaction_amount()));
        pendingRReq.setTotalAmount(String.valueOf(pendingReversal.getTotal_amount()));


        if (!TextUtils.isEmpty(pendingReversal.getStd_ref_no())){
            pendingRReq.setStudentRefNo(pendingReversal.getStd_ref_no());
        }

        if (!TextUtils.isEmpty(pendingReversal.getCash_back_amount())) {
            pendingRReq.setCashBackAmount(String.valueOf(pendingReversal.getCash_back_amount()));
        }

        pendingRReq.setEmvData(pendingReversal.getEmv_field_55());
        pendingRReq.setPosEntryMode(String.valueOf(pendingReversal.getChip_status()));
        pendingRReq.setManualSale(pendingReversal.getTransaction_code() == TranTypes.SALE_MANUAL
                || pendingReversal.getTransaction_code() == TranTypes.SALE_PRE_AUTHORIZATION_MANUAL);
        pendingRReq.setPan(pendingReversal.getPan());

        if (!TextUtils.isEmpty(pendingReversal.getCash_back_amount())) {
            pendingRReq.setCashBackAmount(String.valueOf(pendingReversal.getCash_back_amount()));
        }

        if (!TextUtils.isEmpty(pendingReversal.getEmv_field_55())) {
            pendingRReq.setPanSequenceNumber(Utility.getPanSequenceNumber(pendingReversal.getEmv_field_55()));
        }

        if (pendingReversal.getProcessing_code().equals(Transaction.VOID_PROCESSING_CODE)) {
            pendingRReq.setVoidSale(true);
            pendingRReq.setTxnDate(pendingReversal.getTxn_date());
            pendingRReq.setTxnTime(pendingReversal.getTxn_time());
            pendingRReq.setRrn(pendingReversal.getRrn());
        }

        return pendingRReq;
    }

    protected int getFeatureBitmapIndex(Repository repository) {
        if (repository.isOfflineSale()) {
            return BitMapUtil.OFFLINE_SALE;
        } else if (repository.isManualSale()) {
            return BitMapUtil.MANUAL_SALE;
        } else if (repository.isOfflineManualSale()) {
            return BitMapUtil.OFFLINE_MANUAL_SALE;
        } else if (repository.isPreAuthSale()) {
            return BitMapUtil.PRE_AUTH;
        } else if (repository.isRefundSale()) {
            return BitMapUtil.REFUND;
        } else if (repository.isPreAuthManualSale()) {
            return BitMapUtil.PRE_AUTH_MANUAL;
        } else if (repository.isRefundManualSale()) {
            return BitMapUtil.REFUND_MANUAL;
        } else if (repository.isCashAdvance()) {
            return BitMapUtil.CASH_ADVANCE;
        } else if (repository.isInstallmentSale()) {
            return BitMapUtil.INSTALLMENT;
        } else if (repository.isCashBackSale()) {
            return BitMapUtil.CASH_BACK;
        } else if (repository.isQuasiCashFlow()) {
            return BitMapUtil.QUASI_CASH;
        } else if (repository.isQuasiCashManualFlow()) {
            return BitMapUtil.QUASI_CASH_MANUAL;
        } else if (repository.isStudentRefSale()){
            return BitMapUtil.STD_REF_SALE;
        }
        else {
            return BitMapUtil.SALE;
        }
    }

    protected String getSaleTitle(Repository repository) {
        if (repository.isManualSale()) {
            return "Manual Sale";
        } else if (repository.isOfflineSale()) {
            return "Offline Sale";
        } else if (repository.isOfflineManualSale()) {
            return "Offline Manual Sale";
        } else if (repository.isQrSale()) {
            return "QR Sale";
        } else if (repository.isVoidSale()) {
            return "Void Sale";
        } else if (repository.isPreAuthSale()) {
            return "Pre Authorization";
        } else if (repository.isPreAuthManualSale()) {
            return "Pre-Auth Manual";
        } else if (repository.isInstallmentSale()) {
            return "Installment Sale";
        } else if (repository.isPreCompSale()) {
            return "Pre-Completion";
        } else if (repository.isRefundSale()) {
            return "Refund";
        } else if (repository.isRefundManualSale()) {
            return "Refund Manual";
        } else if (repository.isCashBackSale()) {
            return "Cash Back Sale";
        } else if (repository.isQuasiCashFlow()) {
            return "Quasi Cash Sale";
        } else if (repository.isQuasiCashManualFlow()) {
            return "Quasi Cash Manual";
        } else if (repository.isCashAdvance()) {
            return "Cash Advance";
        } else if (repository.isStudentRefSale()){
            return "Student Ref Sale";
        } else if (repository.isAuthOnlySale()){
            return "Auth Only";
        } else {
            return "Sale";
        }
    }

    protected boolean shouldAutoSettle(TCT tct) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Const.AUTO_SETTLE_DATE_FORMAT + " " + Const.AUTO_SETTLE_TIME_FORMAT);
            Date settlementDate = dateFormat.parse(tct.getAutoSettDate() + " " + tct.getAutoSettTime());
            return new Date().after(settlementDate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isViewAttached() {
        return mView != null;
    }

    public boolean ispendingsettlement() {
        return repository.hasPendingAutoSettlement();
    }

}

