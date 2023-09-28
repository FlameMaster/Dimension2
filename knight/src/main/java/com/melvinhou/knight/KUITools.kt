package com.melvinhou.knight

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.TimeZone
import android.os.Build
import android.text.TextUtils
import android.text.format.Time
import android.view.*
import android.widget.*
import androidx.collection.arrayMapOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.tool.ItemFullSnapHelper
import com.melvinhou.kami.tool.UITools
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.ResourcesUtils
import com.melvinhou.knight.databinding.ItemCheckTitleBinding
import java.util.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/13 0013 10:24
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
object KUITools {


    /**
     * 列表弹窗
     */
    fun showListDialog(
        activity: Activity,
        title: String?,
        adaper: RecyclerView.Adapter<*>?
    ): () -> Unit {
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_select_list,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        //3个控件
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tv_title)
        val ivCancel = dialog.window?.findViewById<View>(R.id.iv_cancel)
        val listView = dialog.window?.findViewById<RecyclerView>(R.id.rl_list)
        if (!TextUtils.isEmpty(title)) tvTitle?.text = title
        ivCancel?.setOnClickListener {
            dialog.dismiss()
        }

        listView?.adapter = adaper
        listView?.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL, false
        )
        return { dialog.dismiss() }
    }

    /**
     * 列表选择器
     */
    fun showListSelectDialog01(
        activity: Activity,
        owner: LifecycleOwner,
        title: String?,
        datas: MutableCollection<String>,
        callBack: (Int) -> Unit
    ) {
        val list = arrayListOf<String>()
        val position = MutableLiveData(1)
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_select_list01,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        //3个控件
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tv_title)
        val ivCancel = dialog.window?.findViewById<View>(R.id.iv_cancel)
        val tvSubmit = dialog.window?.findViewById<TextView>(R.id.bt_submit)
        if (!TextUtils.isEmpty(title)) tvTitle?.text = title
        ivCancel?.setOnClickListener {
            dialog.dismiss()
        }
        tvSubmit?.setOnClickListener {
            var value = (position.value ?: 1) - 1
            callBack(value)
            dialog.dismiss()
        }

        //两个列表
        val rlList = dialog.window?.findViewById<RecyclerView>(R.id.rl_list)
        rlList?.layoutManager = LinearLayoutManager(activity)
        val adapter = object : BindRecyclerAdapter<String, ItemCheckTitleBinding>() {
            override fun getViewBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): ItemCheckTitleBinding {
                return ItemCheckTitleBinding.inflate(inflater, parent, false)
            }

            override fun bindData(binding: ItemCheckTitleBinding, pos: Int, data: String) {
                binding.tab.text = data
                position.observe(owner) {
                    binding.tab.isSelected = it == pos
                }
            }
        }
        rlList?.adapter = adapter
        ItemFullSnapHelper().attachToRecyclerView(rlList)

        //数据初始化
        list.add("")
        list.addAll(datas)
        list.add("")
        adapter.addDatas(list)
        rlList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(1))
                    position.postValue(pos)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE || dy == 0) {
                    val pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(1))
                    position.postValue(pos)
                }
            }
        })

    }

    /**
     * 列表选择器
     */
    fun showListSelectDialog02(
        activity: Activity,
        title: String?,
        datas: MutableCollection<String>,
        callBack: (Int) -> Unit
    ) {
        val list = datas.toList()
        val map = arrayMapOf<Int, Int>()//id对应位置
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_select_list02,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        //3个控件
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tvTitle)
        val ivCancel = dialog.window?.findViewById<View>(R.id.ivCancel)
        val rgGroup = dialog.window?.findViewById<RadioGroup>(R.id.rgGroup)
        if (!TextUtils.isEmpty(title)) tvTitle?.text = title
        ivCancel?.setOnClickListener {
            dialog.dismiss()
        }

        rgGroup?.setOnCheckedChangeListener { _, checkedId ->
            val pos = map[checkedId] ?: -1
            callBack(pos)
            dialog.dismiss()
        }

        rgGroup?.removeAllViews()
        for (i in datas.indices) {
            val id = View.generateViewId()
            map.put(id, i)
            val rb = RadioButton(activity)
            rb.text = list[i]
            rb.id = id
            rb.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            rb.setTextColor(ResourcesUtils.getColor(R.color.black))
            rb.textSize = 15f
            rb.buttonDrawable = null
            rb.setPadding(0, DimenUtils.dp2px(16), 0, DimenUtils.dp2px(16))
            rb.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.selector_check, 0)
            rgGroup?.addView(rb)
        }

    }

    /**
     * ios的月份选择器
     */
    fun showDateSelectDialog(
        activity: Activity, owner: LifecycleOwner, title: String?, callBack: (String) -> Unit
    ) {
        val year = MutableLiveData<Int>(2022)
        val month = MutableLiveData<Int>(1)
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_select_date,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        //3个控件
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tvTitle)
        val tvCancel = dialog.window?.findViewById<TextView>(R.id.bt_cancel)
        val tvSubmit = dialog.window?.findViewById<TextView>(R.id.bt_submit)
        if (!TextUtils.isEmpty(title)) tvTitle?.text = title
        tvCancel?.setOnClickListener {
            dialog.dismiss()
        }
        tvSubmit?.setOnClickListener {
            val y = year.value
            val m = month.value ?: 1
            callBack("${y}-${if (m < 10) "0" else ""}${m}")
            dialog.dismiss()
        }

        //两个列表
        val rlDate1 = dialog.window?.findViewById<RecyclerView>(R.id.rl_date1)
        val rlDate2 = dialog.window?.findViewById<RecyclerView>(R.id.rl_date2)
        rlDate1?.layoutManager = LinearLayoutManager(activity)
        rlDate2?.layoutManager = LinearLayoutManager(activity)
        val adapter1 = object : BindRecyclerAdapter<Int, ItemCheckTitleBinding>() {
            override fun getViewBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): ItemCheckTitleBinding {
                return ItemCheckTitleBinding.inflate(inflater, parent, false)
            }

            override fun bindData(binding: ItemCheckTitleBinding, position: Int, data: Int) {
                binding.tab.text = if (data < 0) "" else "${data}年"
                year.observe(owner) {
                    binding.tab.isSelected = it == data
                }
            }
        }
        val adapter2 = object : BindRecyclerAdapter<Int, ItemCheckTitleBinding>() {
            override fun getViewBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): ItemCheckTitleBinding {
                return ItemCheckTitleBinding.inflate(inflater, parent, false)
            }

            override fun bindData(binding: ItemCheckTitleBinding, position: Int, data: Int) {
                binding.tab.text = if (data < 0) "" else "${if (data < 10) "0" else ""}${data}月"
                month.observe(owner) {
                    binding.tab.isSelected = it == data
                }
            }
        }
        rlDate1?.adapter = adapter1
        rlDate2?.adapter = adapter2
        ItemFullSnapHelper().attachToRecyclerView(rlDate1)
        ItemFullSnapHelper().attachToRecyclerView(rlDate2)

        //数据初始化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val calendar = android.icu.util.Calendar.getInstance(TimeZone.getTimeZone("GMT+08"))
            year.value = calendar[android.icu.util.Calendar.YEAR]
            //系统月份从0角标开始
            month.value = calendar[android.icu.util.Calendar.MONTH] + 1
        } else {
            val t = Time("GMT+8") // 设置Time Zone资料。
            t.setToNow() // 获得当前系统时间。
            year.value = t.year
            month.value = t.month + 1
        }
        val nowY = year.value ?: 0
        val nowM = month.value ?: 1
        adapter1.addData(-1)
        adapter1.addData(-1)
        for (i in 0 until 20) {
            adapter1.addData(nowY - i)
        }
        adapter1.addData(-1)
        adapter1.addData(-1)
        for (i in 1..16) {
            if (i < 3 || i > 14) adapter2.addData(-1)
            else adapter2.addData(i - 2)
        }
        rlDate2?.let {
            //-1为月份对应实际角标，+4显示5条最后角标为4
            it.layoutManager?.smoothScrollToPosition(it, RecyclerView.State(), nowM - 1 + 4)
        }
        rlDate1?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(2))
                    year.postValue(adapter1.getData(pos))
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE || dy == 0) {
                    val pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(2))
                    year.postValue(adapter1.getData(pos))
                }
            }
        })
        rlDate2?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(2))
                    month.postValue(adapter2.getData(pos))
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE || dy == 0) {
                    val pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(2))
                    month.postValue(adapter2.getData(pos))
                }
            }
        })

    }

    /**
     * Android的系统日期选择器
     */
    fun showDatePickerDialog(activity: Activity,title: String?, callBack: (String, String, String) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        //获取Calendar对象，用于获取当前时间
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        //时间选择器
        val datePickerDialog = DatePickerDialog(activity, R.style.KamiDialog)
        datePickerDialog.updateDate(year, month, day)
        datePickerDialog.setTitle(title)
        //选择完日期后会调用该回调函数
        datePickerDialog.setOnDateSetListener { view, year1, monthOfYear, dayOfMonth ->
            //因为monthOfYear会比实际月份少一月所以这边要加1
            val selectY = year1.toString()
            val selectM = "${if (monthOfYear < 9) "0" else ""}${monthOfYear + 1}"
            val selectD = "${if (dayOfMonth < 10) "0" else ""}$dayOfMonth"
            callBack(selectY, selectM, selectD)
        }
        datePickerDialog.show()
    }

    /**
     * Android的系统时间选择器
     */
    fun showTimePickerDialog(activity: Activity,title: String?, callBack: (String, String) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        //获取Calendar对象，用于获取当前时间
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        //时间选择
        val timePickerDialog = TimePickerDialog(activity,
            //选择完日期后会调用该回调函数
            { view: TimePicker?, hourOfDay: Int, minute1: Int ->
                val selectH = "${if (hourOfDay < 10) "0" else ""}${hourOfDay}"
                val selectM = "${if (minute1 < 10) "0" else ""}$minute1"
                callBack(selectH,selectM)
            }, hour, minute, true
        )
        timePickerDialog.setTitle(title)
        timePickerDialog.show()
    }


    /**
     * 输入框弹窗
     */
    fun showInputDialog01(
        activity: Activity,
        title: String,
        inputHint: String,
        inputType: Int,
        callBack: (String?) -> Unit
    ) {
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_input02,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        dialog.setOnCancelListener {
            callBack(null)
        }
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val btSubmit = dialog.window?.findViewById<View>(R.id.bt_submit)
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tv_title)
        val etInput = dialog.window?.findViewById<EditText>(R.id.et_input)
        btSubmit?.setOnClickListener {
            callBack(etInput?.text.toString())
            UITools.hideSoftKeyboard(dialog)
            dialog.dismiss()
        }
        tvTitle?.text = title
        etInput?.hint = inputHint
        etInput?.inputType = inputType
    }

    /**
     * 输入框弹窗
     */
    fun showInputDialog02(
        activity: Activity,
        title: String,
        inputHint: String,
        inputType: Int,
        callBack: (String) -> Unit
    ) {
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_input03,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val ivCancel = dialog.window?.findViewById<View>(R.id.iv_cancel)
        val btSubmit = dialog.window?.findViewById<View>(R.id.bt_submit)
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tv_title)
        val etInput = dialog.window?.findViewById<EditText>(R.id.et_input)
        ivCancel?.setOnClickListener {
            UITools.hideSoftKeyboard(dialog)
            dialog.cancel()
        }
        btSubmit?.setOnClickListener {
            callBack(etInput?.text.toString())
            UITools.hideSoftKeyboard(dialog)
            dialog.cancel()
        }
        tvTitle?.text = title
        etInput?.hint = inputHint
        etInput?.inputType = inputType
    }
}