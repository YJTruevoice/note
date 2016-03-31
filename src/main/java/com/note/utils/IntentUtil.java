package com.note.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.note.R;
import com.note.custom.NoteItem;

import java.lang.reflect.Field;

/**
 * intent管理工具
 */
public class IntentUtil {

    public static final void sendSharedIntent(Context context, NoteItem item){

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        String titleKey = context.getString(R.string.export_title);
        String contentKey = context.getString(R.string.export_content);

        StringBuilder sb = new StringBuilder();
        sb.append(titleKey).append(item.getExprotTitle(context)).append(",");
        sb.append(contentKey).append(item.getExprotContent(context));

        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share));
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share) + ":" + item.getShortTitle()));

    }

    public static void keepDialog(DialogInterface dialog, boolean isClose){

        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, isClose);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
