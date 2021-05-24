# PocSDK

<p align="center"><a title="Chinese" href="README.md">🇨🇳 中文简体</a>  |  🇬🇧 English</p>

[![N|Solid](https://s.beta.gtimg.com/rdmimg/exp/image2/2018/06/08/_27617a9f-5695-4cd8-ac5a-a05fe10f7525.png)](https://nodesource.com/products/nsolid)

An SDK that can use the minimal API to achieve the following functions

   - Audio call
   - video call
   - Instant messaging
   - Group intercom
   - Video Surveillance
   - Location report
   - One-key alarm

-------------------
[TOC]

### Integrated
> SDK version(https://bintray.com/benzly/notifications)
 
* In the root directory, edit build.gradle file like this:
```groovy
allprojects {
    repositories {
        jcenter()
       	maven {
            url "http://39.106.29.203:8081/nexus/repository/maven-releases/"
            credentials {
                username = "test"
                password = "hmdev"
            }
        }
    }
}
dependencies {
    implementation 'com.huamai:poc:version'
}
```

&nbsp
### ProGuard
ProGuard is not supported temporarily

&nbsp
### Compatibility
  - **Minimum Android SDK:** pocSDK requires a minimum API level of 21.
  - **Compile Android SDK:** pocSDK requires you to compile against API 21 or later.
   -- bugly_crash.jar
   -- greendao-2.2.1.jar
   -- gson-1.7.1.jar
   -- okhttp-2.7.5.jar
   -- simple-xml-2.3.4.jar
   -- uvcCamera.jar
   -- implementation 'com.squareup.okhttp3:okhttp:3.10.0'
   -- implementation 'org.greenrobot:eventbus:3.1.1'
   -- compileOnly 'com.android.support:support-v4:25.3.1'
   -- compileOnly BaiduLBS_Android.jar(BaiDu Map)



&nbsp
### Instructions for use
 - see[javadocs](./doc/index.html)
 - Demo

&nbsp
####Initialization

```java
//When your application onCreate:
PocEngineFactory.initialize(this, configure)
```

```java
//ip、port is necessary. If other parameters are not set, the default values will be used automatically
PocEngine.Configure configure = new PocEngine.Configure();
configure.ip = "xxx";
configure.port = "xxx";
configure.useMusicStream = false;
configure.gpsMode = IPocEngineEventHandler.GpsMode.NET_AND_GPS
...
PocEngineFactory.initialize(this, configure);
```
Core interface class
```java
PocEngine mPocEngine = PocEngineFactory.get();
```


&nbsp
####Event listener
Add event monitor, you can register and monitor in multiple places, it is recommended to monitor incoming calls and
IM messages in Application
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        //What event callback is needed, just rewrite the corresponding method in IPocEngineEventHandler
};
mPocEngine.addEventHandler(mPocEventHandler);
```
  > **Note：**When exiting the interface, the corresponding EventHandler must be removed, otherwise it will cause memory
  leaks


&nbsp
####Login&Logout
```java
//login with account
mPocEngine.login(uid, password);
//login with imei
mPocEngine.login(sign);

//Related callbacks when logging in, the process may take a few seconds, and the corresponding UI can be displayed
//according to different states
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
		@Override
		public void onLoginStepProgress(int progress, String msg) {
			if (progress == LoginProgress.PRO_BINDING_ACCOUNT_START) {
			    //Begin to bind sign code
			} else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_NOT_EXIST) {
                //Sign code not exits
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_NOT_ACTIVE) {
                //Sign code not active
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_FAILED) {
                //Sign code bind failed
            }

            //If it is an account login, the above steps will be skipped
            else if (progress == LoginProgress.PRO_LOGIN_START) {
				//Login begin
			} else if (progress == LoginProgress.PRO_ACCOUNT_VERIFY) {
				//Verify account
			} else if (progress == LoginProgress.PRO_CONN_SERVER) {
				//Try to connect sip server
			} else if (progress == LoginProgress.PRO_SYNC_CONTACTS) {
				//Try to sync contacts
			} else if (progress == LoginProgress.PRO_SYNC_CHANNEL) {
				//Try to sync channel list
			} else if (progress == LoginProgress.PRO_LOGIN_FAILED) {
				//Login failed
			} else if (progress == LoginProgress.PRO_LOGIN_SUCCESS) {
				//Login success
			}
		}
};

