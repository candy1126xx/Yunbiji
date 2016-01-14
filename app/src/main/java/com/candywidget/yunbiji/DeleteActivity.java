package com.candywidget.yunbiji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Candy on 2015/12/21.
 */
public class DeleteActivity extends Activity implements View.OnClickListener{
    private ArrayList<Integer> idList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        needDelete.clear();
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        list.clear();
        Context context = DeleteActivity.this;
        SQLiteDatabase dbArticle = context.openOrCreateDatabase("Articles.db3", context.MODE_PRIVATE, null);
        Cursor cursor = dbArticle.rawQuery("SELECT * FROM articles WHERE username=?", new String[]{username});
        try {
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("title", cursor.getString(cursor.getColumnIndex("title")));
                map.put("time", "写于" + cursor.getString(cursor.getColumnIndex("time")));
                list.add(map);
                idList.add(cursor.getInt(cursor.getColumnIndex("id")));
            }
        } finally {
            cursor.close();
            dbArticle.close();
        }

        initView();
    }

    private ListView lvDelete;
    private ImageView ivDelete;

    private void initView() {
        lvDelete = (ListView) findViewById(R.id.list_delete);
        ivDelete = (ImageView) findViewById(R.id.iv_delete);

        MyAdapter adapter = new MyAdapter(DeleteActivity.this);
        lvDelete.setAdapter(adapter);
        ivDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(DeleteActivity.this, "成功删除笔记", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeleteActivity.this,MainActivity.class);
                startActivity(intent);
            }
        };
        DeleteThread thread = new DeleteThread(DeleteActivity.this, username, needDelete,handler);
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    private ArrayList<Integer> needDelete = new ArrayList<>();

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater inflater = null;

        public MyAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.article_list_item_delete, null);
            TextView tvTitle = (TextView) view.findViewById(R.id.article_title_delete);
            TextView tvTime = (TextView) view.findViewById(R.id.article_time_delete);
            final CheckBox cbDelete = (CheckBox) view.findViewById(R.id.cb_delete);
            tvTitle.setText(list.get(position).get("title"));
            tvTime.setText(list.get(position).get("time"));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cbDelete.isChecked()) {
                        cbDelete.setChecked(false);
                        needDelete.remove(idList.get(position));
                        System.out.println("需要删除的笔记有：" + needDelete.size()+"个");
                    } else {
                        cbDelete.setChecked(true);
                        needDelete.add(idList.get(position));
                        System.out.println("需要删除的笔记有：" + needDelete.size()+"个");
                    }
                }
            });
            return view;
        }
    }
}
