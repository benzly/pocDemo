
# PocSDK

[![N|Solid](https://s.beta.gtimg.com/rdmimg/exp/image2/2018/06/08/_27617a9f-5695-4cd8-ac5a-a05fe10f7525.png)](https://nodesource.com/products/nsolid)

一个可使用极简API实现以下功能的SDK

  - 音频通话
  - 视频通话
  - 即时消息
  - 集群对讲
  - 视频监控
  - 位置上报
  - 一键报警

-------------------
[TOC]


### 集成

根目录build.gradle文件中
```groovy
allprojects {
    repositories {
        jcenter()
        maven { url 'https://dl.bintray.com/huamai/maven' }
    }
}
dependencies {
    implementation 'com.huamai:poc:版本号'
}
```

&nbsp
### ProGuard
暂时不支持混淆

&nbsp
### 兼容性
  - **Minimum Android SDK:** pocSDK requires a minimum API level of 21.
  - **Compile Android SDK:** pocSDK requires you to compile against API 21 or later.
  - **依赖的3方库:** pocSDK内部包含了以下jar包，集成时需注意版本冲突
   -- bugly_crash.jar
   -- greendao-2.2.1.jar
   -- gson-1.7.1.jar
   -- okhttp-2.7.5.jar
   -- simple-xml-2.3.4.jar
   -- uvcCamera.jar
   -- implementation 'com.squareup.okhttp3:okhttp:3.10.0'
   -- implementation 'org.greenrobot:eventbus:3.1.1'
   -- compileOnly 'com.android.support:support-v4:25.3.1'
   -- compileOnly BaiduLBS_Android.jar(百度地图全量包)



&nbsp
### 使用说明
 - 熟悉[javadocs](./doc/index.html)
 - 参考Demo

&nbsp
####初始化
自定义Application继承PocApplication
```java
YourApplication extends PocApplication
```

在Application.onCreate时，进行初始化 (注意只在主进程中初始化)
```java
PocEngineFactory.initialize(this, configure)
```

初始化时配置参数 (具体参数含义，请看后边的`设置项`)
```java
//ip、port是必须的，其它参数不设置会自动使用默认值
PocEngine.Configure configure = new PocEngine.Configure();
configure.ip = "xxx";
configure.port = "xxx";
configure.useMusicStream = false;
configure.gpsMode = IPocEngineEventHandler.GpsMode.NET_AND_GPS
...
PocEngineFactory.initialize(this, configure);
```
核心接口类
```java
//使用mPocEngine调用各种接口
PocEngine mPocEngine = PocEngineFactory.get();
```


&nbsp
####事件监听
添加事件监听，可多处进行注册监听，建议在Application中监听来电和IM消息
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        //需要什么事件回调，重写IPocEngineEventHandler中相应的方法即可
};
mPocEngine.addEventHandler(mPocEventHandler);
```
  > **注意：**退出界面时，必须移除相应的EventHandler，否则会引起内存泄露


&nbsp
####登录和退出
```java
//帐号登录
mPocEngine.login(uid, password);
//唯一码登录
mPocEngine.login(sign);
//登录时相关的回调，该过程可能需要几秒，可根据不同状态显示相应的UI
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
		@Override
		public void onLoginStepProgress(int progress, String msg) {
			if (progress == LoginProgress.PRO_BINDING_ACCOUNT_START) {
			    //开始绑定唯一码
			} else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_NOT_EXIST) {
                //唯一码不存在
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_NOT_ACTIVE) {
                //唯一码未激活
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_FAILED) {
                //唯一码绑定失败
            }

            //如果是账号登录，上面的步骤会跳过
            else if (progress == LoginProgress.PRO_LOGIN_START) {
				//登录开始
			} else if (progress == LoginProgress.PRO_ACCOUNT_VERIFY) {
				//验证帐号
			} else if (progress == LoginProgress.PRO_CONN_SERVER) {
				//连接SIP服务
			} else if (progress == LoginProgress.PRO_SYNC_CONTACTS) {
				//同步联系人
			} else if (progress == LoginProgress.PRO_SYNC_CHANNEL) {
				//同步频道列表
			} else if (progress == LoginProgress.PRO_LOGIN_FAILED) {
				//登录失败
			} else if (progress == LoginProgress.PRO_LOGIN_SUCCESS) {
				//登录成功
			}
		}
};

