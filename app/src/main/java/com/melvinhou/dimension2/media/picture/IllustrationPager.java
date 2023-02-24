package com.melvinhou.dimension2.media.picture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.dimension2.PairEntity;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ItemIllustrationBD;
import com.melvinhou.dimension2.pager.BaseListPager;
import com.melvinhou.kami.adapter.DataBindingHolder;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
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
 * = 分 类 说 明：相册列表
 * ================================================
 */
public class IllustrationPager extends BaseListPager<IllustrationItem, IllustrationPager.IllustrationHolder> {

    int maxItemWidth = 0;
    int margin = 0;

    @Override
    public void onCreate(int position) {
        super.onCreate(position);
        margin = DimenUtils.dp2px(4);
        int[] size = DimenUtils.getScreenSize();
        maxItemWidth = size[0]/2 - margin * 3;
        getBinding().list.setPadding(margin, 0, margin, 0);
    }

    @Override
    protected IllustrationHolder getHolder(ViewDataBinding binding, int viewType) {
        return new IllustrationHolder((ItemIllustrationBD) binding);
    }

    @Override
    protected int getItemLayoutID(int viewType) {
        return R.layout.item_illustration;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected void onItemBind(IllustrationHolder viewHolder, int realPosition, IllustrationItem data) {
        viewHolder.updata(data);

    }

    @SuppressLint("CheckResult")
    @Override
    public void loadData(int size, int page) {
        if (page == 1)
            Observable.create((ObservableOnSubscribe<IllustrationInfo>) emitter -> {
                IllustrationInfo entity = LoadUtils.getData(
                        LoadUtils.SOURCE_ASSETS, AssetsFileKey.MEDIAT_ILLUSTRATION_LIST,
                        new TypeToken<CYEntity<IllustrationInfo>>() {
                        });
                emitter.onNext(entity);
                emitter.onComplete();
            })
                    .compose(IOUtils.setThread())
                    .subscribe(entity -> {
                        if (entity != null) {
                            if (entity.getList() != null) {
                                getAdapter().addDatas(entity.getList());
                            }
                            updateRequestState(RequestState.EMPTY);
                            updataTailState(ResultState.FAILED);
                            updateLoadingState(false);
                        }
                    });
    }

    @Override
    public void onItemClick(IllustrationHolder viewHolder, int position, IllustrationItem data) {
        Intent intent = new Intent(FcUtils.getContext(), PictureActivity.class);
        intent.putExtra("url", data.getUrl());
//        toActivity( intent);

        RxBus.instance().post(RxBusMessage.Builder
                .instance(RxBusMessage.CommonType.ACTIVITY_LAUNCH)
                .client(getClass().getName())
                .attach(new PairEntity(viewHolder.itemView,intent))
                .build());
    }


    class IllustrationHolder extends DataBindingHolder<ItemIllustrationBD> {

        public IllustrationHolder(ItemIllustrationBD binding) {
            super(binding);
        }

        public void updata(IllustrationItem data) {
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            int height = data.getWidth() > 0 ? maxItemWidth * data.getHeight() / data.getWidth() : 0;
            if (lp.height != height) {
                lp.height = height + margin * 2;
                itemView.setLayoutParams(lp);
            }
            getBinding().setHeight(height);
            getBinding().setWidth(maxItemWidth);
            getBinding().setItem(data);
        }
    }
}
