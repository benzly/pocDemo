package my.poc.demo.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.chat.ChatMessageStatus;
import com.huamai.poc.chat.ChatUtil;
import com.huamai.poc.greendao.ChatMessage;
import com.huamai.poc.greendao.MessageDialogue;
import com.huamai.poc.media.MediaFileUtil;
import com.huamai.poc.media.MediaRecordFunc;
import com.huamai.poc.media.OnVolumeListener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import my.poc.demo.R;
import my.poc.demo.widget.ChatMessageViewAdapter;
import my.poc.demo.widget.MediaPlayerFactory;
import my.poc.demo.widget.VoiceDialog;


public class ChatMessageActivity extends Activity {

    public static final String EXTRAS_CHAT_ID = "extras_chat_id";
    private long chatId;
    private ChatMessageViewAdapter adapter;
    private Handler handler = new Handler();
    private VoiceDialog voiceDialog;
    private boolean isCurVoiceBtn = true;
    private boolean isIMEShown;
    private GestureDetector gestureDetector;
    private Vibrator vibrator;

    View rootView;
    TextView titleTv;
    EditText inputEt;
    ImageView inputPic;
    ImageView sendBt;
    View sendRoundLayout;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_layout);

        rootView = findViewById(R.id.root);
        titleTv = (TextView) findViewById(R.id.title);
        inputEt = (EditText) findViewById(R.id.input_et);
        inputPic = (ImageView) findViewById(R.id.input_cam);
        sendBt = (ImageView) findViewById(R.id.send_bt);
        sendRoundLayout = findViewById(R.id.chat_send_panel);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        chatId = getIntent().getLongExtra(EXTRAS_CHAT_ID, -1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatMessageViewAdapter(this, recyclerView);
        recyclerView.setAdapter(adapter);

        Log.d("ChatMessageActivity", "Show Chat[" + chatId + "] message list");
        initViewListeners();
        PocEngineFactory.get().addEventHandler(pocEngineEventHandler);

        MessageDialogue md = PocEngineFactory.get().getConversation(chatId);
        titleTv.setText(md.getName());

        // load messages.
        List<ChatMessage> list = PocEngineFactory.get().getConversationMessages(chatId);
        if (list != null) {
            adapter.updateAll((ArrayList<ChatMessage>) list);
        }
        // mark all as read
        PocEngineFactory.get().markMessagesAsRead(list);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaPlayerFactory.getInstance().release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaRecordFunc.getInstance().destroy();
        PocEngineFactory.get().removeEventHandler(pocEngineEventHandler);
    }

    public static void show(Activity context, long chatId) {
        Intent intent = new Intent(context, ChatMessageActivity.class);
        intent.putExtra(EXTRAS_CHAT_ID, chatId);
        context.startActivity(intent);
    }

    private final IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {

        @Override
        public void onMessageArrived(ChatMessage message) {
            adapter.updateItem(message);
        }

        @Override
        public void onMessageSendFailed(ChatMessage message) {
            adapter.updateItem(message);
        }

        @Override
        public void onMessageFileSendProgressChanged(ChatMessage message) {
            adapter.updateItem(message);
        }

        @Override
        public void onMessageReceived(long chatId, List<ChatMessage> messages) {
            adapter.addChatMessage(messages);
        }
    };

    private void initViewListeners() {
        inputPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browserImageFile();
            }
        });

        inputEt.setPadding(30, 0, 0, 0);
        inputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendBtnStatus(TextUtils.isEmpty(inputEt.getText().toString()));
            }
        });
        // 键盘弹出
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int minKeyboardHeight = 150;
                int statusBarHeight = getStatusHeight(ChatMessageActivity.this);
                int screenHeight = rootView.getRootView().getHeight();
                int height = screenHeight - (r.bottom - r.top);
                if (isIMEShown) {
                    if (height - statusBarHeight < minKeyboardHeight) {
                        isIMEShown = false;
                    }
                } else {
                    if (height - statusBarHeight > minKeyboardHeight) {
                        isIMEShown = true;
                        recyclerView.smoothScrollToPosition(adapter.getLastPosition());
                    }
                }
            }
        });

        // 点击
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, int position, long id) {
                ChatMessage message = adapter.getDataByPosition(position);
                if (message == null) {
                    return;
                }
            }
        });

        // 长按
        adapter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final ChatMessage message = adapter.getDataByPosition(position);
                if (message == null) {
                    return false;
                }
                AlertDialog.Builder ab = new AlertDialog.Builder(ChatMessageActivity.this);
                ab.setCancelable(true);
                ab.setItems(new String[]{"Delete"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                PocEngineFactory.get().cancelUploadFile("");
                                PocEngineFactory.get().deleteMessage(message);
                                adapter.removeChatMessage(position);
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                ab.show();
                return false;
            }
        });

        // 列表滑动距离
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                if (Math.abs(e2.getY() - e1.getY()) > 100 && Math.abs(velocityY) > 100) {
                    if (isIMEShown) {
                        hideIME();
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        // 列表区触摸事件
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        // 语音按钮动效
        sendRoundLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isCurVoiceBtn) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startVoiceRecord();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            if (event.getX() <= -50 || event.getY() <= -50 || (event.getX() <= -35 && event.getY() <= -35)) {
                                showVoiceRecordCancelStatus(true);
                            } else {
                                showVoiceRecordCancelStatus(false);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (event.getX() <= -50 || event.getY() <= -50 || (event.getX() <= -35 && event.getY() <= -35)) {
                                cancelVoiceRecord();
                            } else {
                                finishVoiceRecord();
                            }
                            return true;
                    }
                }
                return false;
            }
        });

        //消息重发(发送失败情况下)
        adapter.setOnItemResendClickListener(new ChatMessageViewAdapter.OnItemResendClickListener() {
            @Override
            public void onItemResendClick(View item, int position) {
                ChatMessage message = adapter.getDataByPosition(position);
                if (message == null) {
                    return;
                }
                if (message.getSip_status() == ChatMessageStatus.Sip.FAILED) {
                    PocEngineFactory.get().sendMessage(message);
                    adapter.notifyItemChanged(position);
                }
            }
        });

        findViewById(R.id.chat_send_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCurVoiceBtn) {
                    ChatMessage message = ChatUtil.createOutTextMessage(inputEt.getText().toString(), chatId);
                    PocEngineFactory.get().sendMessage(message);
                    inputEt.setText("");
                    adapter.addChatMessage(message);
                }
            }
        });
    }

    public int getStatusHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object object = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(object);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return 0;
        }
    }

    public void browserImageFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 10086);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 10086 && data != null && data.getData() != null) {
            try {
                String path = MediaFileUtil.getPath(this, data.getData());
                MediaFileUtil.MediaFile.MediaFileType type = MediaFileUtil.MediaFile.getFileType(path);
                if (type != null && MediaFileUtil.MediaFile.isImageFileType(type.fileType)) {
                    ChatMessage message = ChatUtil.createOutImgMessage(path, chatId);
                    PocEngineFactory.get().sendMessage(message);
                    adapter.addChatMessage(message);
                } else {
                    Toast.makeText(this, "demo只实现了图片...", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startVoiceRecord() {
        MediaPlayerFactory.getInstance().release();

        aniSendRoundLayoutScaleBig(true);
        showVoiceDialog(true);
        MediaRecordFunc.getInstance().startRecordAndFile(onVolumeListener);

        long[] pattern = {0, 40};
        vibrator.vibrate(pattern, -1);
    }

    private void showVoiceRecordCancelStatus(boolean show) {
        if (voiceDialog != null) {
            if (show) {
                voiceDialog.showCancel();
            } else {
                voiceDialog.showVoiceing();
            }
        }
    }

    private void cancelVoiceRecord() {
        aniSendRoundLayoutScaleBig(false);
        String filePath = MediaRecordFunc.getInstance().stopRecordAndFile();
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        showVoiceDialog(false);
    }

    private void finishVoiceRecord() {
        aniSendRoundLayoutScaleBig(false);
        String filePath = MediaRecordFunc.getInstance().stopRecordAndFile();
        Log.d("ChatMessageActivity", "finishVoiceRecord: " + filePath);
        long duration = MediaRecordFunc.getInstance().getRecordTime();
        if (duration < 1000) {
            Toast.makeText(this, "record_two_small", Toast.LENGTH_SHORT).show();
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } else {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "record_failed", Toast.LENGTH_SHORT).show();
            } else {
                ChatMessage message = ChatUtil.createOutAudioMessage(filePath, chatId);
                message.setDuration(duration / 1000);
                adapter.addChatMessage(message);
                PocEngineFactory.get().sendMessage(message);
            }
        }
        showVoiceDialog(false);
    }

    private void hideIME() {
        if (isIMEShown) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }
    }

    private OnVolumeListener onVolumeListener = new OnVolumeListener() {

        @Override
        public void onVolumeChanged(int volume) {
            if (voiceDialog != null) {
                voiceDialog.updateVoice(volume);
            }
        }
    };

    private void showVoiceDialog(boolean show) {
        if (show) {
            if (voiceDialog == null) {
                voiceDialog = new VoiceDialog(this);
            }
            voiceDialog.show();
        } else {
            if (voiceDialog != null) {
                voiceDialog.dismiss();
            }
        }
    }

    void updateSendBtnStatus(boolean isVoice) {
        if (isCurVoiceBtn && !isVoice) {
            isCurVoiceBtn = false;
            aniSwitchSendButtonIcon(R.drawable.ic_input_send);
        } else if (!isCurVoiceBtn && isVoice) {
            isCurVoiceBtn = true;
            aniSwitchSendButtonIcon(R.drawable.ic_input_voice);
        }
    }

    void aniSwitchSendButtonIcon(final int iconResId) {
        final ArrayList<Animator> animators = new ArrayList<Animator>(3);
        ObjectAnimator xOutScaleAnim = ObjectAnimator.ofFloat(sendBt, "scaleX", 1.0f, 2.0f);
        ObjectAnimator yOutScaleAnim = ObjectAnimator.ofFloat(sendBt, "scaleY", 1.0f, 2.0f);
        ObjectAnimator outAlphaAnim = ObjectAnimator.ofFloat(sendBt, "alpha", 1.0f, 0.0f);
        xOutScaleAnim.setDuration(100);
        yOutScaleAnim.setDuration(100);
        outAlphaAnim.setDuration(100);
        animators.add(xOutScaleAnim);
        animators.add(yOutScaleAnim);
        animators.add(outAlphaAnim);
        AnimatorSet aniSet = new AnimatorSet();
        aniSet.playTogether(animators);
        aniSet.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBt.setImageResource(iconResId);
                ObjectAnimator xInScaleAnim = ObjectAnimator.ofFloat(sendBt, "scaleX", 0.0f, 1.0f);
                ObjectAnimator yInScaleAnim = ObjectAnimator.ofFloat(sendBt, "scaleY", 0.0f, 1.0f);
                ObjectAnimator inAlphaAnim = ObjectAnimator.ofFloat(sendBt, "alpha", 0.0f, 1.0f);
                xInScaleAnim.setDuration(50);
                xInScaleAnim.setDuration(50);
                xInScaleAnim.setDuration(50);
                animators.clear();
                animators.add(xInScaleAnim);
                animators.add(yInScaleAnim);
                animators.add(inAlphaAnim);
                AnimatorSet aniSet = new AnimatorSet();
                aniSet.playTogether(animators);
                aniSet.start();
            }
        }, 100);
    }

    void aniSendRoundLayoutScaleBig(boolean big) {
        final ArrayList<Animator> animators = new ArrayList<Animator>(2);
        ObjectAnimator xScaleAnim;
        ObjectAnimator yScaleAnim;
        if (big) {
            xScaleAnim = ObjectAnimator.ofFloat(sendRoundLayout, "scaleX", sendRoundLayout.getScaleX(), 2.1f);
            yScaleAnim = ObjectAnimator.ofFloat(sendRoundLayout, "scaleY", sendRoundLayout.getScaleY(), 2.1f);
        } else {
            xScaleAnim = ObjectAnimator.ofFloat(sendRoundLayout, "scaleX", sendRoundLayout.getScaleX(), 1.0f);
            yScaleAnim = ObjectAnimator.ofFloat(sendRoundLayout, "scaleY", sendRoundLayout.getScaleY(), 1.0f);
        }
        xScaleAnim.setDuration(350);
        yScaleAnim.setDuration(350);
        xScaleAnim.setInterpolator(new OvershootInterpolator(3.0F));
        yScaleAnim.setInterpolator(new OvershootInterpolator(3.0F));
        animators.add(xScaleAnim);
        animators.add(yScaleAnim);
        AnimatorSet aniSet = new AnimatorSet();
        aniSet.playTogether(animators);
        aniSet.start();
    }
}