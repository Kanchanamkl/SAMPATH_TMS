package com.epic.pos.ui.settings;

import com.epic.pos.ui.BaseView;

import java.io.File;

public interface SettingsContract {
    interface View extends BaseView {

    }

    interface Presenter {
        void printDiagnosticReport(File emvFile);
    }
}
