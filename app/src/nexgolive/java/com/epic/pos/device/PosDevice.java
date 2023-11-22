package com.epic.pos.device;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.epic.pos.R;
import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.modal.Aid;
import com.epic.pos.device.data.CVMResult;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
import com.epic.pos.device.data.CardType;
import com.epic.pos.device.data.EmvData;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintDataBuilder;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.data.PrintItem;
import com.epic.pos.device.listener.CardInListener;
import com.epic.pos.device.listener.EcrInitListener;
import com.epic.pos.device.listener.ErrorListener;
import com.epic.pos.device.listener.GetTLVDataListener;
import com.epic.pos.device.listener.InitListener;
import com.epic.pos.device.listener.PosCheckCardListener;
import com.epic.pos.device.listener.PosCheckCardListener2;
import com.epic.pos.device.listener.PosPinListener;
import com.epic.pos.device.listener.PrintListener;
import com.epic.pos.device.listener.PrinterStatusListener;
import com.epic.pos.device.listener.SmartCardListener;
import com.epic.pos.device.listener.VerifyOnlineProcessListener;
import com.epic.pos.device.serial.ECRCom;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.ISO8583u;
import com.epic.pos.util.Utility;
import com.google.gson.Gson;
import com.nexgo.common.ByteUtils;
import com.nexgo.oaf.apiv3.APIProxy;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.DeviceInfo;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.card.cpu.APDUEntity;
import com.nexgo.oaf.apiv3.card.cpu.CPUCardHandler;
import com.nexgo.oaf.apiv3.device.beeper.Beeper;
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.OnPinPadInputListener;
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinKeyboardModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinPad;
import com.nexgo.oaf.apiv3.device.pinpad.PinPadKeyCode;
import com.nexgo.oaf.apiv3.device.pinpad.WorkKeyTypeEnum;
import com.nexgo.oaf.apiv3.device.printer.AlignEnum;
import com.nexgo.oaf.apiv3.device.printer.GrayLevelEnum;
import com.nexgo.oaf.apiv3.device.printer.Printer;
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity;
import com.nexgo.oaf.apiv3.device.reader.CardReader;
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener;
import com.nexgo.oaf.apiv3.emv.AidEntity;
import com.nexgo.oaf.apiv3.emv.CandidateAppInfoEntity;
import com.nexgo.oaf.apiv3.emv.CapkEntity;
import com.nexgo.oaf.apiv3.emv.EmvDataSourceEnum;
import com.nexgo.oaf.apiv3.emv.EmvEntryModeEnum;
import com.nexgo.oaf.apiv3.emv.EmvHandler2;
import com.nexgo.oaf.apiv3.emv.EmvOnlineResultEntity;
import com.nexgo.oaf.apiv3.emv.EmvProcessFlowEnum;
import com.nexgo.oaf.apiv3.emv.EmvProcessResultEntity;
import com.nexgo.oaf.apiv3.emv.EmvTransConfigurationEntity;
import com.nexgo.oaf.apiv3.emv.OnEmvProcessListener2;
import com.nexgo.oaf.apiv3.emv.PromptEnum;
import com.nexgo.oaf.apiv3.platform.Platform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.nexgo.mdbclient.MdbServiceManager;
import cn.nexgo.mdbclient.constant.TransResult;

/**
 * The PosDevice class is used to manage Verifone X990 device.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-08
 */
public class PosDevice {

    private final String TAG = "PosDevice";
    private final int FONT_SIZE_SMALL = 18;
    private final int FONT_SIZE_NORMAL = 24;
    private final int FONT_SIZE_BIG = 24;
    private final String LINE = "----------------------------------------------------------------------------";

    private DeviceEngine deviceEngine;
    private Beeper beeper;
    private Printer printer;
    private DeviceInfo deviceInfo;
    //pin pad related
    private PinPad pinPad;
    private String pwdText = "";
    private View pinView;
    private TextView tvPin;
    private AlertDialog pinAlertDialog;
    //card reading related
    private CardReader cardReader;
    private CardSlotTypeEnum mExistSlot;
    //emv related
    private EmvHandler2 emvHandler2;
    private EmvUtils emvUtils;
    //verify listener
    private VerifyOnlineProcessListener verifyOnlineProcessListener = null;
    //TLE card handler
    private CPUCardHandler cpuCardHandler = null;
    private boolean apduPowerOn = false;
    //Device configurations
    private Platform platform;

    private Aid visaAid;
    private Aid masterAid;
    private Aid cupAid;
    private Aid amexAid;
    private Aid dinersAid;
    private Aid jcbAid;
    private ECRCom ecrCom;
    private static PosDevice mInstance;
    private static Context context;
    private boolean isBinded = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private final byte PIN_LENGTHS_PINBYPASS_DISABLED[] = {4, 6, 12};
    private final byte PIN_LENGTHS_PINBYPASS_ENABLE[] = {0, 4, 6, 12};
    private Bundle globalParam = new Bundle();

    private Activity activity = null;
    private long txnAmount = 0;
    private long cashBackAmount = 0;
    private String merchantName = "";
    private String merchantId = "";
    private String terminalID = "00000000";
    private String countryCode = "144";
    private String currencyCode = "144";
    private String traceNo = "00000000";
    private boolean posDeviceInitialized = false;
    private String transactionAid = "";
    private boolean isCardChecking = false;
    private byte transProcessCode = 0x00;

    private int cvmResult = 0;

    private ErrorListener errorListener = null;

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    /**
     * Set init listener
     *
     * @param initListener
     */
    public void setInitListener(InitListener initListener) {
        if (posDeviceInitialized) {
            initListener.onInitCompleted();
        } else {
            new Thread() {
                @Override
                public void run() {
                    while (!posDeviceInitialized) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    handler.post(initListener::onInitCompleted);
                }
            }.start();
        }
    }

    /**
     * Is paper exists in paper.
     *
     * @return
     */
    public boolean isPaperExistsInPrinter(){
        log("isPaperExistsInPrinter()");
        if (printer != null){
            return printer.getStatus() == 0;
        }

        return true;
    }

