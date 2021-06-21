package com.melvinhou.dimension2.media;

import java.util.List;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/8 23:56
 * <p>
 * = 分 类 说 明：媒体实体类
 * ================================================
 */
public class MediaItemEntity {

    private int type;
    private String title;
    private String subTitle;
    private String coverUrl;
    private List<MediaTabEntity> tabs;

    public static class MediaTabEntity {
        private int type;
        private String title;
        private String iconUrl;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }


    public int getType() {
        return type;
    }

    public MediaItemEntity setType(int type) {
        this.type = type;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MediaItemEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public MediaItemEntity setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public MediaItemEntity setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
        return this;
    }

    public List<MediaTabEntity> getTabs() {
        return tabs;
    }

    public void setTabs(List<MediaTabEntity> tabs) {
        this.tabs = tabs;
    }
}
