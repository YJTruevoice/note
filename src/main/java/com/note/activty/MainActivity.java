package com.note.activty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.note.R;
import com.note.adapter.NoteAdapter;
import com.note.custom.NoteItemViewHolder;
import com.note.custom.NoteDataManager;
import com.note.custom.NoteItem;
import com.note.utils.IntentUtil;
import com.note.utils.SDCardUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {

    private NoteAdapter noteAdapter;

    private TextView noteTitle;
    private ImageView creatNewNoteBar;// 标题栏的新建便签按钮
    private ListView noteList;
    private LinearLayout deleteSoftKey;
    private LinearLayout move2folderSoftKey;

    private Button deleteSelected, selectAllDel, selectCancelDel, cancelDel;
    private Button moveSelected, selectAllMov, selectCancelMov, cancelMov;

    public static final int MAX_NOTES = 1000;// 最大便签数

    public static final int MAX_FOLDERS = 1000;// 最大文件夹数

    public static final int MAX_FOLDERNAMENUM = 15;// 文件夹名输入限制的最大字数
    // 菜单
    protected static final int MENU_NEW_NOTE = Menu.FIRST;// 新建一个便签
    protected static final int MENU_NEW_FOLDER = Menu.FIRST + 1;// 新建一个文件夹
    protected static final int MENU_DELETE = Menu.FIRST + 2;// 删除
    protected static final int MENU_DELETE_ALL = Menu.FIRST + 5;// 删除所有
    protected static final int MENU_UPDATE_FOLDER = Menu.FIRST + 6;// 更新文件夹
    protected static final int MENU_MOVEOUTFOLDER = Menu.FIRST + 7;// 移除文件夹
    protected static final int MENU_EXPORT = Menu.FIRST + 8;// 导出

    protected static final int MENU_SHARE = Menu.FIRST + 9;// 分享

    protected static final int MENU_MOVETOFOLDER = Menu.FIRST + 3;// 移进文件夹
    //    protected static final int Menu_setAlarm = Menu.FIRST + 4;// 设置便签铃声
    protected static final int MENU_RENAME_FOLDER = Menu.FIRST + 4;// 重命名文件夹

    public static final int FOLDER_NO = -1;// 无文件夹

    int mFolderID = FOLDER_NO;// 根目录标志，
    protected List<NoteItem> mItems = null;
    private long exitTime = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    /**
     * 初始化View
     */
    private void initViews() {

        noteList = (ListView) findViewById(R.id.note_list);
        noteList.setOnItemClickListener(this);
        noteList.setOnCreateContextMenuListener(this);

        noteTitle = (TextView) findViewById(R.id.note_title_tv);
        creatNewNoteBar = (ImageView) findViewById(R.id.new_note_bar);
        creatNewNoteBar.setOnClickListener(this);

        deleteSoftKey = (LinearLayout) findViewById(R.id.delete_softkey);
        deleteSoftKey.setVisibility(View.GONE);
        move2folderSoftKey = (LinearLayout) findViewById(R.id.move2folder_softkey);
        move2folderSoftKey.setVisibility(View.GONE);

        deleteSelected = (Button) findViewById(R.id.delete_note_selected);
        selectAllDel = (Button) findViewById(R.id.selecte_note_all_del);
        selectCancelDel = (Button) findViewById(R.id.cancel_select_del);
        cancelDel = (Button) findViewById(R.id.cancel_del);
        deleteSelected.setOnClickListener(this);
        selectAllDel.setOnClickListener(this);
        selectCancelDel.setOnClickListener(this);
        cancelDel.setOnClickListener(this);

        moveSelected = (Button) findViewById(R.id.move_note_2folder);
        selectAllMov = (Button) findViewById(R.id.selecte_note_all_mov);
        selectCancelMov = (Button) findViewById(R.id.cancel_select_mov);
        cancelMov = (Button) findViewById(R.id.cancel_mov);
        moveSelected.setOnClickListener(this);
        selectAllMov.setOnClickListener(this);
        selectCancelMov.setOnClickListener(this);
        cancelMov.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }

    // 负责更新ListView中的数据
    protected void updateDisplay() {
        if (mFolderID == FOLDER_NO) { // 根目录
            mItems = getDataManager(this).getRootFoldersAndNotes();// 获取文件夹和便签
            if (noteAdapter == null) {
                noteAdapter = new NoteAdapter(mItems, this);
                noteList.setAdapter(noteAdapter);
            } else {
                noteAdapter.setListItems(mItems);
                noteAdapter.notifyDataSetChanged();
            }
            noteTitle.setText(getString(R.string.mynote));
        } else { // 在文件夹中
            mItems = getDataManager(this).getNotesFromFolder(mFolderID);
            noteAdapter.setListItems(mItems);
            noteAdapter.notifyDataSetChanged();
            noteTitle.setText(getDataManager(this).getNoteItem(mFolderID).content);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_NEW_NOTE, 1, R.string.new_note).setIcon(R.mipmap.new_note);
        menu.add(Menu.NONE, MENU_NEW_FOLDER, 1, R.string.new_folder).setIcon(R.mipmap.new_folder);
        menu.add(Menu.NONE, MENU_DELETE, 1, R.string.delete).setIcon(R.mipmap.delete);
        menu.add(Menu.NONE, MENU_DELETE_ALL, 1, R.string.delete_all).setIcon(R.mipmap.delete);
        menu.add(Menu.NONE, MENU_MOVETOFOLDER, 1, R.string.movetoFolder).setIcon(R.mipmap.menu_move);
        menu.add(Menu.NONE, MENU_MOVEOUTFOLDER, 1, R.string.moveoutFolder).setIcon(R.mipmap.menu_move);
        menu.add(Menu.NONE, MENU_EXPORT, 1, R.string.menu_export).setIcon(R.mipmap.ic_menu_goto);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case MENU_NEW_NOTE:
                toCreateNewNote();
                break;
            case MENU_NEW_FOLDER:
                newFolder();
                break;
            case MENU_DELETE:
                noteAdapter.setShowType(NoteAdapter.SHOW_DELETE);
                deleteSoftKey.setVisibility(View.VISIBLE);
                break;
            case MENU_DELETE_ALL:
                showDialog(DIALOG_DELETE_ALL_NOTES, null);
                break;
            case MENU_MOVETOFOLDER:
                noteAdapter.setShowType(NoteAdapter.SHOW_MOVETOFOLDER);
                moveSelected.setText(R.string.movetoFolder);
                move2folderSoftKey.setVisibility(View.VISIBLE);
                break;
            case MENU_MOVEOUTFOLDER:
                noteAdapter.setShowType(NoteAdapter.SHOW_MOVETOFOLDER);
                moveSelected.setText(R.string.moveoutFolder);
                move2folderSoftKey.setVisibility(View.VISIBLE);
                break;
            case MENU_EXPORT:
                exportAllNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 导出所有便签
     */
    private void exportAllNote() {
        // 先拍判断SDCard是否存在
        if (!SDCardUtil.isSDCardExists()) {
            Toast.makeText(this, R.string.sdcard_is_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        File filePath = null;
        try {
            filePath = SDCardUtil.saveFilePath(this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.can_not_make_file, Toast.LENGTH_SHORT).show();
        }
        List<NoteItem> notes = getDataManager(this).getNotes();
        ExportTask task = new ExportTask(this, filePath, notes.size());
        task.execute(notes);
    }

    /**
     * 新建文件夹
     */
    private void newFolder() {
        if (getDataManager(this).getFolderAndAllItems().size() > MAX_FOLDERS) {
            Toast.makeText(this, R.string.toast_add_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        showDialog(DIALOG_NEW_FOLDER, null);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.new_note_bar:
                toCreateNewNote();
                break;
            case R.id.delete_note_selected:
                showDialog(DIALOG_DELETE_SOME_NOTES, null);
                break;
            case R.id.selecte_note_all_del:
                noteAdapter.setAllItemCheckedAndNotify(true);
                break;
            case R.id.cancel_select_del:
                noteAdapter.setAllItemCheckedAndNotify(false);
                break;
            case R.id.cancel_del:
                noteAdapter.setShowType(NoteAdapter.SHOW_NORMAL);
                deleteSoftKey.setVisibility(View.GONE);
                updateDisplay();
                break;

            case R.id.move_note_2folder:
                if (mFolderID == FOLDER_NO) {
                    moveToFolder();
                } else {
                    moveOutFolder();
                }
                break;
            case R.id.selecte_note_all_mov:
                noteAdapter.setAllItemCheckedAndNotify(true);
                break;
            case R.id.cancel_select_mov:
                noteAdapter.setAllItemCheckedAndNotify(false);
                break;
            case R.id.cancel_mov:
                noteAdapter.setShowType(NoteAdapter.SHOW_NORMAL);
                move2folderSoftKey.setVisibility(View.GONE);
                updateDisplay();
                break;
        }

    }

    /**
     * 新建便签
     */
    private void toCreateNewNote() {
        if (noteAdapter != null && noteAdapter.getCount() > MAX_NOTES) {
            Toast.makeText(this, R.string.toast_add_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, NoteEditActivity.class);
        if (mFolderID != FOLDER_NO) {
            intent.putExtra(NoteEditActivity.OPEN_TYPE, NoteEditActivity.TYPE_NEW_FOLDER_NOTE);
            intent.putExtra(NoteEditActivity.NOTE_FOLDER_ID, mFolderID);
        } else {
            intent.putExtra(NoteEditActivity.OPEN_TYPE, NoteEditActivity.TYPE_NEW_NOTE);
        }
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // 判断是否是checkBox状态
        if (noteAdapter.getShowType() == NoteAdapter.SHOW_DELETE
                || noteAdapter.getShowType() == NoteAdapter.SHOW_MOVETOFOLDER) {
            NoteItemViewHolder viewHolder = (NoteItemViewHolder) view.getTag();
            viewHolder.mCheck.toggle();
            NoteAdapter adapter2 = (NoteAdapter) parent.getAdapter();
            adapter2.toggleChecked(position);
            return;
        }

        NoteItem item = mItems.get(position);
        if (item.isFileFolder) {
            mFolderID = item._id;
            updateDisplay();
        } else {
            Intent intent = new Intent();
            intent.setClass(this, NoteEditActivity.class);
            intent.putExtra(NoteEditActivity.ID, item._id);
            intent.putExtra(NoteEditActivity.OPEN_TYPE, NoteEditActivity.TYPE_EDIT_NOTE);
            startActivity(intent);
        }

    }

    // 观察输入的文件名长度
    TextWatcher textwatch = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() > MAX_FOLDERNAMENUM) {
                s.delete(MAX_FOLDERNAMENUM, s.length());
            }
        }
    };

    // 文件夹同名处理函数
    private void insertFolder(String folderName) {
        List<NoteItem> folders = getDataManager(this).getFolders();
        for (NoteItem noteItem : folders) {
            if (noteItem.content.equals(folderName)) {
                Toast.makeText(MainActivity.this,
                        R.string.Thisfolderalreadyexists, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }
        NoteItem item = new NoteItem(folderName, new Date().getTime(), -1, true, -1);
        getDataManager(this).insertItem(item);
    }

    public static final int DIALOG_DELETE = 0;
    public static final int DIALOG_NEW_FOLDER = 1;
    public static final int DIALOG_DELETE_SOME_NOTES = 2;
    public static final int DIALOG_DELETE_ALL_NOTES = 3;
    public static final int DIALOG_MOVE_FOLDER = 4;
    public static final int DIALOG_RENAME_FOLDER = 5;

    int selectedItemID = -1;
    public static final String DIALOG_KEY_1 = "key1";

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_DELETE:
                builder.setNegativeButton(R.string.cancel, cancelDialog);
                builder.setTitle(R.string.delete_note);
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final NoteItem noteItem = getDataManager(
                                        MainActivity.this).getNoteItem(
                                        selectedItemID);
                                getDataManager(MainActivity.this).deleteNoteItem(
                                        noteItem);

                                Toast.makeText(getApplicationContext(),
                                        R.string.delete_success, Toast.LENGTH_SHORT).show();
                                updateDisplay();
                            }
                        });
                break;

            case DIALOG_NEW_FOLDER:
                builder.setNegativeButton(R.string.cancel, cancelDialog);
                builder.setTitle(R.string.new_folder);
                View layout = LayoutInflater.from(this).inflate(
                        R.layout.dialog_layout_new_folder, null);
                builder.setView(layout);
                // 实例化AlertDialog中的EditText对象
                final EditText newFolderEditText = (EditText) layout
                        .findViewById(R.id.et_dialog_new_folder);
                // 对EditText输入内容监听，如果超过最大字符数限制，则弹出提示
                newFolderEditText.addTextChangedListener(textwatch);
                // 设置一个确定的按钮
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newFolderName = newFolderEditText.getText()
                                        .toString().trim();
                                newFolderEditText.setText("");

                                if (newFolderName.length() > 0) {
                                    insertFolder(newFolderName);
                                    MainActivity.this.updateDisplay();
                                    IntentUtil.keepDialog(dialog, true);
                                } else { // 文件夾名稱为空时，保存失敗
                                    Toast.makeText(MainActivity.this,
                                            R.string.folder_add_fail, Toast.LENGTH_SHORT).show();
                                    IntentUtil.keepDialog(dialog, false);
                                }

                            }
                        });
                break;

            case DIALOG_DELETE_SOME_NOTES:
                builder.setNegativeButton(R.string.cancel, cancelDialog);
                builder.setTitle(R.string.delete_note);
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int selectedCount = 0;
                                for (int i = 0; i < noteAdapter.getCount(); i++) {
                                    if (noteAdapter.getItem(i).isSelected) {
                                        selectedCount++;
                                        NoteItem item = mItems.get(i);
                                        getDataManager(MainActivity.this)
                                                .deleteNoteItem(item);
                                    }
                                }

                                updateDisplay();
                                if (selectedCount > 0) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.delete_success,
                                            Toast.LENGTH_LONG).show();
                                    deleteSoftKey.setVisibility(View.GONE);
                                    noteAdapter.setShowType(NoteAdapter.SHOW_NORMAL);
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.selected_is_empty,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }).show();
                break;
            case DIALOG_DELETE_ALL_NOTES:
                builder.setNegativeButton(R.string.cancel, cancelDialog);
                builder.setTitle(R.string.delete_all_note);
                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(mDeleteThread).start();
                            }
                        });
                break;
            case DIALOG_MOVE_FOLDER:
                builder.setTitle(R.string.movetoFolder);
                String[] folderTitle = args.getStringArray(DIALOG_KEY_1);
                builder.setItems(folderTitle,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NoteDataManager dataManager = getDataManager(MainActivity.this);
                                if (dataManager.getFolders().size() == 0) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.listis_empty,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (noteAdapter.getSelectedCount() == 0) { // select is
                                    // empty.
                                    Toast.makeText(getApplicationContext(),
                                            R.string.pleasechoosenote,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                NoteItem folderItem = dataManager.getFolders().get(
                                        which);
                                for (NoteItem item : mItems) {
                                    if (item.isSelected) {
                                        item.parentFolder = folderItem._id;
                                        item.isSelected = false;
                                        dataManager.updateItem(item);
                                    }
                                }
                                move2folderSoftKey.setVisibility(View.GONE);
                                noteAdapter.setShowType(NoteAdapter.SHOW_NORMAL);
                                updateDisplay();
                            }
                        });
                break;
            case DIALOG_RENAME_FOLDER:
                builder.setNegativeButton(R.string.cancel, cancelDialog);
                builder.setTitle(R.string.edit_folder_title);
                View renameLayout = LayoutInflater
                        .from(this)
                        .inflate(
                                R.layout.dialog_layout_new_folder,
                                (ViewGroup) findViewById(R.id.dialog_layout_new_folder_root));
                builder.setView(renameLayout);

                final EditText mFolderEditText = (EditText) renameLayout
                        .findViewById(R.id.et_dialog_new_folder);

                final NoteItem noteItem = getDataManager(this).getNoteItem(
                        selectedItemID);
                mFolderEditText.setText(noteItem.content);
                mFolderEditText.setSelection(noteItem.content.length());
                mFolderEditText.addTextChangedListener(textwatch);

                builder.setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newFolderName = mFolderEditText.getText()
                                        .toString();
                                // 新文件夹名称不为空,且不等于原有的名称,则更新
                                if (newFolderName.length() == 0
                                        || newFolderName.equals(noteItem.content)) {
                                    Toast.makeText(MainActivity.this,
                                            R.string.folder_add_fail,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                List<NoteItem> folders = getDataManager(
                                        MainActivity.this).getFolders();
                                for (NoteItem item : folders) {
                                    if (newFolderName.equals(item.content)) {
                                        Toast.makeText(MainActivity.this,
                                                R.string.Thisfolderalreadyexists,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                noteItem.content = newFolderName;
                                getDataManager(MainActivity.this).updateItem(
                                        noteItem);
                                updateDisplay();
                            }
                        });

            default:
                break;
        }
        return builder.create();
    }

    Handler mHandler = new Handler();
    ProgressDialog mProgressDialog = null;// 窗口进度条
    Runnable mDeleting = new Runnable() {
        @Override
        public void run() {
            String delete = MainActivity.this.getString(R.string.delete);
            String deleting = MainActivity.this.getString(R.string.Deleting);
            mProgressDialog = ProgressDialog.show(MainActivity.this, delete,
                    deleting);
        }
    };

    Runnable mFinishDelete = new Runnable() {
        @Override
        public void run() {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                // 删除成功，更新UI
                updateDisplay();
            }
        }

    };

    Runnable mDeleteThread = new Runnable() {
        // 此为非ui线程，不能处理ui操作。
        @Override
        public void run() {
            mHandler.removeCallbacks(mDeleting);
            mHandler.post(mDeleting);

            List<NoteItem> items = getDataManager(MainActivity.this).getNotesFromFolder(mFolderID);
            for (NoteItem item : items) {
                getDataManager(MainActivity.this).deleteNoteItem(item);
            }
            mHandler.removeCallbacks(mFinishDelete);
            mHandler.post(mFinishDelete);
            mHandler.post(mToast_delete_success);
        }

    };

    Runnable mToast_delete_success = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), R.string.delete_success,
                    Toast.LENGTH_LONG).show();
        }
    };

    Runnable mToast_list_is_empty = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), R.string.list_is_empty,
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo acMenuinfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        NoteItem contextItem = mItems.get(acMenuinfo.position);
        selectedItemID = contextItem._id;
        menu.add(0, MENU_DELETE, 0, R.string.delete);
        menu.setHeaderTitle(contextItem.getShortTitle());
        if (contextItem.isFileFolder) {
            menu.add(0, MENU_RENAME_FOLDER, 1, R.string.edit_folder_title);
            return;
        }
        if (mFolderID == FOLDER_NO) {
            menu.add(0, MENU_MOVETOFOLDER, 2, R.string.movetoFolder);
        } else {
            menu.add(0, MENU_MOVEOUTFOLDER, 2, R.string.moveoutFolder);
        }

        menu.add(0, MENU_SHARE, 3, R.string.share);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case MENU_DELETE:
                showDialog(DIALOG_DELETE, null);
                return true;
            case MENU_RENAME_FOLDER:
                renameFolder();
                return true;
            case MENU_MOVETOFOLDER:
                noteAdapter.setItemChecked(menuInfo.position, true);
                moveToFolder();
                return true;
            case MENU_MOVEOUTFOLDER:
                noteAdapter.setItemChecked(menuInfo.position, true);
                moveOutFolder();
                return true;
            case MENU_SHARE:
                NoteItem noteItem = mItems.get(menuInfo.position);
                IntentUtil.sendSharedIntent(this, noteItem);
                return true;
        }
        return false;
    }

    /**
     * 移出文件夹
     */
    private void moveOutFolder() {
        NoteDataManager noteDataManager = getDataManager(this);
        if (noteAdapter.getSelectedCount() == 0) {
            Toast.makeText(getApplicationContext(), R.string.pleasechoosenote, Toast.LENGTH_LONG).show();
        }
        for (NoteItem item : mItems) {
            if (item.isSelected) {
                item.parentFolder = -1;
                item.isFileFolder = false;
                noteDataManager.updateItem(item);
            }
        }
        move2folderSoftKey.setVisibility(View.GONE);
        noteAdapter.setShowType(NoteAdapter.SHOW_NORMAL);
        updateDisplay();
    }

    /**
     * 移进文件夹
     */
    private void moveToFolder() {
        List<NoteItem> folders = getDataManager(this).getFolders();
        if (folders.size() == 0) {
            Toast.makeText(getApplicationContext(), R.string.no_folder_found, Toast.LENGTH_LONG).show();
            return;
        }
        String[] folderNameStr = new String[folders.size()];
        for (int i = 0; i < folders.size(); i++) {
            folderNameStr[i] = folders.get(i).content;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArray(DIALOG_KEY_1, folderNameStr);
        removeDialog(DIALOG_MOVE_FOLDER);
        showDialog(DIALOG_MOVE_FOLDER, bundle);
    }

    /**
     * 重命名文件夹名
     */
    private void renameFolder() {
        removeDialog(DIALOG_RENAME_FOLDER);
        showDialog(DIALOG_RENAME_FOLDER, null);
    }

    class ExportTask extends AsyncTask<List<NoteItem>, Integer, Boolean> {
        ProgressDialog progressDialog = null;
        private Context context;
        private File filePath;
        private int maxSize;
        String content;
        String title;
        String time;

        public ExportTask(Context context, File filePath, int size) {
            this.context = context;
            this.filePath = filePath;
            this.maxSize = size;

            iniString();
            initDialog(context, maxSize);
        }

        private void iniString() {
            time = context.getString(R.string.export_time);
            title = context.getString(R.string.export_title);
            content = context.getString(R.string.export_content);
        }

        private void initDialog(Context context, int maxSize) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(R.string.exporting);
            progressDialog.setMax(maxSize);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NoteItem>... params) {
            boolean success = true;

            List<NoteItem> notes = params[0];
            List<String> values = getNoteString(notes);
            for (int i = 0; i < values.size(); i++) {
                try {
                    SDCardUtil.saveFileWithAppendMode(filePath, values.get(i));
                    publishProgress(i);
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
            return success;
        }

        private List<String> getNoteString(List<NoteItem> notes) {
            StringBuffer sb = new StringBuffer();
            List<String> noteStr = new ArrayList<String>();
            for (int i = 0; i < notes.size(); i++) {
                NoteItem noteItem = notes.get(i);
                sb.append("--------------第" + (i + 1) + "个便签-------------").append("\n");
                sb.append(title).append(noteItem.getExprotTitle(context)).append("\n");
                sb.append(time).append(noteItem.getDate(context) + "  " + noteItem.getTime(context)).append("\n");
                sb.append(content).append(noteItem.getExprotContent(context)).append("\n\n");
            }
            noteStr.add(sb.toString());
            return noteStr;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            Integer integer = values[0];
            progressDialog.setProgress(integer);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (result) {
                String resultStr = context.getString(R.string.export_sucess_log, maxSize + "", filePath.toString());
                Toast.makeText(context, resultStr, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, R.string.export_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void finish() {
        if (noteAdapter.getShowType() == NoteAdapter.SHOW_DELETE || noteAdapter.getShowType() == NoteAdapter.SHOW_MOVETOFOLDER) {
            noteAdapter.setShowType(NoteAdapter.SHOW_NORMAL);
            deleteSoftKey.setVisibility(View.GONE);
            move2folderSoftKey.setVisibility(View.GONE);
            noteAdapter.notifyDataSetChanged();
            return;
        }
        if (mFolderID != FOLDER_NO) {
            mFolderID = FOLDER_NO;
            updateDisplay();
            return;
        }
        super.finish();
    }
}
