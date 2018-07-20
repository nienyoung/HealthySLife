package com.example.admin.healthyslife_android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.admin.healthyslife_android.music.MusicActivity;
import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.bean.Song;
import com.example.admin.healthyslife_android.utils.MusicUtils;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {
    private Context context;
    private List<Song> list;
    public MusicListAdapter(MusicActivity musicActivity, List<Song> list) {
        this.context = musicActivity;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            //引入布局
            view = View.inflate(context, R.layout.item_music_listview, null);
            //实例化对象
            holder.song = (TextView) view.findViewById(R.id.item_mymusic_song);
            holder.singer = (TextView) view.findViewById(R.id.item_mymusic_singer);
            holder.duration = (TextView) view.findViewById(R.id.item_mymusic_duration);
            holder.position = (TextView) view.findViewById(R.id.item_mymusic_postion);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if(list.get(i)==null) {

            if(i<list.size()-1) {
                holder.song.setText(null);
            }else{
                if(list.size()==2)
                holder.song.setText("当前手机中暂无音乐,无法进行播放。");
                else
                    holder.song.setText(null);
            }
            holder.singer.setText(null);
            holder.duration.setText(null);
            holder.position.setText(null);

        }else {
            //给控件赋值
            holder.song.setText(list.get(i).song.toString());
            holder.singer.setText(list.get(i).singer.toString());
            //时间需要转换一下
            int duration = list.get(i).duration;
            String time = MusicUtils.formatTime(duration);
            holder.duration.setText(time);
            holder.position.setText(i+1+"");
        }

        return view;
    }
    class ViewHolder{
        TextView song;//歌曲名
        TextView singer;//歌手
        TextView duration;//时长
        TextView position;//序号
    }

}
