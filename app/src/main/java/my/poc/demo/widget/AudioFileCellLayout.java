package my.poc.demo.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.chat.ChatMessageStatus;
import com.huamai.poc.greendao.ChatMessage;

import my.poc.demo.R;


public class AudioFileCellLayout extends RelativeLayout implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayerFactory.OnPlayerListener, View.OnLongClickListener {

    MediaPlayer mMediaPlayer;
    String localSource;
    String httpSource;
    String id;
    boolean isOut;
    ChatMessage message;

    ImageView imageView;

    public AudioFileCellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        imageView = (ImageView) findViewById(R.id.voiceing);
    }

    public void bindData(ChatMessage message) {
        localSource = message.getLocalFile();
        httpSource = message.getHttpFile();
        id = message.getId();
        isOut = message.getIs_out();
        this.message = message;

        if (MediaPlayerFactory.getInstance().getCurrentPlayer() != null && id.equals(MediaPlayerFactory.getInstance().getCurrentId())) {
            mMediaPlayer = MediaPlayerFactory.getInstance().getCurrentPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);

            if (mMediaPlayer.isPlaying()) {
                imageView.setBackgroundResource(isOut ? R.drawable.ani_audio_file_right_playing : R.drawable.ani_audio_file_left_playing);
                ((AnimationDrawable) imageView.getBackground()).start();
            } else {
                imageView.setBackgroundResource(isOut ? R.drawable.ic_right_voice_play : R.drawable.ic_left_voice_play);
            }
        } else {
            mMediaPlayer = null;
            imageView.setBackgroundResource(isOut ? R.drawable.ic_right_voice_play : R.drawable.ic_left_voice_play);
        }
    }

    @Override
    public void onClick(View v) {
        startOrStopPlay();
        ((ViewGroup) getParent()).performClick();
        if (message.getMsg_status() == ChatMessageStatus.Chat.UNREAD) {
            PocEngineFactory.get().markMessageAsRead(message);
        }
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        stopPlay();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            stopPlay();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ((ViewGroup) getParent()).performLongClick();
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    private void startOrStopPlay() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                stopPlay();
            } else {
                startPlay();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mMediaPlayer = null;
        }
    }

    private void startPlay() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayerFactory.getInstance().getPlayer(id, this);
            mMediaPlayer.reset();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
        }
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (!TextUtils.isEmpty(localSource)) {
                mMediaPlayer.setDataSource(localSource);
            } else if (!TextUtils.isEmpty(httpSource)) {
                mMediaPlayer.setDataSource(httpSource);
            } else {
                Toast.makeText(getContext(), "invalid_audio_file", Toast.LENGTH_SHORT).show();
                return;
            }
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "invalid_audio_file", Toast.LENGTH_SHORT).show();
        }
        imageView.setBackgroundResource(isOut ? R.drawable.ani_audio_file_right_playing : R.drawable.ani_audio_file_left_playing);
        ((AnimationDrawable) imageView.getBackground()).start();
    }

    private void stopPlay() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        if (imageView != null) {
            imageView.setBackgroundResource(isOut ? R.drawable.ic_right_voice_play : R.drawable.ic_left_voice_play);
        }
    }

    @Override
    public void onRemove() {
        mMediaPlayer = null;
        imageView.setBackgroundResource(isOut ? R.drawable.ic_right_voice_play : R.drawable.ic_left_voice_play);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        imageView.setBackgroundResource(isOut ? R.drawable.ic_right_voice_play : R.drawable.ic_left_voice_play);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        imageView.setBackgroundResource(isOut ? R.drawable.ic_right_voice_play : R.drawable.ic_left_voice_play);
        return true;
    }
}
