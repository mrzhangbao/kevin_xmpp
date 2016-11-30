package com.kevin_xmpp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kevin_xmpp.utils.ShowLogUtils;
import com.kevin_xmpp.utils.dbhelper.ContactsOpenHelper;

/**
 * Created by Benson_Tom on 2016/6/26.
 */
public class ContactsProvider extends ContentProvider {

    private static final String TAG = "ContactsProvider";

    //主机地址的常量，-->当前类的完成路径
    public static final String AUTHORITIES = ContactsProvider.class.getCanonicalName();

    //地址匹配对象的创建
    static UriMatcher mUriMatcher;

    //对应联系人表的一个Uri常量
    public static Uri URI_CONTACT = Uri.parse("content://" + AUTHORITIES + "/contact");

    public static final int CONTACT = 1;

    private ContactsOpenHelper mHelper;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);//使用默认的code
        //添加一个匹配规则
        mUriMatcher.addURI(AUTHORITIES, "contact", CONTACT);//要注意匹配规则，以免导致不匹配
        //content://com.kevin_xmpp.provider.ContactsProvider/contacts
    }

    private String nullColumnHack;




    @Override
    public boolean onCreate() {
        mHelper = ContactsOpenHelper.getInstance(getContext());
        if (mHelper != null) {
            //所有东西都搞定了，就是表建不出来，原来SqliteHelper的OnCreate()方法不会被自动调用，只有使用getReadableDatabase()后才会新建表
            mHelper.getWritableDatabase();
            return true;
        }
        return false;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //数据存储到SQLite中-->建立表，SQLiteOpenHelper
        int code = mUriMatcher.match(uri);//先匹配Uri，判断是否为CONTACT
        switch (code) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                nullColumnHack = "";
                long id = db.insert(ContactsOpenHelper.T_CONTACT, nullColumnHack, values);
                if (id != -1) {
                    Log.i(TAG, "-------------ContactsProvider----insertSuccess---------");
                    System.out.print("-------------ContactsProvider----insertSuccess---------");
                    //插入成功，则凭借uri
                    //content://com.kevin_xmpp.provider.ContactsProvid
                    // er/contact
                    uri = ContentUris.withAppendedId(uri, id);
                    //通知ContentObserver数据改变了
                    //getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);
                    /**为null表示所有的ContentObserver 均可以收到通知
                     * 不为空则为特定的ContentObserver才能收到通知*/
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);
                }else {
                    Log.i(TAG, "-------------ContactsProvider----insertFailure---------");
                    System.out.print("-------------ContactsProvider----insertFailure---------");
                }
                break;
            default:
                break;

        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        int code = mUriMatcher.match(uri);//先匹配Uri，判断是否为CONTACT
        int deleteCount = 0;
        switch (code) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //影响的行数
                deleteCount = db.delete(ContactsOpenHelper.T_CONTACT, s, strings);
                if (deleteCount > 0) {
                    Log.i(TAG, "-------------ContactsProvider----deleteSuccess---------");
                    /**为null表示所有的ContentObserver 均可以收到通知
                     * 不为空则为特定的ContentObserver才能收到通知*/
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);


                }
                break;
            default:
                break;

        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        int code = mUriMatcher.match(uri);//先匹配Uri，判断是否为CONTACT
        int updateCount = 0;
        switch (code) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //更新的条数
                updateCount = db.update(ContactsOpenHelper.T_CONTACT, contentValues, s, strings);
                if (updateCount > 0) {
                    Log.i(TAG, "-------------ContactsProvider----updateSuccess---------");
                    /**为null表示所有的ContentObserver 均可以收到通知
                     * 不为空则为特定的ContentObserver才能收到通知*/
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);
                }
                break;
            default:
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        int code = mUriMatcher.match(uri);//先匹配Uri，判断是否为CONTACT
        Cursor cursor = null;
        switch (code) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = db.query(ContactsOpenHelper.T_CONTACT, strings, s, strings1, null, null, s1);
                Log.i(TAG, "-------------ContactsProvider----querySuccess---------");
                break;
            default:

        }
        return cursor;
    }
}
