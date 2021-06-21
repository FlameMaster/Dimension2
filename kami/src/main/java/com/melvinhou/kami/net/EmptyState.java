package com.melvinhou.kami.net;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.melvinhou.kami.net.EmptyState.EMPTY;
import static com.melvinhou.kami.net.EmptyState.NET_ERROR;
import static com.melvinhou.kami.net.EmptyState.NORMAL;
import static com.melvinhou.kami.net.EmptyState.NOT_AVAILABLE;
import static com.melvinhou.kami.net.EmptyState.NOT_MORE_DATA;
import static com.melvinhou.kami.net.EmptyState.PROGRESS;
import static com.melvinhou.kami.net.EmptyState.USER_DEFINED;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/11 19:59
 * <p>
 * = 分 类 说 明：状态
 * ================================================
 */

@IntDef({NORMAL, PROGRESS, EMPTY, NOT_MORE_DATA, NET_ERROR, NOT_AVAILABLE,USER_DEFINED})
@Retention(RetentionPolicy.SOURCE)
public @interface EmptyState {
    /*正常*/
    int NORMAL = 1;
    /*显示进度条*/
    int PROGRESS = 0;

    /*列表数据为空*/
    int EMPTY = -1;
    /*没有更多数据*/
    int NOT_MORE_DATA = -2;
    /*网络未连接*/
    int NET_ERROR = 404;
    /*服务器不可用*/
    int NOT_AVAILABLE = 500;
    /*用户自定义*/
    int USER_DEFINED = 233;
}
