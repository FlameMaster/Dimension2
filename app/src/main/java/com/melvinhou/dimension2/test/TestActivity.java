package com.melvinhou.dimension2.test;

import android.net.Uri;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.ijk.IjkVideoView;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.BaseActivity;
import com.melvinhou.kami.wiget.NestedPhotoView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import cc.shinichi.library.ImagePreview;

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


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test1;
    }

    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowManager.LayoutParams lp =getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode
                    =WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(lp);
    }


    @Override
    protected void initView() {
        ViewPager2 viewPager2 = findViewById(R.id.viewpager);
        RecyclerView listView = findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(FcUtils.getContext(),LinearLayoutManager.HORIZONTAL,false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        RecyclerAdapter adapter = new RecyclerAdapter<String,RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
                NestedPhotoView view = (NestedPhotoView) viewHolder.itemView;
                String url = "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg";
//                view.setImageURI(Uri.parse(url));
        Glide.with(FcUtils.getContext()).asBitmap().load(url).into(view);
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_photo;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
//        viewPager2.setAdapter(adapter);
//        listView.setAdapter(adapter);
        adapter.addData("");
        adapter.addData("");
        adapter.addData("");
        adapter.addData("");
        adapter.addData("");
        adapter.addData("");
        adapter.notifyDataSetChanged();

    }

    void test(){

        TestView testView = findViewById(R.id.test);
        testView.setOnClickListener(v -> testView.start());

        IjkVideoView videoView = findViewById(R.id.video);
        videoView.setVideoPath("https://uploadstatic.mihoyo.com/hk4e/upload/officialsites/202012/zhongli_gameplayPV_final_V3_fix.mp4");
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        String url = "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg";

        List<String> list = new ArrayList<>();
        for (int i=0 ;i<10; i++)
            list.add(url);
        ImagePreview
                .getInstance()
                // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                .setContext(TestActivity.this)

                // 设置从第几张开始看（索引从0开始）
                .setIndex(0)

                //=================================================================================================
                // 有三种设置数据集合的方式，根据自己的需求进行三选一：
                // 1：第一步生成的imageInfo List
//                .setImageInfoList(imageInfoList)

                // 2：直接传url List
                .setImageList(list)

                // 3：只有一张图片的情况，可以直接传入这张图片的url
                //.setImage(String image)
                //=================================================================================================

                .setEnableClickClose(false)
                .setEnableDragClose(true)
                .setEnableUpDragClose(true)
                .setIndex(1)
                .setLoadStrategy(ImagePreview.LoadStrategy.NetworkAuto)
                .setZoomTransitionDuration(500)
                // 开启预览
                .start();

        // 默认配置为：
        //      显示顶部进度指示器、
        //      显示右侧下载按钮、
        //      隐藏关闭按钮、
        //      开启点击图片关闭、
        //      开启下拉图片关闭、
        //      加载策略为全自动

    }


}
