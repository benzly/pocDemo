package my.poc.demo.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngine;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.chat.ChatMessageCategory;
import com.huamai.poc.greendao.ChatMessage;
import com.huamai.poc.greendao.MessageDialogue;

import java.util.ArrayList;

import my.poc.demo.R;
import my.poc.demo.activity.ChatMessageActivity;

public class MessageListFragment extends Fragment {

    View rootView;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    MyAdapter adapter;
    Dialog deleteDialog;
    PocEngine pocEngine = PocEngineFactory.get();
    ArrayList<MessageDialogue> messageDialogueArray = new ArrayList<>(20);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.home_fragment_msg, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
            progressBar = (ProgressBar) rootView.findViewById(R.id.loading);

            messageDialogueArray.clear();
            messageDialogueArray.addAll(PocEngineFactory.get().getAllConversation());

            adapter = new MyAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }
        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView.getChildCount() > 0) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PocEngineFactory.get().addEventHandler(mPocEventHandler);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PocEngineFactory.get().removeEventHandler(mPocEventHandler);
    }

    private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onNewConversationCreated(MessageDialogue messageDialogue) {
            messageDialogueArray.clear();
            messageDialogueArray.addAll(PocEngineFactory.get().getAllConversation());
            adapter.notifyDataSetChanged();
        }
    };

    public void addNewConversation(MessageDialogue messageDialogue) {
        for (MessageDialogue dialogue : messageDialogueArray) {
            if (dialogue.getChat_id() == messageDialogue.getChat_id()) {
                messageDialogueArray.remove(dialogue);
                break;
            }
        }
        messageDialogueArray.add(0, messageDialogue);
        adapter.notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView userIcon;
        TextView name;
        TextView previewMsg;
        TextView unreadTip;
        TextView type;
        MessageDialogue dialogue;
        ChatMessage lastMessage;


        MyViewHolder(View itemView) {
            super(itemView);
            userIcon = (ImageView) itemView.findViewById(R.id.user_icon);
            name = (TextView) itemView.findViewById(R.id.name);
            previewMsg = (TextView) itemView.findViewById(R.id.message);
            unreadTip = (TextView) itemView.findViewById(R.id.unread_tip);
            type = (TextView) itemView.findViewById(R.id.type);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        LayoutInflater inflater;

        MyAdapter() {
            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(inflater.inflate(R.layout.item_messagelist, null));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder viewHolder, int position) {
            final MessageDialogue dialogue = messageDialogueArray.get(viewHolder.getAdapterPosition());
            ChatMessage lastMessage = pocEngine.getLastConversationMessage(dialogue.getChat_id());
            viewHolder.name.setText(dialogue.getName());
            viewHolder.type.setVisibility(View.GONE);

            if (!dialogue.getIsGroup()) {
                viewHolder.userIcon.setImageResource(R.drawable.ic_users_offline);
            } else {
                viewHolder.userIcon.setImageResource(R.drawable.re_default_custom_group);
            }

            if (lastMessage != null) {
                switch (lastMessage.getCategory()) {
                    case ChatMessageCategory.TEXT:
                        viewHolder.previewMsg.setText(lastMessage.getText());
                        break;
                    case ChatMessageCategory.AUDIO:
                        viewHolder.previewMsg.setText("[语音呼叫]");
                        break;
                    case ChatMessageCategory.VIDEO:
                        viewHolder.previewMsg.setText("[视频呼叫]");
                        break;
                    case ChatMessageCategory.IMAGE:
                        viewHolder.previewMsg.setText("[图片]");
                        break;
                    case ChatMessageCategory.AUDIO_FILE:
                    case ChatMessageCategory.PTT_AUDIO_FILE:
                        viewHolder.previewMsg.setText("[语音]");
                        break;
                    default:
                        viewHolder.previewMsg.setText("[未知消息]");
                        break;
                }
                viewHolder.previewMsg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.previewMsg.setVisibility(View.GONE);
            }
            int unReadCount = dialogue.getUnread();
            if (unReadCount <= 0) {
                viewHolder.unreadTip.setVisibility(View.GONE);
            } else {
                viewHolder.unreadTip.setVisibility(View.VISIBLE);
                if (unReadCount > 99) {
                    viewHolder.unreadTip.setText("99+");
                } else {
                    viewHolder.unreadTip.setText(String.valueOf(unReadCount));
                }
            }

            viewHolder.dialogue = dialogue;
            viewHolder.lastMessage = lastMessage;

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogue.setUnread(0);
                    PocEngineFactory.get().markConversationAsRead(dialogue.getChat_id());
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    ChatMessageActivity.show(getActivity(), dialogue.getChat_id());
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteDialog = new AlertDialog.Builder(getContext())
                            .setItems(new String[]{"删除对话"}, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            pocEngine.deleteConversation(dialogue.getChat_id());
                                            pocEngine.deleteConversationMessages(dialogue.getChat_id());
                                            messageDialogueArray.remove(dialogue);
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                            break;
                                    }
                                }
                            }).create();
                    deleteDialog.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return messageDialogueArray.size();
        }
    }
}
