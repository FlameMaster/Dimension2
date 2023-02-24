package com.melvinhou.kami.net;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

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
 * = 分 类 说 明：网络状态
 * ================================================
 */

@IntDef({SUCCESS, FAILED,RELOGIN, NETWORK_ERROR, CONVERT_ERROR})
@Retention(RetentionPolicy.SOURCE)
public @interface ResultState {
    //解析错误
    int CONVERT_ERROR = 600;
    //网络错误
    int NETWORK_ERROR = 404;
    //加载成功
    int SUCCESS = 200;
    //加载失败
    int FAILED = 400    ;
    //需要重新登录
    int RELOGIN = 401;

}
