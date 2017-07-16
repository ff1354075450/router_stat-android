package demo.com.rounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import demo.com.rounter.adapter.MainPagerAdapter;
import demo.com.rounter.bean.SelectActivity;
import demo.com.rounter.qr.CaptureActivity;

import static demo.com.rounter.R.id.viewpager;

public class MainActivity extends Activity {

    private static final String TAG = "mainActivity";

    private Context context;

    private ImageView icon1;
    private ImageView icon2;
    private ImageView icon3;
    private TextView icon1Text;
    private TextView icon2Text;
    private TextView icon3Text;


    private List<WebView> viewList;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    //开启二维码扫描
                    startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 0);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();
        initViewPager();
    }

    private void initView() {
        icon1 = (ImageView) findViewById(R.id.icon1);
        icon2 = (ImageView) findViewById(R.id.icon2);
        icon3 = (ImageView) findViewById(R.id.icon3);
        icon1Text = (TextView) findViewById(R.id.icon1text);
        icon2Text = (TextView) findViewById(R.id.icon2text);
        icon3Text = (TextView) findViewById(R.id.icon3text);
        viewPager = (ViewPager) findViewById(viewpager);
    }

    private void initViewPager() {
        viewList = new ArrayList<WebView>();
        viewList.add(initWebView("file:///android_asset/index.html"));
        viewList.add(initWebView("file:///android_asset/index.html"));
        viewList.add(initWebView("file:///android_asset/index.html"));
        viewPager.setAdapter(new MainPagerAdapter(viewList));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewList.get(position).loadUrl("javascript:onResume()");
                initfooter();
                switch (position){
                    case 0:
                        Drawable drawable = getResources().getDrawable(R.drawable.footer11,null);
                        icon1.setImageDrawable(drawable);
                        icon1Text.setTextColor(Color.rgb(35, 192, 250));
                        break;
                    case 1:
                        Drawable drawable1 = getResources().getDrawable(R.drawable.footer12,null);
                        icon2.setImageDrawable(drawable1);
                        icon2Text.setTextColor(Color.rgb(35, 192, 250));
                        break;
                    case 2:
                        Drawable drawable2 = getResources().getDrawable(R.drawable.footer13,null);
                        icon3.setImageDrawable(drawable2);
                        icon3Text.setTextColor(Color.rgb(35, 192, 250));
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state){
            }
        });
    }

    private void initfooter() {
        Drawable footer1 = getResources().getDrawable(R.drawable.footer01,null);
        Drawable footer2 = getResources().getDrawable(R.drawable.footer02,null);
        Drawable footer3 = getResources().getDrawable(R.drawable.footer03,null);
        icon1.setImageDrawable(footer1);
        icon2.setImageDrawable(footer2);
        icon3.setImageDrawable(footer3);
        icon1Text.setTextColor(Color.rgb(151, 151, 151));
        icon2Text.setTextColor(Color.rgb(151, 151, 151));
        icon3Text.setTextColor(Color.rgb(151, 151, 151));
    }

    private WebView initWebView(String url){
        WebView webView = new WebView(context);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webSettings.setDefaultTextEncodingName("utf-8");
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(true);
        webView.addJavascriptInterface(new Jsoperation(context,mainHandler),"client");
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //title为html的title标签中的值
            }
        });
        return webView;
    }

    public  void Icon1Click(View view){
        viewPager.setCurrentItem(0);
    }
    public  void Icon2Click(View view){
        viewPager.setCurrentItem(1);

    }
    public  void Icon3Click(View view){
        viewPager.setCurrentItem(2);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //    获取扫码的二维码中的信息
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String qrinfo = data.getStringExtra(CaptureActivity.EXTRA_RESULT);
            Log.e("qrinfo","二维码中的信息"+qrinfo);
        }else if(requestCode == 0 && resultCode == RESULT_OK){
            String select = data.getStringExtra(SelectActivity.EXTRA_RESULT);
            Log.e("select",select);
        }
    }
}
