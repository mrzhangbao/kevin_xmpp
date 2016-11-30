package com.kevin_xmpp.utils;

import android.view.View;
import android.widget.TextView;

import com.kevin_xmpp.R;
import com.zhy.android.percent.support.PercentLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benson_Tom on 2016/6/26.
 */
public class ToolBarUtils {

    private List<TextView> mTextViews = new ArrayList<>();

    public void createToolBar(PercentLinearLayout container, String[] toolBarTitleArrs, int[] iconArrs) {
        for (int i = 0; i < toolBarTitleArrs.length; i++) {
            TextView tv = (TextView) View.inflate(container.getContext(), R.layout.inflate_toolbar_btn, null);
            tv.setText(toolBarTitleArrs[i]);
            //动态修改TextView里面的drawableTop属性
            tv.setCompoundDrawablesWithIntrinsicBounds(0,iconArrs[i],0,0);

            int width = 0;
            int height = PercentLinearLayout.LayoutParams.MATCH_PARENT;
            PercentLinearLayout.LayoutParams params = new PercentLinearLayout.LayoutParams(width,height);
            params.weight = 1;//设置weight
            container.addView(tv,params);
            mTextViews.add(tv);//保存TextView到集合中
            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //3.需要传值的地方，用接口对象调用接口方法
                    mOnToolBarClickListener.onToolBarClick(finalI);
                }
            });
        }
    }

    //1.创建接口和接口方法
    public interface OnToolBarClickListener{
        void onToolBarClick(int position);
    }
    //2.定义接口变量
    OnToolBarClickListener mOnToolBarClickListener;

    //4.暴露一个公共方法供外部调用


    public void setmOnToolBarClickListener(OnToolBarClickListener mOnToolBarClickListener) {
        this.mOnToolBarClickListener = mOnToolBarClickListener;
    }

    public void changeIconColor(int position){
        //还原初始化颜色
        for (TextView tv : mTextViews){
            tv.setSelected(false);
        }
        mTextViews.get(position).setSelected(true); //通过设置selected 属性来设定选定效果
    }
}
