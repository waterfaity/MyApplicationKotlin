package com.waterfaity.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2019/4/25 19:03
 * @info:
 */
public class MusicPlayService extends Service {
    MyBinder myBinder;
    public static final String ACTION_PLAY = "music_play";//播放
    public static final String ACTION_PLAY_OR_PAUSE = "music_play_or_pause";//播放或暂停
    public static final String ACTION_PLAY_SEEK = "music_play_seek";//seek
    public static final String ACTION_PLAY_PRE = "music_play_pre";//播放前一首
    public static final String ACTION_PLAY_NEXT = "music_play_next";//播放后一首
    public static final String ACTION_STOP = "music_stop";//暂停
    public static final String ACTION_RELEASE = "music_release";//释放
    public static final String ACTION_PLAY_LIST = "music_play_list";//播放列表
    public static final String ACTION_PLAY_TYPE = "music_play_type";//播放类型
    public static final String ACTION_PLAY_SPEED = "music_play_speed";//倍速

    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_DATA_2 = "data2";

    public static final int PLAY_TYPE_PLAY_SINGLE = 1;
    public static final int PLAY_TYPE_PLAY_ALL = 2;
    public static final int PLAY_TYPE_LOOP_SINGLE = 3;
    public static final int PLAY_TYPE_LOOP_ALL = 4;


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("log", "----");
        return myBinder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && !TextUtils.isEmpty(action)) {
                switch (action) {
                    case ACTION_PLAY:
                        myBinder.play(intent.getStringExtra(EXTRA_DATA), intent.getIntExtra(EXTRA_DATA_2, 0));
                        break;
                    case ACTION_PLAY_OR_PAUSE:
                        myBinder.playOrPause();
                        break;
                    case ACTION_PLAY_SEEK:
                        myBinder.playSeek(intent.getIntExtra(EXTRA_DATA, 0));
                        break;
                    case ACTION_PLAY_PRE:
                        myBinder.playPre();
                        break;
                    case ACTION_PLAY_NEXT:
                        myBinder.playNext();
                        break;
                    case ACTION_STOP:
                        myBinder.playStop();
                        break;
                    case ACTION_RELEASE:
                        myBinder.release();
                        break;
                    case ACTION_PLAY_LIST:
                        myBinder.setPlayList(intent.getStringArrayListExtra(EXTRA_DATA));
                        break;
                    case ACTION_PLAY_TYPE:
                        myBinder.setPlayType(intent.getIntExtra(EXTRA_DATA, PLAY_TYPE_PLAY_SINGLE));
                        break;
                    case ACTION_PLAY_SPEED:
                        myBinder.setPlaySpeed(intent.getFloatExtra(EXTRA_DATA, 1F));
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public class MyBinder extends Binder {
        private Mp3Player.onMp3PlayListener onMp3PlayListener;
        private ArrayList<String> playList;
        private int currentPos;
        private String currentUrl;
        private int playType;
        private Mp3Player mp3Player;


        public void play(String url) {
            play(url, -1);
        }

        public void play(String url, int seek) {
            this.currentUrl = url;
            initPos();
            initPlayer();
            mp3Player.play(currentUrl, seek);
        }

        private void initPlayer() {
            if (mp3Player == null) {
                mp3Player = new Mp3Player();
                mp3Player.setOnMp3PlayListener(new Mp3Player.onMp3PlayListener() {
                    @Override
                    public void onMp3PlayError(int state, String message) {
                        if (onMp3PlayListener != null)
                            onMp3PlayListener.onMp3PlayError(state, message);
                    }

                    @Override
                    public void onPlayStateChanged(int state, String message) {
                        if (onMp3PlayListener != null)
                            onMp3PlayListener.onPlayStateChanged(state, message);
                    }

                    @Override
                    public void OnPlaying(int current, int total) {
                        if (onMp3PlayListener != null)
                            onMp3PlayListener.OnPlaying(current, total);
                    }
                });
            }
        }

        /**
         * 定位当前播放pos
         */
        private void initPos() {
            if (playList == null || TextUtils.isEmpty(this.currentUrl)) currentPos = 0;
            else for (int i = 0; i < playList.size(); i++) {
                if (TextUtils.equals(playList.get(i), this.currentUrl)) {
                    currentPos = i;
                    return;
                }
            }
        }

        public void playOrPause() {
            if (mp3Player.getPlayState() == Mp3Player.PAUSE || mp3Player.getPlayState() == Mp3Player.COMPLETE)
                mp3Player.play(currentUrl);
            else if (mp3Player.getPlayState() == Mp3Player.PLAYING)
                mp3Player.pause();
        }

        public void playPre() {

        }

        public void playNext() {

        }

        public void playStop() {
            mp3Player.stop();
        }

        public void release() {
            mp3Player.release();
        }

        public void setPlayList(ArrayList<String> playList) {
            this.playList = playList;
        }

        public void setPlayType(int playType) {
            this.playType = playType;
        }

        public int getPlayType() {
            return playType;
        }

        public void setPlaySpeed(float playSpeed) {
            mp3Player.setPlaySpeed(playSpeed);
        }

        public void playSeek(int seek) {

            if (seek > 0) {
                mp3Player.seekTo(seek);
            }
        }

        public void setOnMp3PlayListener(Mp3Player.onMp3PlayListener onMp3PlayListener) {
            this.onMp3PlayListener = onMp3PlayListener;
        }
    }

    public static void setPlayList(Context context, ArrayList<String> playList) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY_LIST);
        intent.putExtra(EXTRA_DATA, playList);
        context.startService(intent);
    }

    /**
     * 播放
     *
     * @param context
     * @param url
     */
    public static void play(Context context, String url) {
        play(context, url, -1);
    }

    public static void play(Context context, String url, int seek) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_DATA, url);
        intent.putExtra(EXTRA_DATA_2, seek);
        context.startService(intent);
    }

    public static void setSpeed(Context context, float speed) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY_SPEED);
        intent.putExtra(EXTRA_DATA, speed);
        context.startService(intent);
    }

    /**
     * 播放/暂停
     *
     * @param context
     */
    public static void playOrPause(Context context) {
        execute(context, ACTION_PLAY_OR_PAUSE);
    }

    /**
     * seek
     *
     * @param context
     * @param current
     */
    public static void seekTo(Context context, int current) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY_SEEK);
        intent.putExtra(EXTRA_DATA, current);
        context.startService(intent);
    }

    /**
     * 上一曲
     *
     * @param context
     */
    public static void playPre(Context context) {
        execute(context, ACTION_PLAY_PRE);
    }

    /**
     * 下一曲
     *
     * @param context
     */
    public static void playNext(Context context) {
        execute(context, ACTION_PLAY_NEXT);
    }

    /**
     * 停止
     *
     * @param context
     */
    public static void playStop(Context context) {
        execute(context, ACTION_STOP);
    }


    /**
     * 释放
     *
     * @param context
     */
    public static void release(Context context) {
        execute(context, ACTION_RELEASE);
    }

    public static void execute(Context context, String action) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }


    private void sendBroadcast(String action, Intent intent) {

    }
}
