package com.epic.pos.data.db.dbtxn.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.epic.pos.common.Const;
import com.epic.pos.common.TranTypes;

@Entity(tableName = "TRANSACTION")
public class Transaction implements Parcelable {

    public Transaction() {
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "invoice_no")
    private String invoice_no;

    @ColumnInfo(name = "trace_no")
    private String trace_no;

    @ColumnInfo(name = "total_amount")
    private String total_amount;

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
    private int chip_status; //pos entry mode

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

    @ColumnInfo(name = "voided")
    private int voided;

    @ColumnInfo(name = "ext_data")
    private String ext_data;

    @ColumnInfo(name = "card_label")
    private String card_label;

    @ColumnInfo(name = "cash_back_amount")
    private String cash_back_amount;

    @ColumnInfo(name = "no_of_installments")
    private String no_of_installments;

    @ColumnInfo(name = "interest_rate")
    private String interest_rate;

    @ColumnInfo(name = "total_interest")
    private int total_interest;

    @ColumnInfo(name = "first_installment")
    private int first_installment;

    @ColumnInfo(name = "down_payment")
    private int down_payment;

    @ColumnInfo(name = "monthly_installment")
    private int monthly_installment;

    @ColumnInfo(name = "monthly_interest")
    private int monthly_interest;

    @ColumnInfo(name = "easy_pay_plan")
    private int easy_pay_plan;

    @ColumnInfo(name = "down_payment_mode")
    private int down_payment_mode;

    @ColumnInfo(name = "qr_id")
    private String qr_id;

    @ColumnInfo(name = "qr_app_name")
    private String qr_app_name;

    @ColumnInfo(name = "natch_number")
    private int natch_number;

    @ColumnInfo(name = "card_holder_name")
    private String card_holder_name;

    @ColumnInfo(name = "first_tran_code")
    private int first_tran_code;

    @ColumnInfo(name = "dcc_data")
    private String dcc_data;

    @ColumnInfo(name = "currency_code")
    private int currency_code;

    @ColumnInfo(name = "currency_symbol")
    private String currency_symbol;

    @ColumnInfo(name = "dcc_chca")
    private String dcc_chca;

    @ColumnInfo(name = "dcc_comn")
    private String dcc_comn;

    @ColumnInfo(name = "dcc_mrkp")
    private String dcc_mrkp;

    @ColumnInfo(name = "dcc_froex")
    private String dcc_froex;

    @ColumnInfo(name = "dcc_famt")
    private String dcc_famt;

    @ColumnInfo(name = "is_dcc_tran")
    private int is_dcc_tran;

    @ColumnInfo(name = "std_ref_no")
    private String std_ref_no;

