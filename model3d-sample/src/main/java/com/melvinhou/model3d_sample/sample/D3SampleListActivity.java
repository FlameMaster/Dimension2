package com.melvinhou.model3d_sample.sample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.kami.adapter.BindRecyclerAdapter;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.dialog.DialogCheckBuilder;
import com.melvinhou.model3d_sample.R;
import com.melvinhou.model3d_sample.databinding.ActivityD3SampleListBinding;
import com.melvinhou.model3d_sample.databinding.ItemD3SampleBinding;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/3 19:49
 * <p>
 * = 分 类 说 明：模型列表@Assets:sample_d3_list.json
 * ================================================
 */
public class D3SampleListActivity extends BindActivity<ActivityD3SampleListBinding, D3SampleModel> {
    @Override
    protected ActivityD3SampleListBinding openViewBinding() {
        return ActivityD3SampleListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<D3SampleModel> openModelClazz() {
        return D3SampleModel.class;
    }


    private final String TAG = D3SampleListActivity.class.getName();
    //权限列表：文件
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    private MyAdapter mAdapter;

    @Override
    protected void initView() {
        mModel = new ViewModelProvider(this).get(D3SampleModel.class);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    protected void initListener() {
        mBinding.list.setLayoutManager(new GridLayoutManager(getBaseContext(), 2));
        mAdapter = new MyAdapter();
        mBinding.list.setAdapter(mAdapter);
        mBinding.list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //设定底部边距为
                int height = DimenUtils.dp2px(10);
                outRect.set(0, 0, 0, height);
            }
        });

        mAdapter.setOnItemClickListener((viewHolder, position, data) -> {
            if (data.isDownload()) {
                toShowActivity(data);
            }else{
                downloadObj(data);
            }
        });
    }


    @Override
    protected void initData() {
        //数据监听
        mModel.list.observe(this, list -> {
            mAdapter.clearData();
            mAdapter.addDatas(list);
        });

        // 权限管理
        checkFileManagePermission();
    }

    /**
     * 检查权限
     */
    private void checkFileManagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                onPermissionGranted();
                return;
            }
            showCheckView(new DialogCheckBuilder("权限提醒",
                    "模型需要文件管理权限，是否授予权限？",
                    "授权", "取消") {
                @Override
                public void confirm() {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    toResultActivity(intent, result -> {
                        if (Environment.isExternalStorageManager())
                            onPermissionGranted();
                        else onPermissionCancel();
                    });
                }
                @Override
                public void cancel() {
                    onPermissionCancel();
                }
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(REQUIRED_PERMISSIONS)) {
                onPermissionGranted();
                return;
            }
            requestPermissions(REQUIRED_PERMISSIONS);
        } else {
            onPermissionGranted();
        }
    }


    @Override
    protected void onPermissionGranted() {
        mModel.loadList();
    }

    @Override
    protected void onPermissionCancel() {
        FcUtils.showToast("没有权限读取文件");
    }

    /**
     * 前往详情页
     *
     * @param data
     */
    protected void toShowActivity(@NonNull D3SampleEntity data) {
        Intent intent = new Intent(D3SampleListActivity.this, D3SampleActivity.class);
        intent.putExtra("objName", data.getFileName() + ".obj");
        intent.putExtra("objPath", data.getDirectoryPath());
        startActivity(intent);
    }


    /**
     * 下载
     *
     * @param data
     */
    private void downloadObj(D3SampleEntity data) {
        showCheckView(new DialogCheckBuilder("下载提醒", "是否下载[" + data.getTitle() + "]所属模型文件？", "下载", "取消") {
            @Override
            public void confirm() {
                showProcess("下载模型中...");
                mModel.downloadModelFile(data.getFileName(), data.getUrl(), file -> {
                    if (file == null) {
                        FcUtils.showToast("模型下载失败");
                        return;
                    }
                    showProcess("解压模型中...");
                    mModel.unZipObj(file, isSucceed -> {
                        hideProcess();
                        FcUtils.showToast(isSucceed ? "解压完成" : "解压失败");
                        //重新加载
                        mModel.loadList();
                    });
                });
            }
        });
    }

    /**
     * 列表
     */
    class MyAdapter extends BindRecyclerAdapter<D3SampleEntity, ItemD3SampleBinding> {
        @Override
        protected ItemD3SampleBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup parent) {
            return ItemD3SampleBinding.inflate(inflater, parent, false);
        }

        @Override
        protected void bindData(@NonNull ItemD3SampleBinding binding, int position, @NonNull D3SampleEntity data) {
            binding.tvName.setText(TextUtils.isEmpty(data.getTitle()) ? data.getFileName() : data.getTitle());
            binding.tvOther.setText(data.getExplain());
            Glide.with(FcUtils.getContext()).load(data.getCover()).apply(new RequestOptions().override(160, 240).placeholder(R.mipmap.img_d3_andy).error(R.mipmap.img_d3_andy).diskCacheStrategy(DiskCacheStrategy.ALL)).into(binding.ivImg);
            binding.tvDownload.setText(data.isDownload() ? "已下载" : "未下载");
            binding.tvDownload.setSelected(data.isDownload());
        }
    }

}
