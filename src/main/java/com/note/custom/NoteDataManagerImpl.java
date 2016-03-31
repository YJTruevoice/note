package com.note.custom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.note.database.DBOpenHelper;

import java.util.List;

/**
 * 便签数据管理的具体实现
 * Created by user on 2015/8/11.
 */
public class NoteDataManagerImpl implements NoteDataManager {

    private NoteList mNoteList = new NoteList();
    private DBOpenHelper mDBHelper;
    private SQLiteDatabase sdb;
    private Context mContext;
    private volatile boolean mNeedUpdate = false;

    private static NoteDataManager mDataManager = null;

    /**
     * 私有构造方法
     * @param context
     */
    private NoteDataManagerImpl(Context context){
        mContext = context;
        mDBHelper = DBOpenHelper.getInstance(context);
        sdb = mDBHelper.getWritableDatabase();
    }

    /**
     * 通过单例模式提供便签数据管理对象
     * @param context
     * @return
     */
    public static NoteDataManager getNoteDataManager(Context context){
        if (mDataManager == null){
            mDataManager = new NoteDataManagerImpl(context);
        }
        return mDataManager;
    }


    @Override
    public void initData(Context context) {
        mNoteList.clear();
        Cursor cursor = null;
        try{
            String orderBy = DBOpenHelper.NOTE_IS_FOLDER + " desc , "
                    + DBOpenHelper.NOTE_UPDATE_DATE + " desc ";
            cursor = sdb.query(DBOpenHelper.TABLE_NAME, DBOpenHelper.NOTE_ALL_COLUMNS, null, null,
                    null, null, orderBy);
            while (cursor.moveToNext()){
                NoteItem noteItem = buildNoteItem(cursor, context);
                mNoteList.add(noteItem);
            }
        }finally {
            if (cursor != null){
                cursor.close();
                cursor = null;
            }
        }
        mNeedUpdate = false;
    }

    private NoteItem buildNoteItem(Cursor cursor, Context context) {

        NoteItem noteItem = new NoteItem();

        noteItem._id = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.ID));
        noteItem.title = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
        noteItem.content = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CONTENT));
        noteItem.date = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.NOTE_UPDATE_DATE));
        noteItem.parentFolder = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_PARENT_FOLDER));
        int isFileFolder = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_IS_FOLDER));
        if (isFileFolder == 1){
            noteItem.isFileFolder = true;
        } else if (isFileFolder == 0){
            noteItem.isFileFolder = false;
        }

//        int alarmEnable = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_ALARM_ENABLE));
//        if (alarmEnable == 1){
//            noteItem.alarmEnable = true;
//        } else {
//            noteItem.alarmEnable = false;
//        }

        return noteItem;
    }

    @Override
    public List<NoteItem> getFolderAndAllItems() {
        updateFromDB();
        return mNoteList.getList();
    }

    private void updateFromDB() {
        if (mNeedUpdate){
            initData(mContext);
        }
    }

    @Override
    public NoteItem getNoteItem(int id) {
        updateFromDB();
        NoteItem item = mNoteList.getNoteItemByID(id);
        if (item == null && id > 0){
            item = getNoteItemFromDB(id);
            initData(mContext);
        }
        return item;
    }

    @Override
    public NoteItem getNoteItemFromDB(int id) {
        Cursor cursor = null;
        NoteItem noteItem = null;
        try {
            cursor = sdb.query(DBOpenHelper.TABLE_NAME, DBOpenHelper.NOTE_ALL_COLUMNS, "_id=?",
                    new String[]{id + ""}, null, null, null);
            while (cursor.moveToNext()){
                noteItem = buildNoteItem(cursor, mContext);
            }
        }finally {
            if (cursor != null){
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    @Override
    public int insertItem(NoteItem item) {

        ContentValues cValues = buildValuesNoID(item);
        long l_id = sdb.insert(DBOpenHelper.TABLE_NAME, DBOpenHelper.NOTE_CONTENT, cValues);
        item._id = (int) l_id;
        mNoteList.addOneItem(item);
        return (int) l_id;
    }

    private ContentValues buildValuesNoID(NoteItem item) {
        ContentValues cValues = new ContentValues();
        cValues.put(DBOpenHelper.NOTE_CONTENT, item.content);
        cValues.put(DBOpenHelper.NOTE_IS_FOLDER, item.isFileFolder ? 1 : 0);
        cValues.put(DBOpenHelper.NOTE_BG_COLOR, item.bgColor);
        cValues.put(DBOpenHelper.NOTE_PARENT_FOLDER, item.parentFolder);
        cValues.put(DBOpenHelper.NOTE_UPDATE_DATE, item.date);
        cValues.put(DBOpenHelper.NOTE_TITLE, item.title == null ? "" : item.title);

        return cValues;
    }

    @Override
    public int updateItem(NoteItem item) {
        ContentValues contentValues = buildValuesNoID(item);
        long l_id = sdb.update(DBOpenHelper.TABLE_NAME,contentValues,DBOpenHelper.ID + "=?",
                new String[]{item._id + ""});
        mNoteList.updateOneItem(item);
        return (int) l_id;
    }

    @Override
    public List<NoteItem> getFolders() {
        updateFromDB();
        return mNoteList.getFolderList();
    }

    @Override
    public List<NoteItem> getNotes() {
        updateFromDB();
        return mNoteList.getNotes();
    }

    @Override
    public List<NoteItem> getRootFoldersAndNotes() {
        updateFromDB();
        return mNoteList.getRootFolderAndNotes();
    }

    @Override
    public List<NoteItem> getRootNotes() {
        updateFromDB();
        return mNoteList.getRootNotes();
    }

    @Override
    public List<NoteItem> getNotesFromFolder(int folderID) {
        updateFromDB();
        return mNoteList.getNoteFromFolder(folderID);
    }

    @Override
    public void deleteNoteItem(NoteItem item) {

        String sqlExecuteStr = "_id=? or " + DBOpenHelper.NOTE_PARENT_FOLDER + "=?";
        sdb.delete(DBOpenHelper.TABLE_NAME, sqlExecuteStr, new String[]{item._id + "",item._id + ""});

        // 此处可判断闹铃是否开启，若开启则一并取消删除

        mNoteList.deleteNoteItemOrFolder(item);
    }

    @Override
    public void deleteAllNotes() {
        sdb.delete(DBOpenHelper.TABLE_NAME, null, null);
        mNoteList.clear();
    }

    @Override
    public List<NoteItem> getColckAlarmItems() {
//        updateFromDB();
        return null;
    }

    @Override
    public List<NoteItem> getNotesIncludeContent(String content) {
        return null;
    }

    @Override
    public List<NoteItem> getColckAlarmItemsFromDB() {
        return null;
    }

    public void updateCacheFromDB() {
        mNeedUpdate = true;
    }
}
