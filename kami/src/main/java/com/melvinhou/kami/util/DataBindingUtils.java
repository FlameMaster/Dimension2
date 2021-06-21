package com.melvinhou.kami.util;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import androidx.databinding.BindingAdapter;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/11 21:25
 * <p>
 * = 分 类 说 明：通用的绑定数据工具类
 * ============================================================
 */
public class DataBindingUtils {


    /**
     * 加载网络图片
     * @param view
     * @param url
     * @param loadImageWidth
     * @param loadImageHeight
     */
    @BindingAdapter({"loadImageUrl", "loadImageWidth", "loadImageHeight"})
    public static void loadImageUrl(ImageView view, String url, int loadImageWidth, int loadImageHeight) {
        if (view != null && StringCompareUtils.isImageUrl(url)) {
//            ImageParameter imageParameter = new ImageParameter(url)
//                    .setWidth(FCUtils.dp2px(loadImageWidth))
//                    .setHeight(FCUtils.dp2px(loadImageHeight));
//            RxImageLoader.with().load(imageParameter).into(view);

            RequestOptions options = new RequestOptions()
                    .override(loadImageWidth > 0 ? DimenUtils.dp2px(loadImageWidth) : -1,
                            loadImageHeight > 0 ? DimenUtils.dp2px(loadImageHeight) : -1)
//                    .placeholder(R.mipmap.icon_hint_empty)
//                    .error(R.mipmap.icon_hint_empty)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(FcUtils.getContext()).asBitmap().load(url).apply(options).into(view);
//            Glide.with(FcUtils.getContext()).load(url).apply(options).into(view);
        }
    }

    /**
     * 加载textview的图片，设置大小
     * @param view
     * @param textDrawableWidth
     * @param textDrawableHeight
     */
    @BindingAdapter({"textDrawableWidth", "textDrawableHeight"})
    public static void initTextDrawableSize(TextView view, int textDrawableWidth, int textDrawableHeight) {
        int width = DimenUtils.dp2px(textDrawableWidth);
        int height = DimenUtils.dp2px(textDrawableHeight);
        for (int i = 0; i < view.getCompoundDrawables().length; i++) {
            Drawable drawable = view.getCompoundDrawables()[i];
            if (drawable != null) {
//                int x = (drawable.getIntrinsicWidth() - width) / 2;
//                int y = (drawable.getIntrinsicHeight() - height) / 2;
//                if (i <= 1)
//                    drawable.setBounds(x, y, x + width, y + height);
//                else if (i <= 2)
//                    drawable.setBounds(0, y, width, y + height);
//                else if (i <= 3)
//                    drawable.setBounds(x, y, x + width, y + height);
//                else
//                    drawable.setBounds(x, 0, x + width, height);
//                view.postInvalidate();
                drawable.setBounds(0,0,width,height);
            }
            view.setCompoundDrawables(
                    view.getCompoundDrawables()[0],
                    view.getCompoundDrawables()[1],
                    view.getCompoundDrawables()[2],
                    view.getCompoundDrawables()[3]);
        }
    }
}
