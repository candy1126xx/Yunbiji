package com.candywidget.yunbiji;

import android.content.Context;
import android.database.Cursor;
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
import java.util.ArrayList;

public class DeleteThread extends Thread{
    private Context context;
    private String username;
    private ArrayList<Integer> needDelete;
    private Handler handler;

    public DeleteThread(Context context, String username, ArrayList<Integer> needDelete, Handler handler){
        this.context = context;
        this.username = username;
        this.needDelete = needDelete;
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
            //删除本地数据库
            dbArticle = context.openOrCreateDatabase("Articles.db3", context.MODE_PRIVATE, null);
            for (int i = 0; i <needDelete.size(); i++){
                dbArticle.execSQL("DELETE FROM articles WHERE id=?", new Object[]{needDelete.get(i)});
                System.out.println("删除的笔记ID："+needDelete.get(i));
            }
            //把article转为json
            Gson gson = new Gson();
            DownloadSendBean bean = new DownloadSendBean(username, needDelete);
            String sendJson = gson.toJson(bean, new TypeToken<DownloadSendBean>() {}.getType());
            System.out.println(sendJson);
            //发送sendJson
            URL url = new URL("http://1.meidaoyunbiji.sinaapp.com/delete.php");
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
                System.out.println("删除笔记成功");
                handler.sendEmptyMessage(0);
            }else {
                System.out.println("删除笔记失败");
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
