package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Terminal;

import java.util.List;

@Dao
public interface IssuerDao {

    @Insert
    void insert(Issuer issuer);

    @Query("SELECT * FROM IIT")
    List<Issuer> getAll();

    @Query("SELECT * FROM IIT WHERE IssuerNumber == :issuerId")
    Issuer getIssuerById(int issuerId);



}
