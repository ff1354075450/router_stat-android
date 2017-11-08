package com.dotwin.router_stat.tencentmap;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dotwin.router_stat.R;
import com.dotwin.router_stat.location.Gps;
import com.dotwin.router_stat.util.Https;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class MapActivity extends com.tencent.tencentmap.mapsdk.map.MapActivity implements TencentLocationListener{

    private MapView mapView;
    private RecyclerView recyclerView;
    private TencentLocationManager mLocationManager;
    private TextView sure;
    private ImageView loation;
    private ArrayList<Item> data;
    private MyAdapter myAdapter;
    private Marker marker;
    private String key="Y7TBZ-KNHC3-7S634-YID54-BBCEQ-XVBS7";
    private TencentMap tencentMap;
    private Dialog progressDialog;
    public static Item selectedItem;


    private double lat;
    private double lng;
    private double loclat;
    private double loclng;

//    用于上拉加载
    private int pageIndex =1;
    private int totalPage=20;//临时数据,应该是20
    private boolean isLoading = false;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tencentmap);
//        获取数据
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        lat = bundle.getDouble("lat");
        lng = bundle.getDouble("lng");
        loclat = lat;
        loclng = lng;

        mapView = (MapView) findViewById(R.id.mapview);
        sure= (TextView) findViewById(R.id.sure);
        loation = (ImageView) findViewById(R.id.location);
        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        progressDialog = createLoadingDialog(this,"数据加载中……");
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        initTencentMap(lat,lng,null);
        getData(lat,lng,pageIndex,5);
        myAdapter = new MyAdapter(data);
        myAdapter.setOnClickListener(new MyCilckListener() {
            @Override
            public void onClick(int position,View v) {
                for (int i = 0; i < data.size(); i++) {
                    if (i==position){
                        sure.setTextColor(getResources().getColor(R.color.sure));
                        selectedItem = data.get(position);
                        selectedItem.setTvgou(R.drawable.gou);
                        LatLng latLng = new LatLng(data.get(position).getLat(),data.get(position).getLon());
                        marker.setPosition(latLng);
                        tencentMap.setCenter(latLng);
                        marker.setTitle(data.get(position).getName());
                        marker.showInfoWindow();
                    }else {
                        Item item = data.get(i);
                        item.setTvgou(0);
                    }
                    myAdapter.updateData(data);
                }

            }
        });
        recyclerView.setAdapter(myAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));




//       上拉加载监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && lastVisibleItemPosition+1==myAdapter.getItemCount()){
                    if (pageIndex<totalPage) {
                        isLoading = true;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getData(lat, lng, ++pageIndex,5);
                                isLoading = false;
                            }
                        }, 1000);
                    }else {
                        myAdapter.changeEnd(MyAdapter.TYPE_END);
                        myAdapter.notifyItemRemoved(lastVisibleItemPosition);
                    }
                }
            }
        });


