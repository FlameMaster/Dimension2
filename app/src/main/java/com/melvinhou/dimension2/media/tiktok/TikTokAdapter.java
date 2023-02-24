package com.melvinhou.dimension2.media.tiktok;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ItemTiktokBD;
import com.melvinhou.kami.adapter.DataBindingRecyclerAdapter;
import com.melvinhou.kami.tool.ThreadManager;
import com.melvinhou.kami.util.FcUtils;

import androidx.databinding.ViewDataBinding;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/21 20:23
 * <p>
 * = 分 类 说 明：抖音列表适配器
 * ================================================
 */
public class TikTokAdapter extends DataBindingRecyclerAdapter<TiktokEntity, TikTokHolder> {

    private boolean lock = false;
    //点击事件
    private OnItemDoubleClickListener mClickListener;

    /**
     * 设置点击事件
     *
     * @param listener
     */
    public void setOnItemDoubleClickListener(TikTokAdapter.OnItemDoubleClickListener listener) {
        mClickListener = listener;
    }

    @SuppressLint("CheckResult")
    @Override
    public void bindData(TikTokHolder viewHolder, int position, TiktokEntity data) {
        viewHolder.updateData(data);
        if (position == 0 && !lock) {
            lock = true;
            viewHolder.play();
        }

        //手势判断
        if (mClickListener != null) {
            GestureDetector gestureDetector
                    = new GestureDetector(FcUtils.getContext(), new GestureListener() {
                //调用mClickListener接口
                @Override
                protected void onSingleClick() {
                    mClickListener.onItemSingleClick(viewHolder, position, data);
                }

                @Override
                protected void onDoubleClick(float x, float y) {
                    mClickListener.onItemDoubleClick(viewHolder, position, data,x,y);
                }
            });
            viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_tiktok;
    }

    @Override
    protected TikTokHolder onCreate(ViewDataBinding binding, int viewType) {
        return new TikTokHolder((ItemTiktokBD) binding);
    }


    /*条目点击事件接口*/
    public interface OnItemDoubleClickListener {
        //点击
        void onItemSingleClick(TikTokHolder viewHolder, int position, TiktokEntity data);


        /**
         * 连击
         * @param viewHolder
         * @param position
         * @param data
         * @param x 点击位置
         * @param y
         */
        void onItemDoubleClick(TikTokHolder viewHolder, int position, TiktokEntity data, float x, float y);
    }


    /**
     * 手势监听，需要用什么继承什么
     */
    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        //解锁时间
        private long unLockTime = 0;
        private int number = 0;

        //点击触发
        protected void onSingleClick() {
            number = 0;
            Log.e("TikTokAdapter", "单击");
        }

        //双击触发
        protected void onDoubleClick(float x, float y) {
            Log.d("TikTokAdapter", "双击:" + number++);
        }

        //单击确认
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //必须距离上次连击超过1秒后才能使用
            if (unLockTime == 0) onSingleClick();
            return super.onSingleTapConfirmed(e);
        }

        //双击
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //第一次双击触发启动计时器
            if (unLockTime == 0) {
                unLockTime = System.currentTimeMillis() + 1000;
                ThreadManager.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (unLockTime <= System.currentTimeMillis()) {
                                unLockTime = 0;
                                return;
                            }
                            SystemClock.sleep(1000);
                        }
                    }
                });
                onDoubleClick(e.getRawX(), e.getRawY());
            }
            return super.onDoubleTap(e);
        }

        //双击按下抬起各触发一次
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            unLockTime = System.currentTimeMillis() + 1000;
            onDoubleClick(e.getRawX(), e.getRawY());
            return super.onDoubleTapEvent(e);
        }
    }
}