    /**
     * Set ECR init listener
     *
     * @param listener
     */
    public void setEcrInitListener(EcrInitListener listener) {
        if (ecrCom != null) {
            listener.onEcrComInitCompleted();
        } else {
            new Thread() {
                @Override
                public void run() {
                    while (ecrCom == null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.post(listener::onEcrComInitCompleted);
                }
            }.start();
        }
    }

    public boolean isCardChecking() {
        return isCardChecking;
    }

    public void stopCheckCard() {
        log("stopCheckCard()");
        stopEmvFlow();


//        try {
//            if (emvHandler2 != null) {
//                emvHandler2.emvProcessCancel();
//                isCardChecking = false;
//            }
//            stopEmvFlow();
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            if (e instanceof DeadObjectException) {
//                if (errorListener != null) {
//                    errorListener.onServiceError();
//                }
//            }
//            isCardChecking = false;
//        }
    }

    /**
     * Get tag data
     *
     * @param tagName Name of the tag
     * @return ASCII value of the tag
     */
    public String getTagDataToDisplay(String tagName) {
        log("getTagDataToDisplay(): tagName: " + tagName);
        String tagValHex = getTagData(tagName);
        if (!TextUtils.isEmpty(tagValHex)) {
            if (tagValHex.length() >= 2) {
                tagValHex = tagValHex.substring(tagName.length() + 2);
            }

            try {
                String asciiValue = hexToAscii(tagValHex).trim();
                log("ASCII value:" + asciiValue);
                return asciiValue;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Get tag data
     *
     * @param tagName Name of the tag
     * @return HexString of tag
     */
    public String getTagData(String tagName) {
        log("getTagData() tag: " + tagName);
        if (emvHandler2 != null) {
            try {
                String value = emvHandler2.getTlvByTags(new String[]{tagName});
                log("Value: " + value);
                return value;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * This method will initiate card reading process.
     *
     * @param checkContactLess
     * @param checkSmartCard
     * @param checkMagneticCard
     * @param timeOut
     * @param listener
     */
    public void checkCard(boolean checkContactLess, boolean checkSmartCard, boolean checkMagneticCard, boolean beepEnable, int timeOut, PosCheckCardListener listener) {

        log("checkCard (" + checkContactLess + ", " + checkSmartCard + ", " + checkMagneticCard + ", " + timeOut + ")");

        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();

        if (checkSmartCard) {
            slotTypes.add(CardSlotTypeEnum.ICC1);
        }

        if (checkContactLess) {
            slotTypes.add(CardSlotTypeEnum.RF);
        }

        if (checkMagneticCard) {
            slotTypes.add(CardSlotTypeEnum.SWIPE);
        }

        cardReader.searchCard(slotTypes, timeOut, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, final CardInfoEntity cardInfo) {
                log("onCardInfo() retCode: " + retCode);
                if (retCode == SdkResult.Success) {
                    log("card info result success.");
                    mExistSlot = cardInfo.getCardExistslot();
                    isCardChecking = false;
                    if (beepEnable) {
                        log("beep()");
                        beep(100);
                    }

                    if (mExistSlot == CardSlotTypeEnum.SWIPE) {
                        log("card swiped.");
                        handler.post(() -> listener.onCardData(CardAction.SWIPE, toCardData(cardInfo, false)));
                    } else {
                        log("card not swiped state.");
                        CardAction cardAction = CardAction.INSERT;

                        if (mExistSlot == CardSlotTypeEnum.ICC1) {
                            log("card inserted.");
                            handler.post(listener::  onCardInserted);
                        } else if (mExistSlot == CardSlotTypeEnum.RF) {
                            log("Card tap.");
                            cardAction = CardAction.TAP;
                        }

                        EmvTransConfigurationEntity transData = new EmvTransConfigurationEntity();
                        transData.setTransAmount(Utility.padLeftZeros(String.valueOf(txnAmount), 12));

                        if (cashBackAmount != 0) {
                            transData.setCashbackAmount(Utility.padLeftZeros(String.valueOf(cashBackAmount), 12));
                        }

                        log("currencyCode: " + countryCode);
                        log("currencyCode: " + currencyCode);
                        log("terminalID: " + terminalID);
                        log("merchantId: " + merchantId);
                        log("traceNo: " + traceNo);

                        transData.setEmvTransType((byte) 0x00); //0x00-sale, 0x20-refund,0x09-sale with cashback
                        transData.setCountryCode(countryCode);
                        transData.setCurrencyCode(currencyCode);
                        transData.setTermId(terminalID);
                        transData.setMerId(merchantId);
                        transData.setTransDate(new SimpleDateFormat("yyMMdd", Locale.getDefault()).format(new Date()));
                        transData.setTransTime(new SimpleDateFormat("hhmmss", Locale.getDefault()).format(new Date()));
                        transData.setTraceNo(traceNo);

                        transData.setEmvProcessFlowEnum(EmvProcessFlowEnum.EMV_PROCESS_FLOW_STANDARD);
                        if (cardInfo.getCardExistslot() == CardSlotTypeEnum.RF) {
                            transData.setEmvEntryModeEnum(EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACTLESS);
                        } else {
                            transData.setEmvEntryModeEnum(EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACT);
                        }

                        log("start emv process.");
                        CardAction finalCardAction = cardAction;
                        emvHandler2.emvProcess(transData, new OnEmvProcessListener2() {
                            @Override
                            public void onSelApp(final List<String> appNameList, List<CandidateAppInfoEntity> appInfoList, boolean isFirstSelect) {
                                log("onSelectApplication");
                                if (appNameList.size() == 1) {
                                    log("auto select first application.");
                                    selectApplication(1);
                                } else {
                                    log("prompt user to select application.");
                                    handler.post(() -> listener.onSelectApplication(appNameList));
                                }
                            }

                            @Override
                            public void onTransInitBeforeGPO() {
                                log("onTransInitBeforeGPO() - onAfterFinalSelectedApp");

                                byte[] aid = emvHandler2.getTlv(new byte[]{0x4F}, EmvDataSourceEnum.FROM_KERNEL);
                                transactionAid = ByteUtils.byteArray2HexString(aid).toUpperCase();
                                log("AID: " + transactionAid);

                                if (mExistSlot == CardSlotTypeEnum.RF) {
                                    log("CTLS terminal capabilities");
                                    if (isVisaAid()) {
                                        //Visa
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF19"), ByteUtils.hexString2ByteArray(visaAid.getFLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF20"), ByteUtils.hexString2ByteArray(visaAid.getTxnAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF21"), ByteUtils.hexString2ByteArray(visaAid.getCVMLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(visaAid.getCTLSPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(visaAid.getTTQCTLSValue()));
                                        log("CTLS - Visa terminal capabilities are updated.");
                                    } else if (isMasterAid()) {
                                        //Master
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF19"), ByteUtils.hexString2ByteArray(masterAid.getFLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF20"), ByteUtils.hexString2ByteArray(masterAid.getTxnAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF21"), ByteUtils.hexString2ByteArray(masterAid.getCVMLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(masterAid.getCTLSPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(masterAid.getTTQCTLSValue()));
                                        log("CTLS - Master terminal capabilities are updated.");
                                    } else if (isAmexAid()){
                                        //AMEX
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF19"), ByteUtils.hexString2ByteArray(amexAid.getFLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF20"), ByteUtils.hexString2ByteArray(amexAid.getTxnAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF21"), ByteUtils.hexString2ByteArray(amexAid.getCVMLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(amexAid.getCTLSPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(amexAid.getTTQCTLSValue()));
                                        log("CTLS - Amex terminal capabilities are updated.");
                                    } else if (isDinersAid()){
                                        //Diners
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF19"), ByteUtils.hexString2ByteArray(dinersAid.getFLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF20"), ByteUtils.hexString2ByteArray(dinersAid.getTxnAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF21"), ByteUtils.hexString2ByteArray(dinersAid.getCVMLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(dinersAid.getCTLSPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(dinersAid.getTTQCTLSValue()));
                                        log("CTLS - Diners terminal capabilities are updated.");
                                    } else if (isJCBAid()){
                                        //JCB
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF19"), ByteUtils.hexString2ByteArray(jcbAid.getFLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF20"), ByteUtils.hexString2ByteArray(jcbAid.getTxnAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF21"), ByteUtils.hexString2ByteArray(jcbAid.getCVMLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(jcbAid.getCTLSPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(jcbAid.getTTQCTLSValue()));
                                        log("CTLS - Diners terminal capabilities are updated.");
                                    } else if (isCUPAid()){
                                        //CUP
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF19"), ByteUtils.hexString2ByteArray(cupAid.getFLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF20"), ByteUtils.hexString2ByteArray(cupAid.getTxnAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF21"), ByteUtils.hexString2ByteArray(cupAid.getCVMLAmt()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(cupAid.getCTLSPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(cupAid.getTTQCTLSValue()));
                                        log("CTLS - CUP terminal capabilities are updated.");
                                    }
                                } else {
                                    log("Contact terminal capabilities.");
                                    //contact terminal capability
                                    if (isVisaAid()) {
                                        //Visa
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(visaAid.getPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(visaAid.getTTQValue()));
                                        log("Contact - Visa contact terminal capabilities are updated.");
                                    } else if (isMasterAid()) {
                                        //master
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(masterAid.getPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(masterAid.getTTQValue()));
                                        log("Contact - Master terminal capabilities are updated.");
                                    } else if (isAmexAid()) {
                                        //Amex
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(amexAid.getPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(amexAid.getTTQValue()));
                                        log("Contact - AMEX terminal capabilities are updated.");
                                    } else if (isDinersAid()) {
                                        //Diners
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(dinersAid.getPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(dinersAid.getTTQValue()));
                                        log("Contact - Diners terminal capabilities are updated.");
                                    } else if (isJCBAid()) {
                                        //JCB
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(jcbAid.getPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(jcbAid.getTTQValue()));
                                        log("Contact - JCB terminal capabilities are updated.");
                                    } else if (isCUPAid()) {
                                        //CUP
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F33"), ByteUtils.hexString2ByteArray(cupAid.getPinSupportValue()));
                                        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), ByteUtils.hexString2ByteArray(cupAid.getTTQValue()));
                                        log("Contact - CUP terminal capabilities are updated.");
                                    }
                                }

                                emvHandler2.onSetTransInitBeforeGPOResponse(true);
                            }

                            @Override
                            public void onConfirmCardNo(CardInfoEntity info) {
                                log("onConfirmCardNo()");
                                log("Card no: " + info.getCardNo());
                                handler.post(() -> {
                                    try {
                                        CardData data = toCardData(info, true);
                                        log(data.toString());
                                        listener.onCardData(finalCardAction, data);
                                    } catch (Exception ex) {
                                        err(ex);
                                    }
                                });
                            }

                            @Override
                            public void onCardHolderInputPin(final boolean isOnlinePin, int leftTimes) {
                                log("onCardHolderInputPin() isOnlinePin: " + isOnlinePin + " | leftTimes: " + leftTimes);
                                handler.post(() -> listener.onPinRequested(isOnlinePin, leftTimes));
                            }

                            @Override
                            public void onContactlessTapCardAgain() {
                                log("onContactlessTapCardAgain()");
                            }

                            @Override
                            public void onOnlineProc() {
                                log("onOnlineProc()");
                                handler.post(() -> {
                                    listener.onRequestOnlineProcess();
                                });
                            }

                            @Override
                            public void onPrompt(PromptEnum promptEnum) {
                                log("onPrompt() promptEnum: " + promptEnum);
                                emvHandler2.onSetPromptResponse(true);
                            }

                            @Override
                            public void onRemoveCard() {
                                log("onRemoveCard()");
                                emvHandler2.onSetRemoveCardResponse();
                            }

                            @Override
                            public void onFinish(int retCode, EmvProcessResultEntity entity) {
                                log("onFinish()");
                                onEmvProcessFinished(retCode, entity, listener);
                            }
                        });
                    }
                } else {
                    log("card scab failed - retCode: " + retCode);
                    isCardChecking = false;
                    stopCheckCard();
                    if (beepEnable) {
                        beep(100);
                    }

                    if (retCode == -3) {
                        log("onTimeout");
                        handler.post(listener::onTimeOut);
                    } else {
                        handler.post(() -> listener.onCheckCardError(retCode, "Unable to read the card."));
                    }

                    setTransResult(TransResult.TRADE_FAILURE);
                }
            }

            @Override
            public void onSwipeIncorrect() {
                log("onSwipeIncorrect()");

            }

            @Override
            public void onMultipleCards() {
                log("onMultipleCards()");
            }
        });
    }

    private boolean isVisaAid() {
        return transactionAid.toUpperCase().contains("A0000000031010")
                || transactionAid.toUpperCase().contains("A0000000032010")
                || transactionAid.toUpperCase().contains("A000000003101001")
                || transactionAid.toUpperCase().contains("A000000003101002");
    }

    private boolean isMasterAid() {
        return transactionAid.toUpperCase().contains("A0000000041010")
                || transactionAid.toUpperCase().contains("A0000000046000")
                || transactionAid.toUpperCase().contains("A0000000101030");
    }

    private boolean isAmexAid() {
        return transactionAid.toUpperCase().contains("A00000002501");
    }

    private boolean isDinersAid() {
        return transactionAid.toUpperCase().contains("A0000001523010");
    }

    private boolean isJCBAid() {
        return transactionAid.toUpperCase().contains("A00000006510");
    }

    private boolean isCUPAid() {
        return transactionAid.toUpperCase().contains("A000000333010101")
                || transactionAid.toUpperCase().contains("A000000333010102")
                || transactionAid.toUpperCase().contains("A000000333010103")
                || transactionAid.toUpperCase().contains("A000000333010106")
                || transactionAid.toUpperCase().contains("A0000003241010");
    }

    public void confirmCardNumber() {
        log("confirmCardNumber()");
        emvHandler2.onSetConfirmCardNoResponse(true);
    }

    private void onEmvProcessFinished(int retCode, EmvProcessResultEntity entity, PosCheckCardListener listener) {
        log("onEmvProcessFinished()");
        log("emvHandler2.getSignNeed() " + emvHandler2.getSignNeed());
        log("getcardinfo: " + new Gson().toJson(emvHandler2.getEmvCardDataInfo()));
        log("getEmvCvmResult: " + emvHandler2.getEmvCvmResult());

        boolean isFallback = false;
        final String msg;

        switch (retCode) {
            case SdkResult.Emv_Success_Arpc_Fail:
            case SdkResult.Success:
            case SdkResult.Emv_Script_Fail:
                //online approve
                msg = "Online approve.";
                break;
            case SdkResult.Emv_Qpboc_Offline:// EMV Contactless: Offline Approval
            case SdkResult.Emv_Offline_Accept://EMV Contact: Offline Approval
                //offline approve
                msg = "Offline approve";
                break;
            //this retcode is Abolished
            case SdkResult.Emv_Qpboc_Online://EMV Contactless: Online Process for union pay
                //union pay online contactless--application should go online
                msg = "Union pay online contactless--application should go online";
                break;
            case SdkResult.Emv_Candidatelist_Empty:// Application have no aid list
            case SdkResult.Emv_FallBack://  FallBack ,chip card reset failed
                //fallback process
                msg = "Fallback process.";
                isFallback = true;
                break;
            case SdkResult.Emv_Arpc_Fail: //
            case SdkResult.Emv_Declined:
                //online decline ,if it is in second gac, application should decide if it is need reversal the transaction
                msg = "Online decline";
                break;
            case SdkResult.Emv_Cancel:// Transaction Cancel
                //user cancel
                msg = "User cancel";
                break;
            case SdkResult.Emv_Offline_Declined: //
                //offline decline
                msg = "Offline decline";
                break;
            case SdkResult.Emv_Card_Block: //Card Block
                //card is blocked
                msg = "Card is blocked";
                break;
            case SdkResult.Emv_App_Block: // Application Block
                //card application block
                msg = "Card application block";
                break;
            case SdkResult.Emv_App_Ineffect:
                //card not active
                msg = "Card not active";
                break;
            case SdkResult.Emv_App_Expired:
                //card Expired
                msg = "Card Expired";
                break;
            case SdkResult.Emv_Other_Interface:
                //try other entry mode, like contact or mag-stripe
                msg = "Try other entry mode, like contact or mag-stripe";
                break;
            case SdkResult.Emv_Plz_See_Phone:
                //see phone flow
                //prompt a dialog to user to check phone-->search contactless card(another card) -->start emvProcess again
                msg = "See phone flow";
                break;
            case SdkResult.Emv_Terminate:
                //transaction terminate
                msg = "Transaction terminate";
                break;
            default:
                msg = "Other error";
                //other error
                break;
        }

        //verify callbacks
        //callback on verification
        if (verifyOnlineProcessListener != null) {
            handler.post(() -> {
                if (retCode == SdkResult.Success) {
                    log("verify_callback: success");
                    verifyOnlineProcessListener.onlineProcessSuccess();
                } else if (retCode == SdkResult.Emv_Terminate) {
                    log("verify_callback: terminate");
                    verifyOnlineProcessListener.onlineProcessTerminate();
                } else if (retCode == SdkResult.Emv_Declined) {
                    log("verify_callback: refuse");
                    verifyOnlineProcessListener.onlineProcessRefuse();
                } else {
                    log("verify_callback: error");
                    verifyOnlineProcessListener.onError(retCode);
                }
            });
        }

        log("retCode: " + retCode + " | msg: " + msg + " | isFallback: " + isFallback);
        boolean finalIsFallback = isFallback;
        handler.post(() -> {
            if (finalIsFallback) {
                log("fallback.");
                listener.onFallback();
            } else {
                if (retCode == SdkResult.Success) {
                    log("success.");
                    setTransResult(TransResult.TRADE_SUCCESS);
                    writeEmvTagsOnFile();
                    listener.onRequestOnlineProcess();
                } else {
                    log("failed.");
                    setTransResult(TransResult.TRADE_FAILURE);
                    listener.onEmvError(retCode, null, msg);
                }
            }
        });
    }

    /**
     * Select application for malty application cards
     *
     * @param index application index
     */
    public void selectApplication(int index) {
        log("selectApplication: " + index);
        emvHandler2.onSetSelAppResponse(index);
    }

    /**
     * Launch device pin pad
     *
     * @param workKeyId    Work key slot id from IHT table
     * @param isOnlinePin  Is online pin
     * @param pan          Card PAN
     * @param promptString Prompt String
     * @param listener     Pin pad callbacks
     */
    public void launchPinPad(int workKeyId, boolean isOnlinePin, boolean isPinBypass, String pan, String promptString, PosPinListener listener) {
        log("launchPinPad- workKeyId: " + workKeyId + " | isOnlinePin: " + isOnlinePin + " | isPinBypass: " + isPinBypass + " | pan: " + pan);
        pwdText = "";
        pinView = activity.getLayoutInflater().inflate(R.layout.dialog_inputpin_layout, null);
        tvPin = pinView.findViewById(R.id.input_pin);
        pinAlertDialog = new AlertDialog.Builder(activity).setView(pinView).create();
        pinAlertDialog.setCanceledOnTouchOutside(false);
        pinAlertDialog.show();
        tvPin.setText(pwdText);

        try {
            OnPinPadInputListener ppiListener = new OnPinPadInputListener() {
                @Override
                public void onInputResult(final int retCode, final byte[] data) {
                    log("onInputResult() [i: " + retCode + "]");
                    pinAlertDialog.dismiss();

                    if (retCode == SdkResult.Success || retCode == SdkResult.PinPad_No_Pin_Input || retCode == SdkResult.PinPad_Input_Cancel) {
                        if (data != null) {
                            byte[] temp = new byte[8];
                            System.arraycopy(data, 0, temp, 0, 8);
                        }

                        listener.onConfirm(data, isOnlinePin);
                        emvHandler2.onSetPinInputResponse(retCode != SdkResult.PinPad_Input_Cancel, retCode == SdkResult.PinPad_No_Pin_Input);
                    } else {
                        log("PIN enter failed. retCode: " + retCode);
                        emvHandler2.onSetPinInputResponse(false, false);
                        listener.onError(retCode);
                    }
                }

                @Override
                public void onSendKey(byte keyCode) {
                    log("onSendKey()");
                    if (keyCode == PinPadKeyCode.KEYCODE_CLEAR) {
                        pwdText = "";
                    } else {
                        pwdText += "* ";
                    }

                    tvPin.setText(pwdText);
                }
            };

            if (isOnlinePin) {
                pinPad.inputOnlinePin(new int[]{0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c}, Const.PIN_ENTER_TIMEOUT, pan.getBytes(), workKeyId, PinAlgorithmModeEnum.ISO9564FMT1, ppiListener);
            } else {
                pinPad.inputOfflinePin(new int[]{0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c}, Const.PIN_ENTER_TIMEOUT, ppiListener);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listener.onError(-1);
        }
    }

    private void setTransResult(TransResult code) {
        log("setTransResult() code: " + code);
        MdbServiceManager.getInstance().setPayResult(code);
    }

    /**
     * Get TLV tags
     *
     * @param cardType card Types (VISA, MASTER, AMEX)
     */
    public void getTLVFromTags(CardType cardType, GetTLVDataListener listener) {
        log("getTLVFromTags: " + cardType.toString());

        String[] tagList = EmvData.VISA_TAG_LIST_NEXGO;

        if (cardType == CardType.MASTER) {
            tagList = EmvData.MASTER_TAG_LIST_STRING;
        } else if (cardType == CardType.AMEX) {
            tagList = EmvData.AMEX_TAG_LIST_STRING;
        } else if (cardType == CardType.CUP) {
            tagList = EmvData.CUP_TAG_LIST_STRING;
        }

        String finalTlv = emvHandler2.getTlvByTags(tagList);
        transactionAid = emvHandler2.getTlvByTags(new String[]{"84"});

        if (!TextUtils.isEmpty(transactionAid) && transactionAid.length() >= 5 && transactionAid.startsWith("84")) {
            transactionAid = transactionAid.substring(4);
        }

        final String panSequenceNumber = Utility.getPanSequenceNumber(finalTlv);

        log("TLV: " + finalTlv);
        log("Transaction AID: " + transactionAid);
        log("PAN Sequence Number: " + panSequenceNumber);

        handler.post(() -> listener.onTLVDataReceived(finalTlv, panSequenceNumber));
    }

    /**
     * Verify online emv process
     *
     * @param responseCode
     * @param approvalCode
     * @param emvData
     * @param listener
     */
    public void verifyOnlineProcess(String responseCode, String approvalCode, String emvData, VerifyOnlineProcessListener listener) {
        log("verifyOnlineProcess() responseCode: " + responseCode + " | approvalCode: " + approvalCode + " | emvData: " + emvData);

        verifyOnlineProcessListener = listener;
        EmvOnlineResultEntity emvOnlineResult = new EmvOnlineResultEntity();

        if (!TextUtils.isEmpty(responseCode)) {
            emvOnlineResult.setRejCode(responseCode);
        }

        if (!TextUtils.isEmpty(approvalCode)) {
            emvOnlineResult.setAuthCode(approvalCode);
        }

        if (!TextUtils.isEmpty(emvData)) {
            emvOnlineResult.setRecvField55(Utility.hexStr2Byte(emvData));
        }

        emvHandler2.onSetOnlineProcResponse(SdkResult.Success, emvOnlineResult);
    }

    public static final String EMV_TAG_FILE = "emvtag.txt";
    private static final int NUM_TAGS = 60;
    private String emvTags[][] = {{"4F	", "AID                    "}, {"57	", "TRACK2_EQ_DATA         "}, {"5A	", "APPL_PAN               "}, {"5F24	", "EXPIRY_DATE            "}, {"5F2A	", "TRANS_CURCY_CODE       "}, {"5F30	", "SERVICE_CODE           "}, {"5F34	", "APPL_PAN_SEQNUM        "}, {"81	", "AMOUNT_AUTH            "}, {"82	", "APPL_INTCHG_PROF       "}, {"86	", "ISSUER_SCRIPT_CMD      "}, {"89	", "AUTH_CODE              "}, {"8A	", "AUTH_RESP_CODE         "}, {"8C	", "CDOL1                  "}, {"8D	", "CDOL2                  "}, {"8E	", "CVM_LIST               "}, {"8F	", "CA_PK_INDEX(ICC)       "}, {"91	", "ISS_AUTH_DATA          "}, {"94	", "AFL                    "}, {"93	", "SGN_SAD                "}, {"95	", "TVR                    "}, {"97	", "TDOL                   "}, {"9A	", "TRANS_DATE             "}, {"9C	", "TRANS_TYPE             "}, {"9B	", "TSI                    "}, {"9F02	", "AMT_AUTH_NUM           "}, {"9F03	", "OTHER_AMT              "}, {"9F07	", "APPL_USE_CNTRL         "}, {"9F08	", "APP_VER_NUM            "}, {"9F09	", "TERM_VER_NUM           "}, {"9F0D	", "IAC_DEFAULT            "}, {"9F0E	", "IAC_DENIAL             "}, {"9F0F	", "IAC_ONLINE             "}, {"9F10	", "ISSUER_APP_DATA        "}, {"9F12	", "PREFERRED_NAME         "}, {"9F15	", "MERCHANT_CAT_CODE      "}, {"9F18	", "ISSUER_SCRIPT_ID       "}, {"9F1A	", "TERM_COUNTY_CODE       "}, {"9F1B	", "TERM_FLOOR_LIMIT       "}, {"9F1D	", "TERM_RISKMGMT_DATA     "}, {"9F1E	", "IFD_SER_NUM            "}, {"9F23	", "UC_OFFLINE_LMT         "}, {"9F26	", "AC9                    "}, {"9F27	", "CRYPT_INFO_DATA        "}, {"9F2A	", "TRANS_CURCY_CODE       "}, {"9F33	", "TERM_CAP               "}, {"9F34	", "CVM_RESULTS            "}, {"9F36	", "APP_TXN_COUNTER        "}, {"9F37	", "UNPREDICT_NUMBER       "}, {"9F38	", "PDOL                   "}, {"9F39	", "POS_ENT_MODE           "}, {"9F3A	", "AMT_REF_CURR           "}, {"9F3C	", "TRANS_REF_CURR         "}, {"9F45	", "DATA_AUTH_CODE         "}, {"9F49	", "DDOL                   "}, {"9F4A	", "SDA_TAGLIST            "}, {"9F4B	", "DYNAMIC_APPL_DATA      "}, {"9F4C	", "ICC_DYNAMIC_NUM        "}, {"9F53	", "TRAN_CURR_CODE         "}, {"9F5B	", "ISS_SCRIPT_RES         "}, {"71	", "ISUER_SCRPT_TEMPL_71   "}, {"72	", "ISUER_SCRPT_TEMPL_72   "},};

    private void writeEmvTagsOnFile() {
        log("writeEmvTagsOnFile()");
        new Thread() {
            @Override
            public void run() {
                try {
                    int TAG_NAME = 0;
                    int TAG_DESC = 1;

                    //construct the tag array
                    String tagName;

                    String inTagList[] = new String[NUM_TAGS];

                    //convert the tag list to set of integers
                    for (int iTag = 0; iTag < NUM_TAGS; iTag++) {
                        tagName = emvTags[iTag][TAG_NAME].trim();
                        inTagList[iTag] = tagName;
                    }

                    //by now we have the tlv string start writing on the file
                    File currentDir = context.getFilesDir();
                    String path = currentDir.getPath();
                    path += "/Secured";

                    File emvFileDir = new File(path);

                    if (!emvFileDir.exists()) emvFileDir.mkdirs();

                    File emvFile = new File(path, EMV_TAG_FILE);

                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(emvFile));

                    String tagDesc = "";
                    String data = "";
                    String writeLine = "";

                    Map<String, String> tlvData = extractAndBuildMapFromTags(inTagList);

                    String sTag = "";

                    for (Map.Entry<String, String> tlvx : tlvData.entrySet()) {
                        tagName = tlvx.getKey();
                        if ((tagName.length() & 0x01) == 0x01) tagName = "0" + tagName;

                        data = tlvx.getValue();

                        //search through the array
                        for (int i = 0; i < NUM_TAGS; i++) {
                            sTag = emvTags[i][TAG_NAME].trim();
                            int IntTag = Integer.valueOf(sTag, 16);
                            int IntTagName = Integer.valueOf(tagName, 16);

                            if (IntTag == IntTagName) {
                                tagDesc = emvTags[i][TAG_DESC].trim();
                                break;
                            }
                        }
                        writeLine = tagName + "|" + tagDesc + "|" + data + "\n";
                        bufferedWriter.write(writeLine);
                        if (Const.ENABLE_TAG_DATA_LOG) {
                            log(writeLine);
                        }
                    }

                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    private Map<String, String> extractAndBuildMapFromTags(String[] tagList) {
        String TAG = "EMV55";
        byte[] tlv;

        Map<String, String> tagMap = new HashMap<>();
        ISO8583u iso55 = new ISO8583u();

        try {
            //load emv TLV data here
            for (String tag : tagList) {
                tlv = emvHandler2.getTlvByTags(new String[]{tag}).getBytes();
                if (null != tlv && tlv.length > 0) {
                    tagMap.put(tag, Utility.byte2HexStr(tlv));  // build up the field 55
                } else {
                    AppLog.e(TAG, "getCardData:" + tag + ", fails");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tagMap;
    }

    public void checkPowerUp() {
        log("checkPowerUp()");
    }

    public void smartCardProcess(byte[] apduRequest, boolean isProcessFinished, SmartCardListener listener) {
        log("smartCardProcess()");
        if (apduPowerOn) {
            log("APDU power on.");
            executeAPDUCmd(apduRequest, isProcessFinished, listener);
        } else {
            log("APDU not power on.");
            HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
            slotTypes.add(CardSlotTypeEnum.ICC1);
            cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
                @Override
                public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                    log("onCardInfo() retCode: " + retCode);
                    byte[] atr = new byte[128];
                    int ret = -1;

                    cpuCardHandler = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.ICC1);
                    apduPowerOn = cpuCardHandler.powerOn(atr);

                    if (!apduPowerOn) {
                        log("Power on failed.");
                        listener.onCheckCardError("Power on failed.");
                        return;
                    }

                    executeAPDUCmd(apduRequest, isProcessFinished, listener);
                }

                @Override
                public void onSwipeIncorrect() {
                    listener.onCheckCardError("Swipe incorrect.");
                }

                @Override
                public void onMultipleCards() {
                    listener.onCheckCardError("Multiple cards detected.");
                    cardReader.stopSearch();
                }
            });
        }
    }

    private void executeAPDUCmd(byte[] apduRequest, boolean isProcessFinished, SmartCardListener listener) {
        log("executeAPDUCmd()");
        byte[] apduResult = cpuCardHandler.exchangeAPDUCmd(apduRequest);
        String result = Utility.byte2HexStr(apduResult);
        log("APDU RESULT: " + result);

        if (result.endsWith("9000")) {
            log("APDU Success.");
            result = result.substring(0, result.length() - 4);
            listener.onAPDUSuccess(result);

            if (isProcessFinished) {
                beep(100);
            }
        } else {
            log("APDU Failed.");
            beep(100);
            listener.onCheckCardError("Pin Verification Failed");
        }
    }


    public CVMResult analyseCVMResult(boolean isTap) {
        boolean isSignNeeded = emvHandler2.getSignNeed();
        if (isSignNeeded) {
            return CVMResult.SIGNATURE;
        } else {
            return CVMResult.NO_CMV_REQUIRED;
        }
    }

    /**
     * Call this function to cancel emv flow
     */
    public void stopEmvFlow() {
        try {
            log("stopEmvFlow()");
            isCardChecking = false;
            cardReader.stopSearch();
            emvHandler2.emvProcessCancel();
            emvHandler2.emvProcessAbort();
        } catch (Exception ex) {
            err(ex);
            if (ex instanceof DeadObjectException) {
                if (errorListener != null) {
                    errorListener.onServiceError();
                }
            }
            isCardChecking = false;
        }
    }

    public void isCardIn(CardInListener listener) {
        log("isCardIn()");
        if (cardReader != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        while (cardReader.isCardExist(CardSlotTypeEnum.ICC1)) {
                            log("cardIsStillIn()");
                            if (listener != null) {
                                handler.post(listener::cardIsStillIn);
                            }
                            Thread.sleep(2000);
                        }
                        if (listener != null) {
                            log("onCardRemoved()");
                            handler.post(listener::onCardRemoved);
                        }
                    } catch (Exception ex) {
                        log("isCardIn() error: " + ex.getMessage());
                        ex.printStackTrace();
                        if (listener != null) {
                            handler.post(() -> listener.onError(ex));
                        }
                    }
                }
            }.start();
        } else {
            log("isCardIn() error: smartCardReader is null");
            if (listener != null) {
                handler.post(() -> listener.onError(new NullPointerException("smartCardReader is null")));
            }
        }
    }

    protected void SleepMe(int mSeconds) {
        try {
            Thread.sleep(mSeconds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private CardData toCardData(CardInfoEntity cardInfo, boolean isEmv) {
        log("toCardData()");
        String track1 = cardInfo.getTk1();
        String track2 = cardInfo.getTk2();
        String track3 = cardInfo.getTk3();

        //-- process track2 data --
        if (!TextUtils.isEmpty(track2)) {
            if (track2.contains("F")) {
                track2 = track2.substring(0, track2.length() - 1);
            }

            if (track2.length() > 37) {
                track2 = track2.substring(0, 37);
            }
            track2 = track2.replace("D", "=");
        }
        //-------------------------

        CardData c = new CardData();
        c.setPan(cardInfo.getCardNo());
        c.setTrack1(track1);
        c.setTrack2(track2);
        c.setTrack3(track3);
        c.setServiceCode(cardInfo.getServiceCode());
        c.setExpiryDate(cardInfo.getExpiredDate());

        try {
            if (isEmv) {
                log("EMV");
                String cardHolderName = getTagDataToDisplay("5F20");

                log("cardHolderName: " + cardHolderName);
                log("aid: " + transactionAid);

                if (!TextUtils.isEmpty(cardHolderName)) {
                    c.setCardHolderName(cardHolderName);
                }

                if (!TextUtils.isEmpty(transactionAid)) {
                    c.setAid(transactionAid);
                }
            } else {
                log("Not EMV");

                if (!TextUtils.isEmpty(track1)) {
                    String[] track1Data = track1.split("\\^");
                    c.setCardHolderName(track1Data[1].trim());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return c;
    }

    public boolean checkCardInsertOrNot() {
        log("checkCardInsertOrNot()");
        boolean isInsert = false;
        try {
            int waitCounter = 0;
            while (!cardReader.isCardExist(CardSlotTypeEnum.ICC1)) {
                if (waitCounter++ % 15 == 0) {
                    AppLog.i("TTTTTTTTTT", "Please Insert Card");
                }

                SleepMe(200);
                if (waitCounter > 45) {
                    AppLog.i("TTTTTTTTTT", "Ready");
                    isInsert = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInsert;
    }

    public void initECR() {
        if (ecrCom == null) {
            ecrCom = new ECRCom();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Serial Comunication and Key Related">
    public void loadMainKey(int mainKeyId, String key) {
        log("loadMainKey: " + mainKeyId + " | key: " + key);
        try {
            byte[] main_key_data = Utility.hexStr2Byte(key);
            int result = pinPad.writeMKey(mainKeyId, main_key_data, main_key_data.length);
            log("Write main key result: " + result);
        } catch (Exception ex) {
            err(ex);
        }
    }

    public void loadWorkKey(int mainKeyId, int workKeyId, String key) {
        log("loadWorkKey() [mainKeyId:" + mainKeyId + "| workKeyId:" + workKeyId + " | key: " + key + "]");
        try {
            byte[] work_key_data = Utility.hexStr2Byte(key);
            int result = pinPad.writeWKey(workKeyId, WorkKeyTypeEnum.MACKEY, work_key_data, work_key_data.length);
            log("WriteWKey MACKEY result:" + result);
            result = pinPad.writeWKey(workKeyId, WorkKeyTypeEnum.PINKEY, work_key_data, work_key_data.length);
            log("WriteWKey PINKEY result:" + result);
            result = pinPad.writeWKey(workKeyId, WorkKeyTypeEnum.TDKEY, work_key_data, work_key_data.length);
            log("WriteWKey TDKEY result:" + result);
            result = pinPad.writeWKey(workKeyId, WorkKeyTypeEnum.ENCRYPTIONKEY, work_key_data, work_key_data.length);
            log("WriteWKey ENCRYPTIONKEY result:" + result);
            log("loadWorkKey: end");
        } catch (Exception ex) {
            err(ex);
        }
    }

    public boolean isPinKeyExists(int keyId) {
        log("isPinKeyExists " + keyId);
        try {
            boolean isExists = pinPad.isKeyExist(keyId);
            log("isExists: " + isExists);
            return isExists;
        } catch (Exception ex) {
            err(ex);
        }

        return false;
    }

    public void clearMasterKey(int masterKey) {
        log("clearMasterKey() masterKey: " + masterKey);
        try {
            pinPad.deleteMKey(masterKey);
        } catch (Exception ex) {
            err(ex);
        }
    }

    public void clearWorkerKey(int workKeyId) {
        log("clearWorkerKey() workKeyId: " + workKeyId);
        try {
            pinPad.deleteWKey(workKeyId, WorkKeyTypeEnum.MACKEY);
            pinPad.deleteWKey(workKeyId, WorkKeyTypeEnum.PINKEY);
            pinPad.deleteWKey(workKeyId, WorkKeyTypeEnum.TDKEY);
            pinPad.deleteWKey(workKeyId, WorkKeyTypeEnum.ENCRYPTIONKEY);
        } catch (Exception ex) {
            err(ex);
        }
    }

    public ECRCom getEcrCom() {
        return ecrCom;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public String getSerialNo(){
        return deviceInfo.getSn();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Print Related">
    private boolean shouldRunPintThread = false;
    private boolean printingThreadStarted = false;
    private boolean isPrinterBusy = false;
    private boolean runPrintStatusCheck = false;
    private List<Print> printQueue = new ArrayList<>();

    /**
     * Add print to printer queue
     *
     * @param print
     */
    public void addToPrintQueue(Print print) {
        log("addToPrintQueue: " + print.toString());
        printQueue.add(print);
    }

    /**
     * Clear printer queue
     */
    public void clearPrintQueue() {
        printQueue.clear();
    }

    /**
     * Start print thread (call this method before entering printing flow)
     */
    public void startPrinting() {
        log("startPrinting");
        shouldRunPintThread = true;
        isPrinterBusy = false;
        startPrintingThread();
    }

    /**
     * Stop print thread (call this method when exit from printing flow)
     */
    public void stopPrinting() {
        log("stopPrinting");
        shouldRunPintThread = false;
    }

    /**
     * Check printer status method will check printer is busy
     *
     * @param listener onPrintingFinished() will be called if printer is not busy
     */
    public void checkPrinterStatus(PrinterStatusListener listener) {
        if (runPrintStatusCheck) {
            return;
        }
        log("checkPrinterStatus");

        runPrintStatusCheck = true;

        new Thread() {
            @Override
            public void run() {
                while (runPrintStatusCheck) {
                    if (!isPrinterBusy && printQueue.isEmpty()) {
                        log("printer is not busy");
                        runPrintStatusCheck = false;
                        handler.post(listener::onPrintingFinished);
                    } else {
                        log("printer is busy");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }


    private void startPrintingThread() {
        if (printingThreadStarted) {
            log("printing thread running.");
            return;
        }

        log("startPrintingThread()");
        printingThreadStarted = true;

        new Thread() {
            @Override
            public void run() {
                while (shouldRunPintThread) {
                    if (!printQueue.isEmpty()) {
                        if (!isPrinterBusy) {
                            Print p = printQueue.get(0);
                            if (p.getPrintType() == Print.PRINT_DATA_BUILDER) {
                                log("pt: print type builder.");
                                isPrinterBusy = true;
                                PrintListener printListener = p.getPrintListener();

                                printDataBuilder(p.getPrintDataBuilder(), new PrintListener() {
                                    @Override
                                    public void onPrintFinished() {
                                        log("pt: print finished.");
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(printListener::onPrintFinished);
                                        }
                                    }

                                    @Override
                                    public void onPrintError(PrintError printError) {
                                        log("pt: printer error.");
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(() -> printListener.onPrintError(printError));
                                        }
                                    }
                                });
                            } else if (p.getPrintType() == Print.PRINT_TYPE_ISO) {
                                log("pt: print type iso.");
                                isPrinterBusy = true;
                                PrintListener printListener = p.getPrintListener();
                                printISO(p.getIsoSentType(), p.getIsoData(), new PrintListener() {
                                    @Override
                                    public void onPrintFinished() {
                                        log("pt: print finished.");
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(printListener::onPrintFinished);
                                        }
                                    }

                                    @Override
                                    public void onPrintError(PrintError printError) {
                                        log("pt: printer error.");
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(() -> printListener.onPrintError(printError));
                                        }
                                    }
                                });
                            } else if (p.getPrintType() == Print.PRINT_TYPE_IMAGE) {
                                log("pt: print type image.");
                                isPrinterBusy = true;
                                PrintListener printListener = p.getPrintListener();
                                printBitmap(p.getBitmap(), new PrintListener() {
                                    @Override
                                    public void onPrintFinished() {
                                        log("pt: print finished.");
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(printListener::onPrintFinished);
                                        }
                                    }

                                    @Override
                                    public void onPrintError(PrintError printError) {
                                        log("pt: printer error.");
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(() -> printListener.onPrintError(printError));
                                        }
                                    }
                                });
                            }
                        } else {
                            log("pt: printer busy.");
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                printingThreadStarted = false;
            }
        }.start();
    }

    private void printDataBuilder(PrintDataBuilder printDataBuilder, PrintListener listener) {
        try {
            int printerInitStatus = printer.initPrinter();
            log("printerInitStatus: " + printerInitStatus);
            printer.setTypeface(Typeface.DEFAULT);
            printer.setLetterSpacing(0);
            printer.setGray(GrayLevelEnum.LEVEL_4);

            for (PrintItem p : printDataBuilder.getPrintItems()) {
                int fontSize = FONT_SIZE_SMALL;

                if (p.getFontSize() == PrintItem.FontSize.SIZE_16) {
                    fontSize = FONT_SIZE_SMALL;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_16_BOLD) {
                    fontSize = FONT_SIZE_SMALL;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_24) {
                    fontSize = FONT_SIZE_NORMAL;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_24_BOLD) {
                    fontSize = FONT_SIZE_NORMAL;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_32) {
                    fontSize = FONT_SIZE_BIG;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_32_BOLD) {
                    fontSize = FONT_SIZE_BIG;
                }

                if (p.getItemType() == PrintItem.ItemType.TEXT_LINE) {
                    printer.appendPrnStr(p.getLeftText() + " " + p.getMiddleText(), p.getRightText(), fontSize, true);
                } else if (p.getItemType() == PrintItem.ItemType.DOT_LINE) {
                    printer.appendPrnStr(LINE, fontSize, AlignEnum.CENTER, true);
                } else if (p.getItemType() == PrintItem.ItemType.BANK_LOGO) {
                    InputStream is = p.getImgInputStream();
                    if (is != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        printer.appendImage(bitmap, AlignEnum.CENTER);
                    }
                } else if (p.getItemType() == PrintItem.ItemType.SPACE) {
                    //nexgo will import lines automatically after end of the receipt
                }
            }

            printer.startPrint(false, code -> {
                if (code == 0) {
                    log("printISO(): success");
                    listener.onPrintFinished();
                } else {
                    log("printISO(): error: " + code);
                    listener.onPrintError(PrintError.PRINTER_ERROR);
                }
            });
        } catch (Exception ex) {
            listener.onPrintError(PrintError.PRINTER_ERROR);
        }
    }

    private void printISO(int sentType, byte[] data, PrintListener listener) {
        log("printISO()");
        String formatted = "";
        String prLine = "";
        int remLineLen = 27, len = 0, lastLine = 0;
        int start = 0;
        String dataStr = Utility.byte2HexStr(data);
        dataStr = dataStr.substring(4, dataStr.length());

        //put space between each byte
        while (true) {
            String sb = dataStr.substring(start, start + 2);
            formatted = formatted + sb + " ";
            start += 2;

            if (start >= dataStr.length()) break;
        }

        len = formatted.length();
        start = 0;

        try {
            int printerInitStatus = printer.initPrinter();
            log("printerInitStatus: " + printerInitStatus);
            printer.setTypeface(Typeface.DEFAULT);
            printer.setLetterSpacing(0);
            printer.setGray(GrayLevelEnum.LEVEL_2);

            if (sentType == 1) {
                printer.appendPrnStr("| SEND PACKET", FONT_SIZE_SMALL, AlignEnum.LEFT, true);
            } else {
                printer.appendPrnStr("| RECEIVE PACKET", FONT_SIZE_SMALL, AlignEnum.LEFT, true);
            }

            while (true) {
                if ((start + remLineLen) < len)
                    prLine = formatted.substring(start, start + remLineLen);
                else {
                    prLine = formatted.substring(start, start + (len - start));
                    lastLine = 1;
                }
                start += remLineLen;

                prLine = "| " + prLine + "|";

                printer.appendPrnStr(prLine, FONT_SIZE_SMALL, AlignEnum.LEFT, true);

                if (lastLine == 1) {
                    break;
                }
            }

            printer.startPrint(false, code -> {
                if (code == 0) {
                    log("printISO(): success");
                    listener.onPrintFinished();
                } else {
                    listener.onPrintError(PrintError.PRINTER_ERROR);
                    log("printISO(): error: " + code);
                }
            });
        } catch (Exception e) {
            err(e);
            listener.onPrintError(PrintError.PRINTER_ERROR);
        }
    }

    /**
     * This method will print given bitmap by resizing the given bitmap to 380px with maintaining aspect ratio.
     *
     * @param original
     * @param listener
     */
    private void printBitmap(Bitmap original, PrintListener listener) {
        log("printBitmap()");
        try {
            int printerInitStatus = printer.initPrinter();
            log("printerInitStatus: " + printerInitStatus);
            printer.setTypeface(Typeface.DEFAULT);
            printer.appendImage(original, AlignEnum.CENTER);

            printer.startPrint(false, code -> {
                original.recycle();
                if (code == 0) {
                    log("printBitmap() success");
                    listener.onPrintFinished();
                } else {
                    log("printBitmap() error: " + code);
                    listener.onPrintError(PrintError.PRINTER_ERROR);
                }
            });
        } catch (Exception ex) {
            err(ex);
            listener.onPrintError(PrintError.PRINTER_ERROR);
        }
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Accessors and Matadors">
    public void setApduPowerOn(boolean apduPowerOn) {
        log("setApduPowerOn(): apduPowerOn: " + apduPowerOn);
        this.apduPowerOn = apduPowerOn;
    }

    public void setActivity(Activity activity) {
        log("setActivity() activity: " + activity);
        this.activity = activity;
    }

    public void setTxnAmount(long txnAmount) {
        log("setTxnAmount: " + txnAmount);
        this.txnAmount = txnAmount;
    }

    public void setCashBackAmount(long cashBackAmount) {
        log("setCashBackAmount: " + txnAmount);
        this.cashBackAmount = cashBackAmount;
    }

    public void setMerchantName(String merchantName) {
        log("setMerchantName: " + merchantName);
        this.merchantName = merchantName;
    }

    public void setMerchantId(String merchantId) {
        log("setMerchantId: " + merchantId);
        this.merchantId = merchantId;
    }

    public void setTerminalID(String terminalID) {
        log("setTerminalID: " + terminalID);
        this.terminalID = terminalID;
    }

    public void setTransProcessCode(byte transProcessCode) {
        log("setTransProcessCode: " + transProcessCode);
        this.transProcessCode = transProcessCode;
    }

    public void setTransactionAid(String transactionAid) {
        log("setTransactionAid: " + transactionAid);
        this.transactionAid = transactionAid;
    }

    public String getTransactionAid() {
        return transactionAid;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Bind Service">

    /**
     * Bind X990 device service
     */
    public void bindDeviceService() {
        log("bindDeviceService");
        emvUtils = new EmvUtils(context);
        deviceEngine = APIProxy.getDeviceEngine(context);
        emvHandler2 = deviceEngine.getEmvHandler2("app2");
        deviceInfo = deviceEngine.getDeviceInfo();
        beeper = deviceEngine.getBeeper();
        printer = deviceEngine.getPrinter();
        pinPad = deviceEngine.getPinPad();
        pinPad.setAlgorithmMode(AlgorithmModeEnum.DES);
        pinPad.setPinKeyboardMode(PinKeyboardModeEnum.FIXED);
        cardReader = deviceEngine.getCardReader();
        platform = deviceEngine.getPlatform();

        updateAIDsAndRIDs();
        posDeviceInitialized = true;

        if (ecrCom != null) {
            ecrCom = new ECRCom();
        }

        //todo - add home button disable code
        platform.disableHomeButton();
    }

    public boolean isPosDeviceInitialized() {
        return posDeviceInitialized;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="AID and RID">

    /**
     * This method is use to clear all the aids and add new aids and rids.
     */
    public void updateAIDsAndRIDs() {
        log("updateAIDsAndRIDs");

        log("delete AIDs");
        emvHandler2.delAllAid();
        if (emvHandler2.getAidListNum() <= 0) {
            List<AidEntity> aidEntityList = emvUtils.getAidList();
            log("AID count: " + aidEntityList.size());
            if (aidEntityList == null) {
                log("initAID failed");
                return;
            }
            int i = emvHandler2.setAidParaList(aidEntityList);
            log("setAidParaList() res:, " + i);
        } else {
            log("setAidParaList " + "already load aid");
        }

        log("delete RIDs");
        emvHandler2.delAllCapk();
        int capk_num = emvHandler2.getCapkListNum();
        log("capk_num: " + capk_num);
        if (capk_num <= 0) {
            List<CapkEntity> capkEntityList = emvUtils.getCapkList();
            log("RID List: " + capkEntityList.size());

            if (capkEntityList == null) {
                log("initCAPK failed");
                return;
            }
            int j = emvHandler2.setCAPKList(capkEntityList);
            log("setCAPKList() res:" + j);
        } else {
            log("setCAPKList already load capk");
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Common">

    /**
     * Play Device Beep
     *
     * @param mSeconds
     */
    public void beep(int mSeconds) {
        log("beep: " + mSeconds);
        beeper.beep(mSeconds);
    }

    public static String byte2HexStr(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            String var1 = "";
            StringBuilder var2 = new StringBuilder("");

            for (int var3 = 0; var3 < var0.length; ++var3) {
                var1 = Integer.toHexString(var0[var3] & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static String asciiToString(String asciiString) {
        int start = 0;
        int end = 2;

        String char_value = "";
        String str = "";

        while (end <= asciiString.length()) {
            char_value = asciiString.substring(start, end);
            int val = Integer.valueOf(char_value, 16);
            str += Character.toString((char) val);
            start = end;
            end += 2;
        }

        return str;
    }

    private void err(Exception ex) {
        ex.printStackTrace();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder">

    /**
     * Get the PosDevice object
     *
     * @return PosDevice
     */
    public static PosDevice getInstance() {
        if (mInstance != null) {
            return mInstance;
        }

        throw new RuntimeException("PosDevice class not correctly instantiated. Please call PosDevice.Builder().setContext(context).build(); in the Application class onCreate.");
    }

    private PosDevice() {
        super();
        mInstance = this;
    }

    private void init(Context appContext, Aid visa, Aid master, Aid cup, Aid amex, Aid diners, Aid jcb) {
        context = appContext;
        visaAid = visa;
        masterAid = master;
        cupAid = cup;
        amexAid = amex;
        dinersAid = diners;
        jcbAid = jcb;
        bindDeviceService();
    }

    public final static class Builder {

        private Context mContext;
        private Aid visa;
        private Aid master;
        private Aid cup;
        private Aid amex;
        private Aid diners;
        private Aid jcb;

        public void clearAll() {
            mContext = null;
            visa = null;
            cup = null;
            amex = null;
            diners = null;
            jcb = null;
        }

        /**
         * Set the Context used to instantiate the PosDevice
         *
         * @param context the application context
         * @return the {@link Builder} object.
         */
        public Builder setContext(final Context context) {
            mContext = context;
            return this;
        }

        public Builder setVisa(Aid visa) {
            this.visa = visa;
            return this;
        }

        public Builder setMaster(Aid master) {
            this.master = master;
            return this;
        }

        public Builder setCup(Aid cup) {
            this.cup = cup;
            return this;
        }

        public Builder setAmex(Aid amex) {
            this.amex = amex;
            return this;
        }

        public Builder setDiners(Aid diners) {
            this.diners = diners;
            return this;
        }

        public Builder setJCB(Aid jcb) {
            this.jcb = jcb;
            return this;
        }

        /**
         * Initialize the EpicQRCom instance to used in the application.
         */
        public void build() {
            if (mContext == null) {
                throw new RuntimeException("Context not set, please set context before building the PosDevice instance.");
            }

            new PosDevice().init(mContext, visa, master, cup, amex, diners, jcb);
        }
    }
    // </editor-fold>


}
