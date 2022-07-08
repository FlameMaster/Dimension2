package com.melvinhou.dimension2.function;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.function.desktop.DesktopActivity;
import com.melvinhou.dimension2.function.messenger.ImHomeActivity;
import com.melvinhou.dimension2.function.pdf.PdfActivity;
import com.melvinhou.dimension2.function.screenrecord.ScreenRecordActivity;
import com.melvinhou.dimension2.function.zip.ZipActivity;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FunctionFragment extends Fragment {

    private static final String ITEM_KEY_IMAGE = "image";
    private static final String ITEM_KEY_TITLE = "title";

    private FunctionViewModel functionViewModel;
    private View root;
    private RecyclerView mListView;
    private MyAdapter mAdapter;

    // 图片封装为一个数组
    private int[] icon = {
            R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_round,
            R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_round,
            R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_round,
            R.mipmap.ic_launcher_round
    };

    //聊天，录屏，pdf,投屏，桌面，压缩，地图，扫描
    private String[] title = {
            "聊天", "录屏",
            "网络", "PDF",
            "投屏", "桌面",
            "压缩"
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        functionViewModel =
                ViewModelProviders.of(this).get(FunctionViewModel.class);
        root = inflater.inflate(R.layout.fragment_function, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        mListView = root.findViewById(R.id.list);

    }

    private void initListener() {
        mAdapter = new MyAdapter();
        //配置适配器
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(new GridLayoutManager(FcUtils.getContext(), 5){
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mAdapter.setOnItemClickListener(this::onItemClick);

        //
        functionViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });
    }

    private void initData() {
        loadData();

    }

    public void loadData() {
        List<Map<String, Object>> data_list = new ArrayList<>();
        for (int i = 0; i < icon.length; i++) {
            Map<String, Object> map = new ArrayMap<>();
            map.put(ITEM_KEY_IMAGE, icon[i]);
            map.put(ITEM_KEY_TITLE, title[i]);
            data_list.add(map);
        }
        mAdapter.addDatas(data_list);
    }

    /**
     * 条目点击
     *
     * @param viewHolder
     * @param position
     * @param data
     */
    public void onItemClick(MyHolder viewHolder, int position, Map<String, Object> data) {
        String key = (String) data.get(ITEM_KEY_TITLE);
        if (title[0].equals(key)){//聊天
            Intent intent = new Intent(getContext(), ImHomeActivity.class);
            startActivity(intent);
        }else if (title[1].equals(key)){//录屏
            Intent intent = new Intent(getContext(), ScreenRecordActivity.class);
            startActivity(intent);
        }else if (title[5].equals(key)){//桌面
            Intent intent = new Intent(getContext(), DesktopActivity.class);
            startActivity(intent);
        }else if (title[6].equals(key)){//zip
            Intent intent = new Intent(getContext(), ZipActivity.class);
            startActivity(intent);
        }else  if (title[3].equals(key)){//pdf
            String url = HttpConstant.SERVER_RES +"pdf/product.pdf";
            Intent intent = new Intent(getContext(), PdfActivity.class);
            intent.putExtra("url",url);
            intent.putExtra("title","测试专用PDF");
            startActivity(intent);
        }else {
            FcUtils.showToast("功能开发中...");
        }
    }


    public void toActivity(View view, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> p = new Pair<>(view, view.getTransitionName());
            ActivityOptions activityOptions =
                    ActivityOptions.makeSceneTransitionAnimation(getActivity(), p);
            startActivityForResult(intent, 0, activityOptions.toBundle());
        } else
            startActivityForResult(intent, 0);
    }


    static class MyAdapter extends RecyclerAdapter<Map<String, Object>, MyHolder> {
        @Override
        public void bindData(MyHolder viewHolder, int position, Map<String, Object> data) {
            viewHolder.update((int) data.get(ITEM_KEY_IMAGE), (String) data.get(ITEM_KEY_TITLE));
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_function;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            return new MyHolder(View);
        }
    }

    static class MyHolder extends RecyclerHolder {
        ImageView imageView;
        TextView textView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.title);
        }

        public void update(int iconResource, String title) {
            imageView.setImageResource(iconResource);
            textView.setText(title);
        }

    }
}
