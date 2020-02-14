package my.poc.demo.widget;

import android.view.View;

import com.huamai.poc.greendao.ChatMessage;

public class EmptyHolder extends BaseChatMessageViewHolder {

    public EmptyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindViewData(ChatMessage message, boolean timeDivision) {

    }
}
