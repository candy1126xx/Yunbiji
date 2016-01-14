package com.candywidget.yunbiji;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadFileThread extends Thread{
    private String uploadUrl;
    private File file;
    private Handler handler;

    public UploadFileThread(String uploadUrl, File file, Handler handler) {
        this.uploadUrl = uploadUrl;
        this.file = file;
        this.handler = handler;
    }

    @Override
    public void run() {
        String end = "\r\n";
        String boundary = "******";

        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            // 使用POST方法
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes("--"+boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+file.getName()+"\"" + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();
            dos.writeBytes(end);
            dos.writeBytes("--"+boundary +"--"+ end);
            dos.flush();
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = "";
            String line;
            while ((line = br.readLine()) != null){
                result += line;
            }
            System.out.println(result);
            handler.sendEmptyMessage(1);
            dos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
