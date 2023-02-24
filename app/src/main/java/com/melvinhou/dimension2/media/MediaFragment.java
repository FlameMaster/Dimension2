package com.melvinhou.dimension2.media;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.media.picture.ScanActivity;
import com.melvinhou.medialibrary.video.ijk.IjkVideoActivity;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.GlobalParameters;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FgtMediaBD;
import com.melvinhou.dimension2.media.animation.SvgAnimationActivity;
import com.melvinhou.dimension2.media.animation.SystemAnimationActivity;
import com.melvinhou.dimension2.media.music.MusicListActivity;
import com.melvinhou.dimension2.media.picture.AlbumActivity;
import com.melvinhou.dimension2.media.picture.CameraActivity;
import com.melvinhou.dimension2.media.picture.IllustrationPager;
import com.melvinhou.dimension2.media.picture.PictureActivity;
import com.melvinhou.dimension2.media.tiktok.TiktokActivity;
import com.melvinhou.dimension2.media.video.TVListPager;
import com.melvinhou.dimension2.media.video.VideoActivity;
import com.melvinhou.dimension2.media.video.VideoActivity2;
import com.melvinhou.dimension2.media.video.VideoLivePager;
import com.melvinhou.dimension2.pager.BasePager;
import com.melvinhou.dimension2.pager.PagerActivity;
import com.melvinhou.kami.mvvm.DataBindingFragment;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringCompareUtils;
import com.melvinhou.kami.view.wiget.PhotoCutterView;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusClient;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
 * = 时 间：2020/7/1 0:0
 * <p>
 * = 分 类 说 明：媒体主页
 * ================================================
 */
public class MediaFragment extends DataBindingFragment<FgtMediaBD> {


