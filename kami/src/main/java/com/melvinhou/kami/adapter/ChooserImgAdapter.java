package com.melvinhou.kami.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.kami.R;
import com.melvinhou.kami.model.UriFileInfo;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/4/18 20:30
 * <p>
 * = 分 类 说 明：图片选择适配器
 * ============================================================
 */
public class ChooserImgAdapter extends ChooserFileAdapter {


    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(FcUtils.getContext(), 4);
//        return new LinearLayoutManager(FcUtils.getContext(), LinearLayoutManager.HORIZONTAL, false);
    }

    @Override
    public void bindData(ChooserFileAdapter.Helper viewHolder, int position, UriFileInfo data) {
        super.bindData(viewHolder, position, data);
//        viewHolder.updata(position, data);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.items_img_choose;
    }

    @Override
    protected ChooserFileAdapter.Helper onCreate(View view, int viewType) {
        Helper helper = new Helper(view);
        return helper;
    }


    /**
     * 实现交互
     */
    public class Helper extends ChooserFileAdapter.Helper {
        private ImageView mImageView;
        private View mDeleteView, mAddView;

        public Helper(View view) {
            super(view);
            //初始大小
            int size = DimenUtils.dp2px(86);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
            view.setLayoutParams(lp);
            //
            mImageView = view.findViewById(R.id.img);
            mDeleteView = view.findViewById(R.id.delete);
            mAddView = view.findViewById(R.id.add);
        }

        @Override
        public void setChooserFileListener(int position, ChooserFileListener listener) {
            mDeleteView.setOnClickListener(v -> listener.detele(position));
            mAddView.setOnClickListener(v -> listener.openChooser());
        }

        @Override
        public void updata(int position, UriFileInfo data) {
            //位置
            boolean isEmpty = data.getUri() == null;
            mImageView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
            mDeleteView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            mAddView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (!isEmpty) {
                RequestOptions options = new RequestOptions()
                        .override(DimenUtils.dp2px(100), DimenUtils.dp2px(100))
//                        .placeholder(R.drawable.ic_launcher_background)
//                        .error(R.drawable.ic_launcher_background)
                        ;
                Glide.with(FcUtils.getContext())
                        .asBitmap()
                        .load(data.getUri())
                        .apply(options)
                        .into(mImageView);
            }
        }
    }
}
