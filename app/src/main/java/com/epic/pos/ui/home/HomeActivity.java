package com.epic.pos.ui.home;

import static com.epic.pos.ui.common.host.HostSelectActivity.RESULT_HOST;
import static com.epic.pos.ui.common.merchant.MerchantSelectActivity.RESULT_MERCHANT;
import static com.epic.pos.ui.common.usernamepassword.UserNamePasswordInputActivity.TRANTYPE;
//import static EPIC_TMS.EPIC_SDK_API.OPER_APPLICATION_DOWNLOAD;
//import static EPIC_TMS.EPIC_SDK_API.OPER_DEBIT_KEY_DOWNLOAD;
//import static EPIC_TMS.EPIC_SDK_API.OPER_DISABLE_TERMINAL_OPP;
//import static EPIC_TMS.EPIC_SDK_API.OPER_ENABLE_TERMINAL_OPP;
//import static EPIC_TMS.EPIC_SDK_API.OPER_INIT_SETTLEMENT;
//import static EPIC_TMS.EPIC_SDK_API.OPER_PROFILE_DOWNLOAD;
//import static EPIC_TMS.EPIC_SDK_API.OPER_TERMINAL_ECHO;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.epic.pos.BuildConfig;
import com.epic.pos.R;
import com.epic.pos.adapter.CommonViewPagerAdapter;
import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.dagger.AppComponent;
import com.epic.pos.data.db.DbHandler;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Feature;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.databinding.ActivityHomeBinding;
import com.epic.pos.device.PosDevice;
import com.epic.pos.domain.entity.HomeMenuBean;
import com.epic.pos.helper.FixedSpeedScroller;
import com.epic.pos.receiver.BaseAppReceiver;
import com.epic.pos.tle.KeyManager;
import com.epic.pos.tms.TMSActivity;
import com.epic.pos.ui.BaseActivity;
import com.epic.pos.ui.common.host.HostSelectActivity;
import com.epic.pos.ui.common.invoice.InvoiceSearchActivity;
import com.epic.pos.ui.common.merchant.MerchantSelectActivity;
import com.epic.pos.ui.common.merchant.MerchantType;
import com.epic.pos.ui.common.password.PasswordActivity;
import com.epic.pos.ui.common.password.PasswordType;
import com.epic.pos.ui.common.receipttype.ReceiptTypeActivity;
import com.epic.pos.ui.common.usernamepassword.UserNamePasswordInputActivity;
import com.epic.pos.ui.ecrcardscan.EcrCardScanActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.home.banner.HomeBannerFragment;
import com.epic.pos.ui.home.menu.HomeMenuFragment;
import com.epic.pos.ui.newsettlement.auto.AutoSettleActivity;
import com.epic.pos.ui.newsettlement.settle.SettleActivity;
import com.epic.pos.ui.report.ReportMenuActivity;
import com.epic.pos.ui.sale.amount.AmountActivity;
import com.epic.pos.ui.sale.merchantselect.MerchantListActivity;
import com.epic.pos.ui.sale.precomp.PreCompActivity;
import com.epic.pos.ui.settings.tms.DeviceOperationData;
import com.epic.pos.ui.settings.tms.DeviceStatus;
import com.epic.pos.ui.settings.tms.ProfileData;
import com.epic.pos.ui.voidsale.VoidActivity;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.Partition;
import com.epic.pos.view.CDTListDialog;
import com.example.atemhub.MainSDK;
import com.example.atemhub.OperationManager;
import com.google.gson.Gson;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;

