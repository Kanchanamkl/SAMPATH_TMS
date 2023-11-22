package com.epic.pos.receipt;

import static com.epic.pos.common.Const.RECEIPT_DIAGNOSTIC_REPORT;
import static com.epic.pos.common.Const.RECEIPT_DUPLICATE;
import static com.epic.pos.common.Const.RECEIPT_SETTLEMENT_SUCCESS;
import static com.epic.pos.common.TranTypes.CASH_ADVANCE;
import static com.epic.pos.common.TranTypes.CASH_BACK;
import static com.epic.pos.common.TranTypes.QR_SALE;
import static com.epic.pos.common.TranTypes.QUASI_CASH;
import static com.epic.pos.common.TranTypes.QUASI_CASH_MANUAL;
import static com.epic.pos.common.TranTypes.SALE;
import static com.epic.pos.common.TranTypes.SALE_INSTALLMENT;
import static com.epic.pos.common.TranTypes.SALE_MANUAL;
import static com.epic.pos.common.TranTypes.SALE_OFFLINE;
import static com.epic.pos.common.TranTypes.SALE_OFFLINE_MANUAL;
import static com.epic.pos.common.TranTypes.SALE_PRE_COMPLETION;
import static com.epic.pos.common.TranTypes.SALE_REFUND;
import static com.epic.pos.common.TranTypes.SALE_REFUND_MANUAL;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.epic.pos.BuildConfig;
import com.epic.pos.common.Const;
import com.epic.pos.common.TranTypes;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.PrintDataBuilder;
import com.epic.pos.device.data.PrintItem;
import com.epic.pos.receipt.modal.SaleReceipt;
import com.epic.pos.receipt.modal.SettlementReceipt;
import com.epic.pos.receipt.modal.VoidReceipt;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.Formatter;
import com.epic.pos.util.ImageUtils;
import com.epic.pos.util.Partition;
import com.epic.pos.util.Utility;
import com.epic.pos.util.ValidatorUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AppReceipts class use to generate recepit images.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-18
 */
public class AppReceipts {

    private final String TAG = AppReceipts.class.getSimpleName();
    private Context context;

    private String serialNo = "";
    private String versionName = "";
    private String versionInfo = "";

    public AppReceipts(Context context) {
        this.context = context;
        versionName = BuildConfig.VERSION_NAME;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
        versionInfo = serialNo + " " + versionName;
        AppLog.i(TAG, "AppReceipts: version info: " + versionInfo);
    }

    private final String DASH_LINE = "-------------------------------------------------------------------------";
    private final String LINE = "_________________________________________________________________________";
    private final String SHORT_DASH_LINE = "--------";
    private final String DOT_SIGNATURE_LINE = "....................................................";
    private final String DIR_SALE_RECEIPTS = "sale_receipts";
    private final String DIR_IMAGES = "images";

    private final int RECEIPT_SIZE = 760;
    private final int MARGIN_TOP = 40;
    private final int MARGIN_BOTTOM = 200;

    private int TEXT_LINE_SIZE = 20;
    private int TEXT_XXXSMALL = 30;
    private int TEXT_XXSMALL = 38;
    private int TEXT_XSMALL = 42;
    private int TEXT_SMALL = 46;
    private int TEXT_MEDIUM = 50;
    private int TEXT_LARGE = 54;
    private int TEXT_XLARGE = 58;

