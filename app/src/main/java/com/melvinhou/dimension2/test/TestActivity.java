package com.melvinhou.dimension2.test;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.ijk.IjkVideoView;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.BaseActivity;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import io.reactivex.disposables.Disposable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/16 2:39
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TestActivity extends BaseActivity {
    private Disposable mDisposable;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test1;
    }

    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode
                    = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(lp);
    }


    @Override
    protected void initView() {
        ViewPager2 viewPager2 = findViewById(R.id.viewpager);

        RecyclerAdapter adapter = new RecyclerAdapter<String, RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
                NestedPhotoView view = viewHolder.itemView.findViewById(R.id.item_photo);
                String url = "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg";
                Glide.with(FcUtils.getContext()).load(url).into(view);
                view.viewPager2 = viewPager2;
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_photo;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        viewPager2.setAdapter(adapter);
        for (int i = 0; i < 10; i++)
            adapter.addData("");
        adapter.notifyDataSetChanged();
        viewPager2.setUserInputEnabled(false);


//        test1();
    }

    int verticalScrolloffset = 0;

    void test1() {
        RecyclerView listView = findViewById(R.id.list);

//        CardLayoutManager mCardLayoutManager = new CardLayoutManager(3, 0.5f);
//        listView.setLayoutManager(mCardLayoutManager);
//        listView.setLayoutManager(new ScrollSpeedLinearLayoutManger(this,
//                LinearLayoutManager.HORIZONTAL, false));
//        new PagerSnapHelper().attachToRecyclerView(listView);
        MeteorShowerManager meteorShowerManager = new MeteorShowerManager();
        listView.setLayoutManager(meteorShowerManager);

        RecyclerAdapter adapter = new RecyclerAdapter<String, RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
                ImageView view = viewHolder.itemView.findViewById(R.id.image);
                TextView textView = viewHolder.itemView.findViewById(R.id.title);
                textView.setText(String.valueOf(position));
                String url = "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg";
//                view.setImageURI(Uri.parse(url));
                Glide.with(FcUtils.getContext()).load(url).into(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FcUtils.showToast("点击了" + position);
                    }
                });
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_test01;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter1 = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView =
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test01, parent, false);
                return new RecyclerHolder(itemView) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                final int index = position;
                ImageView view = holder.itemView.findViewById(R.id.image);
                TextView textView = holder.itemView.findViewById(R.id.title);
                textView.setText(String.valueOf(position));
                String url = "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg";
//                view.setImageURI(Uri.parse(url));
                Glide.with(FcUtils.getContext()).load(url).into(view);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FcUtils.showToast("点击了"+index);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return Integer.MAX_VALUE;
            }
        };
        listView.setAdapter(adapter1);
        for (int i = 0; i < 100; i++)
            adapter.addData("");
        adapter1.notifyDataSetChanged();

        ValueAnimator animator = ValueAnimator.ofInt(
                meteorShowerManager.ITEM_SCROLL_HEIGHT * meteorShowerManager.MAX_COUNT);
        animator.setDuration(10*1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int offset = value - verticalScrolloffset;
                verticalScrolloffset = value;
//                    listView.smoothScrollBy(0, offset);
                listView.scrollBy(0, offset);

            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                verticalScrolloffset = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }


    void test() {

        TestView testView = findViewById(R.id.test);
        testView.setOnClickListener(v -> testView.start());

        IjkVideoView videoView = findViewById(R.id.video);
        videoView.setVideoPath("https://uploadstatic.mihoyo.com/hk4e/upload/officialsites/202012/zhongli_gameplayPV_final_V3_fix.mp4");
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }


}
