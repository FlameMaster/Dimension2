package com.melvinhou.kami.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/5/24 15:21
 * <p>
 * = 分 类 说 明：IO操作相关的工具类
 * ================================================
 */

public class IOUtils {


    /* 应用公共文件夹*/
    public static final String ROOT_NAME = Environment.getExternalStorageDirectory().getPath() ;
    /*照片*/
    public static final String TYPE_PATH_IMAGE = "image";
    /*录像*/
    public static final String TYPE_PATH_VIDEO = "vidoe";
    /*缓存*/
    public static final String TYPE_PATH_CACHE = "cache";

    /* jpg*/
    public static final String MEDIA_TYPE_JPEG = ".jpg";
    /* png*/
    public static final String MEDIA_TYPE_PNG = ".png";
    /*录像*/
    public static final String MEDIA_TYPE_VIDEO = ".mp4";
    /*app安装包*/
    public static final String MEDIA_TYPE_APK = ".apk";


    /**
     * 关闭流
     *
     * @param c 需要关闭的流
     */
    public static void close(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }


    public static  <T> ObservableTransformer<T, T> setThread() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


    /**
     * 是否能成功连接
     * @param path
     * @return
     */
    public static boolean isHttpHas(String path) {
        boolean isHas=false;
        try {
            URL url = new URL(path);
            HttpURLConnection urlcon2 = (HttpURLConnection) url.openConnection();
            Long TotalSize = Long.parseLong(urlcon2.getHeaderField("Content-Length"));
            if (TotalSize > 0)
                isHas=true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }finally {
            System.out.println("存在:"+isHas);
            return isHas;
        }
    }





    /**
     * 获取本地ip地址
     *
     * @return
     */
    @SuppressLint({"MissingPermission","WifiManagerLeak"})
    public static String getIPAddress() {
        NetworkInfo info = ((ConnectivityManager) FcUtils.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) FcUtils.getContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
