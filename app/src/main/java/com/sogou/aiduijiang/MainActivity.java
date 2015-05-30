package com.sogou.aiduijiang;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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
import android.widget.ImageView;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.sogou.aiduijiang.im.IMCallBack;
import com.sogou.aiduijiang.im.IMClient;
import com.sogou.aiduijiang.model.AppData;
import com.sogou.aiduijiang.model.User;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener,
        AMap.OnMarkerDragListener,
        AMap.OnMapLoadedListener,
        View.OnClickListener,
        AMap.InfoWindowAdapter,
        LocationSource,
        AMapLocationListener,
        IMCallBack,
        AMapNaviListener {

    private static final int MSG_USER_JOIN = 1;
    private static final int MSG_LOC_UPDATE = 2;
    private static final int MSG_USER_QUIT = 3;
    private static final int MSG_START_TALK = 4;
    private static final int MSG_END_TALK = 5;
    private static final int MSG_DEST_CHANGE = 6;
    public static final int MSG_SET_DEST = 7;
    private static final int MSG_TIMER1 = 100;

    private MapView mMapView;
    private AMap mAMap;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    private Marker mMyMarker;
    private Marker mDestinationMarker;
    private Hashtable<String, Marker> mFriendMarkers = new Hashtable<>();
    private Hashtable<Integer, ArrayList<Integer>> mAvatars = new Hashtable<>();
    private static int sAvatarSize = 0;
    private LatLng mCurrentPos = new LatLng(0, 0);
    private LatLng mDestPos = new LatLng(0, 0);

    private boolean mIsRouteSuccess = false;
    private AMapNavi mAMapNavi;
    private RouteOverLay mRouteOverLay;

    @Override
    public void onInitNaviFailure() {
    }

    @Override
    public void onInitNaviSuccess() {
    }

    @Override
    public void onStartNavi(int i) {
    }

    @Override
    public void onTrafficStatusUpdate() {
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
    }

    @Override
    public void onGetNavigationText(int i, String s) {
    }

    @Override
    public void onEndEmulatorNavi() {
    }

    @Override
    public void onArriveDestination() {
    }

    @Override
    public void onCalculateRouteSuccess() {
        AMapNaviPath naviPath = mAMapNavi.getNaviPath();
        if (naviPath == null) {
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        mRouteOverLay.setStartPointBitmap(bmp);
        mRouteOverLay.setEndPointBitmap(bmp2);
        mRouteOverLay.setRouteInfo(naviPath);
        mRouteOverLay.addToMap();
        mIsRouteSuccess = true;
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        mIsRouteSuccess = false;
    }

    @Override
    public void onReCalculateRouteForYaw() {
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
    }

    @Override
    public void onArrivedWayPoint(int i) {
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
    }

    Timer tm = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_TIMER1;
            mHandler.sendMessage(msg);
        }
    };

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
                    addUser(user);
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

                case MSG_TIMER1: {
                    if (mAMapLocationManager == null) {
                        mAMapLocationManager = LocationManagerProxy.getInstance(MainActivity.this);
                    }

                    mAMapLocationManager.requestLocationData(
                            LocationProviderProxy.AMapNetwork, -1, 1, MainActivity.this);
                }
                    break;
                case MSG_SET_DEST: {
                    LatLonPoint point = (LatLonPoint)msg.obj;
                    SetDestination(point);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    private void  SetDestination(LatLonPoint DesPos) {
        double Lat = DesPos.getLatitude();
        double Long = DesPos.getLongitude();
        mDestPos = new LatLng(Lat, Long);
        calculateRoute();
        IMClient.getsInstance().setDestination(String.valueOf(Lat), String.valueOf(Long));
    }

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

    private ArrayList<Integer> findAvatarGif(int resid) {
        for (Iterator it = mAvatars.keySet().iterator(); it.hasNext();) {
            Integer i = (Integer)it.next();
            if (i.intValue() == resid) {
                return mAvatars.get(i);
            }
        }

        return null;
    }

    private boolean updateTalkStatus(String uid, boolean isTalking) {
        Marker mk = mFriendMarkers.get(uid);
        User user = findUser(uid);
        if (mk != null && user != null) {
            if (isTalking) {
                ArrayList<BitmapDescriptor> gifList = new ArrayList<>();
                ArrayList<Integer> avs = findAvatarGif(user.mAvatar);
                if (avs != null) {
                    for (Integer i : avs) {
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), i.intValue());
                        Log.d("zzx", "205 bmp width: " + String.valueOf(bmp.getWidth()) + " bmp height: " + String.valueOf(bmp.getHeight()));
                        gifList.add(
                                BitmapDescriptorFactory.fromBitmap(
                                        getResizedBitmap(bmp,
                                                sAvatarSize, sAvatarSize)));
                    }

                    mk.setIcons(gifList);
                    mk.setPeriod(20);
                }
            } else {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), user.mAvatar);
                Log.d("zzx", "217 bmp width: " + String.valueOf(bmp.getWidth()) + " bmp height: " + String.valueOf(bmp.getHeight()));
                bmp = getResizedBitmap(bmp, sAvatarSize, sAvatarSize);
                mk.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
            }
        }

        return true;
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        if (resizedBitmap != bm) {
            bm.recycle();
        }
        return resizedBitmap;
    }

    private boolean updateDest(User user) {

        Log.v("hccc", "----------update dest---");

        AppData.Pos pos = AppData.getInstance().getDestination();
        pos.mLatitude = user.mLatitude;
        pos.mLongitude = user.mLongitude;

        AppData.getInstance().setDestination(pos);

        mDestinationMarker.setPosition(new LatLng(pos.mLatitude, pos.mLongitude));
        mDestPos = new LatLng(pos.mLatitude, pos.mLongitude);
        calculateRoute();

        return true;
    }

    private boolean calculateRoute() {
        ArrayList<NaviLatLng> startPoints = new ArrayList<>();
        startPoints.add(new NaviLatLng(mCurrentPos.latitude, mCurrentPos.longitude));
        ArrayList<NaviLatLng> endPoints = new ArrayList<>();
        endPoints.add(new NaviLatLng(mDestPos.latitude, mDestPos.longitude));

        return mAMapNavi.calculateDriveRoute(startPoints, endPoints, null, AMapNavi.DrivingDefault);
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
        user.mAvatar = Integer.valueOf(avatar).intValue();
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

        sAvatarSize = (int)getResources().getDisplayMetrics().density * 50;

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IMClient.getsInstance().sendMessage();
            }
        });

        findViewById(R.id.btn_together).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gpsIntent = new Intent(MainActivity.this,
                        NaviActivity.class);
                gpsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gpsIntent);
            }
        });

        mMapView = (MapView)findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        findViewById(R.id.btn_search_des).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        SearchDesDlgView.class);

                startActivity(intent);
            }
        });

        init();

        tm.schedule(task, 1000, 5000);
    }

    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            setupMap();
        }

        mAMapNavi = AMapNavi.getInstance(this);
        mAMapNavi.setAMapNaviListener(this);

        mRouteOverLay = new RouteOverLay(mAMap, null);

        mAvatars.clear();

        ArrayList<Integer> list = new ArrayList<>();
        list.add(Integer.valueOf(R.drawable.avatar1l));
        list.add(Integer.valueOf(R.drawable.avatar1m));
        list.add(Integer.valueOf(R.drawable.avatar1h));

        mAvatars.put(Integer.valueOf(R.drawable.avatar1), list);

        list = new ArrayList<>();
        list.add(Integer.valueOf(R.drawable.avatar2l));
        list.add(Integer.valueOf(R.drawable.avatar2m));
        list.add(Integer.valueOf(R.drawable.avatar2h));

        mAvatars.put(Integer.valueOf(R.drawable.avatar2), list);

        list = new ArrayList<>();
        list.add(Integer.valueOf(R.drawable.avatar3l));
        list.add(Integer.valueOf(R.drawable.avatar3m));
        list.add(Integer.valueOf(R.drawable.avatar3h));

        mAvatars.put(Integer.valueOf(R.drawable.avatar3), list);

        list = new ArrayList<>();
        list.add(Integer.valueOf(R.drawable.avatar4l));
        list.add(Integer.valueOf(R.drawable.avatar4m));
        list.add(Integer.valueOf(R.drawable.avatar4h));

        mAvatars.put(Integer.valueOf(R.drawable.avatar4), list);

        list = new ArrayList<>();
        list.add(Integer.valueOf(R.drawable.avatar5l));
        list.add(Integer.valueOf(R.drawable.avatar5m));
        list.add(Integer.valueOf(R.drawable.avatar5h));

        mAvatars.put(Integer.valueOf(R.drawable.avatar5), list);
    }

    private void setupMap() {
        User curr = AppData.getInstance().getCurrentUser();
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.title(" ");
        markerOption.perspective(true);
        markerOption.draggable(true);
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.come_here));

        mMyMarker = mAMap.addMarker(
                markerOption
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(Integer.valueOf(IMClient.getsInstance().getAvatar())))
                        .position(new LatLng(curr.mLatitude, curr.mLongitude)));


        MyLocationStyle myLocationStyle = new MyLocationStyle();
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        myLocationStyle.strokeColor(Color.BLUE);
        myLocationStyle.radiusFillColor(Color.argb(88, 0, 0, 180));
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
                        .position(new LatLng(39.90403, 116.407525)));