//退出
mPocEngine.logout()

//被强制下线(挤线)
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public boolean onForcedOffline(int progress, String msg) {
		//return true，说明处理了该次挤线事件()，sdk不会再弹dialog
		//return false, sdk会弹出对话框，无法再进行别的操作
	}
}
```
> **注意：**为了避免莫名奇妙的问题，调用退出后建议kill一下应用进程


&nbsp
####联系人&频道列表

Bean字段说明
```java
//用户
class User {
    String name;//用户名
    Long number;//用户号码
    Integer status;//"1"：在线，"0"：离线
    String type;//用户类型
    private String level;//用户级别
}

//频道
class Channel{
	String ChannelName;//频道名
    Long channelNumber;//频道号
    String channelType;//频道类型，"4"：语音对讲组，其它类型暂不支持
    String createType;//"admin"：
    String isDefault;//"1"：默认组
}
```
> **注意：**User.getNumber()和Channel.getChannelNumber()即代表uid和channelId (历史遗留问题)，SDK中使用到的uid或channelId，都可以使用两个方法返回的number

获取列表
```java
//获取所有用户列表，包含联系人列表和组织架构外的人，比如对讲组里的人，不一定是联系人列表中的人
mPocEngine.getUserList();

//获取联系人列表
mPocEngine.getContactsUserList();

//根据id获取User
mPocEngine.getUser(String uid);

//获取当前登录账号
mPocEngine.getCurrentUser();

//获取调度台用户列表
mPocEngine.getDispatcherUserList();

//获取频道列表
mPocEngine.getChannelList();

//获取语音对讲频道列表
mPocEngine.getAudioPttChannelList();
```

数据发生改变
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onChannelChangedEvent(ChannelChangedEvent event) {
		//频道数据发生改变，比如新增组，删除组，组内添加成员，组内删除成员，目前还未能细分具体事件
		//收到该通知，如果在频道列表或频道成员列表UI界面，建议刷新一遍数据
	}
	@Override
	public void onUserStatusChanged(List<User> changeUsers) {
		//用户状态发生改变
	}
}
```

&nbsp
####音视频通话
发起一个语音通话
```java
mPocEngine.makeAudioCall("uid");
//相应的回调
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onRemoteRinging(long sessionId, long uid) {
	        //远端正在响铃
        }
        @Override
        public void onCallConnected(long sessionId, long uid, int sessionType) {
	        //会话已建立，根据sessionType判断是语音、视频、回传呼叫
        }
        @Override
        public void onSessionDisconnected(long sessionId, int reason) {
	        //会话已断开
        }
};
```

发起一个视频通话
```java
mPocEngine.makeVideoCall("uid");
//相应的回调
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onRemoteRinging(long sessionId, long uid) {
	        //远端正在响铃
        }
        @Override
        public void onCallConnected(long sessionId, long uid, int sessionType) {
            if (sessionType == SessionType.TYPE_VIDEO_CALL) {
	            //视频会话已建立，将远端及本地视频view添加到自己的布局中
	            View localSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);

	            View remoteSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.REMOTE);
	            ...
            }
            else if (sessionType == SessionType.TYPE_VIDEO_MONITOR_CALL) {
				//视频回传，只显示本地画面即可，没有远端画面
				View localSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);
	            ...
			}
        }
        @Override
        public void onSessionDisconnected(long sessionId, int reason) {
            //通话已断开，将远端及本地视频view从自己的布局中移除
            ....
        }
};
```

