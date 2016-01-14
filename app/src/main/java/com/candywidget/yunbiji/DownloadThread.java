package com.candywidget.yunbiji;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 下载线程
 * Created by Candy on 2015/12/17.
 */
class DownloadThread extends Thread {
    private Context context;
    private String username;
    private Handler handler;

    public DownloadThread(Context context, String username, Handler handler) {
        this.context = context;
        this.username = username;
        this.handler = handler;
    }

    @Override
    public void run() {
        SQLiteDatabase dbArticle = null;
        BufferedReader br = null;
        InputStreamReader isr = null;
        InputStream is = null;
        OutputStream os = null;
        Cursor cursor = null;
        try {
            //获取本地数据库的笔记id
            ArrayList<Integer> id = new ArrayList<>();
            dbArticle = context.openOrCreateDatabase("Articles.db3", context.MODE_PRIVATE, null);
            dbArticle.execSQL("CREATE TABLE IF NOT EXISTS articles(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT,time TEXT,content TEXT,username TEXT)");
            cursor = dbArticle.rawQuery("SELECT id FROM articles WHERE username=?", new String[]{username});
            while (cursor.moveToNext()){
                id.add(cursor.getInt(0));
            }
            System.out.println("本地数据库中有几个笔记："+id.size());
            //建立连接
            URL url = new URL("http://1.meidaoyunbiji.sinaapp.com/download.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(3000);
            //DownloadSendBean转换为json
            DownloadSendBean sendBean = new DownloadSendBean(username,id);
            Gson gson = new Gson();
            String sendJson = gson.toJson(sendBean, new TypeToken<DownloadSendBean>() {
            }.getType());
            System.out.println("发送的数据是：" + sendJson);
            //发送数据
            os = connection.getOutputStream();
            os.write(sendJson.getBytes("UTF-8"));
            //获取数据
            if (connection.getResponseCode() != 200){
                handler.sendEmptyMessage(1);
                System.out.println("连接失败");
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
            System.out.println("返回的数据是："+result);
            //json转换为articles
            if (result.equals("")||result.equals("isnull")){
                handler.sendEmptyMessage(2);
                System.out.println("没有需要下载的数据");
                return;
            }
            ArrayList<Article> articles = gson.fromJson(result, new TypeToken<ArrayList<Article>>() {}.getType());
            //把articles存入本地数据库
            for (int j = 0; j < articles.size(); j++) {
                int _id = articles.get(j).getId();
                String title = articles.get(j).getTitle();
                String time = articles.get(j).getTime();
                String content = articles.get(j).getContent();
                dbArticle.execSQL("INSERT INTO articles (id,title,time,content,username) VALUES (?,?,?,?,?)",
                        new Object[]{_id,title, time, content, username});
            }
            handler.sendEmptyMessage(0);
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
                if (cursor != null) {
                    cursor.close();
                }
                if (dbArticle != null) {
                    dbArticle.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