    private MediaViewModel mediaViewModel;
    private MediaListAdapter mListAdapter;
    private float MIN_SCALE = 0.65f;//当前图片缩小比例
    //屏幕旋转监听的参数
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    //上一次的偏移量
    float lastFx = 0;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_media;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerSensor();
    }

    @Override
    public void onStop() {
        unregisterSensor();
        super.onStop();
    }

    @Override
    protected void initView() {
        getAct().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getViewDataBinding().list.setLayoutManager(new LinearLayoutManager(
                FcUtils.getContext(), LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                FcUtils.getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ResourcesUtils.getDrawable(R.drawable.ic_line_h24));
        getViewDataBinding().list.addItemDecoration(itemDecoration);

    }

    @Override
    protected void initListener() {
        mediaViewModel =
                ViewModelProviders.of(this).get(MediaViewModel.class);
        mListAdapter = new MediaListAdapter();
        getViewDataBinding().list.setAdapter(mListAdapter);
        getViewDataBinding().list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                RecyclerView.SCROLL_STATE_IDLE//静止没有滚动
//                RecyclerView.SCROLL_STATE_DRAGGING //外部拖拽
//                RecyclerView.SCROLL_STATE_SETTLING //自动滚动
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                //获取中心点
//                int middle = (int) (recyclerView.getY() + recyclerView.getHeight() / 2);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
//                    if (recyclerView.getLayoutManager().getPosition(group) >=mListAdapter.getHeaderSize())
                    ViewGroup group = (ViewGroup) recyclerView.getChildAt(i);
//                    int childMiddle = (int) (group.getY() + group.getHeight() / 2);
                    //获取相对位置
//                    int gap = Math.abs(middle - childMiddle);
//                    float fraction = gap * 1.0f / recyclerView.getWidth() / 2;
//                    scale(group, fraction);暂且不用，效果冲突
                    scroll(group, 0.3f);
                }
            }
        });

        mListAdapter.setOnTabClickListener(this::onTabItemClick);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initData() {
        getViewDataBinding().setModel(mediaViewModel);

        String[] imgUrls = {
                "https://b-ssl.duitang.com/uploads/item/201607/25/20160725121819_HUZLa.jpeg",
                "https://b-ssl.duitang.com/uploads/item/201609/09/20160909211923_UYcxs.jpeg",
                "https://b-ssl.duitang.com/uploads/item/201902/15/20190215232154_UcYG3.jpeg",
                "https://b-ssl.duitang.com/uploads/item/201607/25/20160725121819_HUZLa.jpeg",
                "https://b-ssl.duitang.com/uploads/item/201712/03/20171203071748_dTrca.jpeg",
                "https://i0.hdslb.com/bfs/article/1b89ce25e3410659c9258e18fadc6f5dfa0e2379.jpg",
                "https://b-ssl.duitang.com/uploads/item/201902/11/20190211200513_wjnxb.jpg",
                "https://b-ssl.duitang.com/uploads/item/201707/24/20170724133648_UhLZt.jpeg"};

        //初始化重力感应
        initGravitySener();


        Observable.create((ObservableOnSubscribe<ArrayList<MediaItemEntity>>) emitter -> {
            ArrayList<MediaItemEntity> entity = LoadUtils.getData(
                    LoadUtils.SOURCE_ASSETS, AssetsFileKey.MEDIA_LIST,
                    new TypeToken<CYEntity<ArrayList<MediaItemEntity>>>() {
                    });
            emitter.onNext(entity);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(list -> {
                    mListAdapter.clearData();
                    mListAdapter.addDatas(list);
                });
    }


/////////////////////////———————————————————自动滚动——————————————————————/////////////////////////

    /**
     * 构造初始化重力感应
     */
    private void initGravitySener() {
        mSensorManager = (SensorManager) FcUtils.getContext()
                .getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                //只需要重力传感器
                if (Sensor.TYPE_GRAVITY != event.sensor.getType()) return;

                //获取xy方向的偏移量
                float[] values = event.values;
                float x = values[0];
//                float y = values[1];
                float fx = x / 10;
                float dfx = fx - lastFx;
//                Log.d("手机重力感应监听", "x=" + x + "\tdfx=" + fx);
                lastFx = fx;
                scrollTabs(dfx);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    /**
     * 开始监听
     */
    public void registerSensor() {
        if (mSensorManager != null && mSensorEventListener != null && mSensor != null) {
//            mSensorManager.registerListener(mSensorEventListener, mSensor,
//                    SensorManager.SENSOR_DELAY_NORMAL);

            mSensorManager.registerListener(mSensorEventListener, mSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     * 停止监听
     */
    public void unregisterSensor() {
        if (mSensorManager != null && mSensorEventListener != null)
            mSensorManager.unregisterListener(mSensorEventListener);
    }


    /**
     * 条目横向自动滚动
     *
     * @param ratioX
     */
    private void scrollTabs(float ratioX) {
        for (int i = 0; i < getViewDataBinding().list.getChildCount(); i++) {
            RecyclerView listV = getViewDataBinding().list.getChildAt(i).findViewById(R.id.tabs);
            if (listV != null && listV.getVisibility() == View.VISIBLE) {
                int maxScrollGoods = DimenUtils.dp2px(200);
                if (listV.getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
                    listV.scrollBy((int) (ratioX * maxScrollGoods), 0);
            }
        }
    }


    /**
     * 滑动缩放效果
     *
     * @param child
     * @param fraction
     */
    private void scale(View child, float fraction) {
        float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(fraction));
        child.setScaleX(scaleFactor);
        child.setScaleY(scaleFactor);
    }

    /**
     * 视差位移效果
     *
     * @param group list的item
     * @param ratio 相对滑动比（建议0-1）
     */
    private void scroll(ViewGroup group, float ratio) {
        View img = group.getChildAt(0);
        //容器的高度
        int height = getViewDataBinding().list.getHeight();
        int[] location = new int[2];
        group.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
//        img.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
//        -height/2+getHeight()/2表示控件在屏幕中心的位置
        float dy = location[1] - DimenUtils.getActionBarSize() - DimenUtils.getStatusHeight()
                + group.getHeight() / 2 - height / 2;
        //添加滑动比例
        dy = dy * ratio;
        //不能超过阀值
        int maxScroll = img.getHeight() - group.getHeight();
        if (Math.abs(dy) > maxScroll) dy = Math.abs(dy) / dy * maxScroll;
//        Log.e("滑动ing...", "屏幕高度:" + height + "\t\t坐标:" + location[1] + "\t\t位移:" + dy);
//        view.layout(view.getLeft(), layoutParams[1]+dy, view.getRight(),layoutParams[3]+dy);
//        postInvalidate();
        img.setTranslationY(dy);
    }


    public void onTabItemClick(int level1Typ, int level2Typ) {
        if (GlobalParameters.MediaLevel1Type.PICTURE == level1Typ) {
            switch (level2Typ) {
                case GlobalParameters.MediaLevel2Type.PICTURE_OPEN_INPUT://打开图片链接
                    openLinkDialog();
                    break;
                case GlobalParameters.MediaLevel2Type.PICTURE_ALBUM:
                    openAlbum();
                    break;
                case GlobalParameters.MediaLevel2Type.PICTURE_ILLUSTRATION:
                    openIllustration();
                    break;
                case GlobalParameters.MediaLevel2Type.PICTURE_CUTTING:
                    openCuttingPicture();
                    break;
                case GlobalParameters.MediaLevel2Type.PICTURE_CAMERA:
                    openCamera();
                    break;
                case GlobalParameters.MediaLevel2Type.PICTURE_SCAN:
                    openSacn();
                    break;
            }
        } else if (GlobalParameters.MediaLevel1Type.VIDEO == level1Typ) {
            switch (level2Typ) {
                case GlobalParameters.MediaLevel2Type.VIDEO_SURFACE:
                    openSurfaceVideo();
                    break;
                case GlobalParameters.MediaLevel2Type.VIDEO_TEXTURE:
                    openTextureVideo();
                    break;
                case GlobalParameters.MediaLevel2Type.VIDEO_IJK:
                    openIjkVideo();
                    break;
                case GlobalParameters.MediaLevel2Type.VIDEO_LIVE:
                    openVideoLive();
                    break;
                case GlobalParameters.MediaLevel2Type.VIDEO_TV:
                    openVideoTV();
                    break;
            }
        } else if (GlobalParameters.MediaLevel1Type.MUSIC == level1Typ) {
            if (GlobalParameters.MediaLevel2Type.MUSIC_LIST == level2Typ)
                openMusicList();
        } else if (GlobalParameters.MediaLevel1Type.ANIMATOR == level1Typ) {
            switch (level2Typ) {
                case GlobalParameters.MediaLevel2Type.ANIMATOR_PROPERTY:
                    openSystemAnimation();
                    break;
                case GlobalParameters.MediaLevel2Type.ANIMATOR_INTERACTION:
                    break;
                case GlobalParameters.MediaLevel2Type.ANIMATOR_SVG:
                    openSvgAnimation();
                    break;
                case GlobalParameters.MediaLevel2Type.ANIMATOR_OTHER:
                    break;
            }

        } else if (GlobalParameters.MediaLevel1Type.TIKTOK == level1Typ) {
            openTiktok();
        }
    }

    private void openCamera() {
        toActivity(new Intent(FcUtils.getContext(), CameraActivity.class));
    }


    private void openSacn() {
        toActivity(new Intent(FcUtils.getContext(), ScanActivity.class));
    }

    private void openPagerActivity(String title, BasePager[] pagers) {
        Intent intent = new Intent(FcUtils.getContext(), PagerActivity.class);
        intent.putExtra("title", title);

        new RxBusClient(RxBusClient.getClientId(getClass().getName())) {
            @Override
            protected void onEvent(@NonNull String eventType, Object attach) {
                //发送
                RxBus.instance().post(RxBusMessage.Builder
                        .instance(":{Pager}init")
                        .client(getClass().getName())
                        .build());
                cancel();
            }
        };
        toActivity(intent);
    }

    private void openAlbum() {
        Intent intent = new Intent(FcUtils.getContext(), AlbumActivity.class);
        toActivity(intent);
    }

    private void openIllustration() {
        openPagerActivity("插画", new BasePager[]{new IllustrationPager()});
    }

    private void openLinkDialog() {
        Dialog dialog = new Dialog(requireActivity(), R.style.Dimension2Dialog);
        dialog.setContentView(R.layout.dialog_open_link);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        //设置布局大小
        dialogWindow.setLayout(DimenUtils.getScreenSize()[0] - DimenUtils.dp2px(32),
                WindowManager.LayoutParams.WRAP_CONTENT);
        //设置整体大小包括外部半透明
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.width = DimenUtils.getScreenSize()[0] - DimenUtils.dp2px(16);
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        dialogWindow.setAttributes(params);
        //设置Dialog位置
        dialogWindow.setGravity(Gravity.CENTER);
        EditText edit = dialog.findViewById(R.id.edit);
        dialog.findViewById(R.id.submit).setOnClickListener(view -> {
            if (edit != null && StringCompareUtils.isImageFile(edit.getText().toString())) {
                dialog.dismiss();
                Intent intent = new Intent(FcUtils.getContext(), PictureActivity.class);
                intent.putExtra("url", edit.getText().toString());
                toActivity(intent);
            } else FcUtils.showToast("图片地址错误");
        });
        dialog.show();
    }

    private void openCuttingPicture() {
        Intent intent = new Intent(FcUtils.getContext(), PictureActivity.class);
        intent.putExtra("url", "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg");
        intent.putExtra("mode", PhotoCutterView.GESTURE_MODE_BOX);
        intent.putExtra("boxSize", DimenUtils.dp2px(300));
        intent.putExtra("boxColor", Color.parseColor("#80000000"));
        toActivity(intent);
    }


    private void openSurfaceVideo() {
        Intent intent = new Intent(FcUtils.getContext(), VideoActivity.class);
        intent.putExtra("dataSource", LoadUtils.SOURCE_ASSETS);//资源位置
        intent.putExtra("dataPath", AssetsFileKey.MEDIAT_MOVIE_DOME);//本地数据位置
        toActivity(intent);
    }

    private void openTextureVideo() {
        Intent intent = new Intent(FcUtils.getContext(), VideoActivity2.class);
//        intent.putExtra("url", "https://webstatic.bh3.com/video/bh3.com/pv/CG_OP_1800.mp4");
        intent.putExtra("url", "https://uploadstatic.mihoyo.com/hk4e/upload/officialsites/202012/zhongli_gameplayPV_final_V3_fix.mp4");
        intent.putExtra("title", "岩王帝君-钟离");
        intent.putExtra("mode", false);
        toActivity(intent);
    }

    private void openIjkVideo() {
        Intent intent = new Intent(FcUtils.getContext(), IjkVideoActivity.class);
        intent.putExtra("url", "https://uploadstatic.mihoyo.com/hk4e/upload/officialsites/202012/zhongli_gameplayPV_final_V3_fix.mp4");
        intent.putExtra("title", "岩王帝君-钟离");
        intent.putExtra("mode", false);
        toActivity(intent);
    }

    private void openVideoLive() {
        openPagerActivity("直播", new BasePager[]{new VideoLivePager()});
    }

    private void openVideoTV() {
        openPagerActivity("电视台", new BasePager[]{new TVListPager()});
    }

    private void openMusicList() {
        Intent intent = new Intent(FcUtils.getContext(), MusicListActivity.class);
        toActivity(intent);
    }

    private void openSystemAnimation() {
        Intent intent = new Intent(FcUtils.getContext(), SystemAnimationActivity.class);
        toActivity(intent);
    }

    private void openSvgAnimation() {
        Intent intent = new Intent(FcUtils.getContext(), SvgAnimationActivity.class);
        toActivity(intent);

    }

    private void openTiktok() {
        Intent intent = new Intent(FcUtils.getContext(), TiktokActivity.class);
        toActivity(intent);
    }


}
