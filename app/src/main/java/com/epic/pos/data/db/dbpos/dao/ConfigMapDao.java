package com.epic.pos.data.db.dbpos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.ConfigMap;
import com.epic.pos.data.db.dbpos.modal.TLE;

import java.util.List;

@Dao
public interface ConfigMapDao {

    @Insert
    void insert(ConfigMap configMap);

    @Update
    void update(ConfigMap configMap);

    @Query("SELECT * FROM ConfigMap WHERE ParameterName == :paramName")
    ConfigMap getConfigDataByParamName(String paramName);

    @Query("DELETE FROM ConfigMap WHERE TableName == :tableName")
    void deleteConfigMapByTableName(String tableName);
}
