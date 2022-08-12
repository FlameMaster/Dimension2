package com.melvinhou.dimension2.prespace;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.SparseArray;

import com.melvinhou.dimension2.databinding.ActivityViewpagerBinding;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
 * = 时 间：2022/8/11 0011 16:17
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class SpacePreActivity extends BindActivity<ActivityViewpagerBinding, SpacePreModel> {
    @Override
    protected ActivityViewpagerBinding openViewBinding() {
        return ActivityViewpagerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<SpacePreModel> openModelClazz() {
        return SpacePreModel.class;
    }

    //权限请求
    public static final int REQUEST_CODE_PERMISSIONS = 2113;
    public static final int REQUEST_CODE_PERMISSIONS2 = 2114;
    //权限列表：文件
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    //
    public static final int PAGE_PHOTO = 0;

    private SparseArray<BindFragment> fragments = new SparseArray();

    @Override
    protected void initView() {
        //禁用预加载
        mBinding.container.setUserInputEnabled(false);
        mBinding.container.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);


        mBinding.container.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });
    }

    @Override
    protected void initListener() {
        mModel.page.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                mBinding.container.setCurrentItem(integer, false);
            }
        });
    }

    @Override
    protected void initData() {
        //权限申请
        if (allPermissionsGranted(REQUIRED_PERMISSIONS)) {
            loadData();
        }else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    private void loadData() {
        fragments.put(PAGE_PHOTO, PrePhotoFragment.getInstance());
        mModel.page.postValue(PAGE_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK && resultCode == REQUEST_CODE_PERMISSIONS2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())
                FcUtils.showToast("获取权限成功");
            else FcUtils.showToast("未能获取权限");
        }
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            loadData();
    }

    @Override
    protected void onPermissionCancel(int requestCode) {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            FcUtils.showToast("没有权限读取文件");
    }
}
