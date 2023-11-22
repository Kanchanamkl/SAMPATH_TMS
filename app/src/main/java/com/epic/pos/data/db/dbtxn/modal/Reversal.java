package com.epic.pos.data.db.dbtxn.modal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "REVERSAL")
public class Reversal {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "invoice_no")
    private String invoice_no;

    @ColumnInfo(name = "trace_no")
    private String trace_no;

    @ColumnInfo(name = "total_amount")
    private String total_amount;

    @ColumnInfo(name = "cash_back_amount")
    private String cash_back_amount;

    @ColumnInfo(name = "txn_date")
    private String txn_date;

    @ColumnInfo(name = "txn_time")
    private String txn_time;

    @ColumnInfo(name = "host")
    private int host;

    @ColumnInfo(name = "merchant_no")
    private int merchant_no;

    @ColumnInfo(name = "service_crg")
    private int service_crg;

    @ColumnInfo(name = "tip_amount")
    private String tip_amount;

    @ColumnInfo(name = "fuel_charge")
    private int fuel_charge;

    @ColumnInfo(name = "credit_debit")
    private String credit_debit;

    @ColumnInfo(name = "approve_code")
    private String approve_code;

    @ColumnInfo(name = "rrn")
    private String rrn;

    @ColumnInfo(name = "discount")
    private int discount;

    @ColumnInfo(name = "mti")
    private String mti;

    @ColumnInfo(name = "processing_code")
    private String processing_code;

    @ColumnInfo(name = "transaction_code")
    private int transaction_code;

    @ColumnInfo(name = "chip_status")
    private int chip_status;

    @ColumnInfo(name = "base_transaction_amount")
    private String base_transaction_amount;

    @ColumnInfo(name = "pan")
    private String pan;

    @ColumnInfo(name = "card_serial_number")
    private String card_serial_number;

    @ColumnInfo(name = "track2")
    private String track2;

    @ColumnInfo(name = "svc_code")
    private String svc_code;

    @ColumnInfo(name = "exp_date")
    private String exp_date;

    @ColumnInfo(name = "terminal_id")
    private String terminal_id;

    @ColumnInfo(name = "terminal_no")
    private int terminal_no;

    @ColumnInfo(name = "merchant_id")
    private String merchant_id;

    @ColumnInfo(name = "merchant_name")
    private String merchant_name;

    @ColumnInfo(name = "nii")
    private String nii;

    @ColumnInfo(name = "secure_nii")
    private String secure_nii;

    @ColumnInfo(name = "tpdu")
    private String tpdu;

    @ColumnInfo(name = "emv_field_55")
    private String emv_field_55;

    @ColumnInfo(name = "response_code")
    private String response_code;

    @ColumnInfo(name = "cdt_index")
    private int cdt_index;

    @ColumnInfo(name = "issuer_number")
    private int issuer_number;

    @ColumnInfo(name = "card_label")
    private String card_label;

    @ColumnInfo(name = "currency_symbol")
    private String currency_symbol;

    @ColumnInfo(name = "std_ref_no")
    private String std_ref_no;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(String invoice_no) {
        this.invoice_no = invoice_no;
    }

    public String getTrace_no() {
        return trace_no;
    }

    public void setTrace_no(String trace_no) {
        this.trace_no = trace_no;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getTxn_date() {
        return txn_date;
    }

    public void setTxn_date(String txn_date) {
        this.txn_date = txn_date;
    }

    public String getTxn_time() {
        return txn_time;
    }

    public void setTxn_time(String txn_time) {
        this.txn_time = txn_time;
    }

    public int getHost() {
        return host;
    }

    public void setHost(int host) {
        this.host = host;
    }

    public int getMerchant_no() {
        return merchant_no;
    }

    public void setMerchant_no(int merchant_no) {
        this.merchant_no = merchant_no;
    }

    public int getService_crg() {
        return service_crg;
    }

    public void setService_crg(int service_crg) {
        this.service_crg = service_crg;
    }

    public String getTip_amount() {
        return tip_amount;
    }

    public void setTip_amount(String tip_amount) {
        this.tip_amount = tip_amount;
    }

    public int getFuel_charge() {
        return fuel_charge;
    }

    public void setFuel_charge(int fuel_charge) {
        this.fuel_charge = fuel_charge;
    }

    public String getCredit_debit() {
        return credit_debit;
    }

    public void setCredit_debit(String credit_debit) {
        this.credit_debit = credit_debit;
    }

    public String getApprove_code() {
        return approve_code;
    }

    public void setApprove_code(String approve_code) {
        this.approve_code = approve_code;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getProcessing_code() {
        return processing_code;
    }

    public void setProcessing_code(String processing_code) {
        this.processing_code = processing_code;
    }

    public int getTransaction_code() {
        return transaction_code;
    }

    public void setTransaction_code(int transaction_code) {
        this.transaction_code = transaction_code;
    }

    public int getChip_status() {
        return chip_status;
    }

    public void setChip_status(int chip_status) {
        this.chip_status = chip_status;
    }

    public String getBase_transaction_amount() {
        return base_transaction_amount;
    }

    public void setBase_transaction_amount(String base_transaction_amount) {
        this.base_transaction_amount = base_transaction_amount;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getCard_serial_number() {
        return card_serial_number;
    }

    public void setCard_serial_number(String card_serial_number) {
        this.card_serial_number = card_serial_number;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getSvc_code() {
        return svc_code;
    }

    public void setSvc_code(String svc_code) {
        this.svc_code = svc_code;
    }

    public String getExp_date() {
        return exp_date;
    }

    public void setExp_date(String exp_date) {
        this.exp_date = exp_date;
    }

    public String getTerminal_id() {
        return terminal_id;
    }

    public void setTerminal_id(String terminal_id) {
        this.terminal_id = terminal_id;
    }

    public int getTerminal_no() {
        return terminal_no;
    }

    public void setTerminal_no(int terminal_no) {
        this.terminal_no = terminal_no;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getSecure_nii() {
        return secure_nii;
    }

    public void setSecure_nii(String secure_nii) {
        this.secure_nii = secure_nii;
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getEmv_field_55() {
        return emv_field_55;
    }

    public void setEmv_field_55(String emv_field_55) {
        this.emv_field_55 = emv_field_55;
    }

    public String getResponse_code() {
        return response_code;
    }

    public void setResponse_code(String response_code) {
        this.response_code = response_code;
    }

    public int getCdt_index() {
        return cdt_index;
    }

    public void setCdt_index(int cdt_index) {
        this.cdt_index = cdt_index;
    }

    public int getIssuer_number() {
        return issuer_number;
    }

    public void setIssuer_number(int issuer_number) {
        this.issuer_number = issuer_number;
    }

    public String getCash_back_amount() {
        return cash_back_amount;
    }

    public void setCash_back_amount(String cash_back_amount) {
        this.cash_back_amount = cash_back_amount;
    }

    public String getCard_label() {
        return card_label;
    }

    public void setCard_label(String card_label) {
        this.card_label = card_label;
    }

    public String getCurrency_symbol() {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol) {
        this.currency_symbol = currency_symbol;
    }

    public String getStd_ref_no() {
        return std_ref_no;
    }

    public void setStd_ref_no(String std_ref_no) {
        this.std_ref_no = std_ref_no;
    }
}
