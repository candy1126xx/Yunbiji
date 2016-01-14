package com.candywidget.yunbiji;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private ViewPager container;
    private Fragment[] fragments;
    private RadioButton[] buttons;

    private String username;
    private void initView() {
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        BookFragment bookFragment = BookFragment.newInstance(username);
        SettingFragment settingFragment = SettingFragment.newInstance();
        fragments = new Fragment[]{bookFragment, settingFragment};
        RadioButton btnBook = (RadioButton) findViewById(R.id.btn_book);
        btnBook.setOnClickListener(this);
        btnBook.setChecked(true);
        RadioButton btnSetting = (RadioButton) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);
        buttons = new RadioButton[]{btnBook, btnSetting};
        container = (ViewPager) findViewById(R.id.container);
        container.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        });
        container.setCurrentItem(0);
        container.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        buttons[position].setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_book:
                container.setCurrentItem(0);
                break;
            case R.id.btn_setting:
                container.setCurrentItem(1);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initView();
    }
}
