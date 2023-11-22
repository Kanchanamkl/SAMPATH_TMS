package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbpos.modal.Terminal;

import java.util.List;

@Dao
public interface TerminalDao {

    @Insert
    void insert(Terminal terminal);

    @Update
    void update(Terminal terminal);

    @Query("SELECT * FROM TMIF")
    List<Terminal> getAll();

    @Query("SELECT * FROM TMIF WHERE ID == :id")
    Terminal getTerminalById(int id);

    @Query("SELECT * FROM TMIF WHERE MerchantNumber == :merchantID")
    Terminal getTerminalByMerchant(int merchantID);

    @Query("SELECT * FROM TMIF WHERE MerchantNumber == :merchantID AND HostId == :hostID")
    Terminal getTMIFByHostAndMerchant(int hostID, int merchantID);

    @Query("SELECT * FROM TMIF WHERE HostId == :hostID LIMIT 1")
    Terminal getTerminalByHost(int hostID);

    @Query("SELECT * FROM TMIF WHERE HostId == :hostID")
    List<Terminal> getAllTerminalsByHost(int hostID);

}
