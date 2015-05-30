package com.sogou.aiduijiang;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;


import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by cyz on 2015/5/30.
 */
public class SearchDesDlgView extends Activity implements TextWatcher,
        PoiSearch.OnPoiSearchListener {
    public static final String EXTRA_ANSWER_LATION =
            "com.sogou.aiduijiang.SearchDesDlgView.Lat";
    public static final String EXTRA_ANSWER_LONGTION =
            "com.sogou.aiduijiang.SearchDesDlgView.Long";

    private AutoCompleteTextView mSearchDesText;
    private Button mSearchBtn;
    private ListView mListView;

    private PoiSearch.Query startSearchQuery;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private LatLonPoint startPoint = null;

    private RouteSearchAdapter adapter;
    private List<PoiItem> poiItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.des_search_dlg);

        mSearchDesText = (AutoCompleteTextView) findViewById(R.id.keyWord);
        mSearchDesText.addTextChangedListener(this);

        mSearchBtn = (Button) findViewById(R.id.searchButton);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchResult();
            }
        });

        findViewById(R.id.btn_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mListView = (ListView) findViewById(R.id.ListView_nav_search_list_poi);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        Inputtips inputTips = new Inputtips(SearchDesDlgView.this,
                new Inputtips.InputtipsListener() {

                    @Override
                    public void onGetInputtips(List<Tip> tipList, int rCode) {
                        if (rCode == 0 && tipList != null) {// 正确返回
                            List<String> listString = new ArrayList<String>();
                            for (int i = 0; i < tipList.size(); i++) {
                                if (tipList.get(i) != null) {
                                    listString.add(tipList.get(i).getName());
                                }
                            }
                            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    R.layout.route_inputs, listString);
                            mSearchDesText.setAdapter(aAdapter);
                            aAdapter.notifyDataSetChanged();
                        }
                    }
                });
        try {
            inputTips.requestInputtips(newText,  "北京");
            // 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号

        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    private void startSearchResult() {
        String strStart = mSearchDesText.getText().toString().trim();
        if (strStart.length() == 0
                || strStart.equals("请输入关键字")) {
            Toast.makeText(this, "请选择目的地",
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            showProgressDialog();
            startSearchQuery = new PoiSearch.Query(strStart, "", "010"); // 第一个参数表示查询关键字，第二参数表示poi搜索类型，第三个参数表示城市区号或者城市名
            startSearchQuery.setPageNum(0);// 设置查询第几页，第一页从0开始
            startSearchQuery.setPageSize(20);// 设置每页返回多少条数据
            PoiSearch poiSearch = new PoiSearch(SearchDesDlgView.this,
                    startSearchQuery);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();// 异步poi查询
        }
    }

    @Override
    public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {

    }

    /**
     * POI搜索结果回调
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();
        if (rCode == 0) {// 返回成功
            if (result != null && result.getQuery() != null
                    && result.getPois() != null && result.getPois().size() > 0) {// 搜索poi的结果
                if (result.getQuery().equals(startSearchQuery)) {
                    poiItems = result.getPois();// 取得poiitem数据
                    adapter = new RouteSearchAdapter(this, poiItems);
                    mListView.setAdapter(adapter);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {

                            PoiItem pos = poiItems.get(position);
                            LatLonPoint startpos = pos.getLatLonPoint();
                            String starttitle = pos.getTitle();
                            Log.v("SearchDesDlgView", "LatLonPoint Lati = " + startpos.getLatitude() + " Longi = " + startpos.getLongitude());
                            setAnswerShownResult(startpos.getLatitude(), startpos.getLongitude());
                            finish();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "没有结果", Toast.LENGTH_SHORT)
                        .show();
            }
        } else{
            Toast.makeText(this, "请求错误",Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void setAnswerShownResult(double lat, double Lon) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_LATION, lat);
        data.putExtra(EXTRA_ANSWER_LONGTION, Lon);
        setResult(RESULT_OK, data);
    }
}
