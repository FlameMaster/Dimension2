package com.melvinhou.kami.bean;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/9 20:12
 * <p>
 * = 分 类 说 明：用于适应BaseRecyclerAdapter2的嵌套条目
 * ================================================
 */
public class InsertItemEntity {

    //绝对位置
    private int position ;
    //是否填充满当前行（列
    private boolean isFull ;
    //数据
    private Object data;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setIsFull(boolean isFull) {
        this.isFull = isFull;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
