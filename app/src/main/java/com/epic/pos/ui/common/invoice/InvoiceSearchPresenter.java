package com.epic.pos.ui.common.invoice;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.Utility;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-07-05
 */
public class InvoiceSearchPresenter extends BasePresenter<InvoiceSearchContact.View> implements InvoiceSearchContact.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;

    private enum State {
        SEARCH_INVOICE, PROCEED_FLOW
    }

    private String invoice;
    private State state = State.SEARCH_INVOICE;
    private Transaction transaction;

    private Issuer issuer;
    private Host host;
    private Merchant merchant;
    private String title;

    private String errorMsg = "";

    private Reversal reversal;

    @Inject
    public InvoiceSearchPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void setHost(Host host) {
        this.host = host;
        validateAndSetData();
    }

    @Override
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
        validateAndSetData();
    }

    private void validateAndSetData(){
        if (host != null && merchant != null){
            mView.onDateReceived(host.getHostName(), merchant.getMerchantName());
        }
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void resetData() {
        repository.saveTransactionOngoing(true);
        state = State.SEARCH_INVOICE;
        invoice = "";
        transaction = null;
    }

    @Override
    public void clearVoid() {
        resetData();
        mView.onClearVoidUI();
    }

    @Override
    public void onSubmit() {
        if (state == State.SEARCH_INVOICE) {
            mView.setConfirmEnabled(false);
            repository.getTransaction(host.getHostID(), merchant.getMerchantNumber(), invoice, transaction -> {
                if (transaction != null) {
                    InvoiceSearchPresenter.this.transaction = transaction;
                    repository.getIssuerById(transaction.getIssuer_number(), issuer -> {
                        InvoiceSearchPresenter.this.issuer = issuer;
                        repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                            InvoiceSearchPresenter.this.host = host;
                            repository.getMerchantById(transaction.getMerchant_no(), merchant -> {
                                String maskingFormat = issuer.getMaskDisplay();
                                if(issuer.getIssuerNumber()==8) {
                                    transaction.setPan("0000000000000000");

                                }
                                if (transaction.getPan().length() != 16) {
                                    maskingFormat = Utility.getMaskingFormat(transaction.getPan());
                                }
                                InvoiceSearchPresenter.this.merchant = merchant;
                                state = State.PROCEED_FLOW;
                                mView.onInvoiceDataReceived(transaction, issuer, merchant, Utility.maskCardNumber(transaction.getPan(), maskingFormat));
                                mView.setConfirmEnabled(true);
                            });
                        });
                    });
                } else {
                    mView.onShowError(Const.MSG_INCORRECT_INVOICE_NO);
                }
            });
        } else if (state == State.PROCEED_FLOW) {
            mView.onTxnSelected(transaction);
        }
    }



    @Override
    public void setInvoiceNumber(String invoice) {
        InvoiceSearchPresenter.this.invoice = invoice;
        mView.setConfirmEnabled(invoice.length() == Const.INVOICE_NO_MAX_LEN);
    }
}