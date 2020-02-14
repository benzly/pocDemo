package my.poc.demo.widget;

import android.media.MediaPlayer;

import java.util.ArrayList;

public class MediaPlayerFactory {

    public interface OnPlayerListener {
        void onRemove();
    }

    private static volatile MediaPlayerFactory sInstance;
    private MediaPlayer mMediaPlayer;
    private String mMediaPlayerId;
    private ArrayList<OnPlayerListener> mOnPlayerListeners = new ArrayList<>();

    private MediaPlayerFactory() {

    }

    public static MediaPlayerFactory getInstance() {
        if (sInstance == null) {
            synchronized (MediaPlayerFactory.class) {
                if (sInstance == null) {
                    sInstance = new MediaPlayerFactory();
                }
            }
        }
        return sInstance;
    }

    public MediaPlayer getPlayer(String id, OnPlayerListener l) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            for (OnPlayerListener listener : mOnPlayerListeners) {
                listener.onRemove();
            }
            mOnPlayerListeners.clear();
        }

        if (!mOnPlayerListeners.contains(l)) {
            mOnPlayerListeners.add(l);
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayerId = id;
        return mMediaPlayer;
    }

    public String getCurrentId() {
        return mMediaPlayerId;
    }

    public MediaPlayer getCurrentPlayer() {
        return mMediaPlayer;
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            for (OnPlayerListener listener : mOnPlayerListeners) {
                listener.onRemove();
            }
            mOnPlayerListeners.clear();
        }
    }
}
