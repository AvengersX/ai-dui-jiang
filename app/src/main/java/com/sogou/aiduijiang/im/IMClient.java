package com.sogou.aiduijiang.im;

import android.content.Context;

/**
 * Created by caohe on 15-5-28.
 */
public class IMClient {

    private static IMClient sInstance;

    private IMInterface mIMImpl;

    private IMClient() {

    }

    public static IMClient getsInstance() {
        return sInstance;
    }

    public static void init(Context context) {
        sInstance = new IMClient();
        sInstance.mIMImpl = new RongIMImpl();
        sInstance.mIMImpl.init(context);
    }


    public void connect(String token) {
        sInstance.mIMImpl.connect(token);
    }

    public void joinChatRoom() {
        sInstance.mIMImpl.joinChatRoom();

    }

    public void sendMessage() {
        sInstance.mIMImpl.sendMessage();
    }

    public void quitChatRoom() {
        sInstance.mIMImpl.quitChatRoom();
    }

}
