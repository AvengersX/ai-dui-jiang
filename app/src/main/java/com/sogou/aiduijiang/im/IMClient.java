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

    public boolean canTalk() {
        return sInstance.mIMImpl.canTalk();
    }

    public void startTalk() {
        sInstance.mIMImpl.startTalk();
    }

    public void endTalk() {
        sInstance.mIMImpl.endTalk();
    }

    /**
     * 更新用户经纬度
     * @param lat
     * @param lon
     */
    public void updateLocation(String lat, String lon) {

    }

    /**
     * 设置目的地：目的地位置
     * @param lat
     * @param lon
     */
    public void setDestination(String lat, String lon) {

    }

    /**
     * 设置CallBack
     * @param callBack
     */
    public void setIMCallBack(IMCallBack callBack) {

    }

}
