package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbpos.modal.TFI;

import java.util.List;

@Dao
public interface TFIDao {

    @Insert
    void insert(TFI tfi);

    @Query("SELECT * FROM TFI")
    List<TFI> getAll();


}
