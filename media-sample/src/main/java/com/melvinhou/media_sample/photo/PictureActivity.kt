package com.melvinhou.media_sample.photo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import androidx.core.view.isVisible
import com.melvinhou.kami.io.DownloadHelper
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.view.wiget.PhotoCutterView
import com.melvinhou.knight.KUITools
import com.melvinhou.knight.loadImage
import com.melvinhou.media_sample.R
import com.melvinhou.media_sample.databinding.FragmentPictureBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/23 0023 14:21
 * <p>
 * = 分 类 说 明：图片浏览
 * ================================================
 */
class PictureActivity : BindActivity<FragmentPictureBinding, BaseViewModel>() {

    override fun openViewBinding(): FragmentPictureBinding =
        FragmentPictureBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<BaseViewModel> =
        BaseViewModel::class.java


    private val TAG = PictureActivity::class.java.name
    private var mPath: String? = null

    override fun upBarMenuID(): Int = R.menu.bar_photo

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                savePicture()
                return true
            }
            R.id.menu_open -> {
                open()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun backward() {
        //finish()不会执行动画
        finishAfterTransition()
    }

    override fun initView() {
        mBinding.barRoot.title.text = "图片详情"
        mBinding.barRoot.barRoot.setBackgroundColor(0x80FFFFFF.toInt())

        //图片查看模式
        val mode = intent.getIntExtra("mode", PhotoCutterView.GESTURE_MODE_INFINITE)
        mBinding.container.setGestureMode(mode)
        if (mode == PhotoCutterView.GESTURE_MODE_BOX) {
            mBinding.container.setCenterCheckBox(
                intent.getIntExtra("boxSize", DimenUtils.dp2px(300)).toFloat()
            )
            mBinding.container.setCheckBoxColor(
                intent.getIntExtra("boxColor", 0x80000000.toInt())
            )
        }
    }

    override fun initListener() {
        mBinding.container.setOnClickListener {
            //标题栏
            val isVisible = mBinding.barRoot.barRoot.isVisible
            mBinding.barRoot.barRoot.isVisible = !isVisible
        }
    }

    override fun initData() {
        val url = intent.getStringExtra("url")
        if (TextUtils.isEmpty(url)) {
            KUITools.showInputDialog01(
                this,
                "图片地址",
                "输入需要打开的图片url",
                InputType.TYPE_CLASS_TEXT
            ) {
                loadData(it)
            }
        } else {
            loadData(url)
        }
    }

    private fun loadData(url: String?) {
        mPath = url;
        mBinding.container.loadImage(url, -1, -1)
    }

    /**
     * 保存图片
     */
    private fun savePicture() {
        mBinding.container.apply {
            if (drawable == null) return
            if (gestureMode == PhotoCutterView.GESTURE_MODE_BOX) {//裁剪
                val rect = checkBoxMargin
                val size = checkBoxSize
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                val canvas = Canvas(bitmap)
                canvas.translate(-rect[0], -rect[1])
                draw(canvas)
//                Bitmap.createBitmap(bitmap,rect[0],rect[1],size,size)//图片裁剪
                mBinding.ivShow.setImageBitmap(bitmap)
            } else {
//                val bitmap = Bitmap.createBitmap(
//                    drawable.intrinsicWidth,
//                    drawable.intrinsicHeight,
//                    Bitmap.Config.ARGB_8888
//                )
//                val canvas = Canvas(bitmap)
//                drawable.draw(canvas)
//                mBinding.ivShow.setImageBitmap(bitmap)
                mPath?.let {
//                    val name  = FileUtils.getFileNameForDate()+".jpg"
                    val name = it.split("/").last()
                    //下载
                    Log.w(TAG, "文件：${name},地址：${it}")
                    val helper = DownloadHelper.getInstance(name, it)
                    helper.setDownloadListener(object : DownloadHelper.DownloadListener {
                        override fun onStart() {
                        }

                        override fun onProgress(soFarSize: Long, totalSize: Long) {

                        }

                        override fun onFinish(fileFullPath: String?, totalSize: Long) {
                            FcUtils.showToast("保存成功：${fileFullPath}")
                        }

                        override fun onFailed() {
                            FcUtils.showToast("保存失败")
                        }
                    })
                    helper.start()
                }
            }
        }
    }

    private fun open() {
        val intent = Intent()
        // 开启Pictures画面Type设定为image
        intent.type = "image/*"
        // 使用Intent.ACTION_GET_CONTENT这个Action
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        toResultActivity(intent) {
            if (it.resultCode == RESULT_OK) { //成功加载
                loadData(it.data?.data.toString())
            }
        }
    }


}