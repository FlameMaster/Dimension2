package com.melvinhou.game.poker;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/22 22:41
 * <p>
 * = 分 类 说 明：一轮牌的信息
 * ================================================
 */
public class PokerPlayInfo {


    private PokerRule rule;
    //连牌大小用连牌最小的表示
    private int size;
    //连牌专用
    private int sequenceCount;

    PokerPlayInfo(PokerRule rule, int size, int count) {
        setRule(rule);
        setSize(size);
        setSequenceCount(count);
    }

    public PokerRule getRule() {
        return rule;
    }

    public void setRule(PokerRule rule) {
        this.rule = rule;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSequenceCount() {
        return sequenceCount;
    }

    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
    }
}
