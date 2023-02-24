package com.melvinhou.rxjava.fileloader.image;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/9 21:08
 * <p>
 * = 分 类 说 明：图片加载器
 * ================================================
 */

public class RxImageLoader {

    /*单例对象*/
    private static RxImageLoader singleton;

    private RxImageLoader() {

//        RequestOptions options = new RequestOptions()
//                .override(itemWidth, (int) itemHeight)
//                .placeholder(R.mipmap.ic_default)
//                .error(R.mipmap.ic_default);
//        Glide.with(FCUtils.getContext()).asBitmap().load(paint.getCover().getUrl()).apply(options).into(getBinding().img);
    }

    /*获取单例对象*/
    public static synchronized RxImageLoader with() {
        if (singleton == null) singleton = new RxImageLoader();
        return singleton;
    }

    /*设置要加载的图片*/
    public RequestBuilder load(@NonNull ImageParameter imageParameter) {
        return new RequestBuilder(imageParameter);
    }


    public static class RequestBuilder {

        /*图片加载管理*/
        private ImageRequestCreator requestCreator;
        /*需要加载的图片的url*/
        private ImageParameter imageParameter;

        public RequestBuilder(ImageParameter imageParameter) {
            this.imageParameter = imageParameter;
            requestCreator = new ImageRequestCreator();
        }

        /*开始加载并设置*/
        public void into(final ImageView imageView) {
            //根据控件大小再次调整图片大小
            if (imageParameter.getWidth() == 0) imageParameter.setWidth(imageView.getWidth());
            if (imageParameter.getHeight() == 0) imageParameter.setHeight(imageView.getHeight());

            //获取key
            String cache_key = imageParameter.getCacheKey();
//        first方法表示判断，如果IamgeBean中的bitmap为空，那么跳过此次连接，
//        例如，requestCreator.getImageFromMemory(mUrl)获取的bitmap为空，
//        那么直接跳过这次concat连接，进行requestCreator.getImageFromDisk(mUrl)操作，
//        直到bitmap不为空则程序继续往下执行，断开concat的连接
            Observable
                    .concat(
                            requestCreator.getFromMemory(cache_key),
                            requestCreator.getFromDisk(cache_key),
                            requestCreator.getFromNetwork(imageParameter)
                    )
                    .first(imageParameter)//选择第一个满足条件的
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BitmapCacheModel>() {
                        @Override
                        public void onSubscribe(Disposable d) {
//                            imageView.setImageResource(0);
                        }

                        @Override
                        public void onNext(BitmapCacheModel imageBean) {
                            if (imageBean.getCacheValue() != null)
                                imageView.setImageBitmap(imageBean.getCacheValue());
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }


        /*开始加载并设置*/
        public void into(final Observer<BitmapCacheModel> observer) {
            String cache_key = imageParameter.getCacheKey();
            Observable.concat(
                    requestCreator.getFromMemory(cache_key),
                    requestCreator.getFromDisk(cache_key),
                    requestCreator.getFromNetwork(imageParameter)
            )
                    .first(imageParameter)//这里判断有问题，会连续加载3次
                    .toObservable()
                    .subscribe(observer);
        }

    }

}
