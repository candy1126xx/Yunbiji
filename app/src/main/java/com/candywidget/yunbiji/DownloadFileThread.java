package com.candywidget.yunbiji;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileThread extends Thread{
    private String username;
    private String title;
    private String fileName;
    private android.os.Handler handler;

    public DownloadFileThread(String username, String title,String fileName, android.os.Handler handler){
        this.username = username;
        this.title = title;
        this.fileName = fileName;
        this.handler = handler;
    }

    @Override
    public void run() {
        System.out.println(">>>>>>>>>>>>>>>>Thread starts<<<<<<<<<<<<<<<");
        try {
            URL url = new URL("http://192.168.0.101/uploads/"+fileName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(3000);
            InputStream is = connection.getInputStream();

            System.out.println(">>>>>>>>>>>>>>>>InputStream opened<<<<<<<<<<<<<<<");

            String dirPath = Environment.getExternalStorageDirectory().getPath()+
                    "/meidao/"+username+"/"+title;
            File dir = new File(dirPath);
            if (dir.mkdirs()){
                System.out.println(">>>>>>>>>>>>>>>>>>>Dir created<<<<<<<<<<<<<<<<<<<<<");
            }
            File file = new File(dirPath+"/"+fileName);
            if (file.exists()){
                System.out.println(">>>>>>>>>>>>>>>>>File exists<<<<<<<<<<<<<<<<<<");
            }else if (file.createNewFile()){
                System.out.println(">>>>>>>>>>>>>>>>>>>File created<<<<<<<<<<<<<<<<<");
            }
            FileOutputStream fos = new FileOutputStream(file);

            System.out.println(">>>>>>>>>>>>>>>>OutputStream opened<<<<<<<<<<<<<<<");

            byte[] bytes = new byte[1024];
            while ((is.read(bytes)) != -1){
                fos.write(bytes);
            }
            System.out.println(">>>>>>>>>>>>>>>>Done<<<<<<<<<<<<<<<");
            handler.sendEmptyMessage(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
