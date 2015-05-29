package com.sogou.aiduijiang.model;

import java.util.ArrayList;

/**
 * Created by zhouzhenxing on 2015/5/29.
 */
public class AppData {
    private static AppData sAppData;
    private ArrayList<User> mUsers = new ArrayList<>();
    private User mCurrentUser = new User();
    private Pos mDestination = new Pos();

    public Pos getDestination() {
        return mDestination;
    }

    public void setDestination(Pos mDestination) {
        this.mDestination = mDestination;
    }


    public class Pos {
        public double mLatitude;
        public double mLongitude;
    }


    public User getCurrentUser() {
        return mCurrentUser;
    }

    public ArrayList<User> getUsers() {
        return mUsers;
    }


    private AppData() {
    }

    public static AppData getInstance() {
        if (sAppData == null) {
            sAppData = new AppData();
        }

        return sAppData;
    }
}