    public void generateVoidSaleReceipt(VoidReceipt receipt, ReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                Bitmap bankLogo = getBankLogo(receipt.getBankLogo());

                String saleTitle = Const.RECEIPT_VOID_SALE;

                if (receipt.isOfflineSale()) {
                    saleTitle = Const.RECEIPT_VOID_OFFLINE_SALE;
                } else if (receipt.isPreCompTxn()) {
                    saleTitle = Const.RECEIPT_VOID_PRE_COMP;
                } else if (receipt.isInstallmentTxn()) {
                    saleTitle = Const.RECEIPT_VOID_INSTALLMENT;
                } else if (receipt.isRefundSale()) {
                    saleTitle = Const.RECEIPT_VOID_REFUND;
                } else if (receipt.isQuasiCash()) {
                    saleTitle = Const.RECEIPT_VOID_QUASI_CASH;
                }

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(receipt.getAddressLine1())
                        .addText(receipt.getAddressLine2())
                        .addText(receipt.getAddressLine3())
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        .setTextSize(TEXT_XXSMALL)
                        //date time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getDateTime(), true)
                        //mid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MERCHANT_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getMerchantId(), true)
                        //tid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getTerminalId(), true)
                        //batch no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_BATCH_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getBatchNo(), true)
                        //invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_INVOICE_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getInvoiceNo(), true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        //void sale
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(saleTitle)
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        //card no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_CARD_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getCardNo(), true)
                        //exp date
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_EXP_DATE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getExpireDate(), true)
                        //card type
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_CARD_TYPE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getCardType(), true)
                        //appr code
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_APPR_CODE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getApprCode(), true)
                        //ref no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_REF_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getRefNo(), true);

                if (!TextUtils.isEmpty(receipt.getStudentRef())) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_STD_REF_NO, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getStudentRef(), true);
                }

                //line
                builder.setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine();


                int amountFontSize = TEXT_MEDIUM;

                if (receipt.getAmount().length() >= 16) {
                    amountFontSize = TEXT_XSMALL;
                } else if (receipt.getAmount().length() >= 13) {
                    amountFontSize = TEXT_SMALL;
                }

                //amount
                builder.setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setTextSize(amountFontSize)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_AMOUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getCurrency() + " -" + receipt.getAmount(), true)
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine();

                if (receipt.isMerchantCopy()) {
                    builder.setTextSize(TEXT_MEDIUM)
                            .setAlign(Paint.Align.CENTER)
                            .addBlankSpace(70)
                            .addText(Const.VOID_MERCHANT_COPY)
                            .addBlankSpace(30);
                }

                if (receipt.isCustomerCopy()) {
                    builder.setTextSize(TEXT_MEDIUM)
                            .setAlign(Paint.Align.CENTER)
                            .addBlankSpace(70)
                            .addText(Const.VOID_CUSTOMER_COPY)
                            .addBlankSpace(30);
                }

                builder.setTextSize(TEXT_XXXSMALL)
                        .addText("", true)
                        .setTypeface(context, getFontRegular())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_TERMS_TXT_1)
                        .addText(Const.RECEIPT_TERMS_TXT_2)
                        .addText(versionInfo)
                        .setMarginBottom(MARGIN_BOTTOM);

                Handler handler = new Handler(context.getMainLooper());

                try {
                    Bitmap receiptImage = builder.build();

                    if (bankLogo != null)
                        bankLogo.recycle();

                    if (receipt.isCustomerCopy()) {
                        ImageUtils.getInstance().saveCustomerVoidSaleReceipt(receiptImage, receipt.getMerchantNo(), receipt.getInvoiceNo());
                    }

                    if (receipt.isMerchantCopy()) {
                        ImageUtils.getInstance().saveMerchantVoidSaleReceipt(receiptImage, receipt.getMerchantNo(), receipt.getInvoiceNo());
                    }

                    receiptImage.recycle();
                    handler.post(listener::onReceiptGenerated);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onReceiptGenerationFailed);
                }
            }
        }.start();
    }

    /**
     * Generate diagnostic report
     *
     * @param emvFile
     * @param listener
     */
    public void generateDiagnosticReport(File emvFile, ReceiptBuilderListener listener) {
        new Thread() {
            @Override
            public void run() {
                Handler handler = new Handler(context.getMainLooper());

                try {
                    List<PrintDataBuilder> printDataBuilders = new ArrayList<>();
                    PrintDataBuilder p1 = new PrintDataBuilder();
                    //address
                    p1.setFontSize(PrintItem.FontSize.SIZE_24);
                    p1.addTextMiddle(RECEIPT_DIAGNOSTIC_REPORT);
                    p1.setFontSize(PrintItem.FontSize.SIZE_16);
                    p1.addDotLine();

                    BufferedReader bufferedReader = new BufferedReader(new FileReader(emvFile));
                    String line = "";
                    int epocs = 0;

                    while ((line = bufferedReader.readLine()) != null) {
                        //tokenize the line
                        int start = 0;
                        int index = line.indexOf("|");
                        String tagName = line.substring(0, index);

                        start = ++index;
                        index = line.indexOf("|", start);
                        String tagDesc = line.substring(start, index);

                        start = ++index;
                        String tagData = line.substring(start, line.length());

                        if (tagName.equals("57")) {
                            tagData = Formatter.maskString(tagData, "**************", '*');
                        } else if (tagName.equals("5a")) {
                            tagData = Formatter.maskString(tagData, "**************", '*');
                        } else if (tagName.equals("5f24")) {
                            tagData = Formatter.maskString(tagData, "******", '*');
                        }

                        String tag = tagDesc + "[" + tagName + "]";
                        String val = tagData;
                        p1.addTextLeft(tag);
                        p1.addTextLeft(val);
                        p1.addTextLeft("  ");
                    }

                    p1.addDotLine();
                    p1.addTextMiddle(versionInfo);
                    p1.addSpace(4);

                    printDataBuilders.add(p1);

                    try {
                        handler.post(() -> listener.onReceiptGenerated(printDataBuilders));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        handler.post(listener::onReceiptGenerationFailed);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onReceiptGenerationFailed);
                }
            }
        }.start();
    }

    public void generateHostInfoReport(Host h, Merchant m, Terminal t, GenerateReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                Bitmap bankLogo = getBankLogo("img/boc_680.gif");

                String addressLine1 = !TextUtils.isEmpty(m.getRctHdr1()) ? m.getRctHdr1().toUpperCase() : "";
                String addressLine2 = !TextUtils.isEmpty(m.getRctHdr2()) ? m.getRctHdr2().toUpperCase() : "";
                String addressLine3 = !TextUtils.isEmpty(m.getRctHdr3()) ? m.getRctHdr3().toUpperCase() : "";
                String encStatus = (h.getTLEEnabled() == 1) ? "ENABLED" : "DISABLED";

                //date time
                String dateTime = "";
                try {
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    dateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH).format(new Date());
                    dateTime = dateTime.toUpperCase();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                String serial = "";
                try {
                    serial = PosDevice.getInstance().getSerialNo();
                    if (serial == null) {
                        serial = "";
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                String simNo = "";
                String simProvider = "";

                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    simNo = telephonyManager.getSimSerialNumber();
                    if (TextUtils.isEmpty(simNo)) {
                        simNo = "-";
                    }

                    simProvider = telephonyManager.getSimOperatorName();
                    if (TextUtils.isEmpty(simProvider)) {
                        simProvider = "-";
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(addressLine1)
                        .addText(addressLine2)
                        .addText(addressLine3)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        //title
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_HOST_INFO_REPORT)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        //date time
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(dateTime, true)
                        //host name
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_HOST_NAME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(h.getHostName(), true)
                        //nii
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_NII, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(t.getNII(), true)
                        //tpdu
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TPDU, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(t.getTPDU(), true)
                        //tid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(t.getTerminalID(), true);
                        if(m.getMerchantID().length()<16){
                        //mid
                        builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(m.getMerchantID(), true);}
                        else{
                        //mid
                        builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MID, true)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(m.getMerchantID(), true);
                        }

                        //snii
                        builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_SNII, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(t.getSecureNII(), true)
                        //encryption
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_ENCRYPTION, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(encStatus, true)
                        //terminal serial
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_SERIAL, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(serial, true)
                        //sim num
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_SIM_NUM, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(simNo, true)
                        //sim provider
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_SIM_PROVIDER, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(simProvider, true)
                        .addText(versionInfo)
                        .setMarginBottom(MARGIN_BOTTOM);

                Handler handler = new Handler(context.getMainLooper());

                try {
                    Bitmap receiptImage = builder.build();

                    if (bankLogo != null)
                        bankLogo.recycle();

                    handler.post(() -> listener.onReceived(receiptImage));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onGenerateReceiptFailed);
                }
            }
        }.start();
    }

    /**
     * generate summary report
     *
     * @param h
     * @param m
     * @param t
     * @param c
     * @param txns
     * @param listener
     */
    public void generateSummaryReport(Host h, Merchant m, Terminal t, Currency c, List<Transaction> txns, GenerateReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                Bitmap bankLogo = getBankLogo("img/boc_680.gif");

                String addressLine1 = !TextUtils.isEmpty(m.getRctHdr1()) ? m.getRctHdr1().toUpperCase() : "";
                String addressLine2 = !TextUtils.isEmpty(m.getRctHdr2()) ? m.getRctHdr2().toUpperCase() : "";
                String addressLine3 = !TextUtils.isEmpty(m.getRctHdr3()) ? m.getRctHdr3().toUpperCase() : "";
                String batchNo = Utility.padLeftZeros(String.valueOf(m.getBatchNumber()), 6);
                //date time
                String dateTime = "";
                try {
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    dateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH).format(new Date());
                    dateTime = dateTime.toUpperCase();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(addressLine1)
                        .addText(addressLine2)
                        .addText(addressLine3)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        .setTextSize(TEXT_XXSMALL)
                        //date time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(dateTime, true);
                        if(m.getMerchantID().length()<16) {
                            //mid
                        builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_MERCHANT_ID, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(m.getMerchantID(), true);
                        }
                        else {
                            //mid
                        builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_MERCHANT_ID, true)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(m.getMerchantID(), true);
                        }
                        //tid
                        builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(t.getTerminalID(), true)
                        //batch no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_BATCH_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(batchNo, true)
                        //invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_HOST, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(h.getHostName(), true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)

                        //settlement
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_SUMMARY_REPORT.toUpperCase())
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true);

                //count sales & void sales
                int saleCount = 0;
                long saleAmount = 0;

                int saleManualCount = 0;
                long saleManualAmount = 0;

                int saleOfflineCount = 0;
                long saleOfflineAmount = 0;

                int saleOfflineManualCount = 0;
                long saleOfflineManualAmount = 0;

                int salePreCopmCount = 0;
                long salePreCopmAmount = 0;

                int saleInstallmentCount = 0;
                long saleInstallmentAmount = 0;

                int voidSaleCount = 0;
                long voidSaleAmount = 0;

                int refundSaleCount = 0;
                long refundSaleAmount = 0;

                int refundManualSaleCount = 0;
                long refundManualSaleAmount = 0;

                int cashBackSaleCount = 0;
                long cashBackSaleAmount = 0;

                int cashAdvanceSaleCount = 0;
                long cashAdvanceSaleAmount = 0;

                int quasiCashSaleCount = 0;
                long quasiCashSaleAmount = 0;

                int quasiCashManualSaleCount = 0;
                long quasiCashManualSaleAmount = 0;

                int qrSaleCount = 0;
                long qrSaleAmount = 0;


                for (Transaction transaction : txns) {
                    int saleOrVoidSale = transaction.getVoided();
                    final int SALED = 0;
                    final int VOID_SALE = 1;
                    switch (saleOrVoidSale) {
                        case SALED: {
                            if (transaction.getTransaction_code() == SALE) {
                                saleCount++;
                                saleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_MANUAL) {
                                saleManualCount++;
                                saleManualAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_OFFLINE) {
                                saleOfflineCount++;
                                saleOfflineAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_OFFLINE_MANUAL) {
                                saleOfflineManualCount++;
                                saleOfflineManualAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_PRE_COMPLETION) {
                                salePreCopmCount++;
                                salePreCopmAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_INSTALLMENT) {
                                saleInstallmentCount++;
                                saleInstallmentAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_REFUND) {
                                refundSaleCount++;
                                refundSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_REFUND_MANUAL) {
                                refundManualSaleCount++;
                                refundManualSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == CASH_BACK) {
                                cashBackSaleCount++;
                                cashBackSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == CASH_ADVANCE) {
                                cashAdvanceSaleCount++;
                                cashAdvanceSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == QUASI_CASH) {
                                quasiCashSaleCount++;
                                quasiCashSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == QUASI_CASH_MANUAL) {
                                quasiCashManualSaleCount++;
                                quasiCashManualSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            }
                            else if (transaction.getTransaction_code() == QR_SALE) {
                                qrSaleCount++;
                                qrSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            }
                            break;
                        }
                        case VOID_SALE: {
                            voidSaleCount++;
                            voidSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            break;
                        }
                    }
                }

                int totalTxns = saleCount + saleManualCount + saleOfflineCount
                        + saleOfflineManualCount + salePreCopmCount + saleInstallmentCount
                        + voidSaleCount + refundSaleCount + refundManualSaleCount
                        + cashBackSaleCount + cashAdvanceSaleCount + quasiCashSaleCount
                        + quasiCashManualSaleCount +qrSaleCount;

                long totalAmount = (saleAmount + saleManualAmount + saleOfflineAmount
                        + saleOfflineManualAmount + salePreCopmAmount + saleInstallmentAmount
                        + cashBackSaleAmount + cashAdvanceSaleAmount + quasiCashSaleAmount
                        + quasiCashManualSaleAmount+ qrSaleAmount)
                        - (refundSaleAmount + refundManualSaleAmount);

                builder //column titles
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TRANSACTION, false)
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_COUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(Const.RECEIPT_TOTAL + "(" + c.getCurrencySymbol() + ")", true)
                        .setTypeface(context, getFontRegular())
                        .addText("", true);
                        //values

                            //SALE
                if(saleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_SALE, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(saleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(saleAmount), true);
                }
                                    //SALE_OFFLINE
                if(saleOfflineCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_OFFLINE_SALE, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(saleOfflineCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(saleOfflineAmount), true);
                }
                                    //SALE_MANUAL
                if(saleManualCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_MANUAL_SALE, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(saleManualCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(saleManualAmount), true);
                }
                                    //SALE_OFFLINE_MANUAL
                if(saleOfflineManualCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_OFFLINE_MANUAL_SALE, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(saleOfflineManualCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(saleOfflineManualAmount), true);
                }
                                    //SALE_PRE_COMPLETION
                if(salePreCopmCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_COMPLETION, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(salePreCopmCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(salePreCopmAmount), true);
                }
                                    //SALE_INSTALLMENT
                if(saleInstallmentCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_INSTALLMENT, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(saleInstallmentCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(saleInstallmentAmount), true);
                }
                                    //SALE_CASHBACK
                if(cashBackSaleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_CASH_BACK, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(cashBackSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(cashBackSaleAmount), true);
                }
                                    //SALE_REFUND
                if(refundSaleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_REFUND, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(refundSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount((-1) * refundSaleAmount), true);
                }
                                    //SALE_REFUND_MANUAL
                if(refundManualSaleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_REFUND_MANUAL, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(refundManualSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount((-1) * refundManualSaleAmount), true);
                }
                                    //SALE_CASH_ADVANCE
                if(cashAdvanceSaleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_CASH_ADVANCE, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(cashAdvanceSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(cashAdvanceSaleAmount), true);
                }
                                    //SALE_QUASI_CASH
                if(quasiCashSaleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_QUASI_CASH, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(quasiCashSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(quasiCashSaleAmount), true);
                }
                                    //SALE_QUASI_CASH_MANUAL
                if(quasiCashManualSaleCount>0) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_QUASI_CASH_MANUAL, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(quasiCashManualSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount(quasiCashManualSaleAmount), true);
                }
                if(voidSaleCount>0) {
                    //void sales
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_VOID_SALE, false)
                            .setAlign(Paint.Align.CENTER)
                            .addText(String.valueOf(voidSaleCount), false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(Utility.getFormattedAmount((-1) * voidSaleAmount), true);
                }

                if(qrSaleCount>0){
                              builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_QR_SALE, false)
                                    .setAlign(Paint.Align.CENTER)
                                    .addText(String.valueOf(qrSaleCount), false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(Utility.getFormattedAmount(qrSaleAmount), true);
                }

                        //line
                        builder.setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(LINE, true)
                        .addText("", true)
                        //label
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TOTALS, false)
                        //total count
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(String.valueOf(totalTxns), false)
                        //total amount
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.RIGHT)
                        .addText(Utility.getFormattedAmount(totalAmount), false)
                        //line
                        .addText("", true)
                        .setTextSize(TEXT_XXXSMALL)
                        .setAlign(Paint.Align.CENTER)
                        .addText(LINE, true)
                        .addText(versionInfo)
                        .setMarginBottom(MARGIN_BOTTOM);

                Handler handler = new Handler(context.getMainLooper());

                try {
                    Bitmap receiptImage = builder.build();

                    if (bankLogo != null)
                        bankLogo.recycle();

                    handler.post(() -> listener.onReceived(receiptImage));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onGenerateReceiptFailed);
                }
            }
        }.start();
    }

    /**
     * Generate and save sale receipt
     *
     * @param receipt
     * @param listener
     */
    public void generateSettlementReceipt(SettlementReceipt receipt, ReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                Bitmap bankLogo = getBankLogo(receipt.getBankLogo());

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(receipt.getAddressLine1())
                        .addText(receipt.getAddressLine2())
                        .addText(receipt.getAddressLine3())
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        .setTextSize(TEXT_XXSMALL)
                        //date time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getDateTime(), true)
                        //mid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MERCHANT_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getMerchantId(), true)
                        //tid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getTerminalId(), true)
                        //batch no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_BATCH_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(ValidatorUtil.getInstance().zeroPadString(receipt.getBatchNo(), 6), true)
                        //invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_HOST, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getHost(), true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)

                        //settlement
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_SETTLEMENT)
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true);

                        /*//Card Name
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_CARD_NAME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getCardName(), true)
                        //Card No.
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_CARD_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getCardNo(), true)
                        //Expire Date
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_EXP_DATE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getExpireDate(), true)
                        //Invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_INVOICE_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getInvoiceNo(), true)
                        //Transaction Date
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TRANSACTION_DATE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getTransactionDate(), true)
                        //Transaction Time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TRANSACTION_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getTransactionTime(), true)
                        //Transaction
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TRANSACTION, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getTransaction(), true)
                        //Amount
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_AMOUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getAmount(), true)
                        //Approve Code
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_APPROVE_CODE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getApproveCode(), true)
                        //Username
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_USERNAME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getUsername(), true);*/

                List<Transaction> transactionList = receipt.getTransactionList();
                //count sales & void sales
                int saleCount = 0;
                long saleAmount = 0;

                int saleManualCount = 0;
                long saleManualAmount = 0;

                int saleOfflineCount = 0;
                long saleOfflineAmount = 0;

                int saleOfflineManualCount = 0;
                long saleOfflineManualAmount = 0;

                int salePreCopmCount = 0;
                long salePreCopmAmount = 0;

                int saleInstallmentCount = 0;
                long saleInstallmentAmount = 0;

                int refundSaleCount = 0;
                long refundSaleAmount = 0;

                int refundManualSaleCount = 0;
                long refundManualSaleAmount = 0;

                int cashBackSaleCount = 0;
                long cashBackSaleAmount = 0;

                int cashAdvanceSaleCount = 0;
                long cashAdvanceSaleAmount = 0;

                int quasiCashSaleCount = 0;
                long quasiCashSaleAmount = 0;

                int quasiCashManualSaleCount = 0;
                long quasiCashManualSaleAmount = 0;

                int qrSaleCount = 0;
                long qrSaleAmount = 0;


                int voidSaleCount = 0;
                long voidSaleAmount = 0;

                for (Transaction transaction : transactionList) {
                    int saleOrVoidSale = transaction.getVoided();
                    final int SALED = 0;
                    final int VOID_SALE = 1;
                    switch (saleOrVoidSale) {
                        case SALED: {
                            if (transaction.getTransaction_code() == SALE) {
                                saleCount++;
                                saleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_MANUAL) {
                                saleManualCount++;
                                saleManualAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_OFFLINE) {
                                saleOfflineCount++;
                                saleOfflineAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_OFFLINE_MANUAL) {
                                saleOfflineManualCount++;
                                saleOfflineManualAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_PRE_COMPLETION) {
                                salePreCopmCount++;
                                salePreCopmAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_INSTALLMENT) {
                                saleInstallmentCount++;
                                saleInstallmentAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == CASH_BACK) {
                                cashBackSaleCount++;
                                cashBackSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_REFUND) {
                                refundSaleCount++;
                                refundSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == SALE_REFUND_MANUAL) {
                                refundManualSaleCount++;
                                refundManualSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == CASH_ADVANCE) {
                                cashAdvanceSaleCount++;
                                cashAdvanceSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == QUASI_CASH) {
                                quasiCashSaleCount++;
                                quasiCashSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            } else if (transaction.getTransaction_code() == QUASI_CASH_MANUAL) {
                                quasiCashManualSaleCount++;
                                quasiCashManualSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            }
                            else if (transaction.getTransaction_code() == QR_SALE) {
                                qrSaleCount++;
                                qrSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            }

                            break;
                        }
                        case VOID_SALE: {
                            voidSaleCount++;
                            voidSaleAmount += Long.parseLong(transaction.getTotal_amount());
                            break;
                        }
                    }
                }

                builder //column titles
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TRANSACTION, false)
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_COUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(Const.RECEIPT_TOTAL + "(" + receipt.getCurrency() + ")", true)
                        .setTypeface(context, getFontRegular())
                        .addText("", true);

                        //values
                            //SALE
                            if(saleCount>0){
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_SALE, false)
                                    .setAlign(Paint.Align.CENTER)
                                    .addText(String.valueOf(saleCount), false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(Utility.getFormattedAmount(saleAmount), true);
                            }
                                    //SALE_OFFLINE
                            if(saleOfflineCount>0) {
                            builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_OFFLINE_SALE, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(saleOfflineCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(saleOfflineAmount), true);
                            }
                                    //SALE_CASHBACK
                            if(cashBackSaleCount>0) {
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_CASH_BACK, false)
                                    .setAlign(Paint.Align.CENTER)
                                    .addText(String.valueOf(cashBackSaleCount), false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(Utility.getFormattedAmount(cashBackSaleAmount), true);
                            }
                                    //SALE_MANUAL
                            if(saleManualCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_MANUAL_SALE, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(saleManualCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(saleManualAmount), true);
                            }
                                //SALE_OFFLINE_MANUAL
                            if(saleOfflineManualCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_OFFLINE_MANUAL_SALE, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(saleOfflineManualCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(saleOfflineManualAmount), true);
                            }
                                    //SALE_PRE_COMPLETION
                           if(salePreCopmCount>0) {
                               builder.setAlign(Paint.Align.LEFT)
                                       .addText(Const.RECEIPT_COMPLETION, false)
                                       .setAlign(Paint.Align.CENTER)
                                       .addText(String.valueOf(salePreCopmCount), false)
                                       .setAlign(Paint.Align.RIGHT)
                                       .addText(Utility.getFormattedAmount(salePreCopmAmount), true);
                           }
                                    //SALE_INSTALLMENT
                            if(saleInstallmentCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_INSTALLMENT, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(saleInstallmentCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(saleInstallmentAmount), true);
                            }
                                    //SALE_REFUND
                            if(refundSaleCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_REFUND, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(refundSaleCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount((-1) * refundSaleAmount), true);
                            }
                                    //SALE_REFUND_MANUAL
                            if(refundManualSaleCount>0) {
                            builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_REFUND_MANUAL, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(refundManualSaleCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount((-1) * refundManualSaleAmount), true);
                            }
                                    //SALE_CASH_ADVANCE
                            if(cashAdvanceSaleCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_CASH_ADVANCE, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(cashAdvanceSaleCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(cashAdvanceSaleAmount), true);
                            }
                                    //SALE_QUASI_CASH
                            if(quasiCashSaleCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_QUASI_CASH, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(quasiCashSaleCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(quasiCashSaleAmount), true);
                            }
                                    //SALE_QUASI_CASH_MANUAL
                            if(quasiCashManualSaleCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_QUASI_CASH_MANUAL, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(quasiCashManualSaleCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount(quasiCashManualSaleAmount), true);
                            }
                                    //void sales
                            if(voidSaleCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                        .addText(Const.RECEIPT_VOID_SALE, false)
                                        .setAlign(Paint.Align.CENTER)
                                        .addText(String.valueOf(voidSaleCount), false)
                                        .setAlign(Paint.Align.RIGHT)
                                        .addText(Utility.getFormattedAmount((-1) * voidSaleAmount), true);
                            }

                                    //QR sales
                            if(qrSaleCount>0) {
                                builder.setAlign(Paint.Align.LEFT)
                                .addText(Const.RECEIPT_QR_SALE, false)
                                .setAlign(Paint.Align.CENTER)
                                .addText(String.valueOf(qrSaleCount), false)
                                .setAlign(Paint.Align.RIGHT)
                                .addText(Utility.getFormattedAmount(qrSaleAmount), true);
                            }


                //totals
                int totalTxn = saleCount + saleOfflineCount + saleOfflineManualCount + salePreCopmCount + saleInstallmentCount + saleManualCount
                        + cashBackSaleCount + refundManualSaleCount + refundSaleCount + quasiCashManualSaleCount + quasiCashSaleCount + cashAdvanceSaleCount + voidSaleCount +qrSaleCount;

                long totalAmount = saleAmount + saleOfflineAmount + saleOfflineManualAmount + salePreCopmAmount + saleInstallmentAmount + saleManualAmount +
                        cashBackSaleAmount + quasiCashManualSaleAmount + quasiCashSaleAmount + cashAdvanceSaleAmount +qrSaleAmount
                        - (refundSaleAmount + refundManualSaleAmount);
                builder//line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(LINE, true)
                        .addText("", true)

                        //label
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TOTALS, false)
                        //total count
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(String.valueOf(totalTxn), false)
                        //total amount
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.RIGHT)
                        .addText(Utility.getFormattedAmount(totalAmount), false)

                        //line
                        .addText("", true)
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText(LINE, true)
                        //total count
                        .addText("", true)
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(RECEIPT_SETTLEMENT_SUCCESS, false)
                        //line
                        .addText("", true)
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText(LINE, true);

                /*builder.setTextSize(TEXT_XXXSMALL)
                        .addText("", true)
                        .setTypeface(context, getFontRegular())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_TERMS_TXT_1)
                        .addText(Const.RECEIPT_TERMS_TXT_2);*/



                builder.setTextSize(TEXT_XXSMALL).addText(versionInfo).setMarginBottom(MARGIN_BOTTOM);

                Bitmap receiptImage = builder.build();

                if (bankLogo != null)
                    bankLogo.recycle();

                Handler handler = new Handler(context.getMainLooper());

                try {
                    ImageUtils.getInstance().saveSettlementReceipt(receipt.getHost(), receipt.getMerchantId(), receiptImage);

                    receiptImage.recycle();
                    handler.post(listener::onReceiptGenerated);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onReceiptGenerationFailed);
                }
            }
        }.start();
    }

    /**
     * Transaction cancel receipt
     *
     * @param m
     * @param issuer
     * @param listener
     */
    public void generateCancelTxnReceipt(Reversal r, Merchant m, Issuer issuer, CancelReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                Bitmap bankLogo = getBankLogo("img/boc_680.gif");

                String addressLine1 = !TextUtils.isEmpty(m.getRctHdr1()) ? m.getRctHdr1().toUpperCase() : "";
                String addressLine2 = !TextUtils.isEmpty(m.getRctHdr2()) ? m.getRctHdr2().toUpperCase() : "";
                String addressLine3 = !TextUtils.isEmpty(m.getRctHdr3()) ? m.getRctHdr3().toUpperCase() : "";
                String batchNo = Utility.padLeftZeros(String.valueOf(m.getBatchNumber()), 6);
                //date time
                String dateTime = "";
                try {
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);

                    Date txnDateTime = new SimpleDateFormat(Const.TXN_DATE_TIME, Locale.ENGLISH)
                            .parse(year + r.getTxn_date() + " " + r.getTxn_time());
                    dateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH)
                            .format(txnDateTime);
                    dateTime = dateTime.toUpperCase();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //title
                String title = TranTypes.toTitle(r.getTransaction_code(), false);

                //mask format and pos entry mode
                String posEntryMode = com.epic.pos.iso.modal.Transaction.posEntryModeToString(String.valueOf(r.getChip_status()));
                String maskingFormat = issuer.getMaskMerchantCopy();
                if (r.getPan().length() != 16) {
                    maskingFormat = Utility.getMaskingFormat(r.getPan());
                }
                String cardNo = Utility.maskCardNumber(r.getPan(), maskingFormat) + " " + posEntryMode.toUpperCase();
                String expDate = Utility.maskCardNumber(r.getExp_date(), issuer.getMaskExpireDate());
                String cardLabel = !TextUtils.isEmpty(r.getCard_label()) ? r.getCard_label().toUpperCase() : "";
                String approvalCode = !TextUtils.isEmpty(r.getApprove_code()) ? r.getApprove_code().toUpperCase() : "";
                String refNo = !TextUtils.isEmpty(r.getRrn()) ? r.getRrn() : "";
                String amount = r.getCurrency_symbol() + " " + Utility.getFormattedAmount(Long.parseLong(r.getBase_transaction_amount()));

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(addressLine1)
                        .addText(addressLine2)
                        .addText(addressLine3)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        .setTextSize(TEXT_XXSMALL)
                        //date time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(dateTime, true)
                        //mid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MERCHANT_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(r.getMerchant_id(), true)
                        //tid
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(r.getTerminal_id(), true)
                        //batch no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_BATCH_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(batchNo, true)
                        //invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_INVOICE_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(r.getInvoice_no(), true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        //sale
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(title)
                        .addText("TRANSACTION CANCELLED")
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        //card no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_CARD_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(cardNo, true)
                        //exp date
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_EXP_DATE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(expDate, true)
                        //card type
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_CARD_TYPE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(cardLabel, true);

                //appr code
                builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_APPR_CODE, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(approvalCode, true)
                        //ref no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_REF_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(refNo, true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine()
                        //amount
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_AMOUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(amount, true)
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine()
                        .setTextSize(TEXT_MEDIUM)
                        .setAlign(Paint.Align.CENTER)
                        .addBlankSpace(70)
                        .setTextSize(TEXT_XXXSMALL)
                        .addText("", true)
                        .setTypeface(context, getFontRegular())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_TERMS_TXT_1)
                        .addText(Const.RECEIPT_TERMS_TXT_2)
                        .addBlankSpace(30)
                        .addText(Const.CUSTOMER_COPY)
                         .addText(Const.THANK_YOU)
                        .addText(versionInfo)
                        .setMarginBottom(MARGIN_BOTTOM);

                try {
                    Bitmap receiptImage = builder.build();
                    listener.onReceiptGenerated(receiptImage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listener.onReceiptFailed();
                }
            }
        }.start();
    }

    /**
     * Generate txn duplicate receipt
     * s
     *
     * @param txn
     * @param m
     * @param issuer
     * @param listener
     */
    public void generateTxnDuplicateReceipt(Transaction txn, Merchant m, Issuer issuer,Boolean ismerchantCopy, DuplicateReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                Bitmap bankLogo = getBankLogo("img/boc_680.gif");

                String addressLine1 = !TextUtils.isEmpty(m.getRctHdr1()) ? m.getRctHdr1().toUpperCase() : "";
                String addressLine2 = !TextUtils.isEmpty(m.getRctHdr2()) ? m.getRctHdr2().toUpperCase() : "";
                String addressLine3 = !TextUtils.isEmpty(m.getRctHdr3()) ? m.getRctHdr3().toUpperCase() : "";
                String batchNo = Utility.padLeftZeros(String.valueOf(m.getBatchNumber()), 6);
                //date time
                String dateTime = "";
                try {
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);

                    Date txnDateTime = new SimpleDateFormat(Const.TXN_DATE_TIME, Locale.ENGLISH)
                            .parse(year + txn.getTxn_date() + " " + txn.getTxn_time());
                    dateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH)
                            .format(txnDateTime);
                    dateTime = dateTime.toUpperCase();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //title
                String title = TranTypes.toTitle(txn.getTransaction_code(), (txn.getVoided() == 1));
                String cardNo="";
                String expDate="";
                String cardLabel="";
                if(issuer.getIssuerNumber()!=8){
                //mask format and pos entry mode
                String posEntryMode = com.epic.pos.iso.modal.Transaction.posEntryModeToString(String.valueOf(txn.getChip_status()));
                String maskingFormat = issuer.getMaskMerchantCopy();
                if (txn.getPan().length() != 16) {
                    maskingFormat = Utility.getMaskingFormat(txn.getPan());
                }
                cardNo = Utility.maskCardNumber(txn.getPan(), maskingFormat) + " " + posEntryMode.toUpperCase();
                expDate = Utility.maskCardNumber(txn.getExp_date(), issuer.getMaskExpireDate());
                cardLabel = !TextUtils.isEmpty(txn.getCard_label()) ? txn.getCard_label().toUpperCase() : "";
                }
                String approvalCode = !TextUtils.isEmpty(txn.getApprove_code()) ? txn.getApprove_code().toUpperCase() : "";
                String refNo = !TextUtils.isEmpty(txn.getRrn()) ? txn.getRrn() : "";
                String amount = "";

                if (txn.getVoided() == 1) {
                    amount = txn.getCurrency_symbol() + " -" + Utility.getFormattedAmount(Long.parseLong(txn.getBase_transaction_amount()));
                } else {
                    amount = txn.getCurrency_symbol() + " " + Utility.getFormattedAmount(Long.parseLong(txn.getBase_transaction_amount()));
                }

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(addressLine1)
                        .addText(addressLine2)
                        .addText(addressLine3)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        .setTextSize(TEXT_XXSMALL)
                        //date time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(dateTime, true);

                        if(txn.getMerchant_id().length()>15) {
                            //mid
                         builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MERCHANT_ID, true)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(txn.getMerchant_id(), true);
                        }
                        else{
                            //mid
                        builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_MERCHANT_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(txn.getMerchant_id(), true);
                        }
                        //tid
                         builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(txn.getTerminal_id(), true)
                        //batch no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_BATCH_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(batchNo, true)
                        //invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_INVOICE_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(txn.getInvoice_no(), true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        //sale
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(RECEIPT_DUPLICATE)
                        .addText(title)
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true);

                        if(issuer.getIssuerNumber()!=8) {
                            //card no
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_CARD_NO, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(cardNo, true)
                                    //exp date
                                    .setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_EXP_DATE, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(expDate, true)
                                    //card type
                                    .setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_CARD_TYPE, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(cardLabel, true);
                            //appr code
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_APPR_CODE, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(approvalCode, true)
                                    //ref no
                                    .setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_REF_NO, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(refNo, true);
                        }else{
                            //PAy Type
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText("PAY METHOD", false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText("QR", true)
                                    .setAlign(Paint.Align.LEFT)
                                    .addText("QR TYPE", false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText("DYNAMIC", true);
                            //card number holder name
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText("ACC / Card No", false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(txn.getPan(), true)
                                    .setAlign(Paint.Align.LEFT)
                                    .addText("CARD HOLDER", false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(txn.getCard_holder_name(), true);

                            //appr code
                            builder.setAlign(Paint.Align.LEFT)
                                    .addText("TRACE", false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(txn.getApprove_code(), true)
                                    //ref no
                                    .setAlign(Paint.Align.LEFT)
                                    .addText(Const.RECEIPT_REF_NO, false)
                                    .setAlign(Paint.Align.RIGHT)
                                    .addText(txn.getRrn(), true);

                        }




                //todo - AID needs to add transaction and reversal table for report purposes. Need to be fixed in report page duplication txn receipt.



                //student ref no
                if (!TextUtils.isEmpty(txn.getStd_ref_no())) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_STD_REF_NO, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(txn.getStd_ref_no(), true);
                }

                //line
                builder.setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine()
                        //amount
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_AMOUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(amount, true)
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine()
                        .setTextSize(TEXT_MEDIUM)
                        .setAlign(Paint.Align.CENTER)
                        .addBlankSpace(70)
                        .addText(Const.RECEIPT_SIG_NOT_REQUIRED)
                        .addBlankSpace(30)
                        .setTextSize(TEXT_XXXSMALL)
                        .addText("", true)
                        .setTypeface(context, getFontRegular())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_TERMS_TXT_1)
                        .addText(Const.RECEIPT_TERMS_TXT_2)
                        .addBlankSpace(30);
                        if(ismerchantCopy){
                            builder.addText(Const.MERCHANT_COPY);}
                        else{
                            builder.addText(Const.CUSTOMER_COPY);}
                        builder.addText(Const.THANK_YOU)
                        .addText(versionInfo)
                        .setMarginBottom(MARGIN_BOTTOM);

                try {
                    Bitmap receiptImage = builder.build();
                    listener.onReceiptGenerated(receiptImage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listener.onReceiptFailed();
                }
            }
        }.start();
    }

    /**
     * Generate and save sale receipt
     *
     * @param receipt
     * @param listener
     */
    public void generateSaleReceipt(SaleReceipt receipt, ReceiptListener listener) {
        new Thread() {
            @Override
            public void run() {
                AppLog.i(TAG, receipt.toString());

                Bitmap bankLogo = getBankLogo(receipt.getBankLogo());
                Bitmap signature = null;

                String saleTitle = Const.RECEIPT_SALE;

                if (receipt.isOfflineSale()) {
                    saleTitle = Const.RECEIPT_OFFLINE_SALE;
                } else if (receipt.isPreAuth()) {
                    saleTitle = Const.RECEIPT_PRE_AUTH;
                } else if (receipt.isAuthOnly()) {
                    saleTitle = Const.RECEIPT_AUTH_ONLY;
                }else if (receipt.isInstallment()) {
                    saleTitle = Const.RECEIPT_INSTALLMENT;
                } else if (receipt.isPreComp()) {
                    saleTitle = Const.RECEIPT_COMPLETION;
                } else if (receipt.isRefund()) {
                    saleTitle = Const.RECEIPT_REFUND;
                } else if (receipt.isCashBack()) {
                    saleTitle = Const.RECEIPT_CASH_BACK;
                } else if (receipt.isQuasiCash()) {
                    saleTitle = Const.RECEIPT_QUASI_CASH;
                } else if (receipt.isCashAdvance()) {
                    saleTitle = Const.RECEIPT_CASH_ADVANCE;
                }
                else if (receipt.isQrSale()) {
                    saleTitle = Const.RECEIPT_QR_SALE;
                }

                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
                builder.setMarginTop(MARGIN_TOP)
                        .setMarginBottom(MARGIN_BOTTOM)
                        .addImage(bankLogo)
                        //address
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setAlign(Paint.Align.CENTER)
                        .addText(receipt.getAddressLine1())
                        .addText(receipt.getAddressLine2())
                        .addText(receipt.getAddressLine3())
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        .setTextSize(TEXT_XXSMALL)
                        //date time
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_DATE_TIME, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getDateTime(), true);

                        if(receipt.getMerchantId().length()>15) {
                            //mid
                            builder.setAlign(Paint.Align.LEFT)
                                .addText(Const.RECEIPT_MERCHANT_ID, true)
                                .setAlign(Paint.Align.RIGHT)
                                .addText(receipt.getMerchantId(), true);

                        }
                        else{
                            //mid
                builder.setAlign(Paint.Align.LEFT)
                                .addText(Const.RECEIPT_MERCHANT_ID, false)
                                .setAlign(Paint.Align.RIGHT)
                                .addText(receipt.getMerchantId(), true);
                        }
                //tid
                builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_TERMINAL_ID, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getTerminalId(), true);

                        //batch no
                builder.setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_BATCH_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getBatchNo(), true)
                        //invoice no
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_INVOICE_NO, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getInvoiceNo(), true)
                        //line
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addText(DASH_LINE, true)
                        .addText("", true)
                        //sale
                        .setTextSize(TEXT_MEDIUM)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.CENTER)
                        .addText(saleTitle)
                        .setTypeface(context, getFontRegular())
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true);


                if(!receipt.isQrSale()) {
                    //card no
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_CARD_NO, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getCardNo(), true)
                             .setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_EXP_DATE, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getExpireDate(), true)
                            //card type
                            .setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_CARD_TYPE, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getCardType(), true);


                    //aid
                    if (!TextUtils.isEmpty(receipt.getAid())) {
                        builder.setAlign(Paint.Align.RIGHT)
                                .addText(receipt.getAid(), true);
                    }
                    //appr code
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_APPR_CODE, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getApprCode(), true)
                            //ref no
                            .setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_REF_NO, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getRefNo(), true);

                }else{
                    //PAy Type
                    builder.setAlign(Paint.Align.LEFT)
                            .addText("PAY METHOD", false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText("QR", true)
                            .setAlign(Paint.Align.LEFT)
                            .addText("QR TYPE", false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText("DYNAMIC", true);
                    //card number holder name
                    builder.setAlign(Paint.Align.LEFT)
                            .addText("ACC / Card No", false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getCardNo(), true)
                            .setAlign(Paint.Align.LEFT)
                            .addText("CARD HOLDER", false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getCardHolderName(), true);

                    //appr code
                    builder.setAlign(Paint.Align.LEFT)
                            .addText("TRACE", false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getApprCode(), true)
                    //ref no
                            .setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_REF_NO, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getRefNo(), true);
                }



                //student reference
                if (!TextUtils.isEmpty(receipt.getStudentRefNo())) {
                    builder.setAlign(Paint.Align.LEFT)
                            .addText(Const.RECEIPT_STD_REF_NO, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getStudentRefNo(), true);
                }

                int amountFontSize = TEXT_MEDIUM;

                if (receipt.getTotalAmount().length() >= 16) {
                    amountFontSize = TEXT_XSMALL;
                } else if (receipt.getTotalAmount().length() >= 13) {
                    amountFontSize = TEXT_SMALL;
                }

                if (receipt.isCashBack()) {
                    //dot line
                    builder.setTextSize(TEXT_LINE_SIZE)
                            .addText("", true)
                            .addText(DASH_LINE, true)
                            .addText("", true)
                            //base amount
                            .setTextSize(amountFontSize)
                            .setTypeface(context, getFontBold())
                            .setAlign(Paint.Align.LEFT)
                            .addText(Const.BASE_AMOUNT, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getCurrency() + " " + receipt.getBaseAmount(), true)
                            //cash back
                            .setAlign(Paint.Align.LEFT)
                            .addText(Const.CASH_BACK, false)
                            .setAlign(Paint.Align.RIGHT)
                            .addText(receipt.getCurrency() + " " + receipt.getCashBackAmount(), true);
                }


                //line and total amount
                builder.setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine()
                        .setTextSize(TEXT_XXSMALL)
                        .addText("", true)
                        .setTextSize(amountFontSize)
                        .setTypeface(context, getFontBold())
                        .setAlign(Paint.Align.LEFT)
                        .addText(Const.RECEIPT_AMOUNT, false)
                        .setAlign(Paint.Align.RIGHT)
                        .addText(receipt.getCurrency() + " " + receipt.getTotalAmount(), true)
                        .setTextSize(TEXT_LINE_SIZE)
                        .addText("", true)
                        .addLine();

                if (receipt.getSignatureType() == SaleReceipt.SignatureType.SIGNATURE) {
                    signature = ImageUtils.getInstance().getSignature(receipt.getMerchantNo(), receipt.getInvoiceNo());

                    builder.setTextSize(TEXT_XXSMALL)
                            .addText("", true)
                            .setTypeface(context, getFontRegular())
                            .setAlign(Paint.Align.CENTER)
                            .addText(Const.RECEIPT_SIGNATURE)
                            .addImage(signature)
                            .setTextSize(TEXT_LINE_SIZE)
                            .addText(DOT_SIGNATURE_LINE, true)
                            .setTextSize(TEXT_XXSMALL)
                            .setTypeface(context, getFontRegular())
                            .setAlign(Paint.Align.CENTER)
                            .addText(receipt.getCardHolderName(), true);
                } else if (receipt.getSignatureType() == SaleReceipt.SignatureType.SIGNATURE_NOT_REQUIRED) {
                    builder.setTextSize(TEXT_MEDIUM)
                            .setAlign(Paint.Align.CENTER)
                            .addBlankSpace(70)
                            .addText(Const.RECEIPT_SIG_NOT_REQUIRED)
                            .addBlankSpace(30);
                } else if (receipt.getSignatureType() == SaleReceipt.SignatureType.PIN_VERIFIED) {
                    builder.setTextSize(TEXT_MEDIUM)
                            .setAlign(Paint.Align.CENTER)
                            .addBlankSpace(70)
                            .addText(Const.RECEIPT_PIN_VERIFIED)
                            .addBlankSpace(30);
                } else if (receipt.getSignatureType() == SaleReceipt.SignatureType.NO_CVM_REQUIRED) {
                    builder.setTextSize(TEXT_MEDIUM)
                            .setAlign(Paint.Align.CENTER)
                            .addBlankSpace(70)
                            .addText(Const.NO_CVM_REQUIRED)
                            .addBlankSpace(30);
                }

                builder.setTextSize(TEXT_XXXSMALL)
                        .addText("", true)
                        .setTypeface(context, getFontRegular())
                        .setAlign(Paint.Align.CENTER)
                        .addText(Const.RECEIPT_TERMS_TXT_1)
                        .addText(Const.RECEIPT_TERMS_TXT_2);


                if (receipt.isMerchantCopy()) {
                    builder.addBlankSpace(30)
                            .addText(Const.MERCHANT_COPY)
                            .addText(Const.THANK_YOU);
                }

                if (receipt.isCustomerCopy()) {
                    builder.addBlankSpace(30)
                            .addText(Const.CUSTOMER_COPY)
                            .addText(Const.THANK_YOU)
                            .addText(versionInfo)
                            .setMarginBottom(MARGIN_BOTTOM);
                }

                Handler handler = new Handler(context.getMainLooper());

                try {
                    Bitmap receiptImage = builder.build();

                    if (bankLogo != null)
                        bankLogo.recycle();
                    if (signature != null)
                        signature.recycle();

                    if (receipt.isCustomerCopy()) {
                        ImageUtils.getInstance().saveCustomerSaleReceipt(receiptImage, receipt.getMerchantNo(), receipt.getInvoiceNo());
                    }

                    if (receipt.isMerchantCopy()) {
                        ImageUtils.getInstance().saveMerchantSaleReceipt(receiptImage, receipt.getMerchantNo(), receipt.getInvoiceNo());
                    }

                    receiptImage.recycle();
                    handler.post(listener::onReceiptGenerated);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onReceiptGenerationFailed);
                }
            }
        }.start();
    }

    /**
     * Generate and save detail report receipt
     *
     * @param receipt
     * @param listener
     */
