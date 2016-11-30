package com.kevin_xmpp.utils.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Benson_Tom on 2016/8/3.
 */

public class SmsOpenHelper extends SQLiteOpenHelper{
    public static final String T_SMS = "t_sms";//表名称

    private static SmsOpenHelper mInstance = null;//单例

    /** 建表语句*/
    private static final String CREATE_TABLE="CREATE TABLE " + T_SMS + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            SmsTable.FROM_ACCOUNT + " TEXT," +
            SmsTable.TOACCOUNT + " TEXT," +
            SmsTable.BODY + " TEXT," +
            SmsTable.STATUS + " TEXT," +
            SmsTable.TYPE + " TEXT," +
            SmsTable.TIME + " TEXT," +
            SmsTable.SESSION_ACCOUNT + " TEXT);";

    public class SmsTable implements BaseColumns{
        /**
         * fromAccount://发送者
         * toAccount://接收者
         * body://消息的内容
         * status://发送的状态
         * type://消息的类型
         * time://发送时间
         * session_account://会话的id -->最近聊天的人
         * */
        public static final String FROM_ACCOUNT = "from_account";
        public static final String TOACCOUNT = "toAccount";
        public static final String BODY = "body";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String SESSION_ACCOUNT = "session_account";
    }

    /** 单列模式*/
    public static synchronized SmsOpenHelper getInstance(Context context){
        if (mInstance  == null){
            mInstance = new SmsOpenHelper(context);
        }
        return mInstance;
    }


    public SmsOpenHelper(Context context) {
        super(context, "sms.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