    protected Transaction(Parcel in) {
        id = in.readInt();
        invoice_no = in.readString();
        trace_no = in.readString();
        total_amount = in.readString();
        txn_date = in.readString();
        txn_time = in.readString();
        host = in.readInt();
        merchant_no = in.readInt();
        service_crg = in.readInt();
        tip_amount = in.readString();
        fuel_charge = in.readInt();
        credit_debit = in.readString();
        approve_code = in.readString();
        rrn = in.readString();
        discount = in.readInt();
        mti = in.readString();
        processing_code = in.readString();
        transaction_code = in.readInt();
        chip_status = in.readInt();
        base_transaction_amount = in.readString();
        pan = in.readString();
        card_serial_number = in.readString();
        track2 = in.readString();
        svc_code = in.readString();
        exp_date = in.readString();
        terminal_id = in.readString();
        terminal_no = in.readInt();
        merchant_id = in.readString();
        merchant_name = in.readString();
        nii = in.readString();
        secure_nii = in.readString();
        tpdu = in.readString();
        emv_field_55 = in.readString();
        response_code = in.readString();
        cdt_index = in.readInt();
        issuer_number = in.readInt();
        voided = in.readInt();
        ext_data = in.readString();
        card_label = in.readString();
        cash_back_amount = in.readString();
        no_of_installments = in.readString();
        interest_rate = in.readString();
        total_interest = in.readInt();
        first_installment = in.readInt();
        down_payment = in.readInt();
        monthly_installment = in.readInt();
        monthly_interest = in.readInt();
        easy_pay_plan = in.readInt();
        down_payment_mode = in.readInt();
        qr_id = in.readString();
        qr_app_name = in.readString();
        natch_number = in.readInt();
        card_holder_name = in.readString();
        first_tran_code = in.readInt();
        dcc_data = in.readString();
        currency_code = in.readInt();
        currency_symbol = in.readString();
        dcc_chca = in.readString();
        dcc_comn = in.readString();
        dcc_mrkp = in.readString();
        dcc_froex = in.readString();
        dcc_famt = in.readString();
        is_dcc_tran = in.readInt();
        std_ref_no = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(invoice_no);
        dest.writeString(trace_no);
        dest.writeString(total_amount);
        dest.writeString(txn_date);
        dest.writeString(txn_time);
        dest.writeInt(host);
        dest.writeInt(merchant_no);
        dest.writeInt(service_crg);
        dest.writeString(tip_amount);
        dest.writeInt(fuel_charge);
        dest.writeString(credit_debit);
        dest.writeString(approve_code);
        dest.writeString(rrn);
        dest.writeInt(discount);
        dest.writeString(mti);
        dest.writeString(processing_code);
        dest.writeInt(transaction_code);
        dest.writeInt(chip_status);
        dest.writeString(base_transaction_amount);
        dest.writeString(pan);
        dest.writeString(card_serial_number);
        dest.writeString(track2);
        dest.writeString(svc_code);
        dest.writeString(exp_date);
        dest.writeString(terminal_id);
        dest.writeInt(terminal_no);
        dest.writeString(merchant_id);
        dest.writeString(merchant_name);
        dest.writeString(nii);
        dest.writeString(secure_nii);
        dest.writeString(tpdu);
        dest.writeString(emv_field_55);
        dest.writeString(response_code);
        dest.writeInt(cdt_index);
        dest.writeInt(issuer_number);
        dest.writeInt(voided);
        dest.writeString(ext_data);
        dest.writeString(card_label);
        dest.writeString(cash_back_amount);
        dest.writeString(no_of_installments);
        dest.writeString(interest_rate);
        dest.writeInt(total_interest);
        dest.writeInt(first_installment);
        dest.writeInt(down_payment);
        dest.writeInt(monthly_installment);
        dest.writeInt(monthly_interest);
        dest.writeInt(easy_pay_plan);
        dest.writeInt(down_payment_mode);
        dest.writeString(qr_id);
        dest.writeString(qr_app_name);
        dest.writeInt(natch_number);
        dest.writeString(card_holder_name);
        dest.writeInt(first_tran_code);
        dest.writeString(dcc_data);
        dest.writeInt(currency_code);
        dest.writeString(currency_symbol);
        dest.writeString(dcc_chca);
        dest.writeString(dcc_comn);
        dest.writeString(dcc_mrkp);
        dest.writeString(dcc_froex);
        dest.writeString(dcc_famt);
        dest.writeInt(is_dcc_tran);
        dest.writeString(std_ref_no);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

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

    public int getVoided() {
        return voided;
    }

    public void setVoided(int voided) {
        this.voided = voided;
    }

    public String getExt_data() {
        return ext_data;
    }

    public void setExt_data(String ext_data) {
        this.ext_data = ext_data;
    }

    public String getCard_label() {
        return card_label;
    }

    public void setCard_label(String card_label) {
        this.card_label = card_label;
    }

    public String getCash_back_amount() {
        return cash_back_amount;
    }

    public void setCash_back_amount(String cash_back_amount) {
        this.cash_back_amount = cash_back_amount;
    }

    public String getNo_of_installments() {
        return no_of_installments;
    }

    public void setNo_of_installments(String no_of_installments) {
        this.no_of_installments = no_of_installments;
    }

    public String getInterest_rate() {
        return interest_rate;
    }

    public void setInterest_rate(String interest_rate) {
        this.interest_rate = interest_rate;
    }

    public int getTotal_interest() {
        return total_interest;
    }

    public void setTotal_interest(int total_interest) {
        this.total_interest = total_interest;
    }

    public int getFirst_installment() {
        return first_installment;
    }

    public void setFirst_installment(int first_installment) {
        this.first_installment = first_installment;
    }

    public int getDown_payment() {
        return down_payment;
    }

    public void setDown_payment(int down_payment) {
        this.down_payment = down_payment;
    }

    public int getMonthly_installment() {
        return monthly_installment;
    }

    public void setMonthly_installment(int monthly_installment) {
        this.monthly_installment = monthly_installment;
    }

    public int getMonthly_interest() {
        return monthly_interest;
    }

    public void setMonthly_interest(int monthly_interest) {
        this.monthly_interest = monthly_interest;
    }

    public int getEasy_pay_plan() {
        return easy_pay_plan;
    }

    public void setEasy_pay_plan(int easy_pay_plan) {
        this.easy_pay_plan = easy_pay_plan;
    }

    public int getDown_payment_mode() {
        return down_payment_mode;
    }

    public void setDown_payment_mode(int down_payment_mode) {
        this.down_payment_mode = down_payment_mode;
    }

    public String getQr_id() {
        return qr_id;
    }

    public void setQr_id(String qr_id) {
        this.qr_id = qr_id;
    }

    public String getQr_app_name() {
        return qr_app_name;
    }

    public void setQr_app_name(String qr_app_name) {
        this.qr_app_name = qr_app_name;
    }

    public int getNatch_number() {
        return natch_number;
    }

    public void setNatch_number(int natch_number) {
        this.natch_number = natch_number;
    }

    public String getCard_holder_name() {
        return card_holder_name;
    }

    public void setCard_holder_name(String card_holder_name) {
        this.card_holder_name = card_holder_name;
    }

    public int getFirst_tran_code() {
        return first_tran_code;
    }

    public void setFirst_tran_code(int first_tran_code) {
        this.first_tran_code = first_tran_code;
    }

    public String getDcc_data() {
        return dcc_data;
    }

    public void setDcc_data(String dcc_data) {
        this.dcc_data = dcc_data;
    }

    public int getCurrency_code() {
        return currency_code;
    }

    public void setCurrency_code(int currency_code) {
        this.currency_code = currency_code;
    }

    public String getCurrency_symbol() {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol) {
        this.currency_symbol = currency_symbol;
    }

    public String getDcc_chca() {
        return dcc_chca;
    }

    public void setDcc_chca(String dcc_chca) {
        this.dcc_chca = dcc_chca;
    }

    public String getDcc_comn() {
        return dcc_comn;
    }

    public void setDcc_comn(String dcc_comn) {
        this.dcc_comn = dcc_comn;
    }

    public String getDcc_mrkp() {
        return dcc_mrkp;
    }

    public void setDcc_mrkp(String dcc_mrkp) {
        this.dcc_mrkp = dcc_mrkp;
    }

    public String getDcc_froex() {
        return dcc_froex;
    }

    public void setDcc_froex(String dcc_froex) {
        this.dcc_froex = dcc_froex;
    }

    public String getDcc_famt() {
        return dcc_famt;
    }

    public void setDcc_famt(String dcc_famt) {
        this.dcc_famt = dcc_famt;
    }

    public int getIs_dcc_tran() {
        return is_dcc_tran;
    }

    public void setIs_dcc_tran(int is_dcc_tran) {
        this.is_dcc_tran = is_dcc_tran;
    }

    public static Creator<Transaction> getCREATOR() {
        return CREATOR;
    }

    public boolean isOfflineTransaction(){
        return getTransaction_code() == TranTypes.SALE_OFFLINE
                || getTransaction_code() == TranTypes.SALE_OFFLINE_MANUAL;
    }

    public boolean isPreCompTransaction(){
        return getTransaction_code() == TranTypes.SALE_PRE_COMPLETION;
    }

    public String getStd_ref_no() {
        return std_ref_no;
    }

    public void setStd_ref_no(String std_ref_no) {
        this.std_ref_no = std_ref_no;
    }
}
