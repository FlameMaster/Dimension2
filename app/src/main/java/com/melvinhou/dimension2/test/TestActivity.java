package com.melvinhou.dimension2.test;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActivityTestBinding;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.activities.BaseActivity;

import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
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
 * = 分 类 说 明：测试页面，从test_navigation添加页面
 * ================================================
 */
public class TestActivity extends BindActivity<ActivityTestBinding, BaseViewModel> {
    @Override
    protected ActivityTestBinding openViewBinding() {
        return ActivityTestBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    private NavController navController;

    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        //沉浸状态栏
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        //状态栏和导航栏颜色
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
        }
        //设置沉浸后专栏栏和导航字体的颜色，
        WindowInsetsControllerCompat controllerCompat = WindowCompat.getInsetsController(getWindow(), mBinding.getRoot());
        assert controllerCompat != null;
        controllerCompat.setAppearanceLightStatusBars(true);
        controllerCompat.setAppearanceLightNavigationBars(true);
    }

    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("实验室");
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.test_navigation);
        navController.getGraph().setStartDestination(R.id.test_start);
        //关联起来
//        AppBarConfiguration appBarConfiguration =
//                new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupWithNavController(mBinding.barRoot.bar, navController, appBarConfiguration);

        mBinding.barRoot.getRoot().setVisibility(View.GONE);
        mBinding.indicator.setVisibility(View.GONE);
    }

    @Override
    protected void initListener() {
        //关联tablayout
        SparseArrayCompat<NavDestination> nodes = navController.getGraph().getNodes();
        mBinding.indicator.removeAllTabs();
        for (int i = 0; i < nodes.size(); i++) {
            NavDestination destination = nodes.valueAt(i);
            TabLayout.Tab tab = mBinding.indicator.newTab()
                    .setText(destination.getLabel());
            tab.view.setOnClickListener(v -> {
                navController.navigate(destination.getId());
            });
            mBinding.indicator.addTab(tab);

            //绑定侧边栏
            mBinding.navView.getMenu().add(0, destination.getId(), 0, destination.getLabel())
                    .setOnMenuItemClickListener(item -> {
                        navController.navigate(item.getItemId());
                        mBinding.drawerLayout.close();
                        return true;
                    });
        }
    }
}
