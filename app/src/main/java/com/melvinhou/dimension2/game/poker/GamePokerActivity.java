package com.melvinhou.dimension2.game.poker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/1 0:19
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class GamePokerActivity extends BaseActivity {
    /**
     * 流程：
     * 洗牌，发牌，地主归属，确定阵营
     * 开始出牌，一轮出牌，按照当前的出牌规则出牌比较，新的最大同规则替代旧的信息（如果下一轮到自己没有人出牌，清空信息规则）
     * 牌出完时自动获胜
     * 己方出牌需要：确定规则的牌，匹配规则，比较大小
     * 电脑出牌需要：匹配规则，寻找规则的牌，比较大小
     */
    /**
     * 牌信息：大小，花色
     * <p>
     * 出牌规则：
     * 单，双，三不带，炸弹，双王
     * 三带一，三带二，四带二
     * 连牌单，连牌双，连牌三
     * 连牌三带一，连牌三带二，连牌四带二
     */

    LinearLayout mDesktop;
    PokerShowCardsContainer mCardsContainer;
    View mPlayBt, mPassBt;
    TextView mPlayer1Count, mPlayer2Count, mPlayerInfo;

    List<Poker> mAllCards = new ArrayList<>();
    List<Poker> myCards = new ArrayList<>();
    List<Poker> player1Cards = new ArrayList<>();
    List<Poker> player2Cards = new ArrayList<>();
    Set<Integer> mCheckedCards = new HashSet<>();
    PokerPlayInfo mCurrentPlayInfo;
    //回合数
    int mGamesNumber = 0;
    //当前庄家
    int mGameBanker = 0;
    int mGamePassCount = 0;


    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        //浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.parseColor("#228b22"));
        getWindow().setNavigationBarColor(Color.parseColor("#228b22"));
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_poker;
    }

    @Override
    protected void initView() {
        mDesktop = findViewById(R.id.desktop);
        mCardsContainer = findViewById(R.id.player_cards);
        mPlayer1Count = findViewById(R.id.player1_count);
        mPlayer2Count = findViewById(R.id.player2_count);
        mPlayerInfo = findViewById(R.id.player_game);
        mPlayBt = findViewById(R.id.play);
        mPassBt = findViewById(R.id.pass);

        mCardsContainer.setCanOpt(true);
    }

    @Override
    protected void initListener() {
        mPlayBt.setOnClickListener(this::onGamePlay);
        mPassBt.setOnClickListener(this::onGamePass);
        findViewById(R.id.player_photo).setOnClickListener(this::reStart);

        mCardsContainer.setCheckedListener(new PokerShowCardsContainer.PokerCheckedListener() {
            @Override
            public void onCheckedChanged(PokerShowCardsContainer cardsContainer, int position) {
                Log.e("PokerShowCardsContainer", new StringBuffer()
                        .append("position").append(":\r").append(position).append("\r\t")
                        .toString());
                if (mCheckedCards.remove(position)) {
                    cardsContainer.unCheck(position);
                } else {
                    mCheckedCards.add(position);
                    cardsContainer.check(position);
                }

                if (mCheckedCards.size() > 0)
                    showPlayGameButton();
                else
                    hidePlayGameButton();
            }
        });
    }

    @Override
    protected void initData() {
        loadNetImage(R.id.player_photo, HttpConstant.SERVER_RES +"image/game/poker/player.jpg");
        loadNetImage(R.id.player1, HttpConstant.SERVER_RES +"image/game/poker/player1.png");
        loadNetImage(R.id.player2, HttpConstant.SERVER_RES +"image/game/poker/player2.png");
        reStart(null);
    }

    private void loadNetImage(int id, String url) {
        ImageView view = findViewById(id);
        if (view == null) return;
        Glide.with(FcUtils.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(view);
    }


    private void reStart(View view) {
        mDesktop.removeAllViews();
        toShuffle();
        toDeal();
//        test();//测试用
    }

    private void test() {
        //初始化一副牌
        mAllCards.clear();
        for (int i = 0; i < 48; i++) {
            Poker poker = new Poker(i % 12 + 1, i % 4 + 1);
            mAllCards.add(poker);
        }
        //为了方便连牌；判断故将2和king单独做出来
        for (int i = 0; i < 4; i++) {
            Poker poker = new Poker(15, i + 1);
            mAllCards.add(poker);
        }
        mAllCards.add(new Poker(20, Poker.LITTLE_JOKER));
        mAllCards.add(new Poker(30, Poker.BIG_JOKER));
//        PokerUtils.sort(mAllCards);


        myCards.clear();
        player1Cards.clear();
        for (int i = 0; i < 27; i++) {
            myCards.add(mAllCards.remove(0));
        }
        player1Cards.addAll(mAllCards);
//        for (int i = player1Cards.size() - 1; i > 0; i--) {
//            if (i % 7 == 0)
//                player1Cards.remove(i);
//        }
        mAllCards.clear();
        //地主牌
        mGamesNumber = 2;
        //序列化
        PokerUtils.sort(myCards);
        //更新控件
        mCardsContainer.removeAllViews();
        for (Poker poker : myCards)
            mCardsContainer.addPokerView(poker);
        updateDesktop();

        //开始出牌
        nextPlay();

    }

    /**
     * 洗牌
     */
    private void toShuffle() {
        //初始化一副牌
        mAllCards.clear();
        for (int i = 0; i < 48; i++) {
            Poker poker = new Poker(i % 12 + 1, i % 4 + 1);
            mAllCards.add(poker);
        }
        //为了方便连牌；判断故将2和king单独做出来
        for (int i = 0; i < 4; i++) {
            Poker poker = new Poker(15, i + 1);
            mAllCards.add(poker);
        }
        mAllCards.add(new Poker(20, Poker.LITTLE_JOKER));
        mAllCards.add(new Poker(30, Poker.BIG_JOKER));

        //洗牌
//        PokerUtils.sort(mAllCards);
        Collections.shuffle(mAllCards);
        Collections.shuffle(mAllCards);
        Collections.shuffle(mAllCards);
//        Collections.reverse(mAllCards);//倒序

    }

    /**
     * 倒牌
     */
    private void toDeal() {
        myCards.clear();
        player1Cards.clear();
        player2Cards.clear();
        for (int i = 0; i < 17; i++) {
            myCards.add(mAllCards.remove(0));
            player1Cards.add(mAllCards.remove(0));
            player2Cards.add(mAllCards.remove(0));
        }
        //地主牌
        mGamesNumber = 2;
        myCards.addAll(mAllCards);
        mAllCards.clear();
        //序列化
        PokerUtils.sort(myCards);
        //更新控件
        mCardsContainer.removeAllViews();
        for (Poker poker : myCards)
            mCardsContainer.addPokerView(poker);
        updateDesktop();

        //开始出牌
        nextPlay();
    }

    @SuppressLint("CheckResult")
    private void nextPlay() {
        mGamesNumber++;
        Observable.timer(1, TimeUnit.SECONDS)
                .compose(IOUtils.setThread())
                .subscribe(aLong -> {
                    if (mGamePassCount == 2) {
                        mGamePassCount = 0;
                        //清空规则
                        mCurrentPlayInfo = null;
                        //给机器人简单点
                        if (mGamesNumber % 3 != 0)
                            mCurrentPlayInfo = new PokerPlayInfo(PokerRule.SINGLE_CARD, 0, 1);
                    }
                    switch (mGamesNumber % 3) {
                        case 0:
                            showPlayGameButton();
                            break;
                        case 1:
                            testPlayer2Play();
                            break;
                        case 2:
                            testPlayer1Play();
                            break;
                    }
                });
    }

    private void testPlayer1Play() {
        List<Poker> playPokers = PokerUtils.findPokerPlayInfo(player1Cards, mCurrentPlayInfo);
        if (playPokers != null) {
            mCurrentPlayInfo = PokerUtils.getPokerPlayInfo(playPokers);
            for (Poker poker : playPokers) {
                player1Cards.remove(poker);
            }
        }
        if (playPokers != null) {
            //添加到桌面
            addPokers2Desktop(playPokers);
            mGameBanker = 2;
            mGamePassCount = 0;
            updateDesktop();
        } else mGamePassCount++;
        //通知下一位出牌
        nextPlay();
    }

    private void testPlayer2Play() {
        List<Poker> playPokers = PokerUtils.findPokerPlayInfo(player2Cards, mCurrentPlayInfo);
        if (playPokers != null) {
            mCurrentPlayInfo = PokerUtils.getPokerPlayInfo(playPokers);
            for (Poker poker : playPokers) {
                player2Cards.remove(poker);
            }
        }
        if (playPokers != null) {
            //添加到桌面
            addPokers2Desktop(playPokers);
            mGameBanker = 1;
            mGamePassCount = 0;
            updateDesktop();
        } else mGamePassCount++;
        //通知下一位出牌
        nextPlay();
    }

    private void updateDesktop() {
        //牌堆数量
        int player1Count = player1Cards.size();
        mPlayer1Count.setText(player1Count > 9 ? String.valueOf(player1Count) : "0" + player1Count);
        int player2Count = player2Cards.size();
        mPlayer2Count.setText(player2Count > 9 ? String.valueOf(player2Count) : "0" + player2Count);
        //庄家信息
        mPlayerInfo.setText("地主: 玩家\n庄家: " + (mGameBanker == 2 ? "矢泽妮可" : mGameBanker == 1 ? "东条希" : "玩家"));

    }


    private void onGamePass(View view) {
        takeBackPokers();
        mGamePassCount++;
        hidePlayGameGroup();
        //通知下一位出牌
        nextPlay();
    }

    private void onGamePlay(View view) {
        List<Poker> pokerList = new ArrayList<>();
        List<Integer> list = new ArrayList<>(mCheckedCards);
        for (int pos : list) {
            Poker poker = myCards.get(pos);
            if (poker != null) pokerList.add(poker);
        }
        PokerPlayInfo myPlayinfo = PokerUtils.getPokerPlayInfo(pokerList);
        //判断是否可以出牌
        if (myPlayinfo == null
                || myPlayinfo.getRule() == PokerRule.EXCEPT) {
            takeBackPokers();
            return;
        }
        boolean isQuell = false;
        if (mCurrentPlayInfo != null) {
            if (myPlayinfo.getRule() == PokerRule.ROCKET)
                isQuell = true;
            else if (myPlayinfo.getRule() == PokerRule.BOMB
                    && mCurrentPlayInfo.getRule() != PokerRule.ROCKET
                    && mCurrentPlayInfo.getRule() != PokerRule.BOMB)
                isQuell = true;

            else if (myPlayinfo.getRule() == mCurrentPlayInfo.getRule()
                    && myPlayinfo.getSize() > mCurrentPlayInfo.getSize())
                isQuell = true;

        } else isQuell = true;
        //出牌
        if (isQuell) {
            mCurrentPlayInfo = myPlayinfo;
            Collections.sort(list);
            Collections.reverse(list);
            for (int pos : list) {
                Log.d("删除手牌", "lll=" + pos);
                myCards.remove(pos);
                mCardsContainer.removeViewAt(pos);
            }
            mCheckedCards.clear();
            hidePlayGameGroup();
            //添加到桌面
            addPokers2Desktop(pokerList);
            mGameBanker = 0;
            mGamePassCount = 0;//重置
            updateDesktop();
            //通知下一位出牌
            nextPlay();
        } else takeBackPokers();
    }

    /**
     * 收回手牌
     */
    private void takeBackPokers() {
        for (int pos : mCheckedCards)
            mCardsContainer.unCheck(pos);
        mCheckedCards.clear();
        hidePlayGameButton();
    }

    private void showPlayGameButton() {
        if (mGamesNumber % 3 == 0) {
            if (mCheckedCards.size() > 0)
                mPlayBt.setVisibility(View.VISIBLE);
            mPassBt.setVisibility(View.VISIBLE);
        }
    }

    private void hidePlayGameButton() {
        mPlayBt.setVisibility(View.INVISIBLE);
    }

    private void hidePlayGameGroup() {
        hidePlayGameButton();
        mPassBt.setVisibility(View.INVISIBLE);
    }

    private void addPokers2Desktop(List<Poker> pokerList) {

        PokerUtils.sort(pokerList);//排序
//        FcUtils.showToast(PokerUtils.getPokerRuleName(PokerUtils.getPokerRule(pokerList)));

        PokerShowCardsContainer cardsContainer = new PokerShowCardsContainer(FcUtils.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                DimenUtils.dp2px(60));
        cardsContainer.setLayoutParams(lp);
        cardsContainer.setTextSize(10);
        for (Poker poker : pokerList) {
            cardsContainer.addPokerView(poker);
        }
        if (mDesktop.getChildCount() > 2) {
            mDesktop.removeViewAt(0);
        }
        mDesktop.addView(cardsContainer);

    }
}
