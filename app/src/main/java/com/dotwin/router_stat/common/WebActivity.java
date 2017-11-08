package com.dotwin.router_stat.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.dotwin.router_stat.BuildConfig;

import java.io.File;


/**
 * Created by ff135 on 2017/9/15.
 */

public class WebActivity extends Activity {

    private final static int FILECHOOSER_RESULTCODE = 10;// 表单的结果回调
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private WebView webView;
    private File picfile;
    private Context context;
    ProgressDialog progressDialog;

    public void initWebview(WebView webView, JsClient jsClient, final Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在加载页面");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                Log.d("TTTT", "弹出提示");
            }
        }

        picfile = new File(Environment.getExternalStorageDirectory(),"temp.jpg");
        if (picfile.exists()){
            picfile.delete();
        }
        this.webView=webView;
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
//        webView.setWebContentsDebuggingEnabled(true);
        webView.loadUrl("file:///android_asset/index.html");
        //5.0以上 webview 需要自己同步cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        webView.setOnLongClickListener(new View.OnLongClickListener() {//禁止长按复制
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        webView.addJavascriptInterface(jsClient, "client");//提供js方法调用
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setDefaultTextEncodingName("utf-8");

        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);

        settings.setDatabaseEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onGeolocationPermissionsHidePrompt() {
                super.onGeolocationPermissionsHidePrompt();
            }


            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            //读取html的title并显示
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(picfile));
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            }
            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                Log.e("onShowFileChooser","拍照");
                mUploadCallbackAboveL = filePathCallback;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //申请权限，REQUEST_TAKE_PHOTO_PERMISSION是自定义的常量
                        String[] permissions = {
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        };
                        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(permissions, 2);
                            Toast.makeText(context, "无拍照权限", Toast.LENGTH_SHORT).show();
                        }
                        if (checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(permissions, 2);
                            Toast.makeText(context, "无存储权限", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    } else {
                        Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", picfile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                        startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                        return true;
                    }
                }else {
                    Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", picfile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                    return true;
                }
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(picfile));
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(picfile));
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=RESULT_OK) return;
        switch (requestCode){
            case FILECHOOSER_RESULTCODE:
                Bitmap bitmap =null;
                if (picfile.exists()){
                    bitmap = BitmapFactory.decodeFile(picfile.getAbsolutePath());
                    Uri result = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
                    if (mUploadCallbackAboveL != null) {
                        mUploadCallbackAboveL.onReceiveValue(new Uri[]{result});
                    } else {
                        mUploadMessage.onReceiveValue(result);
                        mUploadMessage = null;
                    }
                }
                break;
        }
    }


    private long mPressedTime = 0;
    /**
     * 点击返回，返回上一个网页
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); // 后退
            return true;
        } else {
            long mNowTime = System.currentTimeMillis();//获取第一次按键时间
            if((mNowTime - mPressedTime) > 2000){
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                mPressedTime = mNowTime;
            }else{
//                this.finish();
                onDestroy();
            }
            return true;
        }
    }
}
