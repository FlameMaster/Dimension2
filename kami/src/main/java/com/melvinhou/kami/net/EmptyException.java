package com.melvinhou.kami.net;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/11 20:05
 * <p>
 * = 分 类 说 明：异常
 * ================================================
 */

public class EmptyException extends Exception {

    private int code;

    public EmptyException(@EmptyState int code) {
        super();
        this.code = code;
    }


    @EmptyState
    public int getCode() {
        return code;
    }

    public void setCode(@EmptyState int code) {
        this.code = code;
    }
}
