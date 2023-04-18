package com.melvinhou.media_sample

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.LayoutListBinding
import com.melvinhou.kami.mvvm.BindFragment
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.loadImage
import com.melvinhou.media_sample.bean.MediaItemEntity
import com.melvinhou.media_sample.databinding.ItemMediaBinding
import com.melvinhou.media_sample.databinding.ItemMediaTabBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/16 0016 11:55
 * <p>
 * = 分 类 说 明：Sample-样本
 * ================================================
 */
class MediaFragment : BindFragment<LayoutListBinding, MediaViewModel>() {

    override fun openViewBinding(
        inflater: LayoutInflater?,
        container: ViewGroup?
    ): LayoutListBinding = LayoutListBinding.inflate(layoutInflater, container, false)

    override fun openModelClazz(): Class<MediaViewModel> = MediaViewModel::class.java


    companion object {
        fun newInstance() = MediaFragment()
    }

    private lateinit var mParentAdapter: ParentAdapter

    //屏幕旋转监听的参数
    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    private var mSensorEventListener: SensorEventListener? = null

    //上一次的偏移量
    var lastFx = 0f

    override fun initView() {
        intiList()
    }

    private fun intiList() {
        mParentAdapter = ParentAdapter()
        mBinding.container.adapter = mParentAdapter
        mBinding.container.layoutManager = LinearLayoutManager(context)
        //设定边距
        val decoration = DimenUtils.dp2px(10)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
//                val position = parent.getChildAdapterPosition(view)
//                outRect[0,if (position  == 0)decoration else decoration2 , 0] = 0
            }
        })
        //点击事件
//        mParentAdapter?.setOnItemClickListener { tag, position, data ->
//        }
        mBinding.container.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    scroll(child, recyclerView.height, 0.3f)
                }
            }

            /**
             * 视差位移效果
             *
             * @param child list的item
             * @param height 容器高度
             * @param ratio 相对滑动比（建议0-1）
             */
            private fun scroll(child: View, height: Int, ratio: Float) {
                val scrollView = (child as ViewGroup).getChildAt(0)
                val location = IntArray(2)
                child.getLocationInWindow(location) //获取在当前窗口内的绝对坐标
//                img.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
//                -height/2+getHeight()/2表示控件在屏幕中心的位置
                var dy = (location[1] - DimenUtils.getActionBarSize() - DimenUtils.getStatusBarHeight()
                        + child.height / 2 - height / 2).toFloat()
                //添加滑动比例
                dy = dy * ratio
                //不能超过阀值
                val maxScroll = scrollView.height - child.height
                if (Math.abs(dy) > maxScroll) dy = Math.abs(dy) / dy * maxScroll
                //        Log.e("滑动ing...", "屏幕高度:" + height + "\t\t坐标:" + location[1] + "\t\t位移:" + dy);
//                view.layout(view.getLeft(), layoutParams[1]+dy, view.getRight(),layoutParams[3]+dy);
//                postInvalidate();
                scrollView.translationY = dy
            }
        })

    }

    override fun initData() {
        //初始化重力感应
        initGravitySener()
        //
        mModel.getListData {
            mParentAdapter.clearData()
            mParentAdapter.addDatas(it)
        }
    }

    override fun onStart() {
        super.onStart()
        registerSensor()
    }

    override fun onStop() {
        unregisterSensor()
        super.onStop()
    }


    /**
     * 构造初始化重力感应
     */
    private fun initGravitySener() {
        mSensorManager = FcUtils.getContext()
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        mSensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                //只需要重力传感器
                if (Sensor.TYPE_GRAVITY != event.sensor.type) return
                //获取xy方向的偏移量
                val values = event.values
                val x = values[0]
                //                float y = values[1];
                val fx = x / 10
                val dfx: Float = fx - lastFx
//                Log.d("手机重力感应监听", "x=" + x + "\tdfx=" + fx);
                lastFx = fx
                scrollTabs(dfx)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    /**
     * 条目横向自动滚动
     *
     * @param ratioX
     */
    private fun scrollTabs(ratioX: Float) {
        for (i in 0 until mBinding.container.getChildCount()) {
            val listV: RecyclerView = mBinding.container.getChildAt(i).findViewById(R.id.rv_tabs)
            if (listV.visibility == View.VISIBLE) {
                val maxScroll = DimenUtils.dp2px(200)
                if (listV.scrollState == RecyclerView.SCROLL_STATE_IDLE) listV.scrollBy(
                    (ratioX * maxScroll).toInt(),
                    0
                )
            }
        }
    }

    /**
     * 开始监听
     */
    fun registerSensor() {
//            mSensorManager?.registerListener(mSensorEventListener, mSensor,
//                    SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager?.registerListener(
            mSensorEventListener, mSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    /**
     * 停止监听
     */
    fun unregisterSensor() {
        mSensorManager?.unregisterListener(
            mSensorEventListener
        )
    }

    inner class ParentAdapter : BindRecyclerAdapter<MediaItemEntity, ItemMediaBinding>() {

        val decoration = DimenUtils.dp2px(8)

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup?
        ): ItemMediaBinding {
            val binding = ItemMediaBinding.inflate(inflater, parent, false)
            binding.rvTabs.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
            )
            binding.rvTabs.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State
                ) {
                    val index = parent.getChildAdapterPosition(view)
                    outRect[if (index == 0) decoration else 0, decoration, decoration] = decoration
                }
            })
            return binding
        }

        override fun bindData(binding: ItemMediaBinding, position: Int, data: MediaItemEntity) {
            binding.ivCover.loadImage(data.icon)
            //标题
            val builder = SpannableStringBuilder()
            builder.append(data.title).append("\n")
            val start = builder.length
            builder.append(data.explain)
            builder.setSpan(
                AbsoluteSizeSpan(DimenUtils.sp2px(12)),
                start,
                builder.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                ForegroundColorSpan(Color.parseColor("#DDDDDD")),
                start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvTitle.text = builder
            //列表
            val adapter = ChildAdapter()
            binding.rvTabs.adapter = adapter
            adapter.addDatas(data.list)
            adapter.setOnItemClickListener { viewHolder, position, data ->
                mModel.toPage(data.id) {
                    if (it.component != null)
                        toActivity(it)
                }
            }
        }


    }

    inner class ChildAdapter : BindRecyclerAdapter<MediaItemEntity, ItemMediaTabBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup?
        ): ItemMediaTabBinding {
            val binding = ItemMediaTabBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(binding: ItemMediaTabBinding, position: Int, data: MediaItemEntity) {
            binding.ivCover.loadImage(data.icon)
            binding.tvTitle.text = data.title
        }


    }

}