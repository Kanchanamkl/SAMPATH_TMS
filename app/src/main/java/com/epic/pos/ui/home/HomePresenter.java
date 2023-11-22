package com.epic.pos.ui.home;

import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION;
import static com.epic.pos.common.TranTypes.SALE_PRE_AUTHORIZATION_MANUAL;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.epic.pos.BuildConfig;
import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.CardAction;
import com.epic.pos.device.data.CardData;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintDataBuilder;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.listener.AutoSettlementListener;
import com.epic.pos.device.listener.CardInListener;
import com.epic.pos.device.listener.PosCheckCardListener;
import com.epic.pos.device.listener.PrintListener;
import com.epic.pos.device.serial.ECRCom;
import com.epic.pos.device.serial.EcrRes;
import com.epic.pos.domain.entity.HomeMenuBean;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.ConfigMapTableHelper;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.helper.ProfileUpdateHelper;
import com.epic.pos.iso.modal.request.ReversalRequest;
import com.epic.pos.iso.modal.response.ReversalResponse;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.tle.TLEData;
import com.epic.pos.tms.TMSActivity;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.ui.newsettlement.auto.AutoSettleActivity;
import com.epic.pos.ui.settings.tms.DeviceOperationData;
import com.epic.pos.ui.settings.tms.DeviceStatus;
import com.epic.pos.util.AppLog;
import com.epic.pos.util.ImageUtils;
import com.epic.pos.util.Partition;
import com.example.atemhub.MainSDK;
import com.example.atemhub.OperationManager;
import com.example.atemhub.control.AppData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nexgo.oaf.apiv3.SdkResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class HomePresenter extends BasePresenter<HomeContract.View>
        implements HomeContract.Presenter, ECRCom.ECRCommandListener {

    private final String TAG = HomePresenter.class.getSimpleName();
    private Repository repository;
    private NetworkConnection networkConnection;

    private TCT tct;
    private int transactionCount = 0;

    private String errorMsg = "";
    public Host selectedHost;
    private boolean isPause = false;

    private CardInListener cardInListener = null;
    private boolean cardIsStillIn = false;
    private boolean isUpdatingProfile = false;
    private boolean onServiceError = false;

    private int batteryLevel = -1;
    private boolean isCharging = false;
    private String batteryLowToast = Const.MSG_CRITICAL_MSG;
    private boolean printingOnHome = false;

    private Host hostForDetailReport;
    private Merchant merchantForDetailReport;

    private Host hostForLastSettlementReport;
    private Merchant merchantForLastSettlementReport;

    private Host hostForAnyReceipt;
    private Merchant merchantForAnyReceipt;
    private Transaction transactionForAnyReceipt;

    private Handler handler = new Handler(Looper.getMainLooper());

    private String appName;
    private String appVersion;
    private String appPackageName;
    private String appLink;
    long downloadId = -1;
    private File downloadedApkFile;
    Context context;
    MainSDK mainSDK;
    private int installStatus = 100;
    private int uninstallStatus;
    int i = 1;


    @Inject
    public HomePresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    //serial com
    private ECRCom.EcrCommand ecrCommand;

    @Override
    public void startSerialCom() {


        serialLog("startSerialCom()");
        if (tct.getEcrEnabled() == 1) {
            log("ECR Enabled");
            PosDevice.getInstance().initECR();
            PosDevice.getInstance().setEcrInitListener(
                    () -> PosDevice.getInstance().getEcrCom().setListener(HomePresenter.this));
        } else {
            log("ECR disabled");
        }
    }

    @Override
    public void onECRCommandReceived(ECRCom.EcrCommand cmd, EcrRes res) {
        log("onECRCommandReceived()");
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                repository.getTCT(tct -> {
                    if (tct.getEcrEnabled() == 1) {
                        this.ecrCommand = cmd;
                        if (networkConnection.checkNetworkConnection()) {
                            resetData();
                            checkMerchantsForEcr();
                        } else {
                            if (isViewNotNull()) {
                                mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                            }
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    private void checkMerchantsForEcr() {
        log("checkMerchantsForEcr()");
        repository.getEnabledMerchants(repository.isInstallmentSale(), merchants -> {
            if (merchants.size() == 0) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_NO_MERCHANTS);
                }
            } else if (merchants.size() == 1) {
                //One merchant exists
                repository.saveTransactionOngoing(true);
                repository.saveSelectedMerchantGroupId(merchants.get(0).getGroupId());
                onMerchantGroupSelectedForEcr();
            } else {
                //Multiple terminals exists
                repository.saveTransactionOngoing(true);
                if (isViewNotNull()) {
                    mView.selectMerchantForEcr();
                }
            }
        });
    }

    @Override
    public void onMerchantGroupSelectedForEcr() {
        switch (ecrCommand) {
            case ECR_CMD_BIN_REQUEST:
                if (isViewNotNull()) {
                    mView.gotoEcrCardScanActivity();
                }
                break;
        }
    }

    //----


    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public boolean cardIsStillIn() {
        return cardIsStillIn;
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public void initData() {


        context = HomeActivity.getInstance();
        //appPackageName=context.getPackageName();
        mainSDK = new MainSDK(context);
        mainSDK.setOperationManagerCallback((OperationManager) context);

        log("initData()");
        Const.PRINT_ISO_MSG = repository.shouldPrintClearISOPacket();
        Const.PRINT_ENC_ISO_MSG = repository.shouldPrintEncryptedISOPacket();
        Const.IS_LOG_WRITE_ON_FILE = repository.isLogEnabled();
        resetData();

        PosDevice.getInstance().setInitListener(() -> {
            PosDevice.getInstance().setErrorListener(() -> {
                onServiceError = true;
                if (!isPause) {
                    if (isViewNotNull()) {
                        mView.restartActivity();
                    }
                }
            });
        });

        initMainSDK();


    }

    @Override
    public void getFeatures() {
        repository.getAllFeatures(features -> {
            if (isViewNotNull()) {
                mView.onFeaturesReceived(Partition.ofSize(features, 6));
            }
        });
    }

    @Override
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
        setBatteryStatusToUI();
    }

    @Override
    public void setChargerStatus(boolean isCharging) {
        this.isCharging = isCharging;
        setBatteryStatusToUI();
    }

    private void setBatteryStatusToUI() {
        if (tct != null && this.batteryLevel != -1) {
            if (isViewNotNull()) {
                boolean showBatteryLow = false;
                if (tct.getCriticalBatteryLevel() >= batteryLevel) {
                    showBatteryLow = true;
                } else {
                    if (!isCharging && tct.getBatteryLowLevel() >= this.batteryLevel) {
                        showBatteryLow = true;
                    }
                }


                mView.updateBatteryLevelUi(showBatteryLow, this.batteryLevel);
            }
        }
    }

    @Override
    public void onResume() {
        log("onResume()");
        Const.IS_TLE_ENABLE = false;
        if (onServiceError) {
            log("On service error");
            if (isViewNotNull()) {
                mView.restartActivity();
            }
        } else {
            isPause = false;
            if (cardIsStillIn) return;
            log("onResume else.");
            if (isViewNotNull()) {
                mView.setRootLayoutVisibility(false);
            }
            repository.getTCT(tct -> {
                if (isPause) return;
                log("tct received");
                HomePresenter.this.tct = tct;
                setBatteryLevel(batteryLevel);
                repository.getTransactionCount(count -> {
                    log("transaction count received: count = " + count);
                    HomePresenter.this.transactionCount = count;
                    if (!repository.isForceLoadHome()) {
                        log("force load home disabled");
                        if (tct.getStartWithAmountEnter() == 1) {
                            if (!repository.isTerminalDisabled()) {
                                log("start with amount activity enabled");
                                //get all sale support merchants group by group id
                                repository.getEnabledMerchants(false, merchants -> {
                                    if (isPause) return;
                                    if (merchants.size() == 1) {
                                        log("one sale support merchant group exists");
                                        repository.saveStartWithAmountActivity(true);
                                        repository.saveSelectedMerchantGroupId(merchants.get(0).getGroupId());
                                        if (isViewNotNull()) {
                                            if (isPause) return;
                                            resetData();
                                            mView.setRootLayoutVisibility(false);
                                            mView.gotoAmountActivity();
                                        }
                                    } else {
                                        log("more then one merchant groups exists");
                                        if (isViewNotNull()) {
                                            mView.setRootLayoutVisibility(true);
                                        }
                                        repository.saveStartWithAmountActivity(false);
                                        startCheckCard();
                                    }
                                });
                            } else {
                                log("terminal disabled");
                                if (isViewNotNull()) {
                                    mView.setRootLayoutVisibility(true);
                                }
                                repository.saveStartWithAmountActivity(false);
                                startCheckCard();
                            }
                        } else {
                            log("start with amount activity disabled");
                            if (isViewNotNull()) {
                                mView.setRootLayoutVisibility(true);
                            }
                            repository.saveStartWithAmountActivity(false);
                            startCheckCard();
                        }
                    } else {
                        log("force load home enabled");
                        if (isViewNotNull()) {
                            mView.setRootLayoutVisibility(true);
                        }
                        repository.saveStartWithAmountActivity(false);
                        startCheckCard();

                        if (tct.getStartWithAmountEnter() == 1) {
                            log("start home idle count down");
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000L * tct.getStartAmountHomeIdleTimeout());
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                    handler.post(() -> {
                                        log("home idle countdown finished");
                                        if (isPause) return;
                                        resetData();
                                        repository.saveForceLoadHome(false);
                                        onResume();
                                    });
                                }
                            }.start();
                        }
                    }
                });

                if (tct.getStartWithAmountEnter() != 1) {
                    new Thread() {
                        @Override
                        public void run() {
                            log("default home idle idle count down started");

                            try {
                                Thread.sleep(Const.DEFAULT_HOME_IDLE_COUNTDOWN);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            handler.post(() -> {
                                log("default home idle count down stopped");
                                if (isPause || cardIsStillIn) return;
                                if (!PosDevice.getInstance().isCardChecking()) {
                                    log("card is stopped checking");
                                    repository.saveTransactionOngoing(false);
                                    checkCard();
                                }
                            });
                        }
                    }.start();
                }
            });
        }
    }

    @Override
    public void updateProfile() {
        log("updateProfile()");
        log("startProfileDownload()");
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            repository.getTransactionCount(count -> {
                log("txn count: " + count);
                if (count >= 1) {
                    log("goto auto settle");
                    if (isViewNotNull()) {
                        mView.gotoAutoSettlementActivity();
                    }
                } else {
                    log("updating profile");
                    String profileData = repository.getProfileUpdateData();
                    if (!TextUtils.isEmpty(profileData)) {
                        log("profile data is exists"+ profileData);
                        isPause = true;
                        ProfileUpdateHelper p = new ProfileUpdateHelper(repository);

                        if (isViewNotNull()) {
                            mView.showLoader("Please wait", "Updating profile");
                        }

                        p.setUpdateCompleteListener(new ProfileUpdateHelper.UpdateCompleteListener() {
                            @Override
                            public void onCompleted() {
                                log("profile update completed");
                                MyApp.getInstance().getBgThread().setProfileUpdateOngoing(false);
                                repository.saveHasPendingProfileUpdate(false);
                                repository.saveProfileUpdateData("");
                                isPause = false;
                                if (isViewNotNull()) {
                                    mView.hideLoader();
                                    mView.onProfileUpdateCompleted();
                                }
                            }

                            @Override
                            public void onError() {
                                log("Profile update error.");
                                isPause = false;
                                if (isViewNotNull()) {
                                    mView.hideLoader();
                                    mView.showToastMessage("Profile update error!");
                                }
                            }
                        });
                        p.update(profileData);
                    } else {
                        log("profileData is null");
                    }
                }
            });
        } else {
            log("profile download cancelled - battery level critical: " + batteryLevel + "%");
        }
    }

    @Override
    public void generateConfigMap() {
        MyApp.getInstance().getBgThread().setGeneratingConfigMap(true);
        isPause = true;
        if (isViewNotNull()) {
            mView.showLoader("Sampath Bank", "Generating config map\nfor first time use.");
        }

        new ConfigMapTableHelper(repository).clearConfigMap(repository, Const.TABLES, () -> {
            new ConfigMapTableHelper(repository).insertDataToTable(repository, Const.TABLES, () -> {
                MyApp.getInstance().getBgThread().setGeneratingConfigMap(false);
                isPause = false;
                repository.saveConfigMapGenerated(true);
                if (isViewNotNull()) {
                    mView.hideLoader();
                    mView.showToastMessage("ConfigMap generation completed.");
                }
            });
        });
    }


    @Override
    public void UpdateCUPBinFile() {
//        MyApp.getInstance().getBgThread().setGeneratingConfigMap(true);
//        isPause = true;
//        if (isViewNotNull()) {
//            mView.showLoader("Sampath Bank", "Updating CUP BIN List");
//        }
//        new ConfigMapTableHelper(repository).clearConfigMap(repository, Const.TABLES, () -> {
//            new ConfigMapTableHelper(repository).insertDataToTable(repository, Const.TABLES, () -> {
//                MyApp.getInstance().getBgThread().setGeneratingConfigMap(false);
//                isPause = false;
//                repository.saveConfigMapGenerated(true);
//                if (isViewNotNull()) {
//                    mView.hideLoader();
//                    mView.showToastMessage("CUP BIN List Updated");
//                }
//            });
//        });

    }
    @Override
    public void tryToAutoSettle() {
        log("tryToAutoSettle()");
        if (!isUpdatingProfile) {
            if (batteryLevel >= tct.getCriticalBatteryLevel()) {
                repository.getTransactionCount(count -> {
                    log("txn count: " + count);
                    if (count >= 1) {
                        log("goto auto settle");
                        if (isViewNotNull()) {
                            mView.gotoAutoSettlementActivity();
                        }
                    } else {
                        log("auto settle cancelled - not txn exists");
                        incrementAutoSettlementDate(nextSettlementDate ->
                                log("next settlement date: " + nextSettlementDate));
                        repository.saveHasPendingAutoSettlement(false);
                    }
                });
            } else {
                log("auto settle cancelled - battery level critical: " + batteryLevel + "%");
                incrementAutoSettlementDate(nextSettlementDate ->
                        log("next settlement date: " + nextSettlementDate));
                repository.saveHasPendingAutoSettlement(false);
            }
        }
    }

    private void startCheckCard() {
        log("startCheckCard()");
        if (repository.isCheckRemoveCard()) {
            log("check card remove listener");
            if (cardInListener == null) {
                cardInListener = new CardInListener() {
                    @Override
                    public void cardIsStillIn() {
                        cardIsStillIn = true;
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_PLEASE_REMOVE_CARD);
                        }
                    }

                    @Override
                    public void onCardRemoved() {
                        cardIsStillIn = false;
                        cardInListener = null;
                        repository.saveCheckRemoveCard(false);
                        startCheckCard();
                    }

                    @Override
                    public void onError(Exception ex) {
                        cardIsStillIn = false;
                        cardInListener = null;
                        repository.saveCheckRemoveCard(false);
                        startCheckCard();
                    }
                };

                PosDevice.getInstance().setInitListener(() -> {
                    PosDevice.getInstance().isCardIn(cardInListener);
                });
            }
        } else {
            PosDevice.getInstance().setInitListener(this::checkCard);
        }
    }

    @Override
    public void onPause() {
        log("onPause()");
        isPause = true;
        isCheckCardRunning = false;
        PosDevice.getInstance().stopCheckCard();
        log("Card scan stopped");
    }

    // <editor-fold defaultstate="collapsed" desc="Pre comp functions">
    @Override
    public void onHostSelectedForPreComp(Host preCompHost) {
        repository.saveSelectedHostIdForPreComp(preCompHost.getHostID());
        repository.getSaleSupportMerchantsByHost(preCompHost.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
                }
            } else if (merchants.size() == 1) {
                onMerchantSelectedForPreComp(merchants.get(0));
            } else {
                if (isViewNotNull()) {
                    mView.selectMerchantForPreComp(preCompHost);
                }
            }
        });
    }

    @Override
    public void onMerchantSelectedForPreComp(Merchant merchant) {
        repository.saveTransactionOngoing(true);
        repository.saveSelectedMerchantIdForPreComp(merchant.getMerchantNumber());
        if (isViewNotNull()) {
            mView.gotoPreCompActivity();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Void functions">
    @Override
    public void onHostSelectedForVoid(Host voidHost) {
        repository.saveSelectedHostIdForVoid(voidHost.getHostID());
        repository.getEnabledMerchantsByHost(voidHost.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
                }
            } else if (merchants.size() == 1) {
                onMerchantSelectedForVoid(merchants.get(0));
            } else {
                if (isViewNotNull()) {
                    mView.selectMerchantForVoid(voidHost);
                }
            }
        });
    }

    @Override
    public void onMerchantSelectedForVoid(Merchant merchant) {
        repository.saveSelectedMerchantIdForVoid(merchant.getMerchantNumber());
        if (isViewNotNull()) {
            mView.gotoVoidActivity();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Card scan functions">
    private List<Merchant> merchantGroup = new ArrayList<>();
    private CardData cardSwipedCardData = null;
    private CardAction cardSwipedAction = null;
    private boolean isCheckCardRunning = false;

    @Override
    public void checkCard() {
        boolean isCardChecking = PosDevice.getInstance().isCardChecking();
        boolean isTxnOngoing = repository.isTransactionOngoing();
        log("checkCard() : cardChecking: " + isCardChecking + " | isTxnOngoing: " + isTxnOngoing + " | isPause: " + isPause);
        if (isCardChecking || isTxnOngoing || isPause) return;

        if (tct != null) {
            if (tct.getStartWithAmountEnter() == 1) {
                log("Abort check card: amount enter screen will start soon.");
                return;
            }
        }

        if (isCheckCardRunning){
            log("Abort check card: already running.");
            return;
        }

        if (printingOnHome){
            log("Abort check card: printer is busy.");
            return;
        }

        log("check card started");
        isCheckCardRunning = true;
        resetData();

        startSerialCom();

        PosDevice.getInstance().stopCheckCard();
        PosDevice.getInstance().setInitListener(() -> {
            PosDevice.getInstance().checkCard(
                    false,
                    true,
                    true,
                    false,
                    Const.CHECK_CARD_HOME_TIMEOUT,
                    new PosCheckCardListener() {

                        @Override
                        public void onCardInserted() {
                            isCheckCardRunning = false;
                            if (isViewNotNull()) {
                                mView.turnScreenOn();
                            }

                            if (!repository.isTerminalDisabled()) {
                                if (batteryLevel >= tct.getCriticalBatteryLevel()) {
                                    repository.saveCardInitiatedSale(true);
                                    checkMerchantsForCardScan(false);
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(batteryLowToast);
                                    }
                                    reCheckCardIfNotPaused();
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                                }
                                reCheckCardIfNotPaused();
                            }
                        }

                        @Override
                        public void onCheckCardError(int error, String msg) {
                            log("onCheckCardError() error: " + error + " | msg: " + msg);
                            isCheckCardRunning = false;
                            if (isViewNotNull()) {
                                mView.showToastMessage(Const.MSG_CARD_READ_ERROR);
                            }
                            reCheckCardIfNotPaused();
                        }

                        @Override
                        public void onEmvError(int result, Bundle data, String msg) {
                            log("onEmvError()");
                            isCheckCardRunning = false;
                            reCheckCardIfNotPaused();
                        }

                        @Override
                        public void onFallback() {
                            log("onFallback()");
                            isCheckCardRunning = false;
                            reCheckCardIfNotPaused();
                        }

                        @Override
                        public void onTimeOut() {
                            log("onTimeOut()");
                            isCheckCardRunning = false;
                            reCheckCardIfNotPaused();
                        }

                        @Override
                        public void onCardData(CardAction cardAction, CardData cardData) {
                            log("onCardData");
                            isCheckCardRunning = false;
                            if (!repository.isTerminalDisabled()) {
                                if (cardAction == CardAction.SWIPE) {
                                    if (batteryLevel >= tct.getCriticalBatteryLevel()) {
                                        cardSwipedAction = cardAction;
                                        cardSwipedCardData = cardData;
                                        checkCDTByPanForSwipe(cardData.getPan());
                                    } else {
                                        if (isViewNotNull()) {
                                            mView.showToastMessage(batteryLowToast);
                                        }
                                        reCheckCardIfNotPaused();
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                                }
                                reCheckCardIfNotPaused();
                            }
                        }

                        @Override
                        public void onSelectApplication(List<String> applications) {
                            isCheckCardRunning = false;
                            reCheckCardIfNotPaused();
                        }

                        @Override
                        public void onPinRequested(boolean isOnlinePin, int retryTimes) {
                            isCheckCardRunning = false;
                            log("onPinRequested()");
                        }

                        @Override
                        public void onRequestOnlineProcess() {
                            isCheckCardRunning = false;
                            log("onRequestOnlineProcess()");
                        }
                        @Override
                        public void onOfflineapprove() {
                            isCheckCardRunning = false;
                            log("onRequestOnlineProcess()");
                        }
                    });
        });
    }

    private void reCheckCardIfNotPaused(){
        log("reCheckCardIfNotPaused");
        if (!isPause){
            checkCard();
        }else {
            log("Activity paused");
        }
    }

    private void checkCDTByPanForSwipe(String pan) {
        log("checkCDTByPanForSwipe() pan: " + pan);
        repository.getCdtListByPan(pan, cdts -> {
            if (cdts.size() == 0) {
                log("invalid card");
                if (isViewNotNull()) {
                    mView.onCDTError();
                }
                checkCard();
            } else if (cdts.size() == 1) {
                //auto select card type
                onCDTSelectedForCardSwipe(cdts.get(0));
            } else {
                //let user to select card type
                if (isViewNotNull()) {
                    mView.onMultipleCDTReceived(cdts);
                }
            }
        });
    }

    @Override
    public void onCDTSelectedForCardSwipe(CardDefinition cardDefinition) {
        log("onCDTSelectedForCardSwipe()");
        if (cardDefinition.getChkSvcCode() == 0) {
            log("Check svc code disabled");
            repository.saveCardInitiatedSale(true);
            repository.saveCardAction(cardSwipedAction);
            repository.saveCardData(cardSwipedCardData);
            proceedCardSwipe(cardDefinition);
        } else {
            log("Check svc code enabled");
            if (!TextUtils.isEmpty(cardSwipedCardData.getServiceCode())
                    && (cardSwipedCardData.getServiceCode().startsWith("2") || cardSwipedCardData.getServiceCode().startsWith("6"))) {
                log("chip card swiped");
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_PLEASE_INSERT_THE_CARD);
                }
                checkCard();
            } else {
                log("mag card swiped");
                repository.saveCardInitiatedSale(true);
                repository.saveCardAction(cardSwipedAction);
                repository.saveCardData(cardSwipedCardData);
                proceedCardSwipe(cardDefinition);
            }
        }
    }

    private void proceedCardSwipe(CardDefinition cardDefinition) {
        if (!isCardExpired(cardDefinition)) {
            log("card not expired");
            repository.saveSelectedCardDefinitionId(cardDefinition.getId());
            checkMerchantsForCardScan(true);
        } else {
            log("card expired");
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_CARD_EXPIRED);
            }
            checkCard();
        }
    }

    private void checkMerchantsForCardScan(boolean isCardSwiped) {
        if (isPause) return;
        log("checkMerchantsForCardScan()");

        repository.getFeature(HomeMenuBean.TYPE_SALE, feature -> {
            if (feature.getEnabled() == 1) {
                log("sale is enabled");
                isCashAdvanceEnabled(isEnabled -> {
                    if (!isEnabled) {
                        repository.getEnabledMerchants(false, merchants -> {
                            log("enableMerchants() " + merchants.size());
                            if (merchants.size() == 0) {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_NO_MERCHANTS);
                                }
                                reCheckCardIfNotPaused();
                            } else if (merchants.size() == 1) {
                                //One merchant exists
                                repository.saveSelectedMerchantGroupId(merchants.get(0).getGroupId());
                                onMerchantGroupSelected(isCardSwiped);
                            } else {
                                //Multiple terminals exists
                                repository.saveTransactionOngoing(true);
                                if (isViewNotNull()) {
                                    mView.gotoMerchantListActivityForCardScan(isCardSwiped);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                        }
                    }
                });
            } else {
                log("sale is disabled");
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_SALE_IS_DISABLED);
                }
            }
        });
    }

    @Override
    public void onMerchantGroupSelected(boolean isCardSwiped) {
        int selectedMerchantGroup = repository.getSelectedMerchantGroupId();
        repository.getEnabledMerchantsFromGroupId(selectedMerchantGroup, merchants -> {
            merchantGroup = merchants;
            if (isCardSwiped) {
                //mag card
                validateAndProceed();
            } else {
                //chip card
                //commented stop and abort to attempt of fixing 39694 bug
                //PosDevice.getInstance().stopCheckCard();
                //PosDevice.getInstance().abortPBOC();
                repository.saveCardInitiatedSaleWithChip(true);
                repository.saveTransactionOngoing(true);
                if (isViewNotNull()) {
                    log("chip card merchant group selected.");
                    if (!isPause) {
                        mView.gotoAmountActivity();
                    }
                }
            }
        });
    }

    private void validateAndProceed() {
        log("validateAndProceed()");
        //Get CardDefinition - CDT
        repository.getCardDefinitionById(repository.getSelectedCardDefinitionId(), cardDefinition -> {
            //Get Issuer - IIT
            repository.getIssuerById(cardDefinition.getIssuerNumber(), issuer -> {
                if (issuer != null) {
                    //Get Host - IHT
                    repository.getIssuerContainsHost(issuer.getIssuerNumber(), host -> {
                        if (host != null) {
                            //Check must settle status on host
                            if (host.getMustSettleFlag() == 0) {
                                //Check host support for selected merchant group
                                Merchant merchant = getHostSupportedMerchantFromGroup(host);
                                if (merchant != null) {
                                    //Card host support for selected merchant
                                    //Get Terminal for merchant - TMIF
                                    repository.getTerminalByMerchant(merchant.getMerchantNumber(), terminal -> {
                                        if (terminal != null) {
                                            PosDevice.getInstance().stopCheckCard();
                                            PosDevice.getInstance().setTerminalID(terminal.getTerminalID());
                                            repository.saveSelectedTerminalId(terminal.getID());
                                            repository.saveCardInitiatedSaleWithChip(false);
                                            repository.saveTransactionOngoing(true);
                                            repository.saveCardInitiatedSale(true);
                                            if (isViewNotNull()) {
                                                if (!isPause) {
                                                    mView.gotoAmountActivity();
                                                }
                                            }
                                        } else {
                                            if (isViewNotNull()) {
                                                mView.showToastMessage(Const.MSG_TERMINAL_NOT_FOUND_FOR_MERCHANT);
                                            }
                                            checkCard();
                                        }
                                    });
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_HOST_NOT_SUPPORT_FOR_MERCHANT);
                                    }
                                    checkCard();
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_SETTLEMENT_PENDING);
                                }
                                checkCard();
                            }
                        } else {
                            if (isViewNotNull()) {
                                mView.showToastMessage(Const.MSG_HOST_NOT_FOUND);
                            }
                            checkCard();
                        }
                    });
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_ISSUER_NOT_FOUND);
                    }
                    checkCard();
                }
            });
        });
    }

    private boolean isCardExpired(CardDefinition cardDefinition) {
        CardData cardData = repository.getCardData();
        boolean isCardExpired = true;
        try {
            String cYear = cardData.getExpiryDate().substring(0, 2);
            String cMonth = cardData.getExpiryDate().substring(2, 4);
            log("Year: " + cYear);
            log("Month: " + cMonth);

            Calendar cardCalendar = Calendar.getInstance();
            cardCalendar.set(Calendar.YEAR, 2000 + Integer.parseInt(cYear));
            cardCalendar.set(Calendar.MONTH, Integer.parseInt(cMonth) - 1);
            log("Card expire: " + new SimpleDateFormat("yyyy-MM").format(cardCalendar.getTime()));
            Date expiry = cardCalendar.getTime();

            Calendar expCalendar = Calendar.getInstance();
            expCalendar.setTime(expiry);
            expCalendar.add(Calendar.MONTH, 1);
            isCardExpired = expCalendar.getTime().before(new Date());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (cardDefinition.getExpDateRequired() == 1) {
            //check card expiration
            return isCardExpired;
        } else {
            //card expiration check not required
            if (isCardExpired) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_PROCEED_WITH_EXPIRED_CARD);
                }
            }
            return false;
        }
    }

    private Merchant getHostSupportedMerchantFromGroup(Host host) {
        for (Merchant m : merchantGroup) {
            if (m.getHostId() == host.getHostID()) {
                return m;
            }
        }

        return null;
    }


    // </editor-fold>

    private void isCashAdvanceEnabled(FeatureStatusListener listener) {
        isFeatureEnabled(HomeMenuBean.TYPE_CASH_ADVANCE, listener);
    }

    private void isFeatureEnabled(int featureId, FeatureStatusListener listener) {
        repository.getFeature(featureId, feature ->
                listener.onFeatureStatus(feature.getEnabled() == 1));
    }

    public void onAuthOnlyClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_SALE, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    if (repository.isForceLoadHome()) {
                                        repository.saveForceLoadHome(false);
                                    }
                                    resetData();
                                    repository.setAuthOnlySale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }

    }

    private interface FeatureStatusListener {
        void onFeatureStatus(boolean isEnabled);
    }

    @Override
    public void onSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_SALE, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                             //   if (networkConnection.checkNetworkConnection()) {
                                    if (repository.isForceLoadHome()) {
                                        repository.saveForceLoadHome(false);
                                    }
                                    resetData();
                                    prepareSaleFLow();
                             //   } else {
                              //      if (isViewNotNull()) {
                              //          mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                //    }
                              //  }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onOfflineManualSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_OFFLINE_MANUAL_SALE, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if(tct.getManualSalePassword()==1){
                                    if (isViewNotNull()) {
                                        mView.onManualSalePasswordRequest(HomeMenuBean.TYPE_OFFLINE_MANUAL_SALE);
                                    }
                                }else{
                                    resetData();
                                    repository.setOfflineManualSale(true);
                                    prepareSaleFLow();
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onOfflineSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_OFFLINE_SALE, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                resetData();
                                repository.setOfflineSale(true);
                                prepareSaleFLow();
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }
    @Override
    public  void onManualSaleafterpassword(int tran){

        if(tran==HomeMenuBean.TYPE_MANUAL_SALE){
            resetData();
            repository.setManualSale(true);
            prepareSaleFLow();
        }
        if(tran==HomeMenuBean.TYPE_OFFLINE_MANUAL_SALE){
            resetData();
            repository.setOfflineManualSale(true);
            prepareSaleFLow();
        }

    }
    @Override
    public void onManualSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_MANUAL_SALE, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    if(tct.getManualSalePassword()==1){
                                        if (isViewNotNull()) {
                                        mView.onManualSalePasswordRequest(HomeMenuBean.TYPE_MANUAL_SALE);
                                        }
                                    }else{
                                        resetData();
                                        repository.setManualSale(true);
                                        prepareSaleFLow();
                                    }
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onVoidClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_VOID, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                resetData();
                                repository.setVoidSale(true);
                                prepareSaleFLow();
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onSettlementClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_SETTLEMENT, featureEnabled -> {
                    if (isViewNotNull()) {
                        if (featureEnabled) {
                            mView.onProceedSettlement();
                        } else {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onClearReversalClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_CLEAR_REVERSAL, featureEnabled -> {
                    if (isViewNotNull()) {
                        if (featureEnabled) {
                            mView.onProceedClearReversal();
                        } else {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onPreAuthClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_PRE_AUTH, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setPreAuthSale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onPreCompClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_PRE_COM, featureEnable -> {
                    if (featureEnable) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                resetData();
                                repository.setPreCompSale(true);
                                prepareSaleFLow();
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onPreAuthManualClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_PRE_AUTH_MANUAL, featureEnable -> {
                    if (featureEnable) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setPreAuthManualSale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onInstallmentClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_INSTALMENT, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setInstallmentSale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onRefundSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_REFUND, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setRefundSale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onManualRefundClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_REFUND_MANUAL, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setRefundManualSale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onCashBackClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_CASH_BACK, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setCashBackSale(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onQuasiCash() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_QUASI_CASH, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setQuasiCashFlow(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onQuasiCashManual() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_QUASI_CASH_MANUAL, featureEnabled -> {
                    if (featureEnabled) {
                        isCashAdvanceEnabled(isEnabled -> {
                            if (!isEnabled) {
                                if (networkConnection.checkNetworkConnection()) {
                                    resetData();
                                    repository.setQuasiCashManualFlow(true);
                                    prepareSaleFLow();
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                    }
                                }
                            } else {
                                if (isViewNotNull()) {
                                    mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                }
                            }
                        });
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onCashAdvanceClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                repository.getFeature(HomeMenuBean.TYPE_CASH_ADVANCE, feature -> {
                    if (feature.getEnabled() == 1) {
                        if (networkConnection.checkNetworkConnection()) {
                            resetData();
                            repository.setCashAdvance(true);
                            prepareSaleFLow();
                        } else {
                            if (isViewNotNull()) {
                                mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                            }
                        }
                    } else {
                        if (isViewNotNull()) {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    @Override
    public void onPrintDetailReportClicked() {
        if (!repository.isTerminalDisabled()) {
            repository.getFeature(HomeMenuBean.TYPE_DETAIL_REPORT, feature -> {
                if (feature.getEnabled() == 1) {
                    if (isViewNotNull()) {
                        mView.selectHostForDetailReport();
                    }
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                    }
                }
            });
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
            }
        }
    }

    @Override
    public void onPrintLastReceiptClicked() {
        if (!repository.isTerminalDisabled()) {
            repository.getFeature(HomeMenuBean.TYPE_PRINT_LAST_RECEIPT, feature -> {
                if (feature.getEnabled() == 1) {
                    repository.getLastTransaction(transaction -> {
                        if (transaction != null) {
                            if (isViewNotNull()) {
                            mView.gotoReceiptTypeSelectActivity();
                            }
                          //  printDuplicateTxnCopy(transaction,true);


                        } else {
                            mView.showToastMessage(Const.MSG_TXN_EMPTY);
                        }
                    });
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                    }
                }
            });
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
            }
        }
    }

    @Override
    public void onPrintLastSettlementReceiptClicked() {
        if (!repository.isTerminalDisabled()) {
            repository.getFeature(HomeMenuBean.TYPE_LAST_SETTLEMENT_RECEIPT, feature -> {
                if (feature.getEnabled() == 1) {
                    if (isViewNotNull()) {
                        mView.selectHostForLastSettlementReport();
                    }
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                    }
                }
            });
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
            }
        }
    }

    @Override
    public void onAnyReceiptClicked() {
        if (!repository.isTerminalDisabled()) {
            repository.getFeature(HomeMenuBean.TYPE_PRINT_ANY_RECEIPT, feature -> {
                if (feature.getEnabled() == 1) {
                    if (isViewNotNull()) {
                        mView.selectHostForAnyReceipt();
                    }
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                    }
                }
            });
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
            }
        }
    }

    @Override
    public void onCheckReversalClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_CLEAR_REVERSAL, featureEnabled -> {
                    if (isViewNotNull()) {
                        if (featureEnabled) {
                            mView.onProceedCheckReversal();
                        } else {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    void processCheckReversal(Host host) {
        repository.getReversalsByHost(host.getHostID(), reversals -> {
            if (reversals.isEmpty()) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_NO_REVERSALS);
                }
            } else {
                if (networkConnection.checkNetworkConnection()) {
                    pendingReversal = reversals.get(0);
                    sendPendingReversalRequest();
                } else {
                    if (isViewNotNull()) {
                        mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                    }
                }
            }
        });
    }

    @Override
    public void onStudentRefSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
                isFeatureEnabled(HomeMenuBean.TYPE_STUDENT_REF_SALE, featureEnabled -> {
                    if (isViewNotNull()) {
                        if (featureEnabled) {
                            isCashAdvanceEnabled(isEnabled -> {
                                if (!isEnabled) {
                                    if (networkConnection.checkNetworkConnection()) {
                                        resetData();
                                        repository.setStudentRefSale(true);
                                        prepareSaleFLow();
                                    } else {
                                        if (isViewNotNull()) {
                                            mView.showToastMessage(Const.MSG_CHECK_CONNECTION);
                                        }
                                    }
                                } else {
                                    if (isViewNotNull()) {
                                        mView.showToastMessage(Const.MSG_CASH_ADVANCE_ENABLED);
                                    }
                                }
                            });
                        } else {
                            mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                        }
                    }
                });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Pending Reversal Functions">
    private Reversal pendingReversal = null;

    @Override
    public void checkReversalReprint() {
        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_PRINTING_RECEIPT, Const.MSG_PRINTING_CANCEL_TXN_RECEIPT);
        }
        printCheckReversalReceipt();
    }

    private void printCheckReversalReceipt() {
        repository.getMerchantById(pendingReversal.getMerchant_no(), merchant ->
                repository.getIssuerById(pendingReversal.getIssuer_number(), issuer -> {
                    PosDevice.getInstance().startPrinting();

                    MyApp.getInstance().getAppReceipts().generateCancelTxnReceipt(pendingReversal, merchant, issuer,new AppReceipts.CancelReceiptListener() {
                        @Override
                        public void onReceiptGenerated(Bitmap bitmap) {
                            Print p = new Print();
                            p.setPrintType(Print.PRINT_TYPE_IMAGE);
                            p.setBitmap(bitmap);
                            p.setPrintListener(new PrintListener() {
                                @Override
                                public void onPrintFinished() {
                                    try {
                                        mView.hideLoader();
                                        bitmap.recycle();
                                        PosDevice.getInstance().stopPrinting();


                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                }

                                @Override
                                public void onPrintError(PrintError printError) {
                                    try {
                                        mView.hideLoader();
                                        mView.onCheckReversalPrintError(printError.getMsg());
                                        PosDevice.getInstance().stopPrinting();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                            PosDevice.getInstance().addToPrintQueue(p);
                        }

                        @Override
                        public void onReceiptFailed() {
                            if (isViewNotNull()) {
                                mView.hideLoader();
                                mView.gotoFailedActivity(Const.MSG_PRINT_ERROR, Const.MSG_RECEIPT_GENERATE_FAILED);
                            }
                        }
                    });
                }));
    }

    private void sendPendingReversalRequest() {
        log("sendPendingReversalRequest()");
        if (isViewNotNull()) {
            mView.showLoader(Const.MSG_REVERSAL_REQUEST, Const.MSG_PLEASE_WAIT);
        }

        ReversalRequest pendingRReq = createReversalRequest(pendingReversal);

        repository.getHostByHostId(pendingReversal.getHost(), host -> {
            repository.getIssuerById(pendingReversal.getIssuer_number(), issuer -> {
                TLEData tleData = new TLEData();
                tleData.setChipStatus(Integer.parseInt(pendingRReq.getPosEntryMode()));
                tleData.setHostId(host.getHostID());
                tleData.setIssuerId(issuer.getIssuerNumber());
                tleData.setPan(pendingRReq.getPan());
                tleData.setTrack2(pendingRReq.getTrack2Data());
                tleData.setTleEnable(host.getTLEEnabled() == 1);

                repository.reversalRequest(issuer, pendingRReq, tleData, new Repository.ReversalTransactionListener() {
                    @Override
                    public void onReceived(ReversalResponse reversalResponse) {
                        AppLog.i(TAG, "REVERSAL RES CODE: " + reversalResponse.getResponseCode());
                        validateReversal(pendingRReq, reversalResponse, (isValid, error) -> {
                            if (isValid) {
                                log("Reversal response validated.");
                                repository.deleteReversal(pendingReversal, () -> {
                                    log("Reversal successfully cleared.");
                                    printCheckReversalReceipt();
                                });
                            } else {
                                log("Reversal validation failed.");
                                if (isViewNotNull()) {
                                    mView.hideLoader();
                                    mView.gotoFailedActivity(Const.TITLE_REVERSAL_REQ, error);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log("Reversal error.");
                        throwable.printStackTrace();
                        if (isViewNotNull()) {
                            mView.hideLoader();
                            mView.gotoFailedActivity(Const.TITLE_REVERSAL_REQ, Const.MSG_REVERSAL_ERROR);
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void TLEError(String error) {
                        log("Reversal request - TLE error.");
                        if (isViewNotNull()) {
                            mView.hideLoader();
                            mView.gotoFailedActivity(Const.TITLE_REVERSAL_REQ, Const.MSG_REVERSAL_ERROR);
                        }
                    }
                });
            });
        });
    }
    // </editor-fold>

    @Override
    public void onQrSaleClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
        repository.getFeature(HomeMenuBean.TYPE_QR_SALE, feature -> {
            if (feature.getEnabled() == 1) {
                resetData();
                repository.setQrSale(true);
                checkQrEnableTerminals();
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                }
            }
        });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }
    @Override
    public void onQrVerifyClicked() {
        if (batteryLevel >= tct.getCriticalBatteryLevel()) {
            if (!repository.isTerminalDisabled()) {
        repository.getFeature(HomeMenuBean.TYPE_QR_VERIFY, feature -> {
            if (feature.getEnabled() == 1) {
                resetData();
                repository.setQrSale(true);
                checkQrEnableTerminals();
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_FEATURE_IS_DISABLED);
                }
            }
        });
            } else {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_TERMINAL_DISABLED);
                }
            }
        } else {
            if (isViewNotNull()) {
                mView.showToastMessage(batteryLowToast);
            }
        }
    }
    private void resetData() {
        log("resetData()");
        repository.setIsCardPinEntered(false);
        repository.saveOfflineApprovalCode("");
        repository.setSignatureRequired(false);
        repository.saveTotalAmount("");
        repository.saveStudentReferenceNo("");

        repository.setManualSale(false);
        repository.setOfflineSale(false);
        repository.setOfflineManualSale(false);
        repository.setQrSale(false);
        repository.setVoidSale(false);
        repository.setPreAuthSale(false);
        repository.setAuthOnlySale(false);
        repository.setPreAuthManualSale(false);
        repository.setInstallmentSale(false);
        repository.setPreCompSale(false);
        repository.setRefundSale(false);
        repository.setRefundManualSale(false);
        repository.setCashBackSale(false);
        repository.setQuasiCashFlow(false);
        repository.setQuasiCashManualFlow(false);
        repository.setCashAdvance(false);
        repository.setEcrInitiatedSale(false);
        repository.setStudentRefSale(false);

        repository.saveCardInitiatedSale(false);
        repository.saveCardInitiatedSalePinRequested(false);
        repository.saveCardInitiatedSaleIsOnlinePin(false);
        repository.saveTransactionOngoing(false);
        repository.saveCheckRemoveCard(false);
        repository.setOnlinePinRequested(false);
    }

    private void prepareSaleFLow() {
        if (repository.isPreCompSale()) {
            if (isViewNotNull()) {
                mView.onProceedPreCompSale();
            }
        } else if (repository.isVoidSale()) {
            if (isViewNotNull()) {
                mView.onProceedVoidSale();
            }
        } else {
            checkMerchants();
        }
    }

    private void checkQrEnableTerminals() {
      //  if (isViewNotNull()) {
            mView.onProceedQRSale();
      //  }
    }

    private void checkMerchants() {
        repository.getEnabledMerchants(repository.isInstallmentSale(), merchants -> {
            if (merchants.size() == 0) {
                if (isViewNotNull()) {
                    mView.showToastMessage(Const.MSG_NO_MERCHANTS);
                }
            } else if (merchants.size() == 1) {
                //One merchant exists
                repository.saveTransactionOngoing(true);
                repository.saveSelectedMerchantGroupId(merchants.get(0).getGroupId());
                if (isViewNotNull()) {
                    log("checkMerchants()");
                    if (!isPause) {
                        mView.gotoAmountActivity();
                    }
                }
            } else {
                //Multiple terminals exists
                repository.saveTransactionOngoing(true);
                if (isViewNotNull()) {
                    mView.gotoMerchantListActivity();
                }
            }
        });
    }

    @Override
    public void forceClearReversal(Host host) {
        repository.getReversalsByHost(host.getHostID(), reversals -> {
            if (!reversals.isEmpty()) {
                for (Reversal r : reversals) {
                    repository.deleteReversal(r, () -> {
                    });
                }

                if (isViewNotNull()) {
                    mView.forceClearReversalSuccess(1);
                }
            } else {
                if (isViewNotNull()) {
                    mView.forceClearReversalSuccess(0);
                }
            }
        });
    }

//    public void updateProfileData(String profileData) {
//        log("updateProfileData()");
//        repository.getTransactionCount(count -> {
//            log("transaction count: " + count);
//            if (count == 0) {
//                log("profile update");
//                isUpdatingProfile = true;
//                ProfileUpdateHelper p = new ProfileUpdateHelper(repository);
//                if (isViewNotNull()) {
//                    mView.showLoader("Please wait", "Updating profile");
//                }
//                p.setUpdateCompleteListener(() -> {
//                    isUpdatingProfile = false;
//                    repository.saveHasPendingProfileUpdate(false);
//                    repository.saveProfileUpdateData("");
//                    if (isViewNotNull()) {
//                        mView.hideLoader();
//                        mView.onProfileUpdateSuccess();
//                    }
//                });
//                p.update(profileData);
//            } else {
//                log("transactions exists try to auto settle");
//                repository.saveHasPendingProfileUpdate(true);
//                repository.saveProfileUpdateData(profileData);
//                tryToAutoSettle();
//            }
//        });
//    }

    //region Settlement BL
    public void onSettlementHostSelected(Host host) {
        this.selectedHost = host;
        getMerchantList(host);
    }

    public void getMerchantList(Host host) {
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (isViewNotNull()) {
                if (merchants.size() == 0) {
                    mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
                } else if (merchants.size() == 1) {
                    mView.startSettlementDetails(selectedHost, merchants.get(0));
                } else {
                    mView.startMerchantSelectActivity(selectedHost);
                }
            }
        });
    }
    //endregion

    private boolean isViewNotNull() {
        return mView != null;
    }

    private void serialLog(String msg) {
        AppLog.i(TAG + "_Serial", msg);
    }

    // <editor-fold defaultstate="collapsed" desc="Any receipt">
    @Override
    public void setSelectedHostForAntReceipt(Host host) {
        hostForAnyReceipt = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setSelectedMerchantForAnyReceipt(merchants.get(0));
            } else {
                mView.selectMerchantForAnyReceipt(host);
            }
        });
    }

    @Override
    public void setSelectedMerchantForAnyReceipt(Merchant merchant) {
        merchantForAnyReceipt = merchant;
        if (isViewNotNull()) {
            mView.selectInvoiceForAnyReceipt(hostForAnyReceipt, merchantForAnyReceipt);
        }
    }

    @Override
    public void setTransactionForAnyReceipt(Transaction transaction) {
        transactionForAnyReceipt = transaction;
        printDuplicateTxnCopy(transactionForAnyReceipt,true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Last settlement receipt">
    @Override
    public void setSelectedHostForSettlementReceipt(Host host) {
        hostForLastSettlementReport = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setSelectedMerchantForSettlementReceipt(merchants.get(0));
            } else {
                mView.selectMerchantForLastSettlementReport(host);
            }
        });
    }

    @Override
    public void setSelectedMerchantForSettlementReceipt(Merchant merchant) {
        merchantForLastSettlementReport = merchant;
        printLastSettlementReport();
    }

    public void printLastSettlementReport() {
        log("printLastSettlementReport()");
        printingOnHome = true;
        PosDevice.getInstance().stopEmvFlow();
        PosDevice.getInstance().startPrinting();

        Bitmap settlementReceipt = ImageUtils.getInstance()
                .getCustomerSettlementReceipt(hostForLastSettlementReport.getHostName(),
                        merchantForLastSettlementReport.getMerchantID());
        if (settlementReceipt != null) {
            mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
            PosDevice.getInstance().startPrinting();

            Print p = new Print();
            p.setPrintType(Print.PRINT_TYPE_IMAGE);
            p.setBitmap(settlementReceipt);
            p.setPrintListener(new PrintListener() {
                @Override
                public void onPrintFinished() {
                    PosDevice.getInstance().stopPrinting();
                    mView.hideLoader();

                    printingOnHome = false;
                    if (!isCheckCardRunning){
                        onResume();
                    }
                }

                @Override
                public void onPrintError(PrintError printError) {
                    PosDevice.getInstance().stopPrinting();
                    mView.showToastMessage("Printer Error: " + printError.getMsg());
                    mView.hideLoader();

                    printingOnHome = false;
                    if (!isCheckCardRunning){
                        onResume();
                    }
                }
            });
            PosDevice.getInstance().addToPrintQueue(p);
        } else {
            mView.showToastMessage(Const.MSG_LAST_SETTLEMENT_NOT_FOUND);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Print duplicate copy">
    private void printDuplicateTxnCopy(Transaction transaction,Boolean ismerchantcopy) {
        log("printDuplicateTxnCopy()");
        printingOnHome = true;
        PosDevice.getInstance().stopEmvFlow();

        mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
        PosDevice.getInstance().startPrinting();
        repository.getIssuerById(transaction.getIssuer_number(), issuer ->
                repository.getMerchantById(transaction.getMerchant_no(), merchant ->
                        MyApp.getInstance().getAppReceipts().generateTxnDuplicateReceipt(transaction, merchant, issuer,ismerchantcopy,
                                new AppReceipts.DuplicateReceiptListener() {
                                    @Override
                                    public void onReceiptGenerated(Bitmap bitmap) {
                                        Print p = new Print();
                                        p.setPrintType(Print.PRINT_TYPE_IMAGE);
                                        p.setBitmap(bitmap);
                                        p.setPrintListener(new PrintListener() {
                                            @Override
                                            public void onPrintFinished() {
                                                try {
                                                    mView.hideLoader();
                                                    bitmap.recycle();
                                                    PosDevice.getInstance().stopPrinting();

                                                    printingOnHome = false;
                                                    if (!isCheckCardRunning){
                                                        onResume();
                                                    }
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onPrintError(PrintError printError) {
                                                try {
                                                    printingOnHome = false;
                                                    mView.hideLoader();
                                                    mView.showToastMessage(printError.getMsg());
                                                    PosDevice.getInstance().stopPrinting();

                                                    if (!isCheckCardRunning){
                                                        onResume();
                                                    }
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        });
                                        PosDevice.getInstance().addToPrintQueue(p);
                                    }

                                    @Override
                                    public void onReceiptFailed() {
                                        try {
                                            printingOnHome = false;
                                            mView.hideLoader();
                                            PosDevice.getInstance().stopPrinting();

                                            if (!isCheckCardRunning){
                                                onResume();
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                })));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Detail Report">
    private List<PrintDataBuilder> printDataBuilders;
    private int printIndex;
    private String printErrorMsg = "";

    @Override
    public void setSelectedHostForDetailReport(Host host) {
        hostForDetailReport = host;
        repository.getEnabledMerchantsByHost(host.getHostID(), merchants -> {
            if (merchants.size() == 0) {
                mView.showToastMessage(Const.MSG_NO_MERCHANTS_FOR_SELECTED_HOST);
            } else if (merchants.size() == 1) {
                setSelectedMerchantForDetailReport(merchants.get(0));
            } else {
                mView.selectMerchantForDetailReport(host);
            }
        });
    }

    @Override
    public void setSelectedMerchantForDetailReport(Merchant merchant) {
        merchantForDetailReport = merchant;
        prepareDetailReport();
    }

    private void prepareDetailReport() {
        log("prepareDetailReport");
        mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);

        repository.getTransactionByMerchantAndHost(merchantForDetailReport.getMerchantID(), hostForDetailReport.getHostID(), allTransactions -> {
            ArrayList<Transaction> detailReportTransactions = new ArrayList<>();
            for (Transaction t : allTransactions) {
                if (!isPreAuthTxn(t.getTransaction_code())) {
                    detailReportTransactions.add(t);
                }
            }

            log("transaction count : " + detailReportTransactions.size());
            if (detailReportTransactions.size() >= 1) {
                PosDevice.getInstance().startPrinting();
                repository.getTerminalByMerchant(merchantForDetailReport.getMerchantNumber(), terminal -> {
                    log("terminal " + terminal.getTerminalID());
                    repository.getCurrencyByMerchantId(merchantForDetailReport.getMerchantNumber(), currency -> {
                        log("currency " + currency.getCurrencySymbol());
                        repository.getAllIssuers(issuers -> {
                            log("Issuers received count " + issuers.size());
                            MyApp.getInstance().getAppReceipts()
                                    .generateDetailReportReceiptArvin(hostForDetailReport,
                                            merchantForDetailReport, terminal, currency, issuers,
                                            detailReportTransactions, new AppReceipts.ReceiptBuilderListener() {
                                                @Override
                                                public void onReceiptGenerated(List<PrintDataBuilder> pdbs) {
                                                    printDataBuilders = pdbs;
                                                    printIndex = 0;
                                                    startDetailReportPrint();
                                                }

                                                @Override
                                                public void onReceiptGenerationFailed() {
                                                    PosDevice.getInstance().stopPrinting();
                                                    mView.hideLoader();
                                                    mView.showToastMessage("Receipt Error: " + printErrorMsg);
                                                }
                                            });
                        });
                    });
                });
            } else {
                PosDevice.getInstance().stopPrinting();
                mView.hideLoader();
                mView.showToastMessage(Const.MSG_TXN_EMPTY);
            }
        });
    }

    private void startDetailReportPrint() {
        log("startDetailReportPrint()");
        printingOnHome = true;
        PosDevice.getInstance().stopEmvFlow();
        PosDevice.getInstance().startPrinting();
        PrintDataBuilder p = printDataBuilders.get(printIndex);

        Print print = new Print();
        print.setPrintType(Print.PRINT_DATA_BUILDER);
        print.setPrintDataBuilder(p);
        print.setPrintListener(new PrintListener() {
            @Override
            public void onPrintFinished() {
                if ((printIndex + 1) == printDataBuilders.size()) {
                    printIndex = 0;
                    printDataBuilders.clear();
                    log("detail report print completed");
                    PosDevice.getInstance().stopPrinting();
                    mView.hideLoader();

                    printingOnHome = false;
                    if (!isCheckCardRunning){
                        onResume();
                    }
                } else {
                    printIndex += 1;
                    startDetailReportPrint();
                }
            }

            @Override
            public void onPrintError(PrintError printError) {
                log("detail report print error");
                PosDevice.getInstance().clearPrintQueue();
                PosDevice.getInstance().stopPrinting();
                mView.hideLoader();
                printErrorMsg = printError.getMsg();
                mView.hideLoader();
                mView.showToastMessage("Printer Error: " + printErrorMsg);
                PosDevice.getInstance().stopPrinting();

                printingOnHome = false;
                if (!isCheckCardRunning){
                    onResume();
                }
            }
        });

        PosDevice.getInstance().addToPrintQueue(print);
    }

    private boolean isPreAuthTxn(int txnCode) {
        return (txnCode == SALE_PRE_AUTHORIZATION
                || txnCode == SALE_PRE_AUTHORIZATION_MANUAL);
    }
    // </editor-fold>

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }

//    public void insertdccbinlist(DCCBINNLIST values){
//      //  mView.showLoader("DCC","Please Wait Updating DCC BIN List");
//        repository.insertdccbinlist(new DbHandler.UPDATEDCCBINListener() {
//
//            @Override
//            public void OnUpdated() {
//               // mView.hideLoader();
//            }
//
//            @Override
//            public void OnError(String message) {
//               // mView.hideLoader();
//            }
//        },values);
//    }
    public void installapplicationupdate(Context context) {
        Log.d("APPLICATIONINSTALL ","installapplicationupdate");
        Log.d("APPLICATION-DOWNLOAD", "REQUEST C");

        repository.getTransactionCount(count -> {
            log("transaction count: " + count);
            if (count == 0) {

                isUpdatingProfile = true;

                if (isViewNotNull()) {
                    mView.showLoader("Please wait", "Updating Application");
                }
                repository.saveHasPendingApplicationUpdate(false);
                TMSActivity.installapplication(context);
                mView.hideLoader();

            } else {
                log("transactions exists try to auto settle");
                repository.saveHasPendingApplicationUpdate(true);
                tryToAutoSettle();
            }
        });

    }

    private void initMainSDK(){
        log("initMainSDK()");


        PosDevice.getInstance().setInitListener(() -> {
           // PosDevice.getInstance().

        String IP = "192.168.132.35";
        String PORT = "9003";
        String UUID = "000010101010101";
        String APP_ID = context.getPackageName();
        String SERIAL_NO = PosDevice.getInstance().getSerialNo();
        String IMEI = "1234567891";
        String DEVICE_CATEGORY = "DEFAULT";
        String DEVICE_MODEL =PosDevice.getInstance().getdevicemodel();
        String SIM_IP ="102.10.22.100";
        String SIM_SERIAL ="HD160BH004994";
        String CITY_CODE = "CMB01";
        String INSTITUTE_CODE = "HNB";
        String LATI ="6.9000";
        String LONGI = "79.8700";





        mainSDK.startConnection(IP, PORT, UUID, APP_ID,SERIAL_NO, IMEI, DEVICE_CATEGORY, DEVICE_MODEL, SIM_IP, SIM_SERIAL, CITY_CODE,  INSTITUTE_CODE, LATI, LONGI);
        });


    }


    @Override
    public void onTMSOperationReceived(Context context ,String msg , String optCode) {

        switch (optCode){
            case "001":
                System.out.println("001 - Application download");
                onApplicationDownload(context,msg);
                break;
            case"002" :
                System.out.println("002 - Profile Download");
                onProfileDownload(context,msg);
                break;
            case"006" :
                log("006 - Settlement");
                onAutoSettlement();
                break;
            case"007" :
                log("007 - Terminal Disable Request");
                onTerminalDisableRequest();
                break;
            case"008" :
                log("008 - Terminal Enable Request");
                onTerminalEnableRequest();
                break;
            case "009":
                log("009 - LogEnableRequest");
                onLogEnableRequest();
                break;
            case "010":
                log("010 - LogDisableRequest");
                onLogDisableRequest();
                break;
            case"017" :
                log("017 - Application Uninstall");
                onApplicationUnInstall(context,msg);
                break;
            case"019" :
                log("019 - Enable Device Echo");
                onEmitEnableDeviceEcho();
                break;
            case"020" :
                log("020 - Disable Device Echo");
                onEmitDisableDeviceEcho();
                break;
            case"022" :
                log("022 - Tran Sync Enable");
                onTranSyncEnable(context,msg);
                break;
            case"023" :
                log("023 - Tran Sync Disable");
                onTranSyncDisable(context,msg);
                break;
            case"024" :
                log("024 - Banner download");
                onBannerDownload(context,msg);
                break;
        }
    }


    public void onApplicationDownload(Context context ,String s) {
        Gson gson = new Gson();
        DeviceOperationData deviceOperation = gson.fromJson(s, DeviceOperationData.class);

        List<AppData> appDataList = deviceOperation.getAppData();


        appLink = appDataList.get(0).getAppLink();
        appPackageName = appDataList.get(0).getAppPackageName();
        appVersion = appDataList.get(0).getAppVersion();

        log("app link :"+ appLink);
        log("package name :"+ appPackageName);
        log("app version :"+ appVersion);


        if((context.getPackageName()).equals(appPackageName)){

            log("Start Application Download!");


            String apkUrl = appLink;
            String[] extractApk = appLink.split("/");

            appName = extractApk[extractApk.length - 1];

            log("App Name : "+ appName);

            File fileToDelete = new File( Environment.getExternalStorageDirectory() + "/Download/" + appName );
            if (fileToDelete.delete()) {
                log("File deleted successfully");
            } else {
                log("File could not be deleted");
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, appName);
            DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadId = downloadManager.enqueue(request);

            downloadedApkFile = new File(Environment.DIRECTORY_DOWNLOADS, appName);
            log("downloadedApkFile: " + downloadedApkFile);



            if(downloadId != -1){
                mainSDK.emitAppDownloadStatus(context.getPackageName(), appVersion, "00"); // 00 for success

            }else {
                mainSDK.emitAppDownloadStatus(context.getPackageName(), appVersion,"01"); // 00 for not success
            }
            log("emitAppDownloadStatus-notify" );

        }else{
            log("App is not existing in  pos" );
        }

        log("emitAppDownloadStatus-notify" );

        context.registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("Application Downloading");

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            log("emitAppDownloadStatus-notify" );

            if (downloadId != -1) {
                // Download completed, proceed with APK installation

                mainSDK.emitAppDownloadStatus(context.getPackageName(), appVersion, "00");
                log("Start-InstallApplication");
                //installApplication(context_x, appName);
                installApplication(context, appName);
            }else{
                mainSDK.emitAppDownloadStatus(context.getPackageName(), appVersion,"01"); // 00 for not success
            }
        }
    };



    public void installApplication(Context context, String APP_NAME){
        log("installApplication()" );
        try {
            log("APP_NAME: "+ APP_NAME);
            String path_x = Environment.getExternalStorageDirectory() + "/Download/";
            File path_x_File = new File(path_x, APP_NAME);
            log("path_x: "+ path_x);

            if (path_x_File.exists()) {
                log("path_x_File exists()" );
            }else {
                log("path_x_File not exists()" );
            }

            if (path_x_File.exists()) {
                log("downloadedApkPath11: "+ path_x);
                installStatus =  PosDevice.getInstance().installapplication(path_x + APP_NAME);
                log("installStatus: "+ installStatus);
                if( installStatus == SdkResult.Success){
                    log("Application installation success");
                    //Toast.makeText(context_x, "Application Installation Success", Toast.LENGTH_LONG).show();
                    mainSDK.notifyInstallStatus(context.getPackageName() ,appVersion,"00");// 00 for success
                    //restart pos
                    HomeActivity.getInstance().restartapplication();
                } else{
                    log("Application installation fail");
                    mainSDK.notifyInstallStatus(context.getPackageName()  ,appVersion,"01"); // 01 for failed
                    Toast.makeText(context, "Application Installation Fail", Toast.LENGTH_LONG).show();
                }
            } else{
                log("downloadedApkPath: Not exist");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void onProfileDownload(Context context , String message){

        log("Profile Data : "+ message);
       // updateProfile();

        if(!TMSActivity.hasPendingProfileUpdate(context)) {
            log("hasPendingProfileUpdate: ");
            TMSActivity.downloadProfileData(context,message);
        }else {
            log("onProfileDownloadReceived: - else ");
        }
    }

    public void onApplicationUnInstall(Context context , String message){
        log("onApplicationUninstallReceived()");
        Gson gson = new Gson();
        DeviceOperationData deviceOperation = gson.fromJson(message, DeviceOperationData.class);
        List<AppData> appDataList = deviceOperation.getAppData();


        appPackageName = appDataList.get(0).getAppPackageName();
        appVersion = appDataList.get(0).getAppVersion();

        log("package name :"+ appPackageName);
        log("app version :"+ appVersion);

        if(appPackageName.equals(context.getPackageName())){
            uninstallStatus =  PosDevice.getInstance().uninstallApp(appPackageName, mainSDK , appPackageName);
            if( uninstallStatus == 0){
                log("Application uninstallation success");
                mainSDK.emitAppUninstallStatus(context.getApplicationContext().getPackageName(), appVersion, "00"); // 00 for success
            }

        }else {
            log("Application uninstallation fail version mismatch");
                log("Application uninstallation fail");
                mainSDK.emitAppUninstallStatus(context.getApplicationContext().getPackageName(), appVersion, "01"); // 01 for failed
        }

    }

    public void onAutoSettlement(){
        if(repository.isAutoSettlementCompleted()){
            mainSDK.emitDoSettlementSuccess(context.getPackageName(), BuildConfig.VERSION_NAME,"00");
            repository.setAutoSettlementCompleted(false);
        }else{
            mView.gotoAutoSettlementActivity();
        }
    }

    public void onLogEnableRequest(){
        repository.setLogEnabled(true);
        mainSDK.emitLogEnableStatus(context.getPackageName(), "V1.07", "00");

    }

    public void onLogDisableRequest(){
        repository.setLogEnabled(false);
        mainSDK.emitLogDisableStatus(context.getPackageName(),"V1.07","00");
    }

    public void onBannerDownload(Context context , String message){
        log("onBannerDownloadReceived()");
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        String operationCode = jsonObject.get("operationCode").getAsString();
        String operationMessage = jsonObject.get("operationMessage").getAsString();
        String appPackageName = jsonObject.get("appPackageName").getAsString();
        int noOfBanners = jsonObject.getAsJsonObject("data").get("noOfBanners").getAsInt();
        JsonArray bannersArray = jsonObject.getAsJsonObject("data").getAsJsonArray("banners");

        log("noOfBanners: " + noOfBanners);

        File directory1 = new File(Environment.getExternalStorageDirectory() + File.separator + "tmsImages");
        if (directory1.exists() && noOfBanners > 0) {
            deleteImgFolder(directory1);
        }

        for ( JsonElement bannersArray1 : bannersArray) {
            String base64String = bannersArray1.toString();
            byte[] x = Base64.decode(base64String, Base64.DEFAULT);  //convert from base64 to byte array
            Bitmap bitmap = BitmapFactory.decodeByteArray(x,0,x.length);

            if (bitmap != null) {
                log("bitmap != null");
            } else {
                log("bitmap == null");
            }
            log("bitmap i:" + i);
            String fileName = "banner"+ i + ".jpg";
            i++;
            try {
                FileOutputStream fos = null;
                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "tmsImages");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File file = new File(directory, fileName);
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, null, null);
                mainSDK.emitBannerDownloadSuccess(appPackageName, "V1.07","00");
                //restart pos
                mView.restart();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        i=1;
    }

    public void deleteImgFolder(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteImgFolder(child);

        fileOrDirectory.delete();

        log("deleteImgFolder(): " + fileOrDirectory.delete());
    }

    public void onTranSyncEnable(Context context ,String message){

        log("onTranSyncEnableReceived: " + message);
        new Thread() {
            @Override
            public void run() {
                repository.setTranSyncEnable(true);
                if(repository.isTranSyncEnable()){
                    mainSDK.emitEnableTranSyncStatus(context.getPackageName(), "V1.07", "00");
                }
            }
        }.start();
    }

    public void onTranSyncDisable(Context context ,String message){

        log("onTranSyncDisableReceived: " + message);
        new Thread() {
            @Override
            public void run() {
                repository.setTranSyncEnable(false);
                if(repository.isTranSyncEnable()){
                    mainSDK.emitDisableTranSyncStatus(context.getPackageName(), BuildConfig.VERSION_NAME, "00");
                }
            }
        }.start();

    }


    public void onEmitTranData(String tranData){
        log("emitTranData");
        mainSDK.emitTranData(tranData);
    }

    public void onSendTxnReceiptData(String receiptData){
        log("sendTxnReceiptData");
        mainSDK.emitReceiptData(receiptData);
    }

    public void onEmitDebitKeyDownload(){
        log("onEmitDebitKeyDownload");
        mainSDK.emitDebitKeyDownloadSuccess(context.getPackageName(), BuildConfig.VERSION_NAME, "00");
    }

    public void onEmitEnableDeviceEcho(){
        log("onEmitEnableDeviceEcho");
        mainSDK.emitEnableDeviceEcho(context.getPackageName(), "V1.07", "00");
    }

    public void onEmitDisableDeviceEcho(){
        log("onEmitDisableDeviceEcho");
        mainSDK.emitDisableDeviceEcho(context.getPackageName(), "V1.07", "00");
    }

    public void onEmitDeviceStatusEcho(String deviceStatus){
        log("onEmitDeviceStatusEcho");
        mainSDK.emitDeviceStatusEcho(deviceStatus);
    }

    public void onTerminalEnableRequest(){
        log("onTerminalEnableRequest:");
        new Thread() {
            @Override
            public void run() {
                repository.saveShouldDisableTerminal(false);
                if(!repository.isTerminalDisabled()){
                    mainSDK.emitEnableTerminalStatus(context.getPackageName(), "V1.07", "00");
                }
            }
        }.start();

    }

    public void onTerminalDisableRequest(){
        log("onTerminalDisableRequest: ");
        new Thread() {
            @Override
            public void run() {
                repository.saveShouldDisableTerminal(true);
                if(repository.isTerminalDisabled()){
                    mainSDK.emitDisableTerminalStatus(context.getPackageName(), "V1.07", "00");
                }
            }
        }.start();
    }

}
