package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.TCP;

import java.util.List;

@Dao
public interface TCPDao {

    @Insert
    void insert(TCP tcp);

    @Query("SELECT * FROM TCP")
    List<TCP> getAll();


}
