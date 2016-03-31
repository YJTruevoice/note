package com.note.activty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.note.R;
import com.note.custom.NoteDataManager;
import com.note.custom.NoteItem;
import com.note.utils.IntentUtil;
import com.note.utils.StringUtil;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by user on 2015/8/12.
 */
public class NoteEditActivity extends BaseActivity implements View.OnClickListener{

    public static final String OPEN_TYPE = "open_type";
    public static final String ID = "id";
    public static final String NOTE_FOLDER_ID = "folder_id";

    public static final int TYPE_NEW_NOTE = 0;
    public static final int TYPE_NEW_FOLDER_NOTE = 1;
    public static final int TYPE_EDIT_NOTE = 2;
    public static final int TYPE_EDIT_FOLDER_NOTE = 3;

    private TextView titleTv, remainNum, dateAndTime;
    private EditText mContentEt;
    private NoteDataManager mDataManager;
    private NoteItem noteItem;

    // 菜单
    private static final int EDIT_TITLE = Menu.FIRST;
    private static final int SAVE_NOTE = Menu.FIRST + 1;
    private static final int SHARE_NOTE = Menu.FIRST + 2;
    private static final int SAVE_CANCEL = Menu.FIRST + 3;

    int noteDd;
    int folderID = -1;
    String mOldTitle = null;
    String mNewTitle = null;
    private int mOpenType;// 用于判断是新建便签还是更新便签
    private String mOldContent;// 数据库中原有的便签的内容
    private Intent mIntent;// 接受传递过来的Intent对象
    private int mId;// 被编辑的便签的ID

    int mMaxNum = 1000;// 便签输入限制的最大字数
    int num = 0;// 记录便签内容的大小
    private Boolean save_success;// 是否保存成功
    private boolean needSave = true;
    private Date date;// 日期
    private DateFormat dateFormat;// 日期形式
    private boolean is24Hours;// 是否为24小时制

    long mPreTimeStamp = -1;
    static String WHERE_TIME;
    String mPreDescription;
    private String note_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
        setContentView(R.layout.note_editor_activity_layout);

        initData();

