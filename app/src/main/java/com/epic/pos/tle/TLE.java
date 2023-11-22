package com.epic.pos.tle;

import android.util.Base64;
import com.epic.pos.util.AppLog;

import com.epic.pos.common.Const;
import com.epic.pos.crypto.MAC;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.util.ISO8583u;
import com.epic.pos.util.Utility;
import com.epic.pos.util.ValidatorUtil;
import com.epic.pos.util.spcrypto.DESCrypto;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.epic.pos.common.Const.MSG_PLEASE_DOWNLOAD_TLE_KEY;
import static com.epic.pos.service.SocketConnectionService.responseTPDU;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadPresenter.AMEX_MAC_INDEX;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadPresenter.AMEX_TMK_INDEX;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadPresenter.VISA_MASTER_MAC_INDEX;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadPresenter.VISA_MASTER_SESSION_KEY_INDEX;
import static com.epic.pos.ui.settings.tlekeydownload.KeyDownloadPresenter.VISA_MASTER_TMK_INDEX;


public class TLE extends TLEKeyGeneration {
    private static final int TYPE_ASC = 2;
    private static int EMV_CARD = 1;
    private static int CTLS_CARD = 3;
    private static TLE myInstance = null;

    private String transactionRandomNumber = "";
    private String sessionKey = null;
    private static String base64EncodedString = "";
    private String transactionMAC = "";
    public static int tleHeaderLength = 0;
    List<Integer> shouldEncryptFields = new ArrayList<>();

    public static TLE getInstance() {
        if (myInstance == null)
            myInstance = new TLE();
        return myInstance;
    }


    public String getTransactionMAC() {
        return transactionMAC;
    }

    public String getTid() {
        return tid;
    }

    public interface GetEncryptedFiled {
        void onReceived(String isoMessage);

        void onTLEError(String errorMsg);
    }

    public List<Integer> getShouldEncryptFields() {
        return shouldEncryptFields;
    }

    public void getEncryptedPacket(TLEData tleData, byte[] dataPacket, GetEncryptedFiled listener) {
        shouldEncryptFields.clear();
        AppLog.i("ZZZZZZZZZZZZZZZ", "VALUE getIssuer_number = " + tleData.getIssuerId());

        DbHandler.getInstance().getTLEData(tleData.getIssuerId(), tle -> {
            szTHAID = tle.getAID();
            szTHDMTR = tle.getDMTR();
            szTHEAlgo = tle.getEAlgo();
            szTHMAlgo = tle.getMAlgo();
            szTHUKID = tle.getUKID();
            szTHVSN = tle.getVSN();

            DbHandler.getInstance().getIssuerContainsHostRaw(tleData.getIssuerId(), hostId -> {

                TLEKeyGeneration.host = hostId - 1;
                String key;

                if (host == 0 || host == 2) { //for visa master{}
                    key = KeyManager.getKeyFromSlot(VISA_MASTER_TMK_INDEX);
                } else {
                    key = KeyManager.getKeyFromSlot(AMEX_TMK_INDEX);
                }
                AppLog.i("HHHHHHHHHHHHHHHHHHHHH", "TMK KEY  = " + key);

                if (key != null) {
                    AppLog.i("HHHHHHHHHHHHHHHHHHHHH", "DB HOST = " + host);
                    DbHandler.getInstance().getTerminalByHost(hostId, new DbHandler.GetTerminalListener() {
                        @Override
                        public void onReceived(Terminal terminal) {
                            TLEKeyGeneration.tid = terminal.getTerminalID();

                            DbHandler.getInstance().getTFIData(tleData.getIssuerId(), cursor -> {
                                while (cursor.moveToNext()) {
                                    for (int i = 2; i < cursor.getColumnCount(); i++) {
                                        int fieldValue = cursor.getInt(i);

                                        if (fieldValue == 1) {
                                            String columnName = cursor.getColumnName(i).substring(5);
                                            shouldEncryptFields.add(Integer.parseInt(columnName));
                                            //Log.i("HHHHHHHHHHHHHHHHHHHHH", "shouldEncryptFields = " + Integer.parseInt(columnName));
                                        }
                                    }
                                }
                                String encryptedPacket = encryptPacket(tid, tleData, dataPacket);
                                listener.onReceived(encryptedPacket);
                            });
                        }
                    });
                } else {
                    listener.onTLEError(MSG_PLEASE_DOWNLOAD_TLE_KEY);
                }
            });
        });
    }

    private String getMacKey() {
        String macKey = null;

        if (host == 0) //for visa master
            macKey = KeyManager.getKeyFromSlot(VISA_MASTER_MAC_INDEX);

        else if (host == 1)
            macKey = KeyManager.getKeyFromSlot(AMEX_MAC_INDEX);

        return macKey;
    }