发起一个视频回传
```java
mPocEngine.makeVideoMonitorCall("dispatcherUid");
//相应的回调
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onRemoteRinging(long sessionId, long uid) {
	        //远端正在响铃
        }
        @Override
        public void onCallConnected(long sessionId, long uid, int sessionType) {
            if (sessionType == SessionType.TYPE_VIDEO_MONITOR_CALL) {
				//视频回传，只显示本地画面即可，没有远端画面
				View localSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);
	            ...
			}
        }
        @Override
        public void onSessionDisconnected(long sessionId, int reason) {
            //通话已断开，将本地视频view从自己的布局中移除
            ....
        }
};
```

通话控制
```java
//免提
mPocEngine.setEnableSpeakerphone(true, sessionId);

//静音
mPocEngine.muteMic();

//仅语音(关闭画面推送)，如果设置true时，需要同时将本地预览画面(SurfaceView)从布局中移除，才会停止推流
mPocEngine.pauseVideo(!mPocEngine.isVideoOnPause());

//切换摄像头
mPocEngine.switchCamera();

//挂断
mPocEngine.hangUpCall(long sessionId);
```

收到一个来电请求
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onIncoming(long sessionId, int sessionType, long callerId, String extra) {
	        //来电, 使用sessionType区分来电类型 @see IPocEngineEventHandler.SessionType
        }
        @Override
        public void onIgnoreIncoming(int sessionType, long callerId) {
	        //来电被忽略，因为当前正在通话
        }
};
```
挂断一个通话或拒绝一个来电
```java
mPocEngine.hangUpCall(long sessionId);
```


&nbsp
####音视频监控
由控制台发起，APP端会收到监控开始和结束的通知
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onBackstageMonitorStart(String monitorUid) {
		//监控开始
    }
    @Override
    public void onBackstageMonitorStop(String monitorUid) {
		//监控结束
    }
}
```


&nbsp
####集群对讲

判断默认对讲组
```java
"1".equels(Channel.getIsDefault());
```
>一般后台会配置一个默认对讲组，以便应用登录时，进入该对讲组


进入对讲组
```java
//"channelId"为Channel.getChannelNumber()
mPocEngine.joinChannel("channelId");
```
>这是一个异步过程，需要监听对讲组状态，具体看后边Ptt事件订阅

离开对讲组
```java
//"channelId"为Channel.getChannelNumber()
mPocEngine.leaveChannel("channelId");
```

申请话语权
```java
mPocEngine.applyPttSpeak();
```

释放话语权
```java
mPocEngine.freePttSpeak();
```

