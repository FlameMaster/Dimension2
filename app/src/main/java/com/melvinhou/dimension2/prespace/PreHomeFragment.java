package com.melvinhou.dimension2.prespace;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentSpacePreBinding;
import com.melvinhou.kami.mvvm.BindFragment;

import java.io.File;

import androidx.activity.result.ActivityResult;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 16:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class PreHomeFragment extends BindFragment<FragmentSpacePreBinding, SpacePreModel> {
    @Override
    protected FragmentSpacePreBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSpacePreBinding.inflate(inflater, container, false);
    }

    @Override
    protected Class<SpacePreModel> openModelClazz() {
        return SpacePreModel.class;
    }

    //获取单例
    private static PreHomeFragment fragment;

    public static PreHomeFragment getInstance() {
        if (fragment == null)
            fragment = new PreHomeFragment();
        return fragment;
    }

    @Override
    public void backward() {
        mModel.page.postValue(-1);
    }

    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("隐私档案");

        mBinding.ivPicture.setImageResource(R.mipmap.fc);
        mBinding.ivVideo.setImageResource(R.mipmap.fc);
        mBinding.ivFile.setImageResource(R.mipmap.fc);
    }

    @Override
    protected void initListener() {
        mBinding.tabGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.tab01:
                        mModel.locationType.postValue(1);
                        break;
                    case R.id.tab02:
                        mModel.locationType.postValue(2);
                        break;
                    case R.id.tab03:
                        mModel.locationType.postValue(3);
                        break;
                    case R.id.tab04:
                        mModel.locationType.postValue(4);
                        break;
                    case R.id.tab05:
                        openDir(Uri.fromFile(Environment.getExternalStorageDirectory()));
                        break;
                }
            }
        });

        //打开列表
        mBinding.ivPicture.setOnClickListener(v -> mModel.page.postValue(SpacePreActivity.PAGE_PICTURE));
        mBinding.ivVideo.setOnClickListener(v -> mModel.page.postValue(SpacePreActivity.PAGE_VIDEO));
        mBinding.ivFile.setOnClickListener(v -> mModel.page.postValue(SpacePreActivity.PAGE_FILE));
    }


    @SuppressLint("CheckResult")
    @Override
    protected void initData() {
            mBinding.tabGroup.check(R.id.tab01);
            //file=/storage/emulated/0/Android/data/com.melvinhou.dimension2/files
            //file=/storage/6E9D-3084/Android/data/com.melvinhou.dimension2/files
            File[] files = getContext().getExternalFilesDirs(null);//能获取sd卡的android目录
            for (File file : files) {
                Log.e("setOnClickListener", "file=" + file.getAbsolutePath());
            }
            mBinding.tab02.setText(mBinding.tab02.getText() + getFileSize(Environment.getExternalStorageDirectory()));
            //sd卡和usb
            if (files.length > 1) {
                mBinding.tab03.setVisibility(View.VISIBLE);
                mBinding.tab03.setText(mBinding.tab03.getText() + getFileSize(files[1]));
            }
            if (files.length > 2) {
                mBinding.tab04.setVisibility(View.VISIBLE);
                mBinding.tab04.setText(mBinding.tab04.getText() + getFileSize(files[2]));
            }

    }

    //获取指定目录的访问权限
    public void openDir(Uri parcelable) {
        //uri=content://com.android.externalstorage.documents/tree/6E9D-3084%3Abt
        //uri=file:///storage/emulated/0
        //uri=content://com.android.externalstorage.documents/tree/primary%3A%E3%80%90fairytail%E3%80%91
        Log.e("startFor", "uri=" + parcelable);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, parcelable);
        }
        toResultActivity(intent);//开始授权
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityBack(ActivityResult result) {
        super.onActivityBack(result);
        //此处进行数据接收（接收回调）
        if (result.getResultCode() == Activity.RESULT_OK) {
            Uri uri = null;
            if (result.getData() != null) {
                //打开文件
                uri = result.getData().getData();
                // Perform operations on the document using its URI.
                //保留权限
                final int takeFlags = result.getData().getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

                Log.e("onActivityBack", "uri=" + uri);
                mModel.useLocationUri.postValue(uri);
                mModel.locationType.postValue(5);
            }
        }
    }

    /**
     * 获取大小
     *
     * @param file
     * @return
     */
    private String getFileSize(File file) {
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long avBlocks = stat.getAvailableBlocksLong();
        return new StringBuffer()
                .append("\r\t(")
                .append(Formatter.formatFileSize( getContext(), blockSize * avBlocks))
                .append("/")
                .append(Formatter.formatFileSize( getContext(), blockSize * totalBlocks))
                .append(")")
                .toString();
    }
}
