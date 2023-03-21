package com.melvinhou.kami.bean;

import com.melvinhou.kami.net.BaseEntity;
import com.melvinhou.kami.net.ResultState;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/20 0020 13:48
 * <p>
 * = 分 类 说 明：实现类
 * ================================================
 */
public class FcEntity<D> implements BaseEntity<D> {

    private D data;
    private int code;
    private long date;
    private String message;


    @Override
    public D getData() {
        return data;
    }

    @Override
    public boolean isSuccess() {
        return code == ResultState.SUCCESS;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public long getTimer() {
        return date;
    }

}
