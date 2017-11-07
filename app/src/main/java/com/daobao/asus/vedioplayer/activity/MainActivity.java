package com.daobao.asus.vedioplayer.activity;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.daobao.asus.vedioplayer.R;
import com.daobao.asus.vedioplayer.pager.AudioPager;
import com.daobao.asus.vedioplayer.pager.BasePager;
import com.daobao.asus.vedioplayer.pager.NetActivity;
import com.daobao.asus.vedioplayer.pager.NetVedioActivity;
import com.daobao.asus.vedioplayer.pager.SearchPager;
import com.daobao.asus.vedioplayer.pager.VedioPager;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends FragmentActivity implements View.OnClickListener{
    /**
     * 选中的位子
     */
    private int position;
    private FrameLayout main_content;
    private ArrayList<BasePager> basePagers;
    private boolean hadPower = false;//是否拥有权限
    private TextView vedio_textView;
    private ProgressBar vedio_progressBar;
    private TextView audio_textView;
    private ProgressBar audio_progressBar;
    private ImageButton search;
    private LinearLayout search_bar;
    private EditText search_edit;
    private LinearLayout titleBar;
    private ImageView main_vedio_togglebutton;
    private ImageView main_music_togglebutton;
    private ImageView main_netvedio_togglebutton;
    private ImageView main_net_togglebutton;
    private LinearLayout main_activity_linearlayout;
    RadioGroup rg_tap;
    public static AgentApplication agentApplication;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        agentApplication = new AgentApplication();
        agentApplication.addActivity(this);
        basePagers = new ArrayList<>();
        basePagers.add(new VedioPager(this));//添加本地视频页面-0
        basePagers.add(new AudioPager(this));//添加本地视频页面-1
        //接收SplashActivity传过来的参数
        Bundle bundle = this.getIntent().getExtras();
        hadPower = bundle.getBoolean("SplashActivity");
        rg_tap.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        //初始化数据
        rg_tap.check(R.id.main_music);
        rg_tap.check(R.id.main_vedio);
    }
    public void init()
    {
        main_content =(FrameLayout) findViewById(R.id.main_content);
        rg_tap = (RadioGroup) findViewById(R.id.rg_tap);
        vedio_textView = findViewById(R.id.vedio_textview);
        vedio_progressBar = findViewById(R.id.vedio_pb);
        audio_textView = findViewById(R.id.audio_textview);
        audio_progressBar = findViewById(R.id.audio_pb);
        search = findViewById(R.id.search);
        search_edit = findViewById(R.id.search_edit);
        search_bar = findViewById(R.id.search_bar);
        titleBar = findViewById(R.id.titleBar);
        main_net_togglebutton = findViewById(R.id. main_net_togglebutton);
        main_netvedio_togglebutton = findViewById(R.id. main_netvedio_togglebutton);
        main_music_togglebutton = findViewById(R.id. main_music_togglebutton);
        main_vedio_togglebutton = findViewById(R.id. main_vedio_togglebutton);
        main_activity_linearlayout = findViewById(R.id.main_activity_linearlayout);
        search.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.search)
        {
            if(!search_edit.getText().equals(""))
            {
                Intent intent = new Intent(this,SearchPager.class);
                intent.putExtra("Search",search_edit.getText()+"");
                startActivity(intent);
                search_edit.setText("");
            }
        }
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener
    {
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch (checkedId)
            {
                default:
                    titleBar.setVisibility(View.VISIBLE);
                    position=0;
                    setFragment();
                    break;
                case R.id.main_music:
                    titleBar.setVisibility(View.VISIBLE);
                    position=1;
                    setFragment();
                    break;
                case R.id.main_netvedio:
                    position=2;
                    Intent intent = new Intent(MainActivity.this, NetVedioActivity.class);
                    startActivity(intent);
                    //重置选择项
                    rg_tap.check(R.id.main_vedio);
                    break;
                case R.id.main_net:
                    position=3;
                    Intent intent2 = new Intent(MainActivity.this, NetActivity.class);
                    startActivity(intent2);
                    //重置选择项
                    rg_tap.check(R.id.main_vedio);
                    break;
            }
            setToggleButton();
        }

    }
    /**
     * 把页面添加到Fragment中
     */
    public void setFragment()
    {
        //1.得到FragmentManager
        FragmentManager manager = getSupportFragmentManager();
        //2.开启事物
        FragmentTransaction ft = manager.beginTransaction();
        //3.替换
        ft.replace(R.id.main_content,new ReplaceFragment(getBasePager()));
        //4.提交事务
        ft.commit();
    }
    /**
     * 根据位子的到页面
     */
    public BasePager getBasePager()
    {
        BasePager basePager  = basePagers.get(position);
        if(!basePager.isInitDate&&basePager!=null&&hadPower)
        {
            basePager.initDate();//绑定数据
            basePager.isInitDate = true;
        }
        else if(!hadPower)
        {
            vedio_progressBar.setVisibility(View.GONE);
            vedio_textView.setVisibility(View.VISIBLE);
            vedio_textView.setText("没有权限");
            audio_progressBar.setVisibility(View.GONE);
            audio_textView.setVisibility(View.VISIBLE);
            audio_textView.setText("没有权限");
        }
        return basePager;
    }

    /**
     * 用于退出整个程序
     */
    public class AgentApplication extends Application {

        private List<Activity> activities = new ArrayList<Activity>();

        public void addActivity(Activity activity) {
            activities.add(activity);
        }

        @Override
        public void onTerminate() {
            super.onTerminate();

            for (Activity activity : activities) {
                activity.finish();
            }
            onDestroy();
            System.exit(0);
        }
    }
    private void setToggleButton()
    {
        //全部取消显示
        main_vedio_togglebutton.setImageResource(R.drawable.togglebutton_shape);
        main_music_togglebutton.setImageResource(R.drawable.togglebutton_shape);
        switch (position)
        {
            case 0:
                main_vedio_togglebutton.setImageResource(R.drawable.togglebutton_shape_selected);
                break;
            case 1:
                main_music_togglebutton.setImageResource(R.drawable.togglebutton_shape_selected);
                break;
        }
    }
}