package com.note.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 便签列表的操作
 * Created by user on 2015/8/11.
 */
public class NoteList {

    private List<NoteItem> noteList = new LinkedList<NoteItem>();

    /**
     * 排序
     */
    private void sort(){
        Collections.sort(noteList, NoteComparetor.comparetor);
    }

    public List<NoteItem> getList(){
        return noteList;
    }

    /**
     * 获取便签列表
     * @return
     */
    public List<NoteItem> getNotes(){
        ArrayList<NoteItem> arrayList = new ArrayList<NoteItem>();
        for (NoteItem noteItem: noteList){
            if (!noteItem.isFileFolder){
                arrayList.add(noteItem);
            }
        }
        return arrayList;
    }

    /**
     * 获取文件夹列表
     * @return
     */
    public List<NoteItem> getFolderList(){
        ArrayList<NoteItem> arrayList = new ArrayList<NoteItem>();
        for (NoteItem noteItem: noteList){
            if (noteItem.isFileFolder){
                arrayList.add(noteItem);
            }
        }
        return arrayList;
    }

    /**
     * 获取文件夹中的便签列表
     * @param folderID
     * @return
     */
    public List<NoteItem> getNoteFromFolder(int folderID){
        ArrayList<NoteItem> arrayList = new ArrayList<NoteItem>();
        for (NoteItem noteItem: noteList){
            if (noteItem.parentFolder == folderID){
                arrayList.add(noteItem);
            }
        }
        return arrayList;
    }

    /**
     * 获取根节点的文件夹和便签
     * @return
     */
    public List<NoteItem> getRootFolderAndNotes(){
        ArrayList<NoteItem> arrayList = new ArrayList<NoteItem>();
        for (NoteItem noteItem: noteList){
            if (noteItem.parentFolder == -1 || noteItem.isFileFolder){
                arrayList.add(noteItem);
            }
        }
        return arrayList;
    }

    /**
     * 获取根节点的便签
     * @return
     */
    public List<NoteItem> getRootNotes() {
        ArrayList<NoteItem> arrayList = new ArrayList<NoteItem>();
        for (NoteItem noteItem : noteList) {
            if (!noteItem.isFileFolder && noteItem.parentFolder == -1) {
                arrayList.add(noteItem);
            }
        }
        return arrayList;
    }

    /**
     * 根据ID获取便签列表
     * @param ID
     * @return
     */
    public NoteItem getNoteItemByID(int ID) {
        for (NoteItem noteItem : noteList) {
            if (noteItem._id == ID) {
                return noteItem;
            }
        }
        return null;
    }

    public List<NoteItem> getNotesIncludeContent(String content) {
        ArrayList<NoteItem> arrayList = new ArrayList<NoteItem>();
        for (NoteItem noteItem : noteList) {
            if (!noteItem.isFileFolder && noteItem.content != null) { // 如果是便签并且不为空
                if (noteItem.content.toLowerCase().contains(
                        content.toLowerCase())) { // include content
                    arrayList.add(noteItem);
                } else if (noteItem.title != null
                        && noteItem.title.toLowerCase().contains(
                        content.toLowerCase())) {
                    arrayList.add(noteItem);
                }
            }
        }
        return arrayList;
    }


    public void updateOneItem(NoteItem item) {
        noteList.remove(item);
        noteList.add(item);
        sort();
    }

    public void addOneItem(NoteItem item) {
        noteList.add(item);
        sort();
    }

    public void deleteNoteItemOrFolder(NoteItem item) {
        List<NoteItem> removeList = new ArrayList<NoteItem>();
        for (NoteItem noteItem : noteList) {
            if (noteItem.parentFolder == item._id || noteItem._id == item._id) {
                removeList.add(noteItem);
            }
        }
        noteList.removeAll(removeList);
    }

    public void clear() {
        noteList.clear();
    }

    public void add(NoteItem item) {
        noteList.add(item);
        sort();
    }

    public static class NoteComparetor implements Comparator<NoteItem>{

        public static final NoteComparetor comparetor = new NoteComparetor();

        @Override
        public int compare(NoteItem item1, NoteItem item2) {
            if (item1.isFileFolder != item2.isFileFolder){
                if (item1.isFileFolder){
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (item1.date > item2.date){
                    return -1;
                } else if (item1.date < item2.date){
                    return 1;
                } else {
                    if (item1._id > item2._id){
                        return -1;
                    } else if (item1._id < item2._id){
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }
}
