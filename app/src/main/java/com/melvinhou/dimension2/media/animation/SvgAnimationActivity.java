package com.melvinhou.dimension2.media.animation;

import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;
import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/20 19:28
 * <p>
 * = 分 类 说 明：svg动画
 * ================================================
 */
public class SvgAnimationActivity extends BaseActivity {

    private String[] tabs = new String[]{
            "外观变换",
            "颜色变换",
            "路径线"
    };
    private int[] tabLayouts = new int[]{
            R.layout.vp_animation_system,
            R.layout.vp_animation_system,
            R.layout.vp_animation_system
    };


    private ViewPager container;
    private TabLayout indicator;
    private MyAdapter myAdapter;


    @Override
    public int getLayoutID() {
        return R.layout.activity_animation_system;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.activity_slide_out_top);
    }

    @Override
    protected void initWindowUI() {
        overridePendingTransition(R.anim.activity_slide_in_top,0);
//        super.initWindowUI();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setNavigationBarColor(Color.WHITE);
    }

    @Override
    protected void initView() {
        container = findViewById(R.id.container);
        indicator = findViewById(R.id.indicator);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("SVG动画");
    }

    @Override
    protected void initListener() {
        indicator.setupWithViewPager(container, true);//viewpager内容改变时是否刷新
        myAdapter = new MyAdapter();
        container.setAdapter(myAdapter);

    }

    @Override
    protected void initData() {
        container.setOffscreenPageLimit(5);//缓存页数，0无效
        container.setCurrentItem(0);

    }

    class MyAdapter extends PagerAdapter {

        List<View> mViews = new ArrayList<>();

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }


        /*初始化内容*/
        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getItemView(position);
            container.addView(view);
            return view;
        }

        /*清理缓存之外的*/
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(getItemView(position));
        }

        /**
         * 获取pager的布局
         *
         * @param position
         * @return
         */
        public View getItemView(int position) {
            if (position < mViews.size() && mViews.get(position) != null)
                return mViews.get(position);
            else {
                createLayout(position);
                return getItemView(position);
            }
        }

        /**
         * 初始化
         *
         * @param position
         */
        private void createLayout(int position) {
            View view = View.inflate(FcUtils.getContext(), tabLayouts[position], null);
            mViews.add(position, view);
            //初始化各个条目
            LinearLayout show = view.findViewById(R.id.show);
            GridLayout inputs = view.findViewById(R.id.inputs);
            if (position == 0) {
                initTextAnimation(show, inputs);
            } else if (position == 1) {
                initColorAnimation(show, inputs);
            } else if (position == 2) {
                initPathAnimation(show, inputs);
            }
        }
    }

    private void initPathAnimation(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        View child = new View(FcUtils.getContext());
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(240), DimenUtils.dp2px(240)));
        child.setBackgroundResource(R.drawable.av_svg_dome_path);
        show.addView(child);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable avDrawable = child.getBackground();
                if (avDrawable instanceof Animatable){
                    ((Animatable) avDrawable).start();
                }
            }
        });

    }

    private void initColorAnimation(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        View child = new View(FcUtils.getContext());
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(240), DimenUtils.dp2px(240)));
        child.setBackgroundResource(R.drawable.av_svg_dome_color);
        show.addView(child);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable avDrawable = child.getBackground();
                if (avDrawable instanceof Animatable){
                    ((Animatable) avDrawable).start();
                }
            }
        });
    }

    private void initTextAnimation(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        View child = new View(FcUtils.getContext());
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(240), DimenUtils.dp2px(240)));
        child.setBackgroundResource(R.drawable.av_svg_dome_text);
        show.addView(child);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable avDrawable = child.getBackground();
                if (avDrawable instanceof Animatable){
                    ((Animatable) avDrawable).start();
                }
            }
        });

    }

    /**
     * 添加一个按钮
     *
     * @param inputs
     * @param text
     * @param clickListener
     */
    private void addButton(GridLayout inputs, String text, View.OnClickListener clickListener) {
        Button button = new Button(FcUtils.getContext());
        button.setText(text);
        button.setTextColor(Color.BLACK);
        button.setClickable(true);
        button.setBackgroundResource(R.drawable.ripple_down);
        button.setOnClickListener(clickListener);
        inputs.addView(button);
    }



}