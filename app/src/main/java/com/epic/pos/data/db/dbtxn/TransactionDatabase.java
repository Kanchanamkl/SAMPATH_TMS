package com.epic.pos.data.db.dbtxn;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbtxn.dao.ReversalDao;
import com.epic.pos.data.db.dbtxn.dao.TransactionDao;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;

@Database(
        entities = {
                Transaction.class,
                Reversal.class
        },
        version = 3
)
public abstract class TransactionDatabase extends RoomDatabase {

    private static final String TAG = "EpicAPOSDatabase";

    private static TransactionDatabase instance;
    
    public abstract TransactionDao tranDao();
    public abstract ReversalDao reversalDao();

    public static synchronized TransactionDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, TransactionDatabase.class, Const.EPIC_TRANSACTION_DB)
                    .addMigrations(MIGRATION_2_3)
                    .build();
        }

        return instance;
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //
        }
    };


}
