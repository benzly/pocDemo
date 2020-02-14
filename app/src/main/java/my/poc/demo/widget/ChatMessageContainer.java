package my.poc.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huamai.poc.chat.ChatMessageCategory;
import com.huamai.poc.chat.ChatMessageStatus;
import com.huamai.poc.greendao.ChatMessage;

import my.poc.demo.R;


public class ChatMessageContainer extends RelativeLayout {

    public ChatMessageContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    public void bindChatContent(ChatMessage message) {
        removeAllViews();
        final boolean isOut = message.getIs_out();
        final int category = message.getCategory();

        View item;
        switch (category) {
            case ChatMessageCategory.TEXT:
                item = LayoutInflater.from(getContext()).inflate(isOut ? R.layout.chat_right_textview : R.layout.chat_left_textview, null);
                ((TextView) item).setText(message.getText());
                break;
            case ChatMessageCategory.IMAGE:
                item = LayoutInflater.from(getContext()).inflate(isOut ? R.layout.chat_right_textview : R.layout.chat_left_textview, null);
                String tip = "【图片】" + message.getUpdown_progress() + "%";
                ((TextView) item).setText(tip);
                break;
            case ChatMessageCategory.AUDIO:
            case ChatMessageCategory.VIDEO:
            case ChatMessageCategory.AUDIO_PTT:
                item = LayoutInflater.from(getContext()).inflate(isOut ? R.layout.chat_right_audio_video_item : R.layout.chat_left_audio_video_item, null);
                TextView tv = (TextView) item.findViewById(R.id.text);
                ImageView iv = (ImageView) item.findViewById(R.id.icon);
                switch (category) {
                    case ChatMessageCategory.AUDIO:
                        tv.setText("语音呼叫");
                        iv.setImageResource(R.drawable.ic_dialer_sip_black_24dp);
                        break;
                    case ChatMessageCategory.VIDEO:
                        tv.setText("视频呼叫");
                        iv.setImageResource(R.drawable.ic_dialer_sip_black_24dp);
                        break;
                    case ChatMessageCategory.AUDIO_PTT:
                        tv.setText("对讲");
                        iv.setImageResource(R.drawable.ic_dialer_sip_black_24dp);
                        break;
                }
                Integer sip_status = message.getSip_status();
                if (sip_status != null) {
                    switch (sip_status) {
                        case ChatMessageStatus.Sip.CANCEL:
                            tv.setText("已取消");
                            break;
                        case ChatMessageStatus.Sip.UNRESPONSE:
                            tv.setText("未接听");
                            break;
                    }
                }
                break;
            case ChatMessageCategory.PTT_AUDIO_FILE:
            case ChatMessageCategory.AUDIO_FILE:
                AudioFileCellLayout audioCell = (AudioFileCellLayout) LayoutInflater.from(getContext()).
                        inflate(isOut ? R.layout.chat_right_audio_file : R.layout.chat_left_audio_file, null);
                audioCell.bindData(message);
                long len = message.getDuration() != null ? message.getDuration() : 0;
                int min = (int) (getResources().getDimension(R.dimen.chat_audiofile_cell_min_width));
                int max = (int) (getResources().getDimension(R.dimen.chat_audiofile_cell_max_width));
                int increase = (int) getResources().getDimension(R.dimen.chat_audiofile_cell_more);
                if (len > 60) {
                    len = 60;
                }
                if (len <= 3) {
                    increase = 0;
                } else if (len == 4) {
                    // default
                } else if (len <= 10) {
                    increase = (int) (increase * (len - 4));
                } else {
                    increase = (increase * (10 - 4)) + (int) ((len - 10) * (max - min) / 50);
                }

                RelativeLayout.LayoutParams p = (LayoutParams) audioCell.getLayoutParams();
                if (p == null) {
                    p = new RelativeLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.chat_message_min_heght));
                }
                p.width = min + increase;
                audioCell.setLayoutParams(p);
                //===============

                item = audioCell;
                break;
            default:
                TextView tips = new TextView(getContext());
                tips.setText("unsupport");
                item = tips;
                break;
        }

        if (item != null) {
            addView(item);
        }
    }
}
