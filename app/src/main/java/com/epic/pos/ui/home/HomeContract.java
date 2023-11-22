package com.epic.pos.ui.home;

import android.content.Context;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.Feature;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.ui.BaseView;
import com.epic.pos.util.Partition;

import java.util.List;

public interface HomeContract {
    interface View extends BaseView {
        void gotoMerchantListActivity();

        void gotoAmountActivity();

        void gotoVoidActivity();

        void gotoReceiptTypeSelectActivity();

        void gotoPreCompActivity();

        void onProceedPreCompSale();

        void onProceedQRSale();

        void onProceedQRVerify();

        void onProceedVoidSale();

        void onProceedSettlement();

        void onManualSalePasswordRequest(int trantype);

        void onProceedClearReversal();

        void onProceedCheckReversal();

        void selectMerchantForVoid(Host host);

        void selectMerchantForPreComp(Host host);

        void forceClearReversalSuccess(int deletedRecords);

        void startSettlementDetails(Host selectedHost, Merchant selectedMerchant);

        void startMerchantSelectActivity(Host selectedHost);

        void setRootLayoutVisibility(boolean visibility);

        void onFeaturesReceived(Partition<Feature> featurePartitions);

        void restartActivity();

        void updateBatteryLevelUi(boolean showBatteryLow, int level);

        void updateTmsstatuslUi(boolean showui,String msg,String color);

        void onProfileUpdateCompleted();


        void turnScreenOn();

        //auto settle
        void gotoAutoSettlementActivity();

        //card scan
        void onCDTError();

        void onMultipleCDTReceived(List<CardDefinition> cardDefinitionList);

        void gotoMerchantListActivityForCardScan(boolean isCardSwiped);

        //ecr
        void selectMerchantForEcr();

        //profile update
        void onProfileUpdateSuccess();

        //Serial com
        void gotoEcrCardScanActivity();

        //Detail report
        void selectHostForDetailReport();

        void selectMerchantForDetailReport(Host host);

        //Last settlement report
        void selectHostForLastSettlementReport();

        void selectMerchantForLastSettlementReport(Host host);

        //Any Receipt
        void selectHostForAnyReceipt();

        void selectMerchantForAnyReceipt(Host host);

        void selectInvoiceForAnyReceipt(Host host, Merchant merchant);

        void gotoFailedActivity(String title, String msg);

        void onCheckReversalPrintError(String msg);

        void restart();


        void emitTranData(String tranData);

        void sendTxnReceiptData(String receiptData);

        void emitDebitKeyDownload();
    }

    interface Presenter {
        void initData();

        void onSaleClicked();

        void onOfflineSaleClicked();

        void onOfflineManualSaleClicked();

        void onManualSaleClicked();
        void onManualSaleafterpassword(int tran);
        void onVoidClicked();

        void onSettlementClicked();

        void onClearReversalClicked();

        void onQrSaleClicked();

        void onQrVerifyClicked();

        void onPreAuthClicked();

        void onPreAuthManualClicked();

        void onPreCompClicked();

        void onInstallmentClicked();

        void onRefundSaleClicked();

        void onManualRefundClicked();

        void onCashBackClicked();

        void onQuasiCash();

        void onQuasiCashManual();

        void onCashAdvanceClicked();

        void forceClearReversal(Host host);

        void onResume();

        void onPause();

        void onHostSelectedForVoid(Host host);

        void onMerchantSelectedForVoid(Merchant merchant);

        void onHostSelectedForPreComp(Host host);

        void onMerchantSelectedForPreComp(Merchant merchant);

        void getFeatures();

        boolean cardIsStillIn();

        boolean isPause();

        void tryToAutoSettle();

        void updateProfile();

        void generateConfigMap();

        void UpdateCUPBinFile();

        void setBatteryLevel(int batteryLevel);

        void setChargerStatus(boolean isCharging);

        Repository getRepository();

        //ecr
        void onMerchantGroupSelectedForEcr();

        //card scan
        void checkCard();

        void onCDTSelectedForCardSwipe(CardDefinition cardDefinition);

        void onMerchantGroupSelected(boolean isCardSwiped);

        //serial comunication
        void startSerialCom();

        //Detail Report
        void onPrintDetailReportClicked();

        void setSelectedHostForDetailReport(Host host);

        void setSelectedMerchantForDetailReport(Merchant merchant);

        //Last receipt
        void onPrintLastReceiptClicked();

        //Last settlement report
        void onPrintLastSettlementReceiptClicked();

        void setSelectedHostForSettlementReceipt(Host host);

        void setSelectedMerchantForSettlementReceipt(Merchant merchant);

        //Any Receipt
        void onAnyReceiptClicked();

        void setSelectedHostForAntReceipt(Host host);

        void setSelectedMerchantForAnyReceipt(Merchant merchant);

        void setTransactionForAnyReceipt(Transaction transaction);

        //Check reversal
        void onCheckReversalClicked();
        void checkReversalReprint();

        //on student reference
        void onStudentRefSaleClicked();


        void onTMSOperationReceived(Context context , String meg , String optCode);

        void onEmitTranData(String tranData);

    }
}