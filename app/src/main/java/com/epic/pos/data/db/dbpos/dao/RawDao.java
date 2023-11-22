package com.epic.pos.data.db.dbpos.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.epic.pos.data.db.dbpos.modal.ConfigMap;

import java.util.List;

@Dao
public interface RawDao {
    @RawQuery
    ConfigMap getUserViaQuery(SupportSQLiteQuery query);

    @RawQuery
    Cursor getTablePrimaryColumnName(SupportSQLiteQuery query);

    @RawQuery
    long updateProfileData(SupportSQLiteQuery query);

    @RawQuery
    long deleteConfigMapByTableName(SupportSQLiteQuery query);

    @RawQuery
    Cursor rowQuery(SimpleSQLiteQuery query);

    @RawQuery
    Cursor getTableData(SimpleSQLiteQuery query);


}
