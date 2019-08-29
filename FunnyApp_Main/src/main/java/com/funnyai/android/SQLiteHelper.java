package com.funnyai.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //database values
    private static final String DATABASE_NAME      = "test.db";
    private static final int DATABASE_VERSION      = 3;

    //db table
    public static final String Table_Name       = "chat_log";

    public static final String DATABASE_CREATE_TABLE=
            "CREATE TABLE IF NOT EXISTS "+Table_Name+" " +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Msg_ID Int," +
                "Event VARCHAR," +
                 "Time VARCHAR," +
                "Type VARCHAR," +
                "SFrom VARCHAR," +
                "STo VARCHAR," +
                "Message VARCHAR) ";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + Table_Name);
            onCreate(db);
        }
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + Table_Name);
            onCreate(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DATABASE_CREATE_TABLE);
    }
}
