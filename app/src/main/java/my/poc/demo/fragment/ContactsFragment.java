package my.poc.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.common.TempChannel;
import com.huamai.poc.greendao.MessageDialogue;
import com.huamai.poc.greendao.User;
import com.unionbroad.app.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import my.poc.demo.R;
import my.poc.demo.activity.AvActivity;
import my.poc.demo.activity.ChatMessageActivity;
import my.poc.demo.widget.InterceptViewPager;

public class ContactsFragment extends Fragment {

    private Logger logger = Logger.getLogger("ContactsFragment");
    private MyAdapter adapter;
    private ArrayList<User> users = new ArrayList<>();
    private View rootView;
    private RecyclerView recyclerView;
    private InterceptViewPager homeViewPager;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    public void bindViewPager(InterceptViewPager viewPager) {
        homeViewPager = viewPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.home_fragment_contacts, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.listview);

            users.clear();
            users.addAll(PocEngineFactory.get().getContactsUserList());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new MyAdapter();
            recyclerView.setAdapter(adapter);

            rootView.findViewById(R.id.audio_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<User> users = adapter.getSelectUsers();
                    if (users.size() == 0) {
                        return;
                    }
                    //勾选单人，发起点对点语音呼叫
                    if (users.size() == 1) {
                        Intent intent = new Intent(getContext().getApplicationContext(), AvActivity.class);
                        intent.putExtra("callerId", users.get(0).getNumber() + "");
                        intent.putExtra("type", IPocEngineEventHandler.SessionType.TYPE_AUDIO_CALL);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().getApplicationContext().startActivity(intent);
                    }
                    //勾选多人，发起临时对讲组
                    else {
                        TempChannel tempChannel = PocEngineFactory.get().createTempAudioPTTChannel(users);
                        PocEngineFactory.get().joinChannel(tempChannel.getChannelNumber() + "");
                        homeViewPager.setCurrentItem(0);
                    }
                }
            });

            rootView.findViewById(R.id.video_call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<User> users = adapter.getSelectUsers();
                    if (users.size() == 0) {
                        return;
                    }
                    //勾选单人，发起点对点视频呼叫
                    if (users.size() == 1) {
                        Intent intent = new Intent(getContext().getApplicationContext(), AvActivity.class);
                        intent.putExtra("callerId", users.get(0).getNumber() + "");
                        intent.putExtra("type", IPocEngineEventHandler.SessionType.TYPE_VIDEO_CALL);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().getApplicationContext().startActivity(intent);
                    }
                }
            });

            rootView.findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<User> users = adapter.getSelectUsers();
                    if (users.size() == 0) {
                        return;
                    }
                    //勾选单人，进行点对点IM
                    if (users.size() == 1) {
                        MessageDialogue md = PocEngineFactory.get().createMessageDialogueIfNeed(users.get(0));
                        ChatMessageActivity.show(getActivity(), md.getChat_id());
                    }
                    //勾选多人，进行临时群组IM
                    else {
                        MessageDialogue md = PocEngineFactory.get().createBroadcastMessageDialogue(users);
                        ChatMessageActivity.show(getActivity(), md.getChat_id());
                    }
                }
            });
        }

        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PocEngineFactory.get().addEventHandler(mPocEventHandler);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PocEngineFactory.get().removeEventHandler(mPocEventHandler);
    }

    public static class ContactsComparator implements Comparator<User> {

        @Override
        public int compare(User lhs, User rhs) {
            return (lhs.getStatus() == rhs.getStatus() ? 0 : (lhs.getStatus() > rhs.getStatus() ? -1 : 1));
        }

    }

    private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onUserStatusChanged(List<User> changeUsers) {
            Toast.makeText(getContext(), "onUserStatusChanged.size=" + changeUsers.size(), Toast.LENGTH_SHORT);
            users.clear();
            users.addAll(PocEngineFactory.get().getContactsUserList());
            Collections.sort(users, new ContactsComparator());
            adapter.notifyDataSetChanged();
        }
    };

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView userIcon;
        TextView name;
        TextView status;
        CheckBox checkBox;
        TextView uid;

        MyViewHolder(View itemView) {
            super(itemView);
            userIcon = (ImageView) itemView.findViewById(R.id.user_icon);
            name = (TextView) itemView.findViewById(R.id.name);
            status = (TextView) itemView.findViewById(R.id.status);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            uid = (TextView) itemView.findViewById(R.id.uid);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        List<User> selectUsers = new ArrayList<>();

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_user, null));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {
            final User user = users.get(position);
            viewHolder.name.setText(user.getName());
            viewHolder.uid.setText(user.getNumber() + "");
            viewHolder.status.setText(user.getStatus() == 1 ? "在线" : "离线");
            viewHolder.userIcon.setImageResource(user.getStatus() == 1 ? R.drawable.ic_user_online : R.drawable.ic_user_offline);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());

                    if (viewHolder.checkBox.isChecked()) {
                        selectUsers.add(user);
                    } else {
                        selectUsers.remove(user);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public List<User> getSelectUsers() {
            return selectUsers;
        }
    }
}