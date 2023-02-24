package com.melvinhou.dimension2.media.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.NullBoxBD;
import com.melvinhou.dimension2.pager.BaseListPager;
import com.melvinhou.kami.adapter.DataBindingHolder;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/10 1:24
 * <p>
 * = 分 类 说 明：tv列表
 * ================================================
 */
public class TVListPager extends BaseListPager<MapEntity, TVListPager.TVHolder> {

    private static final String REGEX_ITEM = "#EXTVLCOPT:network-caching=1000";
    private static final String REGEX_LIST = "#EXTINF:0,";


    @Override
    protected void initData() {
        super.initData();
        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                FcUtils.getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ResourcesUtils.getDrawable(R.drawable.ic_line_h24));
        getBinding().list.addItemDecoration(itemDecoration);
    }

    @Override
    protected TVHolder getHolder(ViewDataBinding binding, int viewType) {
        return new TVHolder((NullBoxBD) binding);
    }

    @Override
    protected int getItemLayoutID(int viewType) {
        return R.layout.null_box;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(FcUtils.getContext(), 2);
    }

    @Override
    protected void onItemBind(TVHolder viewHolder, int realPosition, MapEntity data) {
        viewHolder.updata(data);
    }

    @SuppressLint("CheckResult")
    @Override
    public void loadData(int size, int page) {
        if (page == 1)
            Observable.create((ObservableOnSubscribe<List<MapEntity>>) emitter -> {
                List<MapEntity> entity = new ArrayList<>();
                String text = LoadUtils.readAssetsTxt(AssetsFileKey.MEDIAT_TV_LIST);
                text = text
                        .replaceAll(" ", "")
                        .replaceAll("\r|\n", "");
                String[] datas = text.split(REGEX_LIST);
                for (String data : datas) {
                    if (StringUtils.nonEmpty(data)) {
                        String[] item = data.split(REGEX_ITEM);
                        if (item != null && item.length > 1)
                            entity.add(new MapEntity(item[0], item[1]));
                    }
                }
                emitter.onNext(entity);
                emitter.onComplete();
            })
                    .compose(IOUtils.setThread())
                    .subscribe(entity -> {
                        if (entity != null) {
                            getAdapter().addDatas(entity);
                            updateRequestState(RequestState.EMPTY);
                            updataTailState(ResultState.FAILED);
                            updateLoadingState(false);
                        }
                    });
    }

    @Override
    public void onItemClick(TVHolder viewHolder, int position, MapEntity data) {
        Intent intent = new Intent(FcUtils.getContext(), VideoActivity2.class);
        intent.putExtra("url", data.getValue());
        intent.putExtra("title", data.getKey());
        intent.putExtra("mode", true);
        toActivity(intent);
    }


    class TVHolder extends DataBindingHolder<NullBoxBD> {
        TextView tv;

        public TVHolder(NullBoxBD binding) {
            super(binding);
            tv = new TextView(FcUtils.getContext());
            binding.box.addView(tv);
            binding.box.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            tv.setTextColor(ResourcesUtils.getColor(R.color.colorPrimary));
            tv.setPadding(DimenUtils.dp2px(16),DimenUtils.dp2px(8),DimenUtils.dp2px(16),DimenUtils.dp2px(8));
        }

        public void updata(MapEntity data) {
            tv.setText(data.getKey());
        }
    }
}
