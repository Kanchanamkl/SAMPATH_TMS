package com.epic.pos.ui.common.receipttype;

import android.graphics.Bitmap;

import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.listener.PrintListener;
import com.epic.pos.domain.entity.HomeMenuBean;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.ui.BasePresenter;

import javax.inject.Inject;

public class ReceiptTypePresenter extends BasePresenter<ReceiptTypeContract.View> implements ReceiptTypeContract.Presenter{

    @Inject
    public ReceiptTypePresenter(Repository repository) {
        this.repository = repository;
      //  this.networkConnection = networkConnection;
    }


    @Override
    public void initData() {

    }

    @Override
    public void closeButtonPressed() {

    }

    @Override
    public void customercopyclicked() {
        printreceipt(false);
    }

    @Override
    public void merchantcopyclicked() {
        printreceipt(true);
    }

    private void printreceipt(Boolean ismerchant){
        if (!repository.isTerminalDisabled()) {
            repository.getFeature(HomeMenuBean.TYPE_PRINT_LAST_RECEIPT, feature -> {
                if (feature.getEnabled() == 1) {
                    repository.getLastTransaction(transaction -> {
                        if (transaction != null) {
                            printDuplicateTxnCopy(transaction,ismerchant);
                        } else {
                            mView.showToastMessage(Const.MSG_TXN_EMPTY);
                        }
                    });
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                    }
                }
            });
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
            }
        }
    }
    private boolean isViewNotNull() {
        return mView != null;
    }
    private void printDuplicateTxnCopy(Transaction transaction, Boolean ismerchantcopy) {
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
}
