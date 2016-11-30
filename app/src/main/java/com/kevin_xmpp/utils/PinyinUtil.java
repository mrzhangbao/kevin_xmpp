package com.kevin_xmpp.utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by Benson_Tom on 2016/8/1.
 */

public class PinyinUtil {
    public static String getPinyin(String s){
        //String stringPinyin = PinyinHelper.convertToPinyinString("转换内容", "分隔符", 拼音格式是否有声调);
        return PinyinHelper.convertToPinyinString(s,"", PinyinFormat.WITHOUT_TONE);
    }
}