//Logout
mPocEngine.logout()

//Force offline
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public boolean onForcedOffline(int progress, String msg) {
		//return true，Explain that the squeeze event () has been processed, the sdk will no longer pop up the dialog
		//return false, sdk will pop up a dialog box, no other operations can be performed
	}
}
```


&nbsp
####Contacts&Channel list

Bean Field description
```java
class User {
    String name;//account name
    Long number;//account number
    Integer status;//"1"：online，"0"：offline
    String type;//account type
    private String level;//account level
}

class Channel{
	String ChannelName;//Channel name
    Long channelNumber;//Channel number
    String channelType;//Channel type，"4"：audio ptt，Other types are not currently supported
    String createType;//"admin"：
    String isDefault;//"1"：Is default channel
}
```
> **Note：**User.getNumber()、Channel.getChannelNumber() is means uid and channelId
，The uid or channelId used in the SDK can use the number returned by the two methods

Get user list
```java
//Get a list of all users, including the contact list and people outside the organization structure, such as people in
//the intercom group, not necessarily the people in the contact list
mPocEngine.getUserList();

//Get contacts list
mPocEngine.getContactsUserList();

//Get user by uid
mPocEngine.getUser(String uid);

//Get current login account
mPocEngine.getCurrentUser();

//Get disaptch user list
mPocEngine.getDispatcherUserList();

//Get channel list
mPocEngine.getChannelList();

//Get audio ptt channel list
mPocEngine.getAudioPttChannelList();
```

Channel list or user list has change
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onChannelChangedEvent(ChannelChangedEvent event) {
	}
	@Override
	public void onUserStatusChanged(List<User> changeUsers) {
	}
}
```

&nbsp
####AudioVideo Call
Make a audio call
```java
mPocEngine.makeAudioCall("uid");
//Event callback
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onRemoteRinging(long sessionId, long uid) {
	        //Remote which be calling is ringing
        }
        @Override
        public void onCallConnected(long sessionId, long uid, int sessionType) {
	        //The session has been established, according to the sessionType to determine whether it is a voice,
	        //video, or return call
        }
        @Override
        public void onSessionDisconnected(long sessionId, int reason) {
	        //Session disconnected
        }
};
```

Start a video call
```java
mPocEngine.makeVideoCall("uid");
//Event callback
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onRemoteRinging(long sessionId, long uid) {
	        //Remote which be calling is ringing
        }
        @Override
        public void onCallConnected(long sessionId, long uid, int sessionType) {
            if (sessionType == SessionType.TYPE_VIDEO_CALL) {
	            //The video session has been established, add the remote and local video views to your layout
	            View localSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);

	            View remoteSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.REMOTE);
	            ...
            }
            else if (sessionType == SessionType.TYPE_VIDEO_MONITOR_CALL) {
				//Video return, only display local screen, no remote screen
				View localSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);
	            ...
			}
        }
        @Override
        public void onSessionDisconnected(long sessionId, int reason) {
            //The call has been disconnected, remove the remote and local video views from their layout
            ....
        }
};
```

Make a video monitor
```java
mPocEngine.makeVideoMonitorCall("dispatcherUid");
//Event callback
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onRemoteRinging(long sessionId, long uid) {
	        //Remote which be calling is ringing
        }
        @Override
        public void onCallConnected(long sessionId, long uid, int sessionType) {
            if (sessionType == SessionType.TYPE_VIDEO_MONITOR_CALL) {
				//Video return, only display local screen, no remote screen
				View localSurfaceView = mPocEngine.
	            getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);
	            ...
			}
        }
        @Override
        public void onSessionDisconnected(long sessionId, int reason) {
            //The call is disconnected, remove the local video view from its layout
            ....
        }
};
```

Call control
```java
//Hands free
mPocEngine.setEnableSpeakerphone(true, sessionId);

//Mute
mPocEngine.muteMic();

//Voice only (screen push is turned off), if set to true, the local preview screen (SurfaceView) needs to be
//removed from the layout at the same time to stop the push
mPocEngine.pauseVideo(!mPocEngine.isVideoOnPause());

//Switch camera
mPocEngine.switchCamera();

//Hand up call
mPocEngine.hangUpCall(long sessionId);
```

