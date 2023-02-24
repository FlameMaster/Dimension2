package com.melvinhou.kami.view.wiget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.kami.util.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/7/2 10:24
 * <p>
 * = 分 类 说 明：条目
 * ============================================================
 */
public class TabLayout extends ViewGroup {

    int mNowTabPosition = 0;
    /*最大列数*/
    private int maxLine = 1;
    /*纵向间隔*/
    private int mVSpac = 0, mHSpac = 0;
    /*子view宽度*/
    private int mTabWidth = -1;
    /*每一行的集合*/
    private final List<TagLine> mTagLines = new ArrayList<>();
    private TagLine mTagLine = null;
    /*设置tab点击背景*/
    private int mBackgroundNR, mBackgroundHR;
    /*设置tab点击字体颜色*/
    private int mTextColorNR, mTextColorHR;
    private TabOnChangeListener mTabOnChangeListener;


    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHSpac = 0;
        mVSpac = 0;
        maxLine = Integer.MAX_VALUE;
        mBackgroundNR = -1;
        mBackgroundHR = -1;
        mTextColorNR = -1;
        mTextColorHR = -1;
        mNowTabPosition = 0;
        mTabWidth = -1;
    }

    public void setBackgroundR(int nR, int hR) {
        mBackgroundNR = nR;
        mBackgroundHR = hR;
    }


    public void setTextColorR(int nR, int hR) {
        mTextColorNR = nR;
        mTextColorHR = hR;
    }


    public void setTabWidth(int tabWidth) {
        this.mTabWidth = tabWidth;
    }

    public void setHorizontalSpacing(int spacing) {
        if (mHSpac != spacing) {
            mHSpac = spacing;
            requestLayout();
        }
    }

    public void setVerticalSpacing(int spacing) {
        if (mVSpac != spacing) {
            mVSpac = spacing;
            requestLayout();
        }
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    /*改变文字*/
    public void setText(String text, int position) {
        TextView tv = (TextView) getChildAt(position);
        tv.setText(text);
        //重绘
        requestLayout();
        invalidate();
    }

    /*返回当前选中序号*/
    public int getNowTabPosition() {
        return mNowTabPosition;
    }


///////////////////———————————————————————UI—————————————————————————/////////////////////


    /*测量时，测量每个view的宽度*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int availableWidth = MeasureSpec.getSize(widthMeasureSpec)
                - getPaddingRight() - getPaddingLeft();
        int availableHeight = MeasureSpec.getSize(heightMeasureSpec)
                - getPaddingTop() - getPaddingBottom();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 将行的状态重置为最原始的状态，因为新的一行的数据跟以往的无关
        resetLine();
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (mBackgroundNR <= 0)
                getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            else
                getChildAt(i).setBackgroundResource(mBackgroundNR);
            setItemOnClickListener(child, i);
            //默认选中
            if (mNowTabPosition == i) onItemClick(i);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth,
                    widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
                            : widthMode);
            if (mTabWidth >= 0)
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mTabWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    availableHeight,
                    heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST
                            : heightMode);
            // 测量子控件
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);


            Log.e("测量结果", "item=" + ((TextView) child).getText()
                    + "\navailableWidth=" + availableWidth
                    + "\tchildWidth=" + "mAllChildWidth=" + mTagLine.mAllChildWidth
                    + "\tchildWidth=" + child.getMeasuredWidth());
            //判断是否换行
            if (i % maxLine == maxLine - 1
                    || mTagLine.mAllChildWidth + child.getMeasuredWidth()
                    >= availableWidth)
                addLine();
            // 添加子控件
            mTagLine.addView(child);
        }
        if (mTagLine != null && mTagLine.getViewCount() > 0
                && !mTagLines.contains(mTagLine)) {
            //此段代码的作用是为了防止因最后一行代码的子控件未占满空间，但是毕竟也是一行，所以也要添加到行的集合里面
            mTagLines.add(mTagLine);
        }

        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int totalHeight = 0;
        final int size = mTagLines.size();
        for (int i = 0; i < size; i++) {// 加上所有行的高度
            totalHeight += mTagLines.get(i).mChildHeight;
        }
        totalHeight += mVSpac * (size - 1);// 加上所有间距的高度
        totalHeight += getPaddingTop() + getPaddingBottom();// 加上padding
        setMeasuredDimension(totalWidth,
                resolveSize(totalHeight, heightMeasureSpec));
    }

    /*根据测量结果显示位置*/
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();// 获取最初的左上点
        int top = getPaddingTop();
        final int linesCount = mTagLines.size();
        for (int i = 0; i < linesCount; i++) {
            final TagLine oneLine = mTagLines.get(i);
            oneLine.layoutView(left, top);// 设置每一行所在的位置
            top += oneLine.mChildHeight + mVSpac;// 这个top的值其实就是下一个的上顶点值
        }
    }

    /*将行的状态重置为最原始的状态*/
    private void resetLine() {
        mTagLines.clear();
        mTagLine = new TagLine();
    }

    /* 新增加一行*/
    private void addLine() {
        mTagLines.add(mTagLine);
        mTagLine = new TagLine();
    }

    /*设置默认选择*/
    public void setDefaultCheck(int position) {
        onItemClick(position);
    }

    /* 代表着一行，封装了一行所占高度，该行子View的集合，以及所有View的宽度总和*/
    private class TagLine {
        // 该行中所有的子控件加起来的宽度
        int mAllChildWidth = 0;
        // 子控件的高度
        int mChildHeight = 0;
        List<View> viewList = new ArrayList<>();

        public void addView(View view) {// 添加子控件
            viewList.add(view);
            mAllChildWidth += (view.getMeasuredWidth() + mHSpac);
            int childHeight = view.getMeasuredHeight();
            // 行的高度当然是有子控件的高度决定了
            mChildHeight = childHeight;
        }

        public int getViewCount() {
            return viewList.size();
        }

        public void layoutView(int left, int top) {
            int childCount = getViewCount();
            //除去左右边距后可以使用的宽度
            int validWidth = getMeasuredWidth() - getPaddingLeft()
                    - getPaddingRight();
            // 除了子控件后剩余的空间
            int remainWidth = validWidth - viewList.get(0).getMeasuredWidth() * maxLine;
            if (childCount == 1) remainWidth = validWidth - viewList.get(0).getMeasuredWidth();

            if (remainWidth >= 0) {
                //子控件大小
                if (maxLine > 1) {
                    int divideSpac = remainWidth / (maxLine - 1);
                    for (int i = 0; i < childCount; i++) {
                        final View view = viewList.get(i);
                        int childWidth = view.getMeasuredWidth();
                        int childHeight = view.getMeasuredHeight();
                        // 设置子控件的位置
                        view.layout(left, top, left + childWidth, top
                                + childHeight);
                        // 获取到的left值是下一个子控件的左边所在的位置
                        left += childWidth + divideSpac + mHSpac;
                    }
                } else {
                    View view = viewList.get(0);
                    left = left + remainWidth / 2;
                    view.layout(left, top, left + view.getMeasuredWidth(), top
                            + view.getMeasuredHeight());
                }
            } else {
                if (childCount == 1) {//这一种就是一行只有一个子控件的情况
                    View view = viewList.get(0);
                    view.layout(left, top, left + view.getMeasuredWidth(), top
                            + view.getMeasuredHeight());
                }
            }
        }
    }

