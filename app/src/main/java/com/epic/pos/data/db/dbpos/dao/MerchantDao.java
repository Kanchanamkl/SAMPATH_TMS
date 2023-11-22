package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbpos.modal.Merchant;

import java.util.List;

@Dao
public interface MerchantDao {

    @Insert
    void insert(Merchant merchant);

    @Update
    void update(Merchant merchant);

    @Query("SELECT * FROM MIT")
    List<Merchant> getAll();

    @Query("SELECT * FROM MIT WHERE IsInstallment == :hasInstallmentSupport AND IsEnabled == 1 GROUP BY GroupId")
    List<Merchant> getEnabledMerchants(int hasInstallmentSupport);

    @Query("SELECT * FROM MIT WHERE GroupId == :groupId AND IsEnabled == 1")
    List<Merchant> getEnableMerchantsFromGroupId(int groupId);

    @Query("SELECT * FROM MIT WHERE MerchantNumber == :id AND IsEnabled == 1")
    Merchant getEnableMerchantById(int id);

    @Query("SELECT * FROM MIT WHERE HostId == :hostId AND IsEnabled == 1")
    List<Merchant> getEnableMerchantsByHostId(int hostId);

    @Query("SELECT * FROM MIT WHERE HostId == :hostId AND IsInstallment == 0 AND IsEnabled == 1")
    List<Merchant> getSaleSupportMerchantsByHost(int hostId);

    @Query("SELECT * FROM MIT WHERE MerchantNumber == :id LIMIT 1")
    Merchant getMerchantById(int id);

    @Query("UPDATE MIT SET BatchNumber = :batchNumber WHERE MerchantNumber == :merchantId")
    int updateBatchIdByMerchantId(int merchantId, String batchNumber);

    @Query("SELECT * FROM MIT WHERE MerchantNumber IN (:merchantNumbers)")
    List<Merchant> getMerchantsByMerchantNumbers(List<Integer> merchantNumbers);

}
