package com.candywidget.yunbiji;

import java.util.ArrayList;

/**
 * 请求下载笔记的Bean
 * Created by Administrator on 2015/12/17.
 */
public class DownloadSendBean {
    private String username;
    private ArrayList<Integer> id;

    public DownloadSendBean(String username, ArrayList<Integer> id){
        this.username = username;
        this.id = id;
    }

    public ArrayList<Integer> getId() {
        return id;
    }

    public void setId(ArrayList<Integer> id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
