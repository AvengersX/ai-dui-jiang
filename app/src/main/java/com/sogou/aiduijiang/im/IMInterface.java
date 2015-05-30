package com.sogou.aiduijiang.im;

import android.content.Context;

/**
 * Created by caohe on 15-5-28.
 */
public interface IMInterface {

    public String getUID();

    public String getAvatar();

    public void init(Context context);

    public void connect(String token);

    public void joinChatRoom();

    public void quitChatRoom();

    public void sendMessage(String msg);

    public boolean canTalk();

    public void startTalk();

    public void endTalk();

    public void setCallBack(IMCallBack callBack);

}
