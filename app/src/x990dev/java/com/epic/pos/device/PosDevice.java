package com.epic.pos.device;

import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.APDU_GET_COUNTER;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.APDU_GET_PIN_VERFICATION;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.APDU_GET_PIN_VERIF_MODE;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.APDU_SELECT_APP;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.TLE_AID;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;

import com.epic.pos.BuildConfig;
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
import com.epic.pos.device.listener.PosPinListener;
import com.epic.pos.device.listener.PrintListener;
import com.epic.pos.device.listener.PrinterStatusListener;
import com.epic.pos.device.listener.SmartCardListener;
import com.epic.pos.device.listener.VerifyOnlineProcessListener;
import com.epic.pos.device.serial.ECRCom;
import com.epic.pos.util.APDU;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.FileUtils;
import com.epic.pos.util.ISO8583u;
import com.epic.pos.util.Utility;
import com.vfi.smartpos.deviceservice.aidl.CheckCardListener;
import com.vfi.smartpos.deviceservice.aidl.EMVHandler;
import com.vfi.smartpos.deviceservice.aidl.IBeeper;
import com.vfi.smartpos.deviceservice.aidl.IDeviceInfo;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IEMV;
import com.vfi.smartpos.deviceservice.aidl.IExternalSerialPort;
import com.vfi.smartpos.deviceservice.aidl.IInsertCardReader;
import com.vfi.smartpos.deviceservice.aidl.IPinpad;
import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.IScanner;
import com.vfi.smartpos.deviceservice.aidl.ISerialPort;
import com.vfi.smartpos.deviceservice.aidl.OnlineResultHandler;
import com.vfi.smartpos.deviceservice.aidl.PinInputListener;
import com.vfi.smartpos.deviceservice.aidl.PinpadKeyType;
import com.vfi.smartpos.deviceservice.aidl.PrinterConfig;
import com.vfi.smartpos.deviceservice.aidl.PrinterListener;
import com.vfi.smartpos.deviceservice.aidl.ScannerListener;
import com.vfi.smartpos.deviceservice.constdefine.ConstCheckCardListener;
import com.vfi.smartpos.deviceservice.constdefine.ConstIPBOC;
import com.vfi.smartpos.deviceservice.constdefine.ConstIPinpad;
import com.vfi.smartpos.deviceservice.constdefine.ConstOnlineResultHandler;
import com.vfi.smartpos.deviceservice.constdefine.ConstPBOCHandler;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The PosDevice class is used to manage Verifone X990 device.
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-08
 */
public class PosDevice {

    private final String TAG = "PosDevice";

    private IEMV emvProcessor;
    private IPinpad pinPad;
    private IScanner scannerCamFront;
    private IScanner scannerCamRear;
    private IDeviceService deviceService;
    private IPrinter printer;
    private IBeeper beeper;
    private ISerialPort serialPort;
    private IExternalSerialPort externalSerialPort;
    private IDeviceInfo deviceInfo;
    private IInsertCardReader smartCardReader;

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

    private long txnAmount = 0;
    private long cashBackAmount = 0;
    private String merchantName = "";
    private String merchantId = "";
    private String terminalID = "00000000";
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

    /**
     * Is paper exists in paper.
     *
     * @return
     */
    public boolean isPaperExistsInPrinter(){
        log("isPaperExistsInPrinter()");
        return true;
    }

