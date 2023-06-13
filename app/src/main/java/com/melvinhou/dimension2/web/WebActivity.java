package com.melvinhou.dimension2.web;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActWebBD;
import com.melvinhou.kami.view.dialog.DialogCheckBuilder;
import com.melvinhou.kami.mvvm.DataBindingActivity;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.PermissionUtil;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/31 10:32
 * <p>
 * = 分 类 说 明：web浏览器
 * ============================================================
 */
public class WebActivity extends DataBindingActivity<ActWebBD> {

    private String mUrl, mTitle;
    //判断是否是录像
    private boolean videoFlag = false;
    //回调h5
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    //新建图片地址
    private Uri imageUri;
    //视频全屏
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    //请求回调
    private PermissionUtil.PermissionGrant permissionGrant;


    private final static int PHOTO_REQUEST = 100;
    private final static int CAMERA_REQUEST = 110;
    private final static int VIDEO_REQUEST = 120;


    private final static String OFFICE_GOOGLE = "https://docs.google.com/viewer?url=";
    private final static String OFFICE_MICROSOFT = "https://view.officeapps.live.com/op/view.aspx?src=";

    @Override
    protected void initView() {
        //获取转递的参数
        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");

        getViewDataBinding().setTitle(mTitle);

        //文档判断
//        if (StringCompareUtils.isWordUrl(mUrl)){
//            mUrl =OFFICE_MICROSOFT+mUrl;
//        }else if (StringCompareUtils.isExcelUrl(mUrl)){
//            mUrl =OFFICE_MICROSOFT+mUrl;
//        }
    }

