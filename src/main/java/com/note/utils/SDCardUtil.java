package com.note.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * SD卡文件管理工具
 */
public class SDCardUtil {

    // 判断SD卡是否存在
    public static Boolean isSDCardExists(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // 创建便签文件保存目录
    public static File saveFilePath(Context context) throws IOException {

        File sdCardFile = Environment.getExternalStorageDirectory();
        String fileName = "notes.txt";
        File filePath = new File(sdCardFile,fileName);

        int i = 1;
        while(filePath.exists()){
            fileName = "notes(" + i + ").txt";
            i ++;
            filePath = new File(sdCardFile, fileName);
        }
        filePath.createNewFile();

        return filePath;
    }

    // 追加保存便簽內容字符流
    public static void saveFileWithAppendMode(String filePath, String values) throws IOException {
        saveFileWithAppendMode(new File(filePath), values);
    }

    public static void saveFileWithAppendMode(File file,String values) throws IOException {
        BufferedWriter bfWriter = null;
        StringReader stringReader = null;

        try {
            bfWriter = new BufferedWriter(new FileWriter(file, true));
            stringReader = new StringReader(values);

            char[] buffer = new char[1024 * 8];
            int len = 0;
            while ((len = stringReader.read(buffer)) != -1){
                bfWriter.write(buffer, 0, len);
            }
            bfWriter.newLine();
            bfWriter.flush();

        } finally {
            if (bfWriter != null){
                bfWriter.close();
            }
            if (stringReader != null){
                stringReader.close();
            }
        }

    }

}
