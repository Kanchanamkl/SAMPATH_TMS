package com.epic.pos.ui.voidsale.receipt;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.epic.pos.common.Const;
import com.epic.pos.common.TranTypes;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.iso.modal.Transaction;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.receipt.modal.VoidReceipt;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.ImageUtils;
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.listener.PrintListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

/**
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-05-03
 */
public class VoidReceiptPresenter extends BasePresenter<VoidReceiptContact.View> implements VoidReceiptContact.Presenter {

    private final String TAG = VoidReceiptPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private com.epic.pos.data.db.dbtxn.modal.Transaction txn;
    private Merchant mch;
    private VoidReceipt mReceipt;

    private CardDefinition cardDefinition;
    private Issuer issuer;
    private Host host;

    private Bitmap merchantReceipt;
    private Bitmap customerReceipt;

    @Inject
    public VoidReceiptPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void initData() {
        repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
            VoidReceiptPresenter.this.cardDefinition = cardDefinition;
            repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                VoidReceiptPresenter.this.issuer = issuer;
                repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                    VoidReceiptPresenter.this.host = host;
                    repository.getTransactionById(repository.getCurrentVoidSaleId(), transaction -> {
                        txn = transaction;
                        repository.getMerchantById(txn.getMerchant_no(), merchant -> {
                            mch = merchant;
                            generateMerchantCopy();
                        });
                    });
                });
            });
        });
    }

    private void generateMerchantCopy() {
        if (mView != null) {
            mView.showLoader("Printing receipt", "Please wait");
        }

        mReceipt = new VoidReceipt();
        mReceipt.setAddressLine1(mch.getRctHdr1());
        mReceipt.setAddressLine2(mch.getRctHdr2());
        mReceipt.setAddressLine3(mch.getRctHdr3());

        try {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);

            Date txnDateTime = new SimpleDateFormat(Const.TXN_DATE_TIME, Locale.ENGLISH)
                    .parse(year + txn.getTxn_date() + " " + txn.getTxn_time());
            String receiptDateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH)
                    .format(txnDateTime);
            mReceipt.setDateTime(receiptDateTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int bNo = Integer.parseInt(mch.getBatchNumber());
        String batchNo = Utility.padLeftZeros(String.valueOf(bNo), 6);

        mReceipt.setMerchantNo(String.valueOf(txn.getMerchant_no()));
        mReceipt.setMerchantId(txn.getMerchant_id());
        mReceipt.setTerminalId(txn.getTerminal_id());
        mReceipt.setBatchNo(batchNo);
        mReceipt.setInvoiceNo(txn.getInvoice_no());
        mReceipt.setOfflineSale(txn.isOfflineTransaction());
        mReceipt.setPreCompTxn(txn.getTransaction_code() == TranTypes.SALE_PRE_COMPLETION);
        mReceipt.setInstallmentTxn(txn.getTransaction_code() == TranTypes.SALE_INSTALLMENT);
        mReceipt.setRefundSale(txn.getTransaction_code() == TranTypes.SALE_REFUND || txn.getTransaction_code() == TranTypes.SALE_REFUND_MANUAL);
        mReceipt.setQuasiCash(txn.getTransaction_code() == TranTypes.QUASI_CASH || txn.getTransaction_code() == TranTypes.QUASI_CASH_MANUAL);

        if (!TextUtils.isEmpty(txn.getStd_ref_no())) {
            mReceipt.setStudentRef(txn.getStd_ref_no());
        }

        String maskingFormat = issuer.getMaskMerchantCopy();

        if (txn.getPan().length() != 16) {
            maskingFormat = Utility.getMaskingFormat(txn.getPan());
        }

        String posEntryMode = Transaction.posEntryModeToString(String.valueOf(txn.getChip_status()));

        mReceipt.setCardNo(Utility.maskCardNumber(txn.getPan(), maskingFormat) + " " + posEntryMode);
        mReceipt.setExpireDate(Utility.maskCardNumber(txn.getExp_date(), issuer.getMaskExpireDate()));
        mReceipt.setCardType(txn.getCard_label());
        mReceipt.setApprCode(txn.getApprove_code());
        mReceipt.setRefNo(txn.getRrn());

        mReceipt.setCurrency(txn.getCurrency_symbol());
        mReceipt.setAmount(Utility.getFormattedAmount(Long.parseLong(txn.getTotal_amount())));

        mReceipt.setMerchantCopy(true);
        mReceipt.setCustomerCopy(false);

        mReceipt.toUpperCase();


        MyApp.getInstance().getAppReceipts().generateVoidSaleReceipt(mReceipt, new AppReceipts.ReceiptListener() {
            @Override
            public void onReceiptGenerated() {
                merchantReceipt = ImageUtils.getInstance().getMerchantVoidSaleReceipt(mReceipt.getMerchantNo(), mReceipt.getInvoiceNo());
                printMerchantCopy();
                generateCustomerCopy();
            }

            @Override
            public void onReceiptGenerationFailed() {
                if (mView != null) {
                    mView.hideLoader();
                }
            }
        });
    }


    private void generateCustomerCopy() {
        String maskingFormat = issuer.getMaskCustomerCopy();

        if (txn.getPan().length() != 16) {
            maskingFormat = Utility.getMaskingFormat(txn.getPan());
        }

        String posEntryMode = Transaction.posEntryModeToString(String.valueOf(txn.getChip_status()));

        mReceipt.setCardNo(Utility.maskCardNumber(txn.getPan(), maskingFormat) + " " + posEntryMode);
        mReceipt.setMerchantCopy(false);
        mReceipt.setCustomerCopy(true);

        mReceipt.toUpperCase();
        MyApp.getInstance().getAppReceipts().generateVoidSaleReceipt(mReceipt, new AppReceipts.ReceiptListener() {
            @Override
            public void onReceiptGenerated() {
                customerReceipt = ImageUtils.getInstance().getCustomerVoidSaleReceipt(mReceipt.getMerchantNo(), mReceipt.getInvoiceNo());

                if (mView != null) {
                    mView.onCustomerReceiptGenerated(customerReceipt);
                    mView.hideLoader();
                }
            }

            @Override
            public void onReceiptGenerationFailed() {
                if (mView != null) {
                    mView.hideLoader();
                }
            }
        });
    }

    private void printMerchantCopy() {
        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_IMAGE);
        p.setBitmap(merchantReceipt);
        p.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                merchantReceipt.recycle();
            }

            @Override
            public void onPrintError(PrintError printError) {
                if (mView != null) {
                    mView.onMerchantCopyPrintError(printError.getMsg());
                }
            }
        });
        PosDevice.getInstance().addToPrintQueue(p);
    }


    @Override
    public void printCustomerCopy() {
        if (mView != null) {
            mView.setPrintButtonEnabled(false);
        }


        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_IMAGE);
        p.setBitmap(customerReceipt);
        p.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                if (mView != null) {
                    mView.onCustomerReceiptPrinted();
                }
            }

            @Override
            public void onPrintError(PrintError printError) {
                if (mView != null) {
                    mView.setPrintButtonEnabled(true);
                    mView.onCustomerCopyPrintError(printError.getMsg());
                }
            }
        });
        PosDevice.getInstance().addToPrintQueue(p);
    }

    @Override
    public void retryToPrintMerchantCopy() {
        printMerchantCopy();
    }

    @Override
    public void retryToPrintCustomerCopy() {
        printCustomerCopy();
    }


}