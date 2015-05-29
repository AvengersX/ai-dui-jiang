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
        mIMImpl.connect(token);
    }

    public void joinChatRoom() {
        mIMImpl.joinChatRoom();

    }

    public void sendMessage(String msg) {
        mIMImpl.sendMessage(msg);
    }

    public void quitChatRoom() {
        mIMImpl.quitChatRoom();
    }

    public boolean canTalk() {
        return mIMImpl.canTalk();
    }

    public void startTalk() {
        mIMImpl.startTalk();
    }

    public void endTalk() {
        mIMImpl.endTalk();
    }

    /**
     * 更新用户经纬度
     * @param lat
     * @param lon
     */
    public void updateLocation(String lat, String lon) {
        if (mIMImpl.getUID() != null) {
            sendMessage("update_location|" + mIMImpl.getUID() + "|" + lat + "|" + lon);
        }
    }

    /**
     * 设置目的地：目的地位置
     * @param lat
     * @param lon
     */
    public void setDestination(String lat, String lon) {
        if (mIMImpl.getUID() != null) {
            sendMessage("set_destination|" + mIMImpl.getUID() + "|" + lat + "|" + lon);
        }
    }

    /**
     * 设置CallBack
     * @param callBack
     */
    public void setIMCallBack(IMCallBack callBack) {
        mIMImpl.setCallBack(callBack);
    }

}
