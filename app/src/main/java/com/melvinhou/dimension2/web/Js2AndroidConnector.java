package com.melvinhou.dimension2.web;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/31 10:47
 * <p>
 * = 分 类 说 明：
 * ============================================================
 */
public class Js2AndroidConnector {
    /*
     * 对于Android调用JS代码的方法有2种：
     * 通过WebView的loadUrl（）
     * 通过WebView的evaluateJavascript（）
     *
     * 对于JS调用Android代码的方法有3种：
     * 通过WebView的addJavascriptInterface（）进行对象映射
     * 通过 WebViewClient 的shouldOverrideUrlLoading ()方法回调拦截 url
     * 通过 WebChromeClient 的onJsAlert()、onJsConfirm()、onJsPrompt（）方法回调拦截JS对话框alert()、confirm()、prompt（） 消息
     */

    //JS代码调用一定要在 onPageFinished（） 回调之后才能调用，否则不会调用。

    @JavascriptInterface
    public void test(String tag, String ppt) {
        Log.e("JS->Android", "test-->tag=" + tag + "\rppt=" + ppt);
        List list = new ArrayList();
        for (int i = 0;i<list.size();i++){}
        FcUtils.showToast(tag);
    }

}
