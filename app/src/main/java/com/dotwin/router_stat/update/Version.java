package com.dotwin.router_stat.update;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * update用于从服务器下载并且更新apk
 * 1.在mainifest.xml中加入
 *   <receiver android:name=".update.UpdateAppReceiver"
 *   android:enabled="true"
 *   android:exported="true">
 *   <intent-filter>
 *   <action android:name="android.intent.action.DOWNLOAD_COMPLETE" />
 *   <action android:name="android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED"/>
 *   </intent-filter>
 *   </receiver>
 * 2. 检查版本号之后调用down方法
 *
 * Created by ff135 on 2017/9/23.
 */

public class Version {

    private static final String TAG = Version.class.getSimpleName();
    public static long downloadUpdateApkId = -1;//下载更新Apk 下载任务对应的Id
    public static String downloadUpdateApkFilePath;//下载更新Apk 文件路径

    private final static String apkName="router_stat.apk";
    private PackageInfo packageInfo;
    private String packageName;
    private Context context;
    private String downUrl=null;

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public Version(Context context){
        this.context = context;
        PackageManager packageManager = context.getPackageManager();
        try {
            packageName = context.getPackageName();
            packageInfo = packageManager.getPackageInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getVersionCode(){
        return packageInfo.versionName;
    }

    public void update(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), apkName)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public void  down(){
        if (downUrl==null){
            return;
        }
        Uri uri = Uri.parse(downUrl);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setVisibleInDownloadsUi(true);
        request.setTitle("down:"+apkName);
        String filePath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//外部存储卡
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        } else {
            Log.i(TAG,"没有SD卡");
            return;
        }
        downloadUpdateApkFilePath = filePath + File.separator + apkName;
        // 若存在，则删除
        deleteFile(downloadUpdateApkFilePath);
        Uri fileUri = Uri.fromFile(new File(downloadUpdateApkFilePath));
        request.setDestinationUri(fileUri);
        downloadUpdateApkId = downloadManager.enqueue(request);
    }
    private static boolean deleteFile(String fileStr) {
        File file = new File(fileStr);
        return file.delete();
    }
}
