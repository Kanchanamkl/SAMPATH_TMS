package com.epic.pos.ui.sale.signature;

import android.graphics.Bitmap;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.AppUtil;
import com.epic.pos.util.ImageUtils;

import java.io.File;

import javax.inject.Inject;

public class SignaturePresenter extends BasePresenter<SignatureContact.View> implements SignatureContact.Presenter {

    private final String TAG = SignaturePresenter.class.getSimpleName();

    private Repository repository;
    private NetworkConnection networkConnection;

    private String invoiceNum;
    private String amount;
    private Terminal terminal;
    private Merchant merchant;
    private Currency currency;

    private Transaction preCompTxn;

    @Inject
    public SignaturePresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void initData() {
        if (repository.isPreCompSale()) {
            int currentPreCompSaleId = repository.getCurrentPreCompSaleId();
            repository.getTransactionById(currentPreCompSaleId, transaction -> {
                SignaturePresenter.this.preCompTxn = transaction;
                invoiceNum = preCompTxn.getInvoice_no();
            });
        }

        amount = repository.getTotalAmount();

        int selectedTerminalId = repository.getSelectedTerminalId();
        repository.getTerminalById(selectedTerminalId, terminal -> {
            SignaturePresenter.this.terminal = terminal;
            repository.getMerchantById(terminal.getMerchantNumber(), merchant -> {
                SignaturePresenter.this.merchant = merchant;
                repository.getCurrencyByMerchantId(merchant.getMerchantNumber(), currency -> {
                    if (!repository.isPreCompSale()){
                        invoiceNum = AppUtil.toInvoiceNumber(Integer.parseInt(merchant.getInvNumber()));
                    }

                    SignaturePresenter.this.currency = currency;
                    updateUi();
                });
            });
        });
    }

    private void updateUi() {
        mView.updateUi(currency.getCurrencySymbol(), amount);
    }

    @Override
    public void saveSignature(Bitmap bitmap) {
        if (isViewNotNull()){
            mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PROCESSING_SIGNATURE);
        }

        repository.setSignatureRequired(true);
        mView.setConfirmBtnEnabled(false);

        ImageUtils.getInstance().saveSignature(bitmap, String.valueOf(merchant.getMerchantNumber()), invoiceNum, new ImageUtils.SaveSignatureListener() {
            @Override
            public void onSaved(File path) {
                if (isViewNotNull()){
                    //mView.hideLoader();
                    mView.gotoReceiptActivity();
                }
            }

            @Override
            public void onError(Exception ex) {
                if (isViewNotNull()){
                    mView.hideLoader();
                    mView.setConfirmBtnEnabled(true);
                }
            }
        });
    }

    private boolean isViewNotNull(){
        return mView != null;
    }
}