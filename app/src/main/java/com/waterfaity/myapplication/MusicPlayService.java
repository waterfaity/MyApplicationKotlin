package com.waterfaity.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2019/4/25 19:03
 * @info:
 */
public class MusicPlayService extends Service {

    MyBinder myBinder;


    //    - - - - - - - - - - -  服务 - - - - - - - - - - -
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
    public static final String ACTION_SEND_BROADCAST = "music_send_broadcast";//发送广播

    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_DATA_2 = "data2";
    //    - - - - - - - - - - -  服务 - - - - - - - - - - -

    //    - - - - - - - - - - -  广播 - - - - - - - - - - -
    public static final String ACTION_BROADCAST_PLAY_PROGRESS = "MusicPlayService_play_progress";
    public static final String ACTION_BROADCAST_PLAY_STATE = "MusicPlayService_play_state";
    public static final String ACTION_BROADCAST_PLAY_ERROR = "MusicPlayService_play_error";
    public static final String ACTION_BROADCAST_PLAY_NEXT = "MusicPlayService_play_next";
    public static final String ACTION_BROADCAST_PLAY_PRE = "MusicPlayService_play_pre";

    public static final String EXTRA_STATE_CODE = "state_code";
    public static final String EXTRA_ERR_MSG = "err_msg";
    public static final String EXTRA_TOTAL_LEN = "total_len";
    public static final String EXTRA_CURRENT_LEN = "current_len";
    public static final String EXTRA_CURRENT_POS = "current_pos";
    public static final String EXTRA_CURRENT_MUSIC_BEAN = "current_music_bean";
    //    - - - - - - - - - - -  广播 - - - - - - - - - - -

    //    - - - - - - - - - - -  播放方式 - - - - - - - - - - -
    public static final int PLAY_TYPE_PLAY_SINGLE = 1;
    public static final int PLAY_TYPE_PLAY_ALL = 2;
    public static final int PLAY_TYPE_LOOP_SINGLE = 3;
    public static final int PLAY_TYPE_LOOP_ALL = 4;
    //    - - - - - - - - - - -  播放方式 - - - - - - - - - - -


