package com.melvinhou.dimension2.test;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentTest04Binding;
import com.melvinhou.dimension2.media.animation.SvgAnimationActivity;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.mvvm.BaseModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.RetentionPolicy;

import androidx.recyclerview.widget.LinearLayoutManager;

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
public class TestFragment04 extends BindFragment<FragmentTest04Binding, BaseModel> {

    @Override
    protected FragmentTest04Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTest04Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseModel> openModelClazz() {
        return BaseModel.class;
    }


    @Override
    protected void initView() {

        RecyclerAdapter adapter = new RecyclerAdapter<String, RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_im_friend;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        mBinding.listView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.listView.setAdapter(adapter);
        for (int i = 0; i < 10; i++)
            adapter.addData("");
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void initListener() {
        mBinding.root.setSwipeListener(new Test4View.SwipeListener() {
            @Override
            public void onRefresh() {
                toActivity(new Intent(getContext(), SvgAnimationActivity.class));
                mBinding.root.startTop2Original();
            }
        });
    }

    @Override
    protected void initData() {

    }

    private MediaRecorder mediaRecorder = new MediaRecorder();
    private File audioFile;
    private boolean isRecording = false;
    public void recorder_Media(){
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            audioFile = File.createTempFile("recording",".3gp",getContext().getCacheDir());
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
