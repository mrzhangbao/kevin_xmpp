package com.kevin_xmpp.utils;

import android.os.Handler;

/**
 * Created by Benson_Tom on 2016/6/25.
 */
public class ThreadUtils {
    /**
     * 子线程执行task
     */
    public static void runInThread(Runnable task){
        new Thread(task).start();
    }
    //主线程里面的一个Handler
    public static Handler mHandler = new Handler();

    //UI线程执行task
    public static void runInUIThread(Runnable task){
        mHandler.post(task);
    }
}
