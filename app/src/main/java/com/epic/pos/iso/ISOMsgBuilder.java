package com.epic.pos.iso;

import static com.epic.pos.common.Const.MSG_PLEASE_DOWNLOAD_TLE_KEY;

import android.content.Context;
import android.text.TextUtils;

import com.epic.pos.common.Const;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.iso.modal.request.BatchUploadRequest;
import com.epic.pos.iso.modal.request.DCCDownloadTransaction;
import com.epic.pos.iso.modal.request.DCCRequest;
import com.epic.pos.iso.modal.request.KeyDownloadTransaction;
import com.epic.pos.iso.modal.request.OfflineSaleRequest;
import com.epic.pos.iso.modal.request.QrRequest;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.request.SaleRequest;
import com.epic.pos.iso.modal.request.SettlementRequest;
import com.epic.pos.iso.modal.request.VoidRequest;
import com.epic.pos.iso.modal.response.BatchUploadResponse;
import com.epic.pos.iso.modal.response.DCCDownloadResponse;
import com.epic.pos.iso.modal.response.DCCResponse;
import com.epic.pos.iso.modal.response.KeyDownloadResponse;
import com.epic.pos.iso.modal.response.OfflineSaleResponse;
import com.epic.pos.iso.modal.response.QrResponse;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.iso.modal.response.SaleResponse;
import com.epic.pos.iso.modal.response.SettlementResponse;
import com.epic.pos.iso.modal.response.VoidResponse;
import com.epic.pos.tle.TLE;
import com.epic.pos.tle.TLEData;
import com.epic.pos.tle.TLEKeyGeneration;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.Utility;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericValidatingPackager;

import java.io.InputStream;


public class ISOMsgBuilder {
    private static String XML_FILE_PATH = "iso/iso87binaryrec.xml";
    private static ISOMsgBuilder instance;
    private static Context context;
    TLE tle;

