package com.melvinhou.knight

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringUtils
import java.text.DecimalFormat

/**
 * 显示价格
 */
fun TextView.showPrice(price: Double, isUnit: Boolean) {
    val format = DecimalFormat("##0.00")
    if (isUnit)
        text = "¥ ${format.format(price)}"
    else
        text = format.format(price)
}

/**
 * 显示价格
 */
fun TextView.showPrice(price: Float, isUnit: Boolean) {
    val format = DecimalFormat("##0.00")
    if (isUnit)
        text = "¥ ${format.format(price)}"
    else
        text = format.format(price)
}

fun TextView.showLargePrice(price: Double) {
    text = "${(price / 10000.0).toInt()}万起"
}

fun TextView.showLargePrice(price: Float) {
    text = "${(price / 10000f).toInt()}万起"
}

/**
 * 显示价格
 */
fun TextView.showPrice(priceStr: String?, isUnit: Boolean) {
    var price = 0f
    try {
        price = priceStr?.toFloat() ?: 0f
    } finally {
        showPrice(price, isUnit)
    }
}


/**
 * 显示脱敏手机号
 */
fun TextView.showMobile(mobileStr: String?) {
    var mobile = "*********"
    mobileStr?.let {
        mobile = it
        if (it.length > 10) {
            mobile = "${it.substring(0, 3)}****${
                it.substring(7, it.length)
            }"
        }
    }
    text = mobile
}



/**
 * 加载图片
 */
@SuppressLint("CheckResult")
fun TextView.loadImage(orient: Int = 0, url: String?, width: Int, height: Int, emptyRes: Int = -1) {
    if (StringUtils.isEmpty(url)) return
    val builder = Glide.with(context)
        .load(url)
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .format(DecodeFormat.PREFER_RGB_565)
        .disallowHardwareConfig()
    if (emptyRes > 0)
        builder.placeholder(emptyRes)
            .error(emptyRes)
    builder.into(object : CustomTarget<Drawable?>() {
        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable?>?
        ) {
//            resource.bounds = Rect(0, 0, width, height)
            setCompoundDrawablesWithIntrinsicBounds(
                if (orient == 0) resource else null,
                if (orient == 1) resource else null,
                if (orient == 2) resource else null,
                if (orient == 3) resource else null,
            )
        }
        override fun onLoadCleared(placeholder: Drawable?) {}
    })
}

/**
 * 设置图片
 */
fun ImageView.loadImage(path: String?) {
//    Log.d("图片加载","width=${width},height=${height},path=${path}")
    loadImage(path,width,height)
}


/**
 * 设置图片
 */
fun ImageView.loadImage(path: String?, width: Int, height: Int) {
    Glide.with(FcUtils.getContext())
        .load(path)
        .override(width, height)
        .placeholder(R.drawable.img_placeholder)
        .error(R.drawable.img_error)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
//        .apply(
//            RequestOptions()
//                .override(width, height)
//                .placeholder(R.drawable.img_placeholder)
//                .error(R.drawable.img_error)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//        )
        .into(this)
}