    @Override
    protected void initListener() {
        //辅助WebView设置处理关于页面跳转，页面请求等操作（简单处理html
        getViewDataBinding().web.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                showLoadingView();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                hideLoadingView();
//                view.loadUrl("javascript:window.java_obj.showDescription("
//                        + "document.querySelectorAll('title')[0].getAttribute('title-type')"
//                        + ");");
                //获取解析<title title-type="1">商品分类</title>
                view.evaluateJavascript(
                        "javascript:"
                                + "document.querySelectorAll('title')[0]"
                                + ".getAttribute('title-type');",
                        value -> {
                            //js 返回的结果
                            if (!StringUtils.nonEmpty(value)) return;
                            int code = Integer.valueOf(value.replace("\"", ""), -1);
                            Log.e("title-type", "code=" + code);

                            switch (code) {
                                case 1://购物车
                                    break;
                                case 2://分享
                                    break;
                            }

                        });

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler sslErrorHandler, SslError sslError) {
                super.onReceivedSslError(view, sslErrorHandler, sslError);
//                sslErrorHandler.proceed();  // 接受所有网站的证书
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(this.getClass().getName(), url);
                if (TextUtils.isEmpty(url)) return true;
                videoFlag = url.contains("vedio");

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    return false;
                }
                if (url.trim().startsWith("tel")) {//特殊情况tel，调用系统的拨号软件拨号【<a href="tel:1111111111">1111111111</a>】
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else {
                    //尝试要拦截的视频通讯url格式(808端口)：【http://xxxx:808/?roomName】
                    String port = url.substring(url.lastIndexOf(":") + 1, url.lastIndexOf("/"));
                    if (port.equals("808")) {//特殊情况【若打开的链接是视频通讯地址格式则调用系统浏览器打开】
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                }
                return true;
            }
        });

        //辅助WebView处理复杂页面（JS
        getViewDataBinding().web.setWebChromeClient(new WebChromeClient() {

            /**
             * 当网页调用alert()来弹出alert弹出框前回调，用以拦截alert()函数
             */
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            /**
             * 当网页调用confirm()来弹出confirm弹出框前回调，用以拦截confirm()函数
             */
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            /**
             * 当网页调用prompt()来弹出prompt弹出框前回调，用以拦截prompt()函数
             */
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            /**
             * 打印 console 信息
             */
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }

            /**
             * 通知程序当前页面加载进度
             */
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100)
                    hideLoadingView();
                super.onProgressChanged(view, newProgress);
            }

            /**
             * 通知页面标题变化
             */
            @Override
            public void onReceivedTitle(WebView view, String webTitle) {
                super.onReceivedTitle(view, webTitle);
                //没有设置标题时初始化标题
                if (TextUtils.isEmpty(mTitle) && !webTitle.contains("html") && !webTitle.contains("http"))
                    getViewDataBinding().setTitle(webTitle);
                //测试js调用android
                view.loadUrl("javascript:window.Android.test(document.title,document.title);");
            }

            /**
             * 通知当前页面网站新图标
             */
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            /**
             * 通知主程序当前页面将要显示指定方向的View，该方法用来全屏播放视频。
             */
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomView = view;
                ((ViewGroup) getViewDataBinding().getRoot()).addView(mCustomView);
                mCustomViewCallback = callback;
                getViewDataBinding().web.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            }

            /**
             * 与onShowCustomView对应，通知主程序当前页面将要关闭Custom View
             */
            @Override
            public void onHideCustomView() {
                getViewDataBinding().web.setVisibility(View.VISIBLE);
                if (mCustomView == null) {
                    return;
                }
                mCustomView.setVisibility(View.GONE);
                ((ViewGroup) getViewDataBinding().getRoot()).removeView(mCustomView);
                mCustomViewCallback.onCustomViewHidden();
                mCustomView = null;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                super.onHideCustomView();
            }

            /**
             * 请求主程序创建一个新的Window，
             * 如果主程序接收请求，返回true并创建一个新的WebView来装载Window，
             * 然后添加到View中，发送带有创建的WebView作为参数的resultMsg的给Target。
             * 如果主程序拒绝接收请求，则方法返回false。默认不做任何处理，返回false
             */
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }

            /**
             *当停止播放，Video显示为一张图片。默认图片可以通过HTML的Video的poster属性标签来指定。如果poster属性不存在，则使用默认的poster。该方法允许ChromeClient提供默认图片。
             */
            @Nullable
            @Override
            public Bitmap getDefaultVideoPoster() {
                return super.getDefaultVideoPoster();
            }

            /**
             * 当用户重放视频，在渲染第一帧前需要花费时间去缓冲足够的数据。
             * 在缓冲期间，ChromeClient可以提供一个显示的View。如：可以显示一个加载动画。
             */
            @Nullable
            @Override
            public View getVideoLoadingProgressView() {
                return super.getVideoLoadingProgressView();
            }

            /**
             * 显示当前WebView，为当前WebView获取焦点。
             */
            @Override
            public void onRequestFocus(WebView view) {
                super.onRequestFocus(view);
            }

            /**
             * 通知主程序关闭WebView，并从View中移除，WebCore停止任何的进行中的加载和JS功能。
             */
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
            }

            /**
             * 通知客户端显示文件选择器。用来处理file类型的HTML标签，响应用户点击选择文件的按钮操作。
             * 调用filePathCallback.onReceiveValue(null)并返回true取消请求操作。
             * FileChooserParams参数的枚举列表：
             * MODE_OPEN 打开
             * MODE_OPEN_MULTIPLE 选中多个文件打开
             * MODE_OPEN_FOLDER 打开文件夹（暂不支持）
             * MODE_SAVE 保存
             */
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//                fileChooserParams中的方法看https://blog.csdn.net/xialong_927/article/details/80799412最后
                Log.e("WebAct", "打开相册");
                mUploadCallbackAboveL = filePathCallback;
                if (videoFlag) {
                    getVideo();
                } else {
                    getPhoto();
                }
                return true;
            }


            /**
             * 通知主程序 执行的Js操作超时。客户端决定是否中断JavaScript继续执行。
             * 如果客户端返回true，JavaScript中断执行。如果客户端返回false，则执行继续。
             * 注意：如果继续执行，重置JavaScript超时计时器。如果Js下一次检查点仍没有结束，则再次提示。
             */
            @Override
            public boolean onJsTimeout() {
                return super.onJsTimeout();
            }

            /**
             *通知主程序web内容尝试申请指定资源的权限（权限没有授权或已拒绝），
             * 主程序必须调用PermissionRequest#grant(String[])或PermissionRequest#deny()。
             * 如果没有覆写该方法，默认拒绝。
             */
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                super.onPermissionRequest(request);
            }

            /**
             * 通知主程序相关权限被取消。任何相关UI都应该隐藏掉。
             */
            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                super.onPermissionRequestCanceled(request);
            }

            /**
             * 获取访问历史Item，用于链接颜色。
             */
            @Override
            public void getVisitedHistory(ValueCallback<String[]> callback) {
                super.getVisitedHistory(callback);
            }
        });
    }

    @Override
    protected void initData() {
        getViewDataBinding().web.addJavascriptInterface(new Js2AndroidConnector(), "Android");
        //webview 的配置
        initWebViewConfig();
    }

    @Override
    protected void onLoading() {
//        super.onLoading();
        //开始加载
        getViewDataBinding().web.loadUrl(mUrl);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_web;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //调用js刷新页面
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (StringUtils.nonEmpty(mUrl) && mUrl.contains("Idle/IdleList")) return;
            getViewDataBinding().web.evaluateJavascript("RefreshWeb()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //js 返回的结果

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        getViewDataBinding().web.loadUrl("clearLocalstrage()");
        //释放 webview
        destroy(getViewDataBinding().web);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//        if (resultCode == Activity.RESULT_OK) {
        Uri[] results = null;
        if (requestCode == PHOTO_REQUEST) {
            if (null == mUploadCallbackAboveL) {
                return;
            }
            String dataString = intent.getDataString();
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                results = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    results[i] = item.getUri();
                }
            }
            if (dataString != null)
                results = new Uri[]{Uri.parse(dataString)};
        } else if (requestCode == CAMERA_REQUEST) {
            //内容提供者uri转外部可用uri，camera_photos为xml中定义的字段
//            imageUri.getPath().replaceAll(
//                    "content://"+getPackageName()+".provider/"+"camera_photos",
//                    Environment.getExternalStorageDirectory().getPath());
//            Uri imgUri = Uri.fromFile(FileUtils.getFileByUri(imageUri));
//            Log.e("相机返回uri","相机返回uri=" +imageUri+"\t转成html可用的=" +imgUri);
            results = new Uri[]{imageUri};
//            try {
//                Bitmap bitmap = BitmapFactory.decodeStream(
//                        FCUtils.getContext().getContentResolver().openInputStream(imageUri));
//                getViewDataBinding().barBg.setBackground(new BitmapDrawable(bitmap));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        }
        //回传给h5
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
//        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 初始化WebView配置
     */
    @SuppressLint("NewApi")
    private void initWebViewConfig() {


        // 设置WebView初始化尺寸，参数为百分比
//        webview.setInitialScale(100);
//        //设置WebView可触摸放大缩小
//        webview.getSettings().setSupportZoom(true);
//        webview.getSettings().setBuiltInZoomControls(true);
//        //WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
//        webview.getSettings().setUseWideViewPort(true);
//     // 设置页面缓存
//        webSettings.setAppCachePath(cacheDirPath);
//        webSettings.setAppCacheEnabled(true);
//        webSettings.setDomStorageEnabled(true);
//        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 获取WebSettings对象
        WebSettings webSettings = getViewDataBinding().web.getSettings();
        //自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        webSettings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
//        webSettings.setAllowFileAccessFromFileURLs(true);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
//        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);//主要是这句
        webSettings.setJavaScriptEnabled(true);//启用js
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        //（默认）根据cache-control决定是否从网络上取数据。
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }


    /**
     * 销毁资源
     *
     * @param webview
     */
    private void destroy(WebView webview) {
        webview.stopLoading(); //停止加载
        webview.setWebViewClient(null);
        webview.setWebChromeClient(null);
        webview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webview.removeAllViews(); //移除webview上子view
        webview.clearCache(true); //清除缓存
        webview.clearHistory(); //清除历史
        webview.destroy(); //销毁webview自身
        //Process.killProcess(Process.myPid()); //杀死WebView所在的进程
    }


////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 拍照
     */
    private void getPhoto() {

//        //创建ChooserIntent
//        Intent intent = new Intent(Intent.ACTION_CHOOSER);
//        //将相机Intent以数组形式放入Intent.EXTRA_INITIAL_INTENTS
////        intent.putExtra(Intent.EXTRA_INTENT, intentCamera);
//        //创建相册Intent
//        Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
//        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        //将相册Intent放入Intent.EXTRA_INTENT
//        intent.putExtra(Intent.EXTRA_INTENT, albumIntent);
//        startActivityForResult(Intent.createChooser(intent, "Choose"), PHOTO_REQUEST);
        showCheckView("选择需要上传的图片+", "", "相册", "相机",data->{
            if (data){
                openPhoto();
            }else {
                //权限获取
                PermissionUtil.PermissionGrant permissionGrant = new PermissionUtil.PermissionGrant() {
                    @Override
                    public void onPermissionGranted() {
                        takePhoto();
                    }

                    @Override
                    public void onPermissionCancel() {
                        String appName = ResourcesUtils.getString(R.string.app_name);
                        //必要权限判断
                        showCheckView("权限获取失败",
                                "该功能必须开启相机，请进入[设置-应用-" + appName + "-权限管理]进行授权",
                                "退出" + appName, null,data->{

                                });
                    }
                };
                setPermissionGrant(permissionGrant);
                PermissionUtil.requestPermission(WebActivity.this,
                        PermissionUtil.CODE_CAMERA, permissionGrant);
            }
        });
//        Intent albumIntent = new Intent(Intent.ACTION_PICK);
//        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, AfterSaleFillModule.IMAGE_UNSPECIFIED);
//        startActivityForResult(albumIntent, PHOTO_REQUEST);
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();
        if (permissionGrant != null)
            permissionGrant.onPermissionGranted();
    }

    @Override
    protected void onPermissionCancel() {
        super.onPermissionCancel();
        if (permissionGrant != null)
            permissionGrant.onPermissionCancel();
    }

    /**
     * 权限申请成功的回调
     *
     * @param permissionGrant
     */
    public void setPermissionGrant(PermissionUtil.PermissionGrant permissionGrant) {
        this.permissionGrant = permissionGrant;
    }

    /**
     * 录像
     */
    private void getVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //限制时长
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        //开启摄像机
        startActivityForResult(intent, VIDEO_REQUEST);
    }

    private void openPhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Choose"), PHOTO_REQUEST);
    }

    private void takePhoto() {
        File fileUri = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                DateUtils.getCurrentTime() + ".jpg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(FcUtils.getContext(),
                    getPackageName() + ".provider", fileUri);
        } else {
            imageUri = Uri.fromFile(fileUri);
        }
        //创建相机Intent
//        Intent intentCamera = new Intent();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            //添加这一句表示对目标应用临时授权该Uri所代表的文件
//            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        }
//        intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//        //将拍照结果保存至photo_file的Uri中，不保留在相册中
//        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(intentCamera, CAMERA_REQUEST);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //添加权限
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }


    @Override
    public void backward() {
        if (TextUtils.isEmpty(mUrl) || mUrl.equals(getViewDataBinding().web.getUrl()))
            super.backward();
        else
            getViewDataBinding().web.goBack();
    }
}
