package com.epic.pos.data.cp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.epic.pos.util.AppLog;

import com.epic.pos.config.MyApp;
import com.epic.pos.data.datasource.SharedPref;
import com.epic.pos.data.db.dbtxn.TransactionDatabase;

public class AppDataProvider extends ContentProvider {

    private final String TAG = AppDataProvider.class.getSimpleName();
    private TransactionDatabase transactionDatabase;

    private final String TXN_COUNT = "txn_count";
    private final String APP_IS_RUNNING = "app_is_running";
    private final String TXN_DATA = "txn_data";


    public AppDataProvider() {
    }

    @Override
    public boolean onCreate() {
        log("onCreate()");
        transactionDatabase = TransactionDatabase.getInstance(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (uri.toString().endsWith(TXN_COUNT)) {
            int count = transactionDatabase.tranDao().getTransactionCount();
            MatrixCursor cursor = new MatrixCursor(new String[]{TXN_COUNT});
            cursor.newRow().add(TXN_COUNT, count);
            return cursor;
        } else if (uri.toString().endsWith(APP_IS_RUNNING)) {
            MatrixCursor cursor = new MatrixCursor(new String[]{APP_IS_RUNNING});
            cursor.newRow().add(APP_IS_RUNNING, MyApp.getInstance().isAppRunning() ? 1 : 0);
            return cursor;
        } else if (uri.toString().endsWith(TXN_DATA)) {
            //List<Transaction> transactions = transactionDatabase.tranDao().getAll();
            //MatrixCursor cursor = new MatrixCursor(new String[]{TXN_COUNT});
            //cursor.newRow().add(TXN_DATA, "testdata");
            //return cursor;
            return null;
        } else {
            return null;
        }
    }


    private SharedPref prefs() {
        SharedPreferences sharedPref = getContext().getSharedPreferences(SharedPref.PREFS_NAME, Context.MODE_PRIVATE);
        return new SharedPref(sharedPref, getContext());
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void log(String msg) {
        AppLog.i(TAG, msg);
    }
}