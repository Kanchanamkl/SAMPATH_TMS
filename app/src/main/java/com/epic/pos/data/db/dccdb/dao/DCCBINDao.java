package com.epic.pos.data.db.dccdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.data.db.dccdb.model.DCCData;

import java.util.List;

@Dao
public interface DCCBINDao {

    @Insert
    void insert(DCCBINNLIST dccbindata);

    @Query("DELETE  FROM BINLIST")
    void deleteall();
    @Query("SELECT * FROM BINLIST")
    List<DCCBINNLIST> getAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllDCCBinlist(List<DCCBINNLIST> dccdatas);


    @Query("SELECT * FROM BINLIST WHERE PANL <= :pan AND PANH >= :pan")
    List<DCCBINNLIST> getbinlistbyrange(String pan);

}