    @Override
    public IBinder onBind(Intent intent) {
        return myBinder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && !TextUtils.isEmpty(action)) {
                switch (action) {
                    case ACTION_PLAY:
                        if (intent.getSerializableExtra(EXTRA_DATA) != null) {
                            myBinder.play((MusicBean) intent.getSerializableExtra(EXTRA_DATA), intent.getIntExtra(EXTRA_DATA_2, 0));
                        }
                        break;
                    case ACTION_PLAY_OR_PAUSE:
                        myBinder.playOrPause();
                        break;
                    case ACTION_PLAY_SEEK:
                        myBinder.seekTo(intent.getIntExtra(EXTRA_DATA, 0));
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
                        if (intent.getSerializableExtra(EXTRA_DATA) != null) {
                            myBinder.setPlayList((ArrayList<MusicBean>) intent.getSerializableExtra(EXTRA_DATA));
                        }
                        break;
                    case ACTION_PLAY_TYPE:
                        myBinder.setPlayType(intent.getIntExtra(EXTRA_DATA, PLAY_TYPE_PLAY_SINGLE));
                        break;
                    case ACTION_PLAY_SPEED:
                        myBinder.setPlaySpeed(intent.getFloatExtra(EXTRA_DATA, 1F));
                        break;
                    case ACTION_SEND_BROADCAST:
                        myBinder.setSendBroadcast(intent.getBooleanExtra(EXTRA_DATA, false));
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public class MyBinder extends Binder {
        private OnPlayListener onPlayListener;
        private Mp3Player.onMp3PlayListener onMp3PlayListener;
        private ArrayList<MusicBean> playList;
        private int currentPos;
        private MusicBean currentMusicBean;
        private int playType;
        private Mp3Player mp3Player;
        private boolean sendBroadcast;

        /**
         * 获取播放列表
         *
         * @return
         */
        public ArrayList<MusicBean> getPlayList() {
            return playList;
        }

        /**
         * 当前位置
         *
         * @return
         */
        public int getCurrentPos() {
            return currentPos;
        }

        /**
         * 当前播放bean
         *
         * @return
         */
        public MusicBean getCurrentMusicBean() {
            return currentMusicBean;
        }

        /**
         * 播放器
         *
         * @return
         */
        public Mp3Player getMp3Player() {
            return mp3Player;
        }

        public void play(MusicBean bean) {
            play(bean, -1);
        }

        public void play(MusicBean bean, int seek) {
            if (bean != null) {
                this.currentMusicBean = bean;
                initPos();
                initPlayer();
                mp3Player.play(currentMusicBean.getMp3UrlOrPath(), seek);
            }
        }

        private void initPlayer() {
            if (mp3Player == null) {
                mp3Player = new Mp3Player();
                mp3Player.setOnMp3PlayListener(new Mp3Player.onMp3PlayListener() {
                    @Override
                    public void onMp3PlayError(int state, String message) {
                        if (sendBroadcast) {
                            Intent intent = new Intent(ACTION_BROADCAST_PLAY_ERROR);
                            intent.putExtra(EXTRA_STATE_CODE, state);
                            intent.putExtra(EXTRA_ERR_MSG, message);
                            sendBroadcast(intent);
                        }
                        if (onMp3PlayListener != null)
                            onMp3PlayListener.onMp3PlayError(state, message);
                    }

                    @Override
                    public void onPlayStateChanged(int state, String message) {
                        if (sendBroadcast) {
                            Intent intent = new Intent(ACTION_BROADCAST_PLAY_STATE);
                            intent.putExtra(EXTRA_STATE_CODE, state);
                            sendBroadcast(intent);
                        }
                        if (onMp3PlayListener != null)
                            onMp3PlayListener.onPlayStateChanged(state, message);

                        if (state == Mp3Player.COMPLETE) {
                            onComplete();
                        }
                    }

                    @Override
                    public void OnPlaying(int current, int total) {
                        if (sendBroadcast) {
                            Intent intent = new Intent(ACTION_BROADCAST_PLAY_PROGRESS);
                            intent.putExtra(EXTRA_TOTAL_LEN, total);
                            intent.putExtra(EXTRA_CURRENT_LEN, current);
                            sendBroadcast(intent);
                        }
                        if (onMp3PlayListener != null)
                            onMp3PlayListener.OnPlaying(current, total);
                    }
                });
            }
        }

        private void onComplete() {
            if (playType != 0 && playType != PLAY_TYPE_PLAY_SINGLE) {
                if (playType == PLAY_TYPE_LOOP_SINGLE) {
                    //单曲循环
                    play(currentMusicBean);
                } else if (playType == PLAY_TYPE_PLAY_ALL) {
                    //全部播放
                    playNext();
                } else if (playType == PLAY_TYPE_LOOP_ALL) {
                    //循环全部
                    if (playList != null) {
                        if (currentPos == playList.size() - 1) {
                            //最后一首
                            play(playList.get(0));
                        } else {
                            //非最后一首
                            playNext();
                        }
                    } else {
                        play(currentMusicBean);
                    }
                }
            }
        }

        /**
         * 定位当前播放pos
         */
        private void initPos() {
            if (playList == null || this.currentMusicBean == null)
                currentPos = 0;
            else {
                currentPos = playList.indexOf(currentMusicBean);
            }
        }

        public void playOrPause() {
            if (mp3Player.getPlayState() == Mp3Player.PAUSE || mp3Player.getPlayState() == Mp3Player.COMPLETE) {
                if (currentMusicBean != null) {
                    mp3Player.play(currentMusicBean.getMp3UrlOrPath());
                }
            } else if (mp3Player.getPlayState() == Mp3Player.PLAYING) {
                mp3Player.pause();
            }
        }

        public void playPre() {
            if (playList != null && playList.size() > 0) {
                if (currentPos > 0) {
                    if (onPlayListener != null)
                        onPlayListener.onPlayPre(currentPos - 1, playList.get(currentPos - 1), currentPos - 1 > 0);
                    if (sendBroadcast) {
                        Intent intent = new Intent(ACTION_BROADCAST_PLAY_PRE);
                        intent.putExtra(EXTRA_CURRENT_POS, currentPos - 1);
                        intent.putExtra(EXTRA_CURRENT_MUSIC_BEAN, playList.get(currentPos - 1));
                        sendBroadcast(intent);
                    }
                    play(playList.get(currentPos - 1));
                } else if (playType == PLAY_TYPE_LOOP_ALL || playType == PLAY_TYPE_LOOP_SINGLE) {
                    //循环 播放最后一个
                    play(playList.get(playList.size() - 1));
                }
            }
        }

        public void playNext() {
            if (playList != null && playList.size() > 0) {
                if (currentPos < playList.size() - 1) {
                    if (onPlayListener != null)
                        onPlayListener.onPlayNext(currentPos + 1, playList.get(currentPos + 1), currentPos + 1 < playList.size() - 1);
                    if (sendBroadcast) {
                        Intent intent = new Intent(ACTION_BROADCAST_PLAY_NEXT);
                        intent.putExtra(EXTRA_CURRENT_POS, currentPos + 1);
                        intent.putExtra(EXTRA_CURRENT_MUSIC_BEAN, playList.get(currentPos + 1));
                        sendBroadcast(intent);
                    }
                    play(playList.get(currentPos + 1));
                } else if (playType == PLAY_TYPE_LOOP_ALL || playType == PLAY_TYPE_LOOP_SINGLE) {
                    //循环 播放第一个
                    play(playList.get(0));
                }
            }
        }

        public void playStop() {
            mp3Player.stop();
        }

        public void release() {
            mp3Player.release();
        }

        public void setPlayList(ArrayList<MusicBean> playList) {
            this.playList = playList;
        }

        public void setPlayType(int playType) {
            this.playType = playType;
        }

        /**
         * 播放方式
         *
         * @return
         */
        public int getPlayType() {
            return playType;
        }

        /**
         * 设置播放速度
         *
         * @param playSpeed
         */
        public void setPlaySpeed(float playSpeed) {
            mp3Player.setPlaySpeed(playSpeed);
        }

        /**
         * seek
         *
         * @param seek
         */
        public void seekTo(int seek) {
            if (seek > 0) {
                mp3Player.seekTo(seek);
            }
        }

        /**
         * 播放监听 上/下曲
         *
         * @param onPlayListener
         */
        public void setOnPlayListener(OnPlayListener onPlayListener) {
            this.onPlayListener = onPlayListener;
        }

        /**
         * 播放监听状态 暂停/播放/完成/停止/播放中/错误
         *
         * @param onMp3PlayListener
         */
        public void setOnMp3PlayListener(Mp3Player.onMp3PlayListener onMp3PlayListener) {
            this.onMp3PlayListener = onMp3PlayListener;
        }

        public void setSendBroadcast(boolean sendBroadcast) {
            this.sendBroadcast = sendBroadcast;
        }
    }

    /**
     * 设置播放列表
     *
     * @param context
     * @param playList
     */
    public static void setPlayList(Context context, ArrayList<MusicBean> playList) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY_LIST);
        intent.putExtra(EXTRA_DATA, playList);
        context.startService(intent);
    }