Received a call request
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
        @Override
        public void onIncoming(long sessionId, int sessionType, long callerId, String extra) {
	        //Incoming call, use sessionType to distinguish the call type @see IPocEngineEventHandler.SessionType
        }
        @Override
        public void onIgnoreIncoming(int sessionType, long callerId) {
	        //Incoming call was ignored because there is currently a call
        }
};
```
Hang up a call or reject an incoming call
```java
mPocEngine.hangUpCall(long sessionId);
```


&nbsp
####AudioVideo monitor
Initiated by the console, the APP will receive notification of the start and end of monitoring
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onBackstageMonitorStart(String monitorUid) {
		//Begin monitor
    }
    @Override
    public void onBackstageMonitorStop(String monitorUid) {
		//End monitor
    }
}
```


&nbsp
####PTT

Determine the default intercom group
```java
"1".equels(Channel.getIsDefault());
```
>Generally, a default intercom group will be configured in the background so that when the app logs in,
it can enter the intercom group


Enter an audio channel
```java
//"channelId"为Channel.getChannelNumber()
mPocEngine.joinChannel("channelId");
```
>This is an asynchronous process, you need to monitor the status of the intercom group, see Ptt event subscription later

Level channel group
```java
//"channelId" is Channel.getChannelNumber()
mPocEngine.leaveChannel("channelId");
```

apply ptt speak
```java
mPocEngine.applyPttSpeak();
```

free ptt speak
```java
mPocEngine.freePttSpeak();
```

Ptt event
```java
private final IPocEngineEventHandler mPocEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onPttStateEvent(String channelId, int state) {
		if (TextUtils.isEmpty(channelId)) {
			//No channels currently attached
		} else {
            if (state == IPocEngineEventHandler.PttState.CONNECTED) {
		        //Current ptt group is connected
            } else if (state == IPocEngineEventHandler.PttState.CONNECTING) {
	            //Current ptt group is connecting
            } else {
                //Current ptt group is disconnected
            }
		}
    }
    @Override
    public void onPttIdle() {
		//Current ptt group is idle, No one speaks
    }
    @Override
    public void onPttRequesting() {
		//This machine is applying for the right to speak
	}
	@Override
    public void onPttKicked() {
	    //When I speak, the right to speak is robbed
	}
	@Override
    public onPttMemberChanged(int total, boolean join, String which) {
	    //Intercom group membership changes
	}
	@Override
    public void onPttTalking(boolean isRemote, String uid, int total) {
	    //Successfully applied for the right to speak and started speaking
	}
	@Override
    public void onPttVoiceDecibelChanged(int decibel, boolean isRemote) {
	    //The decibel value change of the current intercom group's speaking voice
	}

	@Override
    public void onPttDisband(String channelId) {
	    //Ptt group is disband
	}

	@Override
    public void onChannelChangedEvent(ChannelChangedEvent event) {
		//Intercom group data has changed and needs to be processed separately according to the action value

		//Intercom group switching occurs, refresh the intercom UI at this time
		if (event.action == ChannelChangedEvent.ACTION_SWITCH) {
            //Trigger onPttStateEvent() callback
            PocEngineFactory.get().pttStateNotify();
        }
        //Group members change, refresh the intercom group member UI at this time
        else if (event.action == ChannelChangedEvent.ACTION_ADD_USER
            || event.action == ChannelChangedEvent.ACTION_DEL_USER
            || event.action == ChannelChangedEvent.ACTION_EDIT_USER) {
                //TODO
            }
        //Add or delete a group, refresh the group list UI at this time
        else if (event.action == ChannelChangedEvent.ACTION_ADD_GROUP
	        || event.action == ChannelChangedEvent.ACTION_DEL_GROUP) {
                //TODO
            }
        }

	@Override
    public void onPttError(int code) {
	    //When an error occurs, the UI will be restored when the callback is received
	}
}
```

Get the online members in the current intercom group
```java
mPocEngine.getInChannelUsers()
```

Create a temporary voice intercom group
```java
mPocEngine.createTempAudioPTTChannel(List<User> memberList);
```

Create a fixed voice intercom group
```java
createAudioPTTChannel(String name, List<User> memberList, IPocEngineEventHandler.Callback cb);
```


