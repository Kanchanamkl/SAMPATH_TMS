package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.Aid;

import java.util.List;

@Dao
public interface AidDao {

    @Insert
    void insert(Aid aid);

    @Query("SELECT * FROM AID")
    List<Aid> getAll();

    @Query("SELECT * FROM AID WHERE IssuerID  == :issuerId")
    Aid getAidByIssuer(int issuerId);


}
