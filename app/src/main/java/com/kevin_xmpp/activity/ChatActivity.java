package com.kevin_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kevin_xmpp.R;
import com.kevin_xmpp.provider.SmsProvider;
import com.kevin_xmpp.service.IMService;
import com.kevin_xmpp.utils.ShowLogUtils;
import com.kevin_xmpp.utils.ThreadUtils;
import com.kevin_xmpp.utils.dbhelper.SmsOpenHelper;

import org.jivesoftware.smack.packet.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatActivity extends AppCompatActivity {
    public static final String TAG = "ChatActivity";

    public static final String CLICK_ACCOUNT = "clickAccount";
    public static final String CLICK_NICKNAME = "clickNickname";
    public static final String QUERY_CONDITION = "(from_account = ? and toAccount = ? ) or ( from_account = ? and toAccount = ?)";//where条件

    @Bind(R.id.chat_title)
    TextView mChatTitle;
    @Bind(R.id.chat_ListView)
    ListView mChatListView;
    @Bind(R.id.chat_EtBody)
    EditText mChatEtBody;
    @Bind(R.id.chat_BtSendBody)
    Button mChatBtSendBody;
    private String mClickAccount;
    private String mClickNickname;
    private CursorAdapter mAdapter;
    private static final int RECEIVE = 1;
    private static final int SEND = 0;
    private IMService mImService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        init();
        initView();
        initData();
        initListener();
    }

    private void init() {
        registerContentObserver();
        //绑定服务
        Intent service = new Intent(ChatActivity.this, IMService.class);
        //BIND_AUTO_CREATE---flag
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);

        mClickAccount = getIntent().getStringExtra(CLICK_ACCOUNT);
        mClickNickname = getIntent().getStringExtra(CLICK_NICKNAME);
    }

    private void initView() {
        mChatTitle.setText(mClickNickname);
    }

    private void initData() {
        setOrNotifyAdapter();
    }

    private void setOrNotifyAdapter() {
        //判断Adapter是否存在
        if (mAdapter != null) {

            Cursor c = mAdapter.getCursor();
            c.requery();
            mChatListView.setSelection(c.getCount() - 1);//聊天记录滚动到最后一条数据

            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String[] QUERY_CONDITIONS = new String[]{IMService.mCurtAccount, mClickAccount, mClickAccount, IMService.mCurtAccount};

                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS, null, QUERY_CONDITION, QUERY_CONDITIONS,
                        SmsOpenHelper.SmsTable.TIME + " ASC");

//                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS, null,"(from_account =? and toAccount =?)or" +
//                        "( from_account =? and toAccount =?)",
//                        new String[]{IMService.mCurtAccount, mClickAccount, mClickAccount, IMService.mCurtAccount},
//                        SmsOpenHelper.SmsTable.TIME + " ASC");

                //如果没有数据直接返回
                if (cursor.getCount() < 1) {
                    ShowLogUtils.show(TAG, "***************没有查询到消息数据************");
                    return;
                }
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new CursorAdapter(ChatActivity.this, cursor) {
                            @Override
                            public int getItemViewType(int position) {
                                cursor.moveToPosition(position);
                                //取出消息的创建者
                                String fromAccount = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
                                if (!IMService.mCurtAccount.equals(fromAccount)) {//接收
                                    return RECEIVE;
                                } else {//发送
                                    return SEND;
                                }
//                                return super.getItemViewType(position);// 0 1
                                //接收--->如果当前账号 不等于 消息的创建者
                                //发送--->如果当前账号 等于 消息的创建者
                            }
                            @Override
                            public int getViewTypeCount() {
                                return super.getViewTypeCount() + 1;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                ViewHolder mViewHolder;
                                if (getItemViewType(position) == RECEIVE) {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_receive, null);
                                        mViewHolder = new ViewHolder();
                                        convertView.setTag(mViewHolder);
                                        mViewHolder.mChatBody = (TextView) convertView.findViewById(R.id.chat_body);
                                        mViewHolder.mChatTime = (TextView) convertView.findViewById(R.id.chat_time);
                                        mViewHolder.mAvatar = (ImageView) convertView.findViewById(R.id.chat_avatar);
                                    } else {
                                        mViewHolder = (ViewHolder) convertView.getTag();
                                    }
                                } else {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_send, null);
                                        mViewHolder = new ViewHolder();
                                        convertView.setTag(mViewHolder);
                                        mViewHolder.mChatBody = (TextView) convertView.findViewById(R.id.chat_body);
                                        mViewHolder.mChatTime = (TextView) convertView.findViewById(R.id.chat_time);
                                        mViewHolder.mAvatar = (ImageView) convertView.findViewById(R.id.chat_avatar);
                                    } else {
                                        mViewHolder = (ViewHolder) convertView.getTag();
                                    }
                                }
                                //得到数据，显示数据
                                cursor.moveToPosition(position);

                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String time = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TIME));

                                String formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time)));
                                mViewHolder.mChatBody.setText(body);
                                mViewHolder.mChatTime.setText(formatTime);

                                return super.getView(position, convertView, parent);
                            }

                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                return null;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {

                            }
//                            @Override
//                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                               TextView tv = new TextView(context);
//                                return tv;
//                            }
//                            @Override
//                            public void bindView(View view, Context context, Cursor cursor) {
//                                TextView tv = (TextView) view;
//                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
//                                tv.setText(body);
//                            }

                            class ViewHolder {
                                TextView mChatTime;
                                TextView mChatBody;
                                ImageView mAvatar;
                            }

                        };
                        mChatListView.setAdapter(mAdapter);
                        //聊天记录滚动到最后一条数据
                        mChatListView.setSelection(mAdapter.getCount() - 1);
                    }
                });
            }
        });
    }

    private void initListener() {
    }

    @OnClick(R.id.chat_BtSendBody)
    public void sendBody() {
        final String body = mChatEtBody.getText().toString();
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //1.获取消息的管理者
                //3.创建消息实体
                Message msg = new Message();
                msg.setFrom(IMService.mCurtAccount);//当前登录的用户
                msg.setTo(mClickAccount);//被发送消息的用户
                msg.setBody(body);//消息的内容
                msg.setType(Message.Type.chat);//类型是chat

                //调用Service里面的sendMessage方法发送消息
                mImService.sendMessage(msg);

                //4.清除输入框的内容
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatEtBody.setText("");
                    }
                });
            }
        });
    }


    @Override
    protected void onDestroy() {
        unRegisterContentObserver();
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        super.onDestroy();
    }

    /* ================使用ContentObserver时刻监听消息数据的改变=============== */
    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mMyContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unRegisterContentObserver() {
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 接收聊天数据的改变
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //设置Adapter或者更新notifyAdapter
            setOrNotifyAdapter();
            super.onChange(selfChange, uri);
        }
    }

    MyServiceConnection mMyServiceConnection = new MyServiceConnection();

    /**
     * Service连接的监听
     * 两个接口回调方法
     */
    class MyServiceConnection implements ServiceConnection {

        /**
         * 服务链接成功
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "-----------onServiceConnected OK-----------------");
            IMService.MyBinder binder = (IMService.MyBinder) iBinder;
            mImService = binder.getService();
        }

        /**
         * 服务链接断开
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "-----------onServiceDisconnected-----------------");

        }
    }
}
