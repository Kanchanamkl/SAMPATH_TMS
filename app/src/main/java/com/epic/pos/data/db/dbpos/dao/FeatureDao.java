package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.epic.pos.data.db.dbpos.modal.Aid;
import com.epic.pos.data.db.dbpos.modal.Feature;

import java.util.List;

@Dao
public interface FeatureDao {

    @Insert
    void insert(Feature feature);

    @Query("SELECT * FROM FEATURE ORDER BY sorting_order")
    List<Feature> getAll();

    @Query("SELECT * FROM FEATURE WHERE enabled == 1 ORDER BY sorting_order")
    List<Feature> getEnabledFeatures();

    @Query("SELECT * FROM FEATURE WHERE id  == :id")
    Feature getFeatureById(int id);

}
