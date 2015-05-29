package com.sogou.aiduijiang;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sogou.aiduijiang.im.IMClient;

/**
 * Created by caohe on 15-5-28.
 */
public class ADJApplication extends Application {


    private static final String TOKEN_1 = "fZ11b/Q/ijr5Xcd32/+SbbdcZzg7k0226AkqN9VvhS6OPh7oEsnljDylVoC+yvTo5vXNpw4ECs3BP3OHkLpw4A=="; // user1 caohe

    private static final String TOKEN_2 = "5D77iRqOnr3xiAhIv8ibBvnxWCq7ejYkOz13Zbm9mtGvo6N70qMuGuqpUlOLEnHoyudweaU66MH7NDwxqlPY8Q=="; // user2 zhenxing

    private static final String TOKEN_3 = "1Z5eH3FEoi14BNqaNPqRu7dcZzg7k0226AkqN9VvhS6OPh7oEsnljJmm9BdeK/A/nUEXmmAizfSdu/d70b3QUA=="; // user3 youzi

    private static final String TOKEN_4 = "F0QepntFKhXx3WZQIdHcKffa/CyeTR4A+5TqMneRzjet2OcEuL/xg2lkzRwE+UY5Wg5ulFL3Duwlewg9N5jQzQ=="; // user4 jialing

    private static final String TOKEN_5 = "6ZrI3PRcu5d69RANtQH8rbdcZzg7k0226AkqN9VvhS6OPh7oEsnljALPrzwZhhgznUEXmmAizfTFdLrsaI8mXQ=="; // user5 caohe1

    private static Application sInstance;

    public static Application getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        if (getCurProcessName(this).equals("com.sogou.aiduijiang")) {
            IMClient.init(this);
            IMClient.getsInstance().connect(getToken());
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sInstance = null;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    //TEST
    private String getToken() {
        String imei = "000000000000000";
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED
                    && tm != null) {
                imei = tm.getDeviceId();
            }
        } catch (Exception e) {
        }

        Log.v("hccc", "====imei " + imei + "======");

        if (imei.equals("357784052673413")) {
            return TOKEN_1;
        } else if (imei.equals("357092068912340")) { // Samsung A7
            return TOKEN_2;
        } else if (imei.equals("357747058397626")) {
            return TOKEN_3;
        } else if (imei.equals("2")) {
            return TOKEN_4;
        } else if (imei.equals("357194042133207")) {
            return TOKEN_5;
        }

        return TOKEN_1;
    }

}
