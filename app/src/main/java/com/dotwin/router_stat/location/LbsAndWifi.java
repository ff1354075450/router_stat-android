package com.dotwin.router_stat.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.dotwin.router_stat.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ff135 on 2017/9/15.
 */

public class LbsAndWifi {

    public static JSONArray lbs = new JSONArray();

    public static JSONArray wifi = new JSONArray();

    public static JSONArray getWifiInfo(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
        //赋值
        wifi=jsonArray;
        return jsonArray;
    }

    private static JSONObject getCurrentLbs(Context context){
        int cid=0,lac=0;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        if(telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA){
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)
                    telephonyManager.getCellLocation();
            cid = cdmaCellLocation.getBaseStationId(); //获取cdma基站识别标号 BID
            lac = cdmaCellLocation.getNetworkId(); //获取cdma网络编号NID
        }else{
            GsmCellLocation gsmCellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
            cid = gsmCellLocation.getCid(); //获取gsm基站识别标号
            lac = gsmCellLocation.getLac(); //获取gsm网络编号
        }
       if (lac!=0 && cid!=0){
         JSONObject jsonObject = new JSONObject();
           try {
               jsonObject.put("lac",lac);
               jsonObject.put("cid",cid);
               return jsonObject;
           } catch (JSONException e) {
               e.printStackTrace();
           }
       }
       return null;
    }


    public static JSONArray getlbs(Context context){
        JSONArray jsonArray=new JSONArray();
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
            jsonArray = getCellInfo(cellInfo);
            JSONObject jsonObject = getCurrentLbs(context);
            if (jsonObject != null) {
                boolean flag=true;
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    double lac = json.getDouble("lac");
                    double cid = json.getDouble("cid");
                    if (jsonObject.getDouble("lac") == lac && jsonObject.getDouble("cid") == cid){
                        flag=false;
                        break;
                    }
                }
                if (flag) {
                    jsonArray.put(jsonObject);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray getCellInfo(List<CellInfo> cellInfo) {
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
                boolean flag = true;
                //如果与已有数据重合则不添加
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    int tcid = (int) json.get("cid");
                    int tlac = (int) json.get("lac");
                    if (tcid==cid && tlac == lac){
                       flag=false;
                        break;
                    }
                }
                if (flag) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cid", cid);
                    jsonObject.put("lac", lac);
                    jsonArray.put(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

}
