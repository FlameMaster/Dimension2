package com.melvinhou.dimension2.media.tiktok;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.VideoDecoder;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.databinding.ItemTiktokBD;
import com.melvinhou.dimension2.media.video.FCVidoeView;
import com.melvinhou.kami.adapter.DataBindingHolder;
import com.melvinhou.kami.util.FcUtils;

import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/22 22:21
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TikTokHolder extends DataBindingHolder<ItemTiktokBD> {

    public TikTokHolder(ItemTiktokBD binding) {
        super(binding);
        getBinding().tv.setInitDisplayerType(FCVidoeView.TYPE_DISPLAYER_CROP);
    }

    /**
     * 刷新数据
     *
     * @param data
     */
    public void updateData(TiktokEntity data) {
        getBinding().tv.setVideoURI(Uri.parse(data.getUrl()));

        /*
        //加载网络图片慢需要3级缓存
        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {

            Bitmap bitmap
                    = ImageUtils.decodeVideoThumbnail(path,0);

            emitter.onNext(bitmap);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(bitmap -> {
                    if (bitmap!=null)
                        viewHolder.getBinding().cover.setImageBitmap(bitmap);

                });
        */

        decodeVideoThumbnail(getBinding().cover, data.getUrl());
    }

    /**
     * 使用Glide加载视频第一帧的方法
     *
     * @param view
     * @param videoUrl
     */
    @SuppressLint("CheckResult")
    public static void decodeVideoThumbnail(ImageView view, String videoUrl) {
        RequestOptions requestOptions = RequestOptions.frameOf(0);
        requestOptions.set(VideoDecoder.FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST);
        requestOptions.transform(new BitmapTransformation() {
            @Override
            protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                return toTransform;
            }

            @Override
            public void updateDiskCacheKey(MessageDigest messageDigest) {
                try {
                    messageDigest.update((FcUtils.getContext().getPackageName() + "RotateTransform").getBytes("utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Glide.with(FcUtils.getContext())
                .load(videoUrl)
                .apply(requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(view);
    }

    /**
     * 播放
     */
    public void play() {
        if (getBinding().tv.getCurrentState() != FCVidoeView.STATE_PLAYING) {
            getBinding().tv.start();
            showPlayButton(false);
        }
    }

    public void pause(){

        getBinding().tv.pause();
        showPlayButton(true);
    }

    public void switchVideoState() {
        if (getBinding().tv.getCurrentState() != FCVidoeView.STATE_PLAYING) {
            play();
        } else {
            pause();
        }
    }

    /**
     * 显示播放按钮
     *
     * @param show
     */
    private void showPlayButton(boolean show) {
        getBinding().btPlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}