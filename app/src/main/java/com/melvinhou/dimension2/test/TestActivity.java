package com.melvinhou.dimension2.test;

import android.os.Build;
import android.util.SparseArray;
import android.view.WindowManager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.melvinhou.dimension2.R;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.view.BaseActivity;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/16 2:39
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TestActivity extends BaseActivity {
    private SparseArray<BindFragment>  fragments =  new SparseArray();
    private TabLayoutMediator mediator = null;
    private String[] tabs = {"测试1", "测试2", "测试3", "测试4"};

    private ViewPager2 mViewPager;
    private TabLayout mIndicator;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void initWindowUI() {
        super.initWindowUI();
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            lp.layoutInDisplayCutoutMode
//                    = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        }
//        getWindow().setAttributes(lp);
    }


    @Override
    protected void initView() {
        mViewPager = findViewById(R.id.viewpager);
        mIndicator = findViewById(R.id.indicator);
    //禁用预加载
    //        binding.container.offscreenPageLimit = 4
        mViewPager.setUserInputEnabled(false);
        //fragment嵌套报错解决
        mViewPager.setSaveEnabled(false);
        //Adapter
        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @Override
            public int getItemCount() {
                return fragments.size();
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }
        });
        mediator = new TabLayoutMediator(mIndicator, mViewPager, (tab, position) -> {

        });
        //要执行这一句才是真正将两者绑定起来
        mediator.attach();

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        fragments.clear();
        mIndicator.removeAllTabs();
        for (int i=0;i<tabs.length;i++){
            mIndicator.addTab(mIndicator.newTab().setText(tabs[i]));
        }
        fragments.put(0,new TestFragment01());
        fragments.put(1,new TestFragment02());
        fragments.put(2,new TestFragment03());
        fragments.put(3,new TestFragment04());
    }


}
