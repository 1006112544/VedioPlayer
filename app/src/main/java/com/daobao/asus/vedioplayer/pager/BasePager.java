package com.daobao.asus.vedioplayer.pager;

import android.content.Context;
import android.view.View;

/**
 * Created by ASUS on 2017/6/27.
 * 所有页面的父类
 */

public abstract class BasePager {
    public View rootview;
    Context context;
    public boolean isInitDate = false;
    public BasePager(Context context)
    {
        this.context = context;
        rootview = initView();
    }

    /**
     *子页面需要实现该方法来显示不同内容
     */
    public abstract View initView();

    /**
     * 当子页面需要初始化数据，联网请求数据，或者绑定数据的时候重写该方法。
     */
    public void initDate()
    {

    }
}
