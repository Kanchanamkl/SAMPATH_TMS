package com.epic.pos.iso.modal.request;

import com.epic.pos.iso.modal.Transaction;
import com.epic.pos.util.Utility;


public class DCCDownloadTransaction extends Transaction {

    private String tpdu ="6000110000" ;
    private String mti              = MTI_DCC_DOWNLOAD;
    private String processingCode   = "010000";
    private String nii=DCCNII;
    private String posConditionCode = "00";;
    private String traceNumber;
    private String mid;
    private String tid;
    private String privateData ;
    private String tleHeader ;
    private String mac ;
    private boolean isOnlinePin ;
    private String terminalData;

    public DCCDownloadTransaction() {
    }

    public DCCDownloadTransaction(String tid, String mid, String nii) {
        this.mid = mid;
        this.tid = tid;
        this.nii = nii;
    }

    public String getTpdu() {
        return POS_TXN + Utility.padLeftZeros(nii, 4) + TO_NII;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }


    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getPosConditionCode() {
        return posConditionCode;
    }

    public void setPosConditionCode(String posConditionCode) {
        this.posConditionCode = posConditionCode;
    }


    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getPrivateData() {
        return privateData;
    }

    public void setPrivateData(String privateData) {
        this.privateData = privateData;
    }

    public String getTleHeader() {
        return tleHeader;
    }

    public void setTleHeader(String tleHeader) {
        this.tleHeader = tleHeader;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean isOnlinePin() {
        return isOnlinePin;
    }

    public void setOnlinePin(boolean onlinePin) {
        isOnlinePin = onlinePin;
    }

    public String getTerminalData() {
        return terminalData;
    }

    public void setTerminalData(String terminalData) {
        this.terminalData = terminalData;
    }
}
