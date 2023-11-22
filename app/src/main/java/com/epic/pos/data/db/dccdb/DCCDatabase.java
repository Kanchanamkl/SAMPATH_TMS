package com.epic.pos.data.db.dccdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dccdb.dao.DCCBINDao;
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.data.db.dccdb.model.DCCData;
import com.epic.pos.data.db.dccdb.dao.DCCDao;

@Database(
        entities = {
                  DCCData.class,
                  DCCBINNLIST.class
        }, version = 1)
public abstract class DCCDatabase extends RoomDatabase {
    private static DCCDatabase instance;
    public abstract DCCDao dccDao();
    public abstract DCCBINDao dccBINDao();
    public static synchronized DCCDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, DCCDatabase.class, Const.EPIC_DCC_DB)
                    .createFromAsset("databases/" + Const.EPIC_DCC_DB)
                    .build();
        }

        return instance;
    }
}

