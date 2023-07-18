package com.melvinhou.knight

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.melvinhou.kami.util.FcUtils
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
        .apply(
            RequestOptions()
                .override(width, height)
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        )
        .into(this)
}