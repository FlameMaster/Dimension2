package com.melvinhou.dimension2;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/10 4:39
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class PairEntity<K, V> {

    private K key;
    private V value;


    public PairEntity() {

    }

    public PairEntity(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public PairEntity setKey(K key) {
        this.key = key;
        return this;
    }

    public V getValue() {
        return value;
    }

    public PairEntity setValue(V value) {
        this.value = value;
        return this;
    }
}
