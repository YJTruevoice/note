package com.note.utils;

/**
 * 字符串管理工具
 */
public class StringUtil {

    public static String deleteExtraSpace(String oldString){
        if (oldString.trim().equals("")){
            return "";
        }
        String content = null;
        int l = oldString.length();
        if (l > 0){
            for (int i = oldString.length() - 1; i >= 0; i --){
                if (oldString.charAt(i) == ' ' || oldString.charAt(i) == '\n'){
                    continue;
                }else {
                    l = i;
                    break;
                }
            }
            if (l < oldString.length()){
                content = oldString.substring(0, l + 1);
            } else {
                content = oldString.substring(0, l);
            }
        } else {
            content = oldString;
        }

        return content;
    }

}
