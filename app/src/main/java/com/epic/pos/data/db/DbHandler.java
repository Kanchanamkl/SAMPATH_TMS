package com.epic.pos.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.epic.pos.data.db.dbpos.EpicAPOSDatabase;
import com.epic.pos.data.db.dbpos.dao.AidDao;
import com.epic.pos.data.db.dbpos.dao.CardDefinitionDao;
import com.epic.pos.data.db.dbpos.dao.ConfigMapDao;
import com.epic.pos.data.db.dbpos.dao.CurrencyDao;
import com.epic.pos.data.db.dccdb.dao.DCCBINDao;
import com.epic.pos.data.db.dccdb.dao.DCCDao;
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
import com.epic.pos.data.db.dccdb.model.DCCBINNLIST;
import com.epic.pos.data.db.dccdb.model.DCCData;
import com.epic.pos.data.db.dbpos.modal.Feature;
import com.epic.pos.data.db.dbpos.modal.Host;
import com.epic.pos.data.db.dbpos.modal.Issuer;
import com.epic.pos.data.db.dbpos.modal.Merchant;
import com.epic.pos.data.db.dbpos.modal.TCT;
import com.epic.pos.data.db.dbpos.modal.TLE;
import com.epic.pos.data.db.dbpos.modal.Terminal;
import com.epic.pos.data.db.dbtxn.TransactionDatabase;
import com.epic.pos.data.db.dbtxn.dao.ReversalDao;
import com.epic.pos.data.db.dbtxn.dao.TransactionDao;
import com.epic.pos.data.db.dbtxn.modal.Reversal;
import com.epic.pos.data.db.dbtxn.modal.Transaction;
import com.epic.pos.data.db.dccdb.DCCDatabase;
import com.epic.pos.domain.entity.ColumnInfoEntity;
import com.epic.pos.device.PosDevice;

import java.util.ArrayList;
import java.util.List;
/**
 * DbHandler class is used to manage app databases
 *
 * @author Arvin Jayanake
 * @version 1.0
 * @since 2021-03-26
 */
public class DbHandler {

    private static DbHandler mInstance;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Context context;
    //DCC database
    private DCCDatabase dccdb;
    //Pos database
    private EpicAPOSDatabase posDb;
    private CardDefinitionDao cardDefinitionDao;
    private ConfigMapDao configMapDao;
    private CurrencyDao currencyDao;
    private HostDao hostDao;
    private IssuerDao issuerDao;
    private MerchantDao merchantDao;
    private TCPDao tcpDao;
    private TCTDao tctDao;
    private TerminalDao terminalDao;
    private TFIDao tfiDao;
    private TLEDao tleDao;
    private RawDao rawDao;
    private AidDao aidDao;
    private FeatureDao featureDao;
    //Txn database
    private TransactionDao transactionDao;
    private ReversalDao reversalDao;

    private DCCDao dccDao;
    private DCCBINDao dccBINDao;

