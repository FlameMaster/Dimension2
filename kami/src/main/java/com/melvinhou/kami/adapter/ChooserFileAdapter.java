package com.melvinhou.kami.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.kami.R;
import com.melvinhou.kami.bean.UriFileInfo;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.StringCompareUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
 * = 分 类 说 明：文件选择适配器
 * ============================================================
 */
public class ChooserFileAdapter extends RecyclerAdapter<UriFileInfo, ChooserFileAdapter.Helper> {

    /*最多选择几张*/
    private int maxCount;
    private ChooserFileListener mChooserFileListener;

    public ChooserFileAdapter() {
        super();
        maxCount = Integer.MAX_VALUE;
        addData(new UriFileInfo());
    }

    /**
     * 设置最大值
     *
     * @param maxCount
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    /**
     * 添加文件
     *
     * @param data
     */
    public void putFile(UriFileInfo data) {
        //添加文件
        removedData(getDatas().size() - 1);
        addData(data);
        if (getDatas().size() < maxCount)
            addData(new UriFileInfo());
    }

    public void setChooserFileListener(ChooserFileListener listener) {
        mChooserFileListener = listener;
    }


    public RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(FcUtils.getContext());
    }

    @Override
    public void bindData(Helper viewHolder, int position, UriFileInfo data) {
        if (mChooserFileListener != null)
            viewHolder.setChooserFileListener(position, mChooserFileListener);
        viewHolder.updata(position, data);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.items_file_choose;
    }

    @Override
    protected Helper onCreate(View View, int viewType) {
        Helper helper = new Helper(View);
        return helper;
    }

    /**
     * 实现交互
     */
    public static class Helper extends RecyclerHolder {
        private ImageView mImageView;
        private TextView mTextView;
        private View mDeleteView, mAddView;

        public Helper(@NonNull View itemView) {
            super(itemView);
            //
            mImageView = itemView.findViewById(R.id.img);
            mTextView = itemView.findViewById(R.id.name);
            mDeleteView = itemView.findViewById(R.id.delete);
            mAddView = itemView.findViewById(R.id.add);
        }

        public void setChooserFileListener(int position, ChooserFileListener listener) {
            mDeleteView.setOnClickListener(v -> listener.detele(position));
            mAddView.setOnClickListener(v -> listener.openChooser());
        }

        public void updata(int position, UriFileInfo data) {
            mTextView.setText(data.getFileName());
            if (data.getUri() != null) {
                if (StringCompareUtils.isImageFile(data.getFileName())) {
                    RequestOptions options = new RequestOptions()
                            .override(DimenUtils.dp2px(100), DimenUtils.dp2px(100))
//                            .placeholder(R.mipmap.icon_hint_empty)
//                            .error(R.mipmap.icon_hint_empty)
                            ;
                    Glide.with(FcUtils.getContext())
                            .asBitmap()
                            .load(data.getUri())
                            .apply(options)
                            .into(mImageView);
                }
            } else if (data.getFilePath() != null) {
                if (StringCompareUtils.isImageFile(data.getFileName())) {
                    RequestOptions options = new RequestOptions()
                            .override(DimenUtils.dp2px(100), DimenUtils.dp2px(100))
//                            .placeholder(R.mipmap.icon_hint_empty)
//                            .error(R.mipmap.icon_hint_empty)
                            ;
                    Glide.with(FcUtils.getContext())
                            .asBitmap()
                            .load(data.getFilePath())
                            .apply(options)
                            .into(mImageView);
                }
            }
        }
    }

    public interface ChooserFileListener {

        /**
         * 删除
         *
         * @param position
         */
        void detele(int position);

        /**
         * 打开文件选择器
         */
        void openChooser();
    }


}
