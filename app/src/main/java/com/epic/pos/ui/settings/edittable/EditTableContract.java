package com.epic.pos.ui.settings.edittable;

import com.epic.pos.domain.entity.ColumnInfoEntity;
import com.epic.pos.ui.BaseView;

import java.util.List;

public interface EditTableContract {
    interface View extends BaseView {
        void onColumnsUpdated();
        void onRowIndexUpdated(int rowIndex);
        void setActionsEnabled(boolean isEnabled);
    }

    interface Presenter {
        List<ColumnInfoEntity> getColumns();
        void onTableSelected(String tableName);
        void onNextClicked();
        void onBackClicked();
        void gotoRecord(String record);
        void onColumnInfoChanged(ColumnInfoEntity c);
        void onSaveClicked();
    }
}
