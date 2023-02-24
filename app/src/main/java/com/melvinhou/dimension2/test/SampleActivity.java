package com.melvinhou.dimension2.test;

import android.net.Uri;

import com.melvinhou.dimension2.R;
import com.melvinhou.medialibrary.video.ijk.IjkVideoView;
import com.melvinhou.kami.view.activities.BaseActivity2;
import com.melvinhou.medialibrary.video.TimVideoView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/2/20 0020 13:58
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class SampleActivity extends BaseActivity2 {
    @Override
    protected int getLayoutID() {
        return R.layout.activity_sample;
    }

    @Override
    protected void initView() {
        String uri = "https://uploadstatic.mihoyo.com/hk4e/upload/officialsites/202012/zhongli_gameplayPV_final_V3_fix.mp4";
        uri = "https://webstatic.bh3.com/video/bh3.com/pv/CG_OP_1800.mp4";
        IjkVideoView videoView =findViewById(R.id.video);
        videoView.setVideoPath(uri);
        videoView.getController().start();

        TimVideoView videoView2 =findViewById(R.id.video2);
        videoView2.setVideoURI(Uri.parse(uri));
//        videoView2.start();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }
}
