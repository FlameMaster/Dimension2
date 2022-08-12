package com.melvinhou.dimension2;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;

import com.melvinhou.dimension2.databinding.ActMainBD;
import com.melvinhou.kami.mvvm.DataBindingActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/1 0:0
 * <p>
 * = 分 类 说 明：主页
 * ================================================
 */
public class MainActivity extends DataBindingActivity<ActMainBD> {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController mNavController;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        setSupportActionBar(getViewDataBinding().bar);
        //显示自带title
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        DrawerArrowDrawable drawable= new DrawerArrowDrawable(
                getDelegate().getSupportActionBar().getThemedContext());
//        getSupportActionBar().setIcon(drawable);
        getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    @Override
    protected void initListener() {
        //绑定
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //id用于匹配当前点击位置
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_media, R.id.navigation_function)
                .build();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_gallery, R.id.navigation_slideshow)
                .setDrawerLayout(getViewDataBinding().drawerLayout)
                .build();
//        NavigationUI.setupActionBarWithNavController(this, mNavController, appBarConfiguration);
        //为了不执行AbstractAppBarOnDestinationChangedListener的setNavigationIcon
        mNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            CharSequence label = destination.getLabel();
            if (!TextUtils.isEmpty(label)) {
                // Fill in the data pattern with the args to build a valid URI
                StringBuffer title = new StringBuffer();
                Pattern fillInPattern = Pattern.compile("\\{(.+?)\\}");
                Matcher matcher = fillInPattern.matcher(label);
                while (matcher.find()) {
                    String argName = matcher.group(1);
                    if (arguments != null && arguments.containsKey(argName)) {
                        matcher.appendReplacement(title, "");
                        //noinspection ConstantConditions
                        title.append(arguments.get(argName).toString());
                    } else {
                        throw new IllegalArgumentException("Could not find " + argName + " in "
                                + arguments + " to fill label " + label);
                    }
                }
                matcher.appendTail(title);
                setTitle(title);
            }

            /*
            boolean isTopLevelDestination = NavigationUI.matchDestinations(destination,
                    appBarConfiguration.getTopLevelDestinations());
            if (drawerLayout == null && isTopLevelDestination) {
                setNavigationIcon(null, 0);
            } else {
                setActionBarUpIndicator(drawerLayout != null && isTopLevelDestination);
            }
            */
        });
        NavigationUI.setupWithNavController(getViewDataBinding().btnNavView, mNavController);
//        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(getViewDataBinding().navView, mNavController);

    }

    @Override
    protected void initData() {
    }


    @Override
    public boolean onSupportNavigateUp() {
//        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
        return super.onSupportNavigateUp();
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getViewDataBinding().drawerLayout.openDrawer(Gravity.START, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onLoading() {
//        super.onLoading();

    }

}
