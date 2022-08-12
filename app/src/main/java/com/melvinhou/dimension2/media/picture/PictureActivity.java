package com.melvinhou.dimension2.media.picture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActPictureBD;
import com.melvinhou.kami.mvvm.DataBindingActivity;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.wiget.PhotoCutterView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/10 3:50
 * <p>
 * = 分 类 说 明：图片查看
 * ================================================
 */
public class PictureActivity extends DataBindingActivity<ActPictureBD> {

    //文件查找格式
    public static final String FILE_UNSPECIFIED = "*/*";
    //图片查找格式
    public static final String IMAGE_UNSPECIFIED = "image/*";
    //文件
    public static final int FILE_REQUEST_CODE = 0;
    //相册
    public static final int ALBUM_REQUEST_CODE = 1;
    //相机
    public static final int CAMERA_REQUEST_CODE = 2;

    private String url;

    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        int statusColor = 0x40ffffff;
        getWindow().setStatusBarColor(statusColor);
        getWindow().setNavigationBarColor(statusColor);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            event.startTracking();//这个会执行过渡动画
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish()不会执行动画
                finishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_picture;
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initListener() {
        getViewDataBinding().tools.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePhoto();
            }
        });
    }

    @Override
    protected void initData() {
        url = getIntent().getStringExtra("url");
        RequestOptions options = new RequestOptions();
//        Glide.with(FcUtils.getContext()).asBitmap().load(url).apply(options).into(getViewDataBinding().photo);

        getViewDataBinding().setUrl(url);
        //图片查看模式
        int mode = getIntent().getIntExtra("mode", PhotoCutterView.GESTURE_MODE_INFINITE);
        getViewDataBinding().photo.setGestureMode(mode);
        if (mode == PhotoCutterView.GESTURE_MODE_BOX) {
            getViewDataBinding().photo.setCenterCheckBox(
                    getIntent().getIntExtra("boxSize",DimenUtils.dp2px(300)));
            getViewDataBinding().photo.setCheckBoxColor(
                    getIntent().getIntExtra("boxColor",0x80000000));
        }
    }

    @Override
    protected void onLoading() {
//        super.onLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "requestCode=" + requestCode);
        if (resultCode == Activity.RESULT_OK) {//成功加载
            if (requestCode == 0 || requestCode == 1 || requestCode == 2) {
                updatePhoto(data.getData());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *
     */
    public void updatePhoto() {
        Intent intent = new Intent();
        // 开启Pictures画面Type设定为image
        intent.setType(IMAGE_UNSPECIFIED);
        // 使用Intent.ACTION_GET_CONTENT这个Action
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // 取得相片后返回本画面
        startActivityForResult(intent, ALBUM_REQUEST_CODE);
    }


    private void updatePhoto(Uri uri) {

        getViewDataBinding().photo.setImageURI(uri);
    }

}
