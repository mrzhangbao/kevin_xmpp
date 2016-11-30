package com.kevin_xmpp.utils.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.kevin_xmpp.utils.ShowLogUtils;

/**
 * Created by Benson_Tom on 2016/6/26.
 */
public class ContactsOpenHelper extends SQLiteOpenHelper{
    public static final String T_CONTACT = "t_contact";//表的名称
    private static final String DB_NAME = "contacts.db";//数据库名称
    private static final int DATABASE_VERSION=1;//数据库版本号

    private static ContactsOpenHelper mInstance = null;//单例

    private static final String CREATE_TABLE="create table " + T_CONTACT + "(_id integer primary key autoincrement," +
            ContactTable.ACCOUNT + " TEXT," +
            ContactTable.NICKNAME + " TEXT," +
            ContactTable.AVATAR + " TEXT," +
            ContactTable.PINYIN + " TEXT);";

    //就会默认的给我们添加一列 _id
    public class ContactTable implements BaseColumns{
        public static final String ACCOUNT = "account";//账号
        public static final String NICKNAME = "nickname";//昵称
        public static final String AVATAR= "avatar";//头像
        public static final String PINYIN = "pinyin";
    }

    ContactsOpenHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        ShowLogUtils.show("TAG","创建数据库成功");
    }

    /**单例模式**/
    public static synchronized ContactsOpenHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new ContactsOpenHelper(context);

        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        String sql = "create table " + T_CONTACT + "(_id integer primary key autoincrement," +
//                ContactTable.ACCOUNT + " TEXT, " +
//                ContactTable.NICKNAME + " TEXT, " +
//                ContactTable.AVATAR + " TEXT, " +
//                ContactTable.PINYIN + " TEXT);";
        ShowLogUtils.show("TAG","创建table成功---");
        db.execSQL(CREATE_TABLE);
        ShowLogUtils.show("TAG","创建table成功");
//        db.execSQL("CREATE TABLE user(id integer primary key,userAccount varchar(20),username varchar(20)," +
//                "userIcon varchar(30),userIntroduce varchar(60),userFansNumbers varchar(20),userFocusNumbers varchar(20))");
//        ShowLogUtils.show("TAG","创建table成功---------");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        db.execSQL("drop if table exists "+T_CONTACT);
//        ShowLogUtils.show("TAG","删除table成功");
//        onCreate(db);
    }
}