Ptt事件订阅
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onPttStateEvent(String channelId, int state) {
		if (TextUtils.isEmpty(channelId)) {
			//当前无附着频道
		} else {
            if (state == IPocEngineEventHandler.PttState.CONNECTED) {
		        //当前已经连接
            } else if (state == IPocEngineEventHandler.PttState.CONNECTING) {
	            //频道连接中
            } else {
                //频道未连接
            }
		}
    }
    @Override
    public void onPttIdle() {
		//对讲组当前空闲，无人讲话
    }
    @Override
    public void onPttRequesting() {
		//本机正在申请话语权
	}
	@Override
    public void onPttKicked() {
	    //自己讲话时，话语权被抢
	}
	@Override
    public onPttMemberChanged(int total, boolean join, String which) {
	    //对讲组成员变化
	}
	@Override
    public void onPttTalking(boolean isRemote, String uid, int total) {
	    //申请话语权成功，开始讲话
	}
	@Override
    public void onPttVoiceDecibelChanged(int decibel, boolean isRemote) {
	    //当前对讲组说话声音的分贝值变化
	}

	@Override
    public void onPttDisband(String channelId) {
	    //对讲组解散
	}

	@Override
    public void onChannelChangedEvent(ChannelChangedEvent event) {
		//对讲组数据发生改变，需要根据action值分别处理

		//发生对讲组切换，此时刷新对讲UI
		if (event.action == ChannelChangedEvent.ACTION_SWITCH) {
            //调用该接口，会重新得到 public void onPttStateEvent(String channelId, int state) 回调
            PocEngineFactory.get().pttStateNotify();
        }
        //组内成员发生变化，此时刷新对讲组成员UI
        else if (event.action == ChannelChangedEvent.ACTION_ADD_USER
            || event.action == ChannelChangedEvent.ACTION_DEL_USER
            || event.action == ChannelChangedEvent.ACTION_EDIT_USER) {
                //TODO
            }
        //新增或删除组，此时刷新组列表UI
        else if (event.action == ChannelChangedEvent.ACTION_ADD_GROUP
	        || event.action == ChannelChangedEvent.ACTION_DEL_GROUP) {
                //TODO
            }
        }

	@Override
    public void onPttError(int code) {
	    //发生错误，收到该回调时，恢复UI
	}
}
```

获取当前对讲组内在线的成员
```java
mPocEngine.getInChannelUsers()
```

创建 一个临时语音对讲组
```java
mPocEngine.createTempAudioPTTChannel(List<User> memberList);
```

创建 一个固定语音对讲组
```java
createAudioPTTChannel(String name, List<User> memberList, IPocEngineEventHandler.Callback cb);
```


&nbsp
####即时消息

Bean字段说明
```java
//会话Bean
class MessageDialogue {
    //会话id，如果是点对点IM，则为远端用户id，如果是群组IM，则为群组id，如果是广播IM，则为113开头的随机数
	long chat_id;
    String name;//会话名
    Long update_time;//会话最后消息的更新时间，用于列表排序
    isGroup;//是否是群消息
    String member;//成员串，多个uid使用#连接
    int unread;//会话内未读消息数量
}
//消息Bean
class ChatMessage {
    long chat_id;//对话id，跟MessageDialogue.chat_id为同一个
    long remote_number;//远端号码
    String remote_name;//远端名称
    Integer msg_status;//消息已读状态
    Integer sip_status;//消息发送状态
    int category;//消息类型
    Boolean is_out;//发出还是收到
    String text;//消息文本
    Long time_stamp;//消息的时间戳
    String httpFile;//消息文件地址
    int download_status;//消息文件下载状态
    int updown_progress;//消息文件上传、下载进度
    String location;//位置描述
    String media_resolution;//图片、视频分辨率，eg:1920*1080
}
```
>未标出字段一般为用不到的字段


新建IM对话
```java
//点对点IM
User remoteUser;
MessageDialogue md = mPocEngine.createMessageDialogueIfNeed(remoteUser);

//群组IM
Channel channel;
MessageDialogue md = mPocEngine.createMessageDialogueIfNeed(channel);

//群组广播
List<User> remoteUsers;
MessageDialogue md = mPocEngine.createBroadcastMessageDialogue(List<User> memberList);
```
>只有主动发起的消息会话，才需要创建一个MessageDialogue，比如在通迅录中勾选联系人发起IM或广播

发送文本消息
```java
ChatMessage message = ChatUtil.createOutTextMessage(String text, long chatId);
mPocEngine.sendMessage(message);
```

发送音频消息
```java
//filePath: 音频文件本地路径，支持格式.amr.mp3
ChatMessage message = ChatUtil.createOutAudioMessage(String filePath, long chatId);
mPocEngine.sendMessage(message);
```

发送图片消息
```java
//filePath: 图片文件本地路径
ChatMessage message = ChatUtil.createOutImgMessage(String filePath, long chatId);
mPocEngine.sendMessage(message);
```

发送视频消息
```java
//filePath: 视频文件本地路径，支持文件格式.mp4
ChatMessage message = ChatUtil.createOutVideoMessage(String filePath, long chatId);
mPocEngine.sendMessage(message);
```

发送位置消息
```java
//jd_wd: 精度和维度，使用"_"连接起来的String串； locationDesc: 坐标点的位置描述
ChatMessage message = ChatUtil.createOutLocationMessage(String jd_wd,String locationDesc, long chatId);
mPocEngine.sendMessage(message);
```

发送自定义消息
```java
//远端接收消息的uid，即为会话的chatId
String chatId = chatId;
//随机生成一个消息id
String messageId = ChatUtil.createRandomMessageId(chatId);
//定义消息类型，需要 > ChatMessageCategory.CUSTOM_BASE
int messageCategory = ChatMessageCategory.CUSTOM_BASE + 1;
//将扩展的内容封装成json，设置到text字段中
String text = {xxx,xxx}
//并且进行加密，否则会解析出错，收到相应消息类型时，对text字段进行解密
text = Base64.encode(text);

