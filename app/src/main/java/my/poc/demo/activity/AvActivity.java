package my.poc.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngine;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.greendao.User;
import com.unionbroad.app.util.Logger;

import java.util.ArrayList;
import java.util.List;

import my.poc.demo.R;

public class AvActivity extends Activity {

    Logger logger = Logger.getLogger("AvActivity");
    long sessionId;
    String remoteId;
    String remoteName;
    int type;
    String extra;

    //来电或外呼
    boolean isIncomingCall;
    PocEngine pocEngine = PocEngineFactory.get();
    AlertDialog mOutCallDialog, mInCallDialog;

    boolean temp = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_av);

        sessionId = getIntent().getLongExtra("sessionId", 0);
        remoteId = getIntent().getStringExtra("callerId");
        remoteName = getIntent().getStringExtra("callerName");
        type = getIntent().getIntExtra("type", 0);
        extra = getIntent().getStringExtra("extra");
        isIncomingCall = getIntent().getBooleanExtra("isIncomingCall", false);
        logger.d(">>>>> type=" + type + " isIncomingCall=" + isIncomingCall);

        boolean isVideoCall = type != IPocEngineEventHandler.SessionType.TYPE_AUDIO_CALL;
        ((ViewGroup) findViewById(R.id.btn_voice).getParent()).setVisibility(isVideoCall ? View.VISIBLE : View.GONE);
        ((ViewGroup) findViewById(R.id.btn_switch_camera).getParent()).setVisibility(isVideoCall ? View.VISIBLE : View.GONE);
        ((ViewGroup) findViewById(R.id.video_call_record).getParent()).setVisibility(isVideoCall ? View.VISIBLE : View.GONE);
        ((ViewGroup) findViewById(R.id.video_call_capture).getParent()).setVisibility(isVideoCall ? View.VISIBLE : View.GONE);
        ((ViewGroup) findViewById(R.id.btn_dispatch).getParent()).setVisibility(isVideoCall ? View.VISIBLE : View.GONE);

        //免提
        findViewById(R.id.btn_hangfree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.setEnableSpeakerphone(!pocEngine.isSpeakerphoneEnable());
                Toast.makeText(getApplicationContext(), "当前免提：" + pocEngine.isSpeakerphoneEnable(), Toast.LENGTH_SHORT).show();
            }
        });
        //静音
        findViewById(R.id.video_call_mute_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.muteMic(!pocEngine.isMuteMic());
                Toast.makeText(getApplicationContext(), "当前静音：" + pocEngine.isMuteMic(), Toast.LENGTH_SHORT).show();
            }
        });
        //仅语音(关闭画面推送)
        findViewById(R.id.btn_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 仅是通知对端当前操作，除此之外，还需要移除两个surfaceView
                pocEngine.pauseVideo(!pocEngine.isVideoOnPause());
                if (pocEngine.isVideoOnPause()) {
                    removeLocalVideo();
                    removeRemoteVideo();
                } else {
                    setupLocalVideo();
                    setupRemoteVideo();
                }

                Toast.makeText(getApplicationContext(), "当前画面关闭：" + pocEngine.isVideoOnPause(), Toast.LENGTH_SHORT).show();
            }
        });
        //切换摄像头(如果已经转成仅语音了，应该隐藏这个按钮)
        findViewById(R.id.btn_switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pocEngine.isVideoOnPause()) {
                    pocEngine.switchCamera();
                }
                Toast.makeText(getApplicationContext(), "当前前置摄像头：" + pocEngine.isFrontFacingCamera(), Toast.LENGTH_SHORT).show();
            }
        });
        //挂断
        findViewById(R.id.btn_end_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryHangUp();
            }
        });

        //录像
        findViewById(R.id.video_call_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRecording = pocEngine.isVideoStreamRecording();
                if (isRecording) {
                    pocEngine.stopVideoStreamRecord();
                } else {
                    pocEngine.startVideoStreamRecord();
                }
                isRecording = pocEngine.isVideoStreamRecording();
                ((TextView) findViewById(R.id.union_toolbar_record_tv)).setText(isRecording ? "录制中..." : "已停止");
            }
        });
        //截屏
        findViewById(R.id.video_call_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pocEngine.takeVideoStreamPicture();
            }
        });
        //视频分发
        findViewById(R.id.btn_dispatch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //========随机取两个联系人======
                List<User> notifyUsers = new ArrayList<>();
                List<User> all = PocEngineFactory.get().getContactsUserList();
                for (User user : all) {
                    if (user.getNumber() == 115 || user.getNumber() == 114) {
                        notifyUsers.add(user);
                    }
                }
                //============================
                pocEngine.dispatchVideoEvent(sessionId, notifyUsers);
            }
        });

        //外呼时，挂断弹框
        mOutCallDialog = new AlertDialog.Builder(this)
                .setNegativeButton("挂断", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryHangUp();
                    }
                }).setCancelable(false).create();

        //来电时，接听、挂断弹框
        mInCallDialog = new AlertDialog.Builder(this)
                .setNegativeButton("接听", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        tryAcceptCall();
                    }
                }).setPositiveButton("挂断", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryHangUp();
                    }
                }).setCancelable(false).create();

        //监听必要的事件
        pocEngine.addEventHandler(pocEngineEventHandler);

        if (isIncomingCall) {
            if (type == IPocEngineEventHandler.SessionType.TYPE_AUDIO_PTT_TEMP) {
                mInCallDialog.setTitle(remoteName + " 临时组来电");
            } else if (type == IPocEngineEventHandler.SessionType.TYPE_AUDIO_PTT) {
                mInCallDialog.setTitle(remoteName + " 对讲组来电");
            } else {
                mInCallDialog.setTitle(remoteName + " 来电");
            }
            mInCallDialog.show();
        } else {
            boolean ret = makeCall(remoteId, type);
            if (!ret) {
                AvActivity.this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pocEngine.removeEventHandler(pocEngineEventHandler);
    }

    final IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {

        @Override
        public void onRemoteRinging(long sessionId, long uid) {
            mOutCallDialog.setTitle("远端正在响铃: " + uid);
        }

        @Override
        public void onCallConnected(long sessionId, String uid, int sessionType) {
            logger.d(">>>>>>onCallConnected sessionId=" + sessionId + " uid=" + uid + " sessionType=" + sessionType);
            AvActivity.this.sessionId = sessionId;
            /** 如果是一个视频呼叫，一般远端画面全屏，本地画面右上角小窗口显示 */
            if (sessionType == SessionType.TYPE_VIDEO_CALL || sessionType == SessionType.TYPE_VIDEO_MEETING) {
                setupLocalVideo();
                setupRemoteVideo();
            }
            /** 如果是一个视频回传呼叫，是没有远端画面的，把本地画面全屏显示即可 */
            else if (sessionType == SessionType.TYPE_VIDEO_MONITOR_CALL) {
                setupMonitorVideo();
            }
            mInCallDialog.dismiss();
            mOutCallDialog.dismiss();

            //工具建立通话成功后，才显示
            findViewById(R.id.video_call_menu_panel).setVisibility(View.VISIBLE);
            findViewById(R.id.video_call_menu_panel_vertical).setVisibility(View.VISIBLE);
        }

        @Override
        public void onCallDisconnected(long sessionId, int reason) {
            logger.d(">>>>>>onCallDisconnected sessionId=" + sessionId + " reason=" + reason);
            if (reason == CallEndReason.TERMINATED) {
                showToast("挂断");
            } else if (reason == CallEndReason.REMOTE_BUSY) {
                showToast("对方繁忙");
            } else if (reason == CallEndReason.REMOTE_OFFLINE) {
                showToast("对方离线");
            } else {
                showToast("未定义");
            }
            mInCallDialog.dismiss();
            mOutCallDialog.dismiss();
            AvActivity.this.finish();
        }

        @Override
        public void onIgnoreIncoming(int type, long callerId) {
            //当前已有会话，新的来电被自动拒绝
            showToast("有来电被拒绝: " + callerId);
        }

        @Override
        public void onRemoteVideoPause(boolean pause) {
            if (pause) {
                //去掉两个SurfaceView，并显示一张图片
                removeLocalVideo();
                removeRemoteVideo();
            } else {
                //重新显示SurfaceView
                setupRemoteVideo();
                setupLocalVideo();
            }
        }

        @Override
        public void onVideoStreamRecordFinish(String filePath) {
            if (TextUtils.isEmpty(filePath)) {
                Toast.makeText(getApplicationContext(), "录制失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "文件保存到：" + filePath, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onVideoStreamTakePictureFinish(String filePath) {
            Toast.makeText(getApplicationContext(), "拍照成功->" + filePath, Toast.LENGTH_SHORT).show();
        }
    };

    private void setupLocalVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);

        // ===for debug start
        container.setPadding(5, 5, 5, 5);
        container.setBackgroundColor(Color.RED);
        // ===for debug end

        container.setVisibility(View.VISIBLE);
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }
        View surfaceView = pocEngine.getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);
        if (surfaceView != null) {
            container.addView(surfaceView);
            pocEngine.sendLocalVideo();
        } else {
            logger.d("get local surface view null");
        }
    }

    private void removeLocalVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        container.setVisibility(View.GONE);
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }
    }

    private void setupRemoteVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container.setVisibility(View.VISIBLE);
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }
        View surfaceView = pocEngine.getRendererView(IPocEngineEventHandler.SurfaceType.REMOTE);
        if (surfaceView != null) {
            container.addView(surfaceView);
            container.bringChildToFront(surfaceView);
        } else {
            logger.d("get remote surface view null");
        }
    }

    private void removeRemoteVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container.setVisibility(View.VISIBLE);
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        //一般需要显示一张表示当前为语音通话的图片
        View tipView = new View(this);
        tipView.setBackgroundResource(R.drawable.bg_audio_only);
        tipView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(tipView);
    }

    private void setupMonitorVideo() {
        findViewById(R.id.local_video_view_container).setVisibility(View.GONE);
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container.setVisibility(View.VISIBLE);
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }
        View surfaceView = pocEngine.getRendererView(IPocEngineEventHandler.SurfaceType.LOCAL);
        if (surfaceView != null) {
            //((SurfaceView) surfaceView).setZOrderMediaOverlay(true);
            container.addView(surfaceView);
            pocEngine.sendLocalVideo();
        } else {
            logger.d("get local surface view null");
        }
    }

    private boolean makeCall(String uid, int sessionType) {
        if (TextUtils.isEmpty(uid)) {
            Toast.makeText(this, "无效id", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!pocEngine.hasServiceConnected()) {
            Toast.makeText(this, "服务连接已断开", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sessionType == IPocEngineEventHandler.SessionType.TYPE_VIDEO_CALL) {
            sessionId = pocEngine.makeVideoCall(uid + "", "args");
        } else if (sessionType == IPocEngineEventHandler.SessionType.TYPE_VIDEO_MONITOR_CALL) {
            sessionId = pocEngine.makeVideoMonitorCall(uid);
        } else {
            sessionId = pocEngine.makeAudioCall(uid + "", "args");
        }
        if (sessionId > 0) {
            mOutCallDialog.setTitle("连接中");
            mOutCallDialog.show();
            mInCallDialog.dismiss();
        }
        return sessionId > 0;
    }

    /**
     * 点对点接听 和 对讲组接听
     */
    private void tryAcceptCall() {
        if (type == IPocEngineEventHandler.SessionType.TYPE_AUDIO_PTT || type == IPocEngineEventHandler.SessionType.TYPE_AUDIO_PTT_TEMP) {
            boolean ret = pocEngine.joinChannel(remoteId);
            if (!ret) {
                showToast("接听失败 " + sessionId);
                tryHangUp();
            }
            AvActivity.this.finish();
            //对讲组接听成功后，要退出来电界面，如果是对讲组来电，一般使用全局dialog就好，这里跳到AvActivity只是为了方便
        } else {
            boolean ret = pocEngine.acceptCall(sessionId);
            if (!ret) {
                showToast("接听失败 " + sessionId);
                AvActivity.this.finish();
            }
        }
    }

    private void tryHangUp() {
        pocEngine.hangUpCall(sessionId);
        AvActivity.this.finish();
    }

    private void showToast(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        tryAcceptCall();
    }
}
