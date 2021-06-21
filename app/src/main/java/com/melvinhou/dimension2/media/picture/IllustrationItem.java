package com.melvinhou.dimension2.media.picture;

import com.melvinhou.dimension2.User;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/15 16:46
 * <p>
 * = 分 类 说 明：插画列表显示条目
 * ============================================================
 */
public class IllustrationItem {

    /*本地assets*/
    public final static int SOURCE_ASSETS = 0;
    /*本地内存*/
    public final static int SOURCE_NATIVE = 2;
    /*本地内存卡*/
    public final static int SOURCE_NATIVE_SD = 3;
    /*网络*/
    public final static int SOURCE_NET = 5;


    private String url;
    private Integer width;
    private Integer height;
    private String title;
    private String explain;
    /*番剧数据来源*/
    private int dataSource;
    private String dataPath;
    private User owner;
    private String dateText;

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public int getDataSource() {
        return dataSource;
    }

    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        if (width==null)
            return 0;
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public int getHeight() {
        if (height==null)
            return 0;
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