//创建消息Bean
ChatMessage message = new ChatMessage();
message.setId(messageId);
message.setText(text);
message.setChat_id(chatId);
message.setRemote_number(chatId);
message.setTime_stamp(new Date().getTime());
message.setSip_status(ChatMessageStatus.Sip.SENDING);
message.setMsg_status(ChatMessageStatus.Chat.READ);
message.setCategory(messageCategory );
message.setIs_out(true);

//上传自定义消息中的文件
mPocEngine.uploadFile(String localFilePath, int fileType, OnFileUploadListener listener);
//文件上传成功后，再发送消息
mPocEngine.sendMessage(message);
//还可以取消文件上传
mPocEngine.cancelUploadFile(String taskId)
```

消息相关回调
```java
private final IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onMessageFileSendProgressChanged(ChatMessage message) {
		//消息文件上传进度变化
		int progress = message.getUpdown_progress();
	}
	@Override
	public void onMessageArrived(ChatMessage message) {
		//消息送达
	}
	@Override
	public void onMessageSendFailed(ChatMessage message) {
		//消息发送失败
	}
	@Override
	public void onMessageReceived(long chatId, List<ChatMessage> messages) {
	    //接收到新消息
    }
    @Override
	public void onNewConversationCreated(MessageDialogue messageDialogue) {
		//创建了新的会话
	}
}
```


&nbsp
####消息记录

会话记录（类似微信消息列表界面）
```java
//获取会话列表
mPocEngine.getAllConversation();

//获取单个会话
mPocEngine.getConversation(long chatId);
```
消息记录
```java
//获取某个会话中所有的消息
mPocEngine.getConversationMessages(long chatId);

//获取所有通话记录
mPocEngine.getAllCallMessages();

//获取某个会话中最后一条消息
mPocEngine.getLastConversationMessage(long chatId);

//获取某个会话中未读消息数量
mPocEngine.getUnReadMsgCount(long chatId);
```

其它
```java
//设置消息为已读
mPocEngine.markMessageAsRead(ChatMessage message);
mPocEngine.markMessagesAsRead(List<ChatMessage> messages);
mPocEngine.markConversationAsRead(long chatId);

//删除单个消息
mPocEngine.deleteMessage(ChatMessage message);
//删除会话中所有的消息(保留会话记录)
mPocEngine.deleteConversationMessages(long chatId);
//删除会话 (连会话中消息一起删除)
mPocEngine.deleteConversation(long chatId);
```


&nbsp
####事件上报
```java
mPocEngine.reportEvent(String content, List<FilesAttachInfo> attachFiles, List<User> notifyUsers, IPocEngineEventHandler.Callback<String> callback);
```


&nbsp
####位置上报
默认定位
```java
//开启位置上报，可通过以下两个参数控制上报模式
//定位模式
PocEngine.Configure.gpsMode
//上报间隔
PocEngine.Configure.gpsReportInterval
```

禁用SDK内部定位模块，并上报自己的位置信息
```java
//在gradle中去掉sdk内的定位包
implementation(name: '×××××××aar包', ext: 'aar') {
     exclude module: 'com.baidu'
}

