package com.candywidget.yunbiji;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Article类
 * Created by Administrator on 2015/12/17.
 */
public class Article implements Parcelable {
    private String username;
    private int id;
    private String title;
    private String time;
    private String content;

    public static final Parcelable.Creator<Article> CREATOR = new Creator<Article>() {
        public Article createFromParcel(Parcel source) {
            Article article = new Article();
            article.username = source.readString();
            article.id = source.readInt();
            article.title = source.readString();
            article.time = source.readString();
            article.content = source.readString();
            return article;
        }
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeString(content);
    }
}
