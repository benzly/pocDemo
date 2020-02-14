package my.poc.demo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngine;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.chat.ChatMessageStatus;
import com.huamai.poc.greendao.Channel;
import com.huamai.poc.greendao.ChatMessage;
import com.huamai.poc.greendao.Department;
import com.huamai.poc.greendao.MessageDialogue;
import com.huamai.poc.greendao.User;
import com.tencent.bugly.crashreport.CrashReport;
import com.unionbroad.app.manager.TTSManager;
import com.unionbroad.app.util.Logger;

import java.util.List;

import my.poc.demo.MyApplication;
import my.poc.demo.R;
import my.poc.demo.activity.AvActivity;
import my.poc.demo.activity.ChatMessageActivity;
import my.poc.demo.activity.MapActivity;


public class OtherFragment extends Fragment {

    private Logger logger = Logger.getLogger("OtherFragment");
    private View rootView;
    private TextView console;
    private LinearLayout funcContentLayout;
    private PocEngine pocEngine;
    private Handler handler = new Handler();

    public OtherFragment() {
    }

    public static OtherFragment newInstance() {
        return new OtherFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.home_fragment_other, container, false);
            initView();
        }
        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
        return rootView;
    }

    private void initView() {
        pocEngine = PocEngineFactory.get();

        console = (TextView) rootView.findViewById(R.id.console);
        funcContentLayout = (LinearLayout) rootView.findViewById(R.id.func_content_layout);

        showDepartments();
        showDispatchUsers();
        showSDKVersionInfo();
        showLastLocationReportInfo();
        showTTSFunc();
        showChannelListenFunc();
        showAllListenChannelIds();
        showCancelAllListenChannels();

        addAlarmFunc();
        addGlobalPttFunc();
        addUserGPSFunc();
        addGPSTrackListFunc();
        addReportLocationFunc();
        addGetInChannelUsersFunc();
        addCreateMessageDialogueIfNeedFunc();
        addRefreshUserStatusFunc();
        addOpenOrCloseLocationReport();
        addSetGpsModeFunc();
        addSetGpsReportIntervalfunc();
        addSetGpsNotIgnoreRepeatedFunc();
        addChangePasswordFunc();
        addVolumeUpFunc();
        addVolumeDownFunc();
        addVolumeSetFunc();
        addGetMaxVolumeLevelFunc();
        addMapViewFunc();
        showMessageUnReadFunc();
        showSetMessageAsReadFunc();
        showCrashFunc();
    }

    private void showDepartments() {
        Button button = new Button(getContext());
        button.setText("获取部门架构");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Department> list = pocEngine.getDepartments();
                StringBuilder sb = new StringBuilder();
                for (Department user : list) {
                    sb.append(user.getName()).append(",");
                }
                console.setText(sb.toString());
            }
        });
    }

    private void showDispatchUsers() {
        Button button = new Button(getContext());
        button.setText("所有调度台账号");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<User> list = pocEngine.getDispatcherUserList();
                StringBuilder sb = new StringBuilder();
                for (User user : list) {
                    sb.append(user.getNumber()).append(",");
                }
                console.setText(sb.toString());
            }
        });
    }

    private void showSDKVersionInfo() {
        Button button = new Button(getContext());
        button.setText("显示SDK版本号");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                console.setText(pocEngine.getSDKVersion());
            }
        });
    }

    private void showLastLocationReportInfo() {
        Button button = new Button(getContext());
        button.setText("显示最新一次位置上报内容");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IPocEngineEventHandler.LocationReportInfo info =
                        pocEngine.getLastLocationReportInfo();
                StringBuilder sb = new StringBuilder();
                sb.append("jd=" + info.jd + " wd=" + info.wd + " time=" + info.time);
                console.setText(sb.toString());
            }
        });
    }

    private void showTTSFunc() {
        Button button = new Button(getContext());
        button.setText("调用TTS");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TTSManager", "onclick");
                TTSManager.getInstance().speaking("你好，我来自 SDK", TTSManager.MODE_NORMAL);
            }
        });
    }

    private void showCancelAllListenChannels() {
        Button button = new Button(getContext());
        button.setText("取消正在监听的频道");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Channel> channels = pocEngine.getListeningChannels();
                int delayTime = 500;
                for (final Channel channel : channels) {
                    funcContentLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //监听和取消监听都是一个轻度阻塞的过程，建议不要同时操作多个
                            pocEngine.cancelListenChannel(channel.getChannelNumber() + "");
                        }
                    }, delayTime);
                    delayTime = +delayTime;
                }
            }
        });


    }

    private void showAllListenChannelIds() {
        Button button = new Button(getContext());
        button.setText("显示正在监听的频道");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Channel> channels = pocEngine.getListeningChannels();
                StringBuilder sb = new StringBuilder();
                for (Channel channel : channels) {
                    sb.append(channel.getChannelName() + " - ");
                }
                console.setText(sb.toString());
            }
        });
    }

    private void showChannelListenFunc() {
        Button button = new Button(getContext());
        button.setText("监听一个频道");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Channel> channels = pocEngine.getChannelList();
                if (channels.size() > 0) {
                    boolean ret = pocEngine.listenChannel(channels.get(0).getChannelNumber() + "");
                    //已经进入的群组，再调用监听会失败
                    if (!ret) {
                        Toast.makeText(getActivity(), "监听失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void showCrashFunc() {
        Button button = new Button(getContext());
        button.setText("主动引发异常，测试进程重启");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CrashReport.testJavaCrash();
                //CrashReport.testANRCrash();
                //CrashReport.testNativeCrash();
                //CrashReport.testBlockCrash();
            }
        });
    }

    private void showSetMessageAsReadFunc() {
        Button button = new Button(getContext());
        button.setText("设置一条未读消息为已读");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage unReadMessage = null;
                List<MessageDialogue> dialogues = pocEngine.getAllConversation();
                for (MessageDialogue dialogue : dialogues) {
                    List<ChatMessage> messages = pocEngine.getConversationMessages(dialogue.getChat_id());
                    for (ChatMessage message : messages) {
                        if (message.getMsg_status() == ChatMessageStatus.Chat.UNREAD) {
                            unReadMessage = message;
                            break;
                        }
                    }
                    if (unReadMessage != null) {
                        break;
                    }
                }

                if (unReadMessage != null) {
                    //设置已读
                    pocEngine.markMessageAsRead(unReadMessage);

                    //显示最新未读数量
                    int unRead = 0;
                    for (MessageDialogue dialogue : dialogues) {
                        unRead = unRead + dialogue.getUnread();
                    }
                    console.setText("最新未读消息数：" + unRead);
                } else {
                    Toast.makeText(getContext(), "当前没有未读消息", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showMessageUnReadFunc() {
        Button button = new Button(getContext());
        button.setText("未读消息数");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<MessageDialogue> dialogues = pocEngine.getAllConversation();
                int unRead = 0;
                for (MessageDialogue dialogue : dialogues) {
                    unRead = unRead + dialogue.getUnread();
                }
                console.setText("未读消息：" + unRead);
            }
        });
    }

    private void addMapViewFunc() {
        Button button = new Button(getContext());
        button.setText("显示地图控件");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), MapActivity.class));
            }
        });
    }

    private void addGetMaxVolumeLevelFunc() {
        Button button = new Button(getContext());
        button.setText("获取当前声音通道最大档位");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("最大档位: " + pocEngine.getMaxVolumeLevel());
            }
        });
    }

    private void addVolumeSetFunc() {
        Button button = new Button(getContext());
        button.setText("设置音量到5档");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.setCurrentStreamVolumeLevel(5, AudioManager.FLAG_PLAY_SOUND);
            }
        });
    }

    private void addVolumeUpFunc() {
        Button button = new Button(getContext());
        button.setText("当前声音通道，音量+");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.volumeUp(AudioManager.FLAG_PLAY_SOUND);
            }
        });
    }

    private void addVolumeDownFunc() {
        Button button = new Button(getContext());
        button.setText("当前声音通道，音量-");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.volumeDown(AudioManager.FLAG_PLAY_SOUND);
            }
        });
    }

    private void addChangePasswordFunc() {
        Button button = new Button(getContext());
        button.setText("修改密码");
        funcContentLayout.addView(button);

        //FIXME 修改密码接口，目前后台会一直返回错误，需要排查一下
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.changePassword("123456", new IPocEngineEventHandler.Callback<Boolean>() {
                    @Override
                    public void onResponse(Boolean aBoolean) {
                        Toast.makeText(getContext(), aBoolean ? "修改成功" : "修改失败", Toast.LENGTH_SHORT).show();
                        if (aBoolean) {
                            //TODO 这里一般需要弹框提示重启，流程就是杀死自己进程，再定时2秒重启自己
                        }
                    }
                });
            }
        });
    }

    private void addSetGpsNotIgnoreRepeatedFunc() {
        Button button = new Button(getContext());
        button.setText("不间断上报");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean gpsNotIgnoreRepeated = MyApplication.PocEngineConfigureProducer.getConfig().gpsNotIgnoreRepeated;
                gpsNotIgnoreRepeated = !gpsNotIgnoreRepeated;
                pocEngine.setGpsNotIgnoreRepeated(gpsNotIgnoreRepeated);
                MyApplication.PocEngineConfigureProducer.getConfig().gpsNotIgnoreRepeated = gpsNotIgnoreRepeated;
                Toast.makeText(getContext(), gpsNotIgnoreRepeated ? "不间断" : "忽略重复坐标", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSetGpsReportIntervalfunc() {
        Button button = new Button(getContext());
        button.setText("设置GPS上报间隔");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{
                        "5秒",
                        "15秒",
                        "1分钟"

                };
                new AlertDialog.Builder(getContext()).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int interval = which == 0 ? 5 : (which == 1 ? 15 : 60);
                        MyApplication.PocEngineConfigureProducer.getConfig().gpsReportInterval = interval;
                        pocEngine.setGpsReportInterval(interval);
                        Toast.makeText(getContext(), items[which], Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
    }

    private void addSetGpsModeFunc() {
        Button button = new Button(getContext());
        button.setText("设置GPS定位模式");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{
                        "低功耗 " + IPocEngineEventHandler.GpsMode.LOW_POWER_CONSUMPTION,
                        "高精度 " + IPocEngineEventHandler.GpsMode.NET_AND_GPS,
                        "仅gps " + IPocEngineEventHandler.GpsMode.ONLY_GPS

                };
                new AlertDialog.Builder(getContext()).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int gpsMode = which;
                        MyApplication.PocEngineConfigureProducer.getConfig().gpsMode = gpsMode;
                        pocEngine.setGpsMode(gpsMode);
                        Toast.makeText(getContext(), "mode:" + gpsMode, Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
    }

    private void addOpenOrCloseLocationReport() {
        Button button = new Button(getContext());
        button.setText("打开或关闭位置上报");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enable = pocEngine.isReportLocationEnable();
                pocEngine.enableReportLocation(!enable);
                Toast.makeText(getContext(), "位置上报: " + (!enable ? "打开" : "关闭"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAlarmFunc() {
        Button button = new Button(getContext());
        button.setText("一键报警并呼叫");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ret = pocEngine.alarm();
                List<User> dispatcherUserList = pocEngine.getDispatcherUserList();
                Toast.makeText(getContext(), "报警:" + ret, Toast.LENGTH_SHORT).show();
                if (dispatcherUserList != null && dispatcherUserList.size() > 0) {
                    for (User user : dispatcherUserList) {
                        //逻辑就是哪个调度台在线，就呼叫谁（此处为了调试写死呼叫2008）
                        if (user.getStatus() == 1 && user.getNumber() == 2008) {
                            Intent intent = new Intent(getContext().getApplicationContext(), AvActivity.class);
                            intent.putExtra("callerId", user.getNumber() + "");
                            intent.putExtra("type", IPocEngineEventHandler.SessionType.TYPE_VIDEO_MONITOR_CALL);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void addGlobalPttFunc() {
        Button button = new Button(getContext());
        button.setText("显示or隐藏全局ptt按钮");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean show = "1".equals(v.getTag()) ? true : false;
                pocEngine.showGlobalPTTButton(!show);
                v.setTag(!show ? "1" : "0");
            }
        });
    }

    private void addUserGPSFunc() {
        Button button = new Button(getContext());
        button.setText("获取gps坐标");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("请求中");
                pocEngine.getUserGPS(pocEngine.getContactsUserList(), new IPocEngineEventHandler.Callback<String>() {
                    @Override
                    public void onResponse(final String json) {
                        //回调在子线程
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                console.setText(json);
                            }
                        });
                    }
                });
            }
        });
    }

    private void addGPSTrackListFunc() {
        Button button = new Button(getContext());
        button.setText("单个用户gps轨迹");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("请求中");
                pocEngine.getGPSTrackList(pocEngine.getCurrentUser().getNumber() + "", "1970-01-01 00:00", "2020-01-01 00:00", new IPocEngineEventHandler.Callback<String>() {
                    @Override
                    public void onResponse(final String json) {
                        //回调在子线程
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                console.setText(json);
                            }
                        });
                    }
                });
            }
        });
    }

    private void addReportLocationFunc() {
        Button button = new Button(getContext());
        button.setText("主动上报自己位置");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("上报中");
                final String longitude = "116.313946";
                final String latitude = "39.856953";
                pocEngine.reportLocationChange(longitude, latitude, "", "test addr", new IPocEngineEventHandler.Callback<Boolean>() {
                    @Override
                    public void onResponse(final Boolean aBoolean) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                console.setText("上报：[" + longitude + "," + latitude + "] " + aBoolean);
                            }
                        });
                    }
                });
            }
        });
    }

    private void addGetInChannelUsersFunc() {
        Button button = new Button(getContext());
        button.setText("当前对讲组中在线的人");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("对讲组前台在线人： " + pocEngine.getInChannelUsers());
            }
        });
    }

    private void addCreateMessageDialogueIfNeedFunc() {
        Button button = new Button(getContext());
        button.setText("进入当前对讲组的IM界面");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialogue md = pocEngine.createMessageDialogueIfNeed(pocEngine.getCurrentPttChannel());
                ChatMessageActivity.show(getActivity(), md.getChat_id());
            }
        });
    }

    private void addRefreshUserStatusFunc() {
        Button button = new Button(getContext());
        button.setText("主动刷新联系人状态");
        funcContentLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.requestUsersStatusImmediately();
            }
        });
    }
}