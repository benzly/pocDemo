package my.poc.demo.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.unionbroad.app.util.Logger;
import com.huamai.poc.greendao.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import my.poc.demo.R;

public class ChatMessageViewAdapter extends RecyclerView.Adapter<BaseChatMessageViewHolder> {

    public interface OnItemResendClickListener {
        void onItemResendClick(View item, int position);
    }

    private static final int TYPE_LEFT = 1;
    private static final int TYPE_RIGHT = 2;

    private Logger mLogger = Logger.getLogger("ChatMessageViewAdapter");
    private final ArrayList<ChatMessage> mMessageList = new ArrayList<ChatMessage>();
    private RecyclerView mRecyclerView;
    private LayoutInflater mLayoutInflater;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener;
    private OnItemResendClickListener mOnItemResendClickListener;


    public ChatMessageViewAdapter(Context context, RecyclerView recyclerView) {
        mLayoutInflater = LayoutInflater.from(context.getApplicationContext());
        mRecyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < getItemCount()) {
            ChatMessage message = mMessageList.get(position);
            return message.getIs_out() ? TYPE_RIGHT : TYPE_LEFT;
        }
        return -1;
    }

    @Override
    public BaseChatMessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mLogger.d("onCreateViewHolder viewType=" + viewType);
        switch (viewType) {
            case TYPE_LEFT:
                return new ChatMessageViewHolder(mLayoutInflater.inflate(R.layout.chat_left_message_layout, null));
            case TYPE_RIGHT:
                return new ChatMessageViewHolder(mLayoutInflater.inflate(R.layout.chat_right_message_layout, null));
        }
        // return en empty view
        return new EmptyHolder(new View(mLayoutInflater.getContext()));
    }

    @Override
    public void onBindViewHolder(final BaseChatMessageViewHolder viewHolder, int position) {
        if (position >= 0 && position < getItemCount()) {
            viewHolder.position = viewHolder.getAdapterPosition();
            boolean needTimeDivision;
            final ChatMessage message = mMessageList.get(position);
            if (position == 0) {
                needTimeDivision = true;
            } else {
                long timeDiff = message.getTime_stamp() - mMessageList.get(position - 1).getTime_stamp();
                needTimeDivision = timeDiff > 200000;
            }
            viewHolder.bindViewData(message, needTimeDivision);

            viewHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(null, v, viewHolder.position, v.getId());
                    }
                }
            });
            viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemLongClickListener != null) {
                        mOnItemLongClickListener.onItemLongClick(null, v, viewHolder.position, v.getId());
                    }
                    return false;
                }
            });
            viewHolder.setOnResendEventListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemResendClickListener != null) {
                        mOnItemResendClickListener.onItemResendClick(viewHolder.itemView, viewHolder.position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener l) {
        mOnItemLongClickListener = l;
    }

    public void setOnItemResendClickListener(OnItemResendClickListener l) {
        mOnItemResendClickListener = l;
    }

    public ChatMessage getDataByPosition(int position) {
        if (position < mMessageList.size()) {
            return mMessageList.get(position);
        }
        return null;
    }

    public ArrayList<ChatMessage> getData() {
        return mMessageList;
    }

    public void clearAll() {
        mMessageList.clear();
        notifyDataSetChanged();
    }

    public void updateItemData(ChatMessage message) {
        for (ChatMessage chatMessage : mMessageList) {
            if (message.getId().equals(chatMessage.getId())) {
                chatMessage.setLocalFile(message.getLocalFile());
                break;
            }
        }
    }

    public void updateItem(ChatMessage chatMessage) {
        boolean hasUpdated = false;
        int count = mRecyclerView.getChildCount();
        for (int i = 0; i < count; i++) {
            BaseChatMessageViewHolder holder = (BaseChatMessageViewHolder) mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i));
            if (holder.position > mMessageList.size() -1) {
                continue;
            }
            ChatMessage hitMessage = mMessageList.get(holder.position);
            if (chatMessage.getId().equals(hitMessage.getId())) {
                hitMessage.setSip_status(chatMessage.getSip_status());
                hitMessage.setDuration(chatMessage.getDuration());
                hitMessage.setUpdown_progress(chatMessage.getUpdown_progress());
                notifyItemChanged(holder.position);
                hasUpdated = true;
                mLogger.d("notifyItemChanged " + holder.position);
                break;
            }
        }
        if (!hasUpdated) {
            for (ChatMessage message : mMessageList) {
                if (message.getId().equals(chatMessage.getId())) {
                    message.setSip_status(chatMessage.getSip_status());
                    break;
                }
            }
        }
    }

    public int getLastPosition() {
        if (getItemCount() > 0) {
            return getItemCount() - 1;
        } else {
            return 0;
        }
    }

    public void updateAll(ArrayList<ChatMessage> messages) {
        mMessageList.clear();
        mMessageList.addAll(messages);
        notifyDataSetChanged();
        mRecyclerView.scrollToPosition(getLastPosition());
    }

    public void addChatMessage(ChatMessage message) {
        // 查找插入位置
        int insertIndex = hitInsertIndex(mMessageList, message.getTime_stamp());
        mMessageList.add(insertIndex, message);
        notifyItemInserted(insertIndex);
        if (insertIndex == getLastPosition()) {
            mRecyclerView.smoothScrollToPosition(getLastPosition());
        }
    }

    public void addChatMessage(List<ChatMessage> messages) {
        /*for (ChatMessage message : mMessageList) {
            mLogger.d("message " + message.getText() + " " + message.getTime_stamp());
        }
        mLogger.d("add ===> " + messages.get(0).getText() + " " + messages.get(0).getTime_stamp());*/
        if (messages.size() > 0) {
            // 查找插入位置
            int insertIndex = hitInsertIndex(mMessageList, messages.get(0).getTime_stamp());
            mMessageList.addAll(insertIndex, messages);
            notifyItemInserted(insertIndex);
            if (insertIndex + messages.size() - 1 == getLastPosition()) {
                mRecyclerView.smoothScrollToPosition(getLastPosition());
            }
        }
    }

    private int hitInsertIndex(ArrayList<ChatMessage> list, long target) {
        if (list.size() == 0) {
            return 0;
        }
        int left = 0;
        int right = list.size() - 1;
        int index = list.size();
        if (target < list.get(left).getTime_stamp()) {
            index = left;
        } else if (target > list.get(right).getTime_stamp()) {
            index = right + 1;
        } else if (list.size() == 2) {
            index = right;
        } else {
            // 二分查找消息插入点
            while (left < right) {
                int mid = (left + right) / 2;
                long midValue = list.get(mid).getTime_stamp();
                long lSideValue = list.get(mid - 1).getTime_stamp();
                long rSideValue = list.get(mid + 1).getTime_stamp();
                //System.out.println("target=" + target + "====== mid.index=" + mid + " lSideValue=" + lSideValue + " midValue=" + midValue + " rSideValue=" + rSideValue + " ======");
                if (midValue == target) { //命中
                    index = mid + 1;
                    //System.out.println("hit mid");
                    break;
                } else if (target >= lSideValue && target < midValue) {//命中
                    index = mid;
                    //System.out.println("hit lMid");
                    break;
                } else if (target > midValue && target <= rSideValue) {//命中
                    index = mid + 1;
                    //System.out.println("hit rMid");
                    break;
                } else if (target < lSideValue) { // 取左侧继续二分查找
                    left = 0;
                    right = mid;
                    //System.out.println("check left [" + left + "," + right + "]");
                } else if (target > rSideValue) {// 取右侧继续二分查找
                    left = mid;
                    right = list.size() - 1;
                    //System.out.println("check right [" + left + "," + right + "]");
                } else {
                    //System.out.println("not match");
                    break;
                }
            }
        }
        return index;
    }

    public void removeChatMessage(int position) {
        mMessageList.remove(position);
        notifyItemRemoved(position);
    }
}
