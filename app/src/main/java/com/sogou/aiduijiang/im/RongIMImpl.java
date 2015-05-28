package com.sogou.aiduijiang.im;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sogou.aiduijiang.ADJApplication;
import com.sogou.aiduijiang.RecordUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    @Override
    public void onReceived(final RongIMClient.Message message, int i) {
//        Log.v("hccc", "=====message received==" + message + message.getClass() + message.getContent());
        if (message.getContent() instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message.getContent();
            Log.d("hccc", "TextMessage---收收收收--接收到一条【文字消息】-----" + textMessage.getContent() + ",getExtra:" + textMessage.getExtra());
            Log.d("hccc", "TextMessage---收收收收--接收到一条【文字消息】getPushContent-----" + textMessage.getPushContent());

        } else if (message.getContent() instanceof VoiceMessage) {
            final VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            Log.e("hccc", "VoiceMessage--收收收收--接收到一条【语音消息】 voiceMessage.getExtra-----" + voiceMessage.getExtra() + voiceMessage.getUri());

            new Thread(new Runnable() {

                @Override
                public void run() {

                    MediaPlayer mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });

                    try {
                        mMediaPlayer.setDataSource(ADJApplication.getInstance(), voiceMessage.getUri());
                        mMediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void joinChatRoom() {
        mRongIMClient.joinChatRoom(CHAT_ROOM_ID, 0, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.v("hccc", "=======join chat room success=");
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.v("hccc", "=======join chat room error=" + errorCode.toString());
            }
        });
    }

    @Override
    public void quitChatRoom() {
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
    public void sendMessage() {
        TextMessage textMessage = TextMessage.obtain("这是消息。。。。。。春节快乐！！！！发送时间:" + System.currentTimeMillis());
        textMessage.setExtra("文字消息Extra");
        textMessage.setPushContent("push 内容setPushContent");
        mRongIMClient.sendMessage(RongIMClient.ConversationType.CHATROOM, CHAT_ROOM_ID, textMessage, new RongIMClient.SendMessageCallback(){
            @Override
            public void onSuccess(int i) {

                Log.v("hccc", "=======onSuccess=");
            }

            @Override
            public void onError(int i, ErrorCode errorCode) {
                Log.v("hccc", "=======onError=");

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
        sendMessage();

        RecordUtil.startRecord();

    }

    @Override
    public void endTalk() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Log.v("hccc", "====end talk===");
                File file = RecordUtil.endRecord();
                try {
//            InputStream is = new FileInputStream(file);
//            String path = DemoContext.getInstance().getResourceDir();
//            FileUtil.createFile("voice", path);
//            Uri uri = Uri.parse(path + "/voice");
//            uri = FileUtil.writeByte(uri, FileUtil.toByteArray(is));
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
}
