package com.melvinhou.media_sample.record.audio

import android.Manifest
import android.graphics.Rect
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.FileUtils
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.StringCompareUtils
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.kami.view.activities.BaseActivity
import com.melvinhou.media_sample.R
import com.melvinhou.media_sample.databinding.ItemRecordAudioBinding
import com.melvinhou.media_sample.record.RecordModel
import com.melvinhou.medialibrary.AudioPlayer
import com.melvinhou.medialibrary.AudioPlayer.Callback
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
 * = 分 类 说 明：音频录制
 * ================================================
 */
class AudioRecordActivity : BaseActivity() {
    private val TAG = AudioRecordActivity::class.java.name


    //权限
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    private lateinit var mModel: RecordModel

    //列表
    private var adapter: MyAdapter? = null
    private lateinit var mContainer: RecyclerView
    private lateinit var mPlay: View
    private lateinit var mDuration: TextView


    override fun getLayoutID(): Int = R.layout.fragment_record_audio


    override fun initView() {
        findViewById<TextView>(R.id.title)?.text = "音频录制"
        mModel = ViewModelProvider(this).get(RecordModel::class.java)
        mModel.register()
        mContainer = findViewById(R.id.container)
        mPlay = findViewById(R.id.bt_play)
        mDuration = findViewById(R.id.tv_duration)
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
        adapter?.setOnItemClickListener { holder, _, data ->
            if (AudioPlayer.getInstance().isPlaying) {
                holder.itemView.isSelected = false
                AudioPlayer.getInstance().stopPlay()
            } else {
                holder.itemView.isSelected = true
                AudioPlayer.getInstance().startPlay(data.path, object : Callback {
                    override fun onCompletion(success: Boolean?) {
                        holder.itemView.isSelected = false
                    }

                    override fun onVoiceDb(db: Double) {
                    }
                })
            }
        }
    }

    override fun initListener() {
        mPlay.setOnClickListener {
            if (it.isSelected) {
                stopRecord()
            } else {
                launchRecord()
            }
        }
    }

    override fun onPermissionGranted(requestCode: Int) {
//        super.onPermissionGranted(requestCode)
        //权限申请成功
        launchRecord()
    }

    override fun initData() {
        //加载列表
        mModel.loadListData {
            adapter?.clearData()
//            adapter?.addDatas(it)
            it.forEach {
                if (StringCompareUtils.isAudioFile(it.name))
                    adapter?.addData(it)
            }
        }
    }


    /**
     * 启动录制
     */
    private fun launchRecord() {
        // 请求权限
        if (!checkPermission(REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS)
            return
        }
        //启动录制
        startRecord()
        //启动倒计时
        object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mDuration.text = "录制时间：${8 - (millisUntilFinished / 1000)}s"
            }

            override fun onFinish() {
                stopRecord()
            }
        }.start()
    }

    private fun startRecord() {
        mPlay.isSelected = true
        AudioPlayer.getInstance().startRecord(object : Callback {
            override fun onCompletion(success: Boolean?) {
                recordComplete(success)
                mDuration.text = ""
            }

            override fun onVoiceDb(db: Double) {
            }
        })
    }

    private fun recordComplete(success: Boolean?) {
        initData()
    }

    private fun stopRecord() {
        mPlay.isSelected = false
        AudioPlayer.getInstance().stopRecord()
    }

    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<File, ItemRecordAudioBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemRecordAudioBinding {
            val binding = ItemRecordAudioBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemRecordAudioBinding,
            position: Int,
            data: File
        ) {
            binding.tvTitle.text = data.name
            binding.tvExplain.text = data.absolutePath
            binding.btPlay.isSelected
        }
    }

}