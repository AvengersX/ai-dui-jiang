package com.sogou.aiduijiang;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.sogou.aiduijiang.im.IMCallBack;
import com.sogou.aiduijiang.im.IMClient;
import com.sogou.aiduijiang.model.AppData;
import com.sogou.aiduijiang.model.User;

import java.util.Hashtable;


public class MainActivity extends ActionBarActivity implements AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener,
        AMap.OnMarkerDragListener,
        AMap.OnMapLoadedListener,
        View.OnClickListener,
        AMap.InfoWindowAdapter,
        LocationSource,
        AMapLocationListener,
        IMCallBack {
    private static final int MSG_USER_JOIN = 1;
    private static final int MSG_LOC_UPDATE = 2;
    private static final int MSG_USER_QUIT = 3;
    private static final int MSG_START_TALK = 4;
    private static final int MSG_END_TALK = 5;
    private static final int MSG_DEST_CHANGE = 6;

    private MapView mMapView;
    private AMap mAMap;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    private Marker mMyMarker;
    private Marker mDestinationMarker;
    private Hashtable<String, Marker> mFriendMarkers = new Hashtable<>();

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_USER_JOIN: {
                    User user = (User)msg.obj;
                    addUser(user);
                }
                    break;
                case MSG_LOC_UPDATE: {
                    User user = (User)msg.obj;
                    User u = findUser(user.mUid);
                    if (u == null) {
                        addUser(user);
                    } else {
                        u.mAvatar = user.mAvatar;
                        u.mLatitude = user.mLatitude;
                        u.mLongitude = user.mLongitude;
                        updateUser(u);
                    }
                }
                    break;
                case MSG_USER_QUIT: {
                    User user = (User)msg.obj;
                    removeUser(user.mUid);
                }
                    break;
                case MSG_START_TALK: {
                    User user = (User)msg.obj;
                    updateTalkStatus(user.mUid, true);
                }
                    break;
                case MSG_END_TALK: {
                    User user = (User)msg.obj;
                    updateTalkStatus(user.mUid, false);
                }
                    break;
                case MSG_DEST_CHANGE: {
                    User user = (User)msg.obj;
                    updateDest(user);
                }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private User findUser(String uid) {
        for (User u : AppData.getInstance().getUsers()) {
            if (u.mUid.equalsIgnoreCase(uid)) {
                return u;
            }
        }

        return null;
    }

    private boolean addUser(User user) {
        User u = findUser(user.mUid);
        if (u == null) {
            if (user.mAvatar == 0) {
                // set avatar randomly
            }

            AppData.getInstance().getUsers().add(user);
            if (mFriendMarkers.containsKey(user.mUid)) {
                Marker mk = mFriendMarkers.get(user.mUid);
                mk.setPosition(new LatLng(user.mLatitude, user.mLongitude));
            } else {
                Marker mk = mAMap.addMarker(
                        new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromResource(user.mAvatar))
                                .position(new LatLng(user.mLatitude, user.mLongitude)));
                // TODO set title?

                mFriendMarkers.put(user.mUid, mk);
            }

        } else {
            updateUser(user);
        }

        return true;
    }

    private boolean updateUser(User user) {
        User u = findUser(user.mUid);
        if (u != null) {
            u.mLatitude = user.mLatitude;
            u.mLongitude = user.mLongitude;
            u.mAvatar = user.mAvatar;

            if (mFriendMarkers.containsKey(user.mUid)) {
                Marker mk = mFriendMarkers.get(user.mUid);
                mk.setPosition(new LatLng(user.mLatitude, user.mLongitude));
            } else {
                Marker mk = mAMap.addMarker(
                        new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory.fromResource(user.mAvatar))
                                .position(new LatLng(user.mLatitude, user.mLongitude)));
                // TODO set title?

                mFriendMarkers.put(user.mUid, mk);
            }
        }
        return false;
    }

    private boolean removeUser(String uid) {
        User u = findUser(uid);
        if (u != null) {
            AppData.getInstance().getUsers().remove(u);
        }

        if (mFriendMarkers.containsKey(uid)) {
            Marker mk = mFriendMarkers.get(uid);
            mk.remove();

            mFriendMarkers.remove(uid);
        }

        return true;
    }

    private boolean updateTalkStatus(String uid, boolean isTalking) {
        Marker mk = mFriendMarkers.get(uid);
        if (mk != null) {
            //mk.setIcon(); // TODO disable animation
        }

        return true;
    }

    private boolean updateDest(User user) {
        AppData.Pos pos = AppData.getInstance().getDestination();
        pos.mLatitude = user.mLatitude;
        pos.mLongitude = user.mLongitude;

        AppData.getInstance().setDestination(pos);

        mDestinationMarker.setPosition(new LatLng(pos.mLatitude, pos.mLongitude));

        return true;
    }


    @Override
    public void onUserJoin(String userId, String avatar, String lat, String lon) {
        Log.v("hccc", "=====onUserJoin====" + userId + " " + avatar + " " + lat + " " + lon);

        User user = new User();
        user.mUid = userId;
        user.mAvatar = Integer.valueOf(avatar).intValue();
        user.mLatitude = Double.valueOf(lat).doubleValue();
        user.mLongitude = Double.valueOf(lon).doubleValue();

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_USER_JOIN;
        msg.obj = user;

        mHandler.sendMessage(msg);
    }

    @Override
    public void onUserLocationUpdate(String userId, String avatar, String lat, String lon) {
        Log.v("hccc", "===onUserLocationUpdate======" + userId + " " + lat + " " + lon);

        User user = new User();
        user.mUid = userId;
        user.mLatitude = Double.valueOf(lat).doubleValue();
        user.mLongitude = Double.valueOf(lon).doubleValue();

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_LOC_UPDATE;
        msg.obj = user;

        mHandler.sendMessage(msg);
    }

    @Override
    public void onUserQuit(String userId) {
        Log.v("hccc", "====onUserQuit=====" + userId);

        User user = new User();
        user.mUid = userId;

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_USER_QUIT;
        msg.obj = user;

        mHandler.sendMessage(msg);
    }

    @Override
    public void onUserStartTalk(String userId) {
        Log.v("hccc", "===onUserStartTalk======" + userId);

        User user = new User();
        user.mUid = userId;

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_START_TALK;
        msg.obj = user;

        mHandler.sendMessage(msg);
    }

    @Override
    public void onUserEndTalk(String userId) {
        Log.v("hccc", "===onUserEndTalk======" + userId);

        User user = new User();
        user.mUid = userId;

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_END_TALK;
        msg.obj = user;

        mHandler.sendMessage(msg);
    }

    @Override
    public void onSetDestination(String userId, String lat, String lon) {
        Log.v("hccc", "======onSetDestination===" + userId + " " + lat + " " + lon);

        User user = new User();
        user.mUid = userId;
        user.mLatitude = Double.valueOf(lat).doubleValue();
        user.mLongitude = Double.valueOf(lon).doubleValue();

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_DEST_CHANGE;
        msg.obj = user;

        mHandler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ADJApplication.sSkipMessage = true;
        IMClient.getsInstance().joinChatRoom();
        IMClient.getsInstance().setIMCallBack(this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ADJApplication.sSkipMessage = false;
            }
        }, 5000);
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

        findViewById(R.id.btn_together).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goto team scene
            }
        });

        mMapView = (MapView)findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            setupMap();
        }
    }

    private void setupMap() {
        User curr = AppData.getInstance().getCurrentUser();
        mMyMarker = mAMap.addMarker(
                new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(curr.mAvatar))
                        .position(new LatLng(curr.mLatitude, curr.mLongitude)));

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(Color.BLACK);
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));
        myLocationStyle.strokeWidth(0.1f);
        mAMap.setMyLocationStyle(myLocationStyle);
        mAMap.setMyLocationRotateAngle(180);
        mAMap.setLocationSource(this);
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);
        mAMap.setMyLocationEnabled(true);
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        mDestinationMarker = mAMap.addMarker(
                new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.end))
                        .draggable(true)
                        .position(
                                new LatLng(
                                        AppData.getInstance().getDestination().mLatitude,
                                        AppData.getInstance().getDestination().mLongitude)));

        // TODO init friends' marker.

        // set info windows clicks
        mAMap.setOnMarkerClickListener(this);
        mAMap.setOnInfoWindowClickListener(this);
        mAMap.setInfoWindowAdapter(this);
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
        deactivate();
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

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            mListener.onLocationChanged(aMapLocation);// ��ʾϵͳС����
            mMyMarker.setPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation
                    .getLongitude()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);

            mAMapLocationManager.requestLocationUpdates(
                    LocationProviderProxy.AMapNetwork, 2000, 10, this);
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destory();
        }
        mAMapLocationManager = null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
