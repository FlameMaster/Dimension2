package com.melvinhou.dimension2.game.klotski;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.BaseActivity;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/26 20:40
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class GameKlotskiActivity extends BaseActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_klotski;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        loadNetImage(R.id.caocao,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/caocao.jpg");
        loadNetImage(R.id.guanyu,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/guanyu.jpg");
        loadNetImage(R.id.zhangfei,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/zhangfei.jpg");
        loadNetImage(R.id.zhaoyun,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/zhaoyun.jpg");
        loadNetImage(R.id.huangzhong,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/huangzhong.jpg");
        loadNetImage(R.id.machao,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/machao.jpg");
        loadNetImage(R.id.bin01,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/zu03.jpg");
        loadNetImage(R.id.bin02,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/zu01.jpg");
        loadNetImage(R.id.bin03,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/zu04.jpg");
        loadNetImage(R.id.bin04,"https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/game/klotski/zu02.jpg");
    }

    private void loadNetImage(int id,String url){
        ImageView view = findViewById(id);
        if (view==null)return;
        Glide.with(FcUtils.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.fc)
                        .error(R.mipmap.fc)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(view);
    }
}
