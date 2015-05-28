package com.sogou.aiduijiang.im;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.rong.imlib.RongIMClient;
import io.rong.message.TextMessage;

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
    public void onReceived(RongIMClient.Message message, int i) {
//        Log.v("hccc", "=====message received==" + message + message.getClass() + message.getContent());
        if (message.getContent() instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message.getContent();

            Log.d("hccc", "TextMessage---收收收收--接收到一条【文字消息】-----" + textMessage.getContent() + ",getExtra:" + textMessage.getExtra());
            Log.d("hccc", "TextMessage---收收收收--接收到一条【文字消息】getPushContent-----" + textMessage.getPushContent());

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
}
