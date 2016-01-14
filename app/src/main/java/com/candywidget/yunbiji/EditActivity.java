package com.candywidget.yunbiji;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑界面
 * Created by Candy on 2015/12/19.
 */
public class EditActivity extends Activity implements View.OnClickListener {
    private Article article;
    private boolean isNew = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        article = intent.getParcelableExtra("article");
        initView();
    }

    //初始化界面
    private EditText etContent;
    private EditText etTitle;

    private void initView() {
        etContent = (EditText) findViewById(R.id.et_content);
        etContent.setMovementMethod(LinkMovementMethod.getInstance());
        String content = article.getContent();
        SpannableString ss = new SpannableString(content);
        if (!content.equals("")) {
            isNew = false;
            Pattern pattern = Pattern.compile("/\\*.+?\\*/");
            Matcher matcher = pattern.matcher(article.getContent());
            while (matcher.find()) {
                ImageSpan imageSpan = null;
                MyClickableSpan clickableSpan = null;
                String s = matcher.group();
                String path = s.substring(2, s.length() - 2);
                if (path.endsWith("jpg")||path.endsWith("png")){
                    imageSpan = new ImageSpan(this,ThumbnailUtils.extractThumbnail(
                            BitmapFactory.decodeFile(path), 200, 300));
                    clickableSpan = new MyClickableSpan(path,"image/*");
                }else {
                    imageSpan = new ImageSpan(this,ThumbnailUtils.createVideoThumbnail(
                            path,MediaStore.Video.Thumbnails.MINI_KIND));
                    clickableSpan = new MyClickableSpan(path,"video/*");
                }
                ss.setSpan(imageSpan,matcher.start(),matcher.end(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(clickableSpan,matcher.start(),matcher.end(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        etContent.setText(ss);
        etTitle = (EditText) findViewById(R.id.et_title);
        etTitle.setText(article.getTitle());
        ImageView ivPic = (ImageView) findViewById(R.id.iv_pic);
        ivPic.setOnClickListener(this);
        ImageView ivVideo = (ImageView) findViewById(R.id.iv_shipin);
        ivVideo.setOnClickListener(this);
        ImageView ivDone = (ImageView) findViewById(R.id.iv_done);
        ivDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //插入图片
            case R.id.iv_pic:
                Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                getImage.setType("image/*");
                getImage.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(getImage, 1);
                break;
            //插入视频
            case R.id.iv_shipin:
                Intent getVideo = new Intent();
                getVideo.setAction(Intent.ACTION_GET_CONTENT);
                getVideo.setType("video/*");
                getVideo.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(getVideo, 2);
                break;
            //完成
            case R.id.iv_done:
                final String contentNew = etContent.getText().toString();
                final String titleNew = etTitle.getText().toString();
                DateFormat sDateFormat = SimpleDateFormat.getDateTimeInstance();
                String date = sDateFormat.format(new java.util.Date());
                final Article articleNew = new Article();
                articleNew.setUsername(article.getUsername());
                articleNew.setTitle(titleNew);
                articleNew.setTime(date);
                articleNew.setContent(contentNew);
                if (isNew) {
                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            Handler mHandler = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {

                                }
                            };
                            Intent i = new Intent(EditActivity.this, MainActivity.class);
                            startActivity(i);
                            Toast.makeText(EditActivity.this, "成功创建笔记！", Toast.LENGTH_SHORT).show();
                            UploadFile(contentNew, mHandler);
                        }
                    };
                    UploadThread thread = new UploadThread(articleNew, this, handler);
                    thread.start();
                } else {
                    articleNew.setId(article.getId());
                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            Handler mHandler = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {

                                }
                            };
                            UploadFile(contentNew, mHandler);
                            Intent i = new Intent(EditActivity.this, MainActivity.class);
                            startActivity(i);
                            Toast.makeText(EditActivity.this, "成功更新笔记！", Toast.LENGTH_SHORT).show();
                        }
                    };
                    UpdateThread thread = new UpdateThread(articleNew, this, handler);
                    thread.start();
                }
                break;
        }
    }

    //上传附件
    private void UploadFile(String contentNew, Handler handler) {
        Pattern pattern = Pattern.compile("/\\*.+?\\*/");
        Matcher matcher = pattern.matcher(contentNew);
        while (matcher.find()) {
            String s = matcher.group();
            String path = s.substring(2,s.length()-2);
            UploadFileThread thread = new UploadFileThread(
                    "http://1.meidaoyunbiji.sinaapp.com/uploadfile.php", new File(path), handler);
            thread.start();
        }
    }

    //接收数据，插入etArticle
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        String path = GetPath.getPath(this, data.getData());
        String pathWith = "/*"+path+"*/";
        SpannableString ss = new SpannableString(pathWith);
        ImageSpan imageSpan = null;
        ClickableSpan clickableSpan = null;
        switch (requestCode) {
            case 1:
                imageSpan = new ImageSpan(this, ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(path),200,300));
                clickableSpan = new MyClickableSpan(path,"image/*");
                break;
            case 2:
                imageSpan = new ImageSpan(this,ThumbnailUtils.createVideoThumbnail(path,
                        MediaStore.Video.Thumbnails.MINI_KIND));
                clickableSpan = new MyClickableSpan(path,"video/*");
                break;
        }
        assert path != null;
        ss.setSpan(imageSpan,0,pathWith.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan,ss.getSpanStart(imageSpan),ss.getSpanEnd(imageSpan),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (etContent.getSelectionStart() == -1){
            etContent.append(ss);
        }else {
            etContent.getText().insert(etContent.getSelectionStart(),ss);
        }
    }

    class MyClickableSpan extends ClickableSpan {
        String path;
        String type;
        private MyClickableSpan(String path, String type){
            this.path = path;
            this.type = type;
        }

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(path)),type);
            startActivity(intent);
        }
    }
}