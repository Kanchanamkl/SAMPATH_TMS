package com.epic.pos.dagger;

import com.epic.pos.ui.common.host.HostSelectActivity;
import com.epic.pos.ui.common.invoice.InvoiceSearchActivity;
import com.epic.pos.ui.common.merchant.MerchantSelectActivity;
import com.epic.pos.ui.common.password.PasswordActivity;
import com.epic.pos.ui.common.receipttype.ReceiptTypeActivity;
import com.epic.pos.ui.common.usernamepassword.UserNamePasswordInputActivity;
import com.epic.pos.ui.ecrcardscan.EcrCardScanActivity;
import com.epic.pos.ui.failed.FailedActivity;
import com.epic.pos.ui.home.HomeActivity;
import com.epic.pos.ui.home.banner.HomeBannerFragment;
import com.epic.pos.ui.home.menu.HomeMenuFragment;
import com.epic.pos.ui.login.LoginActivity;
import com.epic.pos.ui.newsettlement.auto.AutoSettleActivity;
import com.epic.pos.ui.newsettlement.settle.SettleActivity;
import com.epic.pos.ui.report.ReportMenuActivity;
import com.epic.pos.ui.sale.amount.AmountActivity;
import com.epic.pos.ui.sale.approvalcode.ApprovalCodeActivity;
import com.epic.pos.ui.sale.cardscan.CardScanActivity;
import com.epic.pos.ui.sale.cashbackamount.CashbackAmountActivity;
import com.epic.pos.ui.sale.detail.TransactionDetailsActivity;
import com.epic.pos.ui.sale.manual.ManualSaleActivity;
import com.epic.pos.ui.sale.merchantselect.MerchantListActivity;
import com.epic.pos.ui.sale.precomp.PreCompActivity;
import com.epic.pos.ui.sale.qr.QrActivity;
import com.epic.pos.ui.sale.receipt.ReceiptActivity;
import com.epic.pos.ui.sale.signature.SignatureActivity;
import com.epic.pos.ui.sale.studentref.StudentRefActivity;
import com.epic.pos.ui.settings.SettingsActivity;
import com.epic.pos.ui.settings.edittable.EditTableActivity;
import com.epic.pos.ui.settings.keyinject.key.KeyInjectActivity;
import com.epic.pos.ui.settings.setup.SetUpActivity;
import com.epic.pos.ui.settings.tlekeydownload.KeyDownloadActivity;
import com.epic.pos.ui.voidsale.VoidActivity;
import com.epic.pos.ui.voidsale.receipt.VoidReceiptActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class, DataSourceModule.class, RepositoryModule.class})
public interface AppComponent {

    void inject(LoginActivity target);

    void inject(AmountActivity amountActivity);

    void inject(CardScanActivity cardScanActivity);

    void inject(TransactionDetailsActivity transactionDetailsActivity);

    void inject(FailedActivity failedActivity);

    void inject(HomeActivity homeActivity);

    void inject(HomeBannerFragment homeBannerFragment);

    void inject(HomeMenuFragment homeMenuFragment);

    void inject(ReceiptActivity receiptActivity);

    void inject(SignatureActivity signatureActivity);

    void inject(KeyDownloadActivity keyDownloadActivity);

    void inject(ReportMenuActivity reportMenuActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(MerchantListActivity merchantListActivity);

    void inject(PasswordActivity passwordActivity);

    void inject(HostSelectActivity hostSelectActivity);

    void inject(MerchantSelectActivity merchantSelectActivity);

    void inject(VoidActivity voidActivity);

    void inject(VoidReceiptActivity voidReceiptActivity);

    void inject(ManualSaleActivity manualSaleActivity);

    void inject(com.epic.pos.ui.settings.keyinject.host.HostSelectActivity hostSelectActivity);

    void inject(KeyInjectActivity keyInjectActivity);

    void inject(ApprovalCodeActivity approvalCodeActivity);

    void inject(QrActivity qrActivity);

    void inject(EditTableActivity editTableActivity);

    void inject(PreCompActivity preCompActivity);

    void inject(SetUpActivity setUpActivity);

    void inject(InvoiceSearchActivity invoiceSearchActivity);

    void inject(CashbackAmountActivity cashbackAmountActivity);

    void inject(EcrCardScanActivity ecrCardScanActivity);

    void inject(SettleActivity settleActivity);

    void inject(AutoSettleActivity autoSettleActivity);

    void inject(StudentRefActivity studentRefActivity);

    void inject(UserNamePasswordInputActivity userNamePasswordInputActivity);

    void inject(ReceiptTypeActivity receiptTypeActivity);
}
