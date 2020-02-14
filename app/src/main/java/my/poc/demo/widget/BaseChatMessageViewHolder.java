package my.poc.demo.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.huamai.poc.greendao.ChatMessage;


public abstract class BaseChatMessageViewHolder extends RecyclerView.ViewHolder {

    public int position;

    BaseChatMessageViewHolder(View itemView) {
        super(itemView);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public abstract void bindViewData(ChatMessage message, boolean timeDivision);

    public void setOnClickListener(View.OnClickListener l) {
    }

    public void setOnLongClickListener(View.OnLongClickListener l) {
    }

    public void setOnResendEventListener(View.OnClickListener l) {
    }
}
