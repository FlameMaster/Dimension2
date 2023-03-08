package com.melvinhou.kami.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.melvinhou.kami.util.ImageUtils;

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
 * = 时 间：2021/4/2 18:46
 * <p>
 * = 分 类 说 明：Glide加载高斯模糊
 * ================================================
 */
public class GlideBlurTransformation extends BitmapTransformation {

    private static final int VERSION = 1;
    private static final String ID = "BlurTransformation." + VERSION;

    private static int MAX_RADIUS = 25;
    private static int DEFAULT_DOWN_SAMPLING = 1;

    private int radius;
    private int sampling;

    public GlideBlurTransformation() {
        this(MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public GlideBlurTransformation(int radius) {
        this(radius, DEFAULT_DOWN_SAMPLING);
    }

    public GlideBlurTransformation(int radius, int sampling) {
        this.radius = radius;
        this.sampling = sampling;
    }

    //图片变换
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        int scaledWidth = width / sampling;
        int scaledHeight = height / sampling;

        Bitmap bitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) sampling, 1 / (float) sampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(toTransform, 0, 0, paint);
        bitmap = ImageUtils.doBlur(bitmap, radius, false);

        return bitmap;
    }

    @Override public String toString() {
        return "BlurTransformation(radius=" + radius + ", sampling=" + sampling + ")";
    }

    @Override public boolean equals(Object o) {
        return o instanceof GlideBlurTransformation &&
                ((GlideBlurTransformation) o).radius == radius &&
                ((GlideBlurTransformation) o).sampling == sampling;
    }

    @Override public int hashCode() {
        return ID.hashCode() + radius * 1000 + sampling * 10;
    }

    @Override public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((ID + radius + sampling).getBytes(CHARSET));
    }


}
