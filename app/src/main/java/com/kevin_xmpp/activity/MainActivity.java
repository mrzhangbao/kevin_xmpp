package com.kevin_xmpp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.kevin_xmpp.R;
import com.kevin_xmpp.adapter.MainPagerAdapter;
import com.kevin_xmpp.fragment.ContactsFragment;
import com.kevin_xmpp.fragment.SessionFragment;
import com.kevin_xmpp.utils.ToolBarUtils;
import com.zhy.android.percent.support.PercentLinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private ToolBarUtils mToolBarUtils;
    private String[] mToolBarTitleArrays = {"消息", "好友"};

    @Bind(R.id.main_viewPager)
    ViewPager mMainViewPager;
    @Bind(R.id.main_bottom)
    PercentLinearLayout mMianBottom;
    @Bind(R.id.main_Title)
    TextView mMainTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initChangeListener();

    }



    private void initChangeListener() {
        mMainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mToolBarUtils.changeIconColor(position);
                //修改Title
                mMainTitle.setText(mToolBarTitleArrays[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mToolBarUtils.setmOnToolBarClickListener(new ToolBarUtils.OnToolBarClickListener() {
            @Override
            public void onToolBarClick(int position) {
                mMainViewPager.setCurrentItem(position);
            }
        });
    }

    private void initData() {
        //viewPager -->fragment--?fragmentPagerAdapter
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactsFragment());
        mMainViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), mFragments));

        //底部的按钮
        mToolBarUtils = new ToolBarUtils();

        //底部图标
        int[] iconArrs = {R.drawable.selector_meassage, R.drawable.selector_selfinfo};
        mToolBarUtils.createToolBar(mMianBottom, mToolBarTitleArrays, iconArrs);
        //设置默认选定状态
        mToolBarUtils.changeIconColor(0);
    }
}
