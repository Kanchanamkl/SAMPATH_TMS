package com.epic.pos.ui.report;

import static com.epic.pos.ui.common.host.HostSelectActivity.RESULT_HOST;
import static com.epic.pos.ui.common.merchant.MerchantSelectActivity.RESULT_MERCHANT;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.epic.pos.R;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.databinding.ActivityReportMenuBinding;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.host.HostSelectActivity;
import com.epic.pos.ui.common.invoice.InvoiceSearchActivity;
import com.epic.pos.ui.common.merchant.MerchantSelectActivity;
import com.epic.pos.ui.common.merchant.MerchantType;
import com.epic.pos.ui.common.password.PasswordActivity;
import com.epic.pos.ui.common.password.PasswordType;
import com.epic.pos.ui.common.receipttype.ReceiptTypeActivity;
import com.epic.pos.ui.settings.SettingsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportMenuActivity extends BaseActivity<ReportMenuPresenter> implements ReportMenuContract.View {

    private final String TAG = "ReportMenuActivity";
    private ActivityReportMenuBinding binding;

    private final int EXIT_PW_RES = 100;
    private final int HOST_SELECT_FOR_ANY_RECEIPT = 101;
    private final int MERCHANT_SELECT_FOR_ANY_RECEIPT = 102;
    private final int INVOICE_SEARCH_FOR_ANY_RECEIPT = 103;
    private final int HOST_SELECT_FOR_DETAIL_REPORT = 104;
    private final int MERCHANT_SELECT_FOR_DETAIL_REPORT = 105;
    private final int HOST_SELECT_FOR_SUMMARY_REPORT = 106;
    private final int MERCHANT_SELECT_FOR_SUMMARY_REPORT = 107;
    private final int HOST_SELECT_FOR_LAST_SETTLEMENT = 108;
    private final int MERCHANT_SELECT_FOR_LAST_SETTLEMENT = 109;
    private final int HOST_SELECT_FOR_HOST_INFO = 110;
    private final int MERCHANT_SELECT_FOR_HOST_INFO = 111;

    @BindString(R.string.toolbar_any_receipt)
    String toolbar_any_receipt;
    @BindString(R.string.toolbar_detail_report)
    String toolbar_detail_report;
    @BindString(R.string.toolbar_summary_report)
    String toolbar_summary_report;
    @BindString(R.string.toolbar_last_settlement)
    String toolbar_last_settlement;
    @BindString(R.string.activity_report_menu_host_info_report)
    String host_info_report;

    public static void startActivity(BaseActivity activity) {
        activity.startActivity(new Intent(activity, ReportMenuActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report_menu);
        ButterKnife.bind(this);
        setUpToolbarDefault((Toolbar) binding.toolbar, "", true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        init();
        DbHandler.getInstance().getTCT(tct -> {
            if(tct.getIsAuronetDCC()==1){
                if(tct.getDCCEnable()==1) {
                Log.i(TAG, "Downloading Auronet DCC Data ");
                TextView tv = findViewById(R.id.tv_update_dccdata);
                tv.setVisibility(View.VISIBLE);
            }}
        });

    }

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void init() {
    }

    @OnClick(R.id.btnSettings)
    public void onClickSettings(View view) {
        SettingsActivity.startActivity(this);
        finish();
    }

    @OnClick(R.id.btnExit)
    public void onClickExit(View view) {
        PasswordActivity.startActivityForResult(this, PasswordType.EXIT_PASSWORD, EXIT_PW_RES);
    }

    @OnClick(R.id.tv_print_last_receipt)
    public void onClickPrintLastReceipt(View view) {
        mPresenter.printLastReceipt();
    }

    @OnClick(R.id.tv_last_settlement_receipt)
    public void onClickLastSettlementReceipt(View view) {
        HostSelectActivity.startActivityForResult(this, toolbar_last_settlement, HOST_SELECT_FOR_LAST_SETTLEMENT);
    }

    @Override
    public void selectMerchantForLastSettlementReport(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                toolbar_last_settlement, MerchantType.ALL, MERCHANT_SELECT_FOR_LAST_SETTLEMENT);
    }

    @OnClick(R.id.tv_any_receipt)
    public void onClickAnyReceipt(View view) {
        HostSelectActivity.startActivityForResult(this, toolbar_any_receipt, HOST_SELECT_FOR_ANY_RECEIPT);
    }

    @Override
    public void selectMerchantForAnyReceipt(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                toolbar_any_receipt, MerchantType.ALL, MERCHANT_SELECT_FOR_ANY_RECEIPT);
    }

    @Override
    public void selectInvoiceForAnyReceipt(Host host, Merchant merchant) {
        InvoiceSearchActivity.startActivityForResult(this,
                toolbar_any_receipt, host, merchant, INVOICE_SEARCH_FOR_ANY_RECEIPT);
    }

    @OnClick(R.id.tv_detail_report)
    public void onClickDetailReport(View view) {
        HostSelectActivity.startActivityForResult(this, toolbar_detail_report, HOST_SELECT_FOR_DETAIL_REPORT);
    }

    @Override
    public void selectMerchantForDetailReport(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                toolbar_any_receipt, MerchantType.ALL, MERCHANT_SELECT_FOR_DETAIL_REPORT);
    }

    @OnClick(R.id.tv_summary_report)
    public void onClickSummaryReport(View view) {
        HostSelectActivity.startActivityForResult(this, toolbar_summary_report, HOST_SELECT_FOR_SUMMARY_REPORT);
    }

    @Override
    public void selectMerchantForSummaryReport(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                toolbar_summary_report, MerchantType.ALL, MERCHANT_SELECT_FOR_SUMMARY_REPORT);
    }

    @OnClick(R.id.tv_host_info_report)
    void onHostInfoReportClicked(View view) {
        HostSelectActivity.startActivityForResult(this, host_info_report, HOST_SELECT_FOR_HOST_INFO);
    }
    @OnClick(R.id.tv_update_dccdata)
    void onDCCDownloadClicked(View view) {
        DbHandler.getInstance().getTCT(tct -> {
            if(tct.getIsAuronetDCC()==1){
                if(tct.getDCCEnable()==1) {
                Log.i(TAG, "Downloading Auronet DCC Data ");

                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        new updatedccbin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        new updatedccbin().execute();



            }}
        });
    }
    private   class updatedccbin extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... urls) {
            showLoader("DCC BIN ","Updating DCC BIN");
            updatedccbinlistfromfile();
            mPresenter.downloaddccdata();
            return null;
        }

        protected void onPostExecute(Void feed) {
            hideLoader();

        }
    }

    public void updatedccbinlistfromfile(){
        String cddbinlist = "bin.txt";
        InputStream dccbinInputStream = null;
        String line = "";

        try {
            try {
                dccbinInputStream = getResources().getAssets().open(cddbinlist);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(dccbinInputStream));
            List<DCCBINNLIST> dccbinnlists = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                try {

                DCCBINNLIST values = new DCCBINNLIST();
                values.setPANL(line.substring(19,38).trim());
                values.setPANH(line.substring(0,19).trim());
                values.setCARDTYPE(line.substring(38,39));
                values.setCURRENCY(Integer.parseInt(line.substring(39,42)));
                values.setCOUNTRY(Integer.parseInt(line.substring(42,45)));
                dccbinnlists.add(values);}
                catch (Exception e){
                    e.printStackTrace();
                }

            }
            try {
                mPresenter.insertdccbinlist(dccbinnlists);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void selectMerchantForHostInfo(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                host_info_report, MerchantType.ALL, MERCHANT_SELECT_FOR_HOST_INFO);
    }
    @Override
    public void gotoReceiptTypeSelectActivity() {
        ReceiptTypeActivity.startActivity(this);
    }

    @Override
    protected void onActivityResult(int request, int result, @Nullable Intent data) {
        super.onActivityResult(request, result, data);
        if (request == EXIT_PW_RES) {
            if (result == RESULT_OK) {
                mPresenter.onExitApp();
                finishAffinity();
            }
        } else if (request == HOST_SELECT_FOR_ANY_RECEIPT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setHostForAnyReceipt(h);
            }
        } else if (request == MERCHANT_SELECT_FOR_ANY_RECEIPT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setMerchantForAnyReceipt(m);
            }
        } else if (request == INVOICE_SEARCH_FOR_ANY_RECEIPT) {
            if (result == RESULT_OK) {
                Transaction t = data.getParcelableExtra(InvoiceSearchActivity.RESULT_TRANSACTION);
                mPresenter.setTransactionForAnyReceipt(t);
            }
        } else if (request == HOST_SELECT_FOR_DETAIL_REPORT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setHostForDetailReport(h);
            }
        } else if (request == MERCHANT_SELECT_FOR_DETAIL_REPORT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setMerchantForDetailReport(m);
            }
        } else if (request == HOST_SELECT_FOR_SUMMARY_REPORT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setHostForSummaryReport(h);
            }
        } else if (request == MERCHANT_SELECT_FOR_SUMMARY_REPORT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setMerchantForSummaryReport(m);
            }
        } else if (request == HOST_SELECT_FOR_LAST_SETTLEMENT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setHostForLastSettlementReport(h);
            }
        } else if (request == MERCHANT_SELECT_FOR_LAST_SETTLEMENT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setMerchantForLastSettlementReport(m);
            }
        } else if (request == HOST_SELECT_FOR_HOST_INFO) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setHostForHostInfoReport(h);
            }
        } else if (request == MERCHANT_SELECT_FOR_HOST_INFO) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setMerchantForHostInfoReport(m);
            }
        }
    }
}