        initViews();

    }

    /**
     * 初始化View
     */
    private void initViews() {

        titleTv = (TextView) findViewById(R.id.note_title);
        remainNum = (TextView) findViewById(R.id.remain_num);
        dateAndTime = (TextView) findViewById(R.id.tv_note_date_time);
        mContentEt = (EditText) findViewById(R.id.et_content);

        mContentEt.addTextChangedListener(textWatcher);
        findViewById(R.id.editor_image).setOnClickListener(this);
        titleTv.setOnClickListener(this);
    }

    // 监听编辑框内的文本变化
    TextWatcher textWatcher = new TextWatcher() {

        private CharSequence temp;
        private int selectionStart;
        private int selectionEnd;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int before) {
            temp = s;
        }

        @Override
        public void afterTextChanged(Editable s) {

            int number = s.length();
            remainNum.setText("" + number + "/" + mMaxNum);

            selectionStart = mContentEt.getSelectionStart();
            selectionEnd = mContentEt.getSelectionEnd();
            if(temp.length() > mMaxNum){
                Toast.makeText(NoteEditActivity.this, R.string.toast_edit_outbound, Toast.LENGTH_LONG).show();
                // 截断超出的内容
                int i = temp.length() - mMaxNum; // 超出多少字符
                s.delete(selectionStart - i, selectionEnd);
                int tempSelection = selectionEnd;
                mContentEt.setText(s);
                mContentEt.setSelection(tempSelection); // 设置光标在最后
            }

        }
    };

    /**
     * 初始化接收过来的数据
     */
    private void initData() {
        Intent intent = getIntent();
        mOpenType = intent.getIntExtra(OPEN_TYPE, -1);
        int noteID = intent.getIntExtra(ID, -1);
        folderID = intent.getIntExtra(NOTE_FOLDER_ID, -1);

        switch (mOpenType){
            case TYPE_EDIT_NOTE:
            case TYPE_EDIT_FOLDER_NOTE:
                noteItem = getDataManager(this).getNoteItem(noteID);
                mOldTitle = noteItem.getTitle();
                break;
            case TYPE_NEW_NOTE:
            case TYPE_NEW_FOLDER_NOTE:
                mOldContent = "";
                mOldTitle = getString(R.string.new_note);
                noteItem = new NoteItem(folderID);
                break;
            default:
                break;
        }
        num = noteItem.getContent().length();
        mOldContent = noteItem.content;
    }

    @Override
    protected void onResume() {
        if (noteItem != null){
            updateDisplay();
        }
        super.onResume();
    }

    private void updateDisplay() {
        remainNum.setText(num + "" + "/" + mMaxNum);
        dateAndTime.setText(noteItem.getTime(this) + "   " + noteItem.getDate(this));
        mContentEt.setText(noteItem.getContent());

        // 定位光标位置
        int selection_end = noteItem.getContent().toString().length();
        mContentEt.setSelection(selection_end);

        titleTv.setText(mOldTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, EDIT_TITLE, 1, R.string.edit_title).setIcon(R.mipmap.ic_menu_edit);
        menu.add(Menu.NONE, SAVE_NOTE, 1, R.string.save).setIcon(R.mipmap.save);
        menu.add(Menu.NONE, SHARE_NOTE, 1, R.string.share).setIcon(android.R.drawable.ic_menu_share);
        menu.add(Menu.NONE, SAVE_CANCEL, 1, R.string.cancel).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case EDIT_TITLE:
                editTitle();
                break;
            case SAVE_NOTE:
                saveNote();
                break;
            case SHARE_NOTE:
                if (noteItem != null){
                    saveNote();
                    IntentUtil.sendSharedIntent(this, noteItem);
                }
                break;
            case SAVE_CANCEL:
                showDialog(DIALOG_CANCEL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String content = StringUtil.deleteExtraSpace(mContentEt.getText().toString().trim());
        switch (mOpenType){
            case TYPE_NEW_NOTE:
            case TYPE_NEW_FOLDER_NOTE:
                createNote(content);
                break;
            case TYPE_EDIT_NOTE:
            case TYPE_EDIT_FOLDER_NOTE:
                updateNote(content);
                break;
            default:
                break;
        }
    }

    /**
     * 更新便签
     * @param content
     */
    private void updateNote(String content) {
        // 当前内容不等于以前内容且不为空，则更新便签
        if ((content.length() > 0 && !content.equals(mOldContent)) || isTitleUpdate()){
            if (noteItem == null){
                return;
            }
            updateTitle();
            noteItem.content = content;
            noteItem.date = new Date().getTime();
            getDataManager(this).updateItem(noteItem);
            mOldContent = content;
            Toast.makeText(NoteEditActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
            save_success = true;
        } else if (content.length() == 0){// 如果编辑的内容为空，提示保存失败！
            Toast.makeText(NoteEditActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            save_success = false;
        }

    }

    /**
     * 创建新便签
     * @param content
     */
    private void createNote(String content) {
        if (content.length() > 0 || isTitleUpdate()){// 不为空就插入记录
            if (noteItem == null)
                return;
            updateTitle();
            noteItem.content = content;
            getDataManager(this).insertItem(noteItem);
            mOldContent = content;
            mOpenType = TYPE_EDIT_NOTE;
            Toast.makeText(NoteEditActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
            save_success = true;
        } else {// 内容为空则取消保存
            Toast.makeText(NoteEditActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            save_success = false;
        }
    }

    /**
     * 更新便签标题
     */
    private void updateTitle() {
        switch (mOpenType){
            case TYPE_NEW_NOTE:
            case TYPE_NEW_FOLDER_NOTE:
                if (mNewTitle != null){
                    mOldTitle = mNewTitle;
                    noteItem.title = mNewTitle;
                }
                break;
            case TYPE_EDIT_NOTE:
            case TYPE_EDIT_FOLDER_NOTE:
                if (mNewTitle != null){
                    mOldTitle = mNewTitle;
                    noteItem.title = mNewTitle;
                }
                break;
        }
    }

    /**
     * 是否是在更新便签标题
     * @return
     */
    private boolean isTitleUpdate() {
        switch (mOpenType) {
            case TYPE_NEW_NOTE:
            case TYPE_NEW_FOLDER_NOTE:
                if (mNewTitle != null) {
                    return true;
                }
                break;
            case TYPE_EDIT_NOTE:
            case TYPE_EDIT_FOLDER_NOTE:
                if (mNewTitle != null && !mNewTitle.equals(mOldTitle)) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * 编辑便签标题
     */
    private void editTitle() {
        removeDialog(DIALOG_EDIT_TITLE);
        showDialog(DIALOG_EDIT_TITLE, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.editor_image:
            case R.id.note_title:
                editTitle();
                break;
            default:
                break;
        }

    }

    public static final int DIALOG_DELETE = 0;
    public static final int DIALOG_CANCEL = 1;
    public static final int DIALOG_EDIT_TITLE = 2;

    @Override
    protected Dialog onCreateDialog(int id, final Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.cancel, cancelDialog);
        switch (id) {
            case DIALOG_DELETE:
                builder.setTitle(R.string.delete_note);
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                needSave = false;
                                getDataManager(NoteEditActivity.this).deleteNoteItem(
                                        noteItem);
                                Toast.makeText(getApplicationContext(),
                                        R.string.delete_success, Toast.LENGTH_SHORT)
                                        .show();
                                NoteEditActivity.this.finish();
                            }
                        });
                break;

            case DIALOG_CANCEL:
                builder.setTitle(R.string.cancel);
                builder.setMessage(R.string.cancel_commit);
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                needSave = false;
                                NoteEditActivity.this.finish();
                            }
                        });
                break;

            case DIALOG_EDIT_TITLE:
                builder.setTitle(R.string.edit_title);
                View layout = LayoutInflater.from(this).inflate(
                        R.layout.dialog_layout_new_folder, null);
                builder.setView(layout);
                final EditText titleView = (EditText) layout
                        .findViewById(R.id.et_dialog_new_folder);

                String title = getNowTitle2Edit();
                if (null != title) {
                    titleView.setText(title);
                    titleView.setSelection(0, title.length());
                }
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String title = titleView.getText().toString()
                                        .trim();
                                titleView.setText("");
                                if (title.length() > 0) {
                                    mNewTitle = title;
                                    titleTv.setText(title);
                                } else { // 文件夾名稱为空时，保存失敗
                                    Toast.makeText(NoteEditActivity.this,
                                            R.string.title_len_is_zero,
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                break;
        }
        return builder.create();
    }

    private String getNowTitle2Edit() {
        String title = null;
        if (mNewTitle != null) {
            title = mNewTitle;
        } else if (mOpenType == TYPE_NEW_NOTE
                || mOpenType == TYPE_NEW_FOLDER_NOTE) {
            title = "";
        } else {
            title = mOldTitle;
        }
        return title;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (needSave){
            saveNote();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }
}
