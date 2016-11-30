package com.kevin_xmpp.utils;

import android.util.Log;

/**
 * Created by Benson_Tom on 2016/8/1.
 */

public class ShowLogUtils {
    public static void show(String TAG,String s){
        for (int i = 0; i < 5 ; i++){
            Log.i(TAG,s);
        }

    }
}
