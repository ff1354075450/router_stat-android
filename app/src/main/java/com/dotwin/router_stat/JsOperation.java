package com.dotwin.router_stat;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.dotwin.router_stat.common.JsClient;
import com.dotwin.router_stat.location.Gps;
import com.dotwin.router_stat.location.LbsAndWifi;
import com.dotwin.router_stat.tencentmap.MapActivity;
import com.dotwin.router_stat.update.Version;
import com.dotwin.router_stat.util.Https;
import com.dotwin.router_stat.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;

/**
 * Created by ff135 on 2017/9/15.
 */

public class JsOperation extends JsClient {

    //日6000分3000
    String[] aks = {"EhGOOxZsIkkTt5037gtjoz17au5mqFvA","n2VEeMGdvIERguO80PEsxMIPW8NWlDuj","0IWZ3it8NrGqRk71y5Q7UoHgRGsBGFxV"};
    //每日600条，一分钟最高并发3000
    private final static String baiduApi = "http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&output=json&pois=0&location=";//+39.983424,116.322987
    public final static String baiduApilon="http://api.map.baidu.com/geoconv/v1/?from=1&to=5&coords=";//118.9093208313,32.0823018482,经纬度转换
    private Handler handler;
    private SharedPreferences sharedPreferences= null;
    public JsOperation(Context context, Handler handler){
        super(context);
        this.handler = handler;
        sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE);
    }

    @JavascriptInterface
    public void myExit() {
        setUserName(null);
        setPassword(null);
        ((Activity) context).finish();
    }

    @JavascriptInterface
    public void qr(String function){
        Log.e("qr","open qe");
        Message message = new Message();
        message.what=MainActivity.QRCODE;
        message.obj = function;
        handler.sendMessage(message);
    }

    private int count=0;
    @JavascriptInterface
    public String getGps(){
        count++;
        Message message = new Message();
        message.what=MainActivity.GPS;
        handler.sendMessage(message);
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        Gps gps = new Gps(context);
        if (gps.isOpen()) {
            try {
                double lon = Gps.getLon();
                double lat = Gps.getLat();
                JSONArray lbsList = LbsAndWifi.getlbs(context);
                LogUtil.log("getLocation",lon+":"+lat+":"+Gps.satelliteCount+ "\n"+lbsList.toString());
                if (lon > 0.0 && lat > 0.0 && Gps.satelliteCount > 2 && lbsList.length() > 0) {
                    JSONObject jsonObject = new JSONObject();
                    wifiManager.setWifiEnabled(true);
                    jsonObject.put("wifiList", LbsAndWifi.getWifiInfo(context));
                    jsonObject.put("lbsList", lbsList);
                    jsonObject.put("lon", lon);
                    jsonObject.put("lat", lat);
                    count=0;
                    return jsonObject.toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "timeout";
        } else {
            return "gpsNotOpen";
        }
    }

    @JavascriptInterface
    public String getServerHost(){
        return MainActivity.SERVER;
    }
    @JavascriptInterface
    public void setUserName(String userName){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName",userName);
        editor.commit();
    }
    @JavascriptInterface
    public void setPassword(String password){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password",password);
        editor.commit();
    }
    @JavascriptInterface
    public String getUserName(){
        return sharedPreferences.getString("userName",null);
    }
    @JavascriptInterface
    public String getPassword(){
        return sharedPreferences.getString("password",null);
    }



    @JavascriptInterface
    public void openMap(double lat,double lon){
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lon);
        startActivity(intent);
    }


    @JavascriptInterface
    public String getVersion(){
        Version version = new Version(context);
        return version.getVersionCode();
    }

}