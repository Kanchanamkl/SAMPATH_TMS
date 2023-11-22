package com.epic.pos.ui.sale.precomp;

import com.epic.pos.common.Const;
import com.epic.pos.common.TranTypes;
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
 * @since 2021-05-03
 */
public class PreCompPresenter extends BasePresenter<PreCompContract.View> implements PreCompContract.Presenter {

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

    private String errorMsg = "";

    private Reversal reversal;

    @Inject
    public PreCompPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public String getTitle() {
        return getSaleTitle(repository);
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
        merchant = null;
        repository.getHostByHostId(repository.getSelectedHostIdForPreComp(), host -> {
            PreCompPresenter.this.host = host;
            repository.getMerchantById(repository.getSelectedMerchantIdForPreComp(), merchant -> {
                PreCompPresenter.this.merchant = merchant;
                mView.onDateReceived(host.getHostName(), merchant.getMerchantName());
            });
        });
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
                    if (transaction.getTransaction_code() == TranTypes.SALE_PRE_AUTHORIZATION
                            || transaction.getTransaction_code() == TranTypes.SALE_PRE_AUTHORIZATION_MANUAL) {
                        //pre auth txn
                        PreCompPresenter.this.transaction = transaction;
                        repository.getIssuerById(transaction.getIssuer_number(), issuer -> {
                            PreCompPresenter.this.issuer = issuer;
                            repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                                PreCompPresenter.this.host = host;
                                if (host.getMustSettleFlag() == 0) {
                                    repository.getMerchantById(transaction.getMerchant_no(), merchant -> {
                                        String maskingFormat = issuer.getMaskDisplay();

                                        if (transaction.getPan().length() != 16) {
                                            maskingFormat = Utility.getMaskingFormat(transaction.getPan());
                                        }

                                        PreCompPresenter.this.merchant = merchant;
                                        state = State.PROCEED_FLOW;
                                        mView.onInvoiceDataReceived(transaction, issuer, merchant, Utility.maskCardNumber(transaction.getPan(), maskingFormat));
                                        mView.setConfirmEnabled(true);
                                    });
                                } else {
                                    mView.onShowError(Const.MSG_SETTLE_TO_PROCEED);
                                }
                            });
                        });
                    }else {
                        mView.onShowError(Const.MSG_SELECT_PRE_AUTH_SALE);
                    }
                } else {
                    mView.onShowError(Const.MSG_INCORRECT_INVOICE_NO);
                }
            });
        } else if (state == State.PROCEED_FLOW) {
            proceedPreComp();
        }
    }

    private void proceedPreComp() {
        repository.saveCurrentPreCompSaleId(transaction.getId());
        repository.saveSelectedTerminalId(transaction.getTerminal_no());
        mView.gotoAmountActivity();
    }

    @Override
    public void setInvoiceNumber(String invoice) {
        PreCompPresenter.this.invoice = invoice;
        mView.setConfirmEnabled(invoice.length() == Const.INVOICE_NO_MAX_LEN);
    }
}