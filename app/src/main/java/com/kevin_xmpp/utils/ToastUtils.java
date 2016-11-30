package com.kevin_xmpp.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Benson_Tom on 2016/6/26.
 */
public class ToastUtils {
    public static void showToast(final Context context , final String text){
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
