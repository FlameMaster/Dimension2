package com.melvinhou.test;

import android.graphics.Color;
import android.os.Build;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActivityTestBinding;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindActivity;

import androidx.collection.SparseArrayCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

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
    protected void initView() {
        mBinding.barRoot.title.setText("实验室");
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.nav_test);
//        navController.getGraph().setStartDestination(R.id.nav_test_start);
        //关联起来
//        AppBarConfiguration appBarConfiguration =
//                new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupWithNavController(mBinding.barRoot.bar, navController, appBarConfiguration);

        mBinding.barRoot.getRoot().setVisibility(View.GONE);
    }

    @Override
    protected void initListener() {
        //关联tablayout
        SparseArrayCompat<NavDestination> nodes = navController.getGraph().getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            NavDestination destination = nodes.valueAt(i);
            //绑定侧边栏
            mBinding.navView.getMenu()
                    .add(0, destination.getId(), 0, destination.getLabel())
                    .setOnMenuItemClickListener(item -> {
                        navController.navigate(item.getItemId());
                        mBinding.drawerLayout.close();
                        return true;
                    });
        }
    }
}
