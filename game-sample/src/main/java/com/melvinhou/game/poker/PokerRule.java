package com.melvinhou.game.poker;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/1 21:20
 * <p>
 * = 分 类 说 明：出牌规则
 * ================================================
 */
public enum PokerRule {
    EXCEPT,//不属于规则
    SINGLE_CARD,//单牌
    PAIR,//对子
    TRIPLET,//三不带
    TRIPLET_WITH_AN_ATTACHED_CARD,//三带一
    TRIPLET_WITH_AN_ATTACHED_PAIR,//三代二
    QUADPLEX_SET,//四带二
    SEQUENCE,//单顺子
    SEQUENCE_OF_PAIRS,//双顺子
    SEQUENCE_OF_TRIPLETS,//三顺子
    SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_CARDS,//飞机单
    SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_PAIRS,//飞机双
    SEQUENCE_OF_QUADPLEX_SET, //四带二连牌
    BOMB,//炸弹
    ROCKET//火箭
}
