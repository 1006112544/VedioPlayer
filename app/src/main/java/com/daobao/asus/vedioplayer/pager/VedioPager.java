package com.daobao.asus.vedioplayer.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.daobao.asus.vedioplayer.Player.SystemVedioPlayer;
import com.daobao.asus.vedioplayer.R;
import com.daobao.asus.vedioplayer.domain.MedioItem;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by ASUS on 2017/6/27.
 */

public class VedioPager extends BasePager {
    ListView vedio_listview;
    TextView vedio_textview;
    ProgressBar vedio_pb;
    public static ArrayList<MedioItem> medioItems;

    public VedioPager(Context context) {
        super(context);
    }

    public View initView() {
        View view = View.inflate(context, R.layout.vedio, null);
        vedio_listview = view.findViewById(R.id.vedio_listview);
        vedio_textview = view.findViewById(R.id.vedio_textview);
        vedio_pb = view.findViewById(R.id.vedio_pb);
        //设置点击事件
        vedio_listview.setOnItemClickListener(new MyOnItemClickListener());
        vedio_listview.setOnItemLongClickListener(new MyOnItemLongClickListener());
        return view;
    }
    //Item长按监听事件
    class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener
    {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            Mydialog(position);
            return true;
        }
    }
    //Item点击事件监听器
    class MyOnItemClickListener implements AdapterView.OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            MedioItem medioItem = medioItems.get(position);
            Toast.makeText(context,"开始播放",Toast.LENGTH_SHORT).show();
            //显示意图播放
            Intent intent = new Intent(context,SystemVedioPlayer.class);
            intent.putExtra("vedioActivity",position);
            intent.putExtra("FromPager","VideoPager");
            intent.setDataAndType(Uri.parse(medioItem.getData()),"video/*");
            context.startActivity(intent);
        }
    }


    private Handler handler = new Handler() {
        //接受消息
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (medioItems != null && medioItems.size() > 0) {
                //有数据设置适配器
                VideoPagerAdapter vedioPagerAdapter = new VideoPagerAdapter();
                vedio_listview.setAdapter(vedioPagerAdapter);
            } else {
                //没数据
                vedio_textview.setVisibility(View.VISIBLE);
            }
            vedio_pb.setVisibility(View.GONE);
        }
    };

    public void initDate() {
        super.initDate();
        Log.d("本地视频", "本地视频数据更新");
        //加载数据
        getDataFromLocal();
    }

    /**
     * 从本地SD卡得到数据
     * 方法一 遍历SDcard 速度慢不考虑
     * 方法二 内容数据库获得数据
     */
    private void getDataFromLocal() {
        new Thread() {
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                if(resolver!=null)
                {
                    Log.d("123", "resolver != null");
                }
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//文件名称
                        MediaStore.Video.Media.DURATION,//视频长短
                        MediaStore.Video.Media.SIZE,//视频大小
                        MediaStore.Video.Media.DATA,//视频绝对路径
                        MediaStore.Video.Media.ARTIST//艺术家
                };
                //访问数据返回一个游标
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    medioItems = new ArrayList<MedioItem>();
                    while (cursor.moveToNext()) {
                        MedioItem medioItem = new MedioItem();
                        String name = cursor.getString(0);
                        medioItem.setName(name);
                        long duration = cursor.getLong(1);
                        medioItem.setDuration(duration);
                        long size = cursor.getLong(2);
                        medioItem.setSize(size);
                        String data = cursor.getString(3);
                        medioItem.setData(data);
                        String artist = cursor.getString(4);
                        medioItem.setArtist(artist);
                        medioItems.add(medioItem);
                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(1);
            }
        }.start();
    }
    class VideoPagerAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            //文件个数
            return medioItems.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = new ViewHolder();
            if(view == null)
            {
                view = View.inflate(context,R.layout.vedio_item_layout,null);
                viewHolder.vedio_name = (TextView) view.findViewById(R.id.vedio_name);
                viewHolder.vedio_size = (TextView) view.findViewById(R.id.vedio_size);
                view.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) view.getTag();
            }
            MedioItem medioItem = medioItems.get(position);
            viewHolder.vedio_name.setText(medioItem.getName());
            viewHolder.vedio_size.setText(Formatter.formatFileSize(context,medioItem.getSize()));
            return view;
        }
    }
    class ViewHolder {
        TextView vedio_name;
        TextView vedio_size;
    }
    /**
     * 自定义dialog
     */
    private void Mydialog(final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(context,R.style.dialog);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("是否确认删除?"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(medioItems.get(position).getData())) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(medioItems.get(position).getData());
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    context.sendBroadcast(intent);
                    medioItems.remove(position);
                    file.delete();
                    initDate();
                    if(file.exists())
                    {
                        Toast.makeText(context, "该文件不可删除", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                        medioItems.remove(position);
                        vedio_listview.setAdapter(new VideoPagerAdapter());
                    }
                }
                dialog.dismiss(); //关闭dialog
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(context, "取消" , Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("忽略", new DialogInterface.OnClickListener() {//设置忽略按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(context, "忽略" , Toast.LENGTH_SHORT).show();
            }
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }
}
