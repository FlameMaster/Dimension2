package com.melvinhou.tiktok_sample.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import com.melvinhou.tiktok_sample.R;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/23 21:20
 * <p>
 * = 分 类 说 明：自定义控件支持抖音评论区
 * ================================================
 */
public class TikTokCommentLayout extends ViewGroup implements NestedScrollingParent3 {

    //控件
    private View mCommentCountView, mCommentInputView, mScrollView;

    //常用位置
    private float downY, lastY, dy;
    //移动范围
    private int maxTop, minTop;


    //        ViewDragHelper
    //滚动器
    private OverScroller mScroller;
    //速度计算器
    private VelocityTracker mVelocityTracker;
    //最大速度
    private int mMaximumVelocity;

    //滑动父类帮助
    private NestedScrollingParentHelper mParentHelper;
    //是否这执行动画
    private boolean isAnimatorExecuting = false;

    private boolean isOpen = false;//是否展开

    private CommentContainerStateListener mStateListener;

    //打开评论区
    public void open() {
        fling(-5000, 0);
    }

    //关闭评论区
    public void close() {
        fling(5000, 0);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setStateListener(CommentContainerStateListener listener) {
        mStateListener = listener;
    }

    public TikTokCommentLayout(Context context) {
        this(context, null);
    }

    public TikTokCommentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TikTokCommentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TikTokCommentLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        minTop = 0;

        mParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new OverScroller(getContext());
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mCommentCountView = findViewById(R.id.comment_count);
        mScrollView = findViewById(R.id.scroll);
        mCommentInputView = findViewById(R.id.comment_input);
        //测量所有子控件的宽和高,只有先测量了所有子控件的尺寸，后面才能使用child.getMeasuredWidth()
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //调用系统的onMeasure一般是测量自己(当前ViewGroup)的宽和高
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /*测量某一个child的宽高，考虑margin值*/
    @Override
    protected void measureChildWithMargins(View child,
                                           int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        //获取子控件的宽高约束规则
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, lp.leftMargin + lp.rightMargin
                + widthUsed, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, lp.topMargin + lp.bottomMargin
                + heightUsed, lp.height);
        //测量子控件
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        maxTop = bottom - top;
        int commentCountViewHeight = mCommentCountView.getMeasuredHeight();
        int commentInputViewHeight = mCommentInputView.getMeasuredHeight();
        mCommentCountView.layout(left, 0, right, commentCountViewHeight);
        mScrollView.layout(left, commentCountViewHeight, right, maxTop - commentInputViewHeight);
        mCommentInputView.layout(left, maxTop - commentInputViewHeight, right, maxTop);

        //直接关闭
        setTranslationY(maxTop);
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /**
         * VelocityTracker速度是以MotionEvent.getY取值的
         * getY是获取控件的相对位置（即控件的左上角为原点）
         * 相对位置容易受到控件位置变化影响，造成速度获取错误
         */
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(event);
        vtev.offsetLocation(0, -event.getY());
        vtev.offsetLocation(0, event.getRawY());
        mVelocityTracker.addMovement(vtev);
        vtev.recycle();
        return super.dispatchTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        float y = event.getRawY();
//        Log.w("scrollChild", new StringBuffer()
//                .append("MotionEvent").append("：").append(event.getAction()).append("\r\t")
//                .append("rawY").append("：").append(event.getY()).append("\r\t")
//                .append("y").append("：").append(event.getY()).append("\r\t")
//                .toString());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downY = y;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                dy = y - lastY;
                if (getTranslationY() + dy < minTop) dy = minTop - getTranslationY();
                if (getTranslationY() + dy > maxTop) dy = maxTop - getTranslationY();
                scrollContainer(dy);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                float toucy = y - downY;
                //速度矫正,未能研究透彻为什么会这样，只能矫正
//                if ((toucy < 0 && initialVelocity > 0)//手指从下往上滑，v>0
//                        || (toucy > 0 && initialVelocity < 0)) {//手指从上往下滑,v<0
//                    initialVelocity = -initialVelocity;
//                }
                fling(initialVelocity, toucy);
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                break;
        }
        lastY = y;
        return true;
    }

    private void scrollContainer(float dy) {
//        Log.d("scrollChild", new StringBuffer("")
//                .append("dy").append("：").append(dy).append("\r\t")
//                .append("translationY").append("：").append(getTranslationY()).append("\r\t")
//                .toString());
        final float top = getTranslationY() + dy;
        setTranslationY(top);
        invalidate();
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        isOpen = translationY < maxTop;
        //更新监听
        if (mStateListener != null)
            if (translationY == maxTop || translationY == minTop) {
                mStateListener.onChangeUnfoldState(isOpen);
            }
    }

    /**
     * 滑动
     *
     * @param velocityY 速度
     * @param touchY    总共滑动距离
     */
    public void fling(int velocityY, float touchY) {
        final int startY = (int) getTranslationY();
//        Log.w("scrollChild", new StringBuffer()
//                .append("velocityY").append("：").append(velocityY).append("\r\t")
//                .append("startY").append("：").append(startY).append("\r\t")
//                .toString());
        int absVelocity = Math.abs(velocityY);
        //根据速度值优化体验
        if (absVelocity < 300) {
            start2Original();
            return;
        } else if (absVelocity < 1000) {
            velocityY = velocityY / absVelocity * 5000;
        } else {
            velocityY *= 5;
        }
        mScroller.fling(0, startY, 0, velocityY,
                0, 0, minTop, maxTop, 0, 20);
//            postInvalidateOnAnimation();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /*重写滚动*/
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollContainer(mScroller.getCurrY() - getTranslationY());
        }
    }


    /*执行动画回到吸附位置*/
    private void start2Original() {
        isAnimatorExecuting = true;
        float maxSize = maxTop - minTop;
        float finalTop = getTranslationY() > maxSize / 2 ? maxTop : minTop;
        float time = Math.abs(finalTop - getTranslationY()) / maxSize * 500f;
//        mScroller.startScroll(0,currentTop,0,(int) finalTop-currentTop,(int)time);
        ValueAnimator animator = ValueAnimator.ofFloat(getTranslationY(), finalTop);
        animator.addUpdateListener(animator1 -> {
            float targetTop = (float) animator1.getAnimatedValue();
            scrollContainer(targetTop - getTranslationY());
            if (targetTop == finalTop) {
                isAnimatorExecuting = false;
                animator1.cancel();
            }
        });
        //弹性的插值器
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(Float.valueOf(time).longValue());
        animator.start();
    }


