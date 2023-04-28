package com.melvinhou.game.poker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.game.R;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/1 21:56
 * <p>
 * = 分 类 说 明：扑克牌容器
 * ================================================
 */
public class PokerShowCardsContainer extends ViewGroup {

    //是否可以被选中
    private boolean isCanOpt = false;
    //选中和未选中的偏移量
    private int mCardOffset = 0;
    private PokerCheckedListener mCheckedListener;
    //按下时的位置
    private int mDownPosition = -1;
    private int mTextSize = 14;

    public PokerShowCardsContainer(Context context) {
        this(context, null);
    }

    public PokerShowCardsContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PokerShowCardsContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intit();
    }

    private void intit() {
    }

    public void setTextSize(int size) {
        mTextSize = size;
    }

    public void setCanOpt(boolean canOpt) {
        isCanOpt = canOpt;
    }

    public void setCheckedListener(PokerCheckedListener checkedListener) {
        this.mCheckedListener = checkedListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量所有子控件的宽和高,只有先测量了所有子控件的尺寸，后面才能使用child.getMeasuredWidth()
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int cardHeight = bottom - top;
        if (isCanOpt)
            cardHeight = Float.valueOf((bottom - top) * 0.8f).intValue();
        int cardWidth = Float.valueOf(cardHeight * 3f / 4f).intValue();
        //扑克的位置
        int childTop = (bottom - top) - cardHeight;
        mCardOffset = childTop;
        int childBottom = childTop + cardHeight;
        int startLeft = 0;
        //计算宽度
        int childWidth = cardWidth;
        int maxWidth = cardWidth * getChildCount();
        if (maxWidth > getWidth()) {
            childWidth = (getWidth() - cardWidth) / (getChildCount() - 1);
        } else {
            startLeft += (getWidth() - maxWidth) / 2;
        }
        //初始化扑克位置
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childLeft = startLeft + i * childWidth;
            child.layout(childLeft, childTop, childLeft + cardWidth, childBottom);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if (action == MotionEvent.ACTION_DOWN) {
            mDownPosition = getCurrentPosition(x, y);
        } else if (action == MotionEvent.ACTION_UP) {
            int position = getCurrentPosition(x, y);
            boolean isOpt = position == mDownPosition;
            mDownPosition = -1;
            if (position >= 0 && isOpt) {
                if (mCheckedListener != null) {
                    mCheckedListener.onCheckedChanged(this, position);
                }
            }
        }
        return true;
    }


    /**
     * 获取当前坐标的子view
     *
     * @param x
     * @param y
     * @return
     */
    private int getCurrentPosition(int x, int y) {
        Rect rect = new Rect();
        //倒序
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            child.getGlobalVisibleRect(rect);
            if (rect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 取消选中
     *
     * @param index
     */
    public void unCheck(int index) {
        getChildAt(index).setTranslationY(0);
    }

    /**
     * 选中
     *
     * @param index
     */
    public void check(int index) {
        getChildAt(index).setTranslationY(-mCardOffset);
    }


    /**
     * 添加一个poker的控件
     *
     * @param poker
     */
    public void addPokerView(Poker poker) {
        if (poker != null) {
            TextView view = new TextView(FcUtils.getContext());
            LayoutParams lp = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
            int padding = DimenUtils.dp2px(4);
            view.setPadding(padding, padding, padding, padding);
            view.setTextSize(mTextSize);
            if (poker.getSuit() == Poker.BIG_JOKER || poker.getSuit() == Poker.LITTLE_JOKER) {
                view.setIncludeFontPadding(false);
                view.setLineSpacing(0.8f, 0.8f);
            }

            //颜色
            if (poker.getSuit() == Poker.SUIT_HEART
                    || poker.getSuit() == Poker.SUIT_DIAMOND
                    || poker.getSuit() == Poker.BIG_JOKER) {
                view.setTextColor(Color.RED);
                view.setBackgroundResource(R.drawable.bg_poker_red);
            } else {
                view.setTextColor(Color.BLACK);
                view.setBackgroundResource(R.drawable.bg_poker_black);
            }
            //显示文字
            if (poker.getShowSuit() != null &&
                    poker.getShowSuit().length() > 1 &&
                    poker.getShowSuit().length() < 3) {
                view.setText(new StringBuffer(poker.getShowValue()).append("\n")
                        .append(poker.getShowSuit().substring(0, 1)).append("\n")
                        .append(poker.getShowSuit().substring(1))
                );
            } else {
                view.setText(poker.getShowValue() + "\n" + poker.getShowSuit());
            }
            addView(view);
        }
    }


    public interface PokerCheckedListener {

        void onCheckedChanged(PokerShowCardsContainer cardsContainer, int position);
    }
}
