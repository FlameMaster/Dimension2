package com.melvinhou.kami.net;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

import static com.melvinhou.kami.net.RequestState.EMPTY;
import static com.melvinhou.kami.net.RequestState.READY;
import static com.melvinhou.kami.net.RequestState.RUNNING;
import static com.melvinhou.kami.net.ResultState.CONVERT_ERROR;
import static com.melvinhou.kami.net.ResultState.FAILED;
import static com.melvinhou.kami.net.ResultState.NETWORK_ERROR;
import static com.melvinhou.kami.net.ResultState.RELOGIN;
import static com.melvinhou.kami.net.ResultState.SUCCESS;

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
 * = 分 类 说 明：网络加载状态
 * ================================================
 */

@IntDef({EMPTY, READY, RUNNING, SUCCESS, FAILED, RELOGIN, NETWORK_ERROR, CONVERT_ERROR})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestState {

    //初始
    int EMPTY = 0;
    //准备
    int READY = 1;
    //进行中
    int RUNNING = 10;

}
