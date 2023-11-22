package com.epic.pos.ui.settings;

import com.epic.pos.common.Const;
import com.epic.pos.config.MyApp;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.receipt.AppReceipts;
import com.epic.pos.ui.BasePresenter;
import com.epic.pos.device.PosDevice;
import com.epic.pos.device.data.Print;
import com.epic.pos.device.data.PrintDataBuilder;
import com.epic.pos.device.data.PrintError;
import com.epic.pos.device.listener.PrintListener;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class SettingsPresenter extends BasePresenter<SettingsContract.View> implements SettingsContract.Presenter {
    private Repository repository;
    private NetworkConnection networkConnection;

    @Inject
    public SettingsPresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    // <editor-fold defaultstate="collapsed" desc="Diagnostic Report">
    @Override
    public void printDiagnosticReport(File emvFile) {
        if (emvFile.exists()) {
            mView.showLoader(Const.MSG_PLEASE_WAIT, Const.MSG_PRINTING_RECEIPT);
            PosDevice.getInstance().startPrinting();

            MyApp.getInstance().getAppReceipts().generateDiagnosticReport(emvFile, new AppReceipts.ReceiptBuilderListener() {
                @Override
                public void onReceiptGenerated(List<PrintDataBuilder> printDataBuilders) {
                    PrintDataBuilder p = printDataBuilders.get(0);
                    Print print = new Print();
                    print.setPrintType(Print.PRINT_DATA_BUILDER);
                    print.setPrintDataBuilder(p);
                    print.setPrintListener(new PrintListener() {
                        @Override
                        public void onPrintFinished() {
                            PosDevice.getInstance().stopPrinting();
                            mView.hideLoader();
                        }

                        @Override
                        public void onPrintError(PrintError printError) {
                            PosDevice.getInstance().stopPrinting();
                            mView.hideLoader();
                            mView.showToastMessage("Printer Error: " + printError.getMsg());
                        }
                    });

                    PosDevice.getInstance().addToPrintQueue(print);
                }

                @Override
                public void onReceiptGenerationFailed() {
                    mView.hideLoader();
                    mView.showToastMessage(Const.MSG_RECEIPT_GENERATE_FAILED);
                    PosDevice.getInstance().stopPrinting();
                }
            });
        } else {
            mView.showToastMessage(Const.MSG_DIAGNOSTIC_REPORT_NOT_FOUND);
        }
    }

    // </editor-fold>

}