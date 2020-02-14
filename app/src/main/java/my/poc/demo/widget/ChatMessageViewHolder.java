package my.poc.demo.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.unionbroad.app.util.BusinessUtil;
import com.huamai.poc.chat.ChatMessageCategory;
import com.huamai.poc.chat.ChatMessageStatus;
import com.huamai.poc.greendao.ChatMessage;
import my.poc.demo.R;



public class ChatMessageViewHolder extends BaseChatMessageViewHolder {

    ImageView iv;
    TextView name;
    ChatMessageContainer container;
    View containerRoot;
    TextView time_stamp;
    View status_layout;
    ImageView status_icon;
    TextView status_tip;
    ImageView unread_icon;
    ProgressBar sending_progressBar;


    public ChatMessageViewHolder(View itemView) {
        super(itemView);

        iv = (ImageView) itemView.findViewById(R.id.icon);
        name = (TextView) itemView.findViewById(R.id.name);
        container = (ChatMessageContainer) itemView.findViewById(R.id.content);
        containerRoot = itemView.findViewById(R.id.content_root);
        time_stamp = (TextView) itemView.findViewById(R.id.time_stamp);
        status_layout = itemView.findViewById(R.id.status_layout);
        status_icon = (ImageView) itemView.findViewById(R.id.status_icon);
        status_tip = (TextView) itemView.findViewById(R.id.status_tip);
        unread_icon = (ImageView) itemView.findViewById(R.id.unread_icon);
        sending_progressBar = (ProgressBar) itemView.findViewById(R.id.sending_progressbar);
    }

    @Override
    public void bindViewData(ChatMessage message, boolean timeDivision) {
        if (message.getIs_out() || !BusinessUtil.isGroup(message.getChat_id())) {
            name.setVisibility(View.GONE);
        } else {
            name.setText(message.getRemote_name() != null ? message.getRemote_name() : String.valueOf(message.getRemote_number()));
            name.setVisibility(View.VISIBLE);
        }

        if (!message.getIs_out()) {
            iv.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) containerRoot.getLayoutParams();
            lp.leftMargin = 0;
            lp.rightMargin = 60;
            containerRoot.setLayoutParams(lp);
        }

        if (!message.getIs_out()) {
            sending_progressBar.setVisibility(View.GONE);
            status_icon.setVisibility(View.GONE);
        }

        if (timeDivision) {
            time_stamp.setText(message.getTime_stamp() + "");
            time_stamp.setVisibility(View.VISIBLE);
        } else {
            time_stamp.setVisibility(View.GONE);
        }

        int sip_status = message.getSip_status() == null ? ChatMessageStatus.Sip.UNKNOW : message.getSip_status();
        switch (sip_status) {
            case ChatMessageStatus.Sip.UNKNOW:
            case ChatMessageStatus.Sip.FAILED:
                sending_progressBar.setVisibility(View.GONE);
                status_tip.setVisibility(View.GONE);
                status_icon.setImageResource(R.drawable.ic_chat_message_send_failed);
                status_icon.setVisibility(View.VISIBLE);
                break;
            case ChatMessageStatus.Sip.SENDING:
                status_icon.setVisibility(View.GONE);
                status_tip.setVisibility(View.GONE);
                sending_progressBar.setVisibility(View.VISIBLE);
                break;
            case ChatMessageStatus.Sip.SENDED:
                if (message.getCategory() == ChatMessageCategory.AUDIO_FILE) {
                    status_tip.setVisibility(View.VISIBLE);
                    status_tip.setText(message.getDuration() + "″");
                } else {
                    status_tip.setVisibility(View.GONE);
                }
                sending_progressBar.setVisibility(View.GONE);
                status_icon.setVisibility(View.GONE);
                break;
            default:
                status_icon.setVisibility(View.GONE);
                sending_progressBar.setVisibility(View.GONE);
                break;
        }
        int chat_status = message.getMsg_status() == null ? ChatMessageStatus.Chat.UNKNOW : message.getMsg_status();
        switch (chat_status) {
            case ChatMessageStatus.Chat.UNREAD:
                if (message.getCategory() == ChatMessageCategory.AUDIO_FILE || message.getCategory() == ChatMessageCategory.VIDEO_FILE) {
                    unread_icon.setVisibility(View.VISIBLE);
                } else {
                    unread_icon.setVisibility(View.GONE);
                }
                break;
            default:
                unread_icon.setVisibility(View.GONE);
                break;
        }

        container.bindChatContent(message);
    }

    @Override
    public void setOnClickListener(final View.OnClickListener l) {
        container.setOnClickListener(l);
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener l) {
        container.setOnLongClickListener(l);
    }

    @Override
    public void setOnResendEventListener(View.OnClickListener l) {
        status_icon.setOnClickListener(l);
    }
}