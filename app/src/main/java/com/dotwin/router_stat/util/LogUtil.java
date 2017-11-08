package com.dotwin.router_stat.util;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.data;

/**
 * Created by ff135 on 2017/10/21.
 */

public class LogUtil {

    private static PrintWriter mylogout = null;

    @SuppressLint("SimpleDateFormat")
    public static void log(String tag,String text) {
        try {
            Log.i(tag, text);
            // 保存本地日志
            if (mylogout == null) {
                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath()
                        + File.separator
                        +"router.log");
                if((System.currentTimeMillis() - file.lastModified())> 24 * 60 * 60 * 1000){
                    file.delete();
                    file.createNewFile();
                }
                mylogout = new PrintWriter(new FileWriter(file, true));
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
            String data = df.format(new Date());
            mylogout.println(data + "    " + tag + "    " + text);
            mylogout.flush();
        } catch (Exception e) {
            try{
                mylogout.close();
            }catch (Exception e1){
            }finally{
                mylogout=null;
            }
        }
    }
}
