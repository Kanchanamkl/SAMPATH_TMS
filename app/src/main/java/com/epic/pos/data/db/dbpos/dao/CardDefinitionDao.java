package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;

import java.util.List;

@Dao
public interface CardDefinitionDao {

    @Insert
    void insert(CardDefinition cardDefinition);

    @Query("SELECT * FROM CDT")
    List<CardDefinition> getAll();

    @Query("SELECT * FROM CDT WHERE PANLow <= :pan AND PANHigh >= :pan")
    List<CardDefinition> getCdtByPan(String pan);

    @Query("SELECT * FROM CDT WHERE ID == :id")
    CardDefinition getCardDefinitionById(int id);

}
