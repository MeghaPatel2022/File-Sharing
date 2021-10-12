package com.bbot.copydata.xender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bbot.copydata.xender.Model.History;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    // Database Name
    public static final String DATABASE_NAME = "FileSharing.db";
    private static final int DATABASE_VERSION = 1;

    // Table_History_share TableColumns names
    private static final String _ID = "_Id";
    private static final String FILEPATH = "filepath";
    private static final String TYPE = "type";
    private static final String IS_SEND_RECEIVED = "isSend";

    // Send-Received History
    public static String TABLE_HISTORY = "Table_History_share";
    Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DATABASE_TABLE_HISTORY = "CREATE TABLE " + TABLE_HISTORY + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FILEPATH + " TEXT," +
                TYPE + " TEXT," +
                IS_SEND_RECEIVED + " INTEGER);";

        db.execSQL(CREATE_DATABASE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
    }

    // Methods For Table_Pills_Details_No
    public boolean insertHistoryData(String filepath, String type, int isSendReceived) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FILEPATH, filepath);
        values.put(TYPE, type);
        values.put(IS_SEND_RECEIVED, isSendReceived);

        long result = db.insert(TABLE_HISTORY, null, values);

        Log.e("LLLL_Data: Filepath: ", filepath + " Type: " + type + " isSenderReceiver: " + isSendReceived);

        db.close();
        return result != -1;
    }

    public ArrayList<History> getHistory() {

        ArrayList<History> histories = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_HISTORY, null);

        if (cur.getCount() > 0) {
            cur.moveToFirst();
            do {

                History history = new History();
                history.setFilePath(cur.getString(1));
                history.setType(cur.getString(2));
                history.setIsSendReceived(cur.getInt(3));

                histories.add(history);
            } while (cur.moveToNext());
            cur.close();
        }

        return histories;
    }

    public ArrayList<History> getSendReceived(String isSendReceived) {

        ArrayList<History> histories = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " WHERE " + IS_SEND_RECEIVED + " = ?", new String[]{isSendReceived});

        if (cur.getCount() > 0) {
            cur.moveToFirst();
            do {

                History history = new History();
                history.setFilePath(cur.getString(1));
                history.setType(cur.getString(2));
                history.setIsSendReceived(cur.getInt(3));

                histories.add(history);
            } while (cur.moveToNext());
            cur.close();
        }

        return histories;
    }

    // Delete all chats in the database
    public void deleteHistoryRecords(String path) {
        try {
            // Order of deletions is important when foreign key relationships exist.
            String deleteQuery = "DELETE FROM " + TABLE_HISTORY + " WHERE " + FILEPATH + "='" + path + "'";

            getWritableDatabase().execSQL(deleteQuery);
        } catch (Exception e) {
            Log.e("LLLLL_DB_DELETE", "deleteAllChats: " + e.getMessage());
        }
    }

}
