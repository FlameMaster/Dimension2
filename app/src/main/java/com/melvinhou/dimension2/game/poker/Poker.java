package com.melvinhou.dimension2.game.poker;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/1 1:13
 * <p>
 * = 分 类 说 明：扑克牌对象
 * ================================================
 */
class Poker {
    public static final int BIG_JOKER = 11;//大王
    public static final int LITTLE_JOKER = 12;//小王
    public static final int SUIT_HEART = 1;//红桃
    public static final int SUIT_SPADE = 2;//黑桃
    public static final int SUIT_DIAMOND = 3;//方块
    public static final int SUIT_CLUB = 4;//梅花


    //大小
    private int value;
    private String showValue;
    //花色
    private int suit;
    private String showSuit;

    public Poker(int value, int suit) {
        setValue(value);
        setSuit(suit);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        if (value == 12) {
            this.showValue = "A";
        } else if (value == 15) {
            this.showValue = "2";
        } else if (value < 9) {
            this.showValue = String.valueOf(value + 2);
        } else if (value == 9) {
            this.showValue = "J";
        } else if (value == 10) {
            this.showValue = "Q";
        } else if (value == 11) {
            this.showValue = "K";
        } else {
            this.showValue = "J\nO\nK\nE\nR";
        }
    }

    public String getShowValue() {
        return showValue;
    }

    public void setShowValue(String showValue) {
        this.showValue = showValue;
    }

    public int getSuit() {
        return suit;
    }

    public void setSuit(int suit) {
        this.suit = suit;
        if (suit == SUIT_SPADE) {
//            this.showSuit = "黑桃";
            this.showSuit = "♠";
        } else if (suit == SUIT_HEART) {
//            this.showSuit = "红桃";
            this.showSuit = "♥";
        } else if (suit == SUIT_CLUB) {
//            this.showSuit = "梅花";
            this.showSuit = "♣";
        } else if (suit == SUIT_DIAMOND) {
//            this.showSuit = "方块";
            this.showSuit = "♦";
        }
    }

    public String getShowSuit() {
        if (showSuit == null) return "";
        return showSuit;
    }

    public void setShowSuit(String showSuit) {
        this.showSuit = showSuit;
    }
}
