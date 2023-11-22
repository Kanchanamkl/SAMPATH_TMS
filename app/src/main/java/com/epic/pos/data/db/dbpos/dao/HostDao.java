package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Terminal;

import java.util.List;

@Dao
public interface HostDao {

    @Insert
    void insert(Host host);

    @Query("SELECT * FROM IHT")
    List<Host> getAll();

    @Query("SELECT * FROM IHT WHERE IssuerList LIKE :issuerIdLikeTxt")
    Host getIssuerContainsHost(String issuerIdLikeTxt);

    @Query("SELECT * FROM IHT WHERE HostID == :hostId")
    Host getHostByHostId(int hostId);

    @Query("UPDATE IHT SET MustSettleFlag = :mustSettleFlag WHERE HostID == :hostId")
    int updateMustSettleFlagByHostId(int hostId, int mustSettleFlag);

    @Query("SELECT HostID FROM IHT WHERE IssuerList LIKE :issuerIdLikeTxt")
    int getIssuerContainsHostRaw(String issuerIdLikeTxt);

}