//                        .position(
//                                new LatLng(
//                                        AppData.getInstance().getDestination().mLatitude,
//                                        AppData.getInstance().getDestination().mLongitude)));

        mDestPos = new LatLng(39.90403, 116.407525);
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
        if (aMapLocation != null) {
            Log.d("zzx", "location changed.");
            mMyMarker.setPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation
                    .getLongitude()));
            double lat = aMapLocation.getLatitude();
            double lng = aMapLocation.getLongitude();

            IMClient.getsInstance().updateLocation(String.valueOf(lat),
                    String.valueOf(lng));

            mCurrentPos = new LatLng(lat, lng);

            calculateRoute();

            if (mListener != null) {
                mListener.onLocationChanged(aMapLocation);
            }
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
        }

        mAMapLocationManager.requestLocationData(
                LocationProviderProxy.AMapNetwork, -1, 1, this);
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
        ImageView view = new ImageView(getApplication());
        view.setImageResource(R.drawable.come_here);
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
//        Log.v("hccc", "====on info window click==" + marker  + " " + mMyMarker);
//        if (marker == mMyMarker) {
//            Log.v("hccc", "====on info window click=2222=");
//            mMyMarker.hideInfoWindow();
//        }
        marker.hideInfoWindow();
        //������
        IMClient.getsInstance().setDestination(String.valueOf(mCurrentPos.latitude), String.valueOf(mCurrentPos.longitude));
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == mMyMarker) {
            mMyMarker.showInfoWindow();
        }
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
