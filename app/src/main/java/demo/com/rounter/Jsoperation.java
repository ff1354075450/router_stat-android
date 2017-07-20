package demo.com.rounter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import demo.com.rounter.location.GPSServer;


public class Jsoperation {
    private Context context;
    private Handler handler;
    private int opensize;
    private Dialog loadingDialog = null;
    private Toast toast = null;

    public Jsoperation(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    //将界面存入数组中去，方便删除
    public Jsoperation(Context context, Handler handler, int type) {
        this.context = context;
        this.handler = handler;
    }

    private void startActivity(Intent intent) {
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            a.startActivity(intent);
            //设置动画效果
            a.overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @JavascriptInterface
    public void progress(String type, String msg, String func) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view1 = inflater.inflate(R.layout.toast_1, null);
        toast = new Toast(context);
        ImageView img = (ImageView) view1.findViewById(R.id.toast_img);
        TextView textView = (TextView) view1.findViewById(R.id.toast_text);
        textView.setText(msg);
        if (type.equals("Success")) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.yes,null));
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(view1);
            toast.show();
        }
        if (type.equals("Error")) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.no,null));
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(view1);
            toast.show();
        }
        if (type.equals("Progress")) {
            loadingDialog = new Dialog(context);
            loadingDialog.setCancelable(true);
            loadingDialog.setContentView(view1);
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.loading1,null));
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.loading_animation);
            img.startAnimation(animation);
            loadingDialog.show();
        }
        if (type.equals("Dismiss")) {
            loadingDialog.dismiss();
        }
    }
    /**
     * 退出程序
     */
    @JavascriptInterface
    public void myexit() {

    }

    /**
     * print log
     * @param s
     */
    @JavascriptInterface
    public void jslog(String s) {
        Log.i("js", s);
    }

    @JavascriptInterface
    public void qr(String function){
        Message message = new Message();
        message.what=1;
        message.obj = function;
        handler.sendMessage(message);
    }

    @JavascriptInterface
    public void selectPhoto(String func){
        Message message = new Message();
        message.what=2;
        message.obj = func;
        handler.sendMessage(message);
    }

    @JavascriptInterface
    public void takePhoto(String func){
        Message message = new Message();
        message.what=3;//3
        message.obj = func;
        handler.sendMessage(message);
    }
    @JavascriptInterface
    public String getGps(){
        Message message = new Message();
        message.what=4;
        handler.sendMessage(message);
        return GPSServer.gpsinfo;
    }

    @JavascriptInterface
    public String getServerHost(){
        return WebActivity.server;
    }
}