//    public void generateDetailReportReceipt(DetailReportReceipt receipt, ReceiptListener listener) {
//        new Thread() {
//            @Override
//            public void run() {
//                Bitmap bankLogo = getBankLogo(receipt.getBankLogo());
//
//                ReceiptBuilder builder = new ReceiptBuilder(RECEIPT_SIZE);
//                builder.setMarginTop(MARGIN_TOP)
//                        .setMarginBottom(MARGIN_BOTTOM)
//                        .addImage(bankLogo)
//                        //address
//                        .setTypeface(context, getFontRegular())
//                        .setTextSize(TEXT_XXSMALL)
//                        .addText("", true)
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(receipt.getAddressLine1())
//                        .addText(receipt.getAddressLine2())
//                        .addText(receipt.getAddressLine3())
//                        //line
//                        .setTextSize(TEXT_LINE_SIZE)
//                        .addText("", true)
//                        .addText(DASH_LINE, true)
//                        .addText("", true)
//                        .setTextSize(TEXT_XXSMALL)
//                        //date time
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_DATE_TIME, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(receipt.getDateTime(), true)
//                        //mid
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_MERCHANT_ID, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(receipt.getMerchantId(), true)
//                        //tid
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_TERMINAL_ID, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(receipt.getTerminalId(), true)
//                        //batch no
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_BATCH_NO, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(ValidatorUtil.getInstance().zeroPadString(receipt.getBatchNo(), 6), true)
//                        //invoice no
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_HOST, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(receipt.getHost(), true)
//                        //line
//                        .setTextSize(TEXT_LINE_SIZE)
//                        .addText("", true)
//                        .addText(DASH_LINE, true)
//                        .addText("", true)
//
//                        //Detail Report
//                        .setTextSize(TEXT_MEDIUM)
//                        .setTypeface(context, getFontBold())
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(Const.RECEIPT_DETAIL_REPORT)
//                        .setTypeface(context, getFontRegular())
//                        .setTextSize(TEXT_XXSMALL)
//
//
//                        //line
//                        .setTextSize(TEXT_LINE_SIZE)
//                        .addText("", true)
//                        .addText(DASH_LINE, true)
//                        .addText("", true)
//                        .setTextSize(TEXT_XXSMALL)
//
//                        //field names
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_CARD_NAME, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Const.RECEIPT_CARD_NO, true)
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_EXP_DATE, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Const.RECEIPT_INVOICE_NO, true)
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_TRANSACTION_DATE, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Const.RECEIPT_TRANSACTION_TIME, true)
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_TRANSACTION, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Const.RECEIPT_AMOUNT, true)
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_APPROVE_CODE, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Const.RECEIPT_USERNAME, true)
//
//                        //line
//                        .setTextSize(TEXT_LINE_SIZE)
//                        .addText("", true)
//                        .addText(DASH_LINE, true)
//                        .addText("", true)
//                        .setTextSize(TEXT_XXSMALL);
//
//                //field values
//                List<Transaction> transactionList = receipt.getTransactionList();
//                for (Transaction transaction : transactionList) {
//                    if (transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION)
//                        continue;
//                    //'void sale' or 'sale'
//                    String tranType = "";
//                    if (transaction.getVoided() == 1)
//                        tranType = Const.RECEIPT_VOID + " ";
//
//                    if (transaction.getTransaction_code() == SALE) {
//                        tranType = tranType + Const.RECEIPT_SALE;
//                    } else if (transaction.getTransaction_code() == SALE_OFFLINE) {
//                        tranType = tranType + Const.RECEIPT_OFFLINE_SALE;
//                    } else if (transaction.getTransaction_code() == SALE_MANUAL) {
//                        tranType = tranType + Const.RECEIPT_MANUAL_SALE;
//                    } else if (transaction.getTransaction_code() == SALE_OFFLINE_MANUAL) {
//                        tranType = tranType + Const.RECEIPT_OFFLINE_MANUAL_SALE;
//                    } else if (transaction.getTransaction_code() == SALE_PRE_COMPLETION) {
//                        tranType = tranType + Const.RECEIPT_PRE_COMP;
//                    } else if (transaction.getTransaction_code() == SALE_INSTALLMENT) {
//                        tranType = tranType + Const.RECEIPT_INSTALLMENT;
//                    }
//
//                    //to mask card no
//                    String mask = Utility.getMaskingFormat(transaction.getPan());
//
//                    builder //card name
//                            .setAlign(Paint.Align.LEFT)
//                            .setTextSize(TEXT_XSMALL)
//                            .addText(transaction.getCard_label(), false)
//
//                            //card no
//                            .setTextSize(TEXT_XXSMALL)
//                            .setAlign(Paint.Align.RIGHT)
//                            .addText(Utility.maskCardNumber(transaction.getPan(), mask), true)
//
//                            //exp date
//                            .setAlign(Paint.Align.LEFT)
////                            .addText(Utility.getFormattedDate(transaction.getExp_date()), false)
//                            .addText("**/**", false)
//
//                            //invoice no
//                            .setAlign(Paint.Align.RIGHT)
//                            .addText(transaction.getInvoice_no(), true)
//
//                            //transaction date
//                            .setAlign(Paint.Align.LEFT)
//                            .addText(Utility.getFormattedDate(transaction.getTxn_date()), false)
//
//                            //tran time
//                            .setAlign(Paint.Align.RIGHT)
//                            .addText(Utility.getFormattedTime(transaction.getTxn_time()), true)
//
//                            //tran type
//                            .setAlign(Paint.Align.LEFT)
//                            .setTextSize(TEXT_SMALL)
//                            .addText(tranType, false)
//
//                            //amount
//                            .setAlign(Paint.Align.RIGHT)
//                            .addText(Utility.getFormattedAmount(Long.parseLong(transaction.getBase_transaction_amount())) + " " + receipt.getCurrency(), true)
//
//                            //approve code
//                            .setTextSize(TEXT_XXSMALL)
//                            .setAlign(Paint.Align.LEFT)
//                            .addText(transaction.getApprove_code(), true)
//                            .addText("", true);
//                            /*//skip username//
//                            .setAlign(Paint.Align.RIGHT)
//                            .addText(transaction.getUsername, true);*/
//                }
//
//
//                //Summary
//                //count card types
//                int visaCount = 0;
//                int masterCount = 0;
//                int amexCount = 0;
//                long visaAmount = 0;
//                long masterAmount = 0;
//                long amexAmount = 0;
//                for (Transaction transaction : transactionList) {
//                    if (transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION)
//                        continue;
//
//                    String cardType = transaction.getCard_label();
//                    switch (cardType) {
//                        case Const.CARD_TYPE_STRING_VISA: {
//                            if (transaction.getVoided() == 0) {
//                                visaCount++;
//                                visaAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                        case Const.CARD_TYPE_STRING_MASTER: {
//                            if (transaction.getVoided() == 0) {
//                                masterCount++;
//                                masterAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                        case Const.CARD_TYPE_STRING_AMEX: {
//                            if (transaction.getVoided() == 0) {
//                                amexCount++;
//                                amexAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                    }
//                }
//
//                builder//line
//                        .setTextSize(TEXT_LINE_SIZE)
//                        .addText(DASH_LINE, true)
//                        .addText("", true)
//                        //title
//                        .setTextSize(TEXT_MEDIUM)
//                        .setTypeface(context, getFontBold())
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(Const.RECEIPT_SUMMARY_REPORT, true)
//                        .addText("", true)
//
//                        //column titles
//                        .setTextSize(TEXT_XXSMALL)
//                        .setTypeface(context, getFontBold())
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_TRANSACTION, false)
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(Const.RECEIPT_COUNT, false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Const.RECEIPT_TOTAL + "(" + receipt.getCurrency() + ")", true)
//                        .setTypeface(context, getFontRegular())
//                        .addText("", true)
//                        //values
//                        //visa
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.CARD_TYPE_STRING_VISA, false)
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(String.valueOf(visaCount), false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Utility.getFormattedAmount(visaAmount), true)
//                        //master
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.CARD_TYPE_STRING_MASTER, false)
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(String.valueOf(masterCount), false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Utility.getFormattedAmount(masterAmount), true)
//                        //amex
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.CARD_TYPE_STRING_AMEX, false)
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(String.valueOf(amexCount), false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Utility.getFormattedAmount(amexAmount), true)
//
//
//                        //line
//                        .setTypeface(context, getFontBold())
//                        .addText(LINE, true)
//                        .addText("", true)
//                        //totals
//                        .setTextSize(TEXT_SMALL)
//                        .setAlign(Paint.Align.LEFT)
//                        .addText(Const.RECEIPT_TOTALS, false)
//                        .setAlign(Paint.Align.CENTER)
//                        .addText(String.valueOf(visaCount + masterCount + amexCount), false)
//                        .setAlign(Paint.Align.RIGHT)
//                        .addText(Utility.getFormattedAmount(visaAmount + masterAmount + amexAmount), true)
//                        //line
//                        .addText(LINE)
//                        .addText(versionInfo)
//                        .setMarginBottom(MARGIN_BOTTOM);
//
//                Bitmap receiptImage = builder.build();
//
//                if (bankLogo != null)
//                    bankLogo.recycle();
//
//                Handler handler = new Handler(context.getMainLooper());
//
//                try {
//                    ImageUtils.getInstance().saveSettlementReceipt(receipt.getHost(), receipt.getMerchantId(), receiptImage);
//                    receiptImage.recycle();
//                    handler.post(listener::onReceiptGenerated);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    handler.post(listener::onReceiptGenerationFailed);
//                }
//            }
//        }.start();
//    }


    /**
     * generate summary report
     *
     * @param receipt
     * @param listener
     */