    public String encryptPacket(String tid, TLEData tleData, byte[] dataPacket) {
        if (Const.PRINT_ISO_MSG)
            printISOPacket(dataPacket, 1);

        ISO8583u originalPacket = new ISO8583u();
        Dukpt dkpt = Dukpt.getInstance(tid, host);
        dkpt.inUniqueKeyPerTxn(szTHUKID);

//        if (host == 0) //for visa master
//            sessionKey = KeyManager.getKeyFromSlot(VISA_MASTER_SESSION_KEY_INDEX);

        //dhanushi requested to comment this
        //else if (host  == 1)
        //    sessionKey = KeyManager.getKeyFromSlot(AMEX_SESSION_KEY_INDEX);

        sessionKey = KeyManager.getKeyFromSlot(VISA_MASTER_SESSION_KEY_INDEX);

        if (sessionKey == null) {
            return null;
        }


        if (!originalPacket.unpack(dataPacket))
            return null;

        String origHexPacket = Utility.byte2HexStr(dataPacket);
        String bitmap = origHexPacket.substring(18, 18 + 16);
        String macKey = getMacKey();
        transactionMAC = generateMacForPacket(origHexPacket, macKey);

        //get the list of fields which have been set in the packet
        List<Integer> origPacketFields = getFieldList(bitmap);

        //get the list of fields which have been configured for encryption
        int field, length;
        String sField, sLength, asciiOrNot;
        String fieldData;
        String fieldSetStringforTLE = "";
        String fieldForTLE = "";

        for (Integer fieldIndex : origPacketFields) {
            if (!shouldEncryptFields.contains(fieldIndex))
                continue;

            field = fieldIndex;
            sField = ValidatorUtil.getInstance().zeroPadString(String.valueOf(field), 3); //get the formatted field number

            fieldData = originalPacket.getUnpack(field);
            byte data[] = originalPacket.getFieldCust(field, fieldData);

            //get the data as its in the packet
            String dataInPacket = Utility.byte2HexStr(data);

            asciiOrNot = "0";
            int fieldType = originalPacket.fieldType(field);

            if (fieldType == TYPE_ASC)
                asciiOrNot = "1";

            if ((fieldIndex == 22) || (fieldIndex == 23) || (fieldIndex == 24))
                dataInPacket = dataInPacket.substring(1, dataInPacket.length()); //strip off the first char

            if ((fieldIndex == 35) && (dataInPacket.length() > 37) && ((tleData.getChipStatus() == EMV_CARD) || (tleData.getChipStatus() == CTLS_CARD)))
                dataInPacket = dataInPacket.substring(0, dataInPacket.length() - 1);

            if ((fieldIndex == 35) && (dataInPacket.length() > tleData.getTrack2Length())) {
                dataInPacket = dataInPacket.substring(0, tleData.getTrack2Length());
            }

            //get the field length and formatted
            length = dataInPacket.length();

            if (fieldIndex == 2 && tleData.getPanLength() > 0)
                length = tleData.getPanLength();

            sLength = ValidatorUtil.getInstance().zeroPadString(String.valueOf(length), 3);
            fieldData = dataInPacket;
            fieldForTLE = sField + asciiOrNot + sLength + fieldData;

            fieldSetStringforTLE += fieldForTLE;
        }

        transactionRandomNumber = getRandomForTLE();
        fieldSetStringforTLE = transactionRandomNumber + fieldSetStringforTLE;
        byte[] encryptedData = null;

        try {
            encryptedData = DESCrypto.encrypt3DesWithCBCTLEPadding(fieldSetStringforTLE, sessionKey);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        base64EncodedString = Base64.encodeToString(encryptedData, Base64.DEFAULT);
        return base64EncodedString;
    }


    public ISOMsg decryptPacket(ISOMsg isoMsg) {
        String field57Data = isoMsg.getString(57).substring(tleHeaderLength);
        //here we have encrypted data found we start decrypting the data
        byte[] Base64Decoded = Base64.decode(field57Data, Base64.DEFAULT);
        String hexDecoded = Utility.byte2HexStr(Base64Decoded);

        byte[] decrypted = null;

        try {
            decrypted = DESCrypto.decrypt3DesCBCTLENoPadding(hexDecoded, sessionKey);

            byte[] b = new byte[2];
            b[0] = 0x30;

            for (int i = 0; i < 8; i++) {
                decrypted[i] = (byte) (decrypted[i] ^ b[0]);
            }
            String decryptedString = Utility.byte2HexStr(decrypted);

            int field;
            int length;
            String data;
            String asciiOrNot = "0";

            Map<Integer, String> clearPacketMap = new HashMap<>();
            String randomNumber = decryptedString.substring(0, 6);

            if (!randomNumber.equals(transactionRandomNumber)) {
                //showToast("ICV Faliure");
                // errorBeep();
                return null;
            }
            decryptedString = decryptedString.substring(6);

            String sNumPadded = decryptedString.substring(decryptedString.length() - 1);
            int numPadded = Integer.valueOf(sNumPadded, 16);
            numPadded *= 2;

            int offset = 0;

            if (numPadded < decryptedString.length())
                decryptedString = decryptedString.substring(0, decryptedString.length() - numPadded);
            else
                offset = decryptedString.length();

            while (offset < (decryptedString.length() - 1)) {
                //extracting  the field number
                field = Integer.parseInt(decryptedString.substring(offset, offset + 3));
                offset += 3;

                //extracting the ascii converted or not
                asciiOrNot = decryptedString.substring(offset, offset + 1);
                offset++;

                //extracting the length of the field
                length = Integer.parseInt(decryptedString.substring(offset, offset + 3));
                offset += 3;

                //extracting the data field
                data = decryptedString.substring(offset, offset + length);

                if (asciiOrNot.equals("1"))    // yes ascii encoded and decode to a utf-8 string
                    data = Utility.asciiToString(data);

                offset += length;

                clearPacketMap.put(field, data);
                isoMsg.set(field, data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (Const.PRINT_ISO_MSG) {
            try {
                String response = responseTPDU + ISOUtil.byte2hex(isoMsg.pack());
                printISOPacket(ISOUtil.hex2byte(response), 2);
            } catch (ISOException e) {
                e.printStackTrace();
            }
        }


        return isoMsg;
    }

    String generateMacForPacket(String packet, String macKey) {
        String mac = "";
        String forMacVerification = packet.substring(14, packet.length());
        try {
            byte[] macBytes = MAC.makeSHA1(forMacVerification);
            mac = Utility.byte2HexStr(macBytes);

            mac = mac + "80" + "000000";
            byte[] initVec = new byte[8];
            byte[] macByteArr = Utility.hexStr2Byte(mac);

            //xor the first byte of the init vector
            byte[] selected = Arrays.copyOfRange(macByteArr, 0, 8);
            byte[] result = Utility.xorArrays(initVec, selected);
            result = DESCrypto.encrypt3DesBytes(result, macKey);

            selected = Arrays.copyOfRange(macByteArr, 8, 16);
            result = Utility.xorArrays(result, selected);
            result = DESCrypto.encrypt3DesBytes(result, macKey);

            selected = Arrays.copyOfRange(macByteArr, 16, 24);
            result = Utility.xorArrays(result, selected);
            result = DESCrypto.encrypt3DesBytes(result, macKey);

            mac = Utility.byte2HexStr(result);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return mac;
    }


    public static List<Integer> getFieldList(String bitmapx) {
        List<Integer> fieldList = new ArrayList<>();
        byte[] bitmap = Utility.hexStr2Byte(bitmapx);

        for (int byteIndex = 7; byteIndex >= 0; byteIndex--) {
            byte curByte = bitmap[byteIndex];
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                if ((curByte & (1 << bitIndex)) == (1 << bitIndex)) {
                    int index = bitIndex + (8 * (7 - byteIndex));
                    index = 64 - index;
                    fieldList.add(index);
                }
            }
        }
        return fieldList;
    }


    private int MAX = 999999;
    private int MIN = 900000;

    private String getRandomForTLE() {
        Random rnd = new Random();
        int rand = rnd.nextInt(MAX - MIN) + MIN;

        String sRand = String.valueOf(rand);
        return sRand;
    }

    public static String calculateMac(byte[] packet, int offset, int length) {
        byte[] tmp = new byte[4096];
        System.arraycopy(packet, 0, tmp, 0, length);
        int len = length - offset;

        int i, j;
        int cnt = (len % 8 != 0) ? (len / 8 + 1) : len / 8;
        byte[] mac = new byte[8];
        Arrays.fill(mac, (byte) 0);

        cnt += offset;
        for (i = offset; i < cnt; i++) {
            for (j = 0; j < 8; j++) {
                mac[j] ^= tmp[i * 8 + j];
            }
        }
        return ISOUtil.byte2hex(mac);
    }

    private void printISOPacket(byte printBytes[], int type) {
        Print p = new Print();
        p.setPrintType(Print.PRINT_TYPE_ISO);
        p.setIsoSentType(type);
        p.setIsoData(printBytes);
        PosDevice.getInstance().addToPrintQueue(p);
    }
}