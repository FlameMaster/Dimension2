package com.melvinhou.ar_sample.sample;

import android.content.Intent;

import com.melvinhou.model3d_sample.sample.D3SampleEntity;
import com.melvinhou.model3d_sample.sample.D3SampleListActivity;

import androidx.annotation.NonNull;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/3 19:49
 * <p>
 * = 分 类 说 明：模型列表@Assets:sample_d3_list.json
 * ================================================
 */
public class ArSampleListActivity extends D3SampleListActivity {

    @Override
    protected void initView() {
        super.initView();
        mBinding.bar.setTitle("AR模型选择");
    }

    @Override
    protected void toShowActivity(@NonNull D3SampleEntity data) {
//        super.toShowActivity(data);
        Intent intent = new Intent(this, ArSampleActivity.class);
        intent.putExtra("objName", data.getFileName() + ".obj");
        intent.putExtra("objPath", data.getDirectoryPath());
        startActivity(intent);
    }
}
