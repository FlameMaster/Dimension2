package com.melvinhou.kami.mvvm;

import android.content.Intent;

import com.melvinhou.kami.view.BaseActivity2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 15:25
 * <p>
 * = 分 类 说 明：mvvm的基类
 * ================================================
 */
public abstract class BindActivity<VB extends ViewBinding, M extends BaseModel> extends BaseActivity2 {

    //新版本的意图打开
    private ActivityResultLauncher<Intent> startActivity;

    protected VB mBinding;
    protected M mModel;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Override
    protected void initActivity(int layoutId) {
        startActivity =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        BindActivity.this::onActivityBack);
        mBinding = openViewBinding();
        setContentView(mBinding.getRoot());
        mModel = new ViewModelProvider(this).get(openModelClazz());
        mModel.register();
        super.initActivity(layoutId);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModel != null)
            mModel.unRegister();
    }


    protected abstract VB openViewBinding();

    protected abstract Class<M> openModelClazz();


    /**
     * 打开有返回值的intent
     *
     * @param intent
     */
    protected void toResultActivity(Intent intent, ActivityResultCallback<ActivityResult> callback) {
        ActivityResultContracts.StartActivityForResult result
                = new ActivityResultContracts.StartActivityForResult();
        registerForActivityResult(result, callback)
                .launch(intent);
    }


    /**
     * 打开有返回值的intent
     *
     * @param intent
     */
    protected void toResultActivity(Intent intent) {
        startActivity.launch(intent);
    }

    /**
     * 替换早期的返回
     */
    protected void onActivityBack(ActivityResult result) {
        //此处进行数据接收（接收回调）
        if (result.getResultCode() == RESULT_OK) {
        }
    }
}
