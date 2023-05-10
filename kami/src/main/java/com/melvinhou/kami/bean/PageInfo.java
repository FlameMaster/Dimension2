package com.melvinhou.kami.bean;

import android.os.Bundle;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/8 0008 15:42
 * <p>
 * = 分 类 说 明：用于页面跳转
 * ================================================
 */
public class PageInfo {
    //页面id
    public int pageId;
    //页面参数
    public Bundle pageArgs;

    public PageInfo(int id) {
        this.pageId = id;
    }

    public PageInfo(int id, Bundle args) {
        this.pageId = id;
        this.pageArgs = args;
    }
}
