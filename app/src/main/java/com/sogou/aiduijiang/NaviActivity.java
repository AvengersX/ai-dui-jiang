package com.sogou.aiduijiang;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;

/**
 * Created by zhouzhenxing on 2015/5/30.
 */
public class NaviActivity extends Activity implements
        AMapNaviViewListener {

    private AMapNaviView mAmapAMapNaviView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navi);

        mAmapAMapNaviView = (AMapNaviView)findViewById(R.id.navimap);
        mAmapAMapNaviView.onCreate(savedInstanceState);
        mAmapAMapNaviView.setAMapNaviViewListener(this);

        AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onNaviCancel();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {
        Intent intent = new Intent(NaviActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mAmapAMapNaviView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAmapAMapNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAmapAMapNaviView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAmapAMapNaviView.onDestroy();
    }
}
