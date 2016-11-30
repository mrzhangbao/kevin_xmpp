package com.kevin_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kevin_xmpp.R;
import com.kevin_xmpp.service.IMService;
import com.kevin_xmpp.service.PushService;
import com.kevin_xmpp.utils.ThreadUtils;
import com.kevin_xmpp.utils.ToastUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Benson_Tom on 2016/6/25.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    public static String SERVER_NAME = "kevin.com";
    public static final String USERNAME_EMPTY = "账号不能为空";
    public static final String PASSWORD_EMPTY = "密码不能为空";
    public static final String HOST = "119.124.31.115";
    public static final int PORT = 5222;

    @Bind(R.id.id_username_et)
    EditText mEtUsername;
    @Bind(R.id.id_password_et)
    EditText mEtPassword;
    @Bind(R.id.id_login_bt)
    Button mBtLogin;

    private String mUsername;
    private String mPassword;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initView();
        initEvent();

    }

    private void initEvent() {
        mUsername = mEtUsername.getText().toString();
        mPassword = mEtPassword.getText().toString();

        mBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsername = mEtUsername.getText().toString();
                mPassword = mEtPassword.getText().toString();
                if (TextUtils.isEmpty(mUsername)) {
                    mEtUsername.setError(USERNAME_EMPTY);

                    return;
                }
                if (TextUtils.isEmpty(mPassword)) {
                    mEtPassword.setError(PASSWORD_EMPTY);
                    return;
                }
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //创建链接配置对象
                            ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);

                            //额外配置，方便开发
                            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//关闭安全模式，明文传输，方便调试
                            config.setDebuggerEnabled(true);//开启调试模式，方便查看调试的内容
                            //开始创建连接对象
                            XMPPConnection conn = new XMPPConnection(config);

                            //开始连接
                            conn.connect();

                            //连接成功，开始登陆
                            conn.login(mUsername, mPassword);
                            IMService.conn = conn;//保存连接对象
                            IMService.mCurtAccount = mUsername + "@" + SERVER_NAME;//保存当前的登陆对象

                            //已经登陆成功
                            ToastUtils.showToast(LoginActivity.this, "登陆成功");
                            finish();
                            //跳转到主界面
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            //登陆成功后启动IMService
                            startService(new Intent(LoginActivity.this, IMService.class));
                            //登陆成功后启动PushService
                            startService(new Intent(LoginActivity.this, PushService.class));
                        } catch (XMPPException e) {
                            ToastUtils.showToast(LoginActivity.this, "登陆失败");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void initView() {
        mEtUsername = (EditText) this.findViewById(R.id.id_username_et);
        mEtPassword = (EditText) this.findViewById(R.id.id_password_et);
        mBtLogin = (Button) this.findViewById(R.id.id_login_bt);
    }

}
