package com.candywidget.yunbiji;

import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 注册线程
 * Created by Candy on 2015/12/19.
 */
public class SignThread extends Thread{
    private String username;
    private String password;
    private Handler handler;
    public SignThread(String username, String password,Handler handler){
        this.username = username;
        this.password = password;
        this.handler = handler;
    }

    @Override
    public void run() {
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br= null;
        try {
            //建立连接
            URL url = new URL("http://1.meidaoyunbiji.sinaapp.com/sign.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(3000);
            //LoginSendBean转换为json
            LoginSendBean sendBean = new LoginSendBean(username,password);
            Gson gson = new Gson();
            String sendJson = gson.toJson(sendBean, new TypeToken<LoginSendBean>() {
            }.getType());
            System.out.println("发送的数据是：" + sendJson);
            //发送数据
            os = connection.getOutputStream();
            os.write(sendJson.getBytes("UTF-8"));
            //获取数据
            if (connection.getResponseCode() != 200){
                handler.sendEmptyMessage(1);
                System.out.println("网络连接错误");
                return;
            }
            is = connection.getInputStream();
            isr = new InputStreamReader(is);
            String result = "";
            String line ;
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null){
                result += line;
            }
            System.out.println("返回的数据是：" + result);
            //告诉主线程注册结果：0成功，1失败
            if (result.contains("success")){
                handler.sendEmptyMessage(0);
                return;
            }
            if (result.contains("fail")){
                handler.sendEmptyMessage(1);
                return;
            }
            if (result.contains("duplicate")){
                handler.sendEmptyMessage(2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
