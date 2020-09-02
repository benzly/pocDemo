package my.poc.demo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.common.FilesAttachInfo;
import com.huamai.poc.common.TempChannel;
import com.huamai.poc.greendao.Channel;
import com.huamai.poc.greendao.User;
import com.huamai.poc.media.MediaFileUtil;
import com.unionbroad.app.eventbus.ChannelChangedEvent;
import com.unionbroad.app.util.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import my.poc.demo.R;
import my.poc.demo.activity.AvActivity;
import my.poc.demo.widget.BigPttButton;

public class PttFragment extends Fragment {

    Logger logger = Logger.getLogger("PttFragment");

    View rootView;
    TextView status;
    ImageView voice;
    TextView timer;
    TextView remoteTalking;
    BigPttButton pttButton;
    TextView channelName;
    TextView member;
    boolean isLocalApplyTalk;

    long mTalkTimeSecond;
    static final SimpleDateFormat sDurationTimerFormat = new SimpleDateFormat("mm:ss:SSS");


    public PttFragment() {
    }

    public static PttFragment newInstance() {
        return new PttFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.home_fragment_ptt, container, false);
            initViews();
        }
        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PocEngineFactory.get().addEventHandler(mEventHandler);

        //恢复UI时，再次得到对讲组状态的回调
        PocEngineFactory.get().pttStateNotify();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PocEngineFactory.get().removeEventHandler(mEventHandler);
    }

    private void initViews() {
        status = (TextView) rootView.findViewById(R.id.status);
        voice = (ImageView) rootView.findViewById(R.id.voice);
        timer = (TextView) rootView.findViewById(R.id.timer);
        remoteTalking = (TextView) rootView.findViewById(R.id.remote_talking);
        pttButton = (BigPttButton) rootView.findViewById(R.id.ptt_button);
        channelName = (TextView) rootView.findViewById(R.id.channel_name);
        member = (TextView) rootView.findViewById(R.id.member);

        pttButton.setOnPressAction(new BigPttButton.OnPressAction() {
            @Override
            public void onPressDownFull() {
                if (!isLocalApplyTalk) {
                    isLocalApplyTalk = true;
                    PocEngineFactory.get().applyPttSpeak();
                }
            }

            @Override
            public void onPressUp() {
                if (isLocalApplyTalk) {
                    isLocalApplyTalk = false;
                    PocEngineFactory.get().freePttSpeak();
                }
            }
        });

        /**============ 功能按钮 ============**/
        //退出对讲组
        rootView.findViewById(R.id.ptt_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Channel channel = PocEngineFactory.get().getCurrentPttChannel();
                if (channel != null) {
                    boolean ret = PocEngineFactory.get().leaveChannel(channel.getChannelNumber() + "");
                    Toast.makeText(getActivity(), "离开" + channel.getChannelName() + (ret ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //退出临时组，并回到默认组
        rootView.findViewById(R.id.ptt_exit_and_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Channel channel = PocEngineFactory.get().getCurrentPttChannel();
                //如果是一个临时组，才进行操作
                if (channel != null && channel instanceof TempChannel) {
                    boolean ret = PocEngineFactory.get().leaveChannel(channel.getChannelNumber() + "");
                    Toast.makeText(getActivity(), "离开" + channel.getChannelName() + (ret ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //查找一个默认组，并加入。 默认组，需要在后台配置
                            List<Channel> pttChannels = PocEngineFactory.get().getAudioPttChannelList();
                            if (pttChannels != null) {
                                for (Channel pttChannel : pttChannels) {
                                    if ("1".equals(pttChannel.getIsDefault())) {
                                        PocEngineFactory.get().joinChannel(pttChannel.getChannelNumber() + "");
                                        return;
                                    }
                                }
                            }
                        }
                    }, 500);
                }
            }
        });

        //视频回传
        rootView.findViewById(R.id.video_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //只能传入调度台帐号，否则会直接返回false
                Intent intent = new Intent(getContext().getApplicationContext(), AvActivity.class);
                intent.putExtra("callerId", "2008");
                intent.putExtra("type", IPocEngineEventHandler.SessionType.TYPE_VIDEO_MONITOR_CALL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().getApplicationContext().startActivity(intent);
            }
        });
        //拍传
        rootView.findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowserFile();
            }
        });
        //报警
        rootView.findViewById(R.id.take_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ret = PocEngineFactory.get().alarm();
                Toast.makeText(getActivity(), ret ? "报警成功" : "报警失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTalkTimer() {
        timer.setVisibility(View.VISIBLE);
        String time = sDurationTimerFormat.format(mTalkTimeSecond * 100);
        if (time.length() >= 2) {
            // 00:00:000，去掉最后毫秒里的两个0
            time = time.substring(0, time.length() - 2);
        }
        timer.setText(time);
        timer.postDelayed(mTalkTimerTiming, 100);

        voice.setImageResource(R.drawable.ic_voice0);
        voice.setVisibility(View.VISIBLE);
    }

    private void stopTalkTimer() {
        timer.setVisibility(View.GONE);
        timer.removeCallbacks(mTalkTimerTiming);
        mTalkTimeSecond = 0;

        voice.setVisibility(View.GONE);
    }

    private Runnable mTalkTimerTiming = new Runnable() {
        @Override
        public void run() {
            mTalkTimeSecond++;
            startTalkTimer();
        }
    };

    public void openBrowserFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 10086);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10086) {
            try {
                Uri uri = data.getData();
                if (uri == null) {
                    return;
                }
                FilesAttachInfo attachInfo = new FilesAttachInfo(MediaFileUtil.getPath(getActivity(), uri));
                List<FilesAttachInfo> attachFiles = new ArrayList<>();
                attachFiles.add(attachInfo);

                //========随机取两个联系人======
                List<User> notifyUsers = new ArrayList<>();
                List<User> all = PocEngineFactory.get().getContactsUserList();
                for (User user : all) {
                    if (user.getNumber() == 115 || user.getNumber() == 2008) {
                        notifyUsers.add(user);
                    }
                }
                //============================
                PocEngineFactory.get().reportEvent("test", attachFiles, notifyUsers, new IPocEngineEventHandler.Callback<String>() {
                    @Override
                    public void onResponse(String s) {
                        logger.d("reportEvent-> " + s);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getStr(int state) {
        if (state == IPocEngineEventHandler.PttState.CONNECTED) {
            return "CONNECTED";
        } else if (state == IPocEngineEventHandler.PttState.CONNECTING) {
            return "CONNECTING";
        } else if (state == IPocEngineEventHandler.PttState.DISCONNECT) {
            return "DISCONNECT";
        } else {
            return "DISCONNECT";
        }
    }

    final IPocEngineEventHandler mEventHandler = new IPocEngineEventHandler() {

        @Override
        public void onPttStateEvent(long sessionId, String channelId, int state) {
            logger.d("onPttStateEvent: channelId=" + channelId + " state=" + getStr(state));

            if (TextUtils.isEmpty(channelId)) {
                status.setText("当前无附着频道");
                member.setText("");
            } else {
                //当前已经连接
                if (state == IPocEngineEventHandler.PttState.CONNECTED) {
                    onPttIdle();
                }
                //频道连接中
                else if (state == IPocEngineEventHandler.PttState.CONNECTING) {
                    status.setText("正在连接");
                }
                //频道未连接
                else {
                    status.setText("未连接");
                }
            }

            Channel channel = PocEngineFactory.get().getCurrentPttChannel();
            channelName.setText(channel != null ? channel.getChannelName() : "");
        }

        /**
         * 当前对讲组空闲
         */
        @Override
        public void onPttIdle() {
            logger.d("onPttIdle");
            pttButton.onStateChanged(BigPttButton.STATE_IDLE);

            status.setText("空闲");
            status.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.media_idle), null, null, null);
            remoteTalking.setVisibility(View.GONE);
            stopTalkTimer();
        }

        /**
         * 正在申请话语权
         */
        @Override
        public void onPttRequesting() {
            logger.d("onPttRequesting");
            pttButton.onStateChanged(BigPttButton.STATE_REQUEST);

            remoteTalking.setVisibility(View.GONE);
            voice.setVisibility(View.GONE);
            status.setVisibility(View.VISIBLE);
            status.setText("申请中");
            status.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.media_listen), null, null, null);
        }

        /**
         * 话语权被抢
         */
        @Override
        public void onPttKicked() {
            logger.d("onPttKicked");
        }

        /**
         * 对讲组成员有人加入或退出
         *
         * @param total 总人数
         * @param join  True:有人加入  False:有人退出
         * @param which 为空时：表示：加入或退出成员id, 如果是-1标示刚进入频道的通知，并且此时频道是idle状态
         */
        @Override
        public void onPttMemberJoinOrLeave(int total, boolean join, String which) {
            //如果是-1标示刚进入频道的通知，并且此时频道是idle状态，一般来说，只需要处理非"-1"的情况
            if ("-1".equals(which)) {
            }
            logger.i("onPttMemberJoinOrLeave: " + which + " " + (join ? "加入" : "离开")
                    + " users=" + PocEngineFactory.get().getInChannelUsers());
        }

        @Override
        public void onPttMemberChanged(ArrayList<String> members) {
            int totalCount = 0;
            Channel channel = PocEngineFactory.get().getCurrentPttChannel();
            if (channel != null) {
                totalCount = channel.getUsers().size();

                if (channel instanceof TempChannel) {
                    //是一个用#号分割的id串
                    ((TempChannel) channel).getTempMemberStr();
                } else {
                    channel.getUsers();
                }

            }
            member.setText("当前组内在线人数：" + members.size() + "  总人数：" + totalCount);
        }

        /**
         * 有人在讲话
         *
         * @param isRemote True:远端有人讲话  False:本机正在讲话
         * @param uid      正在讲话者Id
         * @param total    当前对讲组总人数
         * @param fromChannelNumber 当前组
         */
        @Override
        public void onPttTalking(boolean isRemote, String uid, int total, long fromChannelNumber) {
            logger.d("onPttTalking, isRemote=" + isRemote);
            if (isRemote) {
                pttButton.onStateChanged(BigPttButton.STATE_R_TALKING);
            } else {
                pttButton.onStateChanged(BigPttButton.STATE_I_TALKING);
            }

            if (isRemote) {
                stopTalkTimer();

                User user = PocEngineFactory.get().getUser(uid);
                String name = user != null ? user.getName() : uid;
                String tips = name + " 正在讲话";
                remoteTalking.setText(tips);
                remoteTalking.setVisibility(View.VISIBLE);

                status.setText("");
                status.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.media_listen), null, null, null);

                voice.setImageResource(R.drawable.ic_laba0);
                voice.setVisibility(View.VISIBLE);
            } else {
                startTalkTimer();

                remoteTalking.setVisibility(View.GONE);
                status.setText("");
                status.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.media_talk), null, null, null);
            }
        }

        @Override
        public void onPttVoiceDecibelChanged(int decibel, boolean isRemote) {
            if (isRemote) {
                if (voice != null && voice.getVisibility() == View.VISIBLE) {
                    if (decibel <= 20) {
                        voice.setImageResource(R.drawable.ic_laba0);
                    } else if (decibel > 20 && decibel <= 30) {
                        voice.setImageResource(R.drawable.ic_laba1);
                    } else if (decibel > 30 && decibel <= 32) {
                        voice.setImageResource(R.drawable.ic_laba2);
                    } else if (decibel > 32 && decibel <= 34) {
                        voice.setImageResource(R.drawable.ic_laba3);
                    } else if (decibel > 34 && decibel <= 36) {
                        voice.setImageResource(R.drawable.ic_laba4);
                    } else if (decibel > 36 && decibel <= 38) {
                        voice.setImageResource(R.drawable.ic_laba5);
                    } else if (decibel > 38 && decibel <= 40) {
                        voice.setImageResource(R.drawable.ic_laba6);
                    } else if (decibel > 40 && decibel <= 42) {
                        voice.setImageResource(R.drawable.ic_laba7);
                    } else if (decibel > 42 && decibel <= 44) {
                        voice.setImageResource(R.drawable.ic_laba8);
                    } else if (decibel > 44 && decibel <= 46) {
                        voice.setImageResource(R.drawable.ic_laba9);
                    } else {
                        voice.setImageResource(R.drawable.ic_laba10);
                    }
                }
            } else {
                if (voice.getVisibility() == View.VISIBLE) {
                    if (decibel <= 20) {
                        voice.setImageResource(R.drawable.ic_voice0);
                    } else if (decibel > 20 && decibel <= 30) {
                        voice.setImageResource(R.drawable.ic_voice0);
                    } else if (decibel > 30 && decibel <= 32) {
                        voice.setImageResource(R.drawable.ic_voice2);
                    } else if (decibel > 32 && decibel <= 34) {
                        voice.setImageResource(R.drawable.ic_voice3);
                    } else if (decibel > 34 && decibel <= 36) {
                        voice.setImageResource(R.drawable.ic_voice4);
                    } else if (decibel > 36 && decibel <= 38) {
                        voice.setImageResource(R.drawable.ic_voice5);
                    } else if (decibel > 38 && decibel <= 40) {
                        voice.setImageResource(R.drawable.ic_voice6);
                    } else if (decibel > 40 && decibel <= 42) {
                        voice.setImageResource(R.drawable.ic_voice7);
                    } else if (decibel > 42 && decibel <= 44) {
                        voice.setImageResource(R.drawable.ic_voice8);
                    } else if (decibel > 44 && decibel <= 46) {
                        voice.setImageResource(R.drawable.ic_voice9);
                    } else {
                        voice.setImageResource(R.drawable.ic_voice10);
                    }
                }
            }
        }

        /**
         * 对讲组解散
         * @param channelId 对讲组id
         */
        @Override
        public void onPttDisband(String channelId) {
            Toast.makeText(getActivity(), "对讲组解散", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChannelChangedEvent(ChannelChangedEvent event) {
            //发生对讲组切换，此时刷新对讲UI
            if (event.action == ChannelChangedEvent.ACTION_SWITCH) {
                //调用该接口，会重新得到 public void onPttStateEvent(String channelId, int state) 回调
                PocEngineFactory.get().pttStateNotify();
            }
            //组内成员发生变化，此时刷新对讲组成员UI
            else if (event.action == ChannelChangedEvent.ACTION_ADD_USER || event.action == ChannelChangedEvent.ACTION_DEL_USER || event.action == ChannelChangedEvent.ACTION_EDIT_USER) {
                //TODO
            }
            //新增或删除组，此时刷新组列表UI
            else if (event.action == ChannelChangedEvent.ACTION_ADD_GROUP || event.action == ChannelChangedEvent.ACTION_DEL_GROUP) {
                //TODO
            }
        }
    };
}
