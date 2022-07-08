package com.melvinhou.dimension2.function.desktop;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.music.BlurTransformation;
import com.melvinhou.dimension2.utils.PinYinUtil;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/7/6 0006 13:19
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class DesktopActivity extends BaseActivity {

    private View mRoot;
    private ImageView mBackground;
    private ViewPager2 mViewPager;
    private MyPageAdapter mPageAdapter;
    //多少列
    private final int column = 5;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_desktop;
    }

    @Override
    protected void initView() {
        mRoot = findViewById(R.id.root);
        mBackground = findViewById(R.id.iv_bg);
        mViewPager = findViewById(R.id.container);
    }

    @Override
    protected void initListener() {
        //设置缓存页数
        mViewPager.setOffscreenPageLimit(2);
        //是否可滑动
        mViewPager.setUserInputEnabled(true);
        mPageAdapter = new MyPageAdapter();
        mViewPager.setAdapter(mPageAdapter);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initData() {
        //高斯模糊
        MultiTransformation mation4 = new MultiTransformation(new BlurTransformation(50));
        Glide.with(FcUtils.getContext())
                .load("http://reabk0s12.hb-bkt.clouddn.com/Ciyuan2/app/image/background/baskground001.jpg")
                .apply(new RequestOptions()
                        .transform(mation4)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(mBackground);
        Observable
                .create((ObservableOnSubscribe<List<AppInfo>>) emitter -> {
                    List<AppInfo> allList = getAppList();
                    Collections.sort(allList, comparator);//排序
                    List<AppInfo> entity = null;
                    for (int i = 0; i < allList.size(); i++) {
                        if (i % 25 == 0)//初始化
                            entity = new ArrayList<>();
                        entity.add(allList.get(i));
                        if (i % 25 == 24 || i == allList.size() - 1)//提交一页
                            emitter.onNext(entity);
                    }
                    emitter.onComplete();
                })
                .compose(IOUtils.setThread())
                .subscribe(entity -> {
                    mPageAdapter.addData(entity);
                });
    }

    Comparator<AppInfo> comparator = new Comparator<AppInfo>() {
        @Override
        public int compare(AppInfo o1, AppInfo o2) {
            return o1.getEnName().compareTo(o2.getEnName());
        }
    };

    /**
     * 获取APP列表
     *
     * @return
     */
    private List<AppInfo> getAppList() {
        List<AppInfo> list = new ArrayList<>();
        PackageManager pm = FcUtils.getContext().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL);
        for (ResolveInfo info : activities) {
            String packName = info.activityInfo.packageName;
            //去掉桌面应用
            if (packName.equals(FcUtils.getContext().getPackageName())) {
                continue;
            }
            //自定义对象，包含了App的4个属性
            AppInfo mInfo = new AppInfo();
            mInfo.setIcon(info.activityInfo.applicationInfo.loadIcon(pm));
            String name = info.activityInfo.applicationInfo.loadLabel(pm).toString();
            mInfo.setName(name);
            mInfo.setEnName(PinYinUtil.getPinyin(name));
            mInfo.setPackageName(packName);
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(packName,
                    info.activityInfo.name));
            mInfo.setIntent(launchIntent);
            list.add(mInfo);
        }
        return list;
    }

    private void launchApp(String packageName) {
        Intent intent = FcUtils.getContext().getPackageManager()
                .getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            FcUtils.getContext().startActivity(intent);
        }
    }

    private void unloadApp(String packageName) {
        try {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 1);
        } catch (Exception e) {
            e.printStackTrace();
            FcUtils.showToast("卸载失败");
        }
    }

    private void setting2App(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", packageName, null));
        startActivity(intent);
    }

    private PopupWindow popupWindow;

    private void showPopup(int x, int y, String packageName) {
        View popup = View.inflate(this, R.layout.popup_desktop_select, null);
        popupWindow = new PopupWindow(popup, DimenUtils.dp2px(56), DimenUtils.dp2px(82));
        popupWindow.setAnimationStyle(R.style.TopDialogAnimation);
        // 设置PopupWindow是否能响应外部点击事件
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        popupWindow.setOnDismissListener(() -> {

        });
        popup.findViewById(R.id.tv_setting).setOnClickListener(v -> {
            setting2App(packageName);
            popupWindow.dismiss();
        });
        popup.findViewById(R.id.tv_unload).setOnClickListener(v -> {
            unloadApp(packageName);
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(mRoot, Gravity.NO_GRAVITY, x, y);
    }


    class MyPageAdapter extends RecyclerAdapter<List<AppInfo>, MyHolder> {
        private MyAdapter mAdapter;
        private RecyclerView mList;

        @Override
        public void bindData(MyHolder viewHolder, int position, List<AppInfo> list) {
            mAdapter.clearData();
            mAdapter.addDatas(list);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_desktop_page;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            MyHolder holder = new MyHolder(View);
            mList = View.findViewById(R.id.list);
            mList.setLayoutManager(new GridLayoutManager(FcUtils.getContext(), 5));
            mAdapter = new MyAdapter();
            mList.setAdapter(mAdapter);
            return holder;
        }
    }


    class MyAdapter extends RecyclerAdapter<AppInfo, MyHolder> {
        @Override
        public void bindData(MyHolder viewHolder, int position, AppInfo data) {
            viewHolder.update(data.getIcon(), data.getName());
            viewHolder.itemView.setOnLongClickListener(v -> {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                boolean isRightShow = position % column < column / 2;
                int x = isRightShow ? v.getWidth() + location[0] : location[0] - v.getWidth();
                int y = location[1];
                showPopup(x, y, data.getPackageName());
                return true;
            });
            //启动app
            viewHolder.itemView.setOnClickListener(v -> launchApp(data.getPackageName()));
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_desktop;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            return new MyHolder(View);
        }
    }

    static class MyHolder extends RecyclerHolder {
        ImageView imageView;
        TextView textView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.title);
        }

        public void update(Drawable icon, String name) {
            imageView.setImageDrawable(icon);
            textView.setText(name);
        }

    }
}