&nbsp
####IM

Bean Field description
```java
class MessageDialogue {
    //Session id, if it is a point-to-point IM, it is the remote user id, if it is a group IM, it is the group id,
    //if it is a broadcast IM, it is a random number starting with 113
	long chat_id;
    String name;//session name
    Long update_time;//The update time of the last message of the conversation, used to sort the list
    isGroup;//Is it a group message
    String member;//Member string, multiple uids like this: 111#222#333....
    int unread;//Number of unread messages in the conversation
}
class ChatMessage {
    long chat_id;//Dialogue id, which is the same as MessageDialogue.chat_id
    long remote_number;
    String remote_name;
    Integer msg_status;//Message read status
    Integer sip_status;//Message send status
    int category;//Message category
    Boolean is_out;//Sent or received
    String text;//Text msg
    Long time_stamp;
    String httpFile;//Message file path
    int download_status;//Message file download status
    int updown_progress;//Message file upload and download progress
    String location;//location desc
    String media_resolution;//Picture, video resolution，eg:1920*1080
}
```


Create IM conversation
```java
//Point-to-point IM
User remoteUser;
MessageDialogue md = mPocEngine.createMessageDialogueIfNeed(remoteUser);

//Group IM
Channel channel;
MessageDialogue md = mPocEngine.createMessageDialogueIfNeed(channel);

//Broadcast IM
List<User> remoteUsers;
MessageDialogue md = mPocEngine.createBroadcastMessageDialogue(List<User> memberList);
```
>Only actively initiated message conversations, you need to create a MessageDialogue, for example, check contacts
in the newsletter to initiate IM or broadcast

Send text message
```java
ChatMessage message = ChatUtil.createOutTextMessage(String text, long chatId);
mPocEngine.sendMessage(message);
```

Send audio message
```java
//filePath: support .amr .mp3 .wav
ChatMessage message = ChatUtil.createOutAudioMessage(String filePath, long chatId);
mPocEngine.sendMessage(message);
```

Send image message
```java
//filePath: support .png .jpg
ChatMessage message = ChatUtil.createOutImgMessage(String filePath, long chatId);
mPocEngine.sendMessage(message);
```

Send video message
```java
//filePath: support.mp4
ChatMessage message = ChatUtil.createOutVideoMessage(String filePath, long chatId);
mPocEngine.sendMessage(message);
```

Send location message
```java
//jd_wd: "Longitude_latitude"
ChatMessage message = ChatUtil.createOutLocationMessage(String jd_wd,String locationDesc, long chatId);
mPocEngine.sendMessage(message);
```

Send custom message
```java
//The uid of the remote message received, which is the chatId of the session
String chatId = chatId;
//Randomly generate a message id
String messageId = ChatUtil.createRandomMessageId(chatId);
//Custom message type, must > ChatMessageCategory.CUSTOM_BASE
int messageCategory = ChatMessageCategory.CUSTOM_BASE + 1;
//Encapsulate the extended content into json and set it in the text field
String text = {xxx,xxx}
//And encrypt, otherwise it will parse error, when receiving the corresponding message type, decrypt the text field
text = Base64.encode(text);

//Create message bean
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

//Upload files in custom message
mPocEngine.uploadFile(String localFilePath, int fileType, OnFileUploadListener listener);
//Send a message after the file is uploaded successfully
mPocEngine.sendMessage(message);
//You can also cancel file upload
mPocEngine.cancelUploadFile(String taskId)
```

IM event callback
```java
private final IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onMessageFileSendProgressChanged(ChatMessage message) {
		//Message file upload progress changes
		int progress = message.getUpdown_progress();
	}
	@Override
	public void onMessageArrived(ChatMessage message) {
	}
	@Override
	public void onMessageSendFailed(ChatMessage message) {
	}
	@Override
	public void onMessageReceived(long chatId, List<ChatMessage> messages) {
    }
    @Override
	public void onNewConversationCreated(MessageDialogue messageDialogue) {
	}
}
```


&nbsp
####Message history

Session record
```java
//Get a list of sessions
mPocEngine.getAllConversation();

//Get a single session
mPocEngine.getConversation(long chatId);
```

