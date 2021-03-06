package com.candywidget.yunbiji;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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
 * 更新线程
 * Created by Administrator on 2015/12/21.
 */
public class UpdateThread extends Thread {
    private Article article;
    private Context context;
    private Handler handler;

    public UpdateThread(Article article, Context context, Handler handler) {
        this.article = article;
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        SQLiteDatabase dbArticle = null;
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            //更新本地数据库
            dbArticle = context.openOrCreateDatabase("Articles.db3", context.MODE_PRIVATE, null);
            dbArticle.execSQL("UPDATE articles SET title=?,time=?,content=? WHERE id=?",
                    new Object[]{article.getTitle(), article.getTime(), article.getContent(),article.getId()});
            //把article转为json
            Gson gson = new Gson();
            String sendJson = gson.toJson(article, new TypeToken<Article>() {}.getType());
            System.out.println(sendJson);
            //发送sendJson
            URL url = new URL("http://1.meidaoyunbiji.sinaapp.com/update.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(3000);
            os = connection.getOutputStream();
            os.write(sendJson.getBytes("UTF-8"));
            //获取数据
            if (connection.getResponseCode() != 200){
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
            System.out.println("返回的数据是：" + result);
            if (result.contains("success")){
                System.out.println("更新笔记成功");
                handler.sendEmptyMessage(0);
            }else {
                System.out.println("更新笔记失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dbArticle != null) {
                dbArticle.close();
            }
        }
    }
}
