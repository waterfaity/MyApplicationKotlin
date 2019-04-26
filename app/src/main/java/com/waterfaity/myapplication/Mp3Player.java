package com.waterfaity.myapplication;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by water_fairy on 2017/8/11.
 * 995637517@qq.com
 */

public class Mp3Player {
    private static final String TAG = "mp3Player";
    private MediaPlayer mediaPlayer;//播放器
    private onMp3PlayListener onMp3PlayListener;//播放监听
    private String currentPath;//当前播放的文件路径
    //错误
    public static final int ERROR_NOT_INIT = 101;//未初始化
    public static final int ERROR_NOT_EXIST = 102;//文件不存在
    public static final int ERROR_HAS_STOP = 103;//已经停止
    public static final int ERROR_PLAY = 104;//播放错误

    private int mediaState;//播放器状态
    public static final int NOT_INIT = 0;//未初始化的
    public static final int PLAYED = 1;//开始播放或已经播放过的
    public static final int PREPARED = 2;//准备好的
    public static final int ERROR = 3;//错误

    private int playState;//播放状态
    public static final int PLAYING = 11;
    public static final int PAUSE = 12;
    public static final int STOP = 13;
    public static final int RELEASE = 14;
    public static final int COMPLETE = 15;
    private static Mp3Player mp3Player;
    private float playSpeed;

    public static Mp3Player getInstance() {
        if (mp3Player == null) mp3Player = new Mp3Player();
        return mp3Player;
    }


    public Mp3Player() {
//        initMP3();
    }

    /**
     * 设置监听
     *
     * @param onMp3PlayListener
     */
    public void setOnMp3PlayListener(onMp3PlayListener onMp3PlayListener) {
        this.onMp3PlayListener = onMp3PlayListener;
    }

    /**
     * 初始化播放器
     */
    private void initMP3() {

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        currentPath = "";

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaState = PREPARED;
                setSpeed();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playState = COMPLETE;
                Log.i(TAG, "onCompletion: ");
                if (onMp3PlayListener != null)
                    onMp3PlayListener.onPlayStateChanged(COMPLETE, "播放结束");
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mediaState = ERROR;
                String message = "播放错误";
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                        message = "音频文件格式错误";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        message = "媒体服务停止工作";
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        message = "媒体播放器错误";
                        break;
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        message = "音频文件不支持拖动播放";
                        break;
                }
                Log.i(TAG, "onError: " + what + "--" + extra);
                if (onMp3PlayListener != null)
                    onMp3PlayListener.onMp3PlayError(ERROR_PLAY, message);
                release();
                return false;
            }
        });
    }

    /**
     * 设置播放速度
     */
    private void setSpeed() {
        if (mediaState == PREPARED || mediaState == PLAYED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (playSpeed > 0)
                    mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playSpeed));
            }
        }
    }

    /**
     * 播放
     *
     * @param mediaPath
     */
    public void play(String mediaPath) {
        play(mediaPath, -1);
    }

    /**
     * 播放指定位置
     *
     * @param time
     */
    public void seekTo(int time) {
        play(currentPath, time);
    }

    /**
     * 播放
     *
     * @param mediaPath
     * @param time      播放位置  毫秒
     */
    public void play(String mediaPath, int time) {
        if ((mediaState == PREPARED || mediaState == PLAYED) && TextUtils.equals(currentPath, mediaPath) || prepare(mediaPath)) {
            mediaPlayer.start();
            if (time >= 0) {
                mediaPlayer.seekTo(time);
            }
            playState = PLAYING;
            if (onMp3PlayListener != null)
                onMp3PlayListener.onPlayStateChanged(PLAYING, "播放中");
            mediaState = PLAYED;
            initHandler();
        }
    }


    /**
     * 准备
     *
     * @param mediaPath
     */
    private boolean prepare(String mediaPath) {
        mediaState = NOT_INIT;
        playState = RELEASE;
        if (TextUtils.isEmpty(mediaPath)) {
            if (onMp3PlayListener != null)
                onMp3PlayListener.onMp3PlayError(ERROR_NOT_EXIST, "文件不存在");
            mediaState = ERROR;
        } else {
            try {
                try {
                    initMP3();
                    mediaPlayer.setDataSource(mediaPath);
                    mediaPlayer.prepare();
                } catch (IOException | IllegalStateException e) {
                    if (onMp3PlayListener != null) {
                        onMp3PlayListener.onMp3PlayError(ERROR_PLAY, "文件初始化失败");
                    }
                    mediaState = ERROR;
                }
                if (mediaState != ERROR) {
                    this.currentPath = mediaPath;
                    mediaState = PREPARED;
                    return true;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mediaState = ERROR;
            }
        }
        mediaPlayer.release();
        return false;
    }

    Handler handle;

    private void initHandler() {
        if (handle == null)
            handle = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    handle.sendEmptyMessageDelayed(0, 1000);
                    if (onMp3PlayListener != null && (mediaState == PREPARED || mediaState == PLAYED)) {
                        if (playState == PLAYING) {
                            //播放中
                            onMp3PlayListener.OnPlaying(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                        } else if (playState == COMPLETE) {
                            //播放完成 重置
                            onMp3PlayListener.OnPlaying(0, mediaPlayer.getDuration());
                        }
                    }
                }
            };
        handle.removeMessages(0);
        handle.sendEmptyMessage(0);
    }

    /**
     * 暂停
     */
    public void pause() {
        if (checkInit() && playState == PLAYING) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playState = PAUSE;
                if (onMp3PlayListener != null)
                    onMp3PlayListener.onPlayStateChanged(PAUSE, "暂停");
            }
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (checkInit()) {
            mediaPlayer.stop();
            playState = STOP;
            if (onMp3PlayListener != null)
                onMp3PlayListener.onPlayStateChanged(STOP, "停止");
        }
    }


    /**
     * 释放
     */
    public void release() {
        if (checkInit()) {
            mediaPlayer.release();
            mediaPlayer = null;
            playState = RELEASE;
            mediaState = NOT_INIT;
            if (onMp3PlayListener != null) onMp3PlayListener.onPlayStateChanged(RELEASE, "关闭");
        }
    }


    /**
     * 是否停止
     *
     * @return
     */
    private boolean checkStop() {
        return playState == STOP;
    }

    public int getPlayState() {
        return playState;
    }

    public int getMediaState() {
        return mediaState;
    }

    /**
     * 是否初始化
     *
     * @return
     */
    private boolean checkInit() {
        return mediaPlayer != null;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * 播放进度 百分比
     *
     * @return
     */
    public float getPlayRatio() {
        if (checkInit()) {
            int duration = mediaPlayer.getDuration();
            int currentPosition = mediaPlayer.getCurrentPosition();
            if (duration == 0) return 0;
            else {
                return (float) currentPosition / duration;
            }
        }
        return 0;
    }

    public void setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
        setSpeed();
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public interface onMp3PlayListener {
        void onMp3PlayError(int state, String message);

        void onPlayStateChanged(int state, String message);

        void OnPlaying(int current, int total);
    }
}