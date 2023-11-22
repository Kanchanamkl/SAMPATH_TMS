package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.TFI;
import com.epic.pos.data.db.dbpos.modal.TLE;

import java.util.List;

@Dao
public interface TLEDao {

    @Insert
    void insert(TLE tle);

    @Query("SELECT * FROM TLE")
    List<TLE> getAll();

    @Query("SELECT * FROM TLE WHERE IssuerNumber =:issuerNumber")
    TLE getTLEbyIssuer(int issuerNumber);
}