//import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//import EPIC_AEH.INIT_SDK;
//import EPIC_TMS.EPIC_SDK_API;
//import EPIC_TMS.tmsTerminalCom;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends BaseActivity<HomePresenter> implements HomeContract.View,
        HomeMenuFragment.OnFragmentInteractionListener , OperationManager {

    private static HomeActivity instance;
    private final int ACTIVITY_RESULT_VOID_PW = 101;
    private final int ACTIVITY_RESULT_VOID_HOST = 102;
    private final int ACTIVITY_RESULT_VOID_MERCHANT = 103;
    private final int ACTIVITY_RESULT_PRE_COMP_HOST = 104;
    private final int ACTIVITY_RESULT_PRE_COMP_MERCHANT = 105;
    private final int ACTIVITY_RESULT_CLEAR_REVERSAL_PW = 106;
    private final int ACTIVITY_RESULT_SETTLEMENT_PW = 107;
    private final int ACTIVITY_RESULT_PRE_COMP_PW = 108;
    private final int ACTIVITY_RESULT_CLEAR_REVERSAL_HOST = 109;
    private final int ACTIVITY_RESULT_SELECT_MERCHANT_FOR_CARD_SCAN = 110;
    private final int ACTIVITY_RESULT_SELECT_MERCHANT_ECR = 111;
    private final int ACTIVITY_RESULT_SETTLEMENT_HOST_SELECT = 112;
    private final int ACTIVITY_RESULT_SETTLEMENT_MERCHANT_SELECT = 113;
    private final int HOST_SELECT_FOR_DETAIL_REPORT = 114;
    private final int MERCHANT_SELECT_FOR_DETAIL_REPORT = 115;
    private final int HOST_SELECT_FOR_LAST_SETTLEMENT = 116;
    private final int MERCHANT_SELECT_FOR_LAST_SETTLEMENT = 117;
    private final int HOST_SELECT_FOR_ANY_RECEIPT = 118;
    private final int MERCHANT_SELECT_FOR_ANY_RECEIPT = 119;
    private final int INVOICE_SEARCH_FOR_ANY_RECEIPT = 120;
    private final int ACTIVITY_RESULT_CHECK_REVERSAL_HOST = 122;
    private final int ACTIVITY_RESULT_CHECK_REVERSAL_PRINT_ERROR = 123;
    private final int ACTIVITY_RESULT_MANUALSALE_PW = 124;
    private final int ACTIVITY_RESULT_QR_SALE = 125;

    //TMS Related
    private boolean calledSDK = false;
//    public static INIT_SDK initSdk = new INIT_SDK();
//    static INIT_SDK.HOST_INFOR hstin = initSdk.new HOST_INFOR();
//    public static ArrayList<INIT_SDK.HOST_INFOR> hstInfo = new ArrayList<>();
    String tmsurl = "";
    static String path = "";
    public static String appID = "";
    public static String instanceCode = "";
    public static String institutecode = "";
    public static String appCode = "";
    public static String cityCode = "";
    public static String echotimeout = "15";
    public static String basetid = "";
    Location gpslocation=null;
    // END TMS Related
    CommonViewPagerAdapter adapter;
    private ActivityHomeBinding binding;
    private int currentPage = 0;
    private Timer timer;
    final long DELAY_MS = 200;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 6000; // time in milliseconds between successive task executions.
    private long lastTap = -1; //this variable added to prevent double tap issue on buttons.

    private static final String TAG = HomeActivity.class.getName();
    Map<Integer, Integer> featureIconMap = new HashMap<>();



    MainSDK mainSDK;
    public boolean isAppDownloadSuccess = false;
    public boolean isAppDownloadCompleted = false;
    public boolean isBannerDownloadSuccess = false;
    public boolean isTranSyncDisableSuccess = false;
    public boolean isTranSyncEnableSuccess = false;
    public boolean isDisableTerminalOppSuccess = false;
    public boolean isEnableTerminalOppSuccess = false;
    public boolean isInitSettlement = false;
    public boolean isAppUnInstallSuccess = false;
    public boolean isDebitKeyDownloadSuccess = false;
    public boolean isEnableDeviceEchoSuccess = false;
    public boolean isDisableDeviceEchoSuccess = false;
    public boolean isLogDownloadSuccess = false;
    public boolean isProfileDownloadSuccess = false;
    public boolean isLogEnableSuccess = false;
    public boolean isLogDisableSuccess = false;

    private boolean appDownloadStatus = false;
    private String appVersion;
    private String appName;
    long downloadId = -1;
    private File downloadedApkFile;
    private int installStatus = 100;
    private String optCode;

    private com.epic.pos.ui.settings.tms.DeviceStatus deviceStatus;
    private Handler handler = new Handler();



    @BindString(R.string.toolbar_any_receipt)
    String toolbar_any_receipt;
    @BindString(R.string.toolbar_last_settlement)
    String toolbar_last_settlement;
    @BindString(R.string.toolbar_detail_report)
    String toolbar_detail_report;
    @BindString(R.string.msg_reversal_msg)
    String msg_reversal_msg;
    @BindString(R.string.msg_reversal_msg_no_records)
    String msg_reversal_msg_no_records;
    @BindString(R.string.select_cdt_title)
    String selectCDT;
    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.msg_card_not_support)
    String msg_card_not_support;
    @BindString(R.string.select_application)
    String select_application;
    @BindString(R.string.msg_auto_settlement_started)
    String msg_auto_settlement_started;
    @BindString(R.string.msg_battery_level)
    String msg_battery_level;
    @BindString(R.string.btn_ok)
    String btn_ok;
    @BindString(R.string.msg_feed_paper)
    String msg_feed_paper;

    //    private TMSBgThread tmsBgThread;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, HomeActivity.class));
    }

    public static void startActivityClearTop(BaseActivity activity) {
        activity.startActivity
                (new Intent(activity, HomeActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void restartActivity() {
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.getInstance().initPosDevice();
        mainSDK = new MainSDK(HomeActivity.this);
        mainSDK.setOperationManagerCallback(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        ButterKnife.bind(this);

        try {
            setupViewPagerBanner();
        } catch (IOException e) {
            e.printStackTrace();
        }
        KeyManager.init();
        initFeatureIconMap();
        instance=this;
        mPresenter.initData();
        getgpsdata();

//Auto Download dcc data on app start
        DbHandler.getInstance().getTCT(tct -> {
            if (tct.getDCCEnable() == 1) {
                if (tct.getIsAuronetDCC() == 1) {
                    Log.i(TAG, "Downloading Auronet DCC Data ");
                    mPresenter.downloaddccdata();
                }
            }
        });

        //   Update DCC BIN LIST
//        DbHandler.getInstance().getTCT(tct -> {
//            if(tct.getDCCEnable()==1) {
//            if(tct.getIsAuronetDCC()==1){
//
//                Log.i(TAG, "Updating DCC BIN list ");
//                DCCDatabase dccdb = DCCDatabase.getInstance(this);
//
//
////                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
////                        new updatedccbin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
////                    else
////                        new updatedccbin().execute();
//                    //new CallForSDKTask().execute();
//
//            }
//            }
//        });
    }

    @Override
    public void gotoFailedActivity(String title, String msg) {
        FailedActivity.startActivity(title, msg, btn_ok, this);
    }

    private class updatedccbin extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... urls) {
            updatedccbinlistfromfile();
            return null;
        }

        protected void onPostExecute(Void feed) {

        }
    }

    public void updatedccbinlistfromfile() {
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
                DCCBINNLIST values = new DCCBINNLIST();
                values.setPANL(line.substring(19, 38));
                values.setPANH(line.substring(0, 19));
                values.setCARDTYPE(line.substring(38, 39));
                values.setCURRENCY(Integer.parseInt(line.substring(39, 42)));
                values.setCOUNTRY(Integer.parseInt(line.substring(42, 45)));

                dccbinnlists.add(values);

            }
            dccbinInputStream.close();

            try {
                mPresenter.insertdccbinlist(dccbinnlists);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updatecupbinfiles() {
        String cddbinlist = "CUP_bin.txt";
        InputStream cupbininputstream = null;
        String line = "";


        try {
            try {
                cupbininputstream = getResources().getAssets().open(cddbinlist);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(cupbininputstream));
            List<CardDefinition> cdtlist = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                CardDefinition values = new CardDefinition();
                String[] value = line.split(",");
                values.setPanLow(value[0]);
                values.setPanHigh(value[1]);
                values.setCardLabel("UNIONPAY");
                values.setCardAbbreviation("CU");
                values.setTrackRequired("TRACK");
                values.setTxnBitmap("11111111111111");
                values.setFloorLimit(0);
                values.setHostIndex(1);
                values.setHostGroup(1);
                values.setMinPanDigit(16);
                values.setMaxPanDigit(19);
                values.setIssuerNumber(6);
                values.setCheckLuhn(0);
                values.setExpDateRequired(1);
                values.setManualEntry(0);
                values.setChkSvcCode(1);


                cdtlist.add(values);

            }
            cupbininputstream.close();

            try {


            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCheckReversalPrintError(String msg) {
        FailedActivity.startActivity(Const.MSG_PRINT_ERROR, msg, this, ACTIVITY_RESULT_CHECK_REVERSAL_PRINT_ERROR);
    }

    private void initFeatureIconMap() {
        featureIconMap.put(HomeMenuBean.TYPE_SALE, R.drawable.ic_sale);
        featureIconMap.put(HomeMenuBean.TYPE_OFFLINE_SALE, R.drawable.ic_offline);
        featureIconMap.put(HomeMenuBean.TYPE_MANUAL_SALE, R.drawable.ic_manual_sale);
        featureIconMap.put(HomeMenuBean.TYPE_OFFLINE_MANUAL_SALE, R.drawable.ic_offline);
        featureIconMap.put(HomeMenuBean.TYPE_SETTLEMENT, R.drawable.ic_settlement);
        featureIconMap.put(HomeMenuBean.TYPE_VOID, R.drawable.ic_void);
        featureIconMap.put(HomeMenuBean.TYPE_PRE_AUTH, R.drawable.ic_authorization);
        featureIconMap.put(HomeMenuBean.TYPE_INSTALMENT, R.drawable.ic_instalment);
        featureIconMap.put(HomeMenuBean.TYPE_PRE_AUTH_MANUAL, R.drawable.ic_authorization);
        featureIconMap.put(HomeMenuBean.TYPE_CASH_ADVANCE, R.drawable.ic_cash_advance);
        featureIconMap.put(HomeMenuBean.TYPE_PRE_COM, R.drawable.ic_completion);
        featureIconMap.put(HomeMenuBean.TYPE_CASH_BACK, R.drawable.ic_cash_back);
        featureIconMap.put(HomeMenuBean.TYPE_CLEAR_REVERSAL, R.drawable.ic_clear_reversal);
        featureIconMap.put(HomeMenuBean.TYPE_QR_VERIFY, R.drawable.ic_qr);
        featureIconMap.put(HomeMenuBean.TYPE_REFUND, R.drawable.ic_clear_reversal);
        featureIconMap.put(HomeMenuBean.TYPE_REFUND_MANUAL, R.drawable.ic_clear_reversal);
        featureIconMap.put(HomeMenuBean.TYPE_QUASI_CASH, R.drawable.ic_cash_advance);
        featureIconMap.put(HomeMenuBean.TYPE_QUASI_CASH_MANUAL, R.drawable.ic_cash_advance);
        featureIconMap.put(HomeMenuBean.TYPE_DETAIL_REPORT, R.drawable.ic_detail_report);
        featureIconMap.put(HomeMenuBean.TYPE_PRINT_LAST_RECEIPT, R.drawable.ic_last_receipt);
        featureIconMap.put(HomeMenuBean.TYPE_LAST_SETTLEMENT_RECEIPT, R.drawable.ic_last_settlement);
        featureIconMap.put(HomeMenuBean.TYPE_PRINT_ANY_RECEIPT, R.drawable.ic_any_receipt);
        featureIconMap.put(HomeMenuBean.TYPE_CHECK_REVERSAL, R.drawable.ic_reset_btn);
        featureIconMap.put(HomeMenuBean.TYPE_STUDENT_REF_SALE, R.drawable.ic_student_ref);
        featureIconMap.put(HomeMenuBean.TYPE_AUTH_ONLY, R.drawable.ic_authorization);
        featureIconMap.put(HomeMenuBean.TYPE_QR_SALE, R.drawable.ic_qr);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLog.i(TAG, "onResume()");
        if (isAutomaticTimeZoneEnabled()) {
            mPresenter.onResume();
            mPresenter.getFeatures();
        } else {
            log("Auto timezone disabled");
            showMsgDialog(Const.MSG_ENABLE_AUTO_TIMEZONE, () -> {
                startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
            });
        }
        Log.d("BBBBB", "onResume()");

        if (!calledSDK) {

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//                new CallForSDKTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            else
//                new CallForSDKTask().execute();
            //new CallForSDKTask().execute();
        }


        System.runFinalization();
        //  Runtime.getRuntime().gc();
        // System.gc();
    }

    @Override
    public void onProfileUpdateSuccess() {
        log("onProfileUpdateSuccess()");
        BaseAppReceiver.notifyProfileUpdateComplete(this);
        restartActivity();
    }

    @Override
    public void turnScreenOn() {
        log("turnScreenOn()");
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = -1;
        getWindow().setAttributes(params);
    }

    @Override
    public void setRootLayoutVisibility(boolean visibility) {
        if (visibility) {
            binding.rootLayout.setVisibility(View.VISIBLE);
        } else {
            binding.rootLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void gotoAutoSettlementActivity() {
       // showToastMessage(msg_auto_settlement_started);
        log("Auto Settlement started");
        AutoSettleActivity.startActivity(this);
    }

    @Override
    public void gotoEcrCardScanActivity() {
        EcrCardScanActivity.startActivity(this);
    }

    // <editor-fold defaultstate="collapsed" desc="ECR functions">
    @Override
    public void selectMerchantForEcr() {
        MerchantListActivity.startActivityForResult(this, ACTIVITY_RESULT_SELECT_MERCHANT_ECR);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Card scan functions">
    private boolean isCardSwiped = false;

    @Override
    public void gotoMerchantListActivityForCardScan(boolean isCardSwiped) {
        this.isCardSwiped = isCardSwiped;
        MerchantListActivity.startActivityForResult(this, ACTIVITY_RESULT_SELECT_MERCHANT_FOR_CARD_SCAN);
    }

    @Override
    public void onCDTError() {
        showToastMessage(msg_card_not_support);
    }

    @Override
    public void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList) {
        new CDTListDialog(this).showDialog(selectCDT, cardDefinitionList,
                cdt -> mPresenter.onCDTSelectedForCardSwipe(cdt));
    }

    // </editor-fold>

    @Override
    protected void initDependencies(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void setupViewPagerBanner() throws IOException {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "tmsImages");
        CommonViewPagerAdapter adapter = new CommonViewPagerAdapter(getSupportFragmentManager(), 1);
        if (!directory.exists()) {
            adapter.addFragment(HomeBannerFragment.newInstance(R.drawable.banner1), "Banner");
            adapter.addFragment(HomeBannerFragment.newInstance(R.drawable.banner2), "Banner");
            adapter.addFragment(HomeBannerFragment.newInstance(R.drawable.banner3), "Banner");
        }else {
            log("setupViewPagerBanner()-else");
            Bitmap bitmap1 = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "tmsImages/banner1.jpg");
            Bitmap bitmap2 = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "tmsImages/banner2.jpg");
            Bitmap bitmap3 = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "tmsImages/banner3.jpg");

            if(bitmap1 != null){
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                adapter.addFragment(HomeBannerFragment.newInstance(bytes.toByteArray()), "Banner");
                log("setupViewPagerBanner()-bitmap1");
//                log("emitBannerUpdate");
//                PosDevice.getInstance().getTMSConn().emitBannerUpdate();
            }
            if(bitmap2 != null){
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                adapter.addFragment(HomeBannerFragment.newInstance(bytes.toByteArray()), "Banner");
                log("setupViewPagerBanner()-bitmap2");
            }
            if(bitmap3 != null){
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap3.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                adapter.addFragment(HomeBannerFragment.newInstance(bytes.toByteArray()), "Banner");
                log("setupViewPagerBanner()-bitmap3");
            }
        }

        binding.viewPagerBanner.setAdapter(adapter);
        binding.springDotsIndicator.setViewPager(binding.viewPagerBanner);

        setSmoothPagerScroller();
        setViewPagerTimer(binding.viewPagerBanner, adapter);

    }
    private void setViewPagerTimer(ViewPager viewPager, CommonViewPagerAdapter adapter) {
        /*After setting the adapter use the timer */
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == adapter.getCount()) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    private void setSmoothPagerScroller() {
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(this, new AccelerateInterpolator());
            // scroller.setFixedDuration(5000);
            mScroller.set(binding.viewPagerBanner, scroller);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }

    @Override
    protected void onAutoSettlementNotify() {
        if (!mPresenter.isPause()) {
            log("onAutoSettlementNotify()");
            mPresenter.tryToAutoSettle();
        }
    }

    @Override
    protected void onProfileUpdateNotify() {
        if (!mPresenter.isPause()) {
            log("onProfileUpdateNotify()");
            mPresenter.updateProfile();
        }
    }

    @Override
    protected void onGenerateConfigMapNotify() {
        if (!mPresenter.isPause()) {
            log("config map generation notify");
            mPresenter.generateConfigMap();
        }
    }

    @Override
    public void onProfileUpdateCompleted() {
        Toast.makeText(this, "Profile update completed!", Toast.LENGTH_LONG).show();
        BaseAppReceiver.notifyProfileUpdateComplete(this);
        log("onProfileUpdateCompleted()");
        try {
            mPresenter.mainSDK.emitProfileDownloadSuccess(getPackageName(),BuildConfig.VERSION_NAME, "00");

       } catch (Exception ex) {
           log("get profile data error : " + ex.getMessage());
        }
        restartActivity();
    }

    @Override
    public void onFeaturesReceived(Partition<Feature> featurePartitions) {
        CommonViewPagerAdapter adapter = new CommonViewPagerAdapter(getSupportFragmentManager(), 1);
        int menuIndex = 1;
        for (List<Feature> fList : featurePartitions) {
            ArrayList<HomeMenuBean> menuBeans = new ArrayList<>();
            for (Feature f : fList) {
                menuBeans.add(new HomeMenuBean(f.getName(), featureIconMap.get(f.getId()), f.getId()));
            }
            adapter.addFragment(HomeMenuFragment.newInstance(menuBeans), "Menu" + menuIndex);
            menuIndex++;
        }
        binding.viewPagerMenu.setOffscreenPageLimit(menuIndex);
        binding.viewPagerMenu.setAdapter(adapter);
        binding.springDotsIndicatorMenu.setViewPager(binding.viewPagerMenu);
    }

    @Override
    public void forceClearReversalSuccess(int deletedRecords) {
        if (deletedRecords == 1) {
            showToastMessage(msg_reversal_msg);
        } else {
            showToastMessage(msg_reversal_msg_no_records);
        }
    }

    @Override
    public void startSettlementDetails(Host selectedHost, Merchant selectedMerchant) {
        //SettlementDetailsActivity.startActivity(this, selectedHost, selectedMerchant);
        SettleActivity.startActivity(this, selectedHost, selectedMerchant);
    }

    @Override
    public void startMerchantSelectActivity(Host selectedHost) {
        MerchantSelectActivity.startActivityForResult(this, selectedHost, Const.TITLE_SETTLEMENT, MerchantType.ALL,
                ACTIVITY_RESULT_SETTLEMENT_MERCHANT_SELECT);
    }

    @Override
    public void gotoVoidActivity() {
        VoidActivity.startActivity(this);
    }

    @Override
    public void gotoReceiptTypeSelectActivity() {
        ReceiptTypeActivity.startActivity(this);
    }

    @Override
    public void gotoPreCompActivity() {
        PreCompActivity.startActivity(this);
    }


    @Override
    public void gotoMerchantListActivity() {
        MerchantListActivity.startActivity(this);
    }

    @Override
    public void gotoAmountActivity() {
        binding.unbind();
        mPresenter.onPause();
        AmountActivity.startActivity(this);
    }

    @Override
    public void onProceedVoidSale() {
        PasswordActivity.startActivityForResult(this, PasswordType.SUPER, ACTIVITY_RESULT_VOID_PW);
    }

    @Override
    public void onProceedPreCompSale() {
        PasswordActivity.startActivityForResult(this, PasswordType.SUPER, ACTIVITY_RESULT_PRE_COMP_PW);
    }

    @Override
    public void onProceedQRSale() {
        binding.unbind();
        mPresenter.onPause();
        AmountActivity.startActivity(this);
        //  QrActivity.startActivity(this);
    }

    @Override
    public void onProceedQRVerify() {
        // To be implement
    }

    @Override
    public void onProceedSettlement() {
        PasswordActivity.startActivityForResult(this, PasswordType.SUPER, ACTIVITY_RESULT_SETTLEMENT_PW);
    }

    @Override
    public void onManualSalePasswordRequest(int trantype) {
        UserNamePasswordInputActivity.startActivityForResult(this, trantype, ACTIVITY_RESULT_MANUALSALE_PW);
    }

    @Override
    public void onProceedClearReversal() {
        PasswordActivity.startActivityForResult(this, PasswordType.CLEAR_REVERSAL, ACTIVITY_RESULT_CLEAR_REVERSAL_PW);
    }

    @Override
    public void onProceedCheckReversal() {
        //remove password on dinesh aiyas comment 29-08-1011
        HostSelectActivity.startActivityForResult(this, Const.TITLE_CHECK_REVERSAL,
                ACTIVITY_RESULT_CHECK_REVERSAL_HOST);
    }

    @Override
    public void onMenuIconClicked(HomeMenuBean m) {
        if (mPresenter.cardIsStillIn()) {
            return;
        }

        if (lastTap != -1) {
            long dif = new Date().getTime() - lastTap;
            if (dif <= 600) {
                log("Double tap detected.");
                return;
            }
        }

        if (!PosDevice.getInstance().isPaperExistsInPrinter()) {
            log("Paper not exists in printer.");
            showDialogError(app_name, msg_feed_paper, () -> onMenuIconClicked(m));
            return;
        }

        lastTap = new Date().getTime();

        if (m.getType() == HomeMenuBean.TYPE_SALE) {
            mPresenter.onSaleClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_OFFLINE_SALE) {
            mPresenter.onOfflineSaleClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_MANUAL_SALE) {
            mPresenter.onManualSaleClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_OFFLINE_MANUAL_SALE) {
            mPresenter.onOfflineManualSaleClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_VOID) {
            mPresenter.onVoidClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_SETTLEMENT) {
            mPresenter.onSettlementClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_CASH_ADVANCE) {
            mPresenter.onCashAdvanceClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_CLEAR_REVERSAL) {
            mPresenter.onClearReversalClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_CASH_BACK) {
            mPresenter.onCashBackClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_PRE_AUTH) {
            mPresenter.onPreAuthClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_PRE_AUTH_MANUAL) {
            mPresenter.onPreAuthManualClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_PRE_COM) {
            mPresenter.onPreCompClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_INSTALMENT) {
            mPresenter.onInstallmentClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_QR_VERIFY) {
            mPresenter.onQrVerifyClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_REFUND) {
            mPresenter.onRefundSaleClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_REFUND_MANUAL) {
            mPresenter.onManualRefundClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_QUASI_CASH) {
            mPresenter.onQuasiCash();
        } else if (m.getType() == HomeMenuBean.TYPE_QUASI_CASH_MANUAL) {
            mPresenter.onQuasiCashManual();
        } else if (m.getType() == HomeMenuBean.TYPE_DETAIL_REPORT) {
            mPresenter.onPrintDetailReportClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_PRINT_LAST_RECEIPT) {
            mPresenter.onPrintLastReceiptClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_LAST_SETTLEMENT_RECEIPT) {
            mPresenter.onPrintLastSettlementReceiptClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_PRINT_ANY_RECEIPT) {
            mPresenter.onAnyReceiptClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_CHECK_REVERSAL) {
            mPresenter.onCheckReversalClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_STUDENT_REF_SALE) {
            mPresenter.onStudentRefSaleClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_AUTH_ONLY) {
            mPresenter.onAuthOnlyClicked();
        } else if (m.getType() == HomeMenuBean.TYPE_QR_SALE) {
            mPresenter.onQrSaleClicked();
        }
    }

    @Override
    public void selectHostForDetailReport() {
        HostSelectActivity.startActivityForResult(this,
                toolbar_detail_report, HOST_SELECT_FOR_DETAIL_REPORT);
    }

    @Override
    public void selectMerchantForDetailReport(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                toolbar_detail_report, MerchantType.ALL, MERCHANT_SELECT_FOR_DETAIL_REPORT);
    }

    @Override
    public void selectMerchantForLastSettlementReport(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host,
                toolbar_last_settlement, MerchantType.ALL, MERCHANT_SELECT_FOR_LAST_SETTLEMENT);
    }

    @Override
    public void selectHostForLastSettlementReport() {
        HostSelectActivity.startActivityForResult(this,
                toolbar_last_settlement, HOST_SELECT_FOR_LAST_SETTLEMENT);
    }

    @Override
    public void selectHostForAnyReceipt() {
        HostSelectActivity.startActivityForResult(this,
                toolbar_any_receipt, HOST_SELECT_FOR_ANY_RECEIPT);
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

    @Override
    public void selectMerchantForVoid(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host, Const.TITLE_VOID, MerchantType.ALL,
                ACTIVITY_RESULT_VOID_MERCHANT);
    }

    @Override
    public void selectMerchantForPreComp(Host host) {
        MerchantSelectActivity.startActivityForResult(this, host, Const.TITLE_PRE_COMP,
                MerchantType.SALE_SUPPORT_MERCHANTS, ACTIVITY_RESULT_PRE_COMP_MERCHANT);
    }

    @Override
    protected void onBatteryLevelChanged(int batteryLevel) {
        super.onBatteryLevelChanged(batteryLevel);
        mPresenter.setBatteryLevel(batteryLevel);
    }

    @Override
    protected void onChargerStatesChanged(boolean isCharging) {
        super.onChargerStatesChanged(isCharging);
        mPresenter.setChargerStatus(isCharging);
    }

    @Override
    public void updateBatteryLevelUi(boolean showBatteryLow, int level) {
        log("updateBatteryLevelUi() showBatteryLow: " + showBatteryLow + " | level: " + level);
        if (showBatteryLow) {
            binding.layoutBattery.setVisibility(View.VISIBLE);
        } else {
            binding.layoutBattery.setVisibility(View.GONE);
        }

        String b = msg_battery_level.replace("#bp#", String.valueOf(level));
        binding.tvBatteryText.setText(b);
    }

    @Override
    public void updateTmsstatuslUi(boolean showui, String msg, String color) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (showui) {
                        binding.layoutTMS.setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutTMS.setVisibility(View.GONE);
                    }
                    binding.tvtmsstatus.setTextColor(Color.parseColor(color));
                    binding.tvtmsstatus.setText(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int result, @Nullable Intent data) {
        super.onActivityResult(requestCode, result, data);
        if (requestCode == ACTIVITY_RESULT_VOID_PW) {
            if (result == RESULT_OK) {
                HostSelectActivity.startActivityForResult(this, Const.TITLE_VOID, ACTIVITY_RESULT_VOID_HOST);
            }
        } else if (requestCode == ACTIVITY_RESULT_VOID_HOST) {
            if (result == RESULT_OK) {
                Host host = data.getParcelableExtra(RESULT_HOST);
                mPresenter.onHostSelectedForVoid(host);
            }
        } else if (requestCode == ACTIVITY_RESULT_VOID_MERCHANT) {
            if (result == RESULT_OK) {
                Merchant selectedMerchant = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.onMerchantSelectedForVoid(selectedMerchant);
            }
        } else if (requestCode == ACTIVITY_RESULT_SETTLEMENT_PW) {
            if (result == RESULT_OK) {
                HostSelectActivity.startActivityForResult(this, Const.TITLE_SETTLEMENT,
                        ACTIVITY_RESULT_SETTLEMENT_HOST_SELECT);
            }
        } else if (requestCode == ACTIVITY_RESULT_MANUALSALE_PW) {
            if (result == RESULT_OK) {
                int tratypese = data.getIntExtra(TRANTYPE, 0);
                mPresenter.onManualSaleafterpassword(tratypese);
            }
        } else if (requestCode == ACTIVITY_RESULT_CLEAR_REVERSAL_PW) {
            if (result == RESULT_OK) {
                HostSelectActivity.startActivityForResult(this, Const.TITLE_CLEAR_REVERSAL,
                        ACTIVITY_RESULT_CLEAR_REVERSAL_HOST);
            }
        } else if (requestCode == ACTIVITY_RESULT_CHECK_REVERSAL_HOST) {
            if (result == RESULT_OK) {
                Host host = data.getParcelableExtra(RESULT_HOST);
                mPresenter.processCheckReversal(host);
            }
        } else if (requestCode == ACTIVITY_RESULT_CLEAR_REVERSAL_HOST) {
            if (result == RESULT_OK) {
                Host host = data.getParcelableExtra(RESULT_HOST);
                mPresenter.forceClearReversal(host);
            }
        } else if (requestCode == ACTIVITY_RESULT_PRE_COMP_PW) {
            if (result == RESULT_OK) {
                HostSelectActivity.startActivityForResult(this, Const.TITLE_PRE_COMP, ACTIVITY_RESULT_PRE_COMP_HOST);
            }
        } else if (requestCode == ACTIVITY_RESULT_PRE_COMP_HOST) {
            if (result == RESULT_OK) {
                Host host = data.getParcelableExtra(RESULT_HOST);
                mPresenter.onHostSelectedForPreComp(host);
            }
        } else if (requestCode == ACTIVITY_RESULT_PRE_COMP_MERCHANT) {
            if (result == RESULT_OK) {
                Merchant selectedMerchant = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.onMerchantSelectedForPreComp(selectedMerchant);
            }
        } else if (requestCode == ACTIVITY_RESULT_SETTLEMENT_HOST_SELECT) {
            if (result == RESULT_OK) {
                Host host = data.getParcelableExtra(RESULT_HOST);
                mPresenter.onSettlementHostSelected(host);
            }
        } else if (requestCode == ACTIVITY_RESULT_SETTLEMENT_MERCHANT_SELECT) {
            if (result == RESULT_OK) {
                Merchant selectedMerchant = data.getParcelableExtra(RESULT_MERCHANT);
                SettleActivity.startActivity(this, mPresenter.selectedHost, selectedMerchant);
            }
        } else if (requestCode == ACTIVITY_RESULT_SELECT_MERCHANT_FOR_CARD_SCAN) {
            if (result == RESULT_OK) {
                mPresenter.onMerchantGroupSelected(isCardSwiped);
            }
        } else if (requestCode == ACTIVITY_RESULT_SELECT_MERCHANT_ECR) {
            if (result == RESULT_OK) {
                mPresenter.onMerchantGroupSelectedForEcr();
            }
        } else if (requestCode == HOST_SELECT_FOR_DETAIL_REPORT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setSelectedHostForDetailReport(h);
            }
        } else if (requestCode == MERCHANT_SELECT_FOR_DETAIL_REPORT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setSelectedMerchantForDetailReport(m);
            }
        } else if (requestCode == HOST_SELECT_FOR_LAST_SETTLEMENT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setSelectedHostForSettlementReceipt(h);
            }
        } else if (requestCode == MERCHANT_SELECT_FOR_LAST_SETTLEMENT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setSelectedMerchantForSettlementReceipt(m);
            }
        } else if (requestCode == HOST_SELECT_FOR_ANY_RECEIPT) {
            if (result == RESULT_OK) {
                Host h = data.getParcelableExtra(RESULT_HOST);
                mPresenter.setSelectedHostForAntReceipt(h);
            }
        } else if (requestCode == MERCHANT_SELECT_FOR_ANY_RECEIPT) {
            if (result == RESULT_OK) {
                Merchant m = data.getParcelableExtra(RESULT_MERCHANT);
                mPresenter.setSelectedMerchantForAnyReceipt(m);
            }
        } else if (requestCode == INVOICE_SEARCH_FOR_ANY_RECEIPT) {
            if (result == RESULT_OK) {
                Transaction t = data.getParcelableExtra(InvoiceSearchActivity.RESULT_TRANSACTION);
                mPresenter.setTransactionForAnyReceipt(t);
            }
        } else if (requestCode == ACTIVITY_RESULT_CHECK_REVERSAL_PRINT_ERROR) {
            if (result == RESULT_OK) {
                mPresenter.checkReversalReprint();
            }
        }
    }

    @OnClick(R.id.ivMenu)
    public void onClickMenu(View view) {
        if (mPresenter.cardIsStillIn()) {
            return;
        }

        ReportMenuActivity.startActivity(this);
    }


    @OnClick(R.id.ivNotification)
    public void onClickNotification(View view) {
        if (mPresenter.cardIsStillIn()) {
            return;
        }
    }


    @Override
    public void onBackPressed() {

    }

    /**
     * Called when the activity is about to be destroyed.
     */
    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.unbind();
            binding = null;
        }
        //  PosDevice.getInstance().platform.disableUsbCdc();
        super.onDestroy();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }


    class CallForSDKTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... urls) {
            try {
                DbHandler.getInstance().getTCT(tct -> {
                    tmsurl = tct.getURL();
                    appID = "1001";
                    instanceCode = "EPIC";
                    institutecode = tct.getInstituteName();
                    appCode = tct.getAppcode();
                    cityCode = "10150";
                    echotimeout = tct.getEchoTimeOut();
                    Log.i("TMSURL", tmsurl);

                    callForSDK();
                });
                DbHandler.getInstance().getTMIFByHostAndMerchant(1, 1, new DbHandler.GetTMIFByHostAndMerchantListener() {
                    @Override
                    public void onReceived(Terminal terminal) {
                        basetid = terminal.getTerminalID();
                    }
                });

//                initSdk.setAPP_VERNUMBER(BuildConfig.VERSION_NAME);
//                hstin.setHostId(0);
//
//                String url = "http://192.168.80.254:8085/SIGNATURE/doProcess";
//                hstin.setUrl(new URL(url));
//                hstin.setSignatureUrl(new URL(url));
//                hstInfo.add(hstin);
//                initSdk.setHstInfo(hstInfo);
//
//
//                initSdk.setTID_LIST("00000000");
//
//                initSdk.setUSR_TOKEN_KEY("123456");


                int retVal = -1;


                while (retVal != 0) {
                    System.out.println("Trying to Connect with TMS");
                 //   retVal = initSdk.initSdk(initSdk.getHstInfo(), INIT_SDK.LOGIN_RQUEST_TMS);
                    Thread.sleep(60 * 1000);
                }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

    public void callForSDK() {
        path = Environment.getExternalStorageDirectory() + "/Download/";
        String serialNo = "", imieNo = "", imisi, model, simProviderName, simSerialNo = null;


        //throws sdk exception
        try {

            serialNo = PosDevice.getInstance().getSerialNo();
            Log.d("SERIALFROMNEXGO", serialNo);
            imieNo = serialNo + "b6f6";//"b6f6fe43be55e888";//getIMEI(MainActivity.this);
            Log.d("IMEI", imieNo);
            model = PosDevice.getInstance().getdevicemodel();


            if (simSerialNo == null) {
                simSerialNo = "0";
            }

            simProviderName = "";




        } catch (Exception ex) {
            Log.e("TMSConnection ", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void getgpsdata() {

        // Register the location listener with the LocationManager.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gpslocation=location;
                // Handle the new location data here.
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // You can also access other information like altitude, speed, and more.
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Handle location provider status changes here.
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Handle location provider enabled event here.
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Handle location provider disabled event here.
            }
        };


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    private String getsimip() {
        String phoneNumber="";
        // Get a reference to the TelephonyManager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Check for permission to read phone state (required for Android 10 and above)
        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            // Get the SIM card state
            int simState = telephonyManager.getSimState();

            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    // SIM card is absent
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    // SIM card is ready and operational
                    // Get the phone number
                    phoneNumber = telephonyManager.getLine1Number();
                    break;
                // Handle other SIM card states as needed
            }
        } else {
            // Handle permission denied case
        }
       return phoneNumber;
    }


    public static HomeActivity getInstance() {
        return instance;
    }
    public void restartapplication(){
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    // Perform periodic task of device info echo

    private final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            deviceStatus = new DeviceStatus();

            deviceStatus.setSerialNo(PosDevice.getInstance().getSerialNo());
            deviceStatus.setImei("12345");
            deviceStatus.setBatteryLevel("100");
            deviceStatus.setSignalStatus("-120");
            deviceStatus.setPrinterStatus("0");
            deviceStatus.setLatitude("6.906");
            deviceStatus.setLongitude("79.927");



            String devStatus = new Gson().toJson(deviceStatus);
            mPresenter.onEmitDeviceStatusEcho(devStatus);
            log("Device Echo Status: " + devStatus);
            handler.postDelayed(this, 1 * 30 * 1000); // This sets periodic time to 30 seconds
        }
    };


    @Override
    public void onDeviceStatusRequestReceived(String s) {
        // Your code to handle application download
        Log.i(TAG, "onDeviceStatusRequestReceived: " + s);


        deviceStatus = new DeviceStatus();
        deviceStatus.setSerialNo(PosDevice.getInstance().getSerialNo());
        deviceStatus.setImei("1234567890");
        deviceStatus.setBatteryLevel("20");
        deviceStatus.setSignalStatus("00");
        deviceStatus.setPrinterStatus("00");
        deviceStatus.setLatitude("6.9000");
        deviceStatus.setLongitude("79.8700");
        String devStatus = new Gson().toJson(deviceStatus);
        mainSDK.emitDeviceStatus(devStatus);
        log("Device Echo Status: " + devStatus);



    }

    @Override
    public void onProfileDownloadReceived(String s) {
        Log.i(TAG, "onProfileDownloadReceived: " + s);

        Gson gson = new Gson();
        ProfileData profileData = gson.fromJson(s, ProfileData.class);
        optCode = profileData.getOperationCode();
        mPresenter.onTMSOperationReceived(this,s, optCode);

    }

    @Override
    public void onApplicationDownloadReceived(String s) {
        log("onApplicationDownloadReceived: " + s);

        Gson gson = new Gson();
        DeviceOperationData deviceOperation = gson.fromJson(s, DeviceOperationData.class);
        optCode = deviceOperation.getCode();
        mPresenter.onTMSOperationReceived(this,s, optCode);
    }

    @Override
    public void onApplicationUninstallReceived(String s) {
        Log.i(TAG, "onApplicationUninstallReceived: " + s);
        Gson gson = new Gson();
        DeviceOperationData deviceOperation = gson.fromJson(s, DeviceOperationData.class);
        optCode = deviceOperation.getCode();

        mPresenter.onTMSOperationReceived(this,s, optCode);

    }

    @Override
    public void onDoSettlementReceived(String s) {
        Log.i(TAG, "onDoSettlementReceived: " + s);

        Gson gson = new Gson();
        DeviceOperationData deviceOperation = gson.fromJson(s, DeviceOperationData.class);
        optCode = deviceOperation.getCode();

        log("onDoSettlementReceived-getCode: " + deviceOperation.getCode());
        mPresenter.onTMSOperationReceived(this,s, optCode);
    }

    @Override
    public void onLogEnableRequest(String s) {
        Log.i(TAG, "onLogEnableRequest: " + s);

        mPresenter.onTMSOperationReceived(this,s, "009");
    }

    @Override
    public void onLogDisableRequest(String s) {
        Log.i(TAG, "onLogDisableRequest: " + s);
        mPresenter.onTMSOperationReceived(this,s, "010");

    }

    @Override
    public void onTerminalEnableRequest(String s) {
        Log.i(TAG, "onTerminalEnableRequest: " + s);

        mPresenter.onTMSOperationReceived(this,s, "008");
    }

    @Override
    public void onTerminalDisableRequest(String s) {
        Log.i(TAG, "onTerminalDisableRequest: " + s);
        mPresenter.onTMSOperationReceived(this,s, "007");
    }

    @Override
    public void onLogDownloadReceived(String s) {
        Log.i(TAG, "onLogDownloadReceived: " + s);

    }

    @Override
    public void onDebitKeyDownload(String s) {
        Log.d(TAG, "onDebitKeyDownload: "+ s);
        TMSActivity.downloaddebitkey(s,this);
    }

    @Override
    public void onTranSyncEnableReceived(String s) {
        Log.d(TAG, "onTranSyncEnableReceived: "+ s);
        mPresenter.onTMSOperationReceived(this,s, "022");
    }

    @Override
    public void onTranSyncDisableReceived(String s) {
        Log.d(TAG, "onTranSyncDisableReceived: "+ s);
        mPresenter.onTMSOperationReceived(this,s, "023");
    }

    @Override
    public void onEnableDeviceEchoPending(String s) {
        log("onEnableDeviceEchoPending: " + s);
        handler.post(runnableCode); // Start the periodic task
        mPresenter.onTMSOperationReceived(this,s, "019");

    }

    @Override
    public void onDisableDeviceEchoPending(String s) {
        Log.d(TAG, "onDisableDeviceEchoPending: "+ s);
        log("onDisableDeviceEchoPending: " + s);
        handler.removeCallbacks(runnableCode); // Stop the periodic task
        mPresenter.onTMSOperationReceived(this,s, "020");
    }

    @Override
    public void onBannerDownloadReceived(String s) {
        Log.d(TAG, "onBannerDownloadReceived: "+ s);
        mPresenter.onTMSOperationReceived(this,s, "024");

    }

    @Override
    public void onErrorReceived(String s) {
        Log.d(TAG, "onErrorReceived: "+ s);
    }

    public void restart(){
        log("Application installation-restart");
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        this.finishAffinity();
    }


    @Override
    public void emitTranData(String tranData) {

        mPresenter.onEmitTranData(tranData);
    }

    @Override
    public void sendTxnReceiptData(String receiptData) {
        mPresenter.onSendTxnReceiptData(receiptData);
    }

    @Override
    public void emitDebitKeyDownload(){
        mPresenter.onEmitDebitKeyDownload();

    }
}