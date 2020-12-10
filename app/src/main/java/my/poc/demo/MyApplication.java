package my.poc.demo;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngine;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.chat.ChatMessageCategory;
import com.huamai.poc.chat.ChatMessageStatus;
import com.huamai.poc.greendao.ChatMessage;
import com.unionbroad.app.PocApplication;
import com.unionbroad.app.eventbus.ChannelChangedEvent;
import com.unionbroad.app.util.FileDownloadManager;
import com.unionbroad.app.util.Logger;
import com.unionbroad.app.util.UnionUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import my.poc.demo.activity.AvActivity;


public class MyApplication extends MultiDexApplication {

    private Logger logger = Logger.getLogger("MyApplication");

    @Override
    public void onCreate() {
        super.onCreate();

        /** 防止反复初始化 */
        if (getPackageName() != null && getPackageName().equals(getProcessName(android.os.Process.myPid()))) {
            initBroadcast();
            PocEngineFactory.initialize(this, getConfig());
            //可以在Application中监听，也可以在Service中监听，主要希望全局监听来电和新消息
            PocEngineFactory.get().addEventHandler(pocEventHandler);
        }
    }

    private PocEngine.Configure getConfig() {
        PocEngine.Configure configure = PocEngineConfigureProducer.getConfig();
        /** 可选配置 */
        //定位模式
        configure.gpsMode = IPocEngineEventHandler.GpsMode.NET_AND_GPS;
        //位置上报时间间隔，单位秒
        configure.gpsReportInterval = 300;
        //联系人状态刷新时间间隔，单位秒
        configure.statusUpdateInterval = 60;
        //使用音乐通道
        configure.useMusicStream = false;
        //对讲录音
        configure.pttPlayback = true;
        //防杀服务
        configure.keepLiveService = true;
        //视频通话分辨率，需要和调度台中设置的值一样才能生效
        configure.videoResolution = IPocEngineEventHandler.VideoResolution.RESOLUTION_640X480;
        //缓存提供器，没特殊要求用不到
        //configure.cacheFileSupplier = cacheFileSupplier;
        //侧边功能键注册，一般都用不到
        //configure.broadcastHotKeyActionSupplier = hotKeyActionSupplier;
        //其它...
        return configure;
    }

    /**
     * 目前配置项较多，很多配置没有单独的设置接口，建议全局保持一个Configure，
     * 确保每次 PocEngineFactory.get().config()时，设置进去的配置项都是有效值
     */
    public static class PocEngineConfigureProducer {

        static PocEngine.Configure configure;

        public static PocEngine.Configure getConfig() {
            if (configure == null) {
                configure = new PocEngine.Configure();
                /** 必须设置 */
                configure.ip = "123.57.6.84";
                configure.port = "5060";
                configure.httpPort = "80";
            }
            return configure;
        }
    }

    /**
     * 缓存文件路径及格式提供器
     */
    private final IPocEngineEventHandler.CacheFileSupplier cacheFileSupplier = new IPocEngineEventHandler.CacheFileSupplier() {
        @Override
        public String getBaseDir() {
            //TODO 返回一个缓存根目录路径，需要创建
            return null;
        }

        @Override
        public String getFileNameExpansion() {
            //TODO 返回扩展文件名，即 用户id + ????? + .格式 中问号这一段，缺省时，会按照日期的格式
            return null;
        }

        /**
         * 每需要缓存一个文件时，会调用一次这个接口，根据后缀返回相应的全路径，路径要创建好
         * @param fileFormat 文件后缀
         * @return
         */
        @Override
        public String getFullCacheFilePath(String fileFormat) {
            String baseCache;
            if (fileFormat.contains("3pg") || fileFormat.contains("mp4")) {
                baseCache = UnionUtils.getVideoCachePath();
            } else if (fileFormat.contains("jpg") || fileFormat.contains("png")) {
                baseCache = UnionUtils.getPicCachePath();
            } else if (fileFormat.contains("raw") || fileFormat.contains("wav")
                    || fileFormat.contains("amr") || fileFormat.contains("mp3")) {
                baseCache = UnionUtils.getAudioFileCachePath();
            } else {
                baseCache = UnionUtils.getBaseCachePath();
            }


            Date date = new Date();
            String ymd = new SimpleDateFormat("yyyyMMdd").format(date);
            String saveDir = baseCache + File.separator + ymd;
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            StringBuilder builder = new StringBuilder();
            builder.append(PocApplication.sUserNumber);
            builder.append("_");
            builder.append(ymd);
            builder.append("_");
            builder.append("test");
            builder.append(fileFormat);
            String fileName = builder.toString();

            String path = new File(saveDir, fileName).getPath();
            return path;
        }
    };


    /**
     * 机器PTT热键适配：
     * 如果集成了SDK的应用，想要支持多款机器的热键(侧边功能键)，可以在这里进行配置
     */
    private final IPocEngineEventHandler.BroadcastHotKeyActionSupplier hotKeyActionSupplier = new IPocEngineEventHandler.BroadcastHotKeyActionSupplier() {
        @Override
        public String[] getPttDownActions() {
            return new String[]{
                    "phone1.test.ptt.down",
                    "phone2.test.ptt.down"
            };
        }

        @Override
        public String[] getPttUpActions() {
            return new String[]{
                    "phone1.test.ptt.up",
                    "phone2.test.ptt.up"
            };
        }
    };

