package com.kevin_xmpp.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kevin_xmpp.R;
import com.kevin_xmpp.activity.ChatActivity;
import com.kevin_xmpp.activity.LoginActivity;
import com.kevin_xmpp.provider.ContactsProvider;
import com.kevin_xmpp.provider.SmsProvider;
import com.kevin_xmpp.service.IMService;
import com.kevin_xmpp.utils.ShowLogUtils;
import com.kevin_xmpp.utils.ThreadUtils;
import com.kevin_xmpp.utils.dbhelper.ContactsOpenHelper;
import com.kevin_xmpp.utils.dbhelper.SmsOpenHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SessionFragment extends Fragment {
    public static final String TAG = "SessionFragment";

    private CursorAdapter mAdapter;

    @Bind(R.id.session_ListView)
    ListView mSessionListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        registerContentObserver();
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void init() {
    }


    private void initData() {
        setOrNotifyAdapter();
    }

    /** 设置或者更新Adapter*/
    private void setOrNotifyAdapter() {
        //判断Adapter是否存在
        if (mAdapter!=null){
            //刷新Adapter
            ThreadUtils.runInUIThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.getCursor().requery();
                }
            });

            return;
        }

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //对应查询记录
                final Cursor c = getActivity().getContentResolver().query(SmsProvider.URI_SESSION,
                        null, null, new String[] {IMService.mCurtAccount,IMService.mCurtAccount}, null);
                //没有数据
                if (c.getCount() <= 0 ) {
                    return;
                }

                //设置Adapter,然后显示数据
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果Convertview == null,返回一个具体的根视图
                        //设置数据和显示数据
                        mAdapter = new CursorAdapter(getActivity(), c) {
                            //如果Convertview == null,返回一个具体的根视图
                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                return View.inflate(context, R.layout.item_chat_session, null);
                            }

                            //设置数据和显示数据
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                ImageView mAvatar = (ImageView) view.findViewById(R.id.session_avatar);
                                TextView mNickname = (TextView) view.findViewById(R.id.session_nickname);
                                TextView mMessageBody = (TextView) view.findViewById(R.id.session_body);

                                String body = cursor.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String account = cursor.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                                String nickName = getNickNameByAccount(account);
                                mMessageBody.setText(body);
                                mNickname.setText(nickName);
                            }
                        };
                        mSessionListView.setAdapter(mAdapter);

                    }
                });
            }
        });

    }
    /**根据账号获取用户昵称*/
    private String getNickNameByAccount(String account){
        ShowLogUtils.show(TAG,"CheckAccount:"+account);
        String nickName = "";
        Cursor c = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null, ContactsOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{account}, null);
        if (c.getCount()>0){
            c.moveToFirst();
            nickName = c.getString(c.getColumnIndex(ContactsOpenHelper.ContactTable.NICKNAME));
        }
        return nickName;
    }

    private void initListener() {
        mSessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);
                //拿到jid(账号)---->发送聊天消息的时候需要
                String account = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                account=account.substring(0,account.indexOf("@"))+"@"+ LoginActivity.SERVER_NAME;

                ShowLogUtils.show(TAG,"Account:"+account);
                //拿到nickname--->显示
                String nickname = getNickNameByAccount(account);

                Intent intent = new Intent(getActivity(), ChatActivity.class);
                //将信息传入ChatActivity
                intent.putExtra(ChatActivity.CLICK_ACCOUNT,account);
                intent.putExtra(ChatActivity.CLICK_NICKNAME,nickname);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        unRegisterContentObserver();
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

      /*===============监听数据库记录的改变===========*/

    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    /**注册监听*/
    public void registerContentObserver(){
        //getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT,true,mMyContentObserver);
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_SMS,true,mMyContentObserver);
    }
    /**反注册监听*/
    public void unRegisterContentObserver(){
        //getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT,true,mMyContentObserver);
        getActivity().getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /** 如果数据库中的数据改变会在这里收到通知*/

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            //更新或者刷新Adapter
            setOrNotifyAdapter();
        }
    }
}
