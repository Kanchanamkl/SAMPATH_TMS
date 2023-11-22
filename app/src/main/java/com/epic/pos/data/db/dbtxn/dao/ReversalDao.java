package com.epic.pos.data.db.dbtxn.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;

import java.util.List;

@Dao
public interface ReversalDao {

    @Insert
    long insert(Reversal transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllReversals(List<Reversal> reversals);

    @Delete
    void delete(Reversal transaction);

    @Update
    void update(Reversal transaction);

    @Query("SELECT * FROM `REVERSAL`")
    List<Reversal> getAll();

    @Query("DELETE FROM `REVERSAL`")
    int deleteAll();

    @Query("SELECT * FROM REVERSAL WHERE host == :host")
    List<Reversal> getReversalsByHost(int host);

}
