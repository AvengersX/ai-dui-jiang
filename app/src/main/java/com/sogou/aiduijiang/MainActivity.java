package com.sogou.aiduijiang;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;

import com.sogou.aiduijiang.im.IMCallBack;
import com.sogou.aiduijiang.im.IMClient;


public class MainActivity extends ActionBarActivity implements IMCallBack {
    private MapView mMapView;
    private AMap mAMap;


    @Override
    public void onUserJoin(String userId, String avatar, String lat, String lon) {
        Log.v("hccc", "=====onUserJoin====" + userId + " " + avatar + " " + lat + " " + lon);
    }

    @Override
    public void onUserLocationUpdate(String userId, String lat, String lon) {
        Log.v("hccc", "===onUserLocationUpdate======" + userId + " " + lat + " " + lon);
    }

    @Override
    public void onUserQuit(String userId) {
        Log.v("hccc", "====onUserQuit=====" + userId);
    }

    @Override
    public void onUserStartTalk(String userId) {
        Log.v("hccc", "===onUserStartTalk======" + userId);
    }

    @Override
    public void onUserEndTalk(String userId) {

        Log.v("hccc", "===onUserEndTalk======" + userId);
    }

    @Override
    public void onSetDestination(String userId, String lat, String lon) {
        Log.v("hccc", "======onSetDestination===" + userId + " " + lat + " " + lon);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IMClient.getsInstance().joinChatRoom();
        IMClient.getsInstance().setIMCallBack(this);

        findViewById(R.id.btn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        IMClient.getsInstance().startTalk();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_UP:
                        IMClient.getsInstance().endTalk();
                        break;
                }


                return false;
            }
        });

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IMClient.getsInstance().sendMessage();
            }
        });

        mMapView = (MapView)findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        IMClient.getsInstance().quitChatRoom();
        super.onDestroy();
        mMapView.onDestroy();
    }
}
