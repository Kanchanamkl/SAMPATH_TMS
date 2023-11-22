package com.epic.pos.data.db.dccdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dccdb.model.DCCData;

import java.util.List;

@Dao
public interface DCCDao {

    @Insert
    void insert(DCCData dccdta);
    @Query("SELECT * FROM DCCData")
    List<DCCData> getAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllDCCData(List<DCCData> dccdatas);



}
