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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpStatus;
import com.melvinhou.cameralibrary.FcCameraActivity;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActivityWebBinding;
import com.melvinhou.kami.mvvm.BindActivity;
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
 * = 分 类 说 明：基本web浏览器
 * ============================================================
 */
public class WebActivity extends BindActivity<ActivityWebBinding, WebViewModel> {
    @Override
    protected ActivityWebBinding openViewBinding() {
        return ActivityWebBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<WebViewModel> openModelClazz() {
        return WebViewModel.class;
    }

    private static final String TAG = WebActivity.class.getName();
    protected String mUrl, mTitle;

    @Override
    protected void initView() {
        //获取转递的参数
        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");
        mBinding.barRoot.title.setText(mTitle);
        showProcess(null);
    }

    @Override
    protected void initData() {
        //辅助WebView设置处理关于页面跳转，页面请求等操作（简单处理html
        mBinding.web.setWebViewClient(mWebViewClient);
        //webview 的配置
        WebUtils.initConfig(mBinding.web);
        //开始加载
        mBinding.web.loadUrl(mUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //激活WebView为活跃状态，能正常执行网页的响应
        mBinding.web.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //当页面被失去焦点被切换到后台不可见状态，需要执行onPause
        //通过onPause动作通知内核暂停所有的动作，比如DOM的解析、plugin的执行、JavaScript执行。
        mBinding.web.onPause();
    }

    @Override
    protected void onDestroy() {
        WebUtils.destroy(mBinding.web);//释放 webview
        super.onDestroy();
    }


    @Override
    protected void onBackward(int type) {
        if (type == 1 && mBinding.web.canGoBack()) {
            mBinding.web.goBack();
        } else {
            backward();
        }
    }


    /**
     * 处理各种通知 & 请求事件
     */
    protected WebViewClient mWebViewClient = new WebViewClient() {
        //页面加载开始
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showProcess(null);
            super.onPageStarted(view, url, favicon);
        }

        //页面加载结束
        @Override
        public void onPageFinished(WebView view, String url) {
            hideProcess();
            super.onPageFinished(view, url);
        }

        //加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        //页面的服务器出现错误
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (error.getErrorCode() == HttpStatus.SC_NOT_FOUND) {
//                    view.loadUrl("file:///android_assets/error_handle.html");
            }
        }

        //处理https请求
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError sslError) {
//                super.onReceivedSslError(view, handler, sslError);
            handler.proceed();    //表示等待证书响应
            // handler.cancel();      //表示挂起连接，为默认方式
            // handler.handleMessage(null);    //可做其他处理
        }

        //打开网页时不调用系统浏览器，而是在本WebView中显示
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };

}
