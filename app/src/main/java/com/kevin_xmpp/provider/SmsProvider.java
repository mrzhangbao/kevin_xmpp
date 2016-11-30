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
import com.kevin_xmpp.utils.dbhelper.SmsOpenHelper;
//Edited here by Kevin at  2016/8/3 14:33 ;

/**
 * Created by Benson_Tom on 2016/8/3.
 */
public class SmsProvider extends ContentProvider {
    private static final String TAG = "SmsProvider";
    public static final String AUTHORITIES = SmsProvider.class.getName();

    public static final Uri URI_SESSION = Uri.parse("content://" + AUTHORITIES + "/session");
    public static Uri URI_SMS = Uri.parse("content://" + AUTHORITIES + "/sms");

    static UriMatcher mUriMatcher;

    private static final int SMS = 1;
    private static final int SESSION= 2;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //添加匹配规则
        mUriMatcher.addURI(AUTHORITIES, "sms", SMS);
        mUriMatcher.addURI(AUTHORITIES, "session", SESSION);

    }

    private SmsOpenHelper mSmsOpenHelper;


    @Override
    public boolean onCreate() {
        mSmsOpenHelper = SmsOpenHelper.getInstance(getContext());
        if (mSmsOpenHelper != null) {
            mSmsOpenHelper.getWritableDatabase();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /* ================Crud start=============== */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = mUriMatcher.match(uri);
        switch (mUriMatcher.match(uri)) {
            case SMS:
                long id = mSmsOpenHelper.getWritableDatabase().insert(SmsOpenHelper.T_SMS, "", contentValues);
                if (id > 0) {
                    Log.i(TAG, "--------------SmsProvider insert success-----------");
                    uri = ContentUris.withAppendedId(uri, id);
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        int deleteCount = 0;
        switch (mUriMatcher.match(uri)) {
            case SMS:
                //删除了多少条数据
                deleteCount = mSmsOpenHelper.getWritableDatabase().delete(SmsOpenHelper.T_SMS, s, strings);
                if (deleteCount > 0) {
                    Log.i(TAG, "--------------SmsProvider delete success-----------");
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
            default:
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        int updateCount = 0;
        switch (mUriMatcher.match(uri)) {
            case SMS:
                //更新了多少条数据
                updateCount = mSmsOpenHelper.getWritableDatabase().update(SmsOpenHelper.T_SMS, contentValues, s, strings);
                if (updateCount > 0){
                    Log.i(TAG, "--------------SmsProvider update success-----------");
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
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
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case SMS:
                cursor = mSmsOpenHelper.getReadableDatabase().query(SmsOpenHelper.T_SMS, strings, s, strings1, null, null, s1);
                Log.i(TAG, "--------------SmsProvider query success-----------");
                break;
            case SESSION:
                SQLiteDatabase db = mSmsOpenHelper.getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM (SELECT * FROM t_sms WHERE from_account = ? or toAccount = ? ORDER BY time ASC)"
                +"GROUP BY session_account",strings1);

                break;
            default:
                break;
        }
        return cursor;
    }
}