//       android6.0 权限请求
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(permissions, 0);
            }
        }else {
            requestLocation();
        }


    }



    //腾讯地图相关操作,设置经纬度，设置缩放
    private void initTencentMap(double lat1, final double lng1, String name) {
        LatLng latLng = new LatLng(lat1, lng1);
        tencentMap = mapView.getMap();
        tencentMap.setCenter(latLng);
        tencentMap.setZoom(16);//地图缩放
        marker = tencentMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(name)
                .anchor(0.5f, 0.5f)
                .draggable(true)
                .icon(BitmapDescriptorFactory
                        .defaultMarker())
                .draggable(true));
        marker.showInfoWindow();// 设置默认显示一个infoWindow
        //地图点击事件监听
        tencentMap.setOnMapClickListener(new TencentMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lat=latLng.getLatitude();
                lng=latLng.getLongitude();
                marker.setPosition(latLng);
                loation.setImageResource(R.drawable.location);
                data.clear();
                data=null;
                pageIndex=1;
                selectedItem=null;
                sure.setTextColor(getResources().getColor(R.color.unsure));
                getData(latLng.getLatitude(),latLng.getLongitude(),pageIndex,5);
            }
        });
    }

    /**
     * 请求定位
     */
    public void requestLocation(){
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        request.setAllowCache(false);
        request.setAllowGPS(true);
        request.setInterval(1000);//1秒定位一次
        mLocationManager = TencentLocationManager.getInstance(this);
        int error = mLocationManager.requestLocationUpdates(request, this);
        if (error == 0) {
            Log.d("xxx", "注册监听成功");
        } else {
            Log.d("xxx", "注册监听失败" + error);
        }
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int error, String s) {
        if (TencentLocation.ERROR_OK == error) {
            lat=tencentLocation.getLatitude();
            lng=tencentLocation.getLongitude();
            Log.d("location",tencentLocation.getLatitude()+":"+tencentLocation.getLongitude());
            // 定位成功
        } else {
            // 定位失败
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        Log.i("statusUpdate","name:"+s+" state:"+i+" desc:"+s1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 0:
                if (grantResults.length>0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    授权成功
                    requestLocation();
                }else{
//                  授权失败
                }
                break;
        }
    }




    //获取相关连接数据,coord_type1表示gps的位置，5表示腾讯定位的位置
    public void getData(double lat, double lng, final int index, int coord_type) {
        progressDialog.show();
        if (data==null){
            data = new ArrayList<>();
        }
        Request request = new Request.Builder()
                .url("http://apis.map.qq.com/ws/geocoder/v1/?location="+lat+","+lng+"&key="+key+"&get_poi=1&&coord_type="+coord_type+"&output=json&poi_options=radius=300;page_index="+index)
                .build();
        Https.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("data",e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String res = response.body().string();
                    final JSONObject jsonObject = new JSONObject(res);
                    JSONArray pois = ((JSONObject) jsonObject.get("result")).getJSONArray("pois");
                    final String name=((JSONObject) jsonObject.get("result")).getString("address");
                    final double tlat = ((JSONObject) jsonObject.get("result")).getJSONObject("location").getDouble("lat");
                    final double tlng = ((JSONObject) jsonObject.get("result")).getJSONObject("location").getDouble("lng");
                    if (pois.length()<10){//如果没有数据则提示不存在数据
                        myAdapter.changeEnd(MyAdapter.TYPE_END);
                    }
                    for (int i = 0; i < pois.length(); i++) {
                        JSONObject poi = pois.getJSONObject(i);
                        String title = poi.getString("title");
                        String addr = poi.getString("address");
                        JSONObject location = poi.getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng =location.getDouble("lng");
                        String city= poi.getJSONObject("ad_info").getString("city");
                        Item item = new Item();
                        item.setLat(lat);
                        item.setLon(lng);
                        item.setAddr(addr);
                        item.setName(title);
                        item.setCityName(city);
                        data.add(item);
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (index==1) {
                                marker.setTitle(name);
                                LatLng latLng = new LatLng(tlat, tlng);
                                marker.setPosition(latLng);
                                marker.showInfoWindow();
                                tencentMap.setCenter(latLng);
                            }
                            myAdapter.updateData(data);
                            progressDialog.dismiss();
                        }
                    },1000);
                } catch (JSONException e) {
                    myAdapter.changeEnd(MyAdapter.TYPE_END);
                    e.printStackTrace();
                }
            }
        });
    }

    public void clickBack(View v){
        selectedItem=null;
        this.finish();
    }

    public void clickSure(View v){
        if (selectedItem!=null) {
           this.finish();
        }
    }

    public void location(View v) {
        loation.setImageResource(R.drawable.location_success);
        pageIndex = 1;
        data = null;
        getData(loclat, loclng, pageIndex,5);
    }


    public static Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.load_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.dialog_loading);// 创建自定义样式dialog

//        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;

    }
}
