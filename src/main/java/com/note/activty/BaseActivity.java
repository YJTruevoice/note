package com.note.activty;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.note.application.NoteApplication;
import com.note.custom.NoteDataManager;
import com.note.utils.IntentUtil;

/**
 * Created by user on 2015/8/12.
 */
public class BaseActivity extends Activity {

    /**
     * 获取便签管理对象
     * @param context
     * @return
     */
    public NoteDataManager getDataManager(Context context){
        NoteApplication na = new NoteApplication();
        return na.getNoteDataManager(context);
    }

    protected DialogInterface.OnClickListener cancelDialog = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            IntentUtil.keepDialog(dialog, true);
            dialog.dismiss();
        }
    };
}
