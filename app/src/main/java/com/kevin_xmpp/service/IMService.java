package com.kevin_xmpp.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kevin_xmpp.activity.LoginActivity;
import com.kevin_xmpp.provider.ContactsProvider;
import com.kevin_xmpp.provider.SmsProvider;
import com.kevin_xmpp.utils.PinyinUtil;
import com.kevin_xmpp.utils.ShowLogUtils;
import com.kevin_xmpp.utils.ThreadUtils;
import com.kevin_xmpp.utils.ToastUtils;
import com.kevin_xmpp.utils.dbhelper.ContactsOpenHelper;
import com.kevin_xmpp.utils.dbhelper.SmsOpenHelper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benson_Tom on 2016/6/26.
 */
public class IMService extends Service {
    public static final String TAG = "IMService";
    public static XMPPConnection conn;//保存连接对象，是静态变量,不会被gc所管理，也不会被内存回收
    public static String mCurtAccount;
    private Roster mRoster;
    private MyRosterListener mRosterListener;
    private Chat mCurChat;
    private ChatManager mChatManager;
    private Map<String, Chat> mChatMap = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        /**
         * 返回一个Service的实例
         */
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public void onCreate() {
        ShowLogUtils.show(TAG, "------------Service onCreate-------");
        /***-----------------联系人的同步 begin - ----------*/
        //开启线程，同步用户数据到数据库
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //得到花名册对象
                mRoster = IMService.conn.getRoster();
                //得到所有的联系人
                final Collection<RosterEntry> entries = mRoster.getEntries();
                //设置监听
                mRosterListener = new MyRosterListener();
                mRoster.addRosterListener(mRosterListener);

                for (RosterEntry entry : entries) {
                    saveOrUpdateEntry(entry);
                }
            }
        });
        /***-----------------联系人的同步 end- ----------*/
        /* ================创建消息的管理者 注册监听 start=============== */
        if (mChatManager == null) {
            mChatManager = IMService.conn.getChatManager();
        }
        mChatManager.addChatListener(mMyChatManagerListener);

        /* ================创建消息的管理者 注册监听 end=============== */
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ShowLogUtils.show(TAG, "------------Service onStartCommand-------");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        ShowLogUtils.show(TAG, "------------Service onDestroy-------");
        //移除RosterListener监听
        if (mRoster != null && mRosterListener != null) {
            mRoster.removeRosterListener(mRosterListener);
        }

        //移除消息监听
        if (mCurChat != null && mMessageListener != null) {
            mCurChat.removeMessageListener(mMessageListener);
        }
        super.onDestroy();
    }

    /**
     * 联系人信息的监听
     */
    class MyRosterListener implements RosterListener {

        /**
         * 联系人添加
         **/
        @Override
        public void entriesAdded(Collection<String> collection) {
            for (String entrys : collection) {
                RosterEntry entry = mRoster.getEntry(entrys);
                //插入或者更新
                saveOrUpdateEntry(entry);
            }
        }

        /**
         * 联系人更新
         **/
        @Override
        public void entriesUpdated(Collection<String> collection) {
            ShowLogUtils.show(TAG, "-------entriesUpdated-------");
            //插入或者更新
            for (String entrys : collection) {
                RosterEntry entry = mRoster.getEntry(entrys);
                //插入或者更新
                saveOrUpdateEntry(entry);
            }
        }

        /***
         * 联系人删除
         **/
        @Override
        public void entriesDeleted(Collection<String> collection) {
            ShowLogUtils.show(TAG, "-------entriesDeleted-------");
            //删除或者更新
            for (String account : collection) {
                getContentResolver().delete(ContactsProvider.URI_CONTACT, ContactsOpenHelper.ContactTable.ACCOUNT + "=?",
                        new String[]{account});
            }

        }

        /***
         * 联系人状态改变
         **/
        @Override
        public void presenceChanged(Presence presence) {
            ShowLogUtils.show(TAG, "-------presenceChanged-------");
        }
    }


    MyMessageListener mMessageListener = new MyMessageListener();
    /**
     * 聊天消息的监听
     */
    class MyMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            /**
             * From:kev1@kevin/Spark
             * To:admin@kevin/Smack
             * Type:chat
             */
            //收到消息，保存消息,获取chat的参与者，就是会话的id
            String participant = chat.getParticipant();
            Log.i(TAG, "-----------监听消息中-----------------" + "Message:" + message.getBody());

            String body = message.getBody();
            if (body != null) {
                saveMessage(participant, message);
                Log.i(TAG, "--------------插入消息了--------------");
            }

        }
    }

    MyChatManagerListener mMyChatManagerListener = new MyChatManagerListener();

    class MyChatManagerListener implements ChatManagerListener{
        @Override
        public void chatCreated(Chat chat, boolean b) {
            Log.i(TAG, "------------ChatManagerListener   Create----------------");

            String participant = chat.getParticipant();//参与者，即和我聊天的人
            //因为他人创建的和自己创建的，参与者对应的jid不同，所以保存chat的时候需要统一处理
            participant = filterAccount(participant);
            if (!mChatMap.containsKey(participant)){
                mChatMap.put(participant,chat);
                chat.addMessageListener(mMessageListener);
            }

            if (b) {
                //participant:kev1@kevin
                Log.i(TAG, "-----------当前账户创建的Chat----------participant:"+participant);
            } else {
                //participant:kev1@kevin/Spark
                Log.i(TAG, "-----------别人账户创建的Chat----------participant:"+participant);
            }
        }
    }

    private void saveOrUpdateEntry(RosterEntry entry) {
        ContentValues values = new ContentValues();
        String account = filterAccount(entry.getUser());
        values.put(ContactsOpenHelper.ContactTable.ACCOUNT, account);

        //处理昵称为空的情况
        String nickname = entry.getName();
        ShowLogUtils.show(TAG,"NICKNAME:"+nickname);
        if (nickname == null || "".equals(nickname)) {
            //kevin@qq.com---> kevin
            nickname = account.substring(0, account.indexOf("@"));
            ShowLogUtils.show(TAG,"nickname:"+nickname);
        }
        values.put(ContactsOpenHelper.ContactTable.NICKNAME, nickname);

        values.put(ContactsOpenHelper.ContactTable.AVATAR, "12");
        values.put(ContactsOpenHelper.ContactTable.PINYIN, PinyinUtil.getPinyin(account));

        //先update,再插入
        int updateCount = getContentResolver().update(ContactsProvider.URI_CONTACT, values,
                ContactsOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{account});
        ShowLogUtils.show(TAG, "UpdateCount Success" + values.get(ContactsOpenHelper.ContactTable.ACCOUNT));
        if (updateCount <= 0) {
            getContentResolver().insert(ContactsProvider.URI_CONTACT, values);
            ShowLogUtils.show(TAG, "Insert Success");
        }
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMessage(Message msg) {
        try {
            //2.创建聊天对象
            /** mClickAccount是被发送对象的jid*/
            // Chat chat = mChatManager.createChat(被发送对象的jid, 消息的监听者);
            //保存chat
            String toAccount = msg.getTo();
            if (mChatMap.containsKey(toAccount)) {
                mCurChat = mChatMap.get(toAccount);
            } else {
                mCurChat = mChatManager.createChat(toAccount, mMessageListener);
                mChatMap.put(toAccount, mCurChat);
            }
            //发送消息
            mCurChat.sendMessage(msg);
            //保存发送的消息到本地数据库
            //我（from) -----> 小秘（to) =====>小秘 （会话ID）
            saveMessage(msg.getTo(), msg);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存消息-->ContentResolver-->ContentProvider-->SQLite
     */
    private void saveMessage(String sessionID, Message msg) {
        ContentValues values = new ContentValues();
        //我（from) -----> 小秘（to) =====>小秘 （会话ID）
        //小秘（from）----> 我（to) ======>小秘  （会话ID）
        /** 过滤不规则Account*/
        sessionID= filterAccount(sessionID);
        String fromAccount = msg.getFrom();
        fromAccount=filterAccount(fromAccount);
        String toAccount = msg.getTo();
        toAccount=filterAccount(toAccount);


        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, fromAccount);
        values.put(SmsOpenHelper.SmsTable.TOACCOUNT, toAccount);
        values.put(SmsOpenHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsOpenHelper.SmsTable.STATUS, "offline");
        values.put(SmsOpenHelper.SmsTable.TYPE, msg.getType().name());
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, sessionID);
        getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    @NonNull
    private String filterAccount(String account) {
        return account.substring(0,account.indexOf("@"))+"@"+ LoginActivity.SERVER_NAME;
    }

}
