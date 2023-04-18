package com.melvinhou.media_sample.screenrecord

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.view.*
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.loadImage
import com.melvinhou.media_sample.R
import com.melvinhou.media_sample.databinding.ItemRecordVideoBinding
import com.melvinhou.medialibrary.record.SecreenRecordView
import com.melvinhou.medialibrary.video.FcVideoActivity
import java.io.File


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/12 0012 17:52
 * <p>
 * = 分 类 说 明：录屏
 * ================================================
 */
class ScreenRecordActivity : SecreenRecordView() {
    private val TAG = ScreenRecordActivity::class.java.name


    //权限
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    val REQUIRED_PERMISSIONS_33 = arrayOf(
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    private lateinit var mModel: ScreenRecordModel

    //列表
    private var adapter: MyAdapter? = null
    private lateinit var mContainer: RecyclerView

    //浮动窗口
    private var mParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var mStopView: TextView? = null
    private var mDownTimerView: TextView? = null

    //录制的启动参数
    private var startIntent: Intent? = null


    override fun getLayoutID(): Int = R.layout.fragment_record_list


    override fun initView() {
        findViewById<TextView>(R.id.title)?.text = "屏幕录制"
        mModel = ViewModelProvider(this).get(ScreenRecordModel::class.java)
        mModel.register()
        mContainer = findViewById(R.id.container)
        initList()

    }

    /**
     * 初始化列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mContainer.adapter = this.adapter
        mContainer.layoutManager = LinearLayoutManager(baseContext)
        //设定边距
        val decoration = DimenUtils.dp2px(10)
        mContainer.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[decoration, if (position == 0) decoration else 0, decoration] = decoration
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->
            val intent = Intent()
            intent.setClass(getApplication(), FcVideoActivity::class.java)
            intent.putExtra("title", data.name)
            intent.putExtra("url", data.absolutePath)
            startActivity(intent)
        }
    }

    override fun initListener() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        findViewById<View>(R.id.iv_add)?.setOnClickListener {
            launchRecord()
        }
    }

    override fun onPermissionGranted(requestCode: Int) {
//        super.onPermissionGranted(requestCode)
        //权限申请成功
        initData()
    }

    override fun initData() {
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) REQUIRED_PERMISSIONS_33 else REQUIRED_PERMISSIONS
        // 请求权限
        if (!checkPermission(permissions)) {
            requestPermissions(permissions)
            return
        }
        //加载列表
        mModel.loadListData {
            adapter?.clearData()
            adapter?.addDatas(it)
        }
    }


    /**
     * 启动录制
     */
    private fun launchRecord() {
        //屏幕弹窗权限
        //android 6.0或者之后的版本需要发一个intent让用户授权
        if (!Settings.canDrawOverlays(applicationContext)) {
//            val packageName = "com.melvinhou.dimension2"
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            toResultActivity(intent)
            return
        }
        //开始录制
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val screenCaptureIntent = manager.createScreenCaptureIntent()
        toResultActivity(screenCaptureIntent) {
            if (it.resultCode == RESULT_OK) {
                startIntent = it.data;
                //悬浮窗布局参数
                initFloatWindowParams()
                //倒计时
                showDownTimer()
                //跳桌面
                toDesktop()
//            //直接启动录制
//            startRecord(it.data)
            }
        }
    }

    /**
     * 浮动按钮的参数
     */
    private fun initFloatWindowParams() {
        //创建窗口布局参数
        mParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT
        )
        //设置悬浮窗坐标
//        mParams.x=100;
//        mParams.y=100;
        //表示该Window无需获取焦点，也不需要接收输入事件
        mParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mParams?.gravity = Gravity.CENTER
        //设置window 类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API Level 26
            mParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mParams?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
    }

    /**
     * 倒计时的悬浮窗
     */
    private fun showDownTimer() {
        //创建倒计时的悬浮窗
        if (null == mDownTimerView) {
            mDownTimerView = TextView(FcUtils.getContext())
            mDownTimerView?.setText("3")
            mDownTimerView?.setTextSize(72f)
            mDownTimerView?.setTextColor(Color.RED)
            mWindowManager?.addView(mDownTimerView, mParams)
        }
        //启动倒计时
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mDownTimerView?.setText((millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {
                mDownTimerView?.let {
                    mWindowManager?.removeView(mDownTimerView)
                }
                mDownTimerView = null
                //显示停止键
                showStopButton()
                //开始录制
                startRecord(startIntent)
            }
        }.start()
    }

    /**
     * 结束录制按钮
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun showStopButton() {
        if (null == mStopView) {
            mParams!!.gravity = Gravity.TOP or Gravity.LEFT
            mStopView = TextView(FcUtils.getContext())
            mStopView?.setText("3")
            val paddingh = DimenUtils.dp2px(10)
            val paddingv = DimenUtils.dp2px(5)
            mStopView?.setPadding(paddingh, paddingv, paddingh, paddingv)
            mStopView?.setBackgroundColor(Color.RED)
            mStopView?.setText("点击停止")
            mStopView?.setTextColor(Color.BLACK)
            mWindowManager!!.addView(mStopView, mParams)
            mStopView?.setOnClickListener({ v: View? ->
                mWindowManager?.removeView(mStopView)
                mStopView = null
                //结束
                stopRecord()
            })
        }
    }


    /**
     * 跳转桌面
     */
    private fun toDesktop() {
        val mIntent = Intent(Intent.ACTION_MAIN)
        mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        mIntent.addCategory(Intent.CATEGORY_HOME)
        startActivity(mIntent)
    }

    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<File, ItemRecordVideoBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemRecordVideoBinding {
            val binding = ItemRecordVideoBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemRecordVideoBinding,
            position: Int,
            data: File
        ) {
            binding.tvTitle.text = data.name
            binding.tvExplain.text = data.absolutePath
            binding.ivCover.loadImage(data.path)
        }
    }

}