package com.sogou.aiduijiang.im;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.sogou.aiduijiang.ADJApplication;
import com.sogou.aiduijiang.AmrAudioEncoder;
import com.sogou.aiduijiang.AmrAudioPlayer;

import java.io.File;
import java.util.ArrayList;

import io.rong.imlib.RongIMClient;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by caohe on 15-5-28.
 */
public class RongIMImpl implements IMInterface, RongIMClient.OnReceiveMessageListener {

    private static final String CHAT_ROOM_ID = "chat_room_001";

    RongIMClient mRongIMClient;

    private String mUserId;

    private IMCallBack mCallBack;

    @Override
    public String getUID() {
        return mUserId;
    }

    @Override
    public void init(Context context) {
        RongIMClient.init(context);
    }

    @Override
    public void connect(String token) {
        Log.v("hccc", "====start connect====");
        try {
            mRongIMClient = RongIMClient.connect(token, new RongIMClient.ConnectCallback() {

                @Override
                public void onSuccess(String userId) {
                    Log.d("hccc", "--connect--onSuccess----userId---" + userId);
                    mUserId = userId;
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    Log.d("hccc", "--connect--errorCode-------" + errorCode);
                }
            });
            mRongIMClient.setOnReceiveMessageListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean playing = false;

    ArrayList<Uri> queue = new ArrayList<Uri>();

    private void playUri(final Uri uri, boolean toQueue) {

        Uri toPlay = null;
        if (playing) {
            if (toQueue) {
                queue.add(uri);
            } else {
                toPlay = uri;
            }
        } else {
            toPlay = uri;
        }
        final Uri handle = toPlay;

        new Thread(new Runnable() {

            @Override
            public void run() {

                MediaPlayer mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        playing = true;
                    }
                });

                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playing = false;
                        if (queue.size() > 0) {
                            Uri uri = queue.get(0);
                            queue.remove(0);
                            playUri(uri, false);
                        }
                    }
                });

