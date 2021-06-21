package com.melvinhou.dimension2.media.video;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/18 7:15
 * <p>
 * = 分 类 说 明：key-value
 * ================================================
 */
public class MapEntity {
    private String key;
    private String value;

    public MapEntity(){

    }
    public MapEntity(String key,String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public MapEntity setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public MapEntity setValue(String value) {
        this.value = value;
        return this;
    }
}
