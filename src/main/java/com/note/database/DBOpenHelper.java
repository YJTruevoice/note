package com.note.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Notes1.db";
    private static final int DB_VERSION = 3;

    public static final String TABLE_NAME = "items";
    public static final String ID = "_id";
    public static final String NOTE_TITLE = "title";
    public static final String NOTE_CONTENT = "content";
    public static final String UPDATE_DATE = "udate";
    public static final String UPDATE_TIME = "utime";
    public static final String NOTE_BG_COLOR = "bgcolor";
    public static final String NOTE_IS_FOLDER = "isfilefolder";// 是否为文件夹(yes or no区分)
    public static final String NOTE_PARENT_FOLDER = "parentfile";// 如果不是文件夹,本字段存储其所属文件夹(varchar类型(存储被标记为文件夹的记录的_id字段的值))
    public static final String NOTE_UPDATE_DATE = "udate_long";

    public static final String[] NOTE_ALL_COLUMNS = new String[]{
            ID, NOTE_CONTENT, NOTE_BG_COLOR, NOTE_IS_FOLDER,
            NOTE_PARENT_FOLDER, NOTE_UPDATE_DATE, NOTE_TITLE
    };

    private static DBOpenHelper dbHelper = null;
    private DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBOpenHelper getInstance(Context context){
        if (dbHelper == null){
            dbHelper = new DBOpenHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(" CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOTE_CONTENT
                + " TEXT, " + NOTE_BG_COLOR + " INTEGER, " + NOTE_IS_FOLDER + " INTEGER, "
                + NOTE_PARENT_FOLDER + " VARCHAR, " + NOTE_UPDATE_DATE + " LONG, "
                + NOTE_TITLE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
        
    }
}
