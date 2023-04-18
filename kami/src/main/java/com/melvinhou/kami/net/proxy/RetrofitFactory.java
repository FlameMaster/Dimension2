package com.melvinhou.kami.net.proxy;

import android.util.Log;

import com.google.gson.Gson;
import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.tool.EncryptUtils;
import com.melvinhou.kami.util.AppUtils;
import com.melvinhou.kami.util.DateUtils;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.TrustManager;

import androidx.annotation.NonNull;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/3 15:03
 * <p>
 * = 分 类 说 明：Retrofit工厂
 * ================================================
 */

public class RetrofitFactory {
    public static final String TAG = "RetrofitFactory----";
    private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 10;//连接超时
    private static final long DEFAULT_READ_TIMEOUT_MILLIS = 2000;//读取超时
    private static final long DEFAULT_WRITE_TIMEOUT_MILLIS = 2000;//写入超时

    /**
     * Retrofit单例
     */
//    private static RetrofitFactory mRetrofitFactory;

    public static RetrofitFactory getInstence() {
        RetrofitFactory mRetrofitFactory;
        synchronized (RetrofitFactory.class) {
            mRetrofitFactory = new RetrofitFactory();
        }
        return mRetrofitFactory;
    }


    private Retrofit mRetrofit;
    private OkHttpClient mOkHttpClient;
    private String mServerApi;//api地址
    private boolean isEncrypt = false;//是否加密

    public <T> T create(Class<T> serviceClass, String apiUrl, boolean isEncrypt) {
        if (mRetrofit == null) {
            this.isEncrypt = isEncrypt;
            this.mServerApi = apiUrl;
            initClient();
            initRetrofit();
        }
        return mRetrofit.create(serviceClass);
    }

    /**
     * 初始化okhttp连接
     */
    private void initClient() {
        //证书
        FcX509TrustManager trustManager = new FcX509TrustManager();
        Tls12SocketFactory socketFactory = new Tls12SocketFactory(
                null, new TrustManager[]{trustManager}, new SecureRandom());
        //初始化
        mOkHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.SECONDS)
                .cache(createCacheFile())
                .addInterceptor(new RequestInterceptor())//拦截器-请求参数
                .sslSocketFactory(socketFactory, trustManager)
                .build();
    }

    /**
     * 初始化网络工具
     */
    private void initRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mServerApi)
                .addConverterFactory(GsonConverterFactory.create())//添加gson转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//添加rxjava转换器
                .client(mOkHttpClient)
                .build();
    }

    /**
     * @return 离线缓存
     */
    private Cache createCacheFile() {
        //离线缓存
        File cacheFile = FileUtils.getDiskCacheDir("okcache");
        //缓存大小为10M
        long cacheSize = 10 * 1024 * 1024;
        //创建缓存对象
        return new Cache(cacheFile, cacheSize);
    }

    class RequestInterceptor implements Interceptor {

        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            //请求头
            Request.Builder builder = request.newBuilder()
//                    .header("Content-Type", "application/json; charset=UTF-8")
//                    .header("Cookie", cookie)
//                    .addHeader("X-CHANNEL-ID", "android")
//                    .addHeader("X-CLIENT-VERSION", "2.2.0")
//                    .addHeader("X-MACHINE-ID", "asjfaljdfiayhfdnglajspfhbajnfiaj")
//                    .addHeader("X-PLATFORM", "0")
                    .header("Build_Number", String.valueOf(AppUtils.getVersion()))//构建版本号
                    .header("Version", AppUtils.getAppVersionName())//软件版本号
                    .header("App_Key", AppUtils.getMachineID())//设备key 什么设备
                    .header("Time_Stamp", String.valueOf(DateUtils.getCurrentTime()))//时间戳
//                    .header("Token", token)
//                    .addHeader("source", "Android")//客户端类型(iOS或Android)
//                    .addHeader("AdvertisingIdentifier", AppUtils.getMachineID())//设备key
//                    .addHeader("Distributor-Code", AppUtils.getChannelName("UMENG_CHANNEL"))//对应渠道编号
                    ;

            //加密
            if (isEncrypt) {
                HttpUrl.Builder urlBuilder = request.url().newBuilder();
                HttpUrl httpurl = urlBuilder.build();
                Set<String> queryParameterNames = httpurl.queryParameterNames();
                Map<String, String> queryMap = new HashMap<>();
                //所有参数
                for (String key : queryParameterNames) {
                    String value = httpurl.queryParameter(key);
                    queryMap.put(key, value);
                }
                for (String key : queryMap.keySet()) {
                    urlBuilder.removeAllEncodedQueryParameters(key);
                }
                String paramsData = new Gson().toJson(queryMap);
                Log.e(TAG, "加密前" + httpurl.encodedPath() + "," + paramsData);
                String data = EncryptUtils.encryptToBase64(paramsData);//加密
                //  data = URLEncoder.encode(data, "UTF-8");
                urlBuilder.addEncodedQueryParameter("data", data);
                builder.url(urlBuilder.build());
            }else {
                Log.e(TAG, "请求参数:" + request.url());
            }
            return chain.proceed(builder.build());
        }
    }


}
