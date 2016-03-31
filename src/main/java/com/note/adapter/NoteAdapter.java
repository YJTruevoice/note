package com.note.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.note.R;
import com.note.custom.NoteItemViewHolder;
import com.note.custom.NoteDataManagerImpl;
import com.note.custom.NoteItem;

import java.util.List;

/**
 * 便签listview适配器
 * Created by user on 2015/8/12.
 */
public class NoteAdapter extends BaseAdapter {

    private Context mContext;
    private List<NoteItem> mItems = null;
    private LayoutInflater mInflater;
    private int showType;

    public static final int SHOW_NORMAL = 0;
    public static final int SHOW_DELETE = 1;
    public static final int SHOW_MOVETOFOLDER = 2;

    public NoteAdapter(List<NoteItem> items, Context context){
        mInflater = LayoutInflater.from(context);
        this.mItems = items;
        this.mContext = context;
        setAllItemChecked(false);
        showType = SHOW_NORMAL;
    }

    private void setAllItemChecked(boolean isChecked) {
        for (int i = 0; i < getCount(); i ++){
            mItems.get(i).isSelected = isChecked;
        }
    }


    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public NoteItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NoteItemViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new NoteItemViewHolder();
            convertView = mInflater.inflate(R.layout.note_item_layout, null);
            viewHolder.mLinearLayout = (ViewGroup) convertView.findViewById(R.id.noteList_item_layout);
            viewHolder.mLeftTv = (TextView) convertView.findViewById(R.id.tv_left);
            viewHolder.mRightTv = (TextView) convertView.findViewById(R.id.tv_right);
            viewHolder.mCheck = (CheckBox) convertView.findViewById(R.id.check);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (NoteItemViewHolder) convertView.getTag();
        }

        if (position >= mItems.size())
            position = mItems.size() - 1;
        Log.i("noteitem", position + "");
        NoteItem noteItem = mItems.get(position);
        Log.i("noteitem", noteItem.toString());
        viewHolder.mCheck.setChecked(noteItem.isSelected);
        if (showType == SHOW_NORMAL){
            viewHolder.mRightTv.setVisibility(View.VISIBLE);
            viewHolder.mCheck.setVisibility(View.GONE);
        } else if (showType == SHOW_DELETE){
            viewHolder.mRightTv.setVisibility(View.GONE);
            viewHolder.mCheck.setVisibility(View.VISIBLE);
        } else if (showType == SHOW_MOVETOFOLDER){
            if (noteItem.isFileFolder){
                viewHolder.mCheck.setVisibility(View.GONE);
            } else {
                viewHolder.mRightTv.setVisibility(View.GONE);
                viewHolder.mCheck.setVisibility(View.VISIBLE);
            }
        }

        String noteContent = noteItem.content;
        if (!noteItem.isFileFolder){// 不是文件夹
            viewHolder.mLinearLayout.setBackgroundResource(R.mipmap.item_light_blue);
            if (noteItem.getTitle() == null){
                viewHolder.mLeftTv.setText(noteItem.getContent().toString());
            } else {
                viewHolder.mLeftTv.setText(noteItem.getTitle().toString());
            }
        } else {
            viewHolder.mLinearLayout.setBackgroundResource(R.mipmap.folder_background);
            int count = NoteDataManagerImpl.getNoteDataManager(mContext).getNotesFromFolder(noteItem._id).size();
            viewHolder.mLeftTv.setText(noteContent + "(" + count + ")");
        }
        viewHolder.mRightTv.setText(noteItem.getDate(mContext) + "\n" + noteItem.getTime(mContext));
        return convertView;
    }

    public void setAllItemCheckedAndNotify(boolean isChecked){
        setAllItemChecked(isChecked);
        notifyDataSetChanged();
    }

    public void toggleChecked(int position) {
        getItem(position).isSelected = !getItem(position).isSelected;

    }

    public void setItemChecked(int position, boolean checked) {
        getItem(position).isSelected = checked;
    }

    public int getSelectedCount() {
        int count = 0;
        for (NoteItem item : mItems) {
            if (item.isSelected) {
                count++;
            }
        }
        return count;
    }

    public void setShowType(int type) {
        showType = type;
        if (type == SHOW_DELETE || type == SHOW_MOVETOFOLDER) {
            setAllItemChecked(false);
        }
        notifyDataSetChanged();
    }

    public int getShowType() {
        return showType;
    }

    public int getFolderCount() {
        int count = 0;
        for (NoteItem item : mItems) {
            if (item.isFileFolder) {
                count++;
            }
        }
        return count;
    }

    public int getNotesCount() {
        int count = 0;
        for (NoteItem item : mItems) {
            if (!item.isFileFolder) {
                count++;
            }
        }
        return count;
    }

    public void setListItems(List<NoteItem> items) {
        this.mItems = items;
    }

}
