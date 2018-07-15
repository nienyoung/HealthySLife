package com.example.admin.healthyslife_android.music;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;

import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.adapter.MyAdapter;
import com.example.admin.healthyslife_android.bean.Song;
import com.example.admin.healthyslife_android.utils.MusicUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private Button isPlay;
    private Button previous;
    private Button next;

    // private ObjectAnimator animator;
    private int flag = 0;

    private TextView totalTime;
    private TextView playingTime;
    private SeekBar seekBar;

    private ListView mListView;
    private List<Song> list;
    private MyAdapter adapter;
    private int currentSongIndex;

    private TextView playingSong;
    private TextView playingSinger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOverflowMenu();
        setContentView(R.layout.activity_music);

        bindServiceConnection();
        musicService = new MusicService();

        initView();
        initListView();
        setListener();

        //默认播放第一首
        musicService.resetPlayer();
       String firstPath = list.get(0).path;
        musicService.initPlayer(firstPath);
        playingSong.setText(list.get(0).song);
        playingSinger.setText(list.get(0).singer);


        //设置列表循环播放
        musicService.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                currentSongIndex++;
                if (currentSongIndex >= list.size()) {
                    currentSongIndex = 0;
                }
                seekBar.setProgress(0);
                play();
            }
        });

    }

    private void initView(){
        isPlay = (Button) findViewById(R.id.isPlayButton);
        previous = (Button) findViewById(R.id.previousButton);
        next = (Button) findViewById(R.id.nextButton);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());

        totalTime = (TextView) findViewById(R.id.totalTime);
        playingTime = (TextView) findViewById(R.id.playingTime);
        playingSong = (TextView) findViewById(R.id.playingSong);
        playingSinger = (TextView) findViewById(R.id.playingSinger);
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.main_listview);
        list = new ArrayList<>();
        //把扫描到的音乐赋值给list
        list = MusicUtils.getMusicData(this);
        adapter = new MyAdapter(this,list);
        mListView.setAdapter(adapter);
    }

    private void setListener(){
        isPlay.setOnClickListener(new myOnClickListener());
        previous.setOnClickListener(new myOnClickListener());
        next.setOnClickListener(new myOnClickListener());
        InnerItemOnCLickListener listener = new InnerItemOnCLickListener();
        //给 ListView 添加点击（点击音乐即可播放）
       mListView.setOnItemClickListener(listener);
    }

    private class InnerItemOnCLickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //获取点击的列表的中音乐的位置，赋值给当前播放音乐
            currentSongIndex = position;
            //令暂停的进度为0（即为从头播放）
            seekBar.setProgress(0);
            //播放
            play();
        }
    }

    private class myOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.isPlayButton:
                    musicService.playOrPause();
                    //setPlayButton();
                    break;
                case R.id.previousButton:
                    previous();
                    break;
                case R.id.nextButton:
                    next();
                    break;
                default:
                    break;
            }
        }
    }

    private void previous() {

            //当前音乐播放位置--（上一曲）
            currentSongIndex--;
            if (currentSongIndex < 0) {
                currentSongIndex = list.size() - 1;
            }
                seekBar.setProgress(0);
                play();
    }

    private void next() {

        //当前音乐播放位置--（上一曲）
        currentSongIndex++;
        if (currentSongIndex >= list.size()) {
            currentSongIndex = 0;
        }
            seekBar.setProgress(0);
            play();
    }


    private void setPlayButton(){
        if(musicService.mediaPlayer.isPlaying()){
            isPlay.setText("Pause");
            //animator.pause();
        } else {
            isPlay.setText("Play");

        }
    }


    private void play() {
        //重置
        musicService.resetPlayer();
        try {
            //设置音乐文件来源
            Song cur = list.get(currentSongIndex);
            musicService.playNewSong(cur.path);
            playingSong.setText(cur.song);
            playingSinger.setText(cur.singer);
            //setPlayButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private MusicService musicService;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            setPlayButton();

            playingTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            totalTime.setText(time.format(musicService.mediaPlayer.getDuration()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            handler.postDelayed(runnable, 100);
        }
    };

    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onPause(){
        super.onPause();
        if(isApplicationBroughtToBackground()) {
            musicService.isReturnTo = 1;
            Log.e("b","后台中");
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        musicService.isReturnTo = 1;
    }

    @Override
    protected void onResume() {

        verifyStoragePermissions(this);

        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());
        handler.post(runnable);
        super.onResume();
        Log.d("hint", "handler post runnable");
    }


    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }

    //这里是在登录界面label上右上角添加三个点，里面可添加其他功能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
