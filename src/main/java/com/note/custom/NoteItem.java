package com.note.custom;

import android.content.Context;

import com.note.R;
import com.note.utils.DateTimeUtil;

import java.util.Date;

/**
 *  用于创建便签对象
 */
public class NoteItem {

    public static final int MAX_NUM = 14;

    public int _id;// id
    public String content;// 内容
    public long date;// 日期
//    public boolean alarmEnable;// 是否设置了闹钟
    public int bgColor;// 背景颜色
    public boolean isFileFolder;// 是否为文件夹
    public int parentFolder;
    public String title;// 便签标题
    // 选择删除或者移除
    public boolean isSelected = false;
//    public RingtoneItem clockItem;

    public NoteItem(String content, long date, int bgColor, boolean isFileFolder, int parentFolder) {
        this.content = content;
        this.date = date;
//        this.alarmEnable = alarmEnable;
        this.bgColor = bgColor;
        this.isFileFolder = isFileFolder;
        this.parentFolder = parentFolder;

        this.title = "";

//        clockItem = new RingtoneItem();
    }

    public NoteItem(int parentFolder) {
        this("", new Date().getTime(), -1, false, parentFolder);
    }

    public NoteItem() {
        this(-1);
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public String getTitle() {
        if (title == null || title.equals("")) {
            return getContent();
        }
        return title;
    }

    public String getShortTitle() {
        if (title == null || title.equals("")) {
            return getShortContext();
        }
        return title;
    }

    public String getShortContext() {
        if (getContent().length() > MAX_NUM) {
            return getContent().substring(0, MAX_NUM - 1) + "...";
        } else {
            return getContent();
        }
    }

    public String getDate(Context context) {
        java.text.DateFormat dateFormat = DateTimeUtil.getDateFormat(context);
        String cDate = dateFormat.format(new java.util.Date(date));
        return cDate;
    }

    public String getTime(Context context) {
        java.text.DateFormat timeFormat = DateTimeUtil.getTimeFormat(context);
        String cTime = timeFormat.format(new java.util.Date(date));
        return cTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NoteItem other = (NoteItem) obj;
        if (_id != other._id)
            return false;
        return true;
    }

    public String getExprotTitle(Context context) {
        if (title == null || title.equals("")) {
            return context.getString(R.string.export_no_content);
        }
        return title;
    }

    public String getExprotContent(Context context) {
        if (content == null || content.equals("")) {
            return context.getString(R.string.export_no_content);
        }
        return content;
    }

    @Override
    public String toString() {
        return "NoteItem [_id=" + _id + ", content=" + content + "]";
    }

}
