package com.kevin_xmpp;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import com.kevin_xmpp.provider.ContactsProvider;
import com.kevin_xmpp.utils.dbhelper.ContactsOpenHelper;

/**
 * Created by Benson_Tom on 2016/6/27.
 */
public class TestContactsProvider extends AndroidTestCase {
    private static final String TAG = "TestContactsProvider";

    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(ContactsOpenHelper.ContactTable.ACCOUNT,"kevin@qq.com");
        values.put(ContactsOpenHelper.ContactTable.NICKNAME,"老娘");
        values.put(ContactsOpenHelper.ContactTable.AVATAR,"12");
        values.put(ContactsOpenHelper.ContactTable.PINYIN,"laoNiang");
        getContext().getContentResolver().insert(ContactsProvider.URI_CONTACT,values);
    }

    public void testDelete() {
        getContext().getContentResolver().delete(ContactsProvider.URI_CONTACT,ContactsOpenHelper.ContactTable.ACCOUNT+"=?",
               new String[] {"kevin@qq.com"} );
    }

    public void testUpdate() {
        ContentValues values = new ContentValues();
        values.put(ContactsOpenHelper.ContactTable.ACCOUNT,"kevin123@qq.com");
        values.put(ContactsOpenHelper.ContactTable.NICKNAME,"你的饭");
        values.put(ContactsOpenHelper.ContactTable.AVATAR,"12");
        values.put(ContactsOpenHelper.ContactTable.PINYIN,"laohaha");
        getContext().getContentResolver().update(ContactsProvider.URI_CONTACT,values,
                ContactsOpenHelper.ContactTable.ACCOUNT+"=?",
                new String[] {"kevin@qq.com"});
    }

    public void testQuery() {
        Cursor c = getContext().getContentResolver().query(ContactsProvider.URI_CONTACT,
                null, null, null, null);

        int count = c.getColumnCount();
        if (count > 0){
            while (c.moveToNext()){
                for (int i = 0; i < count; i++){
                    Log.i(TAG,c.getString(i));
                    System.out.print(c.getString(i) + "    ");
                }
                System.out.println("");
            }
        }else {
            System.out.print("--------------Query Failure");
        }

    }
}