    /**
     * This method is use to generate void transaction iso message
     *
     * @param transaction
     * @return
     */
    public void settlementISOMessage(SettlementRequest transaction, TLEData tleData, TLE.GetEncryptedFiled listener) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());

            m.set(3, transaction.getProcessingCode());
            m.set(11, transaction.getTraceNumber());
            if (tleData.isTleEnable())
                m.set(24, transaction.getSecureNii());
            else
                m.set(24, transaction.getNii());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());
            m.set(60, transaction.getBatchNo());
            m.set(63, transaction.getTxnCountAndAmount());

            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

            if (tleData.isTleEnable()) {
                String tpdu = transaction.getTpdu().replace(transaction.getNii(), transaction.getSecureNii());
                encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
            } else {
                listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is use to generate void transaction iso message
     *
     * @param transaction
     * @return
     */
    public void batchUploadISOMessage(BatchUploadRequest transaction, TLEData tleData, TLE.GetEncryptedFiled listener) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());

            m.set(2, transaction.getPan());
            m.set(3, transaction.getProcessingCode());
            m.set(4, transaction.getAmount());
            m.set(11, transaction.getTraceNumber());
            m.set(12, transaction.getTime());
            m.set(13, transaction.getDate());
            m.set(14, transaction.getExpDate());
            m.set(22, transaction.getPosEntryMode());
            if (tleData.isTleEnable())
                m.set(24, transaction.getSecureNii());
            else
                m.set(24, transaction.getNii());
            m.set(25, transaction.getPosConditionCode()); // Temporary hardcoded
            m.set(37, transaction.getRrn());
            m.set(38, transaction.getAuthCode());
            m.set(39, transaction.getResponse());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());
            m.set(55, transaction.getEmv());
            m.set(60, transaction.getOriginalTxnData()); // OriginalMTI + Trace No + RRN
            m.set(62, transaction.getInvoice());

            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

            if (tleData.isTleEnable()) {
                String tpdu = transaction.getTpdu().replace(transaction.getNii(), transaction.getSecureNii());
                encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
            } else {
                listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is use to generate void transaction iso message
     *
     * @param transaction
     * @return
     */


    public void DccISOMessage(DCCRequest transaction, TLEData tleData, TLE.GetEncryptedFiled listener) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());

            m.set(2, transaction.getPan());
            m.set(3, transaction.getProcessingCode());
            m.set(4, transaction.getAmount());
            m.set(11, transaction.getTraceNumber());
            m.set(12, transaction.getTime());
            m.set(13, transaction.getDate());
            m.set(14, transaction.getExpDate());
            m.set(22, transaction.getPosEntryMode());
            if (tleData.isTleEnable())
                m.set(24, transaction.getSecureNii());
            else
                m.set(24, transaction.getNii());
            m.set(25, transaction.getPosConditionCode()); // Temporary hardcoded
            m.set(37, transaction.getRrn());
            m.set(38, transaction.getAuthCode());
            m.set(39, transaction.getResponse());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());
            m.set(55, transaction.getEmv());
            m.set(60, transaction.getOriginalTxnData()); // OriginalMTI + Trace No + RRN
            m.set(62, transaction.getInvoice());

            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

            if (tleData.isTleEnable()) {
                String tpdu = transaction.getTpdu().replace(transaction.getNii(), transaction.getSecureNii());
                encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
            } else {
                listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is use to generate void transaction iso message
     *
     * @param transaction
     * @return
     */
    public void offlineSaleISOMessage(OfflineSaleRequest transaction, TLEData tleData, TLE.GetEncryptedFiled listener) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());
            m.set(2, transaction.getPan());
            m.set(3, transaction.getProcessingCode());
            m.set(4, transaction.getAmount());
            m.set(11, transaction.getTraceNumber());
            m.set(12, transaction.getTime());
            m.set(13, transaction.getDate());
            m.set(14, transaction.getExpDate());
            m.set(22, transaction.getPosEntryMode());

            if (tleData.isTleEnable())
                m.set(24, transaction.getSecureNii());
            else
                m.set(24, transaction.getNii());
            m.set(25, transaction.getPosConditionCode()); // Temporary hardcoded
            m.set(38, transaction.getAuthCode());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());
            m.set(55, transaction.getEmv());
            m.set(62, transaction.getInvoice());


            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

            if (tleData.isTleEnable()) {
                String tpdu = transaction.getTpdu().replace(transaction.getNii(), transaction.getSecureNii());
                encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
            } else {
                listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Convert QRRequest to ISO message
     *
     * @param qrRequest
     * @return
     */
    public String qrRequestIsoMessage(QrRequest qrRequest) {
        //not in use
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(qrRequest.getMti());
            m.set(3, qrRequest.getProcessingCode());
            m.set(4, qrRequest.getAmount()
                    .replaceAll(",", "")
                    .replaceAll("\\.", ""));
            m.set(11, qrRequest.getTraceNumber());
            m.set(22, qrRequest.getPosEntryMode());
            m.set(24, qrRequest.getNii());
            m.set(25, qrRequest.getPosConditionCode());
            m.set(41, qrRequest.getTid());
            m.set(42, qrRequest.getMid());
            m.set(49, qrRequest.getCurrencyCode());

            String isoMsgString = qrRequest.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);
            return ISOUtil.hexString(finalISOMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * This method is use to generate void transaction iso message
     *
     * @param transaction
     * @return
     */
    public void voidISOMessage(VoidRequest transaction, TLEData tleData, TLE.GetEncryptedFiled listener) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);


            m.setMTI(transaction.getMti());
            m.set(2, transaction.getPan());
            m.set(3, transaction.getProcessingCode());
            m.set(4, transaction.getAmount()
                    .replaceAll(",", "")
                    .replaceAll("\\.", ""));

            if (!TextUtils.isEmpty(transaction.getCashBackAmount())) {
                m.set(54, transaction.getCashBackAmount()
                        .replaceAll(",", "")
                        .replaceAll("\\.", ""));
            }

            m.set(11, transaction.getTraceNumber());
            m.set(12, transaction.getTxnTime());
            m.set(13, transaction.getTxnDate());
            m.set(14, transaction.getExpDate());
            m.set(22, transaction.getPosEntryMode());
            m.set(24, tleData.isTleEnable() ? transaction.getSecureNii() : transaction.getNii());
            m.set(25, transaction.getPosConditionCode());
            m.set(37, transaction.getRrn());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());
            m.set(62, transaction.getInvoiceNumber());

            if (!TextUtils.isEmpty(transaction.getStudentRefNo())){
                m.set(58, transaction.getStudentRefNo());
            }

            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);


            if (tleData.isTleEnable()) {
                String tpdu = transaction.getTpdu().replace(transaction.getNii(), transaction.getSecureNii());
                encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
            } else {
                listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Convert QrResponse iso message to QrResponse object
     *
     * @param isoMsg
     * @return
     */
    public QrResponse getQrRequestResponseEntity(ISOMsg isoMsg) {
        QrResponse r = new QrResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setAmount(isoMsg.getString(4));
        r.setTraceNumber(isoMsg.getString(11));
        r.setPosEntryMode(isoMsg.getString(22));
        r.setNii(isoMsg.getString(24));
        r.setPosConditionCode(isoMsg.getString(25));
        r.setTid(isoMsg.getString(41));
        r.setMid(isoMsg.getString(42));
        r.setCurrencyCode(isoMsg.getString(49));
        r.setQrUrl(isoMsg.getString(63));

        return r;
    }


    /**
     * Convert void iso response to VoidResponse object
     *
     * @param isoMsg
     * @return
     */
    public VoidResponse getVoidTransactionResponseEntity(ISOMsg isoMsg) {
        VoidResponse r = new VoidResponse();
        r.setMti(isoMsg.getString(0));
        r.setPan(isoMsg.getString(2));
        r.setProcessingCode(isoMsg.getString(3));
        r.setAmount(isoMsg.getString(4));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setExpDate(isoMsg.getString(14));
        r.setPosEntryMode(isoMsg.getString(22));
        r.setNii(isoMsg.getString(24));
        r.setPosConditionCode(isoMsg.getString(25));
        r.setRrn(isoMsg.getString(37));
        r.setApprovalCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));
        r.setMid(isoMsg.getString(42));
        r.setInvoiceNumber(isoMsg.getString(62));

        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setApprovalCode(isoMsg.getString(38));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    /**
     * Convert void iso response to SettlementResponse object
     *
     * @param isoMsg
     * @return
     */
    public SettlementResponse getSettlementTransactionResponseEntity(ISOMsg isoMsg) {
        SettlementResponse r = new SettlementResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setNii(isoMsg.getString(24));
        r.setRrn(isoMsg.getString(37));
        r.setAuthCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));

        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setAuthCode(isoMsg.getString(38));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    /**
     * Convert void iso response to SettlementResponse object
     *
     * @param isoMsg
     * @return
     */
    public BatchUploadResponse getBatchUploadResponseEntity(ISOMsg isoMsg) {
        BatchUploadResponse r = new BatchUploadResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setNii(isoMsg.getString(24));
        r.setRrn(isoMsg.getString(37));
        r.setAuthCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));
        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setAuthCode(isoMsg.getString(38));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }



    public DCCResponse getdccResponseEntity(ISOMsg isoMsg) {
        DCCResponse r = new DCCResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setNii(isoMsg.getString(24));
        r.setRrn(isoMsg.getString(37));
        r.setAuthCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));
        r.setDccdata(isoMsg.getString(63));
        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setAuthCode(isoMsg.getString(38));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }
    /**
     *
     *
     * Convert void iso response to OfflineSaleResponse object
     *
     * @param isoMsg
     * @return
     */
    public OfflineSaleResponse getOfflineSaleResponseEntity(ISOMsg isoMsg) {
        OfflineSaleResponse r = new OfflineSaleResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setNii(isoMsg.getString(24));
        r.setRrn(isoMsg.getString(37));
        r.setAuthCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));

        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setAuthCode(isoMsg.getString(38));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }
    public DCCDownloadResponse getDCCDownloadResponce(ISOMsg isoMsg) {
        DCCDownloadResponse r = new DCCDownloadResponse();
        r.setMti(isoMsg.getString(0));
      //  r.setProcessingCode(isoMsg.getString(3));
      ///  r.setTraceNumber(isoMsg.getString(11));
     //   r.setTime(isoMsg.getString(12));
     //   r.setDate(isoMsg.getString(13));
        r.setNii(isoMsg.getString(24));
     //   r.setRrn(isoMsg.getString(37));
      //  r.setAuthCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));
        r.setDccdata(isoMsg.getString(48));
//        if (isoMsg.hasField(57)) {
//            try {
//                isoMsg = tle.decryptPacket(isoMsg);
//                r.setRrn(isoMsg.getString(37));
//                r.setAuthCode(isoMsg.getString(38));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        return r;
    }
    /**
     * Reversal iso message
     *
     * @param reversal
     * @return
     */
    public void reversalISOMessage(ReversalRequest reversal, TLEData tleData, TLE.GetEncryptedFiled listener) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);
            m.setMTI(reversal.getMti());

            String pan = "";

            if (reversal.isManualSale()) {
                pan = reversal.getPan();
            } else {
                if (!TextUtils.isEmpty(reversal.getTrack2Data())) {
                    pan = reversal.getTrack2Data().split("=")[0];
                }
            }

            m.set(2, pan);
            m.set(3, reversal.getProcessingCode());
            m.set(4, reversal.getTotalAmount()
                    .replaceAll(",", "")
                    .replaceAll("\\.", ""));
            m.set(11, reversal.getTraceNumber());
            m.set(14, reversal.getExpDate());
            m.set(22, reversal.getPosEntryMode());
            m.set(24, tleData.isTleEnable() ? reversal.getSecureNii() : reversal.getNii());
            m.set(25, reversal.getPosConditionCode());
            m.set(41, reversal.getTid());
            m.set(42, reversal.getMid());
            m.set(62, reversal.getInvoiceNumber());

            if (!TextUtils.isEmpty(reversal.getStudentRefNo())){
                m.set(58, reversal.getStudentRefNo());
            }

            if (!TextUtils.isEmpty(reversal.getCashBackAmount())) {
                m.set(54, reversal.getCashBackAmount()
                        .replaceAll(",", "")
                        .replaceAll("\\.", ""));
            }

            if (reversal.isVoidSale()) {
                m.set(12, reversal.getTxnTime());
                m.set(13, reversal.getTxnDate());
                m.set(37, reversal.getRrn());
            } else {
                if (!TextUtils.isEmpty(reversal.getPanSequenceNumber())) {
                  //  m.set(23, reversal.getPanSequenceNumber());
                }

                if (!TextUtils.isEmpty(reversal.getEmvData())) {
                   m.set(55, reversal.getEmvData());
                }
            }

            String isoMsgString = reversal.getTpdu() + ISOUtil.hexString(m.pack());
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
            byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
            byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

            if (tleData.isTleEnable()) {
                String tpdu = reversal.getTpdu().replace(reversal.getNii(), reversal.getSecureNii());
                encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
            } else {
                listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ReversalResponse getReversalTransactionResponseEntity(ISOMsg isoMsg) {
        ReversalResponse r = new ReversalResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setAmount(isoMsg.getString(4));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setExpDate(isoMsg.getString(14));
        r.setNii(isoMsg.getString(24));
        r.setRrn(isoMsg.getString(37));
        r.setApprovalCode(isoMsg.getString(38));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));

        if (isoMsg.hasField(55)) {
            r.setEmvData(isoMsg.getString(55));
        }
        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setApprovalCode(isoMsg.getString(38));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return r;
    }

    /**
     * This method is use to generate sale transaction iso message
     *
     * @param transaction
     * @return
     */
    public void saleISOMessage(SaleRequest transaction, TLEData tleData, TLE.GetEncryptedFiled listener) throws Exception {
        ISOMsg m = new ISOMsg();
        InputStream is = context.getAssets().open(XML_FILE_PATH);
        GenericValidatingPackager packager = new GenericValidatingPackager(is);
        m.setPackager(packager);

        if (transaction.getCardAction() == CardAction.MANUAL.val) {
            m.set(2, transaction.getPan());
            //m.set(12, transaction.getTxnTime());
            //m.set(13, transaction.getTxnDate());
            m.set(14, transaction.getExpDate());
        }

        m.setMTI(transaction.getMti());
        m.set(3, transaction.getProcessingCode());
        m.set(4, transaction.getTotalAmount()
                .replaceAll(",", "")
                .replaceAll("\\.", ""));
        m.set(11, transaction.getTraceNumber());
        m.set(22, transaction.getPosEntryMode());
        m.set(24, tleData.isTleEnable() ? transaction.getSecureNii() : transaction.getNii());
        m.set(25, transaction.getPosConditionCode());

        if(PosDevice.getInstance().isIsamexmsd()){
            transaction.setTrack2Data(PosDevice.getInstance().getAmextrack2());
        }
        m.set(35, transaction.getTrack2Data());


        m.set(49, transaction.getCurrencycode());

        m.set(41, transaction.getTid());
        m.set(42, transaction.getMid());

        if (transaction.isOnlinePinRequested() && !TextUtils.isEmpty(transaction.getPinBlock())) {
            m.set(52, Utility.hexStr2Byte(transaction.getPinBlock()));
        }

        if (!TextUtils.isEmpty(transaction.getCashBackAmount())) {
            m.set(54, transaction.getCashBackAmount()
                    .replaceAll(",", "")
                    .replaceAll("\\.", ""));
        }

        m.set(62, transaction.getInvoiceNumber());

        if (!TextUtils.isEmpty(transaction.getStudentRefNo())) {
            m.set(58, transaction.getStudentRefNo());
        }
        if (transaction.getCardAction() == CardAction.INSERT.val) {
            // 5F34 value
            if (!TextUtils.isEmpty(transaction.getPanSequenceNumber())) {
              // m.set(23, transaction.getPanSequenceNumber());
            }

            m.set(55, transaction.getEmvData());
        } else if (transaction.getCardAction() == CardAction.TAP.val) {
            // 5F34 value
            if (!TextUtils.isEmpty(transaction.getPanSequenceNumber())) {
              //  m.set(23, transaction.getPanSequenceNumber());
            }
            if(!(PosDevice.getInstance().isIsamexmsd())){
            m.set(55, transaction.getEmvData());}
        }
        String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
        byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
        String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
        byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
        byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

        if (tleData.isTleEnable()) {
            String tpdu = transaction.getTpdu().replace(transaction.getNii(), transaction.getSecureNii());
            encryptedISOMessage(tleData, m, finalISOMessage, tpdu, listener);
        } else {
            listener.onReceived(ISOUtil.byte2hex(finalISOMessage));
        }
    }


    public SaleResponse getSaleTransactionResponseEntity(ISOMsg isoMsg) {
        SaleResponse r = new SaleResponse();
        r.setMti(isoMsg.getString(0));
        r.setProcessingCode(isoMsg.getString(3));
        r.setAmount(isoMsg.getString(4));
        r.setTraceNumber(isoMsg.getString(11));
        r.setTime(isoMsg.getString(12));
        r.setDate(isoMsg.getString(13));
        r.setNii(isoMsg.getString(24));
        r.setResponseCode(isoMsg.getString(39));
        r.setTid(isoMsg.getString(41));
        r.setRrn(isoMsg.getString(37));
        r.setApprovalCode(isoMsg.getString(38));

        if (isoMsg.hasField(55)) {
            r.setEmvData(isoMsg.getString(55));
        }
        if (isoMsg.hasField(57)) {
            try {
                isoMsg = tle.decryptPacket(isoMsg);
                r.setRrn(isoMsg.getString(37));
                r.setApprovalCode(isoMsg.getString(38));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return r;
    }

    public void encryptedISOMessage(TLEData tleData, ISOMsg m, byte[] ISOMessage, String tpdu, TLE.GetEncryptedFiled listener) {
        Const.IS_TLE_ENABLE = true;
        tle = TLE.getInstance();
        tle.getEncryptedPacket(tleData, ISOMessage, new TLE.GetEncryptedFiled() {
            @Override
            public void onReceived(String encryptedPacket) {

                try {
                    for (int i : tle.getShouldEncryptFields()) {
                        if (i != 12 && i != 13 && i != 55 )
                            m.unset(i);
                    }
                    m.set(57, TLEKeyGeneration.szTLE_Header(tle.getTid()) + encryptedPacket);
                    m.set(64, tle.getTransactionMAC());

                    String isoMsgString = tpdu + ISOUtil.hexString(m.pack());
                    byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString + "00");
                    String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);
                    byte[] isoMessageArLen = ISOUtil.hex2byte(isoMsgLength);
                    byte[] finalISOMessage = ISOUtil.concat(isoMessageArLen, isoMessageAr);

                    AppLog.i("REEEEEEEEEQUEST", ISOUtil.byte2hex(finalISOMessage));
                    listener.onReceived(ISOUtil.byte2hex(finalISOMessage));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTLEError(String errorMsg) {
                listener.onTLEError(MSG_PLEASE_DOWNLOAD_TLE_KEY);
            }
        });
    }

    /**
     * This method is use to generate MAC download iso message
     *
     * @param transaction
     * @return
     */
    public String macISOMessage(KeyDownloadTransaction transaction) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());
            m.set(3, transaction.getProcessingCode());
            m.set(11, transaction.getTraceNumber());
            m.set(22, transaction.getPosEntryMode());
            m.set(24, transaction.getNii());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());

            if (transaction.isOnlinePin()) {
                m.set(52, transaction.getPin());
            }
            m.set(62, transaction.getTleHeader());

            if (transaction.getTerminalData() != null && transaction.isOnlinePin()) {
                m.set(63, transaction.getTerminalData());
            }

            m.pack();
            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack());
            String msgWithMac = isoMsgString + TLE.calculateMac(ISOUtil.hex2byte(isoMsgString), 5, isoMsgString.length() / 2) + "00";
            byte[] isoMessageAr = ISOUtil.hex2byte(msgWithMac);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);

            return isoMsgLength + msgWithMac;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * This method is use to generate key download iso message
     *
     * @param transaction
     * @return
     */
    public String keyDownloadISOMessage(KeyDownloadTransaction transaction) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());
            m.set(3, transaction.getProcessingCode());
            m.set(11, transaction.getTraceNumber());
            m.set(22, transaction.getPosEntryMode());
            m.set(24, transaction.getNii());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());

            if (transaction.isOnlinePin()) {
                m.set(52, transaction.getPin());
            }
            m.set(62, transaction.getTleHeader());

            if (transaction.getTerminalData() != null && transaction.isOnlinePin()) {
                m.set(63, transaction.getTerminalData());
            }
            m.set(64, transaction.getMac());

            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack()) + "00";
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);

            return isoMsgLength + isoMsgString;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String DCCDownloadISOMessage(DCCDownloadTransaction transaction) {
        try {
            ISOMsg m = new ISOMsg();
            InputStream is = context.getAssets().open(XML_FILE_PATH);
            GenericValidatingPackager packager = new GenericValidatingPackager(is);
            m.setPackager(packager);

            m.setMTI(transaction.getMti());
            m.set(3, transaction.getProcessingCode());
            m.set(11, transaction.getTraceNumber());

            m.set(24, transaction.getNii());
            m.set(25, transaction.getPosConditionCode());
            m.set(41, transaction.getTid());
            m.set(42, transaction.getMid());


            String isoMsgString = transaction.getTpdu() + ISOUtil.hexString(m.pack()) + "00";
            byte[] isoMessageAr = ISOUtil.hex2byte(isoMsgString);
            String isoMsgLength = Utility.padLeftZeros(Integer.toHexString(isoMessageAr.length), 4);

            return isoMsgLength + isoMsgString;


        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public KeyDownloadResponse getKeyDownloadTransactionResponseEntity(ISOMsg isoMsg) {
        KeyDownloadResponse keyDownloadResponse = new KeyDownloadResponse();
        keyDownloadResponse.setKeyField(isoMsg.getString(62));
        keyDownloadResponse.setErrorMsg(isoMsg.getString(39));
        keyDownloadResponse.setResponseCode(isoMsg.getString(39));

        return keyDownloadResponse;
    }

    // <editor-fold defaultstate="collapsed" desc="Builder">

    /**
     * Get the PosDevice object
     *
     * @return PosDevice
     */
    public static ISOMsgBuilder getInstance() {
        if (instance != null) {
            return instance;
        }

        throw new RuntimeException(
                "ISOMsgBuilder class not correctly instantiated. " +
                        "Please call ISOMsgBuilder.Builder().setContext(context).build(); " +
                        "in the Application class onCreate.");
    }

    private ISOMsgBuilder() {
        super();
        instance = this;
    }

    private void init(Context appContext) {
        context = appContext;
    }

    public final static class Builder {

        private Context mContext;

        /**
         * Set the Context used to instantiate the PosDevice
         *
         * @param context the application context
         * @return the {@link ISOMsgBuilder.Builder} object.
         */
        public Builder setContext(final Context context) {
            mContext = context;
            return this;
        }

        /**
         * Initialize the ISOMsgBuilder instance to used in the application.
         */
        public void build() {
            if (mContext == null) {
                throw new RuntimeException("Context not set, please set context before building the PosDevice instance.");
            }

            new ISOMsgBuilder().init(mContext);
        }
    }
    // </editor-fold>

}
