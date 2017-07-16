package demo.com.rounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import demo.com.rounter.bean.SelectActivity;
import demo.com.rounter.location.GPSServer;
import demo.com.rounter.qr.CaptureActivity;
import demo.com.rounter.utils.Https;

import static android.R.attr.path;
import static demo.com.rounter.R.id.toptitle;


/**
 * Created by ff on 2017/2/28.
 */

public class WebActivity extends Activity {

    public static final int SELECT_PHOTO = 3;
    public static final int TAKE_PHOTO = 1;
    public static final int QRCODE = 4;
    public static String server = "http://192.168.1.26:88/";
    private WebView webView;
    private Context context;
    private String callBack;
    private  Uri imageUri;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    callBack = (String) msg.obj;
                    //开启二维码扫描
                    Log.e("qr","扫描二维码"+callBack);
                    webView.loadUrl("javascript:"+callBack+"(1234)");
//                    startActivityForResult(new Intent(context, CaptureActivity.class), QRCODE);
                    break;
                case 2:
                    callBack = (String) msg.obj;
                    //选择图片
                    startActivityForResult(new Intent(context, SelectActivity.class),SELECT_PHOTO);
                    break;
                case 3:
                    Log.d("qr","拍照");
                    callBack = (String) msg.obj;
                    File outPutImg = new File(getExternalCacheDir(),"outPutImg.jpg");
                    if (outPutImg.exists()){
                        outPutImg.delete();
                    }
                    try {
                        outPutImg.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageUri = Uri.fromFile(outPutImg);
                    //启动相机程序
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, TAKE_PHOTO);
                    break;
                case 4:
                    startService(new Intent(context,GPSServer.class));
                    break;
                case 5:
                    String path=null;
                    String str = (String) msg.obj;
                    try {
                        if(str != null) {
                            JSONObject json = new JSONObject(str);
                            if (json.getInt("result") == 1) {
                                path = json.getString("object");
                            }else{
                                path="error";
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    webView.loadUrl("javascript:"+callBack+"(\""+path+"\")");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_web);
        //		初始化
        webView = (WebView) findViewById(R.id.webView1);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webSettings.setDefaultTextEncodingName("utf-8");
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebChromeClient(new WebChromeClient() {
            //读取html的title并显示
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        //提供js方法调用
        webView.addJavascriptInterface(new Jsoperation(context, handler), "client");
        //		获取传入参数，初始化页面
        webView.loadUrl("file:///android_asset/index.html");
        //开启gps位置监听
        startService(new Intent(context,GPSServer.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.loadUrl("javascript:onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void TopBack(View view) {
        if (webView.canGoBack()) {
            webView.goBack(); // 后退
        } else {
            this.finish();
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QRCODE && resultCode == RESULT_OK) {  //    获取扫码的二维码中的信息
            String qrinfo = data.getStringExtra(CaptureActivity.EXTRA_RESULT);
            webView.loadUrl("javascript:"+callBack+"(\""+qrinfo+"\")");
        }else if(requestCode == SELECT_PHOTO && resultCode == RESULT_OK){//选择图片
            final String select = data.getStringExtra(SelectActivity.EXTRA_RESULT);
            new Thread(){
                @Override
                public void run() {
                    String result = Https.postFile(server+"router_stat/api/router/uploadImg",select);
                    Log.e("pic",result);
                    Message msg = new Message();
                    msg.obj = result;
                    msg.what = 5;
                    handler.sendMessage(msg);
                }
            }.start();
        }else if(requestCode == TAKE_PHOTO && resultCode == RESULT_OK){
                try {
                 //文件为cache目录下的imguri,可以得到图片并且显示
                    new Thread(){
                        @Override
                        public void run() {
                            String result = Https.postFile(server+"router_stat/api/router/upload",getExternalCacheDir()+"/outPutImg.jpg");

                            Message msg = new Message();
                            msg.obj = result;
                            msg.what = 5;
                            handler.sendMessage(msg);
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * 点击返回，返回上一个网页
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); // 后退
            return true;
        } else {
            this.finish();
            return true;
        }
    }
}