//    public void generateSummaryReportReceiptNew(DetailReportReceipt receipt, ReceiptBuilderListener listener) {
//        new Thread() {
//            @Override
//            public void run() {
//                List<PrintDataBuilder> printDataBuilders = new ArrayList<>();
//
//                PrintDataBuilder p1 = new PrintDataBuilder();
//                try {
//                    p1.addImage(context.getAssets().open("img" + File.separator + "boc_380.gif"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //address
//                p1.setFontSize(PrintItem.FontSize.SIZE_16);
//                p1.addTextMiddle("");
//                p1.addTextMiddle(receipt.getAddressLine1());
//                p1.addTextMiddle(receipt.getAddressLine2());
//                p1.addTextMiddle(receipt.getAddressLine3());
//                //line
//                p1.addDotLine();
//                //date time
//                p1.addText(Const.RECEIPT_DATE_TIME, "", receipt.getDateTime());
//                //mid
//                p1.addText(Const.RECEIPT_MERCHANT_ID, "", receipt.getMerchantId());
//                //tid
//                p1.addText(Const.RECEIPT_TERMINAL_ID, "", receipt.getTerminalId());
//                //batch no
//                p1.addText(Const.RECEIPT_BATCH_NO, "", ValidatorUtil.getInstance().zeroPadString(receipt.getBatchNo(), 6));
//                //invoice no
//                p1.addText(Const.RECEIPT_HOST, "", receipt.getHost());
//                //line
//                p1.addDotLine();
//
//                //field values
//                List<Transaction> allTransactions = receipt.getTransactionList();
//
//                //Summary
//                //count card types
//                int visaCount = 0;
//                int masterCount = 0;
//                int amexCount = 0;
//                long visaAmount = 0;
//                long masterAmount = 0;
//                long amexAmount = 0;
//                for (Transaction transaction : allTransactions) {
//                    if (transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION)
//                        continue;
//
//                    String cardType = transaction.getCard_label();
//                    switch (cardType) {
//                        case Const.CARD_TYPE_STRING_VISA: {
//                            if (transaction.getVoided() == 0) {
//                                visaCount++;
//                                visaAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                        case Const.CARD_TYPE_STRING_MASTER: {
//                            if (transaction.getVoided() == 0) {
//                                masterCount++;
//                                masterAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                        case Const.CARD_TYPE_STRING_AMEX: {
//                            if (transaction.getVoided() == 0) {
//                                amexCount++;
//                                amexAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                    }
//                }
//
//                //title
//                p1.setFontSize(PrintItem.FontSize.SIZE_24_BOLD);
//                p1.addTextMiddle(Const.RECEIPT_SUMMARY_REPORT);
//                p1.setFontSize(PrintItem.FontSize.SIZE_16);
//                p1.addTextLeft("  ");
//
//                //column titles
//                p1.addText(Const.RECEIPT_TRANSACTION, Const.RECEIPT_COUNT, Const.RECEIPT_TOTAL + "(" + receipt.getCurrency() + ")");
//                p1.addTextMiddle("");
//
//                //values
//                //visa
//                if (isAvailableIssuer(receipt.getIssuerList(), Const.CARD_TYPE_STRING_VISA))
//                    p1.addText(Const.CARD_TYPE_STRING_VISA, String.valueOf(visaCount), Utility.getFormattedAmount(visaAmount));
//                //master
//                if (isAvailableIssuer(receipt.getIssuerList(), Const.CARD_TYPE_STRING_MASTER))
//                    p1.addText(Const.CARD_TYPE_STRING_MASTER, String.valueOf(masterCount), Utility.getFormattedAmount(masterAmount));
//                //amex
//                if (isAvailableIssuer(receipt.getIssuerList(), Const.CARD_TYPE_STRING_AMEX))
//                    p1.addText(Const.CARD_TYPE_STRING_AMEX, String.valueOf(amexCount), Utility.getFormattedAmount(amexAmount));
//
//
//                //line
//                p1.addDotLine();
//                //totals
//                p1.addText(Const.RECEIPT_TOTALS, String.valueOf(visaCount + masterCount + amexCount), Utility.getFormattedAmount(visaAmount + masterAmount + amexAmount));
//                //line
//                p1.addDotLine();
//                p1.addSpace(5);
//
//                printDataBuilders.add(p1);
//
//                Handler handler = new Handler(context.getMainLooper());
//
//                try {
//                    handler.post(() -> listener.onReceiptGenerated(printDataBuilders));
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    handler.post(listener::onReceiptGenerationFailed);
//                }
//            }
//        }.start();
//    }

    private String getTransactionType(Transaction t) {
        String tranType = "";
        if (t.getVoided() == 1)
            tranType = Const.RECEIPT_VOID + " ";
        if (t.getTransaction_code() == SALE
                || t.getTransaction_code() == SALE_MANUAL) {
            tranType = tranType + Const.RECEIPT_SALE;
        } else if (t.getTransaction_code() == SALE_OFFLINE
                || t.getTransaction_code() == SALE_OFFLINE_MANUAL) {
            tranType = tranType + Const.RECEIPT_OFFLINE_SALE;
        } else if (t.getTransaction_code() == SALE_PRE_COMPLETION) {
            tranType = tranType + Const.RECEIPT_PRE_COMP;
        } else if (t.getTransaction_code() == SALE_INSTALLMENT) {
            tranType = tranType + Const.RECEIPT_INSTALLMENT;
        } else if (t.getTransaction_code() == SALE_REFUND
                || t.getTransaction_code() == SALE_REFUND_MANUAL) {
            tranType = tranType + Const.RECEIPT_REFUND;
        } else if (t.getTransaction_code() == CASH_BACK) {
            tranType = tranType + Const.RECEIPT_CASH_BACK;
        } else if (t.getTransaction_code() == QUASI_CASH
                || t.getTransaction_code() == QUASI_CASH_MANUAL) {
            tranType = tranType + Const.RECEIPT_QUASI_CASH;
        } else if (t.getTransaction_code() == CASH_ADVANCE) {
            tranType = tranType + Const.RECEIPT_CASH_ADVANCE;
        }
        else if (t.getTransaction_code() == QR_SALE) {
            tranType = tranType + Const.RECEIPT_QR_SALE;
        }
        return tranType;
    }

    private Issuer getIssuer(List<Issuer> issuers, Transaction transaction) {
        for (Issuer i : issuers) {
            if (i.getIssuerNumber() == transaction.getIssuer_number()) {
                return i;
            }
        }

        return null;
    }

    /**
     * Generate detail report
     *
     * @param transactions
     * @param listener
     */
    public void generateDetailReportReceiptArvin(Host host, Merchant merchant, Terminal terminal, Currency currency, List<Issuer> issuers, ArrayList<Transaction> transactions, ReceiptBuilderListener listener) {
        new Thread() {
            @Override
            public void run() {
                String receiptDateTime = "";
                String batchNo = Utility.padLeftZeros(merchant.getBatchNumber(), 6);
                String hostName = TextUtils.isEmpty(host.getHostName()) ? "" : host.getHostName();

                try {
                    Date txnDateTime = Calendar.getInstance().getTime();
                    receiptDateTime = new SimpleDateFormat(Const.RECEIPT_DATE_TIME_FORMAT, Locale.ENGLISH)
                            .format(txnDateTime);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                List<PrintDataBuilder> printDataBuilders = new ArrayList<>();

                PrintDataBuilder p1 = new PrintDataBuilder();
                try {
                    p1.addImage(context.getAssets().open("img" + File.separator + "boc_380.gif"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //address
                p1.setFontSize(PrintItem.FontSize.SIZE_16);
                p1.addTextMiddle("");
                p1.addTextMiddle(merchant.getRctHdr1());
                p1.addTextMiddle(merchant.getRctHdr2());
                p1.addTextMiddle(merchant.getRctHdr3());

                //line
                p1.addDotLine();
                //date time
                p1.addText(Const.RECEIPT_DATE_TIME, "", receiptDateTime);
                //mid
                p1.addText(Const.RECEIPT_MERCHANT_ID, "", merchant.getMerchantID());
                //tid
                p1.addText(Const.RECEIPT_TERMINAL_ID, "", terminal.getTerminalID());
                //batch no
                p1.addText(Const.RECEIPT_BATCH_NO, "", batchNo);
                //invoice no
                p1.addText(Const.RECEIPT_HOST, "", hostName);
                //line
                p1.addDotLine();
                //Detail Report
                p1.setFontSize(PrintItem.FontSize.SIZE_24_BOLD);
                p1.addTextMiddle(Const.RECEIPT_DETAIL_REPORT.toUpperCase());
                p1.setFontSize(PrintItem.FontSize.SIZE_16);
                //line
                p1.addDotLine();
                //field names
                if (host.getHostID() != 4) {
                    p1.addText(Const.RECEIPT_CARD_NAME, "", Const.RECEIPT_CARD_NO);
                    p1.addText(Const.RECEIPT_EXP_DATE, "", Const.RECEIPT_INVOICE_NO);
                }
                else{
                    p1.addText(Const.RECEIPT_INVOICE_NO, "", "Trace No");
                }
                p1.addText(Const.RECEIPT_TRANSACTION_DATE, "", Const.RECEIPT_TRANSACTION_TIME);
                p1.addText(Const.RECEIPT_TRANSACTION, "", Const.RECEIPT_AMOUNT);
                if (host.getHostID() != 4) {
                p1.addText(Const.RECEIPT_APPROVE_CODE, "", Const.RECEIPT_USERNAME);}
                else{

                p1.addText("Acc /Card No", "", "");
              //  p1.addText(Const.RECEIPT_APPROVE_CODE, "", "");
                }
                //line
                p1.addDotLine();

                printDataBuilders.add(p1);

                //txn counts
                int visaCount = 0;
                int masterCount = 0;
                int amexCount = 0;
                int cupCount = 0;
                int jcbCount = 0;
                int qrCount = 0;

                //txn amounts
                long visaAmount = 0;
                long masterAmount = 0;
                long amexAmount = 0;
                long cupAmount = 0;
                long jcbAmount = 0;
                long qrAmount = 0;

                Partition<Transaction> featurePartitions = Partition.ofSize(transactions, 5);
                for (List<Transaction> transactionList : featurePartitions) {
                    PrintDataBuilder printDataBuilder = new PrintDataBuilder();
                    for (Transaction t : transactionList) {
                        if (t.getIssuer_number() == 1) {
                            //Visa
                            visaCount += 1;
                            if (t.getVoided() == 0) {
                                //Add to the total if not voided
                                if (t.getTransaction_code() == SALE_REFUND || t.getTransaction_code() == SALE_REFUND_MANUAL ) {
                                    visaAmount -= Long.parseLong(t.getTotal_amount());
                                }else{
                                    visaAmount += Long.parseLong(t.getTotal_amount());
                                }
                            }
                        } else if (t.getIssuer_number() == 2) {
                            //Master
                            masterCount += 1;
                            if (t.getVoided() == 0) {
                                //Add to the total if not voided
                                if (t.getTransaction_code() == SALE_REFUND || t.getTransaction_code() == SALE_REFUND_MANUAL ) {
                                    masterAmount -= Long.parseLong(t.getTotal_amount());
                                }else{
                                    masterAmount += Long.parseLong(t.getTotal_amount());
                                }
                            }
                        } else if (t.getIssuer_number() == 3) {
                            //JCB
                            jcbCount += 1;
                            if (t.getVoided() == 0) {
                                //Add to the total if not voided
                                if (t.getTransaction_code() == SALE_REFUND || t.getTransaction_code() == SALE_REFUND_MANUAL ) {
                                    jcbAmount -= Long.parseLong(t.getTotal_amount());
                                }else{
                                    jcbAmount += Long.parseLong(t.getTotal_amount());
                                }
                            }
                        }
                        else if (t.getIssuer_number() == 4) {
                            //Amex
                            amexCount += 1;
                            if (t.getVoided() == 0) {
                                //Add to the total if not voided
                                if (t.getTransaction_code() == SALE_REFUND || t.getTransaction_code() == SALE_REFUND_MANUAL ) {
                                    amexAmount -= Long.parseLong(t.getTotal_amount());
                                }else{
                                    amexAmount += Long.parseLong(t.getTotal_amount());
                                }
                            }
                        }
                        else if (t.getIssuer_number() == 6) {
                            //CUP
                            cupCount += 1;
                            if (t.getVoided() == 0) {
                                //Add to the total if not voided
                                if (t.getTransaction_code() == SALE_REFUND || t.getTransaction_code() == SALE_REFUND_MANUAL ) {
                                    cupAmount -= Long.parseLong(t.getTotal_amount());
                                }else{
                                    cupAmount += Long.parseLong(t.getTotal_amount());
                                }
                            }
                        }
                        else if (t.getIssuer_number() == 8) {
                            //QR
                            qrCount += 1;
                            if (t.getVoided() == 0) {
                                //Add to the total if not voided
                                qrAmount += Long.parseLong(t.getTotal_amount());
                            }
                        }

                        String posEntryMode = com.epic.pos.iso.modal.Transaction.posEntryModeToString(String.valueOf(t.getChip_status()));

                        String saleType = getTransactionType(t);
                        String expDate="";
                        String maskedCarPan="";
                        if(t.getTransaction_code()!=TranTypes.QR_SALE) {
                             maskedCarPan = Utility.maskCardNumber(t.getPan(), Utility.getMaskingFormat(t.getPan())) + " " + posEntryMode;
                            Issuer issuer = getIssuer(issuers, t);
                             expDate = Utility.maskCardNumber(t.getExp_date(), issuer.getMaskExpireDate());
                        }

                        String txnDate = Utility.getFormattedDate(t.getTxn_date());
                        String txnTime = Utility.getFormattedTime(t.getTxn_time());
                        String amount="0";
                        if (t.getTransaction_code() == SALE_REFUND || t.getTransaction_code() == SALE_REFUND_MANUAL || (t.getVoided()==1)) {
                            amount ="-"+Utility.getFormattedAmount(Long.parseLong(t.getTotal_amount())) + " " + t.getCurrency_symbol();
                        }
                        else{
                            amount = Utility.getFormattedAmount(Long.parseLong(t.getTotal_amount())) + " " + t.getCurrency_symbol();
                        }
                        if(t.getTransaction_code()!=TranTypes.QR_SALE) {
                            printDataBuilder.addText(t.getCard_label(), "", maskedCarPan);
                            printDataBuilder.addText(expDate, "", t.getInvoice_no());
                        }
                        else{
                            printDataBuilder.addText(t.getInvoice_no(), "", t.getTrace_no());
                        }

                        printDataBuilder.addText(txnDate, "", txnTime);
                        printDataBuilder.setFontSize(PrintItem.FontSize.SIZE_16_BOLD);
                        printDataBuilder.addText(saleType, "", amount);
                        printDataBuilder.setFontSize(PrintItem.FontSize.SIZE_16);
                        if(t.getTransaction_code()== QR_SALE){
                        printDataBuilder.addTextLeft(t.getPan());
                        }else{
                        printDataBuilder.addTextLeft(t.getApprove_code());
                        }
                        printDataBuilder.addTextLeft("  ");
                    }
//                    }
                    printDataBuilders.add(printDataBuilder);
                }


                //detail summary report
                PrintDataBuilder p2 = new PrintDataBuilder();
                //line
                p2.addDotLine();
                //title
                p2.setFontSize(PrintItem.FontSize.SIZE_24_BOLD);
                p2.addTextMiddle(Const.RECEIPT_SUMMARY_REPORT.toUpperCase());
                p2.setFontSize(PrintItem.FontSize.SIZE_16);
                p2.addTextLeft("  ");
                //column titles
                p2.addText(Const.RECEIPT_TRANSACTION, Const.RECEIPT_COUNT, Const.RECEIPT_TOTAL + "(" + currency.getCurrencySymbol() + ")");
                p2.addTextMiddle("");

                //values
                //visa
                if (visaCount >= 1) {
                    p2.addText(Const.CARD_TYPE_STRING_VISA, String.valueOf(visaCount), Utility.getFormattedAmount(visaAmount));
                }
                //master
                if (masterCount >= 1) {
                    p2.addText(Const.CARD_TYPE_STRING_MASTER, String.valueOf(masterCount), Utility.getFormattedAmount(masterAmount));
                }
                //amex
                if (amexCount >= 1) {
                    p2.addText(Const.CARD_TYPE_STRING_AMEX, String.valueOf(amexCount), Utility.getFormattedAmount(amexAmount));
                }

                //JCB
                if (jcbCount >= 1) {
                    p2.addText(Const.CARD_TYPE_STRING_JCB, String.valueOf(jcbCount), Utility.getFormattedAmount(jcbAmount));
                }

                //CUP
                if (cupCount >= 1) {
                    p2.addText(Const.CARD_TYPE_STRING_CUP, String.valueOf(cupCount), Utility.getFormattedAmount(cupAmount));
                }

                //QR
                if (qrCount >= 1) {
                    p2.addText(Const.CARD_TYPE_STRING_QR, String.valueOf(qrCount), Utility.getFormattedAmount(qrAmount));
                }

                //line
                p2.addDotLine();
                //totals
                p2.addText(Const.RECEIPT_TOTALS, String.valueOf(visaCount + masterCount + amexCount +jcbCount +cupCount+qrCount), Utility.getFormattedAmount(visaAmount + masterAmount + amexAmount+jcbAmount+cupAmount+qrAmount));
                //line
                p2.addDotLine();
                p2.addText("","","");
                p2.addText("","","");
                p2.addText("","","");
                p2.addText("","","");
                p2.addText("","","");

                printDataBuilders.add(p2);

                Handler handler = new Handler(context.getMainLooper());

                try {
                    handler.post(() -> listener.onReceiptGenerated(printDataBuilders));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handler.post(listener::onReceiptGenerationFailed);
                }
            }
        }.start();
    }

    /**
     * Generate and save detail report receipt
     *
     * @param receipt
     * @param listener
     */
//    public void generateDetailReportReceiptNew(DetailReportReceipt receipt, ReceiptBuilderListener listener) {
//        new Thread() {
//            @Override
//            public void run() {
//                List<PrintDataBuilder> printDataBuilders = new ArrayList<>();
//
//                PrintDataBuilder p1 = new PrintDataBuilder();
//                try {
//                    p1.addImage(context.getAssets().open("img" + File.separator + "boc_380.gif"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //address
//                p1.setFontSize(PrintItem.FontSize.SIZE_16);
//                p1.addTextMiddle("");
//                p1.addTextMiddle(receipt.getAddressLine1());
//                p1.addTextMiddle(receipt.getAddressLine2());
//                p1.addTextMiddle(receipt.getAddressLine3());
//                //line
//                p1.addDotLine();
//                //date time
//                p1.addText(Const.RECEIPT_DATE_TIME, "", receipt.getDateTime());
//                //mid
//                p1.addText(Const.RECEIPT_MERCHANT_ID, "", receipt.getMerchantId());
//                //tid
//                p1.addText(Const.RECEIPT_TERMINAL_ID, "", receipt.getTerminalId());
//                //batch no
//                p1.addText(Const.RECEIPT_BATCH_NO, "", ValidatorUtil.getInstance().zeroPadString(receipt.getBatchNo(), 6));
//                //invoice no
//                p1.addText(Const.RECEIPT_HOST, "", receipt.getHost());
//                //line
//                p1.addDotLine();
//
//                //Detail Report
//                p1.setFontSize(PrintItem.FontSize.SIZE_24_BOLD);
//                p1.addTextMiddle(Const.RECEIPT_DETAIL_REPORT);
//                p1.setFontSize(PrintItem.FontSize.SIZE_16);
//
//                //line
//                p1.addDotLine();
//
//                //field names
//                p1.addText(Const.RECEIPT_CARD_NAME, "", Const.RECEIPT_CARD_NO);
//                p1.addText(Const.RECEIPT_EXP_DATE, "", Const.RECEIPT_INVOICE_NO);
//                p1.addText(Const.RECEIPT_TRANSACTION_DATE, "", Const.RECEIPT_TRANSACTION_TIME);
//                p1.addText(Const.RECEIPT_TRANSACTION, "", Const.RECEIPT_AMOUNT);
//                p1.addText(Const.RECEIPT_APPROVE_CODE, "", Const.RECEIPT_USERNAME);
//
//                //line
//                p1.addDotLine();
//
//                printDataBuilders.add(p1);
//
//                //field values
//                List<Transaction> allTransactions = receipt.getTransactionList();
//                Partition<Transaction> featurePartitions = Partition.ofSize(allTransactions, 20);
//                for (List<Transaction> transactionList : featurePartitions) {
//                    PrintDataBuilder pTxnSet = new PrintDataBuilder();
//                    for (Transaction transaction : transactionList) {
//                        if (transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION)
//                            continue;
//                        //'void sale' or 'sale'
//                        String tranType = "";
//                        if (transaction.getVoided() == 1)
//                            tranType = Const.RECEIPT_VOID + " ";
//
//                        if (transaction.getTransaction_code() == SALE) {
//                            tranType = tranType + Const.RECEIPT_SALE;
//                        } else if (transaction.getTransaction_code() == SALE_OFFLINE) {
//                            tranType = tranType + Const.RECEIPT_OFFLINE_SALE;
//                        } else if (transaction.getTransaction_code() == SALE_MANUAL) {
//                            tranType = tranType + Const.RECEIPT_MANUAL_SALE;
//                        } else if (transaction.getTransaction_code() == SALE_OFFLINE_MANUAL) {
//                            tranType = tranType + Const.RECEIPT_OFFLINE_MANUAL_SALE;
//                        } else if (transaction.getTransaction_code() == SALE_PRE_COMPLETION) {
//                            tranType = tranType + Const.RECEIPT_PRE_COMP;
//                        } else if (transaction.getTransaction_code() == SALE_INSTALLMENT) {
//                            tranType = tranType + Const.RECEIPT_INSTALLMENT;
//                        } else if (transaction.getTransaction_code() == SALE_REFUND) {
//                            tranType = tranType + Const.RECEIPT_REFUND;
//                        } else if (transaction.getTransaction_code() == SALE_REFUND_MANUAL) {
//                            tranType = tranType + Const.RECEIPT_REFUND_MANUAL;
//                        } else if (transaction.getTransaction_code() == CASH_BACK) {
//                            tranType = tranType + Const.RECEIPT_CASH_BACK;
//                        } else if (transaction.getTransaction_code() == QUASI_CASH) {
//                            tranType = tranType + Const.RECEIPT_QUASI_CASH;
//                        } else if (transaction.getTransaction_code() == QUASI_CASH_MANUAL) {
//                            tranType = tranType + Const.RECEIPT_QUASI_CASH_MANUAL;
//                        } else if (transaction.getTransaction_code() == CASH_ADVANCE) {
//                            tranType = tranType + Const.RECEIPT_CASH_ADVANCE;
//                        }
//
//
//                        //to mask card no
//                        String mask = Utility.getMaskingFormat(transaction.getPan());
//
//                        //card name
//                        //card no
//                        pTxnSet.addText(transaction.getCard_label(), "", Utility.maskCardNumber(transaction.getPan(), mask));
//                        //exp date
//                        //invoice no
//                        pTxnSet.addText("**/**", "", transaction.getInvoice_no());
//                        //transaction date
//                        //tran time
//                        pTxnSet.addText(Utility.getFormattedDate(transaction.getTxn_date()), "", Utility.getFormattedTime(transaction.getTxn_time()));
//                        //tran type
//                        //amount
//                        pTxnSet.setFontSize(PrintItem.FontSize.SIZE_16_BOLD);
//                        pTxnSet.addText(tranType, "", Utility.getFormattedAmount(Long.parseLong(transaction.getBase_transaction_amount())) + " " + receipt.getCurrency());
//                        //approve code
//                        //username
//                        pTxnSet.setFontSize(PrintItem.FontSize.SIZE_16);
//                        pTxnSet.addText(transaction.getApprove_code(), "", "");
//                        pTxnSet.addTextLeft("  ");
//                    }
//                    printDataBuilders.add(pTxnSet);
//                }
//
//                //Summary
//                //count card types
//                int visaCount = 0;
//                int masterCount = 0;
//                int amexCount = 0;
//                long visaAmount = 0;
//                long masterAmount = 0;
//                long amexAmount = 0;
//                for (Transaction transaction : allTransactions) {
//                    if (transaction.getTransaction_code() == SALE_PRE_AUTHORIZATION)
//                        continue;
//
//                    String cardType = transaction.getCard_label();
//                    switch (cardType) {
//                        case Const.CARD_TYPE_STRING_VISA: {
//                            if (transaction.getVoided() == 0) {
//                                visaCount++;
//                                visaAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                        case Const.CARD_TYPE_STRING_MASTER: {
//                            if (transaction.getVoided() == 0) {
//                                masterCount++;
//                                masterAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                        case Const.CARD_TYPE_STRING_AMEX: {
//                            if (transaction.getVoided() == 0) {
//                                amexCount++;
//                                amexAmount += Long.parseLong(transaction.getBase_transaction_amount());
//                            }
//                            break;
//                        }
//                    }
//                }
//
//
//                PrintDataBuilder p2 = new PrintDataBuilder();
//                //line
//                p2.addDotLine();
//
//                //title
//                p2.setFontSize(PrintItem.FontSize.SIZE_24_BOLD);
//                p2.addTextMiddle(Const.RECEIPT_SUMMARY_REPORT);
//                p2.setFontSize(PrintItem.FontSize.SIZE_16);
//                p2.addTextLeft("  ");
//
//                //column titles
//                p2.addText(Const.RECEIPT_TRANSACTION, Const.RECEIPT_COUNT, Const.RECEIPT_TOTAL + "(" + receipt.getCurrency() + ")");
//                p2.addTextMiddle("");
//
//                //values
//                //visa
//                if (isAvailableIssuer(receipt.getIssuerList(), Const.CARD_TYPE_STRING_VISA))
//                    p2.addText(Const.CARD_TYPE_STRING_VISA, String.valueOf(visaCount), Utility.getFormattedAmount(visaAmount));
//                //master
//                if (isAvailableIssuer(receipt.getIssuerList(), Const.CARD_TYPE_STRING_MASTER))
//                    p2.addText(Const.CARD_TYPE_STRING_MASTER, String.valueOf(masterCount), Utility.getFormattedAmount(masterAmount));
//                //amex
//                if (isAvailableIssuer(receipt.getIssuerList(), Const.CARD_TYPE_STRING_AMEX))
//                    p2.addText(Const.CARD_TYPE_STRING_AMEX, String.valueOf(amexCount), Utility.getFormattedAmount(amexAmount));
//
//
//                //line
//                p2.addDotLine();
//                //totals
//                p2.addText(Const.RECEIPT_TOTALS, String.valueOf(visaCount + masterCount + amexCount), Utility.getFormattedAmount(visaAmount + masterAmount + amexAmount));
//                //line
//                p2.addDotLine();
//                p2.addSpace(5);
//
//                printDataBuilders.add(p2);
//
//                Handler handler = new Handler(context.getMainLooper());
//
//                try {
//                    handler.post(() -> listener.onReceiptGenerated(printDataBuilders));
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    handler.post(listener::onReceiptGenerationFailed);
//                }
//            }
//        }.start();
//    }

    private boolean isAvailableIssuer(List<Issuer> issuerList, String cardType) {
        for (Issuer issuer : issuerList) {
            if (issuer.getIssuerLable().equals(cardType))
                return true;
        }
        return false;
    }

    private Bitmap scale(Bitmap bm) {
        int maxWidth = 380;
        int width = bm.getWidth();
        int height = bm.getHeight();

        float ratio = (float) width / maxWidth;
        width = maxWidth;
        height = (int) (height / ratio);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public interface CancelReceiptListener {
        void onReceiptGenerated(Bitmap bitmap);

        void onReceiptFailed();
    }

    public interface GenerateReceiptListener {
        void onReceived(Bitmap bitmap);

        void onGenerateReceiptFailed();
    }

    public interface DuplicateReceiptListener {
        void onReceiptGenerated(Bitmap bitmap);

        void onReceiptFailed();
    }

    public interface ReceiptListener {
        void onReceiptGenerated();

        void onReceiptGenerationFailed();
    }

    public interface ReceiptBuilderListener {
        void onReceiptGenerated(List<PrintDataBuilder> printDataBuilders);

        void onReceiptGenerationFailed();
    }


    private String getFontBold() {
        return "fonts/consolab.ttf";
    }

    private String getFontRegular() {
        return "fonts/consola.ttf";
    }


    private Bitmap getBankLogo(String logo) {
        return getImageFromAssetsFile(logo);
    }


    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
