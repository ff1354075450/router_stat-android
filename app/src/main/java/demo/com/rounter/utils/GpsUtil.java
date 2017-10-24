package demo.com.rounter.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ff135 on 2017/7/27.
 */

public class GpsUtil {

    private static LocationManager locationManager;
    private static double lat = 0.0, lon = 0.0;

    public static void openGps(Context context) {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvide");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        try {
            //使用PendingIntent发送广播告诉手机去开启GPS功能
            PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void getGpsConfi(Context context) {
        if(locationManager == null){
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    setLat(location.getLatitude());
                    setLon(location.getLongitude());

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("gpsStatusChange", provider + ";status" + status);
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
//        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if (location != null) {
//            lat = location.getLatitude();
//            lon = location.getLongitude();
//        }
    }

    public  static void setLat(double lat1){
        lat = lat1;
    }

    public  static  void setLon(double lon1){
        lon = lon1;
    }

    public  static double getLat() {
        return lat;
    }


    public  static double getLon() {
        return lon;
    }

}