```java
//Get all the messages in a conversation
mPocEngine.getConversationMessages(long chatId);

//Get all call records
mPocEngine.getAllCallMessages();

//Get the last message in a conversation
mPocEngine.getLastConversationMessage(long chatId);

//Get the number of unread messages in a conversation
mPocEngine.getUnReadMsgCount(long chatId);
```

Other
```java
//Set message as read
mPocEngine.markMessageAsRead(ChatMessage message);
mPocEngine.markMessagesAsRead(List<ChatMessage> messages);
mPocEngine.markConversationAsRead(long chatId);

//Delete a single message
mPocEngine.deleteMessage(ChatMessage message);
//Delete all messages in the conversation (keep conversation records)
mPocEngine.deleteConversationMessages(long chatId);
//Delete conversation (delete even the messages in the conversation)
mPocEngine.deleteConversation(long chatId);
```


&nbsp
####Event Report
```java
mPocEngine.reportEvent(String content, List<FilesAttachInfo> attachFiles, List<User> notifyUsers, IPocEngineEventHandler.Callback<String> callback);
```


&nbsp
####Location Report
```java
//Turn on position reporting, the reporting mode can be controlled by the following two parameters
//Positioning mode
PocEngine.Configure.gpsMode
PocEngine.Configure.gpsReportInterval
```

Disable the SDK internal positioning module and report your own location information
```java
//Disable SDK positioning, it is only effective at the first initialization, and changing this value later will not take effect
PocEngine.Configure.disableInternalGpsFunc = true

//Report your own location information, report regularly, the user determines the positioning mode and the reporting interval
mPocEngine.reportLocationChange(String longitude, String latitude, String altitude)
```

Monitor the positioning mode of the control terminal
```java
private final IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {
	@Override
	public void onLocationModeChanged(int gpsModel, int gpsReportInterval)
		//gpsModel: @see IPocEngineEventHandler.GpsMode
		//gpsReportInterval: Unit second
	}
}
```

Get user GPS coordinates
```java
//The result needs to be parsed by yourself
mPocEngine.getUserGPS(List<User> users, IPocEngineEventHandler.Callback<String> callback)
```

Obtain user GPS track
```java
//The result needs to be parsed by yourself
mPocEngine.getGPSTrackList(String uid, String beginTime, String endTime, IPocEngineEventHandler.Callback<String> callback);
```



&nbsp
####One key alarm
Alarm
```java
mPocEngine.alarm();
```
>The logic behind the alarm is to send an alarm SMS broadcast to the corresponding person. If you want to call
the dispatcher, you need to implement it by the APP itself.

Set alarm SMS
```java
mPocEngine.setAlarmMessage(String message);
```

//Set up a list of members to be notified when alarms
```java
mPocEngine.setAlarmNotifyUsers(List<User> users);
```


&nbsp
####Other
```java
//Turn on or off location reporting
mPocEngine.enableReportLocation(boolean enable);

//Whether you are opening location reporting
mPocEngine.isReportLocationEnable();

/**
 * Set positioning mode
 * @param mode
 * @see IPocEngineEventHandler.GpsMode
 */
mPocEngine.setGpsMode(int mode);

//Set the positioning report interval, in seconds
mPocEngine.setGpsReportInterval(int interval);

//Set not to ignore repeated coordinate points, that is, report continuously, the default is false
mPocEngine.setGpsNotIgnoreRepeated(boolean ignore);

mPocEngine.changePassword(String newPassword, IPocEngineEventHandler.Callback<Boolean> callback);

//Volume+
mPocEngine.volumeUp(int flag);

//Volume-
mPocEngine.volumeDown(int flag);

//Set the volume level of the current sound channel
mPocEngine.setCurrentStreamVolumeLevel(int level, int flag);

//The maximum volume level of the current sound channel
mPocEngine.getMaxVolumeLevel();

//The volume level of the current sound channel
mPocEngine.getCurrentVolumeLevel();

//Stop ringing
mPocEngine.stopInComingRingtone();

//Start video stream recording
mPocEngine.startVideoStreamRecord();

//Stop video recording
mPocEngine.stopVideoStreamRecord();

//Whether video streaming is currently being recorded
mPocEngine.isVideoStreamRecording();
```


