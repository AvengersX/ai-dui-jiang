package com.sogou.aiduijiang.im;

/**
 * Created by caohe on 15-5-28.
 */
public interface IMCallBack {


    /**
     * 新用户加入：用户ID，头像ID(定义一组头像先)，当前位置(经度、纬度)
     * @param userId
     * @param avatar
     * @param lat
     * @param lon
     */
    public void onUserJoin(String userId, String avatar, String lat, String lon);

    /**
     * 位置变化：用户ID，当前位置
     * @param userId
     * @param lat
     * @param lon
     */
    public void onUserLocationUpdate(String userId, String lat, String lon);

    /**
     * 用户离开：用户ID
     * @param userId
     */
    public void onUserQuit(String userId);

    /**
     * 开始说话
     */
    public void onUserStartTalk(String userId);

    /**
     * 结束说话
     * @param userId
     */
    public void onUserEndTalk(String userId);

    /**
     * 设置目的地：目的地位置
     * @param userId
     * @param lat
     * @param lon
     */
    public void onSetDestination(String userId, String lat, String lon);

}
