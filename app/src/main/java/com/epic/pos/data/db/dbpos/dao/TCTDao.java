package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbpos.modal.TCT;

import java.util.List;

@Dao
public interface TCTDao {

    @Insert
    void insert(TCT tct);

    @Query("UPDATE TCT SET AutoSettDate = :autoSettleDate WHERE ID = 1")
    void updateAutoSettleDate(String autoSettleDate);

    @Query("UPDATE TCT SET AutoSettleEnable = :autoSettleEnabled WHERE ID = 1")
    void updateAutoSettleEnable(int autoSettleEnabled);

    @Query("SELECT * FROM TCT")
    List<TCT> getAll();

    @Query("SELECT * FROM TCT LIMIT 1")
    TCT getTCT();

//    @Query("SELECT AutoSettlementTryCount FROM TCT WHERE ID = 1")
//    int reTryCount();
}
