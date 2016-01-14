package com.candywidget.yunbiji;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 笔记列表界面
 * Created by Candy on 2015/12/14.
 */
public class BookFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ImageView ivLoginState;//显示登录状态
    private String username = ""; //用户名
    private ImageView ivRefresh; //刷新按钮
    private Animation animRefresh; //刷新动画
    private boolean isStopRefresh = false;//是否停止刷新动画
    private ListView lvArticle; //笔记列表
    private ImageView ivNewFile;//新建笔记按钮
    private Context context;
    private int ti = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);
        context = getActivity();
        username = getArguments().getString("username");

        if (ti == 1){
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    isStopRefresh = true;
                    ivRefresh.setClickable(true);
                    switch (msg.what) {
                        case 0:
                            initSQLite();
                            lvArticle.setAdapter(adapter);
                            Toast.makeText(context, "同步数据成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(context, "刷新失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(context, "没有需要更新的数据", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
            DownloadThread thread = new DownloadThread(context, username, handler);
            thread.start();
            ti++;
        }

        initSQLite();
        initAnimRefresh();
        ivLoginState = (ImageView) view.findViewById(R.id.tv_login_state);
        ivLoginState.setBackgroundResource(R.drawable.login_success);
        ivLoginState.setOnClickListener(this);
        ivRefresh = (ImageView) view.findViewById(R.id.iv_refresh);
        ivNewFile = (ImageView) view.findViewById(R.id.new_file);
        ivNewFile.setOnClickListener(this);
        ivRefresh.setOnClickListener(this);
        lvArticle = (ListView) view.findViewById(R.id.article_list);
        lvArticle.setAdapter(adapter);
        lvArticle.setOnItemClickListener(this);
        lvArticle.setOnItemLongClickListener(this);

        return view;
    }

    //本地笔记数据库绑定到Adapter
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private void initSQLite() {
        list.clear();
        SQLiteDatabase dbArticle = context.openOrCreateDatabase("Articles.db3", context.MODE_PRIVATE, null);
        dbArticle.execSQL("CREATE TABLE IF NOT EXISTS articles(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT,time TEXT,content TEXT,username TEXT)");
        Cursor cursor = dbArticle.rawQuery("SELECT * FROM articles WHERE username=?", new String[]{username});
        try {
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("title", cursor.getString(cursor.getColumnIndex("title")));
                map.put("time", "写于" + cursor.getString(cursor.getColumnIndex("time")));
                System.out.println("笔记ID："+cursor.getInt(cursor.getColumnIndex("id")));
                list.add(map);
            }
            adapter = new SimpleAdapter(context, list, R.layout.article_list_item,
                    new String[]{"title", "time"}, new int[]{R.id.article_title, R.id.article_time});
        } finally {
            cursor.close();
            dbArticle.close();
        }
    }

    //初始化刷新动画
    private void initAnimRefresh() {
        animRefresh = AnimationUtils.loadAnimation(context, R.anim.anim_refresh);
        animRefresh.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!isStopRefresh) {
                    ivRefresh.startAnimation(animRefresh);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //创建Fragment实例
    public static BookFragment newInstance(String username) {
        Bundle args = new Bundle();
        args.putString("username", username);
        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //刷新
            case R.id.iv_refresh:
                isStopRefresh = false;
                ivRefresh.startAnimation(animRefresh);
                ivRefresh.setClickable(false);
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        isStopRefresh = true;
                        ivRefresh.setClickable(true);
                        switch (msg.what) {
                            case 0:
                                initSQLite();
                                lvArticle.setAdapter(adapter);
                                Toast.makeText(context, "同步数据成功", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Toast.makeText(context, "刷新失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Toast.makeText(context, "没有需要更新的数据", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                DownloadThread thread = new DownloadThread(context, username, handler);
                thread.start();
                break;
            //新建
            case R.id.new_file:
                Intent intent = new Intent(getActivity(), EditActivity.class);
                Article article = new Article();
                article.setUsername(username);
                article.setId(-1);
                article.setTitle("");
                article.setTime("");
                article.setContent("");
                intent.putExtra("article",article);
                startActivity(intent);
                break;
            case R.id.tv_login_state:

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SQLiteDatabase dbArticle = context.openOrCreateDatabase("Articles.db3", context.MODE_PRIVATE, null);
        Cursor cursor = dbArticle.rawQuery("SELECT * FROM articles WHERE username=?", new String[]{username});
        try {
            cursor.moveToPosition(position);
            Intent intent = new Intent(getActivity(), EditActivity.class);
            Article article = new Article();
            article.setUsername(username);
            article.setId(cursor.getInt(cursor.getColumnIndex("id")));
            article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            article.setTime(cursor.getString(cursor.getColumnIndex("time")));
            article.setContent(cursor.getString(cursor.getColumnIndex("content")));
            intent.putExtra("article", article);
            startActivity(intent);
        } finally {
            cursor.close();
            dbArticle.close();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        Intent intent = new Intent(getActivity(),DeleteActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}
