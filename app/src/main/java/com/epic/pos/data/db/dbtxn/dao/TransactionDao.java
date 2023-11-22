package com.epic.pos.data.db.dbtxn.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbtxn.modal.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTransactions(List<Transaction> transactions);

    @Delete
    void delete(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Query("SELECT * FROM `TRANSACTION`")
    List<Transaction> getAll();

    @Query("SELECT * FROM `TRANSACTION` WHERE id == :id")
    Transaction getTransactionById(int id);

    @Query("SELECT * FROM `TRANSACTION` ORDER BY id DESC LIMIT 1")
    Transaction getLastTransaction();

    @Query("SELECT * FROM `TRANSACTION` WHERE host == :hostId AND merchant_no == :merchantNo AND invoice_no == :invoiceNo")
    Transaction getTransaction(int hostId, int merchantNo, String invoiceNo);

    @Query("SELECT * FROM `TRANSACTION` WHERE merchant_id == :merchantId AND host == :hostId")
    List<Transaction> getTransactionByMerchantAndHost(String merchantId, int hostId);

    @Query("DELETE FROM `TRANSACTION` WHERE merchant_id == :merchantId AND host == :hostId AND transaction_code in (:txnCodeList)")
    int deleteTransactionByMerchantAndHostAndTxnCodes(String merchantId, int hostId, List<Integer> txnCodeList);

    @Query("DELETE FROM `TRANSACTION` WHERE merchant_no == :merchantNo AND host == :hostId")
    int deleteTransactions(int merchantNo, int hostId);

    @Query("DELETE FROM `TRANSACTION`")
    int deleteAll();

    @Query("SELECT COUNT(id) FROM `TRANSACTION`")
    int getTransactionCount();

    @Query("SELECT COUNT(id) FROM `TRANSACTION` WHERE merchant_no = :merchantNumber")
    int getTransactionCountByMerchant(int merchantNumber);

}