///////////////////————————————————————————————————————————————————/////////////////////

    /*注册点击事件*/
    private void setItemOnClickListener(View child, int position) {
        child.setOnClickListener(v -> {
            onItemClick(position);
        });
    }

    /**
     * 点击事件
     * @param position
     */
    private void onItemClick(int position) {
        try {
            //设置新的now
            mNowTabPosition = position;
            //改变ui
            for (int i = 0; i < getChildCount(); i++) {
                TextView tv = (TextView) getChildAt(i);
                if (i != position) {
                    if (mBackgroundNR <= 0)
                        tv.setBackgroundColor(Color.TRANSPARENT);
                    else
                        tv.setBackgroundResource(mBackgroundNR);

                    if (mTextColorNR > 0)
                        tv.setTextColor(ResourcesUtils.getColor(mTextColorNR));
                } else {
                    if (mBackgroundNR <= 0)
                        tv.setBackgroundColor(Color.TRANSPARENT);
                    else
                        tv.setBackgroundResource(mBackgroundHR);

                    if (mTextColorHR > 0)
                        tv.setTextColor(ResourcesUtils.getColor(mTextColorHR));
                }
            }
            //回馈点击结果
            if (mTabOnChangeListener != null)
                mTabOnChangeListener.onChange(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTabOnChangeListener(TabOnChangeListener tabOnChangeListener) {
        this.mTabOnChangeListener = tabOnChangeListener;
    }

    public interface TabOnChangeListener {
        void onChange(int position);
    }

}
