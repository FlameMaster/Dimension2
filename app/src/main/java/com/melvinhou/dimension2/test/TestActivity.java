package com.melvinhou.dimension2.test;

import android.view.View;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.ijk.IjkVideoView;
import com.melvinhou.kami.view.BaseActivity;

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
        return R.layout.activity_test;
    }

    @Override
    protected void initView() {

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
