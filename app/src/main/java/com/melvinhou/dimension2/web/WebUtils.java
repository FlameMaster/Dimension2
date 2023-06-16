package com.melvinhou.dimension2.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/13 0013 13:40
 * <p>
 * = 分 类 说 明：网页相关工具类
 * ================================================
 */
public class WebUtils {

    //在线文档查看
    private final static String OFFICE_GOOGLE = "https://docs.google.com/viewer?url=";
    private final static String OFFICE_MICROSOFT = "https://view.officeapps.live.com/op/view.aspx?src=";
    private final static String OFFICE_ALIYUN = "http://office.qingshanboke.com/Default.aspx?url=";
    private final static String OFFICE_TENCENT = "https://vw.usdoc.cn/?src=";//https://vw.usdoc.cn/?m=5&src=//m5为样式模板

    /**
     * @param context
     * @param url     office的地址
     * @param type    类型1谷歌2微软3阿里云4腾讯
     */
    public static void toOfficeWeb(Context context, String url, int type) {
        Intent intent = new Intent(context, WebBrowserActivity.class);
        if (type == 1) {
            url = OFFICE_GOOGLE + url;
        } else if (type == 2) {
            url = OFFICE_MICROSOFT + url;
        } else if (type == 3) {
            url = OFFICE_ALIYUN + url;
        } else if (type == 4) {
            url = OFFICE_TENCENT + url;
        }
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    /**
     * web的权限名称
     *
     * @param permission
     * @return
     */
    public static String getPermissionName(String permission) {
        String name = "手机权限";
        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(permission)) {
            name = "音频录制";
        } else if (PermissionRequest.RESOURCE_MIDI_SYSEX.equals(permission)) {
            name = "MIDI通知";
        } else if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(permission)) {
            name = "多媒体";
        } else if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(permission)) {
            name = "相机";
        }
        return name;
    }


    /**
     * 初始化WebView配置
     */
    public static void initConfig(WebView webview) {
        // 获取WebSettings对象
        WebSettings webSettings = webview.getSettings();
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
//        webSettings.setSupportMultipleWindows(true); // 支持多窗口
        //js
        webSettings.setJavaScriptEnabled(true);//启用js
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        //自适应屏幕，两者合用
//        webview.setInitialScale(100);//设置WebView初始化尺寸，参数为百分比
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true);//缩放至屏幕的大小,跟setUseWideViewPort一起用
        //本地文件
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setAllowContentAccess(true);// 是否可访问Content Provider的资源，默认值 true
        webSettings.setAllowFileAccess(true);// 是否可访问本地文件，默认值 true
        //是否允许通过file url加载的Javascript读取本地文件，默认值 false
        webSettings.setAllowFileAccessFromFileURLs(true);
        //是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setLoadsImagesAutomatically(true);//支持自动加载图片
        //缩放操作
        webSettings.setSupportZoom(true);//支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //缓存
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        webSettings.setDatabaseEnabled(true);   //开启 database storage API 功能
//        webSettings.setAppCacheEnabled(true);//开启 Application Caches 功能
//        webSettings.setAppCachePath(cacheDirPath); //设置  Application Caches 缓存目录
//        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
    }


    /**
     * 销毁资源
     *
     * @param webview
     */
    public static void destroy(WebView webview) {
        webview.stopLoading(); //停止加载
        webview.setWebViewClient(null);
        webview.setWebChromeClient(null);
        webview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webview.removeAllViews(); //移除webview上子view
        webview.clearCache(true); //清除缓存
        webview.clearHistory(); //清除历史
        webview.clearFormData();//清除自动完成填充的表单数据
        webview.destroy(); //销毁webview自身
        //Process.killProcess(Process.myPid()); //杀死WebView所在的进程
    }

    /**
     * 打印web的log
     *
     * @param consoleMessage
     */
    public static void log(String tag, ConsoleMessage consoleMessage) {
        String msg = consoleMessage.message();
        ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
        if (level == ConsoleMessage.MessageLevel.ERROR) {
            Log.e(tag, msg);
        } else if (level == ConsoleMessage.MessageLevel.WARNING) {
            Log.w(tag, msg);
        } else if (level == ConsoleMessage.MessageLevel.LOG) {
            Log.v(tag, msg);
        } else if (level == ConsoleMessage.MessageLevel.TIP) {
            Log.i(tag, msg);
        } else if (level == ConsoleMessage.MessageLevel.DEBUG) {
            Log.d(tag, msg);
        }
    }
}