&nbsp
####Settings
```java
class PocEngine.Configure {
	/**
	 * Server IP
     */
    public String ip;
    /**
     *Server Port
     */
    public String port;
    /**
     * Video Resolution
     *
     * @see IPocEngineEventHandler.VideoResolution
     */
    public int videoResolution = IPocEngineEventHandler.VideoResolution.RESOLUTION_DEFAULT;
    /**
     * Video Fps
     *
     * @see IPocEngineEventHandler.VideoFps
     */
    public int videoFps = IPocEngineEventHandler.VideoFps.FPS_DEFAULT;
    /**
     * Video BitRate
     *
     * @see IPocEngineEventHandler.BitRate
     */
    public int bitRate = IPocEngineEventHandler.BitRate.BITRATE_DEFAULT;
    /**
     * Video QosFec
     */
	public boolean qosFec;
	/**
	 * Redundancy of packet loss protection, the larger the value, the stronger the ability to resist packet loss,
	 * but at the same time more bandwidth is required. It is recommended to use the default value unchanged
	 *
     * @see IPocEngineEventHandler.VideoFecRate
     */
	public int videoFecRate = IPocEngineEventHandler.VideoFecRate.FEC_RATE_DEFAULT;
	/**
	 * Turn on hardware encoding, use hardware to encode video, save power
	 */
	public boolean hardwareEncode;
	/**
	 * Echo suppression level, when the loudspeaker volume is too loud, it can be increased appropriately to
	 * improve the echo and howling suppression effect
	 *
	 * @see IPocEngineEventHandler.AudioAgcLevel
	 */
	public int audioAecLevel = IPocEngineEventHandler.AudioAgcLevel.AGC_LEVEL_DEFAULT;
	/**
	 * Audio MIC gain, the smaller the value, the greater the volume of the collection, 0 means to close the MIC gain
	 *
	 * @see IPocEngineEventHandler.AudioAgcLevel
	 */
	public int audioAgcLevel = IPocEngineEventHandler.AudioAgcLevel.AGC_LEVEL_DEFAULT;
	/**
	 * Noise suppression level, the larger the value, the better the noise elimination effect
	 *
	 * @see IPocEngineEventHandler.AudioNsLevel
     */
	public int audioNsLevel = IPocEngineEventHandler.AudioNsLevel.NS_LEVEL_DEFAULT;
	/**
     * Video Landscape
     */
	public boolean videoLandscape;
	/**
     * Do not disturb
     */
    public boolean callNotDisturb;
    /**
     * Do not disturb
     */
    public boolean messageNotDisturb;
	/**
     * Use music stream to play
     */
	public boolean useMusicStream;
	/**
     * Location Mode
     *
     * @see IPocEngineEventHandler.GpsMode
     */
	public int gpsMode = IPocEngineEventHandler.GpsMode.LOW_POWER_CONSUMPTION;
	/**
     * Time interval for position reporting, unit: second, default value 60 seconds
     */
	public int gpsReportInterval = 60;
	/**
     * Position reporting, do not ignore repeated coordinates
     */
    public boolean gpsNotIgnoreRepeated;
    /**
     * Whether to open location reporting
     */
    public boolean locationReportEnable = true;
    /**
     * Disable the internal positioning module, after calling, the internal integrated Baidu positioning SDK will no longer be started
     * <p>
     * Applicable to the situation where non-Baidu positioning is used. If you need to report the location,
     * you can use a special reporting interface
     */
    public boolean disableInternalGpsFunc;
    /**
     * Use the front camera by default
     */
    public boolean defaultFrontCamera;
    /**
     * Contact status refresh interval, in seconds, the default is 30 seconds
     */
    public int statusUpdateInterval = 30;
    /**
     * Video stream recording segment time, unit: minute
     */
    public int videoStreamRecordPieceMinute = 20;
    /**
     * Prioritize the use of external SD cards for storing media data
     */
    public boolean firstExternalSdcard;
    /**
     * Turn on the intercom playback function, that is, turn on the intercom recording
     */
    public boolean pttPlayback;
    /**
     * Side key broadcast event provider
     */
    public IPocEngineEventHandler.BroadcastHotKeyActionSupplier broadcastHotKeyActionSupplier;
}
```

&nbsp
For more specific usage, please refer to demo
Edit by: https://maxiang.io/