////////////////////////————————————————————嵌套滑动(父)——————————————————————////////////////////////

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
//        type 1是滚动  0是滑动ViewCompat.TYPE_TOUCH
        return child == target;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
//        Log.e("onStopNestedScroll", new StringBuffer()
//                .append("type").append("：").append(type).append("\r\t")
//                .append("translationY").append("：").append(getTranslationY()).append("\r\t")
//                .toString());
        //动画未执行，滚动已经完成
        if (!isAnimatorExecuting && mScroller.isFinished()
                && getTranslationY() < maxTop && getTranslationY() > minTop) {
//            start2Original();
            final VelocityTracker velocityTracker = mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
            int initialVelocity = (int) velocityTracker.getYVelocity();
            fling(initialVelocity, 0);
            recycleVelocityTracker();
        }
    }

    //子类消耗后通知父类
    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, null);
    }


    //子类消耗后通知父类【3代新增】
    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type, @NonNull int[] consumed) {
//        Log.d("onNestedScroll[3]", new StringBuffer()
//                .append("dyConsumed").append("：").append(dyConsumed).append("\r\t")
//                .append("dyUnconsumed").append("：").append(dyUnconsumed).append("\r\t")
//                .append("consumed").append("：").append(consumed[1]).append("\r\t")
//                .toString());
        //只处理滑动
        if (type != ViewCompat.TYPE_TOUCH) return;
        //只处理scroll滑动到最顶部时继续滑动
        if (dyUnconsumed < 0) {
            scrollContainer(-dyUnconsumed);
        }

    }

    //子类消耗前通知父类
    //消耗dy，消耗的值放在consumed中
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
//        Log.w("onNestedPreScroll", new StringBuffer()
//                .append("dy").append("：").append(dy).append("\r\t")
//                .append("consumed").append("：").append(consumed[1]).append("\r\t")
//                .append("translationY").append("：").append(getTranslationY()).append("\r\t")
//                .toString());
//        consumed[1] = dy;

        //只处理滑动
        if (type != ViewCompat.TYPE_TOUCH) return;
        //处理向上滑动
        if (getTranslationY() > minTop && dy > 0) {
            int startY = (int) getTranslationY();
            int targetY = startY - dy;
            if (targetY < minTop) {
                dy = minTop - startY;
                dy = -dy;
            }
            scrollContainer(-dy);
            consumed[1] = dy;
        }
    }


    public interface CommentContainerStateListener {
        void onChangeUnfoldState(boolean isOpen);
    }

}
