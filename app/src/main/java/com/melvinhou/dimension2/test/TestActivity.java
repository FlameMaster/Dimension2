package com.melvinhou.dimension2.test;

import android.net.Uri;
import android.view.View;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.ijk.IjkVideoView;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.BaseActivity;
import com.melvinhou.kami.wiget.NestedPhotoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test1;
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
        listView.setAdapter(adapter);
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

    }


}