    public void startCam() {
        try {
            log("startCam");
            Bundle msgPrompt = new Bundle();
            msgPrompt.putString("upPromptString", "QR Code scanner, please make sure it's in the frame");
            scannerCamFront.startScan(msgPrompt, 20, new ScannerListener.Stub() {
                @Override
                public void onSuccess(String barcode) throws RemoteException {

                }

                @Override
                public void onError(int error, String message) throws RemoteException {

                }

                @Override
                public void onTimeout() throws RemoteException {

                }

                @Override
                public void onCancel() throws RemoteException {

                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public boolean isCardChecking() {
        return isCardChecking;
    }

    public void stopCheckCard() {
        log("stopCheckCard()");
        try {
            if (emvProcessor != null) {
                emvProcessor.stopCheckCard();
                isCardChecking = false;
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof DeadObjectException) {
                if (errorListener != null) {
                    errorListener.onServiceError();
                }
            }
            isCardChecking = false;
        }
    }

    public void abortPBOC() {
        log("abortPBOC");
        try {
            if (emvProcessor != null) {
                emvProcessor.abortEMV();
                isCardChecking = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            isCardChecking = false;
        }
    }


    /**
     * Get tag data
     *
     * @param tagName Name of the tag
     * @return ASCII value of the tag
     */
    public String getTagDataToDisplay(String tagName) {
        log("getTagDataToDisplay() tagName: " + tagName);
        String tagValHex = getTagData(tagName);
        if (!TextUtils.isEmpty(tagValHex)) {
            try {
                String asciiValue = hexToAscii(tagValHex);
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
        if (emvProcessor != null) {
            try {
                byte[] tlv = emvProcessor.getCardData(tagName);
                String value = Utility.byte2HexStr(tlv);
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
    public void checkCard(boolean checkContactLess,
                          boolean checkSmartCard,
                          boolean checkMagneticCard,
                          boolean beepEnable,
                          int timeOut,
                          PosCheckCardListener listener) {

        log("checkCard (" + checkContactLess + ", "
                + checkSmartCard + ", "
                + checkMagneticCard + ", "
                + timeOut + ")");

        Bundle cardOption = new Bundle();

        if (checkContactLess) {
            cardOption.putBoolean(ConstIPBOC.checkCard.cardOption.KEY_Contactless_boolean, ConstIPBOC.checkCard.cardOption.VALUE_supported);
        }

        if (checkSmartCard) {
            cardOption.putBoolean(ConstIPBOC.checkCard.cardOption.KEY_SmartCard_boolean, ConstIPBOC.checkCard.cardOption.VALUE_supported);
        }

        if (checkMagneticCard) {
            cardOption.putBoolean(ConstIPBOC.checkCard.cardOption.KEY_MagneticCard_boolean, ConstIPBOC.checkCard.cardOption.VALUE_supported);
        }

        try {
            isCardChecking = true;
            emvProcessor.checkCard(cardOption, timeOut, new CheckCardListener.Stub() {
                @Override
                public void onCardSwiped(Bundle track) throws RemoteException {
                    log("onCardSwiped");
                    isCardChecking = false;
                    emvProcessor.stopCheckCard();
                    if (beepEnable) {
                        beep(100);
                    }
                    handler.post(() -> listener.onCardData(CardAction.SWIPE, toCardData(track, false)));
                }

                @Override
                public void onCardPowerUp() throws RemoteException {
                    log("onCardPowerUp (card insert)");
                    isCardChecking = false;
                    emvProcessor.stopCheckCard();
                    if (beepEnable) {
                        beep(100);
                    }

                    handler.post(listener::onCardInserted);
                    processInsertOrTAPCardData(CardAction.INSERT, listener);
                }

                @Override
                public void onCardActivate() throws RemoteException {
                    log("onCardActivate (card tap)");
                    isCardChecking = false;
                    emvProcessor.stopCheckCard();
                    //emvProcessor.abortPBOC();
                    if (beepEnable) {
                        beep(100);
                    }
                    processInsertOrTAPCardData(CardAction.TAP, listener);
                }

                @Override
                public void onTimeout() throws RemoteException {
                    log("onTimeout");
                    isCardChecking = false;
                    emvProcessor.stopCheckCard();
                    if (beepEnable) {
                        beep(100);
                    }
                    handler.post(listener::onTimeOut);
                }

                @Override
                public void onError(int error, String message) throws RemoteException {
                    log("onError: (error: " + error + ", message: " + message);
                    isCardChecking = false;
                    emvProcessor.stopCheckCard();
                    if (beepEnable) {
                        beep(100);
                    }
                    handler.post(() -> listener.onCheckCardError(error, message));
                }
            });
        } catch (Exception ex) {
            isCardChecking = false;
            err(ex);
            handler.post(() -> {
                if (beepEnable) {
                    beep(100);
                }
                listener.onCheckCardError(-1, ex.getMessage());
            });
        }
    }

    /**
     * Select application for malty application cards
     *
     * @param index application index
     */
    public void selectApplication(int index) {
        log("selectApplication: " + index);
        try {
            emvProcessor.importAppSelection(index);
        } catch (Exception ex) {
            err(ex);
        }
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
        Bundle param = new Bundle();

        if (isPinBypass) {
            param.putByteArray(ConstIPinpad.startPinInput.param.KEY_pinLimit_ByteArray, PIN_LENGTHS_PINBYPASS_ENABLE);
        } else {
            param.putByteArray(ConstIPinpad.startPinInput.param.KEY_pinLimit_ByteArray, PIN_LENGTHS_PINBYPASS_DISABLED);
        }

        param.putInt(ConstIPinpad.startPinInput.param.KEY_timeout_int, Const.PIN_ENTER_TIMEOUT);
        param.putBoolean(ConstIPinpad.startPinInput.param.KEY_isOnline_boolean, isOnlinePin);
        param.putString(ConstIPinpad.startPinInput.param.KEY_pan_String, pan);
        param.putInt(ConstIPinpad.startPinInput.param.KEY_desType_int, ConstIPinpad.startPinInput.param.Value_desType_3DES);
        param.putString(ConstIPinpad.startPinInput.param.KEY_promptString_String, promptString);

        try {
            PinInputListener pinInputListener = new PinInputListener.Stub() {
                @Override
                public void onInput(int len, int key) throws RemoteException {
                    log("PinPad onInput, len:" + len + ", key:" + key);
                    listener.onKeyPress(len, key);
                }

                @Override
                public void onConfirm(byte[] data, boolean isNonePin) throws RemoteException {
                    AppLog.d(TAG, "PinPad onConfirm data = " + Utility.byte2HexStr(data));
                    exitKeyBoardOnUI();
                    listener.onConfirm(data, isNonePin);
                    emvProcessor.importPin(1, data);
                }

                @Override
                public void onCancel() throws RemoteException {
                    log("cancel");
                    exitKeyBoardOnUI();
                    if (isPinBypass) {
                        log("force insert empty pin to emv processor");
                        emvProcessor.importPin(1, null);
                    } else {
                        emvProcessor.importPin(0, null);
                        listener.onCancel();
                    }
                }

                @Override
                public void onError(int errorCode) throws RemoteException {
                    log("onError: " + errorCode);
                    exitKeyBoardOnUI();
                    listener.onError(errorCode);
                }
            };

            pinPad.startPinInput(workKeyId, param, globalParam, pinInputListener);
        } catch (Exception ex) {
            err(ex);
            listener.onError(-1);
        }
    }

    public void exitKeyBoardOnUI() {
        AppLog.i("TAG", "exitKeyBoardOnUI");
        try {
            pinPad.endPinInputCustomView();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get TLV tags
     *
     * @param cardType card Types (VISA, MASTER, AMEX)
     */
    public void getTLVFromTags(CardType cardType, GetTLVDataListener listener) {
        log("getTLVFromTags: " + cardType.toString());

        int[] tagList = EmvData.VISA_TAG_LIST;

        if (cardType == CardType.MASTER) {
            tagList = EmvData.MASTER_TAG_LIST;
        } else if (cardType == CardType.AMEX) {
            tagList = EmvData.AMEX_TAG_LIST;
        } else if (cardType == CardType.CUP) {
            tagList = EmvData.CUP_TAG_LIST;
        }

        String TAG = "EMV55";
        byte[] tlv;
        Map<Integer, String> tagOfF55 = new HashMap<>();
        ISO8583u iso55 = new ISO8583u();

        try {
            //load emv TLV data here
            for (int tag : tagList) {
                String tagS = Integer.toHexString(tag);
                if (tagS.toLowerCase().equals("9f1d")) {
                    log("build up the field 55");
                    tagOfF55.put(tag, "E007A00000000430600001029F1D086C00800000000000");
                } else {
                    tlv = emvProcessor.getCardData(Integer.toHexString(tag).toUpperCase());
                    if (null != tlv && tlv.length > 0) {
                        tagOfF55.put(tag, Utility.byte2HexStr(tlv));  // build up the field 55
                        AppLog.d("55tag", Integer.toHexString(tag) + "- " + Utility.byte2HexStr(tlv));

                    } else
                        AppLog.e(TAG, "getCardData:" + Integer.toHexString(tag) + ", fails");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String tvrString = "";

        byte[] temp;
        if (tagOfF55 != null) {
            for (Map.Entry<Integer, String> tlvx : tagOfF55.entrySet()) {
                int tag = tlvx.getKey();
                String tags = Integer.toHexString(tag);
                String value = tlvx.getValue();
                if (tags.equals("84")) {
                    transactionAid = value;
                }

                if (value.length() > 0) {
                    temp = iso55.appendF55(tag, value);
                    tvrString += Utility.byte2HexStr(temp);

                    if (temp != null)
                        AppLog.d("EMV", Utility.byte2HexStr(temp));
                }
            }
        }


        final String finalTlv = tvrString;
        final String panSequenceNumber = Utility.getPanSequenceNumber(finalTlv);

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
        Bundle bundle = new Bundle();
        bundle.putBoolean(ConstIPBOC.inputOnlineResult.onlineResult.KEY_isOnline_boolean, true);

        if (!TextUtils.isEmpty(responseCode)) {
            bundle.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_respCode_String, responseCode);
        }

        if (!TextUtils.isEmpty(approvalCode)) {
            bundle.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_authCode_String, approvalCode);
        }

        if (!TextUtils.isEmpty(emvData)) {
            bundle.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String, emvData);
        }

        try {
            emvProcessor.inputOnlineResult(bundle, new OnlineResultHandler.Stub() {
                @Override
                public void onProccessResult(int result, Bundle data) throws RemoteException {
                    switch (result) {
                        case ConstOnlineResultHandler.onProccessResult.result.TC:
                            log("TC");
                            listener.onlineProcessSuccess();
                            break;
                        case ConstOnlineResultHandler.onProccessResult.result.Online_AAC:
                            log("Online AAC");
                            listener.onlineProcessRefuse();
                            break;
                        case ConstOnlineResultHandler.onProccessResult.result.TERMINATE:
                            log("Online Failure, Terminate");
                            listener.onlineProcessTerminate();
                            break;
                        default:
                            log("Error");
                            listener.onError(result);
                            break;
                    }
                }
            });
        } catch (Exception ex) {
            err(ex);
            listener.onError(-1);
        }
    }

    public void confirmCardNumber() {
        log("confirmCardNumber()");
    }

    private void processInsertOrTAPCardData(CardAction cardAction, PosCheckCardListener listener) {
        log("processInsertOrTAPCardData");
        Bundle emvIntent = new Bundle();

        if (cardAction == CardAction.INSERT) {
            emvIntent.putInt(ConstIPBOC.startEMV.intent.KEY_cardType_int, ConstIPBOC.startEMV.intent.VALUE_cardType_smart_card);
        } else if (cardAction == CardAction.TAP) {
            emvIntent.putInt(ConstIPBOC.startEMV.intent.KEY_cardType_int, ConstIPBOC.startEMV.intent.VALUE_cardType_contactless);
        }

        emvIntent.putLong(ConstIPBOC.startEMV.intent.KEY_authAmount_long, txnAmount);
        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_merchantName_String, merchantName);
        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_merchantId_String, merchantId);
        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_terminalId_String, terminalID);
        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isSupportQ_boolean, ConstIPBOC.startEMV.intent.VALUE_supported);
        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isSupportSM_boolean, ConstIPBOC.startEMV.intent.VALUE_supported);
        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isQPBOCForceOnline_boolean, ConstIPBOC.startEMV.intent.VALUE_unforced);
        emvIntent.putByte(ConstIPBOC.startEMV.intent.KEY_transProcessCode_byte, transProcessCode);
        emvIntent.putBoolean("isSupportPBOCFirst", false);

        if (cashBackAmount != 0) {
            emvIntent.putString("otherAmount", String.valueOf(cashBackAmount));
        }

        try {
            emvProcessor.startEMV(ConstIPBOC.startEMV.processType.full_process, emvIntent, new EMVHandler.Stub() {
                @Override
                public void onRequestAmount() throws RemoteException {
                    log("onRequestAmount()");
                    //this is a deprecated method and will not be called
                }

                @Override
                public void onSelectApplication(List<Bundle> appList) throws RemoteException {
                    log("onSelectApplication");
                    List<String> apps = new ArrayList<>();

                    for (Bundle aidBundle : appList) {
                        String aidName = aidBundle.getString("aidName");
                        String aid = aidBundle.getString("aid");
                        String aidLabel = aidBundle.getString("aidLabel");
                        log("AID Name=" + aidName + " | AID Label=" + aidLabel + " | AID=" + aid);
                        apps.add(aidName);
                    }

                    handler.post(() -> listener.onSelectApplication(apps));
                }

                @Override
                public void onConfirmCardInfo(Bundle info) throws RemoteException {
                    log("onConfirmCardInfo");
                    emvProcessor.importCardConfirmResult(ConstIPBOC.importCardConfirmResult.pass.allowed);
                    handler.post(() -> {
                        try {
                            listener.onCardData(cardAction, toCardData(info, true));
                        } catch (Exception ex) {
                            err(ex);
                        }
                    });
                }

                @Override
                public void onRequestInputPIN(boolean isOnlinePin, int retryTimes) throws RemoteException {
                    log("onRequestInputPIN");
                    handler.post(() -> listener.onPinRequested(isOnlinePin, retryTimes));
                }

                @Override
                public void onConfirmCertInfo(String certType, String certInfo) throws RemoteException {
                    log("onConfirmCertInfo"); //not used
                }

                @Override
                public void onRequestOnlineProcess(Bundle aaResult) throws RemoteException {
                    log("onRequestOnlineProcess");
                    cvmResult = aaResult.getInt(ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_CTLS_CVMR_int);
                    log("CVM Result: " + cvmResult);
                    writeEmvTagsOnFile();
                    handler.post(() -> {
                        listener.onRequestOnlineProcess();
                    });
                }

                @Override
                public void onTransactionResult(int result, Bundle data) throws RemoteException {
                    log("onTransactionResult");

                    String msg = "";
                    boolean isFallback = false;

                    switch (result) {
                        case ConstPBOCHandler.onTransactionResult.result.AARESULT_TC:
                            msg = "Transaction Approved Offline";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.AARESULT_AAC:
                            msg = "Transaction Declined Offline";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_NO_APP:
                            if (cardAction == CardAction.INSERT) {
                                isFallback = true;
                            }

                            msg = "Please Swipe Card";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_COMPLETE:
                            msg = "EMV Complete";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_OTHER_ERROR:
                            msg = "EMV Other Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_FALLBACK:
                            msg = "Please Swipe Card";
                            if (cardAction == CardAction.INSERT) {
                                isFallback = true;
                            }
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_DATA_AUTH_FAIL:
                            msg = "EMV Data Auth Fail";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_APP_BLOCKED:
                            msg = "EMV Application is Blocked";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_NOT_ECCARD:
                            msg = "Not an ECC Card";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_UNSUPPORT_ECCARD:
                            msg = "Unsupported Card";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_AMOUNT_EXCEED_ON_PURELYEC:
                            msg = "EMV amount exceeded";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_SET_PARAM_ERROR:
                            msg = "EMV set Param Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_PAN_NOT_MATCH_TRACK2:
                            msg = "EMV PAN not Match with Track2";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_CARD_HOLDER_VALIDATE_ERROR:
                            msg = "Card Holder Validation Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_PURELYEC_REJECT:
                            msg = "Pure Lyce Reject";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_BALANCE_INSUFFICIENT:
                            msg = "Balance Insufficient";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_AMOUNT_EXCEED_ON_RFLIMIT_CHECK:
                            msg = "Balance Insufficient";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_CARD_BIN_CHECK_FAIL:
                            msg = "Please Swipe Card";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_CARD_BLOCKED:
                            msg = "EMV Card is Blocked";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_MULTI_CARD_ERROR:
                            msg = "Multi Cards Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_BALANCE_EXCEED:
                            msg = "EMV Balance Exceed";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_GACERR_GACCMD:
                            msg = "GAC Response Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_TIMEOUT_TRY_AGAIN:
                            msg = "Please try again";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_RFCARD_PASS_FAIL:
                            msg = "Tap Card Failure";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_IN_QPBOC_PROCESS:
                            msg = "QPBOC is Processing";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.EMV_SEE_PHONE:
                            msg = "Please Check the result on phone";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_AAC:
                            msg = "Card Declined";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_ERROR:
                            msg = "Card Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_TC:
                            msg = "Offline Approved";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_CONT:
                            msg = "Insert or Swipe Card";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_NO_APP:
                            msg = "No Application";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_NOT_CPU_CARD:
                            msg = "Not a CPU Card";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.QPBOC_ABORT:
                            msg = "Card not support.";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.PAYPASS_COMPLETE:
                            msg = "Paypass Complete";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.PAYPASS_EMV_TC:
                            msg = "Paypass EMV TC";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.PAYPASS_EMV_AAC:
                            msg = "Paypass Result Refuse";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.PAYPASS_EMV_ERROR:
                            msg = "Paypass EMV Error";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.PAYPASS_END_APP:
                            msg = "Paypass End App";
                            break;
                        case ConstPBOCHandler.onTransactionResult.result.PAYPASS_TRYOTHER:
                            msg = "Paypass Try Other";
                            break;
                    }

                    final String finalMsg = msg;
                    boolean finalIsFallback = isFallback;
                    log(msg);
                    handler.post(() -> {
                        if (finalIsFallback) {
                            listener.onFallback();
                        } else {
                            listener.onEmvError(result, data, finalMsg);
                        }
                    });
                }
            });
        } catch (Exception ex) {
            log("Exception: " + ex.getMessage());
            err(ex);
            handler.post(() -> {
                listener.onEmvError(-1, null, ex.getMessage());
            });
        }
    }

    public static final String EMV_TAG_FILE = "emvtag.txt";
    private static final int NUM_TAGS = 60;
    private String emvTags[][] = {
            {"4F		", "AID                    "},
            {"57		", "TRACK2_EQ_DATA         "},
            {"5A		", "APPL_PAN               "},
            {"5F24	", "EXPIRY_DATE            "},
            {"5F2A	", "TRANS_CURCY_CODE       "},
            {"5F30	", "SERVICE_CODE           "},
            {"5F34	", "APPL_PAN_SEQNUM        "},
            {"81		", "AMOUNT_AUTH            "},
            {"82	", "APPL_INTCHG_PROF       "},
            {"86		", "ISSUER_SCRIPT_CMD      "},
            {"89		", "AUTH_CODE              "},
            {"8A		", "AUTH_RESP_CODE         "},
            {"8C		", "CDOL1                  "},
            {"8D		", "CDOL2                  "},
            {"8E	", "CVM_LIST               "},
            {"8F		", "CA_PK_INDEX(ICC)       "},
            {"91		", "ISS_AUTH_DATA          "},
            {"94		", "AFL                    "},
            {"93		", "SGN_SAD                "},
            {"95	", "TVR                    "},
            {"97		", "TDOL                   "},
            {"9A		", "TRANS_DATE             "},
            {"9C		", "TRANS_TYPE             "},
            {"9B	", "TSI                    "},
            {"9F02	", "AMT_AUTH_NUM           "},
            {"9F03	", "OTHER_AMT              "},
            {"9F07	", "APPL_USE_CNTRL         "},
            {"9F08	", "APP_VER_NUM            "},
            {"9F09	", "TERM_VER_NUM           "},
            {"9F0D	", "IAC_DEFAULT            "},
            {"9F0E	", "IAC_DENIAL             "},
            {"9F0F	", "IAC_ONLINE             "},
            {"9F10	", "ISSUER_APP_DATA        "},
            {"9F12	", "PREFERRED_NAME         "},
            {"9F15	", "MERCHANT_CAT_CODE      "},
            {"9F18	", "ISSUER_SCRIPT_ID       "},
            {"9F1A	", "TERM_COUNTY_CODE       "},
            {"9F1B	", "TERM_FLOOR_LIMIT       "},
            {"9F1D	", "TERM_RISKMGMT_DATA     "},
            {"9F1E	", "IFD_SER_NUM            "},
            {"9F23	", "UC_OFFLINE_LMT         "},
            {"9F26	", "AC9                    "},
            {"9F27	", "CRYPT_INFO_DATA        "},
            {"9F2A	", "TRANS_CURCY_CODE       "},
            {"9F33	", "TERM_CAP               "},
            {"9F34	", "CVM_RESULTS            "},
            {"9F36	", "APP_TXN_COUNTER        "},
            {"9F37	", "UNPREDICT_NUMBER       "},
            {"9F38	", "PDOL                   "},
            {"9F39	", "POS_ENT_MODE           "},
            {"9F3A	", "AMT_REF_CURR           "},
            {"9F3C	", "TRANS_REF_CURR         "},
            {"9F45	", "DATA_AUTH_CODE         "},
            {"9F49	", "DDOL                   "},
            {"9F4A	", "SDA_TAGLIST            "},
            {"9F4B	", "DYNAMIC_APPL_DATA      "},
            {"9F4C	", "ICC_DYNAMIC_NUM        "},
            {"9F53	", "TRAN_CURR_CODE         "},
            {"9F5B	", "ISS_SCRIPT_RES         "},
            {"71		", "ISUER_SCRPT_TEMPL_71   "},
            {"72		", "ISUER_SCRPT_TEMPL_72   "},
    };

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

                    int inTagList[] = new int[NUM_TAGS];

                    //convert the tag list to set of integers
                    for (int iTag = 0; iTag < NUM_TAGS; iTag++) {
                        tagName = emvTags[iTag][TAG_NAME].trim();
                        int tag = Integer.valueOf(tagName, 16);
                        inTagList[iTag] = tag;
                    }

                    //by now we have the tlv string start writing on the file
                    File currentDir = context.getFilesDir();
                    String path = currentDir.getPath();
                    path += "/Secured";

                    File emvFileDir = new File(path);

                    if (!emvFileDir.exists())
                        emvFileDir.mkdirs();

                    File emvFile = new File(path, EMV_TAG_FILE);

                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(emvFile));

                    String tagDesc = "";
                    String data = "";
                    String writeLine = "";

                    Map<Integer, String> tlvData = extractAndBuildMapFromTags(inTagList);

                    int inTag = 0;
                    String sTag = "";

                    for (Map.Entry<Integer, String> tlvx : tlvData.entrySet()) {
                        inTag = tlvx.getKey();
                        tagName = Integer.toHexString(inTag);
                        if ((tagName.length() & 0x01) == 0x01)
                            tagName = "0" + tagName;

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

    private Map<Integer, String> extractAndBuildMapFromTags(int[] tagList) {
        String TAG = "EMV55";
        byte[] tlv;

        Map<Integer, String> tagMap = new HashMap<>();
        ISO8583u iso55 = new ISO8583u();

        try {
            //load emv TLV data here
            for (int tag : tagList) {
                String tagS = Integer.toHexString(tag);
                tlv = emvProcessor.getCardData(Integer.toHexString(tag).toUpperCase());
                if (null != tlv && tlv.length > 0)
                    tagMap.put(tag, Utility.byte2HexStr(tlv));  // build up the field 55
                else
                    AppLog.e(TAG, "getCardData:" + Integer.toHexString(tag) + ", fails");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tagMap;
    }

    public void checkPowerUp() {
        try {
            if (!smartCardReader.powerUp()) {
                forceRemoveCard();
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void smartCardProcess(byte[] apduRequest, boolean isProcessFinished, SmartCardListener listener) {

        try {
            //checkPowerUp();

            byte[] apduResult = smartCardReader.exchangeApdu(apduRequest);
            String result = Utility.byte2HexStr(apduResult);
            log(result);

            if (result.endsWith("9000")) {
                result = result.substring(0, result.length() - 4);
                listener.onAPDUSuccess(result);

                if (isProcessFinished) {
                    beep(100);
                }

            } else {
                beep(100);
                listener.onCheckCardError("Pin Verification Failed");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            err(ex);
            handler.post(() -> listener.onCheckCardError(ex.getMessage()));
        }
    }

    public CVMResult analyseCVMResult(boolean icContactLess) {
        int tag = 0x9f34;
        byte tlv[] = null;

        if (icContactLess) {
            if (cvmResult == ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_CTLS_CVMR_NO_CVM) {
                return CVMResult.NO_CMV_REQUIRED;
            } else if (cvmResult == ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_CTLS_CVMR_CVM_PIN) {
                return CVMResult.ENCTRYPTED_PIN_ONLINE;
            } else if (cvmResult == ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_CTLS_CVMR_CVM_SIGN) {
                return CVMResult.SIGNATURE;
            } else if (cvmResult == ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_CTLS_CVMR_CVM_CDCVM) {
                return CVMResult.NO_CMV_REQUIRED;
            } else {
                return CVMResult.UNKNOWN;
            }
        }

        try {
            tlv = emvProcessor.getCardData(Integer.toHexString(tag).toUpperCase());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        String TLV = Utility.byte2HexStr(tlv);

        if (TLV.equals(""))
            return CVMResult.UNKNOWN;

        String cvmResultByte = TLV.substring(0, 2);
        String cvmResultEnd = TLV.substring(5, 6);

        if (cvmResultEnd.equals("2")) {
            if (cvmResultByte.equals("01") || cvmResultByte.equals("41"))
                return CVMResult.PLAIN_PIN_BY_ICC;
            else if (cvmResultByte.equals("02") || cvmResultByte.equals("42"))
                return CVMResult.ENCTRYPTED_PIN_ONLINE;
            else if (cvmResultByte.equals("03"))
                return CVMResult.PLAIN_PIN_ICC_AND_SIGNATURE;
            else if (cvmResultByte.equals("04") || cvmResultByte.equals("44"))
                return CVMResult.ENCRYPTED_PIN_BY_ICC;
            else if (cvmResultByte.equals("05"))
                return CVMResult.ENCRYPTED_PIN_BY_ICC_ABD_SIGNATURE;
            else if (cvmResultByte.equals("1F"))
                return CVMResult.NO_CMV_REQUIRED;
        } else {
            if (cvmResultByte.substring(1, 2).equals("2"))
                return CVMResult.ENCTRYPTED_PIN_ONLINE;
            else if (cvmResultByte.equals("1E"))
                return CVMResult.SIGNATURE;
            else if (cvmResultByte.equals("1F"))
                return CVMResult.NO_CMV_REQUIRED;
            else {
                return CVMResult.SIGNATURE;
            }
        }

        return CVMResult.UNKNOWN;
    }

    public void clearMasterKey(int masterKey) {
        log("clearMasterKey() masterKey: " + masterKey);

    }

    /**
     * Call this function to cancel emv flow
     */
    public void stopEmvFlow() {
        try {
            log("stopEmvFlow()");
            isCardChecking = false;
            emvProcessor.stopCheckCard();
            emvProcessor.abortEMV();
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

    public void smartCardProcess2(String pin, SmartCardListener listener) {
        try {
            if (!smartCardReader.powerUp()) {
                forceRemoveCard();
                return;
            }
            byte[] apduCommandBytes = APDU.ContructAPDU(APDU_SELECT_APP, TLE_AID);
            byte[] apduResult = smartCardReader.exchangeApdu(apduCommandBytes);
            String result = Utility.byte2HexStr(apduResult);
            log(result);

            if (result.endsWith("9000")) {
                //get the serial number here
                String cardSerialNumber = result.substring(0, result.length() - 4);
                log("Card SerialNumber = " + result);

                //get the pin verification mode
                apduCommandBytes = APDU.ContructAPDU(APDU_GET_PIN_VERIF_MODE, null);
                apduResult = smartCardReader.exchangeApdu(apduCommandBytes);
                result = Utility.byte2HexStr(apduResult);
                log("RESULT 1 = " + result);

                if (result.endsWith("9000")) {
                    String pinVerfivicationMode = result.substring(0, result.length() - 4);

                    //Generating PIN block
                    apduCommandBytes = APDU.ContructAPDU(APDU_GET_PIN_VERFICATION, pin);
                    apduResult = smartCardReader.exchangeApdu(apduCommandBytes);
                    result = Utility.byte2HexStr(apduResult);
                    log("RESULT 2 = " + result);

                    //get the verified pin block
                    if (result.endsWith("9000")) {
                        String tlePINBlock = result.substring(0, result.length() - 4);

                        if (pinVerfivicationMode.equals("01")) //pin verification is online
                            //currentTransaction.encryptedPINBlock = tlePINBlock;

                            apduCommandBytes = APDU.ContructAPDU(APDU_GET_COUNTER, null);
                        apduResult = smartCardReader.exchangeApdu(apduCommandBytes);
                        result = Utility.byte2HexStr(apduResult);
                        log("RESULT 3 = " + result);
                    }
                } else {
                    listener.onCheckCardError("Getting PIN verification mode failed, Aborting!");
                    beep(100);
                    return;
                }
                smartCardReader.powerDown();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            err(ex);
            handler.post(() -> listener.onCheckCardError(ex.getMessage()));
        }
    }

    public void isCardIn(CardInListener listener) {
        log("isCardIn()");
        if (smartCardReader != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        while (smartCardReader.isCardIn()) {
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

    private void forceRemoveCard() {
        try {
            if (smartCardReader != null) {
                while (smartCardReader.isCardIn()) {
                    SleepMe(2000);
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    protected void SleepMe(int mSeconds) {
        try {
            Thread.sleep(mSeconds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private CardData toCardData(Bundle track, boolean isEmv) {
        log("toCardData");
        String track1 = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK1_String);
        String track2 = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK2_String);
        String track3 = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK3_String);

        //-- process track2 data --
        if (!TextUtils.isEmpty(track2)) {
            if (track2.contains("F")) {
                track2 = track2.substring(0, track2.length() - 1);
            }

            if (track2.length() > 37) {
                track2 = track2.substring(0, 37);
            }

            //if (!isEmv && (track2.length() < 37)) {
            //    track2 = Formatter.fillInBack("0", track2, 36);
            //}

            track2 = track2.replace("D", "=");
        }
        //-------------------------

        CardData c = new CardData();
        c.setPan(track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_PAN_String));
        c.setTrack1(track1);
        c.setTrack2(track2);
        c.setTrack3(track3);
        c.setServiceCode(track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_SERVICE_CODE_String));
        c.setExpiryDate(track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_EXPIRED_DATE_String));

        try {
            if (isEmv) {
                c.setCardHolderName(asciiToString(byte2HexStr(emvProcessor.getCardData("5F20"))));
                c.setAid(byte2HexStr(emvProcessor.getCardData(Integer.toHexString(79).toUpperCase())));
            } else {
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
        boolean isInsert = false;
        try {
            int waitCounter = 0;
            while (!smartCardReader.isCardIn()) {
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
            pinPad.loadMainKey(mainKeyId, Utility.hexStr2Byte(key), null);
            log("loadMasterKey: success");
        } catch (Exception ex) {
            err(ex);
        }
    }

    public void loadWorkKey(int mainKeyId, int workKeyId, String key) {
        log("loadWorkKey [mainKeyId:" + mainKeyId + "| workKeyId:" + workKeyId + " | key: " + key + "]");
        try {
            pinPad.loadWorkKey(PinpadKeyType.PINKEY, mainKeyId, workKeyId, Utility.hexStr2Byte(key), null);
            log("loadWorkKey: success");
        } catch (Exception ex) {
            err(ex);
        }
    }

    public boolean isPinKeyExists(int keyId) {
        log("isPinKeyExists " + keyId);
        try {
            boolean isExists = pinPad.isKeyExist(PinpadKeyType.PINKEY, keyId);
            log("isExists: " + isExists);
            return isExists;
        } catch (Exception ex) {
            err(ex);
        }

        return false;
    }

    public void clearWorkerKey(int workKeyId) {
        log("clearWorkerKey: workKeyId = " + workKeyId);
        try {
            pinPad.clearKey(workKeyId, PinpadKeyType.PINKEY);
        } catch (Exception ex) {
            err(ex);
        }
    }

    public ISerialPort getSerialPort() {
        return serialPort;
    }

    public IExternalSerialPort getExternalSerialPort() {
        return externalSerialPort;
    }

    public IPinpad getPinPad() {
        return pinPad;
    }

    public IPrinter getPrinter() {
        return printer;
    }

    public ECRCom getEcrCom() {
        return ecrCom;
    }

    public IDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public String getSerialNo(){
        try {
            return deviceInfo.getSerialNo();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "NA";
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
            return;
        }

        log("startPrintingThread");
        printingThreadStarted = true;

        new Thread() {
            @Override
            public void run() {
                while (shouldRunPintThread) {
                    if (!printQueue.isEmpty()) {
                        if (!isPrinterBusy) {
                            Print p = printQueue.get(0);
                            if (p.getPrintType() == Print.PRINT_DATA_BUILDER) {
                                isPrinterBusy = true;
                                PrintListener printListener = p.getPrintListener();

                                printDataBuilder(p.getPrintDataBuilder(), new PrintListener() {
                                    @Override
                                    public void onPrintFinished() {
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(printListener::onPrintFinished);
                                        }
                                    }

                                    @Override
                                    public void onPrintError(PrintError printError) {
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(() -> printListener.onPrintError(printError));
                                        }
                                    }
                                });
                            } else if (p.getPrintType() == Print.PRINT_TYPE_ISO) {
                                isPrinterBusy = true;
                                PrintListener printListener = p.getPrintListener();
                                printISO(p.getIsoSentType(), p.getIsoData(), new PrintListener() {
                                    @Override
                                    public void onPrintFinished() {
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(printListener::onPrintFinished);
                                        }
                                    }

                                    @Override
                                    public void onPrintError(PrintError printError) {
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(() -> printListener.onPrintError(printError));
                                        }
                                    }
                                });
                            } else if (p.getPrintType() == Print.PRINT_TYPE_IMAGE) {
                                isPrinterBusy = true;
                                PrintListener printListener = p.getPrintListener();
                                printBitmap(p.getBitmap(), new PrintListener() {
                                    @Override
                                    public void onPrintFinished() {
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(printListener::onPrintFinished);
                                        }
                                    }

                                    @Override
                                    public void onPrintError(PrintError printError) {
                                        isPrinterBusy = false;
                                        printQueue.remove(0);
                                        if (printListener != null) {
                                            handler.post(() -> printListener.onPrintError(printError));
                                        }
                                    }
                                });
                            }
                        } else {
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
        Bundle f16Normal = new Bundle();
        f16Normal.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.SMALL_16_16);
        f16Normal.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, FileUtils.getInstance().getFontNormal().toString());
        Bundle f24Normal = new Bundle();
        f24Normal.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);
        f24Normal.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, FileUtils.getInstance().getFontNormal().toString());
        Bundle f32Normal = new Bundle();
        f32Normal.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.LARGE_32_32);
        f32Normal.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, FileUtils.getInstance().getFontNormal().toString());

        Bundle f16Bold = new Bundle();
        f16Bold.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.SMALL_16_16);
        f16Bold.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, FileUtils.getInstance().getFontBold().toString());
        Bundle f24Bold = new Bundle();
        f24Bold.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);
        f24Bold.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, FileUtils.getInstance().getFontBold().toString());
        Bundle f32Bold = new Bundle();
        f32Bold.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.LARGE_32_32);
        f32Bold.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, FileUtils.getInstance().getFontBold().toString());

        try {
            for (PrintItem p : printDataBuilder.getPrintItems()) {
                Bundle font = null;
                if (p.getFontSize() == PrintItem.FontSize.SIZE_16) {
                    font = f16Normal;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_16_BOLD) {
                    font = f16Bold;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_24) {
                    font = f24Normal;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_24_BOLD) {
                    font = f24Bold;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_32) {
                    font = f32Normal;
                } else if (p.getFontSize() == PrintItem.FontSize.SIZE_32_BOLD) {
                    font = f32Bold;
                }

                if (p.getItemType() == PrintItem.ItemType.TEXT_LINE) {
                    printer.addTextInLine(font, p.getLeftText(), p.getMiddleText(), p.getRightText(), 0);
                } else if (p.getItemType() == PrintItem.ItemType.DOT_LINE) {
                    printer.addText(font, p.getLeftText());
                } else if (p.getItemType() == PrintItem.ItemType.BANK_LOGO) {
                    InputStream is = p.getImgInputStream();
                    if (is != null) {
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();

                        Bundle fmtImage = new Bundle();
                        fmtImage.putInt("offset", 0);
                        fmtImage.putInt("width", 380); // smaller then actual, will print the setting
                        fmtImage.putInt("height", 100);
                        printer.addImage(fmtImage, buffer);
                    }
                } else if (p.getItemType() == PrintItem.ItemType.SPACE) {
                    printer.feedLine(p.getSpaceLines());
                }
            }

            printer.startPrint(new PrinterListener.Stub() {
                @Override
                public void onFinish() throws RemoteException {
                    log("print finished");
                    listener.onPrintFinished();
                }

                @Override
                public void onError(int error) throws RemoteException {
                    log("print error");
                    listener.onPrintError(PrintError.convert(error));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void printISO(int sentType, byte[] data, PrintListener listener) {
        String formatted = "", prLine = "";
        int remLineLen = 27, len = 0, lastLine = 0;
        int start = 0;
        String dataStr = Utility.byte2HexStr(data);
        dataStr = dataStr.substring(4, dataStr.length());

        //put space between each byte
        while (true) {
            String sb = dataStr.substring(start, start + 2);
            formatted = formatted + sb + " ";
            start += 2;

            if (start >= dataStr.length())
                break;
        }

        len = formatted.length();
        start = 0;

        try {
            // bundle format for AddTextInLine
            Bundle fmtAddTextInLine = new Bundle();
            fmtAddTextInLine.putInt(PrinterConfig.addTextInLine.FontSize.BundleName, PrinterConfig.addTextInLine.FontSize.NORMAL_24_24);
            //fmtAddTextInLine.putString(PrinterConfig.addTextInLine.GlobalFont.BundleName, PrinterFonts.path + PrinterFonts.FONT_BROADW);

            if (sentType == 1)
                printer.addTextInLine(fmtAddTextInLine, "| SEND PACKET", "", "", 0);
            else
                printer.addTextInLine(fmtAddTextInLine, "| RECEIVE PACKET", "", "", 0);

            while (true) {
                if ((start + remLineLen) < len)
                    prLine = formatted.substring(start, start + remLineLen);
                else {
                    prLine = formatted.substring(start, start + (len - start));
                    lastLine = 1;
                }
                start += remLineLen;

                prLine = "| " + prLine + "|";

                printer.addTextInLine(fmtAddTextInLine, prLine, "", "", 0);

                if (lastLine == 1) {
                    break;
                }
            }
            printer.feedLine(4);
            printer.startPrintInEmv(new PrinterListener.Stub() {
                @Override
                public void onFinish() throws RemoteException {
                    listener.onPrintFinished();
                }

                @Override
                public void onError(int error) throws RemoteException {
                    listener.onPrintError(PrintError.convert(error));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will print given bitmap by resizing the given bitmap to 380px with maintaining aspect ratio.
     *
     * @param original
     * @param listener
     */
    private void printBitmap(Bitmap original, PrintListener listener) {
        log("printBitmap");
        try {
            Bitmap scaleBitmap = scale(original);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaleBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageInByte = baos.toByteArray();

            Bundle format = new Bundle();
            format.putInt("offset", 0);
            format.putInt("width", scaleBitmap.getWidth());
            format.putInt("height", scaleBitmap.getHeight());

            scaleBitmap.recycle();

            printer.addImage(format, imageInByte);
            printer.startPrint(new PrinterListener.Stub() {
                @Override
                public void onFinish() throws RemoteException {
                    log("printBitmap -> onFinish");
                    listener.onPrintFinished();
                }

                @Override
                public void onError(int error) throws RemoteException {
                    log("printBitmap -> onError: " + error);
                    listener.onPrintError(PrintError.convert(error));
                }
            });
        } catch (Exception ex) {
            err(ex);
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

    }

    public void setActivity(Activity activity) {
        log("setActivity() activity: " + activity);

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
        if (!isBinded) {
            Intent intent = new Intent();
            intent.setAction("com.vfi.smartpos.device_service");
            intent.setPackage("com.vfi.smartpos.deviceservice");
            isBinded = context.bindService(intent, connListener, Context.BIND_AUTO_CREATE);
            log("bindDeviceService: " + isBinded);
        }
    }


    private ServiceConnection connListener = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            log("ServiceConnection- onServiceConnected");

            //initialize the reference variables
            deviceService = IDeviceService.Stub.asInterface(service);
            try {
                emvProcessor = deviceService.getEMV();
                pinPad = deviceService.getPinpad(1);        //get the primary pin pad
                scannerCamFront = deviceService.getScanner(1);
                scannerCamRear = deviceService.getScanner(0);
                printer = deviceService.getPrinter();
                beeper = deviceService.getBeeper();
                serialPort = deviceService.getSerialPort("usb-rs232");
                externalSerialPort = deviceService.getExternalSerialPort();
                deviceInfo = deviceService.getDeviceInfo();
                smartCardReader = deviceService.getInsertCardReader(0);

                //Disable Home Button
                Bundle bundle = new Bundle();
                bundle.putBoolean("HOMEKEY", false);
                deviceInfo.setSystemFunction(bundle);


                updateAIDsAndRIDs();

                posDeviceInitialized = true;

                if (ecrCom != null) {
                    //Re initialize on this state if already exists
                    ecrCom = new ECRCom();
                }
            } catch (RemoteException ex) {
                err(ex);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            log("ServiceConnection- onServiceDisconnected");
            deviceService = null;
            posDeviceInitialized = false;
            isCardChecking = false;
        }
    };

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
        clearAIDs();

        final int CONTACT_AID = 1;
        final int CONTACTLESS_AID = 2;

        for (String aid : getContactAids()) {
            updateAID(CONTACT_AID, aid);
        }

        for (String aid : getContactLessAids()) {
            updateAID(CONTACTLESS_AID, aid);
        }

        for (String rid : getRids()) {
            updateRID(rid);
        }
    }

    /**
     * Update AID
     *
     * @param cardType
     * @param aid
     */
    public void updateAID(int cardType, String aid) {
        try {
            log("updateAID");
            emvProcessor.updateAID(1, cardType, aid);
        } catch (Exception ex) {
            err(ex);
        }
    }

    /**
     * Update RID
     *
     * @param rid
     */
    public void updateRID(String rid) {
        try {
            log("updateRID");
            emvProcessor.updateRID(ConstIPBOC.updateRID.operation.append, rid);
        } catch (Exception ex) {
            err(ex);
        }
    }

    /**
     * Clear all aids
     */
    public void clearAIDs() {
        log("clearAIDs");
        try {
            boolean isSuccess = emvProcessor.updateAID(3, 1, null);
            log("Clear AID (smart AID):" + isSuccess);
            isSuccess = emvProcessor.updateAID(3, 2, null);
            log("Clear AID (CTLS):" + isSuccess);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private List<String> getContactLessAids() {
        log("getContactLessAids");
        List<String> aids = new ArrayList<>();
        //aid visa ctls
        String visa1_ctls = context.getString(R.string.VISA_AID_CTLS_A0000000031010);
        String visa3_ctls = context.getString(R.string.VISA_AID_CTLS_A0000000032010);
        String visa4_ctls = context.getString(R.string.VISA_AID_CTLS_A000000003101001);
        String visa5_ctls = context.getString(R.string.VISA_AID_CTLS_A000000003101002);
        String visa_DF19 = "DF1906" + visaAid.getFLAmt();
        String visa_DF20 = "DF2006" + visaAid.getTxnAmt();  //Maximum CTLS amount.
        String visa_DF21 = "DF2106" + visaAid.getCVMLAmt(); //CTLS pin support amount.
        String visa_9F33 = "9F3303" + visaAid.getCTLSPinSupportValue();
        String visa_9F66 = "9F6604" + visaAid.getTTQCTLSValue();
        aids.add(visa1_ctls + visa_DF19 + visa_DF20 + visa_DF21 + visa_9F33 + visa_9F66);
        aids.add(visa3_ctls + visa_DF19 + visa_DF20 + visa_DF21 + visa_9F33 + visa_9F66);
        aids.add(visa4_ctls + visa_DF19 + visa_DF20 + visa_DF21 + visa_9F33 + visa_9F66);
        aids.add(visa5_ctls + visa_DF19 + visa_DF20 + visa_DF21 + visa_9F33 + visa_9F66);

        //aid master ctls
        String master1_ctls = context.getString(R.string.MASTER_AID_CTLS_A0000000041010);
        String master2_ctls = context.getString(R.string.MASTER_AID_CTLS_A0000001523010);
        String master_DF19 = "DF1906" + masterAid.getFLAmt();
        String master_DF20 = "DF2006" + masterAid.getTxnAmt();
        String master_DF21 = "DF2106" + masterAid.getCVMLAmt();
        String master_9F33 = "9F3303" + masterAid.getCTLSPinSupportValue();
        String master_9F66 = "9F6604" + masterAid.getTTQCTLSValue();
        aids.add(master1_ctls + master_DF19 + master_DF20 + master_DF21 + master_9F33 + master_9F66);
        aids.add(master2_ctls + master_DF19 + master_DF20 + master_DF21 + master_9F33 + master_9F66);

        //aid amex ctls
        String amex1_ctls = context.getString(R.string.AMEX_AID_CTLS_A00000002501);
        String amex_DF19 = "DF1906" + amexAid.getFLAmt();
        String amex_DF20 = "DF2006" + amexAid.getTxnAmt();
        String amex_DF21 = "DF2106" + amexAid.getCVMLAmt();
        String amex_9F33 = "9F3303" + amexAid.getCTLSPinSupportValue();
        String amex_9F66 = "9F6604" + amexAid.getTTQCTLSValue();
        aids.add(amex1_ctls + amex_DF19 + amex_DF20 + amex_DF21 + amex_9F33 + amex_9F66);
        return aids;
    }

    private List<String> getContactAids() {
        log("getContactAids");
        List<String> aids = new ArrayList<>();
        //aid for visa
        String visa_9F33 = "9F3303" + visaAid.getPinSupportValue();
        String visa_9F66 = "9F6604" + visaAid.getTTQValue();
        aids.add(context.getString(R.string.VISA_AID_A0000000031010) + visa_9F33 + visa_9F66);
        aids.add(context.getString(R.string.VISA_AID_A0000000032010) + visa_9F33 + visa_9F66);
        //aid for master
        String master_9F33 = "9F3303" + masterAid.getPinSupportValue();
        String master_9F66 = "9F6604" + masterAid.getTTQValue();
        aids.add(context.getString(R.string.MASTER_AID_A0000000041010) + master_9F33 + master_9F66);
        //aids.add(context.getString(R.string.MASTER_AID_A0000000043060) + master_9F33 + master_9F66);
        aids.add(context.getString(R.string.MASTER_AID_A0000000046000) + master_9F33 + master_9F66);
        aids.add(context.getString(R.string.MASTER_AID_A0000000101030) + master_9F33 + master_9F66);
        //aid for AMEX
        String amex_9F33 = "9F3303" + amexAid.getPinSupportValue();
        String amex_9F66 = "9F6604" + amexAid.getTTQValue();
        aids.add(context.getString(R.string.AMEX_AID_A00000002501) + amex_9F33 + amex_9F66);
        //aid for Diners
        String diners_9F33 = "9F3303" + dinersAid.getPinSupportValue();
        String diners_9F66 = "9F6604" + dinersAid.getTTQValue();
        aids.add(context.getString(R.string.DINERS_AID_A0000001523010) + diners_9F33 + diners_9F66);
        //aid for JCB
        String jcb_9F33 = "9F3303" + jcbAid.getPinSupportValue();
        String jcb_9F66 = "9F6604" + jcbAid.getTTQValue();
        aids.add(context.getString(R.string.JCB_AID_A00000006510) + jcb_9F33 + jcb_9F66);
        //aid for cup
        aids.add(context.getString(R.string.CUP_AID_A000000333010101));
        aids.add(context.getString(R.string.CUP_AID_A000000333010102));
        aids.add(context.getString(R.string.CUP_AID_A000000333010103));
        aids.add(context.getString(R.string.CUP_AID_A000000333010106));
        aids.add(context.getString(R.string.CUP_AID_A0000003241010));
        return aids;
    }

    private List<String> getRids() {
        log("getRids");
        List<String> rids = new ArrayList<>();
        //visa rid
        rids.add(context.getString(R.string.VISA_RID_1));
        rids.add(context.getString(R.string.VISA_RID_2));
        rids.add(context.getString(R.string.VISA_RID_3));
        rids.add(context.getString(R.string.VISA_RID_4));
        rids.add(context.getString(R.string.VISA_RID_5));
        rids.add(context.getString(R.string.VISA_RID_6));
        rids.add(context.getString(R.string.VISA_RID_7));
        rids.add(context.getString(R.string.VISA_RID_8));
        rids.add(context.getString(R.string.VISA_RID_9));
        rids.add(context.getString(R.string.VISA_RID_10));
        rids.add(context.getString(R.string.VISA_RID_11));
        //master rid
        rids.add(context.getString(R.string.MASTER_RID_1));
        rids.add(context.getString(R.string.MASTER_RID_2));
        rids.add(context.getString(R.string.MASTER_RID_3));
        rids.add(context.getString(R.string.MASTER_RID_4));
        rids.add(context.getString(R.string.MASTER_RID_5));
        rids.add(context.getString(R.string.MASTER_RID_6));
        rids.add(context.getString(R.string.MASTER_RID_7));
        rids.add(context.getString(R.string.MASTER_RID_8));
        rids.add(context.getString(R.string.MASTER_RID_9));
        //AMEX rid
        rids.add(context.getString(R.string.AMEX_RID_1));
        rids.add(context.getString(R.string.AMEX_RID_2));
        rids.add(context.getString(R.string.AMEX_RID_3));
        rids.add(context.getString(R.string.AMEX_RID_4));
        rids.add(context.getString(R.string.AMEX_RID_5));
        rids.add(context.getString(R.string.AMEX_RID_6));
        rids.add(context.getString(R.string.AMEX_RID_7));
        rids.add(context.getString(R.string.AMEX_RID_8));
        rids.add(context.getString(R.string.AMEX_RID_9));
        rids.add(context.getString(R.string.AMEX_RID_10));
        rids.add(context.getString(R.string.AMEX_RID_11));
        rids.add(context.getString(R.string.AMEX_RID_12));
        rids.add(context.getString(R.string.AMEX_RID_13));
        rids.add(context.getString(R.string.AMEX_RID_14));
        rids.add(context.getString(R.string.AMEX_RID_15));
        //cup rid
        rids.add(context.getString(R.string.CUP_RID_1));
        rids.add(context.getString(R.string.CUP_RID_2));
        rids.add(context.getString(R.string.CUP_RID_3));
        rids.add(context.getString(R.string.CUP_RID_4));
        rids.add(context.getString(R.string.CUP_RID_5));
        rids.add(context.getString(R.string.CUP_RID_6));
        rids.add(context.getString(R.string.CUP_RID_7));
        rids.add(context.getString(R.string.CUP_RID_8));
        rids.add(context.getString(R.string.CUP_RID_9));
        //diners rid
        rids.add(context.getString(R.string.DINERS_RID_1));
        rids.add(context.getString(R.string.DINERS_RID_2));
        rids.add(context.getString(R.string.DINERS_RID_3));
        rids.add(context.getString(R.string.DINERS_RID_4));
        rids.add(context.getString(R.string.DINERS_RID_5));
        rids.add(context.getString(R.string.DINERS_RID_6));
        rids.add(context.getString(R.string.DINERS_RID_7));
        rids.add(context.getString(R.string.DINERS_RID_8));
        //jcb rid
        rids.add(context.getString(R.string.JCB_RID_1));
        rids.add(context.getString(R.string.JCB_RID_2));
        rids.add(context.getString(R.string.JCB_RID_3));
        return rids;
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
        try {
            beeper.startBeep(mSeconds);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
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
        if (BuildConfig.DEBUG) {
            ex.printStackTrace();
        }
    }

    private void log(String msg) {
        if (BuildConfig.DEBUG) {
            AppLog.i(TAG, msg);
        }
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

        throw new RuntimeException(
                "PosDevice class not correctly instantiated. Please call PosDevice.Builder().setContext(context).build(); in the Application class onCreate.");
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