    /**
     * Get all aids from db
     *
     * @param listener
     */
    public void getAllAids(GetAidsListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Aid> aidList = aidDao.getAll();
                listener.onReceived(aidList);
            }
        }.start();
    }

    /**
     * get aid by issuer
     *
     * @param issuerId
     * @param listener
     */
    public void getAidByIssuer(int issuerId, AidListener listener) {
        new Thread() {
            @Override
            public void run() {
                Aid aid = aidDao.getAidByIssuer(issuerId);
                listener.onReceived(aid);
            }
        }.start();
    }

    /**
     * Update BatchId By MerchantId
     *
     * @param merchantId
     * @param batchNumber
     * @param listener
     */
    public void updateBatchIdByMerchantId(int merchantId, String batchNumber,
                                          UpdateBatchIdByMerchantIdListener listener) {
        new Thread() {
            @Override
            public void run() {
                int id = merchantDao.updateBatchIdByMerchantId(merchantId, batchNumber);
                handler.post(listener::onReceived);
            }
        }.start();
    }

    /**
     * Get TMIF By Host And Merchant ID
     *
     * @param hostID
     * @param merchantID
     * @param listener
     */
    public void getTMIFByHostAndMerchant(int hostID, int merchantID, GetTMIFByHostAndMerchantListener listener) {
        new Thread() {
            @Override
            public void run() {
                Terminal terminalId = terminalDao.getTMIFByHostAndMerchant(hostID, merchantID);
                handler.post(() -> listener.onReceived(terminalId));
            }
        }.start();
    }

    /**
     * Get First TID By Host
     *
     * @param hostID
     * @param listener
     */
    public void getTerminalByHost(int hostID, GetTerminalListener listener) {
        new Thread() {
            @Override
            public void run() {
                Terminal terminal = terminalDao.getTerminalByHost(hostID);
                handler.post(() -> listener.onReceived(terminal));
            }
        }.start();
    }

    /**
     * Get Terminals By Host
     *
     * @param hostID
     * @param listener
     */
    public void getAllTerminalsByHost(int hostID, GetTerminalsListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Terminal> terminals = terminalDao.getAllTerminalsByHost(hostID);
                handler.post(() -> listener.onReceived(terminals));
            }
        }.start();
    }

    /**
     * update terminal
     *
     * @param terminal
     * @param listener
     */
    public void updateTerminal(Terminal terminal, UpdateListener listener) {
        new Thread() {
            @Override
            public void run() {
                terminalDao.update(terminal);
                if (listener != null) {
                    handler.post(listener::onSuccess);
                }
            }
        }.start();
    }

    public Cursor getTableData(String tableName) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM " + tableName + "");
        return rawDao.getTableData(query);
    }
    public void getDCCData(GetDCCDataCursorListener listener) {
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM DCCData");
                try {
                    Cursor cursor = dccdb.query(query);
                    handler.post(() -> listener.onReceived(cursor));
                } catch (Exception e) {
                    Cursor c=null;
                    handler.post(() -> listener.onReceived(c));
                    e.printStackTrace();
                }
            }
        }.start();

    }
    public void getDCCDataByCcode(GetDCCDataCursorListener listener,String CCode) {
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM DCCData WHERE CCode = "+ CCode);
                try {
                    Cursor cursor = dccdb.query(query);
                    handler.post(() -> listener.onReceived(cursor));
                } catch (Exception e) {
                    Cursor c=null;
                    handler.post(() -> listener.onReceived(c));
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void checkdccbin(CheckDCCBINListener listener,String pan) {
        new Thread() {
            @Override
            public void run() {

                try {
                    List<DCCBINNLIST> dccbinlist= dccdb.dccBINDao().getbinlistbyrange(pan);
                    handler.post(() -> listener.onDCCBIN(dccbinlist));
                } catch (Exception e) {

                    handler.post(() -> listener.NotDCCBIN(e.getMessage()));
                    e.printStackTrace();
                }
            }
        }.start();

    }
    public  void insertdccbinlist(UPDATEDCCBINListener listener,  List <DCCBINNLIST>  values){
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM DCCBIN");
                try {
                    dccdb.dccBINDao().deleteall();

                    dccdb.dccBINDao().insertAllDCCBinlist(values);

                    handler.post(() -> listener.OnUpdated());
                } catch (Exception e) {
                    Cursor c=null;
                    handler.post(() -> listener.OnError(e.getMessage()));
                    e.printStackTrace();
                }
            }
        }.start();

    }
    public void getcursymbycode(getcursymbycode listener,int ccode) {
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM CUR WHERE Code="+ ccode);
                try {
                    Cursor cursor = rawDao.rowQuery(query);
                    handler.post(() -> listener.onReceived(cursor));
                } catch (Exception e) {
                    Cursor c=null;
                    handler.post(() -> listener.onReceived(c));
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void getuserbyusernameandpassword(String uname, String password, GetTableDataCursorListener listener) {
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM USER WHERE STATUS ='ACT' and ManualSale =1 AND UserName ='" + uname + "' AND Password = '"+ password + "'");
                try {
                    Cursor cursor = rawDao.rowQuery(query);
                    handler.post(() -> listener.onReceived(cursor));
                } catch (Exception e) {
                    Cursor c=null;
                    handler.post(() -> listener.onReceived(c));
                    e.printStackTrace();
                }
            }
        }.start();

    }
    public void insertConfigMap(String parameterName, String tableName, String columnName, int rowIndex, String value) {
        configMapDao.insert(new ConfigMap(parameterName, tableName, columnName, rowIndex, value));
    }

    /**
     * Get table columns
     *
     * @param tableName
     * @param listener
     */
    public void getTableColumns(String tableName, GetTableColumnsListener listener) {
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("pragma table_info(`" + tableName + "`)");
                try {
                    Cursor cursor = rawDao.rowQuery(query);
                    List<String> columns = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        String columnName = cursor.getString(cursor.getColumnIndex("name"));
                        columns.add(columnName);
                    }
                    cursor.close();
                    handler.post(() -> listener.onReceived(columns));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public void getTableDataCursor(String tableName, int rowIndex, GetTableDataCursorListener listener) {
        new Thread() {
            @Override
            public void run() {
                ;
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM " + tableName + " LIMIT 1 OFFSET " + (rowIndex - 1));
                try {
                    Cursor cursor = rawDao.rowQuery(query);
                    handler.post(() -> listener.onReceived(cursor));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Get column info
     *
     * @param tableName
     * @param listener
     */
    public void getTableColumnInfo(String tableName, GetTableColumnInfoListener listener) {
        new Thread() {
            @Override
            public void run() {
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("pragma table_info(`" + tableName + "`)");
                try {
                    Cursor cursor = rawDao.rowQuery(query);
                    List<ColumnInfoEntity> columns = new ArrayList<>();

                    while (cursor.moveToNext()) {
                        int cid = cursor.getInt(cursor.getColumnIndex("cid"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String type = cursor.getString(cursor.getColumnIndex("type"));
                        boolean isNotNull = cursor.getInt(cursor.getColumnIndex("notnull")) == 1;
                        boolean isPrimaryKey = cursor.getInt(cursor.getColumnIndex("pk")) == 1;
                        ColumnInfoEntity columnInfoEntity = new ColumnInfoEntity(cid, name, type, isNotNull, isPrimaryKey);
                        columnInfoEntity.setTable(tableName);
                        columns.add(columnInfoEntity);
                    }
                    cursor.close();

                    handler.post(() -> listener.onReceived(columns));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void updateTableInfo(String tableName, List<ColumnInfoEntity> entities, UpdateTableInfoListener listener) {
        new Thread() {
            @Override
            public void run() {
                SupportSQLiteDatabase db = posDb.getOpenHelper().getWritableDatabase();

                ColumnInfoEntity primaryKey = null;
                ContentValues values = new ContentValues();

                for (ColumnInfoEntity c : entities) {
                    if (c.isPrimaryKey()) {
                        primaryKey = c;
                    } else {
                        values.put(c.getColumnName(), c.getValue());
                    }
                }

                int update = db.update(tableName, SQLiteDatabase.CONFLICT_ROLLBACK, values, primaryKey.getColumnName() + "=?", new String[]{primaryKey.getValue()});
                handler.post(listener::onUpdated);
            }
        }.start();
    }

    public void deleteConfigMapByTableName(String tableName) {
        configMapDao.deleteConfigMapByTableName(tableName);
    }


    public long updateProfileData(String tableName,
                                  String columnName,
                                  String value,
                                  String primaryColName,
                                  String rowID) {
        String updateQuary = "UPDATE " + tableName + " SET " + columnName + " = " + "'" + value + "'" + " WHERE " + primaryColName + " = '" + rowID + "'";
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(updateQuary);
        return rawDao.updateProfileData(query);
    }

    /**
     * Get Primary Column Name by Table Name Name
     *
     * @param tableName
     * @return
     */
    public String getTablePrimaryColumnName(String tableName) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("pragma table_info(" + tableName + ")");
        try {
            Cursor cursor = rawDao.getTablePrimaryColumnName(query);
            String data = null;
            if (cursor.moveToFirst()) {
                data = cursor.getString(cursor.getColumnIndex("name"));
            }
            cursor.close();
            String finalData = data;
            return finalData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get Config Data by Param Name
     *
     * @param paramName
     * @return
     */
    public ConfigMap getConfigDataByParamName(String paramName) {
        ConfigMap configMap = configMapDao.getConfigDataByParamName(paramName);
        return configMap;
    }


    /**
     * Get CDT by PAN
     *
     * @param pan
     * @param listener
     */
    public void getCdtByPan(String pan, GetCDTByPanListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<CardDefinition> cardDefinitions = cardDefinitionDao.getCdtByPan(pan);
                handler.post(() -> listener.onReceived(cardDefinitions));
            }
        }.start();
    }

    /**
     * Get all CDTs from the database
     *
     * @param listener
     */
    public void getAllCDTs(GetAllCDTListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<CardDefinition> allCardDefinitions = cardDefinitionDao.getAll();
                handler.post(() -> listener.onReceived(allCardDefinitions));
            }
        }.start();
    }

    /**
     * Insert CDT
     *
     * @param cardDefinition
     */
    public void insertCDT(CardDefinition cardDefinition) {
        new Thread() {
            @Override
            public void run() {
                cardDefinitionDao.insert(cardDefinition);
            }
        }.start();
    }

    /**
     * Get terminals
     *
     * @param listener
     */
    public void getTerminals(GetTerminalsListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Terminal> terminals = terminalDao.getAll();
                handler.post(() -> listener.onReceived(terminals));
            }
        }.start();
    }

    /**
     * Get hosts
     *
     * @param listener
     */
    public void getHosts(GetHostListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Host> hosts = hostDao.getAll();
                listener.onReceived(hosts);
            }
        }.start();
    }

    /**
     * Get merchants
     *
     * @param listener
     */
    public void getMerchants(GetMerchantListListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Merchant> hosts = merchantDao.getAll();
                handler.post(() -> listener.onReceived(hosts));

            }
        }.start();
    }

    /**
     * Get enabled merchant list
     *
     * @param hasInstallmentSupport
     * @param listener
     */
    public void getEnabledMerchants(boolean hasInstallmentSupport, GetMerchantListListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Merchant> merchants = merchantDao.getEnabledMerchants(hasInstallmentSupport ? 1 : 0);
                handler.post(() -> listener.onReceived(merchants));
            }
        }.start();
    }

    /**
     * Get enable merchants from group id
     *
     * @param groupId
     * @param listener
     */
    public void getEnableMerchantsFromGroupId(int groupId, GetMerchantListListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Merchant> merchants = merchantDao.getEnableMerchantsFromGroupId(groupId);
                handler.post(() -> listener.onReceived(merchants));
            }
        }.start();
    }

    /**
     * Get enable merchants by host id
     *
     * @param hostId
     * @param listener
     */
    public void getEnableMerchantsByHost(int hostId, GetMerchantListListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Merchant> merchants = merchantDao.getEnableMerchantsByHostId(hostId);
                handler.post(() -> listener.onReceived(merchants));
            }
        }.start();
    }

    /**
     * Get enable merchants by host id
     *
     * @param hostId
     * @param listener
     */
    public void getSaleSupportMerchantsByHost(int hostId, GetMerchantListListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Merchant> merchants = merchantDao.getSaleSupportMerchantsByHost(hostId);
                handler.post(() -> listener.onReceived(merchants));
            }
        }.start();
    }


    /**
     * Get Merchants By Merchant Numbers
     *
     * @param listener
     */
    public void getMerchantsByMerchantNumbers(List<Integer> merchantNumbers, GetMerchantListListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Merchant> hosts = merchantDao.getMerchantsByMerchantNumbers(merchantNumbers);
                handler.post(() -> listener.onReceived(hosts));
            }
        }.start();
    }

    /**
     * Update merchant
     *
     * @param merchant
     * @param listener
     */
    public void updateMerchant(Merchant merchant, UpdateListener listener) {
        new Thread() {
            @Override
            public void run() {
                merchantDao.update(merchant);
                if (listener != null) {
                    handler.post(listener::onSuccess);
                }
            }
        }.start();
    }

    /**
     * Get Issuer by issuer id
     *
     * @param issuerId
     * @param listener
     */
    public void getIssuerById(int issuerId, GetIssuerByIdListener listener) {
        new Thread() {
            @Override
            public void run() {
                Issuer issuer = issuerDao.getIssuerById(issuerId);
                handler.post(() -> listener.onReceived(issuer));
            }
        }.start();
    }

    /**
     * Get issuer contains host from host table
     *
     * @param issuerId
     * @param listener
     */
    public void getIssuerContainsHost(int issuerId, GetIssuerListener listener) {
        String likeText = "%" + issuerId + "%";
        new Thread() {
            @Override
            public void run() {
                Host host = hostDao.getIssuerContainsHost(likeText);
                handler.post(() -> listener.onReceived(host));
            }
        }.start();
    }

    /**
     * Get Host By Id from host table
     *
     * @param hostId
     * @param listener
     */
    public void getHostByHostId(int hostId, GetHostByHostIdListener listener) {
        new Thread() {
            @Override
            public void run() {
                Host host = hostDao.getHostByHostId(hostId);
                handler.post(() -> listener.onReceived(host));
            }
        }.start();
    }

    /**
     * Update Must Settle Flag By Host Id from host table
     *
     * @param hostId
     * @param listener
     */
    public void updateMustSettleFlagByHostId(int hostId, int mustSettleFlag, UpdateMustSettleFlagByHostIdListener listener) {
        new Thread() {
            @Override
            public void run() {
                hostDao.updateMustSettleFlagByHostId(hostId, mustSettleFlag);
                handler.post(listener::onReceived);
            }
        }.start();
    }

    /**
     * Get terminal by id
     *
     * @param id       primary key
     * @param listener
     */
    public void getTerminalById(int id, GetTerminalListener listener) {
        new Thread() {
            @Override
            public void run() {
                Terminal t = terminalDao.getTerminalById(id);
                handler.post(() -> listener.onReceived(t));
            }
        }.start();
    }

    /**
     * get terminal by merchant
     *
     * @param merchantId
     * @param listener
     */
    public void getTerminalByMerchant(int merchantId, GetTerminalListener listener) {
        new Thread() {
            @Override
            public void run() {
                Terminal t = terminalDao.getTerminalByMerchant(merchantId);
                handler.post(() -> listener.onReceived(t));
            }
        }.start();
    }

    /**
     * Get merchant by id
     *
     * @param id       primary key
     * @param listener
     */
    public void getMerchantById(int id, GetMerchantListener listener) {
        new Thread() {
            @Override
            public void run() {
                Merchant m = merchantDao.getMerchantById(id);
                handler.post(() -> listener.onReceived(m));
            }
        }.start();
    }


    /**
     * Get enable merchant by id
     *
     * @param id       primary key
     * @param listener
     */
    public void getEnableMerchantById(int id, GetMerchantListener listener) {
        new Thread() {
            @Override
            public void run() {
                Merchant m = merchantDao.getEnableMerchantById(id);
                handler.post(() -> listener.onReceived(m));
            }
        }.start();
    }

    /**
     * Get currency by merchant id
     *
     * @param id       merchant id
     * @param listener
     */
    public void getCurrencyByMerchantId(int id, GetCurrencyListener listener) {
        new Thread() {
            @Override
            public void run() {
                Currency c = currencyDao.getCurrencyByMerchantId(id);
                handler.post(() -> listener.onReceived(c));
            }
        }.start();
    }

    /**
     * update currency
     *
     * @param currency
     * @param listener
     */
    public void updateCurrency(Currency currency, UpdateListener listener) {
        new Thread() {
            @Override
            public void run() {
                currencyDao.update(currency);
                if (listener != null) {
                    handler.post(listener::onSuccess);
                }
            }
        }.start();
    }

    /**
     * Get card definition by id
     *
     * @param id
     * @param listener
     */
    public void getCardDefinitionById(int id, GetCardDefinitionListener listener) {
        new Thread() {
            @Override
            public void run() {
                CardDefinition c = cardDefinitionDao.getCardDefinitionById(id);
                handler.post(() -> listener.onReceived(c));
            }
        }.start();
    }

    //Transaction db


    /**
     * Get all reversals
     *
     * @param listener
     */
    public void getReversals(GetReversalsListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Reversal> all = reversalDao.getAll();
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(all);
                });
            }
        }.start();
    }

    /**
     * Get reversals by host
     *
     * @param listener
     */
    public void getReversalsByHost(int host, GetReversalsListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Reversal> all = reversalDao.getReversalsByHost(host);
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(all);
                });
            }
        }.start();
    }
    /**
     * Insert reversal
     *
     * @param reversal
     */
    public void insertReversal(Reversal reversal, InsertListener insertListener) {
        new Thread() {
            @Override
            public void run() {
                long id = reversalDao.insert(reversal);
                handler.post(() -> {
                    if (insertListener != null)
                        insertListener.onSuccess(id);
                });
            }
        }.start();
    }
    public void insertdccdata(DCCData dccData, InsertListener insertListener) {
        SupportSQLiteDatabase db = dccdb.getOpenHelper().getWritableDatabase();
        new Thread() {
            @Override
            public void run() {
            //    dccDao.insert(dccData);
                ContentValues values =  new ContentValues();
                values.put("CCode",dccData.getCCode());
                values.put("CSymbol",dccData.getCSymbol());
                values.put("ConversionRate",dccData.getConversionRate());
                values.put("BCCode",dccData.getBCCode());

                db.insert("DCCData",SQLiteDatabase.CONFLICT_REPLACE,values);
                handler.post(() -> {
                    if (insertListener != null)
                        insertListener.onSuccess(1);
                });
            }
        }.start();
    }
    /**
     * Delete reversal
     *
     * @param reversal
     * @param listener
     */
    public void deleteReversal(Reversal reversal, DeleteListener listener) {
        new Thread() {
            @Override
            public void run() {
                reversalDao.delete(reversal);
                handler.post(() -> {
                    if (listener != null)
                        listener.onSuccess();
                });
            }
        }.start();
    }

    /**
     * Delete all transactions
     *
     * @param listener
     */
    public void deleteAllTransactions(DeleteListener listener) {
        new Thread() {
            @Override
            public void run() {
                transactionDao.deleteAll();
                handler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                });
            }
        }.start();
    }

    /**
     * Insert all transactions
     *
     * @param transactions
     * @param listener
     */
    public void insertAllTransactions(List<Transaction> transactions, InsertListener listener){
        new Thread(){
            @Override
            public void run() {
                transactionDao.insertAllTransactions(transactions);
                handler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(1);
                    }
                });
            }
        }.start();
    }

    /**
     * Insert all reversals
     *
     * @param reversals
     * @param listener
     */
    public void insertAllReversals(List<Reversal> reversals, InsertListener listener){
        new Thread(){
            @Override
            public void run() {
                reversalDao.insertAllReversals(reversals);
                handler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess(1);
                    }
                });
            }
        }.start();
    }

    /**
     * Delete all reversals
     *
     * @param listener
     */
    public void deleteAllReversals(DeleteListener listener){
        new Thread() {
            @Override
            public void run() {
                reversalDao.deleteAll();
                handler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                });
            }
        }.start();
    }

    /**
     * Insert transaction
     *
     * @param transaction
     * @param listener
     */
    public void insertTransaction(Transaction transaction, InsertListener listener) {
        new Thread() {
            @Override
            public void run() {
                long id = transactionDao.insert(transaction);
                handler.post(() -> {
                    if (listener != null)
                        listener.onSuccess(id);
                });
            }
        }.start();
    }

    /**
     * Update transaction
     *
     * @param transaction
     * @param listener
     */
    public void updateTransaction(Transaction transaction, UpdateListener listener) {
        new Thread() {
            @Override
            public void run() {
                transactionDao.update(transaction);
                handler.post(() -> {
                    if (listener != null)
                        listener.onSuccess();
                });
            }
        }.start();
    }

    /**
     * Get transaction by id
     *
     * @param id
     * @param listener
     */
    public void getTransactionById(int id, GetTransactionListener listener) {
        new Thread() {
            @Override
            public void run() {
                Transaction t = transactionDao.getTransactionById(id);
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(t);
                });
            }
        }.start();
    }

    /**
     * Get last transaction
     *
     * @param listener
     */
    public void getLastTransaction(GetTransactionListener listener) {
        new Thread() {
            @Override
            public void run() {
                Transaction t = transactionDao.getLastTransaction();
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(t);
                });
            }
        }.start();
    }


    /**
     * Get TLE data by issuer
     *
     * @param issuerNumber
     * @param listener
     */
    public void getTLEData(int issuerNumber, GetTLEListener listener) {
        new Thread() {
            @Override
            public void run() {
                TLE t = tleDao.getTLEbyIssuer(issuerNumber);
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(t);
                });
            }
        }.start();
    }

    /**
     * Get transaction
     *
     * @param hostId
     * @param merchantNo
     * @param invoiceNo
     * @param listener
     */
    public void getTransaction(int hostId, int merchantNo, String invoiceNo, GetTransactionListener listener) {
        new Thread() {
            @Override
            public void run() {
                Transaction t = transactionDao.getTransaction(hostId, merchantNo, invoiceNo);
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(t);
                });
            }
        }.start();
    }

    /**
     * Get transaction by merchant number
     *
     * @param merchantId
     * @param hostId
     * @param listener
     */
    public void getTransactionByMerchantAndHost(String merchantId, int hostId, GetTransactionByInvoiceNoListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Transaction> t = transactionDao.getTransactionByMerchantAndHost(merchantId, hostId);
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(t);
                });
            }
        }.start();
    }

    /**
     * Delete transaction by merchant number
     *
     * @param merchantId
     * @param listener
     */
    public void deleteTransactionByMerchantAndHostAndTxnCodes(String merchantId, int hostId, List<Integer> txnCodeList, DeleteTransactionByInvoiceNoListener listener) {
        new Thread() {
            @Override
            public void run() {
                int t = transactionDao.deleteTransactionByMerchantAndHostAndTxnCodes(merchantId, hostId, txnCodeList);
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived();
                });
            }
        }.start();
    }

    /**
     * Delete transactions by host and
     *
     * @param merchantNo
     * @param hostId
     * @param listener
     */
    public void deleteTransactions(int merchantNo, int hostId, DeleteListener listener) {
        new Thread() {
            @Override
            public void run() {
                transactionDao.deleteTransactions(merchantNo, hostId);
                handler.post(() -> {
                    if (listener != null)
                        listener.onSuccess();
                });
            }
        }.start();
    }

    /**
     * Get all transactions
     *
     * @param listener
     */
    public void getTransactions(GetTransactionsListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Transaction> transactions = transactionDao.getAll();
                handler.post(() -> listener.onReceived(transactions));
            }
        }.start();
    }

    /**
     * Get first tct record.
     *
     * @param listener
     */
    public void getTCT(GetTCTListener listener) {
        new Thread() {
            @Override
            public void run() {
                TCT tct = tctDao.getTCT();
                handler.post(() -> {
                    if (listener != null)
                        listener.onReceived(tct);
                });
            }
        }.start();
    }


    /**
     * Update auto settle date
     *
     * @param autoSettleDate
     * @param listener
     */
    public void updateAutoSettleDate(String autoSettleDate, UpdateTctListener listener) {
        new Thread() {
            @Override
            public void run() {
                tctDao.updateAutoSettleDate(autoSettleDate);
                if (listener != null) {
                    handler.post(listener::onUpdated);
                }
            }
        }.start();
    }

    /**
     * Get issuer contains host from host table
     *
     * @param issuerId
     * @param listener
     */
    public void getIssuerContainsHostRaw(int issuerId, GetIssuerHostListener listener) {
        String likeText = "%" + issuerId + "%";
        new Thread() {
            @Override
            public void run() {
                EpicAPOSDatabase posDb = EpicAPOSDatabase.getInstance(context);
                hostDao = posDb.hostDao();

                int host = hostDao.getIssuerContainsHostRaw(likeText);
                if(host==3){
                    host=1;
                }
                int finalHost = host;
                handler.post(() -> listener.onReceived(finalHost));
            }
        }.start();
    }

    /**
     * Get all issuers
     *
     * @param listener
     */
    public void getAllIssuers(GetIssuersListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Issuer> all = issuerDao.getAll();
                handler.post(() -> listener.onReceived(all));
            }
        }.start();
    }


    public void getTFIData(int issuerNo, GetTableDataListener listener) {
        new Thread() {
            @Override
            public void run() {
                EpicAPOSDatabase posDb = EpicAPOSDatabase.getInstance(context);
                rawDao = posDb.rawDao();

                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM TFI WHERE IssuerNumber = " + issuerNo);
                Cursor cursor = rawDao.getTableData(query);
                handler.post(() -> listener.onReceived(cursor));
            }
        }.start();
    }

    public void getAllFeatures(GetFeaturesListener listener) {
        new Thread() {
            @Override
            public void run() {
                List<Feature> all = featureDao.getEnabledFeatures();
                handler.post(() -> listener.onReceived(all));
            }
        }.start();
    }

    public void getFeature(int id, GetFeatureListener listener) {
        new Thread() {
            @Override
            public void run() {
                Feature f = featureDao.getFeatureById(id);
                handler.post(() -> listener.onReceived(f));
            }
        }.start();
    }

    public void getTransactionCount(GetCountListener listener) {
        new Thread() {
            @Override
            public void run() {
                int count = transactionDao.getTransactionCount();
                handler.post(() -> listener.onReceived(count));
            }
        }.start();
    }

    public void getTransactionCountByMerchant(int merchantNo, GetCountListener listener) {
        new Thread() {
            @Override
            public void run() {
                int count = transactionDao.getTransactionCountByMerchant(merchantNo);
                handler.post(() -> listener.onReceived(count));
            }
        }.start();
    }

    public interface GetCountListener {
        void onReceived(int count);
    }

    public interface GetFeatureListener {
        void onReceived(Feature feature);
    }

    public interface GetFeaturesListener {
        void onReceived(List<Feature> features);
    }

    public interface GetAidsListener {
        void onReceived(List<Aid> aids);
    }

    public interface AidListener {
        void onReceived(Aid aid);
    }

    public interface UpdateTableInfoListener {
        void onUpdated();
    }

    public interface GetTableDataCursorListener {
        void onReceived(Cursor cursor);
    }
    public interface GetDCCDataCursorListener {
        void onReceived(Cursor cursor);
    }
    public interface CheckDCCBINListener {
        void onDCCBIN(List<DCCBINNLIST> dccbinlist);

        void NotDCCBIN(String error);
    }

    public interface UPDATEDCCBINListener {
        void OnUpdated();
        void OnError(String message);
    }
    public interface getcursymbycode {
        void onReceived(Cursor cursor);
    }

    public interface UpdateTctListener {
        void onUpdated();
    }

    public interface GetTCTListener {
        void onReceived(TCT tct);
    }

    public interface GetTableColumnsListener {
        void onReceived(List<String> columns);
    }

    public interface GetTableColumnInfoListener {
        void onReceived(List<ColumnInfoEntity> columns);
    }

    public interface GetTransactionListener {
        void onReceived(Transaction transaction);
    }

    public interface GetTransactionByInvoiceNoListener {
        void onReceived(List<Transaction> transactions);
    }

    public interface GetTransactionsListener {
        void onReceived(List<Transaction> transactions);
    }

    public interface DeleteTransactionByInvoiceNoListener {
        void onReceived();
    }

    public interface DeleteListener {
        void onSuccess();
    }

    public interface InsertListener {
        void onSuccess(long id);
    }

    public interface UpdateListener {
        void onSuccess();
    }

    public interface GetCardDefinitionListener {
        void onReceived(CardDefinition cardDefinition);
    }

    public interface GetReversalsListener {
        void onReceived(List<Reversal> reversals);
    }

    public interface GetCurrencyListener {
        void onReceived(Currency currency);
    }

    public interface GetMerchantListener {
        void onReceived(Merchant merchant);
    }

    public interface GetIssuersListener {
        void onReceived(List<Issuer> issuers);
    }

    public interface GetIssuerListener {
        void onReceived(Host host);
    }

    public interface GetHostByHostIdListener {
        void onReceived(Host host);
    }

    public interface UpdateMustSettleFlagByHostIdListener {
        void onReceived();
    }

    public interface GetIssuerByIdListener {
        void onReceived(Issuer issuer);
    }

    public interface GetHostListener {
        void onReceived(List<Host> hosts);
    }

    public interface GetMerchantListListener {
        void onReceived(List<Merchant> merchants);
    }

    public interface GetTerminalListener {
        void onReceived(Terminal terminal);
    }

    public interface GetTerminalsListener {
        void onReceived(List<Terminal> terminals);
    }

    public interface GetCDTByPanListener {
        void onReceived(List<CardDefinition> cardDefinitions);
    }

    public interface GetTMIFByHostAndMerchantListener {
        void onReceived(Terminal terminalId);
    }

    public interface GetTerminalsByHostListener {
        void onReceived(List<Terminal> terminals);
    }

    public interface UpdateBatchIdByMerchantIdListener {
        void onReceived();
    }

    public interface GetAllCDTListener {
        void onReceived(List<CardDefinition> cardDefinitions);
    }

    public interface GetTableDataListener {
        void onReceived(Cursor cursor);
    }

    public interface GetTLEListener {
        void onReceived(TLE tle);
    }

    public interface GetIssuerHostListener {
        void onReceived(int host);
    }

    public static DbHandler getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        throw new RuntimeException(
                "EpicAPosDbHandler class not correctly instantiated. " +
                        "Please call EpicAPosDbHandler.Builder().setContext(context).build(); " +
                        "in the Application class onCreate.");
    }

    private DbHandler() {
        super();
        mInstance = this;
    }

    private void init(Context appContext) {
        context = appContext;
        //pos
        posDb = EpicAPOSDatabase.getInstance(context);
        cardDefinitionDao = posDb.cdtDao();
        configMapDao = posDb.configMapDao();
        currencyDao = posDb.currencyDao();
        hostDao = posDb.hostDao();
        issuerDao = posDb.issuerDao();
        merchantDao = posDb.merchantDao();
        tcpDao = posDb.tcpDao();
        tctDao = posDb.tctDao();
        terminalDao = posDb.terminalDao();
        tfiDao = posDb.tfiDao();
        tleDao = posDb.tleDao();
        rawDao = posDb.rawDao();
        aidDao = posDb.aidDao();
        featureDao = posDb.featureDao();

        //dcc database
        dccdb = DCCDatabase.getInstance(context);
        dccDao = dccdb.dccDao();
        dccBINDao = dccdb.dccBINDao();
        //txn
        TransactionDatabase txnDb = TransactionDatabase.getInstance(context);
        transactionDao = txnDb.tranDao();
        reversalDao = txnDb.reversalDao();
    }

    public final static class Builder {

        private Context mContext;

        /**
         * Set the Context used to instantiate the EpicAPosDbHandler
         *
         * @param context the application context
         * @return the {@link PosDevice.Builder} object.
         */
        public Builder setContext(final Context context) {
            mContext = context;
            return this;
        }

        /**
         * Initialize the EpicAPosDbHandler instance to used in the application.
         */
        public void build() {
            if (mContext == null) {
                throw new RuntimeException("Context not set, please set context before " +
                        "building the EpicAPosDbHandler instance.");
            }

            new DbHandler().init(mContext);
        }
    }
}
