package demo.com.rounter.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.NeighboringCellInfo;
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

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.host;
import static android.R.attr.track;

/**
 * 一个gpsserver类，主要就启动一个线程，用于上传基站信息，和gps信息
 * Created by ff135 on 2017/5/31.
 */

public class GPSServer extends Service implements TencentLocationListener {
    private TencentLocationManager mLocationManager;
    public static String gpsinfo;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        request.setInterval(1000 * 60);//1分钟定位一次
        mLocationManager = TencentLocationManager.getInstance(this);
        int error = mLocationManager.requestLocationUpdates(request, this);
        if (error == 0) {
            Log.d("xxx", "注册监听成功");
        } else {
            Log.d("xxx", "注册监听失败" + error);
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {

    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (TencentLocation.ERROR_OK == i) {
            // 定位成功
            gpsinfo= tencentLocation.getLongitude() + "::" + tencentLocation.getLatitude()  + "::" + tencentLocation.getAddress();
            Log.d("xx",gpsinfo);
        } else {
            Log.e("onLocation","locate failed");
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
//        Log.d("xx", "StatusUpdate:" + s + " " + i + "  " + s1);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }


}
