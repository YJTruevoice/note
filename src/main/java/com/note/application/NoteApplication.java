package com.note.application;

import android.app.Application;
import android.content.Context;

import com.note.custom.NoteDataManager;
import com.note.custom.NoteDataManagerImpl;

/**
 * 定義application管理全局變量
 * Created by user on 2015/8/12.
 */
public class NoteApplication extends Application {

    NoteDataManager mDataManager = null;// 便签数据管理对象

    public synchronized NoteDataManager getNoteDataManager(Context context){
        if (mDataManager == null){
            mDataManager = NoteDataManagerImpl.getNoteDataManager(context);
            mDataManager.initData(context);
        }
        return mDataManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
