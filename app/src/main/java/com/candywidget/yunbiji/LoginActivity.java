package com.candywidget.yunbiji;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 登录界面
 * Created by Candy on 2015/12/19.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    //初始化界面
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSign;

    private String username;
    private String password;

    private void initView() {
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        btnSign = (Button) findViewById(R.id.btn_sign);
        btnSign.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        switch (v.getId()) {
            //登陆
            case R.id.btn_login:
                if (username.equals("") ||password.equals("")){
                    Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Handler handlerLogin = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 0:
                                Intent intentLogin = new Intent(LoginActivity.this, MainActivity.class);
                                intentLogin.putExtra("username", username);
                                startActivity(intentLogin);
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Toast.makeText(LoginActivity.this, "登录失败，密码错误", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Toast.makeText(LoginActivity.this, "登录失败，用户名不存在", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                LoginThread loginThread = new LoginThread(username, password, handlerLogin);
                loginThread.start();
                break;
            case R.id.btn_sign:
                if (username.equals("")||password.equals("")){
                    Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Handler handlerSign = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what){
                            case 0:
                                Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                Intent intentLogin = new Intent(LoginActivity.this, MainActivity.class);
                                intentLogin.putExtra("username", username);
                                startActivity(intentLogin);
                                break;
                            case 1:
                                Toast.makeText(LoginActivity.this, "服务器故障，注册失败", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Toast.makeText(LoginActivity.this, "用户名已存在，注册失败", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                SignThread signThread = new SignThread(username, password, handlerSign);
                signThread.start();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}
