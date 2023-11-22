package com.epic.pos.ui.sale.receipt;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.iso.modal.Transaction;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.receipt.modal.SaleReceipt;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.ImageUtils;
import com.epic.pos.util.Utility;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
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
 * @since 2021-03-23
 */
public class ReceiptPresenter extends BasePresenter<ReceiptContact.View> implements ReceiptContact.Presenter {

    private final String TAG = ReceiptPresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private CardAction cardAction;
    private CardData cardData;
    private com.epic.pos.data.db.dbtxn.modal.Transaction txn;
    private Merchant mch;

    private CardDefinition cardDefinition;
    private Issuer issuer;
    private Host host;
    private TCT tct;
    //Merchant copy
    private SaleReceipt mReceipt;
    private Bitmap merchantReceipt;
    private Bitmap customerReceipt;

    @Inject
    public ReceiptPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void closeButtonPressed() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void CheckReceiptPrintWithECR() {
        Log.d("Buddhika ECR ", String.valueOf(repository.isEcrInitiatedSale()));
        if(repository.isEcrInitiatedSale()){
            closeButtonPressed();
        }
        else{
            initData();
        }
    }
    @Override
    public String getTitle() {
        return getSaleTitle(repository);
    }

    @Override
    public void initData() {
        if (isViewNotNull()) {
            mView.setActionButtonEnabled(false);
        }
        repository.getTCT(t -> {
            tct = t;
        });

        PosDevice.getInstance().stopEmvFlow();

        if (repository.isPreCompSale()) {
            //pre comp sale
            int saleId = repository.getCurrentPreCompSaleId();
            repository.getTransactionById(saleId, transaction -> {
                ReceiptPresenter.this.txn = transaction;
                repository.getIssuerById(transaction.getIssuer_number(), issuer -> {
                    ReceiptPresenter.this.issuer = issuer;
                    repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                        ReceiptPresenter.this.host = host;
                        repository.getMerchantById(txn.getMerchant_no(), merchant -> {
                            mch = merchant;
                            generateMerchantCopy();
                        });
                    });
                });
            });
        }
        else if(repository.isQrSale()){
            log("QR SALE RECEIPT");

                repository.getIssuerById(8, issuer -> {
                    ReceiptPresenter.this.issuer = issuer;
                    repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                        ReceiptPresenter.this.host = host;
                        repository.getTransactionById(repository.getCurrentSaleId(), transaction -> {
                            txn = transaction;
                            repository.getMerchantById(txn.getMerchant_no(), merchant -> {
                                mch = merchant;
                                generateMerchantCopy();
                            });
                        });
                    });
                });

        }
        else {
            //not pre comp
            cardAction = repository.getCardAction();
            cardData = repository.getCardData();
            log("card action: " + cardAction.val);
            log("card data: " + cardData.toString());

            repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
                ReceiptPresenter.this.cardDefinition = cardDefinition;
                repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                    ReceiptPresenter.this.issuer = issuer;
                    repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                        ReceiptPresenter.this.host = host;
                        repository.getTransactionById(repository.getCurrentSaleId(), transaction -> {
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
    }

    public void generateMerchantCopy() {
        if (isViewNotNull()) {
            mView.showLoader("Printing receipt", "Please wait");
        }

        mReceipt = new SaleReceipt();
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

        String maskingFormat = issuer.getMaskMerchantCopy();

        if (!repository.isQrSale()) {
            if (txn.getPan().length() != 16) {
                maskingFormat = Utility.getMaskingFormat(txn.getPan());
            }


            String posEntryMode = Transaction.posEntryModeToString(String.valueOf(txn.getChip_status()));

            mReceipt.setCardNo(Utility.maskCardNumber(txn.getPan(), maskingFormat) + " " + posEntryMode);
            mReceipt.setExpireDate(Utility.maskCardNumber(txn.getExp_date(), issuer.getMaskExpireDate()));

            // card label logic
            String cardLabel = getCardLabel(cardAction, txn);

            mReceipt.setCardType(cardLabel);
            // end of card label logic

            String txnAid = PosDevice.getInstance().getTransactionAid();
            mReceipt.setAid(txnAid);
        }
        mReceipt.setApprCode(txn.getApprove_code());
        mReceipt.setRefNo(txn.getRrn());

        mReceipt.setCurrency(txn.getCurrency_symbol());
        mReceipt.setTotalAmount(Utility.getFormattedAmount(Long.parseLong(txn.getTotal_amount())));
        mReceipt.setBaseAmount(Utility.getFormattedAmount(Long.parseLong(txn.getBase_transaction_amount())));

        String chn = !TextUtils.isEmpty(txn.getCard_holder_name()) ? txn.getCard_holder_name().trim() : "";
        mReceipt.setCardHolderName(chn);

        mReceipt.setMerchantCopy(true);
        mReceipt.setCustomerCopy(false);

        mReceipt.setOfflineSale(repository.isOfflineManualSale() || repository.isOfflineSale());
        mReceipt.setPreAuth(repository.isPreAuthSale() || repository.isPreAuthManualSale());
        mReceipt.setAuthOnly(repository.isAuthOnlySale());
        mReceipt.setInstallment(repository.isInstallmentSale());
        mReceipt.setPreComp(repository.isPreCompSale());
        mReceipt.setRefund(repository.isRefundSale() || repository.isRefundManualSale());
        mReceipt.setQuasiCash(repository.isQuasiCashFlow() || repository.isQuasiCashManualFlow());
        mReceipt.setCashAdvance(repository.isCashAdvance());
        mReceipt.setQrSale(repository.isQrSale());

        if (!TextUtils.isEmpty(txn.getStd_ref_no())){
            mReceipt.setStudentRefNo(txn.getStd_ref_no());
        }

        if (repository.isCashBackSale()) {
            mReceipt.setCashBack(true);
            mReceipt.setCashBackAmount(Utility.getFormattedAmount(Long.parseLong(txn.getCash_back_amount())));
        }

        //set signature type
        if (repository.isPreCompSale()) {
            log("isPreCompSale true, set signature type: signature");
            mReceipt.setSignatureType(SaleReceipt.SignatureType.SIGNATURE);
        } else {
            if (cardAction == CardAction.SWIPE) {
                log("swipe true, set signature type: signature");
                mReceipt.setSignatureType(SaleReceipt.SignatureType.SIGNATURE);
            } else {
                if (!repository.isCardPinEntered() && !repository.isSignatureRequired()) {
                    log("no cvm required");
                    mReceipt.setSignatureType(SaleReceipt.SignatureType.NO_CVM_REQUIRED);
                } else if (repository.isSignatureRequired()) {
                    log("signature required");
                    mReceipt.setSignatureType(SaleReceipt.SignatureType.SIGNATURE);
                } else if (repository.isCardPinEntered()) {
                    log("pin verified");
                    mReceipt.setSignatureType(SaleReceipt.SignatureType.PIN_VERIFIED);
                }
            }
        }

        mReceipt.toUpperCase();

        log("generate sale receipt.");
        MyApp.getInstance().getAppReceipts().generateSaleReceipt(mReceipt, new AppReceipts.ReceiptListener() {
            @Override
            public void onReceiptGenerated() {
                log("sale receipt generated.");
                merchantReceipt = ImageUtils.getInstance().getMerchantSaleReceipt(mReceipt.getMerchantNo(), mReceipt.getInvoiceNo());
                if(repository.isEcrInitiatedSale() && (tct.getECRWithourReceipt()==1)){
                    mView.onCustomerReceiptGenerated(merchantReceipt , txn,repository.isTranSyncEnable());
                    mView.hideLoader();
                    mView.setUiVisible();
                }else {

                    generateCustomerCopy();
                    if (PosDevice.getInstance().isPaperExistsInPrinter()) {
                        log("paper exist in printer.");
                        printMerchantCopy();
                    } else {
                        log("paper not exists.");
                        if (isViewNotNull()) {
                            mView.onMerchantCopyPrintError("Feed paper into the printer and close.");
                        }
                    }


                }

            }

            @Override
            public void onReceiptGenerationFailed() {
                log("sale receipt generation failed.");
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.onReceiptGenerationError(Const.MSG_RECEIPT_GENERATION_ERROR);
                }
            }
        });
    }


    private void generateCustomerCopy() {
        String maskingFormat = issuer.getMaskCustomerCopy();

        if(!repository.isQrSale()) {
            if (txn.getPan().length() != 16) {
                maskingFormat = Utility.getMaskingFormat(txn.getPan());
            }

            String posEntryMode = Transaction.posEntryModeToString(String.valueOf(txn.getChip_status()));


        mReceipt.setCardNo(Utility.maskCardNumber(txn.getPan(), maskingFormat) + " " + posEntryMode);
        }
        else {
            mReceipt.setCardNo(txn.getPan());
        }


        mReceipt.setMerchantCopy(false);
        mReceipt.setCustomerCopy(true);
        mReceipt.setSignatureType(SaleReceipt.SignatureType.SIGNATURE_NOT_REQUIRED);
        mReceipt.toUpperCase();

        MyApp.getInstance().getAppReceipts().generateSaleReceipt(mReceipt, new AppReceipts.ReceiptListener() {
            @Override
            public void onReceiptGenerated() {
                customerReceipt = ImageUtils.getInstance().getCustomerSaleReceipt(mReceipt.getMerchantNo(), mReceipt.getInvoiceNo());
                if (isViewNotNull()) {
                    mView.onCustomerReceiptGenerated(customerReceipt ,txn,repository.isTranSyncEnable());
                    mView.hideLoader();
                    mView.setUiVisible();
                }
            }

            @Override
            public void onReceiptGenerationFailed() {
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.onReceiptGenerationError(Const.MSG_RECEIPT_GENERATION_ERROR);
                }
            }
        });
    }

    public void printMerchantCopy() {
        PosDevice.getInstance().startPrinting();

        log("printMerchantCopy()");
        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_IMAGE);
        p.setBitmap(merchantReceipt);
        p.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                log("onPrintFinished()");
                merchantReceipt.recycle();
                if (isViewNotNull()) {
                    mView.setActionButtonEnabled(true);
                }
            }

            @Override
            public void onPrintError(PrintError printError) {
                log("onPrintError()");
                if (isViewNotNull()) {
                    mView.onMerchantCopyPrintError(printError.getMsg());
                    mView.setActionButtonEnabled(true);
                }
            }
        });
        PosDevice.getInstance().addToPrintQueue(p);
    }

    @Override
    public void printCustomerCopy() {
        PosDevice.getInstance().startPrinting();

        if (isViewNotNull()) {
            mView.setActionButtonEnabled(false);
        }

        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_IMAGE);
        p.setBitmap(customerReceipt);
        p.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                if (isViewNotNull()) {
                    mView.onCustomerReceiptPrinted();
                }
            }

            @Override
            public void onPrintError(PrintError printError) {
                if (isViewNotNull()) {
                    mView.setActionButtonEnabled(true);
                    mView.onCustomerCopyPrintError(printError.getMsg());
                }
            }
        });
        PosDevice.getInstance().addToPrintQueue(p);
    }

    private boolean isViewNotNull() {
        return mView != null;
    }

    @Override
    public void retryToPrintMerchantCopy() {
        generateMerchantCopy();
    }

    @Override
    public void retryToPrintCustomerCopy() {
        printCustomerCopy();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }


}