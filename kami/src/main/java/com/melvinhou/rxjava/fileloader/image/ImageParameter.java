package com.melvinhou.rxjava.fileloader.image;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/11/30  18:04
 * <p>
 * = 分 类 说 明：网络图片参数
 * ============================================================
 */
public class ImageParameter {

    /*图片链接*/
    private String url;
    /*图片宽度*/
    private int width;
    /*图片高度*/
    private int height;
    /*图片最大的宽高*/
    private int max_width, max_height;

    public ImageParameter() {
        width = 0;
        height = 0;
        max_width = 0;
        max_height = 0;
    }

    public ImageParameter(String url) {
        this.url = url;
    }

    public ImageParameter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public ImageParameter(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public ImageParameter setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ImageParameter setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ImageParameter setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getMax_width() {
        return max_width;
    }

    public ImageParameter setMax_width(int max_width) {
        this.max_width = max_width;
        return this;
    }

    public int getMax_height() {
        return max_height;
    }

    public ImageParameter setMax_height(int max_height) {
        this.max_height = max_height;
        return this;
    }



    public String getCacheKey() {

        String cacheKey = getUrl()
                + "?x-oss-process=image/resize,m_fill,w_"
                + getWidth()
                + ",h_"
                + getHeight()
                + ",limit_0/auto-orient,0/quality,q_90";
        return cacheKey;
    }
}
