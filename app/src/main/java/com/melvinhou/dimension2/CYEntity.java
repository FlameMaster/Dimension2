package com.melvinhou.dimension2;


import com.melvinhou.kami.net.BaseEntity;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/14 17:01
 * <p>
 * = 分 类 说 明：网络json的基类（虽然用的本地，但还是按照规范走
 * ============================================================
 */
public class CYEntity<D> implements BaseEntity<D> {

    @Override
    public boolean isSuccess() {
        if (getCode() == CODE_NORMAL)
            return true;
        return false;
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

    /*访问成功*/
    public static final int CODE_NORMAL = 200;

    private D data;
    private int code;
    private long date;
    private String message;


    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
