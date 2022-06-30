package com.melvinhou.dimension2.web;

import android.util.Log;
import android.webkit.JavascriptInterface;

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


    @JavascriptInterface
    public void test(String tag, String ppt) {
        Log.e("JS->Android", "test-->tag=" + tag + "\rppt=" + ppt);
        List list = new ArrayList();
        for (int i = 0;i<list.size();i++){}
    }

}
