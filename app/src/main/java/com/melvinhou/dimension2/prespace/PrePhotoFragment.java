package com.melvinhou.dimension2.prespace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.d3.D3Entity;
import com.melvinhou.dimension2.databinding.ActivityListBinding;
import com.melvinhou.dimension2.databinding.FragmentSpacePreBinding;
import com.melvinhou.dimension2.databinding.ItemAlbumBinding;
import com.melvinhou.dimension2.databinding.ItemIllustrationBD;
import com.melvinhou.dimension2.databinding.ItemIllustrationBDImpl;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.test.TestActivity;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.kami.adapter.BindRecyclerAdapter;
import com.melvinhou.kami.adapter.BindViewHolder;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.shinichi.library.ImagePreview;
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
 * = 时 间：2022/8/11 0011 16:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class PrePhotoFragment extends BindFragment<ActivityListBinding, SpacePreModel> {
    @Override
    protected ActivityListBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return ActivityListBinding.inflate(inflater, container, false);
    }

    @Override
    protected Class<SpacePreModel> openModelClazz() {
        return SpacePreModel.class;
    }

    //获取单例
    private static PrePhotoFragment fragment;

    public static PrePhotoFragment getInstance() {
        if (fragment == null)
            fragment = new PrePhotoFragment();
        return fragment;
    }

    private MyAdapter mAdapter;
    //列数
    private final int spanCount = 3;

    @Override
    protected void initView() {
//        mBinding.container.setBackgroundColor(Color.parseColor("#60000000"));
        mBinding.barRoot.getRoot().setVisibility(View.GONE);
        mAdapter = new MyAdapter();
        mBinding.container.setAdapter(mAdapter);
        mBinding.container.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        int decoration = DimenUtils.dp2px(12);
        mBinding.container.setPadding(decoration / 2, 0, 0, decoration / 2);
        mBinding.container.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect,
                                       @NonNull View view,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
//                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                int left = decoration / 2, top = 0, right = decoration / 2, bottom = decoration;
                //设定边距为
                if (position / spanCount == 0) top = decoration;
                outRect.set(left, top, right, bottom);
            }
        });
    }

    @Override
    protected void initListener() {
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener<String, BindViewHolder<ItemAlbumBinding>>() {
            @Override
            public void onItemClick(BindViewHolder<ItemAlbumBinding> viewHolder, int position, String data) {
                ImagePreview
                        .getInstance()
                        // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                        .setContext(getAct())
                        // 设置从第几张开始看（索引从0开始）
                        .setIndex(0)
                        //=================================================================================================
                        // 有三种设置数据集合的方式，根据自己的需求进行三选一：
                        // 1：第一步生成的imageInfo List
//                      .setImageInfoList(imageInfoList)
                        // 2：直接传url List
                        .setImageList(mAdapter.getDatas())
                        // 3：只有一张图片的情况，可以直接传入这张图片的url
                        //.setImage(String image)
                        //=================================================================================================
                        .setEnableClickClose(false)
                        .setEnableDragClose(true)
                        .setEnableUpDragClose(true)
                        .setIndex(position)
                        .setLoadStrategy(ImagePreview.LoadStrategy.NetworkAuto)
                        .setZoomTransitionDuration(500)
                        // 开启预览
                        .start();
            }
        });
    }


    @SuppressLint("CheckResult")
    @Override
    protected void initData() {

        Observable.create((ObservableOnSubscribe<String[]>) emitter -> {
            String text = LoadUtils.readAssetsTxt(AssetsFileKey.MEDIAT_ALBUM_LIST);
            text = text
                    .replaceAll(" ", "")
                    .replaceAll("\r|\n", "");
            String[] datas = text.split(",");
            emitter.onNext(datas);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(datas -> {
                    if (datas != null) {
//                        mAdapter.clearDatas();
                        for (String data : datas)
                            mAdapter.addData(data);
                    }
                });

        Log.e("位置", "getExternalStorageDirectory=" + Environment.getExternalStorageDirectory().getPath());
        Log.e("位置", "getDataDirectory=" + Environment.getDataDirectory().getPath());
        Log.e("位置", "getDownloadCacheDirectory=" + Environment.getDownloadCacheDirectory().getPath());
        Log.e("位置", "getRootDirectory=" + Environment.getRootDirectory().getPath());
        Log.e("位置", "getFilesDir=" + FcUtils.getContext().getFilesDir().getPath());
        Log.e("位置", "getCacheDir=" + FcUtils.getContext().getCacheDir().getPath());
        Log.e("位置", "getDir=" + FcUtils.getContext().getDir("233", Context.MODE_PRIVATE).getPath());
        Log.e("位置", "getExternalCacheDir=" + FcUtils.getContext().getExternalCacheDir().getPath());
        Log.e("位置", "getStorageDirectory=" + Environment.getExternalStorageDirectory().getPath());
        Log.e("位置", "dowloads=" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
        //本地
        loadList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + File.separator + "Android"+File.separator+"data"+File.separator+"");
        loadList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
    }


    private void loadList(String path){
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (dir.isDirectory()) {// 处理目录
            File[] files = dir.listFiles();
            for (File file : files) {
                mAdapter.addData(file.getAbsolutePath());
            }
        }
    }

    class MyAdapter extends BindRecyclerAdapter<String, ItemAlbumBinding> {

        @Override
        protected ItemAlbumBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup parent) {
            return ItemAlbumBinding.inflate(inflater, parent, false);
        }

        @Override
        protected void bindData(@NonNull ItemAlbumBinding binding, int position, @NonNull String data) {
            Glide.with(FcUtils.getContext()).load(data).into(binding.itemImg);
        }
    }
}