                try {
                    mMediaPlayer.setDataSource(ADJApplication.getInstance(), handle);
                    mMediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String[] parseParams(String s, int num) {
        if (s == null || s.length() == 0) {
            return null;
        }
        String[] result = s.split("\\|");
        if (result == null || result.length != num + 1) {
            return null;
        }
        return result;
    }

    @Override
    public void onReceived(final RongIMClient.Message message, int i) {
        if (message.getContent() instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message.getContent();
            Log.e("hccc", "VoiceMessage--收收收收--接收到一条【语音消息】 voiceMessage.getExtra-----" + textMessage.getContent());
            /**
             *
                sendMessage("start_talk|" + mUserId);
                sendMessage("end_talk|" + mUserId);
                sendMessage("update_location|" + mIMImpl.getUID() + "|" + lat + "|" + lon);
                sendMessage("set_destination|" + mIMImpl.getUID() + "|" + lat + "|" + lon);
                sendMessage("join_chat|" + mUserId);
                sendMessage("quit_chat|" + mUserId);
             */
            String msg = textMessage.getContent();
            if (msg != null) {
                if (msg.startsWith("start_talk")) {
                    String[] params = parseParams(msg, 2);
                    if (params != null && mCallBack != null) {
                        mCallBack.onUserStartTalk(params[1]);
                    }
                } else if (msg.startsWith("end_talk")) {
                    String[] params = parseParams(msg, 2);
                    if (params != null && mCallBack != null) {
                        mCallBack.onUserEndTalk(params[1]);
                        AmrAudioPlayer.getInstance().stop();
                    }
                } else if (msg.startsWith("join_chat")) {
                    String[] params = parseParams(msg, 2);
                    if (params != null && mCallBack != null) {
                        mCallBack.onUserJoin(params[1], "","", "");
                    }
                } else if (msg.startsWith("quit_chat")) {
                    String[] params = parseParams(msg, 2);
                    if (params != null && mCallBack != null) {
                        mCallBack.onUserQuit(params[1]);
                    }
                } else if (msg.startsWith("update_location")) {
                    String[] params = parseParams(msg, 4);
                    if (params != null && mCallBack != null) {
                        mCallBack.onUserLocationUpdate(params[1], params[2], params[3]);
                    }
                } else if (msg.startsWith("set_destination")) {
                    String[] params = parseParams(msg, 4);
                    if (params != null && mCallBack != null) {
                        mCallBack.onSetDestination(params[1], params[2], params[3]);
                    }
                }
            }

        } else if (message.getContent() instanceof VoiceMessage) {
            final VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
//            Log.e("hccc", "VoiceMessage--收收收收--接收到一条【语音消息】 voiceMessage.getExtra-----" + voiceMessage.getExtra() + voiceMessage.getUri());
//            playUri(voiceMessage.getUri(), true);
            AmrAudioPlayer.getInstance().addData(voiceMessage.getUri());
        }
    }

    @Override
    public void joinChatRoom() {
        mRongIMClient.joinChatRoom(CHAT_ROOM_ID, 0, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.v("hccc", "=======join chat room success=");
                sendMessage("join_chat|" + mUserId);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.v("hccc", "=======join chat room error=" + errorCode.toString());
            }
        });
    }

    @Override
    public void quitChatRoom() {
        sendMessage("quit_chat|" + mUserId);
        mRongIMClient.quitChatRoom(CHAT_ROOM_ID, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(ErrorCode errorCode) {

            }
        });
    }

    @Override
    public void sendMessage(String msg) {
        TextMessage textMessage = TextMessage.obtain(msg  + "|" + System.currentTimeMillis());
        Log.v("hccc", "===send message==" + msg);
        mRongIMClient.sendMessage(RongIMClient.ConversationType.CHATROOM, CHAT_ROOM_ID, textMessage, new RongIMClient.SendMessageCallback(){
            @Override
            public void onSuccess(int i) {

                Log.v("hccc", "=======onSuccess=");
            }

            @Override
            public void onError(int i, ErrorCode errorCode) {
                Log.v("hccc", "=======onError=" + errorCode.toString());

            }

            @Override
            public void onProgress(int i, int i2) {
                Log.v("hccc", "=======onProgress=");

            }
        });
    }

    @Override
    public boolean canTalk() {
        return true;
    }

    @Override
    public void startTalk() {
        Log.v("hccc", "====start talk===");
        sendMessage("start_talk|" + mUserId);
        isTalking = true;
        handleTalk();
    }

    private static final int SEND_WAIT_LENGTH = 750;

    private boolean isTalking = false;

    private void handleTalk() {

        AmrAudioEncoder.getArmAudioEncoderInstance().initArmAudioEncoder(ADJApplication.getInstance());
        AmrAudioEncoder.getArmAudioEncoderInstance().start();

        Runnable sendRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    wait(SEND_WAIT_LENGTH);
                } catch (Exception e) {
                }
                while(AmrAudioEncoder.getArmAudioEncoderInstance().isAudioRecording()) {
                    try {
                        File file = AmrAudioEncoder.getArmAudioEncoderInstance().mToSendFiles.take();
                        sendVoiceMessage(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    wait(SEND_WAIT_LENGTH);
                } catch (Exception e) {
                }
                sendMessage("end_talk|" + mUserId);
            }
        };
        new Thread(sendRunnable).start();



    }

    private void sendVoiceMessage(final File file) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    Uri uri = Uri.fromFile(file);
                    VoiceMessage voiceMessage = VoiceMessage.obtain(uri, 10 * 5);
                    voiceMessage.setExtra("I'm Bob,test voice");
                    mRongIMClient.sendMessage(RongIMClient.ConversationType.CHATROOM, CHAT_ROOM_ID, voiceMessage, new RongIMClient.SendMessageCallback(){
                        @Override
                        public void onSuccess(int i) {

                            Log.v("hccc", "=====voiceMessage==onSuccess=");
                        }

                        @Override
                        public void onError(int i, ErrorCode errorCode) {
                            Log.v("hccc", "=====voiceMessage==onError=");

                        }

                        @Override
                        public void onProgress(int i, int i2) {
                            Log.v("hccc", "====voiceMessage===onProgress=");

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();

    }

    @Override
    public void endTalk() {
        isTalking = false;
        AmrAudioEncoder.getArmAudioEncoderInstance().stop();

    }

    @Override
    public void setCallBack(IMCallBack callBack) {
        mCallBack = callBack;
    }
}
