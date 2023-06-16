package com.melvinhou.dimension2.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.melvinhou.dimension2.R;
import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.knight.KUITools;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.annotation.Nullable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/13 0013 16:05
 * <p>
 * = 分 类 说 明：功能齐全的浏览器
 * ================================================
 */
public class WebBrowserActivity extends WebActivity {

    private static final String TAG = WebBrowserActivity.class.getName();
//
    @Override
    protected void onLoading() {
        // 注意调用的JS方法名要对应上
        // 调用javascript的callJS()方法
        mBinding.web.loadUrl("javascript:callJS()");
        mBinding.web.evaluateJavascript(
                "javascript:callJS()",
                value -> {
                    //js 返回的结果

                });
    }

    @Override
    protected void initData() {
        super.initData();
        mBinding.web.setWebChromeClient(mWebChromeClient);
        //js调用安卓
        mBinding.web.addJavascriptInterface(new Js2AndroidConnector(), "Android");
    }

    @Override
    protected void onDestroy() {
        mBinding.flCustomContainer.removeAllViews();
        super.onDestroy();
    }

    /**
     * 辅助 WebView 处理 Javascript 的对话框,网站图标,网站标题等等
     */
    protected WebChromeClient mWebChromeClient = new WebChromeClient() {

        /**
         * 打印 console 信息
         */
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            WebUtils.log(TAG, consoleMessage);
            return true;
        }

        /**
         * 显示当前WebView，为当前WebView获取焦点。
         */
        @Override
        public void onRequestFocus(WebView view) {
            super.onRequestFocus(view);
        }

        /**
         * 通知程序当前页面加载进度
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100)
                hideProcess();
        }

        /**
         * 通知页面标题变化
         */
        @Override
        public void onReceivedTitle(WebView view, String webTitle) {
            super.onReceivedTitle(view, webTitle);
            if (StringUtils.isEmpty(mTitle))
                mBinding.barRoot.title.setText(webTitle);
        }

        /**
         * 通知当前页面网站新图标
         */
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }

        /**
         * 获取访问历史Item，用于链接颜色。
         */
        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            super.getVisitedHistory(callback);
        }


////////////////////////////////////////////////////////////////////////////////////////////////////


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
         * js的Alert警告弹窗
         */
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            showCheckView(null, message, "确定", null, data -> {
                if (data) result.confirm();
                else result.cancel();
            });
            return true;
        }

        /**
         * js的Confirm确认弹窗
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            showCheckView(null, message, "确定", "取消", data -> {
                if (data) result.confirm();
                else result.cancel();
            });
            return true;
        }

        /**
         * js的Prompt输入弹窗
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            KUITools.INSTANCE.showInputDialog01(WebBrowserActivity.this,
                    message, defaultValue, InputType.TYPE_CLASS_TEXT, str -> {
                        if (StringUtils.isEmpty(str))
                            result.cancel();
                        else result.confirm(str);
                        return null;
                    });
            return true;
        }


////////////////////////////////////////////////////////////////////////////////////////////////////


        /**
         * 通知主程序 页面进入全屏模式,返回容器
         */
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            mBinding.flCustomContainer.addView(view);
//            callback.onCustomViewHidden();
        }

        /**
         * 通知主程序 当前页面已经关闭全屏模式，隐藏CustomView
         */
        @Override
        public void onHideCustomView() {
            mBinding.flCustomContainer.removeAllViews();
        }

        /**
         * 请求主程序创建一个新的Window，需要MultipleWindows=true
         * 如果主程序接收请求，返回true并创建一个新的WebView来装载Window，
         * 然后添加到View中，发送带有创建的WebView作为参数的resultMsg的给Target。
         * 如果主程序拒绝接收请求，则方法返回false。默认不做任何处理，返回false
         */
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        /**
         * 通知主程序关闭WebView，并从View中移除，WebCore停止任何的进行中的加载和JS功能。
         */
        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
        }


////////////////////////////////////////////////////////////////////////////////////////////////////


        /**
         * 视频的占位图
         */
        @Nullable
        @Override
        public Bitmap getDefaultVideoPoster() {
            return ImageUtils.decodeBitmapResources(R.mipmap.fc, 200, 200);
        }

        /**
         * 视频加载时的进度条
         */
        @Nullable
        @Override
        public View getVideoLoadingProgressView() {
            return View.inflate(getBaseContext(), R.layout.load_dialog, null);
        }

        /**
         * 通知客户端显示文件选择器。
         * 用来处理file类型的HTML标签，响应用户点击选择文件的按钮操作。
         * 调用filePathCallback.onReceiveValue(null)并返回true取消请求操作。
         * FileChooserParams参数的枚举列表：
         * MODE_OPEN 打开
         * MODE_OPEN_MULTIPLE 选中多个文件打开
         * MODE_OPEN_FOLDER 打开文件夹（暂不支持）
         * MODE_SAVE 保存
         */
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            int mode = fileChooserParams.getMode();
            String[] types = fileChooserParams.getAcceptTypes();
            if (mode == FileChooserParams.MODE_OPEN) {//单文件
                openFile(types, result -> {
                    if (result != null) filePathCallback.onReceiveValue(new Uri[]{result});
                    else filePathCallback.onReceiveValue(null);
                });
            } else if (mode == FileChooserParams.MODE_OPEN_MULTIPLE) {//多文件
                openFiles(types, result -> {
                    if (result != null) {
                        Uri[] uris = new Uri[result.size()];
                        result.toArray(uris);
                        filePathCallback.onReceiveValue(uris);
                    } else filePathCallback.onReceiveValue(null);
                });
            } else if (mode == FileChooserParams.MODE_SAVE) {//保存
            }
            return true;
        }

        /**
         * Web页面请求Android权限，默认拒绝。
         */
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(mBinding.web.getTitle()).append("需要以下权限:");
            for (String resource : request.getResources()) {
                buffer.append("\n[").append(WebUtils.getPermissionName(resource)).append("]");
            }
            showCheckView("权限申请", buffer, "授权", "拒绝", data -> {
                if (data) request.grant(request.getResources());//同意
                else request.deny();//拒绝
            });
        }

        /**
         * Web页面请求Android权限被取消
         */
        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            onBackward(1);//返回
        }

    };

}
