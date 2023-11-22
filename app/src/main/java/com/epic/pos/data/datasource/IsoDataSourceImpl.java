package com.epic.pos.data.datasource;

import com.epic.pos.service.SocketConnectionService;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IsoDataSourceImpl implements IsoDataSource {
    private SocketConnectionService socketConnectionService;

    public IsoDataSourceImpl(SocketConnectionService socketConnectionService) {
        this.socketConnectionService = socketConnectionService;
    }

    @Override
    public void saleRequest(String ip, String port, String saleTransaction, SaleTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, saleTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }

    @Override
    public void reversalRequest(String ip, String port, String reversalTransaction, ReversalTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, reversalTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }

    @Override
    public void voidRequest(String ip, String port, String voidTransaction, VoidTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, voidTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }

    @Override
    public void qrRequest(String ip, String port, String qrReqTransaction, QrRequestTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, qrReqTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }

    @Override
    public void settlementRequest(String ip, String port, String settlementTransaction, SettlementTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, settlementTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }

    @Override
    public void batchUploadRequest(String ip, String port, String batchUploadTransaction, BatchUploadTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, batchUploadTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }
    @Override
    public void DCCRequest(String ip, String port, String dccTransaction, DCCTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port, dccTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }
    @Override
    public void DCCDownloadRequest(String ip, String port, String dccdownloadtran, DCCDownloadTransactionListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port,dccdownloadtran))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }
    @Override
    public void keyDownloadRequest(String ip, String port, String keyDownloadTransaction, KeyDownloadListener listener) {
        Observable.fromCallable(() -> socketConnectionService.getServerResponse(ip, port,keyDownloadTransaction))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onReceived, listener::onError, listener::onCompleted);
    }

}
