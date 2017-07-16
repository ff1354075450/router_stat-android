package demo.com.rounter.utils;

import java.io.File;
import java.io.IOException;
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
    private static OkHttpClient client;
    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT,TimeUnit.MILLISECONDS)
                .build();
    }



    /**
     * 上传单个文件
     * @param url 请求的url地址
     * @param path 文件的本地路径
     * @return
     */
    public static String postFile(String url,String path){
        List<String> list = new ArrayList<>();
        list.add(path);
        return postFile(url, null,list);
    }

    /**
     * 使用okhttp3，上传多个文件，多个参数。
     * @param url 请求的url
     * @param param map，请求参数的key，value
     * @param paths 文件集合，参数问file。
     * @return 请求结果，错误或者异常返回null
     */
    public static String postFile(String url, Map<String,String> param, List<String> paths) {
        String result = null;
        MultipartBody.Builder multipartBodyBuilder  = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //加入参数
        if(param != null){
            for (String key :param.keySet()){
                multipartBodyBuilder .addFormDataPart(key,param.get(key));
            }
        }
        //加入文件
        for(String path :paths) {
            File file = new File(path);
            if(file.exists()) {
                RequestBody body = RequestBody.create(MediaType.parse("image/jpg"), file);
                multipartBodyBuilder.addFormDataPart("file", file.getName(), body);//和后台约定,以file作为接收多张图片的key
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
