package com.dotwin.router_stat.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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
import com.dotwin.router_stat.R;

/**
 * Created by ff135 on 2017/9/15.
 */

public abstract class JsClient {
    protected Context context;
    private Dialog loadingDialog = null;
    private Toast toast = null;

    public JsClient(Context context){
        this.context = context;
    }

    protected void startActivity(Intent intent) {
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
    public void myExit() {
        ((Activity) context).finish();
    }

    /**
     * print log
     * @param s
     */
    @JavascriptInterface
    public void jslog(String s) {
        Log.d("jsClient", s);
    }

}
