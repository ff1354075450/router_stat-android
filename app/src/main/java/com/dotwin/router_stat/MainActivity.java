package com.dotwin.router_stat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import com.dotwin.router_stat.common.WebActivity;
import com.dotwin.router_stat.location.Gps;
import com.dotwin.router_stat.location.LbsAndWifi;
import com.dotwin.router_stat.qr.CaptureActivity;
import com.dotwin.router_stat.tencentmap.Item;
import com.dotwin.router_stat.tencentmap.MapActivity;
import com.dotwin.router_stat.update.Version;
import com.dotwin.router_stat.util.Https;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;



public class MainActivity extends WebActivity {

    private AlertDialog.Builder dialog;
    private  AlertDialog.Builder premissionDialog=null;
    private Dialog progressDialog;
    private Version myVersion;
    private ImageView imageView;
    public final static int QRCODE=1;
    public final static int GPS=2;
//    public final static String SERVER="http://root.dotwintech.com:5088/";
    public final static String SERVER="http://192.168.1.23:88/";
    private WebView webView;
    private String callBack;
    private Context context;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case QRCODE:
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            //申请权限，REQUEST_TAKE_PHOTO_PERMISSION是自定义的常量
                            String[] permissions = {
                                    Manifest.permission.CAMERA
                            };
                            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(permissions, 1);
                            }
                        } else {
                            callBack = (String) msg.obj;
                            //开启二维码扫描
                            startActivityForResult(new Intent(context, CaptureActivity.class), QRCODE);
                        }
                    }else {
                        callBack = (String) msg.obj;
                        //开启二维码扫描
                        startActivityForResult(new Intent(context, CaptureActivity.class), QRCODE);
                    }
                    break;
                case GPS:
                    initGpsAndLbs();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        context = this;
        update(SERVER+"router_stat/api/version/latest");
        initView();
        JsOperation jsOperation = new JsOperation(this,handler);
        initWebview(webView,jsOperation,this);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e("webview","page start time :"+ System.currentTimeMillis());
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.e("webview","page finish time :"+ System.currentTimeMillis());
                imageView.setVisibility(View.GONE);
            }
        });

        //获取gps，lbs，wifi信息,关闭wifi
        initGpsAndLbs();
        initFlag=false;
    }

    private boolean initFlag = false;
    public void initGpsAndLbs(){
        if (!initFlag) {
            try {
                openWifi();
//        //增加lbs定位监听
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        initFlag=false;
                        return;
                    }
                }
                telephonyManager.listen(new PhoneStateListener() {
                    public void onCellInfoChanged(List<CellInfo> cellInfo) {
                        LbsAndWifi.getCellInfo(cellInfo);
                    }
                }, PhoneStateListener.LISTEN_CELL_LOCATION);
                Gps gps = new Gps(this);
                if (!gps.isOpen()) {
                    gps.openGps();
                }
                if (!gps.gpsListener()){
                    Toast.makeText(context,"打开gps失败",Toast.LENGTH_SHORT).show();
                }
                initFlag = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void openWifi(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            Toast.makeText(this,"close wifi",Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.webView);
        imageView = (ImageView) findViewById(R.id.welcom);
        progressDialog = MapActivity.createLoadingDialog(this,"正在升级……");
        progressDialog.setCancelable(false);
        dialog = new AlertDialog.Builder(this);
        dialog.setTitle("升级提示");
        dialog.setMessage("是否需要升级最新版本的路由上报");
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                myVersion.down();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case QRCODE:
                try {
                    String qrinfo = data.getStringExtra(CaptureActivity.EXTRA_RESULT);
                    Log.e("qr", "扫描二维码" + callBack + ":" + qrinfo);
                    webView.loadUrl("javascript:" + callBack + "(\"" + qrinfo + "\")");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    private void requestPremission(){

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            //定位权限请求
            String[] permissions = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
            };
            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED||
                    checkSelfPermission(permissions[2]) != PackageManager.PERMISSION_GRANTED||
                    checkSelfPermission(permissions[3]) != PackageManager.PERMISSION_GRANTED||
                    checkSelfPermission(permissions[4]) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(permissions,2);
            }

        }
        Item item= MapActivity.selectedItem;
        if (item!=null) {
            double lat = item.getLat();
            double lon = item.getLon();
            String addr = item.getAddr();
            String name = item.getName();
            webView.loadUrl("javascript:onResume("+lat
                    +","+lon
                    +",\""+item.getCityName()+"\""
                    +",\""+name+"\""
                    +",\""+addr+"\""
                    +")");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    //授权
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0) {
            if (grantResults.length>=5){
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        premissDialog();
                        break;
                    }
                }
                initGpsAndLbs();
                initFlag=false;
            }
        }
    }

    private boolean dialogShow=true;
    private void premissDialog(){
        if (premissionDialog==null) {
            premissionDialog = new AlertDialog.Builder(this);
            premissionDialog.setTitle("缺少权限");
            premissionDialog.setMessage("请为应用添加必要权限,否则程序部分功能无法使用");
            premissionDialog.setNegativeButton("取消",null);
            premissionDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialogShow=true;
                }
            });
        }
        if (dialogShow) {
            premissionDialog.show();
            dialogShow=false;
        }
    }

    /**
     * 检查最新版本并且更新
     * @param checkUrl 获取最新版本的版本号
     */
    public void update(String checkUrl){
        Request request = new Request.Builder().url(checkUrl).build();
        Https.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("update","check error"+e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    if (jsonObject.getInt("result")==1){
                        String  updatVersion = jsonObject.getString("versionCode");
                        myVersion = new Version(context);
                        String versionCode = myVersion.getVersionCode();
                        if (updatVersion != null && !"".equals(updatVersion) && !updatVersion.equals(versionCode)){
                            myVersion.setDownUrl(jsonObject.getString("downUrl"));
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.show();
                                }
                            }, 1000);

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
