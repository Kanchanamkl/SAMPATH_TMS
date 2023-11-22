package com.epic.pos.data.db.dbpos;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.epic.pos.common.Const;
import com.epic.pos.data.db.dbpos.dao.AidDao;
import com.epic.pos.data.db.dbpos.dao.CardDefinitionDao;
import com.epic.pos.data.db.dbpos.dao.ConfigMapDao;
import com.epic.pos.data.db.dbpos.dao.CurrencyDao;
import com.epic.pos.data.db.dbpos.dao.FeatureDao;
import com.epic.pos.data.db.dbpos.dao.HostDao;
import com.epic.pos.data.db.dbpos.dao.IssuerDao;
import com.epic.pos.data.db.dbpos.dao.MerchantDao;
import com.epic.pos.data.db.dbpos.dao.RawDao;
import com.epic.pos.data.db.dbpos.dao.TCPDao;
import com.epic.pos.data.db.dbpos.dao.TCTDao;
import com.epic.pos.data.db.dbpos.dao.TFIDao;
import com.epic.pos.data.db.dbpos.dao.TLEDao;
import com.epic.pos.data.db.dbpos.dao.TerminalDao;
import com.epic.pos.data.db.dbpos.modal.Aid;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;
import com.epic.pos.data.db.dbpos.modal.ConfigMap;
import com.epic.pos.data.db.dbpos.modal.Currency;
import com.epic.pos.data.db.dbpos.modal.Feature;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCP;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbpos.modal.TFI;
import com.epic.pos.data.db.dbpos.modal.TLE;
import com.epic.pos.data.db.dbpos.modal.Terminal;

@Database(
        entities = {
                CardDefinition.class,
                ConfigMap.class,
                Currency.class,
                Host.class,
                Issuer.class,
                Merchant.class,
                TCP.class,
                TCT.class,
                Terminal.class,
                TFI.class,
                TLE.class,
                Aid.class,
                Feature.class
              //  DCCData.class
//                USER.class
        },
        version = 5
)
public abstract class EpicAPOSDatabase extends RoomDatabase {

    private static final String TAG = "EpicAPOSDatabase";

    private static EpicAPOSDatabase instance;

    public abstract CardDefinitionDao cdtDao();
    public abstract ConfigMapDao configMapDao();
    public abstract CurrencyDao currencyDao();
    public abstract HostDao hostDao();
    public abstract IssuerDao issuerDao();
    public abstract MerchantDao merchantDao();
    public abstract TCPDao tcpDao();
    public abstract TCTDao tctDao();
    public abstract TerminalDao terminalDao();
    public abstract TFIDao tfiDao();
    public abstract TLEDao tleDao();
    public abstract RawDao rawDao();
    public abstract AidDao aidDao();
    public abstract FeatureDao featureDao();
   // public abstract DCCDao dccDao();
//    public abstract UserDao UserDao();

    public static synchronized EpicAPOSDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, EpicAPOSDatabase.class, Const.EPIC_A_POS_DB)
                    .createFromAsset("databases/" + Const.EPIC_A_POS_DB)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .build();
        }

        return instance;
    }

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate version 3 to 4 - no changes!");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate: version 4 to 5");
            try {
//                String Q1 = "ALTER TABLE TCT ADD BypassTxnConf INTEGER NOT NULL DEFAULT 0";
//                String Q2 = "ALTER TABLE TCT ADD SkipReceipt INTEGER NOT NULL DEFAULT 0";
//                String Q3 = "ALTER TABLE TCT ADD OfflineTranLimit TEXT NOT NULL DEFAULT '10000000'";
             //   String Q4 = "ALTER TABLE TCT ADD APPCODE TEXT NOT NULL DEFAULT SAMNEXGON82";

              //  String Q5 = "CREATE TABLE USER ( ID	INTEGER NOT NULL, UserName	TEXT NOT NULL, Password	TEXT NOT NULL, Status	TEXT NOT NULL DEFAULT 'ACT', ManualSale	INTEGER NOT NULL DEFAULT 1)";
//                database.execSQL(Q1);
//                database.execSQL(Q2);
//                database.execSQL(Q3);
             //   database.execSQL(Q4);
          //      database.execSQL(Q5);

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };


}
