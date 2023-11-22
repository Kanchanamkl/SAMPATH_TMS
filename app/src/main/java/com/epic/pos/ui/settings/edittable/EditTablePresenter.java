package com.epic.pos.ui.settings.edittable;

import android.text.TextUtils;

import com.epic.pos.common.Const;
import com.epic.pos.domain.entity.ColumnInfoEntity;
import com.epic.pos.domain.repository.Repository;
import com.epic.pos.helper.NetworkConnection;
import com.epic.pos.ui.BasePresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class EditTablePresenter extends BasePresenter<EditTableContract.View> implements EditTableContract.Presenter {

    private Repository repository;
    private NetworkConnection networkConnection;

    private String table;
    private int rowIndex = 1;
    private List<ColumnInfoEntity> columns = new ArrayList<>();

    private String errorMsg = "";

    @Inject
    public EditTablePresenter(Repository repository, NetworkConnection networkConnection) {
        this.repository = repository;
        this.networkConnection = networkConnection;
    }

    @Override
    public void onSaveClicked() {
        mView.setActionsEnabled(false);
        if (validateData()){
            repository.updateTableInfo(table, columns, () -> {
                mView.onColumnsUpdated();
                mView.showToastMessage(Const.MSG_RECORDS_UPDATED);
                mView.setActionsEnabled(true);
            });
        }else {
            mView.showToastMessage(errorMsg);
            mView.setActionsEnabled(true);
        }
    }

    private boolean validateData() {
        for (ColumnInfoEntity c : columns) {
            if (c.isNotNull()){
                if (TextUtils.isEmpty(c.getValue())){
                    errorMsg = "Field " + c.getColumnName() + " can't be null";
                    return false;
                }
            }

            if(c.getType().equals("INTEGER")){
                try {
                    int val = Integer.parseInt(c.getValue());
                }catch (Exception e){
                    e.printStackTrace();
                    errorMsg = "Enter valid amount for " + c.getColumnName();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onNextClicked() {
        int tempRowIndex = rowIndex + 1;
        getTableData(tempRowIndex);
    }

    @Override
    public void onBackClicked() {
        if (rowIndex >= 2){
            int tempRowIndex = rowIndex - 1;
            getTableData(tempRowIndex);
        }else {
            mView.showToastMessage(Const.MSG_NO_RECORDS);
        }
    }

    @Override
    public void gotoRecord(String record) {
        if (!TextUtils.isEmpty(record)){
            int tempRowIndex = Integer.parseInt(record);
            if (tempRowIndex != 0){
                getTableData(tempRowIndex);
            }else {
                mView.showToastMessage(Const.MSG_NO_RECORDS);
                mView.onRowIndexUpdated(rowIndex);
            }
        }
    }

    @Override
    public void onColumnInfoChanged(ColumnInfoEntity c) {
        columns.set(c.getColumnId(), c);
    }



    @Override
    public void onTableSelected(String tableName) {
        if (!TextUtils.isEmpty(tableName)){
            table = tableName;
            rowIndex = 1;
            repository.getTableColumnInfo(tableName, columnList -> {
                EditTablePresenter.this.columns.clear();
                EditTablePresenter.this.columns.addAll(columnList);
                mView.onColumnsUpdated();
                getTableData(rowIndex);
            });
        }
    }

    private void getTableData(int rowIndex) {
        mView.setActionsEnabled(false);

        repository.getTableDataCursor(table, rowIndex, cursor -> {
            if (cursor.moveToNext()){
                EditTablePresenter.this.rowIndex = rowIndex;
                for (int i=0; i<columns.size(); i++){
                    ColumnInfoEntity c = columns.get(i);
                    c.setValue(cursor.getString(cursor.getColumnIndex(c.getColumnName())));
                    columns.set(i, c);
                }

                mView.onColumnsUpdated();
            }else {
                mView.showToastMessage(Const.MSG_NO_RECORDS);
            }

            cursor.close();
            mView.onRowIndexUpdated(EditTablePresenter.this.rowIndex);
            mView.setActionsEnabled(true);
        });
    }

    @Override
    public List<ColumnInfoEntity> getColumns() {
        return columns;
    }
}