//禁用SDK定位，只在第一次初始化时有效，后边再更改这个值不生效
PocEngine.Configure.disableInternalGpsFunc = true

//上报自己的位置信息，定时上报，使用者自己确定 定位模式 以及 上报间隔
mPocEngine.reportLocationChange(String longitude, String latitude, String altitude)
```

监听调度台控制终端的定位模式
```java
private final IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onLocationModeChanged(int gpsModel, int gpsReportInterval)
		//gpsModel: @see IPocEngineEventHandler.GpsMode
		//gpsReportInterval: 单位秒
	}
}
```

获取用户gps坐标
```java
//结果需要自己解析
mPocEngine.getUserGPS(List<User> users, IPocEngineEventHandler.Callback<String> callback)
```

获取用户gps轨迹
```java
//结果需要自己解析
mPocEngine.getGPSTrackList(String uid, String beginTime, String endTime, IPocEngineEventHandler.Callback<String> callback);
```



&nbsp
####一键报警
报警
```java
mPocEngine.alarm();
```
>报警背后的逻辑是给相应的人发送报警短信广播，如果想呼叫调度台，需要APP自己来实现

设置报警短信
```java
mPocEngine.setAlarmMessage(String message);
```

//设置报警时通知的成员列表
```java
mPocEngine.setAlarmNotifyUsers(List<User> users);
```


&nbsp
####定位及地图

百度地图包需要在自己的工程里配置一次
implementation(name: 'poc-sdk-release-v1.2.1', ext: 'aar')
implementation files('libs/BaiduLBS_Android.jar')
并且使用自己工程包名，到百度地图开发者中心，申请一个自己的key，配置到清单文件名
<meta-data
    android:name="com.baidu.lbsapi.API_KEY"
    android:value="xxxxxx" />


如不想使用百度地图sdk，需要设置
```java
PocEngine.Configure.disableInternalGpsFunc = false
```
如果有上报定位的需求，则自己控制调用上报接口，进行上报


&nbsp
####其它接口
```java
//开启或关闭位置上报
mPocEngine.enableReportLocation(boolean enable);

//是否正开启位置上报
mPocEngine.isReportLocationEnable();

/**
 * 设置定位模式
 * @param mode
 * @see IPocEngineEventHandler.GpsMode
 */
mPocEngine.setGpsMode(int mode);

//设置定位上报间隔，单位秒
mPocEngine.setGpsReportInterval(int interval);

//设置不忽略重复的坐标点，即不间断上报，默认false
mPocEngine.setGpsNotIgnoreRepeated(boolean ignore);

//修改密码
mPocEngine.changePassword(String newPassword, IPocEngineEventHandler.Callback<Boolean> callback);

//音量+
mPocEngine.volumeUp(int flag);

//音量-
mPocEngine.volumeDown(int flag);

//设置当前声音通道的音量等级
mPocEngine.setCurrentStreamVolumeLevel(int level, int flag);

//当前声音通道最大的音量等级
mPocEngine.getMaxVolumeLevel();

//当前声音通道的音量登记
mPocEngine.getCurrentVolumeLevel();

//停止来电铃声
mPocEngine.stopInComingRingtone();

//启动视频流录制
mPocEngine.startVideoStreamRecord();

//停止视频录制
mPocEngine.stopVideoStreamRecord();

