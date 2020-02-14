package my.poc.demo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.greendao.Channel;
import com.unionbroad.app.PocApplication;
import com.unionbroad.app.util.Logger;

import java.util.ArrayList;

import my.poc.demo.R;
import my.poc.demo.widget.InterceptViewPager;


/**
 * 频道类型: Channel.getChannelType()
 * "4"：语音对讲组
 * <p>
 * ----------------其它类型暂时不支持------------------
 * "1"：视频对讲
 * "2"：语音会议
 * "5"：视频会议
 */
public class ChannelListFragment extends Fragment {

    private Logger logger = Logger.getLogger("ChannelListFragment");
    private MyAdapter adapter;
    private ArrayList<Channel> channels = new ArrayList<>();
    private RecyclerView recyclerView;
    private InterceptViewPager homeViewPager;

    public ChannelListFragment() {
    }

    public static ChannelListFragment newInstance() {
        return new ChannelListFragment();
    }

    public void bindViewPager(InterceptViewPager viewPager) {
        homeViewPager = viewPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (recyclerView == null) {
            recyclerView = (RecyclerView) inflater.inflate(R.layout.home_fragment_channellist, container, false);

            channels.clear();
            channels.addAll(PocEngineFactory.get().getAudioPttChannelList());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new MyAdapter();
            recyclerView.setAdapter(adapter);
        }
        if (recyclerView.getParent() != null) {
            ((ViewGroup) recyclerView.getParent()).removeView(recyclerView);
        }
        return recyclerView;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView type;


        MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            type = (TextView) itemView.findViewById(R.id.type);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_channel, null));
        }

        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, final int position) {
            final Channel channel = channels.get(position);
            viewHolder.name.setText(channel.getChannelName());
            viewHolder.type.setText(channel.getChannelType());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] items;
                    if (TextUtils.equals(PocApplication.sUserNumber + "", channel.getCreateType())) {
                        items = new String[]{"进入群组", "删除群组"};
                    } else {
                        items = new String[]{"进入群组"};
                    }
                    new AlertDialog.Builder(v.getContext()).setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                PocEngineFactory.get().joinChannel(channel.getChannelNumber() + "");
                                if (homeViewPager != null) {
                                    homeViewPager.setCurrentItem(0);
                                }
                            } else {
                                PocEngineFactory.get().removeAudioPTTChannel(channel.getChannelNumber(), new IPocEngineEventHandler.Callback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean aBoolean) {
                                        if (aBoolean != null && aBoolean) {
                                            Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                            channels.remove(channel);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return channels.size();
        }
    }
}
