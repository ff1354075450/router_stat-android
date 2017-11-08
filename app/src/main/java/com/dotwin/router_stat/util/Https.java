package com.dotwin.router_stat.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ff135 on 2017/7/11.
 */

public class Https {

    private final static long TIMEOUT = 8000;
    public static OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }


    public static String get(String url){
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 上传单个文件
     *
     * @param url  请求的url地址
     * @param path 文件的本地路径
     * @return
     */
    public static String postFile(String url, String path) {
        List<String> list = new ArrayList<>();
        list.add(path);
        String s = null;
        try {
            s = post(url,new File(path),10000);
            Log.e("post",s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 使用okhttp3，上传多个文件，多个参数。
     *
     * @param url   请求的url
     * @param param map，请求参数的key，value
     * @param paths 文件集合，参数问file。
     * @return 请求结果，错误或者异常返回null
     */
    public static String postFile(String url, Map<String, String> param, List<String> paths) {
        String result = null;
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //加入参数
        if (param != null) {
            for (String key : param.keySet()) {
                multipartBodyBuilder.addFormDataPart(key, param.get(key));
            }
        }
        //加入文件
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                RequestBody body = RequestBody.create(MediaType.parse("image/jpg"), file);
                multipartBodyBuilder.addFormDataPart("file", file.getName(), body);//和后台约定,以file作为接收多张图片的key
//                multipartBodyBuilder.addFormDataPart("file",path);
            }
        }
        //构建请求体
        RequestBody requestBody = multipartBodyBuilder.build();
        Request.Builder RequestBuilder = new Request.Builder();
        RequestBuilder.url(url);// 添加URL地址
        RequestBuilder.post(requestBody);
        Request request = RequestBuilder.build();
        try {
            Response response = client.newCall(request).execute();
            result =  response.body().string();
            Log.e("xx", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String post(String url, File file, int timeout) throws IOException {
        /**
         * 第一部分
         */
        try {
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            /**
             * 设置关键值
             */
            con.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
            con.setConnectTimeout(8000);
            con.setReadTimeout(timeout);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false); // post方式不能使用缓存
            // 设置请求头信息
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            // 设置边界
            String BOUNDARY = "----------" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            // 请求正文信息
            OutputStream out = new DataOutputStream(con.getOutputStream());
            StringBuilder params = new StringBuilder();
            // 第零部分：参数
//            params.append("\r\n").append("--")
//                    .append(BOUNDARY)
//                    .append("\r\n");
//            params.append("Content-Disposition: form-data; name=\"" + "type" + "\"\r\n\r\n");
//            params.append(type);
//            out.write(params.toString().getBytes("utf-8"));

            // 第一部分：
            StringBuilder sb = new StringBuilder();
            sb.append("\r\n").append("--"); // ////////必须多两道线
            sb.append(BOUNDARY);
            sb.append("\r\n");

//			文件
            sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
            sb.append("Content-Type:image/jpg\r\n\r\n");
            byte[] head = sb.toString().getBytes("utf-8");
            out.write(head);
            // 文件正文部分
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();

            // 结尾部分
            byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
            out.write(foot);
            out.flush();
            out.close();

            /**
             * 读取服务器响应，必须读取,否则提交不成功
             */

            // return con.getResponseCode();
            /**
             * 下面的方式读取也是可以的
             */
            String result = "";

            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (SocketTimeoutException e) {
            return "time_out";
        }
    }
}