//当前是否正在视频流录制
mPocEngine.isVideoStreamRecording();
```


&nbsp
####设置项
```java
class PocEngine.Configure {
	/**
	 * 服务器ip
     */
    public String ip;
    /**
     * 服务器端口
     */
    public String port;
    /**
     * 分辨率
     *
     * @see IPocEngineEventHandler.VideoResolution
     */
    public int videoResolution = IPocEngineEventHandler.VideoResolution.RESOLUTION_DEFAULT;
    /**
     * 帧率
     *
     * @see IPocEngineEventHandler.VideoFps
     */
    public int videoFps = IPocEngineEventHandler.VideoFps.FPS_DEFAULT;
    /**
     * 码率
     *
     * @see IPocEngineEventHandler.BitRate
     */
    public int bitRate = IPocEngineEventHandler.BitRate.BITRATE_DEFAULT;
    /**
     * 丢包保护
     */
	public boolean qosFec;
	/**
	 * 丢包保护冗余度，值越大，抗丢包能力越强，但同时需要更多带宽，建议用默认值不变
	 *
     * @see IPocEngineEventHandler.VideoFecRate
     */
	public int videoFecRate = IPocEngineEventHandler.VideoFecRate.FEC_RATE_DEFAULT;
	/**
	 * 开启硬件编码，使用硬件编码视频，省电
	 */
	public boolean hardwareEncode;
	/**
	 * 回声抑制级别，外放音量过大时可适当增加，提高回声及啸叫抑制效果
	 *
	 * @see IPocEngineEventHandler.AudioAgcLevel
	 */
	public int audioAecLevel = IPocEngineEventHandler.AudioAgcLevel.AGC_LEVEL_DEFAULT;
	/**
	 * 音频MIC增益，值越小采集音量越大,0为关闭MIC增益
	 *
	 * @see IPocEngineEventHandler.AudioAgcLevel
	 */
	public int audioAgcLevel = IPocEngineEventHandler.AudioAgcLevel.AGC_LEVEL_DEFAULT;
	/**
	 * 噪声抑制级别，值越大噪声消除效果更好
	 *
	 * @see IPocEngineEventHandler.AudioNsLevel
     */
	public int audioNsLevel = IPocEngineEventHandler.AudioNsLevel.NS_LEVEL_DEFAULT;
	/**
     * 视频横屏
     */
	public boolean videoLandscape;
	/**
     * 来电免打扰
     */
    public boolean callNotDisturb;
    /**
     * 消息免打扰
     */
    public boolean messageNotDisturb;
	/**
     * 使用音乐通道
     */
	public boolean useMusicStream;
	/**
     * 定位模式，默认 LOW_POWER_CONSUMPTION
     *
     * @see IPocEngineEventHandler.GpsMode
     */
	public int gpsMode = IPocEngineEventHandler.GpsMode.LOW_POWER_CONSUMPTION;
	/**
     * 位置上报时间间隔，单位：秒，默认值60秒
     */
	public int gpsReportInterval = 60;
	/**
     * 位置上报，不忽略重复的坐标
     */
    public boolean gpsNotIgnoreRepeated;
    /**
     * 是否打开位置上报
     */
    public boolean locationReportEnable = true;
    /**
     * 禁用内部定位模块，调用后，将不再启动内部集成的百度定位SDK
     * <p>
     * 适用于那种使用非百度定位的情况，如果需要上报位置，可使用专门的上报接口
     */
    public boolean disableInternalGpsFunc;
    /**
     * 默认使用前置摄像头
     */
    public boolean defaultFrontCamera;
    /**
     * 联系人状态刷新时间间隔，单位秒，默认30秒
     */
    public int statusUpdateInterval = 30;
    /**
     * 视频流录制分段时长，单位：分钟
     */
    public int videoStreamRecordPieceMinute = 20;
    /**
     * 优先使用外置sd卡进行存储媒体数据
     */
    public boolean firstExternalSdcard;
    /**
     * 开启对讲回放功能，即开启对讲录音
     */
    public boolean pttPlayback;
    /**
     * 侧键广播事件提供器
     */
    public IPocEngineEventHandler.BroadcastHotKeyActionSupplier broadcastHotKeyActionSupplier;
}
```

&nbsp
更具体的用法请参考demo
Edit by: https://maxiang.io/
