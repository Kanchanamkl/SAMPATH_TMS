package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbpos.modal.Currency;

import java.util.List;

@Dao
public interface CurrencyDao {

    @Insert
    void insert(Currency currency);

    @Update
    void update(Currency currency);

    @Query("SELECT * FROM CST")
    List<Currency> getAll();

    @Query("SELECT * FROM CST WHERE MerchantNumber == :merchantId LIMIT 1")
    Currency getCurrencyByMerchantId(int merchantId);


}
