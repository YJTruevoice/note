package com.note.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.note.custom.NoteDataManagerImpl;

import java.sql.SQLException;

/**
 * 数据库增删改查操作
 */
public class DBOperations extends ContentProvider {

    DBOpenHelper dbOpenHelper;
    SQLiteDatabase db;

    private static final int EVENT = 1;
    private static final int EVENT_ID = 2;

    // 匹配Uri(先初始化、再注册、最后在查询方法中匹配)，初始化
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // 注册需要的Uri
    static {
        uriMatcher.addURI("com.note", "items", EVENT);
        uriMatcher.addURI("com.note", "items/#", EVENT_ID);
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = DBOpenHelper.getInstance(getContext());
        db = dbOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();

        // 开始匹配Uri生成查询体
        int match = uriMatcher.match(uri);
        switch (match){
            case EVENT:
                sqb.setTables("items");
                break;
            case EVENT_ID:
                sqb.setTables("items");
                sqb.appendWhere("_id=");
                sqb.appendWhere(uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknow Uri" + uri);

        }
        return sqb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match){
            case EVENT:
                return "vnd.android.cursor.dir/items";
            case EVENT_ID:
                return "vnd.android.cursor.item/items";
            default:
                throw new IllegalArgumentException("Unknow Uri=" + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri newUri = null;
        SQLiteDatabase sdb = dbOpenHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case EVENT:
                long rowId = 0;
                try {
                    rowId = sdb.insert("items", DBOpenHelper.NOTE_CONTENT, contentValues);
                    if (rowId < 0) {
                        throw new SQLException("filed to insert row");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                newUri = ContentUris.withAppendedId(Uri.parse("content://com.note/items"), rowId);
                NoteDataManagerImpl noteDataManager = (NoteDataManagerImpl) NoteDataManagerImpl.getNoteDataManager(getContext());
                noteDataManager.updateCacheFromDB();
                break;
        }
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (uriMatcher.match(uri)) {
            case EVENT:
                count = db.delete("items", selection, selectionArgs);
                break;
            case EVENT_ID:
                String segment = uri.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(selection)) {
                    selection = "_id=" + segment;
                } else {
                    selection = "_id=" + segment + " AND (" + selection + ")";
                }
                count = db.delete("items", selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int count;
        long rowId = 0;
        int match = uriMatcher.match(uri);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        switch (match) {
            case EVENT_ID: {
                String segment = uri.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("items", contentValues, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Cannot update URL: " + uri);
            }
        }
        return count;
    }
}
