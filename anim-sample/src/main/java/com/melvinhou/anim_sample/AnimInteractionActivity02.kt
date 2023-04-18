package com.melvinhou.anim_sample

import android.app.ActivityOptions
import android.content.Intent
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Pair
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import com.melvinhou.anim_sample.databinding.ActivityAnimInact02Binding
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/2/6 0006 14:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class AnimInteractionActivity02 : BindActivity<ActivityAnimInact02Binding, BaseViewModel>() {

    override fun openViewBinding(): ActivityAnimInact02Binding =
        ActivityAnimInact02Binding.inflate(layoutInflater)

    override fun openModelClazz(): Class<BaseViewModel> =
        BaseViewModel::class.java

    override fun initView() {
        mBinding.barRoot.title.text = "交互动画1"
    }


    override fun initListener() {
        mBinding.ivAnim01.setOnClickListener {
            Intent().apply {
                setClass(getApplication(), AnimInteractionActivity::class.java)
                startActivity(
                    this, ActivityOptions.makeSceneTransitionAnimation(
                        this@AnimInteractionActivity02,
                        Pair(it, "fc")
                    )
                        .toBundle()
                );
            }
        }
        //揭露动画
        mBinding.tvAnim02.setOnClickListener {
            val width = it.width
            val height = it.height
            //斜边
            val viewHypotenuse =
                Math.hypot(width.toDouble(), height.toDouble())
            val anim = ViewAnimationUtils.createCircularReveal(
                it, 0, 0, 0f, viewHypotenuse.toFloat()
            )
            anim.duration = 500
            anim.start()
        }
        //视图状态动画
        var flag = false
        val STATE_CHECKED = intArrayOf(android.R.attr.state_checked)
        val STATE_UNCHECKED = intArrayOf()
        mBinding.ivAnim03.setOnClickListener {
            it.isSelected = !it.isSelected
            if (flag) {
                mBinding.ivAnim03.setImageState(STATE_CHECKED, true);
                flag = false;
            } else {
                mBinding.ivAnim03.setImageState(STATE_UNCHECKED, true);
                flag = true;
            }
        }
        //布局切换动画
        val set01 = ConstraintSet()
        set01.clone(mBinding.cl03)
        val set02 = ConstraintSet()
        set02.clone(this, R.layout.inset_anim_inact02)
        mBinding.tvAnim04.setOnClickListener {
            val isCheck = !it.isSelected
            it.isSelected = isCheck
            if (isCheck){
                TransitionManager.beginDelayedTransition(mBinding.cl03);
                set02.applyTo(mBinding.cl03);
            }else{
                val transition = AutoTransition()
                transition.setDuration(500)
                TransitionManager.beginDelayedTransition(mBinding.cl03,transition)
                set01.applyTo(mBinding.cl03)
            }
        }
        mBinding.tvAnim05.setOnClickListener {
            val isCheck = !it.isSelected
            it.isSelected = isCheck
            if (isCheck){
                set01.connect(mBinding.v01.id,ConstraintSet.LEFT,mBinding.tv01.id,ConstraintSet.RIGHT,10)
                set01.connect(mBinding.v01.id,ConstraintSet.TOP,mBinding.tv01.id,ConstraintSet.BOTTOM,)
                TransitionManager.beginDelayedTransition(mBinding.cl03);
                set01.applyTo(mBinding.cl03);
            }else{
                set01.connect(mBinding.v01.id,ConstraintSet.RIGHT,mBinding.cl03.id,ConstraintSet.RIGHT)
                set01.connect(mBinding.v01.id,ConstraintSet.TOP,mBinding.cl03.id,ConstraintSet.TOP)
                TransitionManager.beginDelayedTransition(mBinding.cl03);
                set01.applyTo(mBinding.cl03)
            }
        }
    }

}