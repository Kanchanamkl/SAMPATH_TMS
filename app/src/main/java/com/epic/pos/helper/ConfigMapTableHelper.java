package com.epic.pos.helper;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import com.epic.pos.util.AppLog;

import com.epic.pos.domain.repository.Repository;

public class ConfigMapTableHelper {

    private final String TAG = ConfigMapTableHelper.class.getSimpleName();
    private Repository repository;
    private Handler handler = new Handler(Looper.getMainLooper());


    public ConfigMapTableHelper(Repository repository) {
        this.repository = repository;
    }

    int deletedTableCount = 0;

    public void clearConfigMap(Repository repository, String[] tables, DeleteListener listener) {
        log("clearConfigMap()");
        new Thread(){
            @Override
            public void run() {
                for (String table : tables) {
                    repository.deleteConfigMapByTableName(table);
                    log("records deleted: " + table);
                }

                handler.post(listener::onProcessCompleted);
            }
        }.start();
    }



    private String[] aidExclude = {"IssuerID"};
    private String[] cdtExclude = {"ID", "CardAbbre", "CardLable", "TrackRequired", "CheckLuhn", "ManualEntry"};
    private String[] cstExclude = {"MerchantNumber"};
    private String[] featureExclude = {"id"};
    private String[] ihtExclude = {"IssuerList", "BaseIssuer", "MustSettleFlag", "HostID", "MasterKeyId", "WorkKeyId"};
    private String[] iitExclude = {"IssuerNumber", "IssuerAbbrev", "IssuerLable"};
    private String[] mitExclude = {"MerchantNumber", "MerchantPassword", "InvNumber", "STAN",
            "MobileNumber", "NIC", "MCC", "ContactNumber", "Country", "Province", "District",
            "City", "Email", "Fax", "Remark"};
    private String[] tctExclude = {"ID"};
    private String[] tmifExclude = {"ID", "IssuerNumber", "IsQrSupport", "MerchantNumber"};

    private boolean shouldInsert(String tableName, String columnName) {
        if (tableName.equals("AID")) {
            for (String column : aidExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("CDT")) {
            for (String column : cdtExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("CST")) {
            for (String column : cstExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("FEATURE")) {
            for (String column : featureExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("IHT")) {
            for (String column : ihtExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("IIT")) {
            for (String column : iitExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("MIT")) {
            for (String column : mitExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        } else if (tableName.equals("TCT")) {
            for (String column : tctExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        }else if (tableName.equals("TMIF")){
            for (String column : tmifExclude) {
                if (columnName.equals(column)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void insertDataToTable(Repository repository, String[] tables, InsertListener listener) {
        log("insertDataToTable()");
        new Thread(){
            @Override
            public void run() {
                try {
                    for (String table : tables) {
                        Cursor cursor = repository.getTableData(table);
                        int curRec = 1;
                        while (cursor.moveToNext()) {
                            for (int i = 0; i < cursor.getColumnCount(); i++) {
                                String columnName = cursor.getColumnName(i);
                                if (shouldInsert(table, columnName)) {
                                    String paramName = table + "_" + columnName + "_" + curRec;
                                    log("paramName: " + paramName);
                                    repository.insertConfigMap(
                                            paramName,
                                            table,
                                            columnName,
                                            curRec,
                                            cursor.getString(cursor.getColumnIndex(columnName)));
                                }
                            }
                            curRec++;
                        }
                        cursor.close();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                handler.post(listener::onCompleted);
            }
        }.start();
    }

    public interface DeleteListener {
        void onProcessCompleted();
    }

    public interface InsertListener{
        void onCompleted();
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}
