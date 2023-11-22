package com.epic.pos.ui.settings.tlekeydownload;

import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.config.ResponseCodes;
import com.epic.pos.crypto.MAC;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.iso.ISOMsgBuilder;
import com.epic.pos.iso.modal.request.KeyDownloadTransaction;
import com.epic.pos.iso.modal.response.KeyDownloadResponse;
import com.epic.pos.tle.Dukpt;
import com.epic.pos.tle.KeyManager;
import com.epic.pos.tle.TLEKeyGeneration;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.util.APDU;
import com.epic.pos.util.ErrorCodeManager;
import com.epic.pos.util.UiUtil;
import com.epic.pos.util.Utility;
import com.epic.pos.util.ValidatorUtil;
import com.epic.pos.util.spcrypto.DESCrypto;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.listener.SmartCardListener;


import org.jpos.iso.ISOUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity.*;


public class KeyDownloadPresenter extends BasePresenter<KeyDownloadContract.View> implements KeyDownloadContract.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;
    KeyDownloadTransaction keyDownloadRequest;
    private static final String TAG = "KeyDownloadPresenter";

    public static final int VISA_MASTER_TMK_INDEX = 3;
    public static final int VISA_MASTER_MAC_INDEX = 4;

    public static final int AMEX_TMK_INDEX = 5;
    public static final int AMEX_MAC_INDEX = 6;

    public static final int VISA_MASTER_SESSION_KEY_INDEX = 29;
    //public static final int AMEX_SESSION_KEY_INDEX = 28; //dhanushi requested to comment this

    public static final int VISA_MASTER_EF_INDEX = 30;
    public static final int AMEX_EF_INDEX = 33;

    public static final int VISA_MASTER_COUNTER_INDEX = 31;
    public static final int VISA_MASTER_KEY_ID_INDEX = 32;

    public static final int AMEX_COUNTER_INDEX = 34;
    public static final int AMEX_KEY_ID_INDEX = 35;

    private final String ONLINE_PIN = "02";

    public String amount;
    public String currency;
    public String pinVerificationMode;
    public String cardSerialNumber;
    public String cardKeyDownloadCounter;
    String keyDownloadObject;
    String pin;
    boolean isInitial = true;
    String encryptionAlgo = null;
    String clearKey;
    Host host;
    int issuerNo;
    Issuer issuer;

    @Inject
    public KeyDownloadPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
        keyDownloadRequest = new KeyDownloadTransaction();

    }

    @Override
    public void onClosing() {
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(true);
    }

    @Override
    public void waitCardInsert(Host selectedHost) {
        try {
            if (PosDevice.getInstance().checkCardInsertOrNot()) {
                selectApp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    int id = 0;
    String GS = "~", RS = "#", str63 = "";
    HashMap<Integer, KeyDownloadTransaction> tidMidMap = new HashMap<>();

    public void initializeData(Host selectedHost) {
        this.pin = repository.getTLEPwd();
        host = selectedHost;
        issuerNo = host.getBaseIssuer();

        repository.getIssuerById(issuerNo, issuerData -> {
            issuer = issuerData;

            repository.getAllTerminalsByHost(host.getHostID(), terminalList -> {
                for (Terminal terminal : terminalList) {
                    repository.getEnableMerchantById(terminal.getMerchantNumber(), merchant -> {
                        if (merchant != null) {
                            String TID = terminal.getTerminalID();
                            String MID = merchant.getMerchantID();
                            tidMidMap.put(terminal.getID(), new KeyDownloadTransaction(TID, MID, terminal.getSecureNII()));
                            str63 += TID + GS + MID + RS;
                        }
                        id++;

                        if (id == terminalList.size() && terminalList.size() > 0) {
                            keyDownloadRequest.setTerminalData(str63.substring(0, str63.length() - 1));
                        }
                    });
                }
            });
        });
    }

    @Override
    public void selectApp() {
        List sortedKeys = new ArrayList(tidMidMap.keySet());
        Collections.sort(sortedKeys);
        keyDownloadRequest.setTid(tidMidMap.get(sortedKeys.get(0)).getTid());
        keyDownloadRequest.setMid(tidMidMap.get(sortedKeys.get(0)).getMid());
        keyDownloadRequest.setNii(tidMidMap.get(sortedKeys.get(0)).getNii());

        PosDevice.getInstance().checkPowerUp();
        byte[] apduCommandBytes = APDU.ContructAPDU(APDU_SELECT_APP, TLE_AID);

        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                if (isInitial) {
                    getSerialNumber();
                }
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
    }

    @Override
    public void getSerialNumber() {
        byte[] apduCommandBytes = APDU.ContructAPDU(APDU_GET_SERIAL, null);
        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                if (isInitial) {
                    cardSerialNumber = result;
                    getPinVerificationMode();
                }
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
    }

    @Override
    public void getPinVerificationMode() {
        byte[] apduCommandBytes = APDU.ContructAPDU(APDU_GET_PIN_VERIF_MODE, null);
        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                if (isInitial) {
                    pinVerificationMode = result;
                    keyDownloadRequest.setOnlinePin(pinVerificationMode.equals("01"));
                    pinVerification();

                }
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
    }

    @Override
    public void pinVerification() {
        byte[] apduCommandBytes = APDU.ContructAPDU(APDU_GET_PIN_VERFICATION, pin);
        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                if (isInitial) {
                    keyDownloadRequest.setPin(result);
                    getPinCounter();
                }
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
    }

    @Override
    public void getPinCounter() {
        byte[] apduCommandBytes = APDU.ContructAPDU(APDU_GET_COUNTER, null);
        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                cardKeyDownloadCounter = result;
                getTLEHeader();
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
    }


    String tleHeader = "";

    public void getTLEHeader() {
        int issuerNumber = 1;

        repository.getTLEData(issuerNumber, tleData -> {
            cardSerialNumber = ValidatorUtil.getInstance().zeroPadString(cardSerialNumber, 16, true);
            int serialLen = cardSerialNumber.length();
            String sSerialLenHex = Integer.toHexString(serialLen);

            tleHeader = tleData.getEAlgo() +
                    tleData.getUKID() +
                    tleData.getKSize() +
                    tleData.getMAlgo() +
                    tleData.getDMTR() +
                    tleData.getAID() +
                    pinVerificationMode +
                    cardKeyDownloadCounter +
                    sSerialLenHex +
                    cardSerialNumber;

            repository.incrementTraceNumber();
            keyDownloadRequest.setTraceNumber(repository.getTraceNumber());
            keyDownloadRequest.setTleHeader(tleHeader);
            getMac();
        });
    }

    @Override
    public void getMac() {
        String hexPacket = ISOMsgBuilder.getInstance().macISOMessage(keyDownloadRequest);
        AppLog.i("AAAAAAAAA ", "hexPacket =" + hexPacket);
        byte[] SHAresult = MAC.makeSHA1ForPacket(hexPacket);
        String shaDigest = Utility.byte2HexStr(SHAresult) + "80000000";
        String apdu = APDU_GET_MAC + 18 + shaDigest;
        byte[] apduCommandBytes = Utility.hexStr2Byte(apdu);

        PosDevice.getInstance().smartCardProcess(apduCommandBytes, true, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                keyDownloadRequest.setMac(result);
                keyDownloadRequest();
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
    }


    @Override
    public void keyDownloadRequest() {
        mView.showLoader("Key Download", "Transaction being process…");
        repository.keyDownloadRequest(issuer, keyDownloadRequest, new Repository.KeyDownloadListener() {
            @Override
            public void onReceived(KeyDownloadResponse keyDownloadResponse) {
                PosDevice.getInstance().checkPrinterStatus(() -> {
                    if (keyDownloadResponse.getResponseCode().equalsIgnoreCase(ResponseCodes.SUCCESS)) {
                        if (saveExtractedEncryptedKeys(keyDownloadResponse.getKeyField())) {
                            mView.hideLoader();
                            mView.showDialogMessage("TLE Key Download", "Key Saving Success", new UiUtil.SuccessDialogListener() {
                                @Override
                                public void onOkClicked() {
                                    mView.onSuccessKeyDownload();
                                }
                            });
                        } else {
                            mView.hideLoader();
                            mView.onTxnFailed("Key Saving Failed");
                        }

                    } else {
                        mView.hideLoader();
                        mView.onTxnFailed(ErrorCodeManager.getInstance().getCodeMessage(keyDownloadResponse.getResponseCode()));
                    }
                });
            }

            @Override
            public void onError(String error) {

            }

            @Override
            public void onError(Throwable throwable) {
                mView.hideLoader();
                mView.onTxnFailedAndRetry(Const.MSG_TXN_REQUEST_ERROR);
            }

            @Override
            public void onCompleted() {

            }
        });
    }


    @Override
    public String getEncryptedMethod() {
        byte[] apduCommandBytes = APDU.ContructAPDU(APDU_GET_ENC_METHOD, null);
        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                encryptionAlgo = result;
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
        return encryptionAlgo;
    }

    @Override
    public String getClearKeys(String encryptedMethod, String key) {
        byte[] apduCommandBytes = APDU.ContructAPDU(encryptedMethod, key);

        PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
            @Override
            public void onAPDUSuccess(String result) {
                clearKey = result;
            }

            @Override
            public void onCheckCardError(String errorMsg) {
                mView.onTxnFailed(errorMsg);
            }
        });
        return clearKey;
    }

    byte[] data;
    String tmk, mac, keyID;

    public boolean saveExtractedEncryptedKeys(String tleData) {
        tleData = tleData.substring(40, tleData.length());

        int start = 0;
        int indexDelim = tleData.indexOf("01");
        start = indexDelim + 2;

        AppLog.i(TAG, "Getting Key ID");
        indexDelim = tleData.indexOf("01", start);
        keyID = tleData.substring(start, indexDelim);
        start = indexDelim + 2;

        AppLog.i(TAG, "Parsing TMK");
        indexDelim = tleData.indexOf("01", start);
        tmk = tleData.substring(start, indexDelim);
        start = indexDelim + 2;

        AppLog.i(TAG, "Parsing TMK check value");
        indexDelim = tleData.indexOf("01", start);
        String tmkCheck = tleData.substring(start, indexDelim);
        start = indexDelim + 2;

        AppLog.i(TAG, "Parsing MAC");
        indexDelim = tleData.indexOf("01", start);
        mac = tleData.substring(start, indexDelim);
        start = indexDelim + 2;

        AppLog.i(TAG, "Parsing MAC check value");
        indexDelim = tleData.indexOf("01", start);
        if (indexDelim == -1)
            indexDelim = tleData.length();
        String macCheck = tleData.substring(start, indexDelim);

        if ((!checkKey(tmk, tmkCheck)) || (!checkKey(mac, macCheck))) {
            return false;

        } else {
            tmk = decryptKeys(tmk);
            mac = decryptKeys(mac);

            AppLog.i("KEYYYYYYYYYY ", "TMK = " + tmk);
            AppLog.i("KEYYYYYYYYYY ", "MAC = " + mac);
            AppLog.i("KEYYYYYYYYYY ", "HOST = " + host);
            tmk = setOddParityKey(tmk);
            mac = setOddParityKey(mac);

            //store the key ID for visa in 32
            keyID = Utility.asciiToString(keyID);
            data = Utility.hexStr2Byte(keyID);

            String dummy = "0000000000000000";

            repository.getIssuerContainsHostRaw(issuerNo, new DbHandler.GetIssuerHostListener() {
                @Override
                public void onReceived(int hostId) {
                    hostId = hostId - 1;
                    AppLog.i("TTTTTTTT", " hostId = " + hostId);

                    if (hostId == 0) //for visa master
                    {
                        KeyManager.setKeyInSlot(VISA_MASTER_TMK_INDEX, tmk);
                        KeyManager.setKeyInSlot(VISA_MASTER_MAC_INDEX, mac);

                        TLEKeyGeneration.inStoreSecureAria(data, VISA_MASTER_KEY_ID_INDEX);  //key id

                        AppLog.i("TTTTTTTT", host + " tmk = " + tmk);
                        AppLog.i("TTTTTTTT", "mac = " + mac);
                        AppLog.i("TTTTTTTT", "data = " + keyID);

                        //reset the initial counter
                        data = Utility.hexStr2Byte(dummy);
                        AppLog.i("TTTTTTTT", "data = " + dummy);
                        TLEKeyGeneration.inStoreSecureAria(data, VISA_MASTER_EF_INDEX);      //e/f
                        TLEKeyGeneration.inStoreSecureAria(data, VISA_MASTER_COUNTER_INDEX);  //counter
                        data = Utility.hexStr2Byte(tmk);
                        AppLog.i("TTTTTTTT", "tmk = " + tmk);
                    } else if (hostId == 1) {
                        KeyManager.setKeyInSlot(AMEX_TMK_INDEX, tmk);
                        KeyManager.setKeyInSlot(AMEX_MAC_INDEX, mac);

                        TLEKeyGeneration.inStoreSecureAria(data, AMEX_KEY_ID_INDEX);  //key id

                        AppLog.i("TTTTTTTT", "tmk = " + tmk);
                        AppLog.i("TTTTTTTT", "mac = " + mac);
                        AppLog.i("TTTTTTTT", "data = " + keyID);

                        data = Utility.hexStr2Byte(dummy);

                        AppLog.i("TTTTTTTT", "data = " + dummy);
                        TLEKeyGeneration.inStoreSecureAria(data, AMEX_EF_INDEX);
                        TLEKeyGeneration.inStoreSecureAria(data, AMEX_COUNTER_INDEX);
                        data = Utility.hexStr2Byte(tmk);
                        AppLog.i("TTTTTTTT", "tmk = " + tmk);
                    }

                    KeyManager.setKeyInSlot(VISA_MASTER_SESSION_KEY_INDEX, ISOUtil.byte2hex(data));
                    Dukpt dkpt = Dukpt.getInstance(keyDownloadRequest.getTid(), hostId);
                    dkpt.inInitKeyDukpt(tmk);
                }
            });
            return true;
        }
    }

    private String decryptKeys(String key) {
        String method = "";
        try {
            selectApp();

            if (pinVerificationMode.equals(ONLINE_PIN)) {
                pinVerification();
            }
            encryptionAlgo = getEncryptedMethod();
            key = Utility.asciiToString(key);

            if (encryptionAlgo.equals("01"))
                method = APDU_3DES_DEC;

            else if (encryptionAlgo.equals("02"))
                method = APDU_RSA_DEC;

            return getClearKeys(method, key);

        } catch (Exception ex) {
            return ex.toString();
        }
    }

    String setOddParityKey(String hexKey) {
        int i = 0;
        byte b = 0;

        byte[] keyBytes = Utility.hexStr2Byte(hexKey);

        for (i = 0; i < keyBytes.length; i++) {
            b = keyBytes[i];
            keyBytes[i] = (byte) ((b & (byte) 0xFE) | ((((b >> 1) ^ (b >> 2) ^ (b >> 3) ^ (b >> 4) ^ (b >> 5) ^ (b >> 6) ^ (b >> 7)) ^ (byte) 0x01) & (byte) 0x01));
        }

        String key = Utility.byte2HexStr(keyBytes);
        return key;
    }

    String strCheckValue = "";
    String keyCheckValue = "";

    private boolean checkKey(String key, String keyCheck) {
        byte apduCommandBytes[] = null;
        String dummyText = "0000000000000000";

        try {
            isInitial = false;
            selectApp();

            if (pinVerificationMode.equals(ONLINE_PIN))
                pinVerification();

            encryptionAlgo = getEncryptedMethod();
            key = Utility.asciiToString(key);

            if (encryptionAlgo.equals("01"))
                apduCommandBytes = APDU.ContructAPDU(APDU_3DES_DEC, key);

            else if (encryptionAlgo.equals("02"))
                apduCommandBytes = APDU.ContructAPDU(APDU_RSA_DEC, key);

            PosDevice.getInstance().smartCardProcess(apduCommandBytes, false, new SmartCardListener() {
                @Override
                public void onAPDUSuccess(String result) {
                    byte[] checkByteValues = new byte[0];
                    try {
                        checkByteValues = DESCrypto.encrypt3Des(dummyText, result);
                        if (checkByteValues != null) {
                            strCheckValue = Utility.byte2HexStr(checkByteValues);
                            keyCheckValue = Utility.asciiToString(keyCheck);

                            AppLog.i("IIIIIIIIIIIIIIIII ", "strCheckValue = " + strCheckValue);
                            AppLog.i("IIIIIIIIIIIIIIIII ", "keyCheckValue = " + keyCheckValue);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCheckCardError(String errorMsg) {
                    mView.onTxnFailed(errorMsg);
                }
            });
            return strCheckValue.startsWith(keyCheckValue);

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void retryTransaction() {
        isInitial = true;
        selectApp();
    }
}
