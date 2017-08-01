package demo.com.rounter.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentLocationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import demo.com.rounter.utils.GpsUtil;

/**
 * 一个gpsserver类，主要就启动一个线程，用于上传基站信息，和gps信息
 * Created by ff135 on 2017/5/31.
 */

public class GPSServer extends Service implements TencentLocationListener {
    private TencentLocationManager mLocationManager;
    private TelephonyManager telephonyManager = null;
    public static JSONObject json;//将要返回给网页的json数据
    private static String provider = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        json = new JSONObject();
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        request.setAllowCache(false);
        request.setAllowGPS(true);
        request.setInterval(1000 * 60);//1分钟定位一次
        mLocationManager = TencentLocationManager.getInstance(this);
        int error = mLocationManager.requestLocationUpdates(request, this);
        if (error == 0) {
            Log.d("xxx", "注册监听成功");
        } else {
            Log.d("xxx", "注册监听失败" + error);
        }
        telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startId) {

        json.remove("lon");
        json.remove("lat");
        json.remove("lbsList");
        json.remove("wifiList");
        getWifiInfo();
        setPhoneStateListener();
        getGps();
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (TencentLocation.ERROR_OK == i) {
            // 定位成功
            try {
                json.put("addr", tencentLocation.getAddress());
//                json.put("lon",tencentLocation.getLongitude());
//                json.put("lat",tencentLocation.getLatitude());

                Log.d("gpsinfo", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("onLocation", "locate failed");
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
//        Log.d("xx", "StatusUpdate:" + s + " " + i + "  " + s1);//4表示gps不可用
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }


    /**
     * 监听基站的变化
     */
    private void setPhoneStateListener() {
        telephonyManager.listen(new PhoneStateListener() {
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                json.remove("lbsList");
                JSONArray jsonArray = getCellInfo(cellInfo);
                try {
                    json.put("lbsList", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, PhoneStateListener.LISTEN_CELL_LOCATION);

        //获基站信息
        List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
        JSONArray jsonArray = getCellInfo(cellInfo);
        try {
            json.put("lbsList", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getCellInfo(List<CellInfo> cellInfo) {
        int cid = 0, lac = 0;
        JSONArray jsonArray = new JSONArray();
        try {
            for (CellInfo info : cellInfo) {
                if (info.getClass().getName().equals("android.telephony.CellInfoLte")) {
                    CellIdentityLte cellIdentityLte = ((CellInfoLte) info).getCellIdentity();
                    cid = cellIdentityLte.getCi();
                    lac = cellIdentityLte.getTac();
                } else if (info.getClass().getName().equals("android.telephony.CellInfoCdma")) {
                    CellIdentityCdma cellIdentityCdma = ((CellInfoCdma) info).getCellIdentity();
                    cid = cellIdentityCdma.getBasestationId();
                    lac = cellIdentityCdma.getNetworkId();
                } else if (info.getClass().getName().equals("android.telephony.CellInfoWcdma")) {
                    CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                    cid = cellIdentityWcdma.getCid();
                    lac = cellIdentityWcdma.getLac();
                } else if (info.getClass().getName().equals("android.telephony.CellInfoGsm")) {
                    CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) info).getCellIdentity();
                    cid = cellIdentityGsm.getCid();
                    lac = cellIdentityGsm.getLac();
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cid", cid);
                jsonObject.put("lac", lac);
                jsonArray.put(jsonObject);
            }
            Log.d("lbs", jsonArray.toString());
        } catch (Exception e) {

        }
        return jsonArray;
    }


    private void getWifiInfo() {
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!manager.isWifiEnabled()) {//打开wifi
            manager.setWifiEnabled(true);
        }
        //扫描热点
        JSONArray jsonArray = new JSONArray();
        manager.startScan();
        List<ScanResult> mWifiList = manager.getScanResults();
        for (ScanResult scanResult : mWifiList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("bssid", scanResult.BSSID);
                jsonObject.put("ssid", scanResult.SSID);
                jsonObject.put("level", scanResult.level);//信号
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            json.put("wifiList", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void getGps() {
        GpsUtil gpsUtil = new GpsUtil();
        if (!gpsUtil.isOpen(this)) {
            gpsUtil.openGps(this);
        }
        gpsUtil.getGpsConfi();
        try {
            json.put("lon", gpsUtil.getLon());
            json.put("lat", gpsUtil.getLat());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("lon", gpsUtil.getLon() + " lat:" + gpsUtil.getLat());

    }

}
