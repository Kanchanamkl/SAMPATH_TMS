package com.epic.pos.data.datasource;

import org.jpos.iso.ISOMsg;

public interface IsoDataSource {

    interface SaleTransactionListener {
        void onReceived(ISOMsg saleResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }

    interface ReversalTransactionListener {
        void onReceived(ISOMsg reversalResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }

    interface VoidTransactionListener {
        void onReceived(ISOMsg voidResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }

    interface QrRequestTransactionListener {
        void onReceived(ISOMsg qrResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }

    interface SettlementTransactionListener {
        void onReceived(ISOMsg settlementResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }

    interface KeyDownloadListener {
        void onReceived(ISOMsg keyResObj);
        void onError(Throwable throwable);
        void onCompleted();
    }

    interface BatchUploadTransactionListener {
        void onReceived(ISOMsg batchUploadResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }
    interface DCCTransactionListener {
        void onReceived(ISOMsg dccResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }
    interface DCCDownloadTransactionListener {
        void onReceived(ISOMsg dccdownResObj);
        void onError(Throwable throwable);
        void onTLEError(String errorMsg);
        void onCompleted();
    }
    void saleRequest(String ip, String port, String saleTransaction, SaleTransactionListener listener);
    void reversalRequest(String ip, String port, String reversalTransaction, ReversalTransactionListener listener);
    void voidRequest(String ip, String port, String voidTransaction, VoidTransactionListener listener);
    void qrRequest(String ip, String port, String qrReqTransaction, QrRequestTransactionListener listener);
    void settlementRequest(String ip, String port, String settlementTransaction, SettlementTransactionListener listener);
    void batchUploadRequest(String ip, String port, String batchUploadTransaction, BatchUploadTransactionListener listener);
    void DCCRequest(String ip, String port, String dccTransaction, DCCTransactionListener listener);
    void DCCDownloadRequest(String ip, String port, String dccdownloadtran, DCCDownloadTransactionListener listener);
    void keyDownloadRequest(String ip, String port, String keyDownloadTransaction, KeyDownloadListener listener);
}
