package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Query;
import com.epic.pos.data.db.dbpos.modal.TLE;
import com.epic.pos.data.db.dbpos.modal.USER;
import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM USER")
    List<USER> getAll();

}
