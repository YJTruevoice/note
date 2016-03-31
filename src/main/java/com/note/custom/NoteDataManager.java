package com.note.custom;

import android.content.Context;

import java.util.List;

/**
 * 便签数据管理接口
 * Created by user on 2015/8/11.
 */
public interface NoteDataManager {

    public void initData(Context context);// 初始化数据

    public List<NoteItem> getFolderAndAllItems();// 获取所有文件夹和所有便签

    public NoteItem getNoteItem(int id);// 通过id获取通过id

    public NoteItem getNoteItemFromDB(int id);

    public int insertItem(NoteItem item);

    public int updateItem(NoteItem item);

    public List<NoteItem> getFolders();

    /**
     * 获取便签（不包括便签文件夹）
     */
    public List<NoteItem> getNotes();

    public List<NoteItem> getRootFoldersAndNotes();

    List<NoteItem> getRootNotes();

    public List<NoteItem> getNotesFromFolder(int folderID);

    public void deleteNoteItem(NoteItem item);

    void deleteAllNotes();

    List<NoteItem> getColckAlarmItems();

    List<NoteItem> getNotesIncludeContent(String content);

    List<NoteItem> getColckAlarmItemsFromDB();
}
