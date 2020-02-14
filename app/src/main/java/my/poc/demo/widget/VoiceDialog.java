package my.poc.demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import my.poc.demo.R;

public class VoiceDialog extends Dialog {

    private ImageView voiceIv;
    private ImageView voiceCancelIv;
    private TextView tip;


    public VoiceDialog(Context context) {
        super(context, R.style.dialog);
        setContentView(R.layout.voice_record_layout);
        voiceIv = (ImageView) findViewById(R.id.voice);
        voiceCancelIv = (ImageView) findViewById(R.id.voice_cancel);
        tip = (TextView) findViewById(R.id.tips);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showVoiceing();
        updateVoice(0);
    }

    public void showCancel() {
        voiceIv.setVisibility(View.GONE);
        voiceCancelIv.setVisibility(View.VISIBLE);
        tip.setText("voice_cancel_tip2");
        tip.setBackgroundResource(R.drawable.fillet_button_normal);
    }

    public void showVoiceing() {
        voiceIv.setVisibility(View.VISIBLE);
        voiceCancelIv.setVisibility(View.GONE);
        tip.setText("voice_cancel_tip");
        tip.setBackground(null);
    }

    public void updateVoice(int db) {
        if (voiceIv == null || voiceIv.getVisibility() != View.VISIBLE) {
            return;
        }
        if (db <= 40) {
            voiceIv.setImageResource(R.drawable.ic_voice0);
        } else if (db <= 45) {
            voiceIv.setImageResource(R.drawable.ic_voice1);
        } else if (db <= 55) {
            voiceIv.setImageResource(R.drawable.ic_voice2);
        } else if (db <= 60) {
            voiceIv.setImageResource(R.drawable.ic_voice3);
        } else if (db <= 65) {
            voiceIv.setImageResource(R.drawable.ic_voice4);
        } else if (db <= 70) {
            voiceIv.setImageResource(R.drawable.ic_voice5);
        } else if (db <= 72) {
            voiceIv.setImageResource(R.drawable.ic_voice6);
        } else if (db <= 73) {
            voiceIv.setImageResource(R.drawable.ic_voice7);
        } else if (db <= 75) {
            voiceIv.setImageResource(R.drawable.ic_voice8);
        } else {
            voiceIv.setImageResource(R.drawable.ic_voice9);
        }
    }

    public VoiceDialog setMessage(String message) {
        tip.setText(message);
        return this;
    }
}
