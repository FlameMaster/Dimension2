package com.melvinhou.dimension2.media.video;

import java.util.List;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/17 1:24
 * <p>
 * = 分 类 说 明：哔哩哔哩直播视频流对象
 * ================================================
 */
public class BilibiliLIveEntity {

    //https://api.live.bilibili.com/room/v1/Room/playUrl?cid=房间号&platform=h5&otype=json&quality=4

    private List<String> accept_quality;
    private int current_qn;
    private int current_quality;
    private List<Durl> durl;
    private List<QualityDescription> quality_description;

    public static class Durl{
        private int length;
        private int order;
        private int p2p_type;
        private int stream_type;
        private String url;

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getP2p_type() {
            return p2p_type;
        }

        public void setP2p_type(int p2p_type) {
            this.p2p_type = p2p_type;
        }

        public int getStream_type() {
            return stream_type;
        }

        public void setStream_type(int stream_type) {
            this.stream_type = stream_type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class QualityDescription{
        private int qn;
        private String desc;

        public int getQn() {
            return qn;
        }

        public void setQn(int qn) {
            this.qn = qn;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public List<String> getAccept_quality() {
        return accept_quality;
    }

    public void setAccept_quality(List<String> accept_quality) {
        this.accept_quality = accept_quality;
    }

    public int getCurrent_qn() {
        return current_qn;
    }

    public void setCurrent_qn(int current_qn) {
        this.current_qn = current_qn;
    }

    public int getCurrent_quality() {
        return current_quality;
    }

    public void setCurrent_quality(int current_quality) {
        this.current_quality = current_quality;
    }

    public List<Durl> getDurl() {
        return durl;
    }

    public void setDurl(List<Durl> durl) {
        this.durl = durl;
    }

    public List<QualityDescription> getQuality_description() {
        return quality_description;
    }

    public void setQuality_description(List<QualityDescription> quality_description) {
        this.quality_description = quality_description;
    }
}