    /**
     * 播放
     *
     * @param context
     * @param musicBean
     */
    public static void play(Context context, MusicBean musicBean) {
        play(context, musicBean, -1);
    }

    /**
     * 播放
     *
     * @param context
     * @param musicBean
     * @param seek
     */
    public static void play(Context context, MusicBean musicBean, int seek) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_DATA, musicBean);
        intent.putExtra(EXTRA_DATA_2, seek);
        context.startService(intent);
    }

    public static void setSpeed(Context context, float speed) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY_SPEED);
        intent.putExtra(EXTRA_DATA, speed);
        context.startService(intent);
    }

    public static void setPlayType(Context context, int playType) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_PLAY_TYPE);
        intent.putExtra(EXTRA_DATA, playType);
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
     * 发送广播开关
     *
     * @param context
     * @param sendBroadcast
     */
    public static void setSendBroadcastState(Context context, boolean sendBroadcast) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(ACTION_SEND_BROADCAST);
        intent.putExtra(EXTRA_DATA, sendBroadcast);
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

    public interface OnPlayListener {
        /**
         * 播放上一曲
         *
         * @param pos
         * @param musicBean
         * @param hasPre
         */
        void onPlayPre(int pos, MusicBean musicBean, boolean hasPre);

        /**
         * 播放下一曲
         *
         * @param pos
         * @param musicBean
         * @param hasNext
         */
        void onPlayNext(int pos, MusicBean musicBean, boolean hasNext);
    }

    public interface MusicBean extends Serializable {
        long serialVersionUID = 20190426121212999L;

        String getMp3UrlOrPath();
    }
}
