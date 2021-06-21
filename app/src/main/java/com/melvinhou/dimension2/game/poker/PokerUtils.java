package com.melvinhou.dimension2.game.poker;

import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/3 20:56
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class PokerUtils {

    /**
     * 获取对应规则的名称
     *
     * @param rule
     * @return
     */
    public static String getPokerRuleName(PokerRule rule) {
        switch (rule) {
            case EXCEPT://不属于规则
                return "不属于规则";
            case SINGLE_CARD:
                return "单牌";
            case PAIR:
                return "对子";
            case TRIPLET:
                return "三不带";
            case TRIPLET_WITH_AN_ATTACHED_CARD:
                return "三带一";
            case TRIPLET_WITH_AN_ATTACHED_PAIR:
                return "三代二";
            case QUADPLEX_SET:
                return "四带二";
            case SEQUENCE:
                return "单顺子";
            case SEQUENCE_OF_PAIRS:
                return "双顺子";
            case SEQUENCE_OF_TRIPLETS:
                return "三顺子";
            case SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_CARDS:
                return "飞机单";
            case SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_PAIRS:
                return "飞机双";
            case BOMB:
                return "炸弹";
            case ROCKET:
                return "火箭";
            case SEQUENCE_OF_QUADPLEX_SET:
                return "四带二连牌";
        }
        return "无规则";
    }


    /**
     * 按牌的大小排序
     *
     * @param list
     */
    public static void sort(List<Poker> list) {
        if (list != null)
            Collections.sort(list, new Comparator<Poker>() {
                /**
                 * 比较它的两个参数的顺序。
                 * 当第一个参数小于、等于或大于第二个参数时，返回一个负整数、零或正整数
                 * @param o1
                 * @param o2
                 * @return
                 */
                @Override
                public int compare(Poker o1, Poker o2) {
                    return o1.getValue() - o2.getValue();
                }
            });
    }

    /**
     * 格式化扑克数据
     *
     * @param list
     * @return
     */
    public static Map<Integer, Integer> formatPokerValue(List<Poker> list) {
        Map<Integer, Integer> map = new ArrayMap<>();
        StringBuffer buffer = new StringBuffer("sex\n");
        for (Poker poker : list) {
            int key = poker.getValue();
            int value = 1;
            if (map.containsKey(key)) value = map.get(key) + 1;
            map.put(key, value);
            buffer
                    .append("牌号：[")
                    .append(poker.getShowValue())
                    .append("]-花色：")
                    .append(poker.getShowSuit())
                    .append("\n");
        }
        Log.w("formatPokerValue", buffer.toString());
        return map;
    }

    /**
     * 扑克牌连牌判断
     *
     * @param array
     * @return
     */
    public static boolean isContinuous(int[] array) {
        if (array.length > 1) {
            //排序
            Arrays.sort(array);
            //连续性判断
            for (int i = 1; i < array.length; i++) {
                //2和王不能组成顺子
                if (array[i] > 12) return false;
                if (array[i] - array[i - 1] != 1)
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 获取当前牌列表的规则
     *
     * @param pokers
     * @return
     */
    public static PokerRule getPokerRule(List<Poker> pokers) {
        if (pokers == null || pokers.size() < 1) return PokerRule.EXCEPT;
        int size = pokers.size();

        if (size == 1) {//单牌
            return PokerRule.SINGLE_CARD;
        } else if (size == 2) {//双牌，火箭
            if (pokers.get(0).getValue() == pokers.get(1).getValue()) {
                return PokerRule.PAIR;//对子
            } else if (pokers.get(0).getSuit() > 10 && pokers.get(1).getSuit() > 10) {
                return PokerRule.ROCKET;//火箭
            }
        } else if (size == 3) {//3不带
            if (pokers.get(0).getValue() == pokers.get(1).getValue()
                    && pokers.get(0).getValue() == pokers.get(2).getValue())
                return PokerRule.TRIPLET;//三不带
        } else if (size == 4) {//三带一，炸弹
            Map<Integer, Integer> map = formatPokerValue(pokers);
            int count = map.get(pokers.get(0).getValue());
            if (map.size() == 1)
                return PokerRule.BOMB;//炸弹
            else if (map.size() == 2 && (count == 1 || count == 3))
                return PokerRule.TRIPLET_WITH_AN_ATTACHED_CARD;//三带一
        } else if (size == 5) {//单连5张，三带二
            Map<Integer, Integer> map = formatPokerValue(pokers);
            if (map.size() == 5) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return PokerRule.SEQUENCE;//单连牌
                }
            } else if (map.size() < 3) {
                int count = map.get(pokers.get(0).getValue());
                if (count == 2 || count == 3)
                    return PokerRule.TRIPLET_WITH_AN_ATTACHED_PAIR;//三带二
            }
        } else if (size == 6) {//四带二，三不带2连，对子3连，单牌6连
            Map<Integer, Integer> map = formatPokerValue(pokers);
            if (map.size() == 6) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return PokerRule.SEQUENCE;//单连牌
                }
            } else if (map.size() == 3) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return PokerRule.SEQUENCE_OF_PAIRS;//双连牌
                }
            } else if (map.size() == 2) {
                int count = map.get(pokers.get(0).getValue());
                if (count == 3) return PokerRule.SEQUENCE_OF_TRIPLETS;//三连牌
                else return PokerRule.QUADPLEX_SET;//四带二
            }

        } else {//剩下的都是连牌了
            Map<Integer, Integer> map = formatPokerValue(pokers);
            if (size == map.size()) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return PokerRule.SEQUENCE;//单连牌
                }
            } else {
                if (size % 5 == 0) {//三带二连牌判断
                    //3和2的数量判断
                    int hit2 = 0;
                    int hit3 = 0;
                    for (int key : map.keySet()) {
                        int count = map.get(key);
                        if (count == 2) hit2++;
                        if (count == 3) hit3++;
                    }
                    if (hit2 == hit3 && hit2 == size / 5) {
                        //建立数组
                        int[] array = new int[hit3];
                        int pos = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count == 3) {
                                array[pos++] = key;
                            }
                        }
                        //判断是否是连牌
                        if (isContinuous(array)) {
                            return PokerRule.SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_PAIRS;//三带二连牌
                        }
                    }
                }
                B:
                if (size % 3 == 0) {//三不带连牌判断
                    int[] array = new int[map.size()];
                    int pos = 0;
                    for (int key : map.keySet()) {
                        int count = map.get(key);
                        if (count != 3) break B;
                        array[pos++] = key;
                    }
                    //判断是否是连牌
                    if (isContinuous(array)) {
                        return PokerRule.SEQUENCE_OF_TRIPLETS;//三连牌
                    }
                }
                if (size % 2 == 0) {//对子连牌判断
                    if (size % 6 == 0) {//四带二连牌判断
                        int hit2 = 0;
                        int hit4 = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count == 2) hit2++;
                            if (count == 4) hit4++;
                        }
                        if (hit2 == hit4 && hit2 == size / 6) {
                            //建立数组
                            int[] array = new int[hit4];
                            int pos = 0;
                            for (int key : map.keySet()) {
                                int count = map.get(key);
                                if (count == 4) {
                                    array[pos++] = key;
                                }
                            }
                            //判断是否是连牌
                            if (isContinuous(array)) {
                                return PokerRule.SEQUENCE_OF_QUADPLEX_SET;//四带二连牌
                            }
                        }

                    }
                    if (size % 4 == 0) {//三带一连牌判断
                        int hit1 = 0;
                        int hit3 = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count == 1) hit1++;
                            if (count == 3) hit3++;
                        }
                        if (hit1 == hit3 && hit1 == size / 4) {
                            //建立数组
                            int[] array = new int[hit3];
                            int pos = 0;
                            for (int key : map.keySet()) {
                                int count = map.get(key);
                                if (count == 3) {
                                    array[pos++] = key;
                                }
                            }
                            //判断是否是连牌
                            if (isContinuous(array)) {
                                return PokerRule.SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_CARDS;//三带一连牌
                            }
                        }
                    }
                    //剩下判断对子连牌
                    E:
                    if (map.size() == size / 2) {
                        int[] array = new int[map.size()];
                        int pos = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count != 2)
                                break E;
                            array[pos++] = key;
                        }
                        //判断是否是连牌
                        if (isContinuous(array)) {
                            return PokerRule.SEQUENCE_OF_PAIRS;//双连牌
                        }
                    }
                }
            }
        }

        return PokerRule.EXCEPT;
    }

    /**
     * 获取当前牌列表的出牌信息
     *
     * @param pokers
     * @return
     */
    public static PokerPlayInfo getPokerPlayInfo(List<Poker> pokers) {
        if (pokers == null || pokers.size() < 1) return null;
        sort(pokers);
        int size = pokers.size();

        if (size == 1) {//单牌
            return new PokerPlayInfo(PokerRule.SINGLE_CARD, pokers.get(0).getValue(), 1);
        } else if (size == 2) {//双牌，火箭
            if (pokers.get(0).getValue() == pokers.get(1).getValue()) {
                return new PokerPlayInfo(PokerRule.PAIR, pokers.get(0).getValue(), 1);//对子
            } else if (pokers.get(0).getSuit() > 10 && pokers.get(1).getSuit() > 10) {
                return new PokerPlayInfo(PokerRule.ROCKET, 20, 1);//火箭
            }
        } else if (size == 3) {//3不带
            if (pokers.get(0).getValue() == pokers.get(1).getValue()
                    && pokers.get(0).getValue() == pokers.get(2).getValue())
                return new PokerPlayInfo(PokerRule.TRIPLET, pokers.get(0).getValue(), 1);//三不带
        } else if (size == 4) {//三带一，炸弹
            Map<Integer, Integer> map = formatPokerValue(pokers);
            int count = map.get(pokers.get(0).getValue());
            if (map.size() == 1)
                return new PokerPlayInfo(PokerRule.BOMB, pokers.get(0).getValue(), 1);//炸弹
            else if (map.size() == 2) {
                //三带一
                if (count == 3)
                    return new PokerPlayInfo(PokerRule.TRIPLET_WITH_AN_ATTACHED_CARD, pokers.get(0).getValue(), 1);
                else if (count == 1)
                    return new PokerPlayInfo(PokerRule.TRIPLET_WITH_AN_ATTACHED_CARD, pokers.get(1).getValue(), 1);
            }
        } else if (size == 5) {//单连5张，三带二
            Map<Integer, Integer> map = formatPokerValue(pokers);
            if (map.size() == 5) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return new PokerPlayInfo(PokerRule.SEQUENCE, pokers.get(0).getValue(), size);//单连牌
                }
            } else if (map.size() == 2) {
                int count = map.get(pokers.get(0).getValue());
                //三带二
                if (count == 3)
                    return new PokerPlayInfo(PokerRule.TRIPLET_WITH_AN_ATTACHED_PAIR, pokers.get(0).getValue(), 1);
                else if (count == 2)//这里取值第4个原因是，已经排序过了，两端肯定不一样
                    return new PokerPlayInfo(PokerRule.TRIPLET_WITH_AN_ATTACHED_PAIR, pokers.get(pokers.size() - 1).getValue(), 1);
            }
        } else if (size == 6) {//四带二，三不带2连，对子3连，单牌6连
            Map<Integer, Integer> map = formatPokerValue(pokers);
            if (map.size() == 6) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return new PokerPlayInfo(PokerRule.SEQUENCE, pokers.get(0).getValue(), size);//单连牌
                }
            } else if (map.size() == 3) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return new PokerPlayInfo(PokerRule.SEQUENCE_OF_PAIRS, pokers.get(0).getValue(), size / 2);//双连牌
                }
            } else if (map.size() == 2) {
                int count = map.get(pokers.get(0).getValue());
                if (count == 3)
                    return new PokerPlayInfo(PokerRule.SEQUENCE_OF_TRIPLETS, pokers.get(0).getValue(), size / 3);//三连牌
            }

            //四带二,因为二不一定是对子所以单独做判断
            int setKey = -1;
            for (int key : map.keySet()) {
                int value = map.get(key);
                if (value == 4) {
                    setKey = key;
                    break;
                }
            }
            if (setKey >= 0) return new PokerPlayInfo(PokerRule.QUADPLEX_SET, setKey, 1);

        } else {//剩下的都是连牌了
            Map<Integer, Integer> map = formatPokerValue(pokers);
            if (size == map.size()) {
                //先转换成数组
                int[] array = new int[map.size()];
                int pos = 0;
                for (int key : map.keySet()) {
                    array[pos++] = key;
                }
                //判断是否是连牌
                if (isContinuous(array)) {
                    return new PokerPlayInfo(PokerRule.SEQUENCE, pokers.get(0).getValue(), size);//单连牌
                }
            } else {
                if (size % 5 == 0) {//三带二连牌判断
                    //3和2的数量判断
                    int hit2 = 0;
                    int hit3 = 0;
                    for (int key : map.keySet()) {
                        int count = map.get(key);
                        if (count == 2) hit2++;
                        if (count == 3) hit3++;
                    }
                    if (hit2 == hit3 && hit2 == size / 5) {
                        //建立数组
                        int[] array = new int[hit3];
                        int pos = 0;
                        int start = -1;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count == 3) {
                                array[pos++] = key;
                                if (start == -1) start = key;
                            }
                        }
                        //判断是否是连牌
                        if (isContinuous(array)) {
                            //三带二连牌
                            return new PokerPlayInfo(PokerRule.SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_PAIRS, start, size / 5);
                        }
                    }
                }
                B:
                if (size % 3 == 0) {//三不带连牌判断
                    int[] array = new int[map.size()];
                    int pos = 0;
                    for (int key : map.keySet()) {
                        int count = map.get(key);
                        if (count != 3) break B;
                        array[pos++] = key;
                    }
                    //判断是否是连牌
                    if (isContinuous(array)) {
                        return new PokerPlayInfo(PokerRule.SEQUENCE_OF_TRIPLETS, pokers.get(0).getValue(), size / 3);//三连牌
                    }
                }
                if (size % 2 == 0) {//对子连牌判断
                    if (size % 6 == 0) {//四带二连牌判断,二不一定是对子
                        int hit4 = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count == 4) hit4++;
                        }
                        if (hit4 == size / 6) {
                            //建立数组
                            int[] array = new int[hit4];
                            int pos = 0;
                            int start = -1;
                            for (int key : map.keySet()) {
                                int count = map.get(key);
                                if (count == 4) {
                                    array[pos++] = key;
                                    if (start == -1) start = key;
                                }
                            }
                            //判断是否是连牌
                            if (isContinuous(array)) {
                                //四带二连牌
                                return new PokerPlayInfo(PokerRule.SEQUENCE_OF_QUADPLEX_SET, start, size / 6);
                            }
                        }

                    }
                    if (size % 4 == 0) {//三带一连牌判断
                        int hit1 = 0;
                        int hit3 = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count == 1) hit1++;
                            if (count == 2) hit1 += 2;
                            if (count == 3) hit3++;
                        }
                        if (hit1 == hit3 && hit1 == size / 4) {
                            //建立数组
                            int[] array = new int[hit3];
                            int pos = 0;
                            int start = -1;
                            for (int key : map.keySet()) {
                                int count = map.get(key);
                                if (count == 3) {
                                    array[pos++] = key;
                                    if (start == -1) start = key;
                                }
                            }
                            //判断是否是连牌
                            if (isContinuous(array)) {
                                //三带一连牌
                                return new PokerPlayInfo(PokerRule.SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_CARDS, start, size / 4);
                            }
                        }
                    }
                    //剩下判断对子连牌
                    E:
                    if (map.size() == size / 2) {
                        int[] array = new int[map.size()];
                        int pos = 0;
                        for (int key : map.keySet()) {
                            int count = map.get(key);
                            if (count != 2)
                                break E;
                            array[pos++] = key;
                        }
                        //判断是否是连牌
                        if (isContinuous(array)) {
                            return new PokerPlayInfo(PokerRule.SEQUENCE_OF_PAIRS, pokers.get(0).getValue(), size / 2);//双连牌
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 被动性出牌，从牌堆中寻找可以匹配的牌,复杂的就不做了
     *
     * @param pokers
     * @param playInfo
     * @return
     */
    public static List<Poker> findPokerPlayInfo(List<Poker> pokers, PokerPlayInfo playInfo) {
        if (pokers == null || pokers.size() < 1
                || playInfo == null || playInfo.getRule() == null || playInfo.getRule() == PokerRule.EXCEPT)
            return null;
        sort(pokers);
//        List<Poker> playPokers = new ArrayList<>();//需要出的牌

        //转化，key为面值，value为数量
        Map<Integer, Integer> map = formatPokerValue(pokers);
        ArrayMap<Integer, Integer> count4 = new ArrayMap<>();//炸弹
        ArrayMap<Integer, Integer> count3 = new ArrayMap<>();//三张
        ArrayMap<Integer, Integer> count2 = new ArrayMap<>();//对子和火箭
        ArrayMap<Integer, Integer> count1 = new ArrayMap<>();//纯粹的零牌
        ArrayMap<Integer, Integer> jokers = new ArrayMap<>();//纯粹的零牌
        for (int key : map.keySet()) {
            int value = map.get(key);
            if (value > 3)
                count4.put(key, value);
            else if (value > 2)
                count3.put(key, value);
            else if (value > 1)
                count2.put(key, value);
            else if (key < 15)
                count1.put(key, value);
            else
                jokers.put(key, value);
        }
        //只有一张时视为零牌
        if (jokers.size() < 2)
            count1.putAll(jokers);


        switch (playInfo.getRule()) {
            case ROCKET://火箭无解
                return null;
            case BOMB://炸弹
                if (count4.size() > 0) {
                    for (int size : count4.keySet()) {
                        if (size > playInfo.getSize()) {
                            return findPokers(pokers, size, 4);
                        }
                    }
                }
                break;
            case SINGLE_CARD://零牌
                for (int size : count1.keySet()) {
                    if (size > playInfo.getSize()) {
                        return findPokers(pokers, size, 1);
                    }
                }
                for (int size : count2.keySet()) {
                    if (size > playInfo.getSize()) {
                        return findPokers(pokers, size, 1);
                    }
                }
                for (int size : count3.keySet()) {
                    if (size > playInfo.getSize()) {
                        return findPokers(pokers, size, 1);
                    }
                }
                break;
            case PAIR://对子
                for (int size : count2.keySet()) {
                    if (size > playInfo.getSize()) {
                        return findPokers(pokers, size, 2);
                    }
                }
                for (int size : count3.keySet()) {
                    if (size > playInfo.getSize()) {
                        return findPokers(pokers, size, 2);
                    }
                }
                break;
            case TRIPLET://三不带
                for (int size : count3.keySet()) {
                    if (size > playInfo.getSize()) {
                        return findPokers(pokers, size, 3);
                    }
                }
                break;
            case TRIPLET_WITH_AN_ATTACHED_CARD://三带一
                if (pokers.size() > 3)
                    for (int size : count3.keySet()) {
                        if (size > playInfo.getSize()) {
                            List<Poker> playPokers = findPokers(pokers, size, 3);
                            //防止错误
                            if (playPokers == null || playPokers.size() < 3) break;
                            if (count1.size() > 0) {
                                int singleValue = count1.keyAt(0);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 1);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);
                            } else if (count2.size() > 0) {
                                int singleValue = count2.keyAt(0);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 1);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);
                            } else if (count3.size() > 1) {
                                int singleValue = count3.keyAt(0);
                                if (singleValue == size)
                                    singleValue = count3.keyAt(1);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 1);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);

                            }

                            if (playPokers.size() > 3)
                                return playPokers;

                        }
                    }
                break;
            case TRIPLET_WITH_AN_ATTACHED_PAIR://三带二
                if (pokers.size() > 4 && count3.size() > 0) {
                    for (int size : count3.keySet()) {
                        if (size > playInfo.getSize()) {
                            List<Poker> playPokers = findPokers(pokers, size, 3);
                            //防止错误
                            if (playPokers == null || playPokers.size() < 3) break;
                            if (count2.size() > 0) {
                                int singleValue = count2.keyAt(0);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 2);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);
                            } else if (count3.size() > 1) {
                                int singleValue = count3.keyAt(0);
                                if (singleValue == size)
                                    singleValue = count3.keyAt(1);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 2);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);
                            }
                            if (playPokers.size() > 4)
                                return playPokers;
                        }
                    }
                }
                break;
            case QUADPLEX_SET://四带二
                //我有炸弹为什么非要用4带二呢？因为可以多带走两张牌
                if (pokers.size() > 5 && count4.size() > 0) {
                    for (int size : count4.keySet()) {
                        if (size > playInfo.getSize()) {
                            List<Poker> playPokers = findPokers(pokers, size, 4);
                            //防止错误
                            if (playPokers == null || playPokers.size() < 4) break;
                            if (count1.size() > 1) {
                                int value1 = count1.keyAt(0);
                                int value2 = count1.keyAt(1);
                                List<Poker> playPokersAttach1 = findPokers(pokers, value1, 1);
                                if (playPokersAttach1 != null) playPokers.addAll(playPokersAttach1);
                                List<Poker> playPokersAttach2 = findPokers(pokers, value2, 1);
                                if (playPokersAttach2 != null) playPokers.addAll(playPokersAttach2);
                            } else if (count1.size() > 0) {
                                int value1 = count1.keyAt(0);
                                List<Poker> playPokersAttach1 = findPokers(pokers, value1, 1);
                                if (playPokersAttach1 != null) playPokers.addAll(playPokersAttach1);
                                if (count2.size() > 0) {
                                    int singleValue = count2.keyAt(0);
                                    List<Poker> playPokersAttach2 = findPokers(pokers, singleValue, 1);
                                    if (playPokersAttach2 != null)
                                        playPokers.addAll(playPokersAttach2);
                                } else if (count3.size() > 0) {
                                    int singleValue = count3.keyAt(0);
                                    List<Poker> playPokersAttach2 = findPokers(pokers, singleValue, 1);
                                    if (playPokersAttach2 != null)
                                        playPokers.addAll(playPokersAttach2);
                                }
                            } else if (count2.size() > 0) {
                                int singleValue = count2.keyAt(0);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 2);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);
                            } else if (count3.size() > 0) {
                                int singleValue = count3.keyAt(0);
                                List<Poker> playPokersAttach = findPokers(pokers, singleValue, 2);
                                if (playPokersAttach != null) playPokers.addAll(playPokersAttach);
                            }

                            if (playPokers.size() > 5)
                                return playPokers;
                        }
                    }
                }
                break;
            case SEQUENCE://单顺子
                if (map.size() >= playInfo.getSequenceCount()) {
                    //先转换成数组
                    int[] array = new int[map.size()];
                    int pos = 0;
                    for (int key : map.keySet()) {
                        array[pos++] = key;
                    }
                    //寻找连牌
                    List<Poker> playPokers = findContinuous(pokers, array, 1,
                            playInfo.getSize(), playInfo.getSequenceCount());
                    if (playPokers != null && playPokers.size() == playInfo.getSequenceCount())
                        return playPokers;
                }
                break;
            case SEQUENCE_OF_PAIRS://双顺子
                if (count2.size() + count3.size() + count4.size() >= playInfo.getSequenceCount()) {
                    //先转换成数组
                    int[] array = new int[count2.size() + count3.size() + count4.size()];
                    int pos = 0;
                    for (int key : count2.keySet()) {
                        array[pos++] = key;
                    }
                    for (int key : count3.keySet()) {
                        array[pos++] = key;
                    }
                    for (int key : count4.keySet()) {
                        array[pos++] = key;
                    }
                    //寻找连牌
                    List<Poker> playPokers = findContinuous(pokers, array, 2,
                            playInfo.getSize(), playInfo.getSequenceCount());
                    if (playPokers != null && playPokers.size() == playInfo.getSequenceCount() * 2)
                        return playPokers;
                }
                break;
            case SEQUENCE_OF_TRIPLETS://三顺子
                if (count3.size() + count4.size() >= playInfo.getSequenceCount()) {
                    //先转换成数组
                    int[] array = new int[count3.size() + count4.size()];
                    int pos = 0;
                    for (int key : count3.keySet()) {
                        array[pos++] = key;
                    }
                    for (int key : count4.keySet()) {
                        array[pos++] = key;
                    }
                    //寻找连牌
                    List<Poker> playPokers = findContinuous(pokers, array, 3,
                            playInfo.getSize(), playInfo.getSequenceCount());
                    if (playPokers != null && playPokers.size() == playInfo.getSequenceCount() * 3)
                        return playPokers;
                }
                break;
            case SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_CARDS://飞机单
                if (count3.size() + count4.size() >= playInfo.getSequenceCount()
                        && pokers.size() >= playInfo.getSequenceCount() * 4) {
                    //先转换成数组
                    int[] array = new int[count3.size() + count4.size()];
                    int pos = 0;
                    for (int key : count3.keySet()) {
                        array[pos++] = key;
                    }
                    for (int key : count4.keySet()) {
                        array[pos++] = key;
                    }
                    //寻找连牌
                    List<Poker> playPokers = findContinuous(pokers, array, 3,
                            playInfo.getSize(), playInfo.getSequenceCount());
                    if (playPokers != null && playPokers.size() == playInfo.getSequenceCount() * 3) {
                        //一级一级取值
                        int required = playInfo.getSequenceCount();
                        for (int i = 0; i < count1.size(); i++) {
                            int value = count1.keyAt(i);
                            List<Poker> playPokersAttach = findPokers(pokers, value, 1);
                            if (playPokersAttach != null)
                                playPokers.addAll(playPokersAttach);
                            //量够就行了
                            if (playPokers.size() == playInfo.getSequenceCount() * 4)
                                break;
                        }
                        required = playInfo.getSequenceCount() * 4 - playPokers.size();
                        if (required > 0)
                            for (int i = 0; i < count2.size(); i++) {
                                int value = count2.keyAt(i);
                                List<Poker> playPokersAttach = findPokers(pokers, value, required > 1 ? 2 : 1);
                                if (playPokersAttach != null)
                                    playPokers.addAll(playPokersAttach);
                                //量够就行了
                                required = playInfo.getSequenceCount() * 4 - playPokers.size();
                                if (required <= 0)
                                    break;
                            }
                        //继续在count3中寻找太麻烦了，所以不写了
                        if (playPokers.size() == playInfo.getSequenceCount() * 4)
                            return playPokers;
                    }
                }
                break;
            case SEQUENCE_OF_TRIPLETS_WITH_ATTACHED_PAIRS://飞机双
                if (count3.size() + count4.size() >= playInfo.getSequenceCount()
                        && count2.size() >= playInfo.getSequenceCount()) {
                    //先转换成数组
                    int[] array = new int[count3.size() + count4.size()];
                    int pos = 0;
                    for (int key : count3.keySet()) {
                        array[pos++] = key;
                    }
                    for (int key : count4.keySet()) {
                        array[pos++] = key;
                    }
                    //寻找连牌
                    List<Poker> playPokers = findContinuous(pokers, array, 3,
                            playInfo.getSize(), playInfo.getSequenceCount());
                    if (playPokers != null
                            && playPokers.size() == playInfo.getSequenceCount() * 3
                    ) {
                        for (int i = 0; i < playInfo.getSequenceCount(); i++) {
                            int value = count2.keyAt(i);
                            List<Poker> playPokersAttach = findPokers(pokers, value, 2);
                            if (playPokersAttach != null)
                                playPokers.addAll(playPokersAttach);
                        }
                        //继续在count3中寻找太麻烦了，所以不写了
                        if (playPokers.size() == playInfo.getSequenceCount() * 5)
                            return playPokers;
                    }
                }
                break;
            case SEQUENCE_OF_QUADPLEX_SET://四带二连牌
                //不写
                break;
        }

        //最后判断一手是否使用炸弹和火箭
        if (playInfo.getRule() != PokerRule.ROCKET
                && playInfo.getRule() != PokerRule.SINGLE_CARD
                && playInfo.getRule() != PokerRule.PAIR) {
            if (count4.size() > 0 && playInfo.getRule() != PokerRule.BOMB) {
                return findPokers(pokers, count4.keyAt(0), 4);
            } else if (jokers.size() > 1) {
                List<Poker> playPokers = new ArrayList<>();
                for (Poker poker : pokers) {
                    if (poker.getValue() > 15)
                        playPokers.add(poker);
                }
                if (playPokers.size() > 1) return playPokers;
            }
        }

        return null;
    }

    /**
     * 从牌堆里寻找对应值对应数量的牌
     *
     * @param pokers
     * @param value
     * @param count
     */
    public static List<Poker> findPokers(List<Poker> pokers, int value, int count) {
        List<Poker> newPokers = new ArrayList<>();
        for (Poker poker : pokers) {
            if (poker.getValue() == value) {
                newPokers.add(poker);
                if (newPokers.size() == count)
                    break;
            }
        }
        //数量校验
        if (newPokers.size() == count)
            return newPokers;
        else return null;
    }


    /**
     * @param pokers        母体
     * @param array         连牌判断器
     * @param valueCount    连牌单体数量
     * @param size          连牌起始牌大小
     * @param sequenceCount 连牌数量
     * @return
     */
    public static List<Poker> findContinuous(List<Poker> pokers, int[] array, int valueCount,
                                             int size, int sequenceCount) {
        //排序
        Arrays.sort(array);
        boolean isEmpty = true;
        int start = -1;
        for (int i = 0; i < array.length; i++) {
            int value = array[i];
            //连续性判断
            if (i > 0 && value - array[i - 1] != 1) {
                start = -1;
            }
            //起始位置判断
            if (value > size && start < 0) start = i;
            //连牌长度判断
            if (start > -1 && i - start + 1 == sequenceCount) {
                isEmpty = false;
                break;
            }
        }
        if (!isEmpty) {
            //找出对应牌
            List<Poker> newPokers = new ArrayList<>();
            sort(pokers);
            int startValue = array[start];
            int value = startValue;
            int nowValueCount = 1;
            for (Poker poker : pokers) {
                if (poker.getValue() == value) {
                    newPokers.add(poker);
                    if (nowValueCount == valueCount) {
                        value++;
                        nowValueCount = 1;
                    } else {
                        nowValueCount++;
                    }
                    //连牌数量足够
                    //value - startValue + 1 > sequenceCount
                    if (value - startValue == sequenceCount) {
                        return newPokers;
                    }
                }
            }
        }
        return null;
    }

}