    private final IPocEngineEventHandler pocEventHandler = new IPocEngineEventHandler() {

        @Override
        public void onMessageReceived(long chatId, List<ChatMessage> messages) {
            logger.d("onMessageReceived-> " + chatId + " size=" + messages.size());
            //收到新的消息
            if (messages != null && messages.size() > 0) {
                //仅为调试，只取出其中一条
                ChatMessage message = messages.get(0);
                //语音通话记录
                if (message.getCategory() == ChatMessageCategory.AUDIO) {
                    logger.i("==> 语音通话记录");
                }
                //视频通话记录
                else if (message.getCategory() == ChatMessageCategory.VIDEO) {
                    logger.i("==> 视频通话记录");
                }
                //语音广播
                else if (message.getCategory() == ChatMessageCategory.AUDIO_BROADCAST) {
                    logger.i("==> 语音广播");
                }
                //语音文件，类似微信的语音消息
                else if (message.getCategory() == ChatMessageCategory.AUDIO_FILE) {
                    logger.i("==> 语音消息");
                }
                //对讲录音，只有开启对讲回放功能，每次有人讲话时，才会把声音录制下来
                else if (message.getCategory() == ChatMessageCategory.PTT_AUDIO_FILE) {
                    logger.i("==> 对讲录音消息");
                }
                //视频消息，类似微信的视频消息
                else if (message.getCategory() == ChatMessageCategory.VIDEO_FILE) {
                    logger.i("==> 视频消息: " + message.getDownload_status());
                    switch (message.getDownload_status()) {
                        case ChatMessageStatus.File.DOWN_SUCCESSED:
                            message.setDownload_status(ChatMessageStatus.File.DOWN_SUCCESSED);
                            //play
                            break;
                        case ChatMessageStatus.File.DOWN_UNINIT:
                            message.setDownload_status(ChatMessageStatus.File.DOWN_STARTING);
                            FileDownloadManager.getInstance().startDownTask(message.getId(), message.getHttpFile(), message.getLocalFile());
                            //使用EventBus监听ChatFileDownloadCompletedEvent，获取文件下载结果
                            break;
                    }
                }
                //图片消息
                else if (message.getCategory() == ChatMessageCategory.IMAGE) {
                    logger.i("==> 图片消息");
                }
                //位置消息
                else if (message.getCategory() == ChatMessageCategory.LOCATION) {
                    logger.i("==> 位置消息");
                }
                //文字消息
                else if (message.getCategory() == ChatMessageCategory.TEXT) {
                    logger.i("==> 文字消息: " + message.getText());
                }

                //================以下两种消息，如果业务需求，可以放到动态页中去=========================
                //拍传@消息
                else if (message.getCategory() == ChatMessageCategory.NOTIFICATION_REPORT) {
                    logger.i("==> 拍传@消息");
                }
                //报警消息
                else if (message.getCategory() == ChatMessageCategory.ALERT) {
                    logger.i("==> 报警消息: " + message.getText());
                }
                //任务消息
                else if (message.getCategory() == ChatMessageCategory.NOTIFICATION_TASK) {
                    logger.i("==> 任务消息: " + message.getText());
                }
                //==================================================================================

                //其它消息一般用不到
                else {
                    logger.i("==> 其它消息: " + message.getText());
                }
            }
        }

        @Override
        public void onIncoming(IncomingInfo incomingInfo) {
            logger.d("onIncoming-> sessionId=" + incomingInfo.sessionId + " type=" + incomingInfo.sessionType + " uid=" +
                    incomingInfo.callerId + " name=" + incomingInfo.callerName + " level=" + incomingInfo.level +
                    " extra=" + incomingInfo.extra);
            Intent intent = new Intent(MyApplication.this, AvActivity.class);
            intent.putExtra("sessionId", incomingInfo.sessionId);
            intent.putExtra("callerId", incomingInfo.callerId);
            intent.putExtra("callerName", incomingInfo.callerName);
            intent.putExtra("type", incomingInfo.sessionType);
            intent.putExtra("extra", incomingInfo.extra);
            intent.putExtra("isIncomingCall", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.this.startActivity(intent);
        }

        @Override
        public void onIgnoreIncoming(int sessionType, long callerId) {
            super.onIgnoreIncoming(sessionType, callerId);
        }

        @Override
        public void onChannelChangedEvent(ChannelChangedEvent event) {
            Toast.makeText(getApplicationContext(), "onChannelChangedEvent", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onForcedOffline() {
            //True: 响应了下线事件，False：没有响应，sdk内部会弹出对话框，禁止再操作
            Toast.makeText(getApplicationContext(), "onForcedOffline", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onBackstageMonitorAction(int action, long sessionId, String monitorUid) {
            logger.i("onBackstageMonitorAction: action=" + action);
        }

        @Override
        public void onReceiveLocation(double latitude, double longitude, String address) {
            Toast.makeText(getApplicationContext(), "坐标变化(" + latitude + "," + longitude + ")", Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onRequestCamera() {
            Toast.makeText(getApplicationContext(), "请求Camera", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReleaseCamera() {
            Toast.makeText(getApplicationContext(), "释放Camera", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestMic() {
            Toast.makeText(getApplicationContext(), "请求Mic", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReleaseMic() {
            Toast.makeText(getApplicationContext(), "释放Mic", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVideoStreamTakePictureFinish(String filePath) {
            Toast.makeText(getApplicationContext(), "已保存到: " + filePath, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallConnected(long sessionId, String remoteId, int sessionType) {
        }

    };

    private String getProcessName(int pid) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : runningApps) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }

    private SDKReceiver mReceiver;

    private void initBroadcast() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    //定位模块状态通知
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            logger.d("SDKReceiver: " + s);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                logger.d("key 验证出错! 错误码");
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                logger.d("key 验证成功! 功能可以正常使用");
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                logger.d("网络出错");
            }
        }
    }
}
