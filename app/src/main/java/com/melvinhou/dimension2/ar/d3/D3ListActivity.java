package com.melvinhou.dimension2.ar.d3;

import android.Manifest;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.helper.DownloadHelper;
import com.melvinhou.kami.manager.DialogCheckBuilder;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.view.BaseActivity2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;

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
 * = 分 类 说 明：屏幕录制
 * ================================================
 */
public class D3ListActivity extends BaseActivity2 {

    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

    private final String TAG = D3ListActivity.class.getName();
    //权限请求
    public static final int REQUEST_CODE_PERMISSIONS = 2113;
    public static final int REQUEST_CODE_PERMISSIONS2 = 2114;
    //权限列表：文件
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    //管理进程的
    private final CompositeDisposable mDisposable = new CompositeDisposable();


    private RecyclerView mRecycler;
    private MyAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_d3_list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted(REQUIRED_PERMISSIONS))
            loadData();
    }

    @Override
    protected void onDestroy() {
        mDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected int getLoadDialogThemeID() {
        return R.style.Dimension2Dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK && resultCode == REQUEST_CODE_PERMISSIONS2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&Environment.isExternalStorageManager())
                FcUtils.showToast("获取权限成功");
            else FcUtils.showToast("未能获取权限");
        }
    }

    @Override
    protected void initView() {
        mRecycler = findViewById(R.id.list);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    protected void initListener() {
        mRecycler.setLayoutManager(new GridLayoutManager(FcUtils.getContext(), 2));
        mAdapter = new MyAdapter();
        mRecycler.setAdapter(mAdapter);
        mRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //设定底部边距为
                int height = DimenUtils.dp2px(10);
                outRect.set(0, 0, 0, height);
            }
        });

        mAdapter.setOnItemClickListener((viewHolder, position, data) -> {
            if (data.isDownload()) {
                Intent intent = new Intent(D3ListActivity.this, D3Activity.class);
                intent.putExtra("objName", data.getFileName() + ".obj");
                intent.putExtra("objPath", data.getDirectoryPath());
                startActivity(intent);
            } else if (allPermissionsGranted(REQUIRED_PERMISSIONS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())
                    downloadObj(data);
                else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, REQUEST_CODE_PERMISSIONS2);
                }
            } else {
                FcUtils.showToast("请开启文件读写权限后重试");
            }
        });
    }


    @Override
    protected void initData() {
        View view = LayoutInflater.from(FcUtils.getContext()).inflate(R.layout.item_loadmore, mRecycler, false);
        mAdapter.addTailView(view);
        view.setVisibility(View.INVISIBLE);
        //权限申请
        if (allPermissionsGranted(REQUIRED_PERMISSIONS))
            loadData();
        else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    //加载数据
    private void loadData() {
        mDisposable.add(Observable.create((ObservableOnSubscribe<ArrayList<D3Entity>>) emitter -> {
            ArrayList<D3Entity> list = LoadUtils.getData(
                    LoadUtils.SOURCE_ASSETS,//资源位置
                    AssetsFileKey.AR_D3_LIST,//资源文件
                    new TypeToken<CYEntity<ArrayList<D3Entity>>>() {
                    });
            Map<String, D3Entity> map = new ArrayMap<>();
            File dir = getModelFilesDir();
            if (dir.isDirectory()) {// 处理目录
                File[] files = dir.listFiles();
                assert files != null;
                for (File file : files) {
                    D3Entity d3Entity = new D3Entity();
                    String fileName = file.getName();
                    d3Entity.setFileName(fileName);
                    d3Entity.setDirectoryPath(file.getAbsolutePath());
                    d3Entity.setDownload(true);
                    d3Entity.setExplain("文件大小：" +
                            showLongFileSzie(new File(file, fileName + ".obj").length()));
                    map.put(fileName, d3Entity);
                }
            }
            for (D3Entity entity : list) {
                D3Entity newEn = map.get(entity.getFileName());
                if (newEn != null) {
                    entity.setDirectoryPath(newEn.getDirectoryPath());
                    entity.setExplain(newEn.getExplain());
                    entity.setDownload(true);
                }
            }
            emitter.onNext(list);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(list -> {
                    //加载
                    mAdapter.clearData();
                    mAdapter.addDatas(list);
                }));
    }

    /****
     * 计算文件大小
     *
     * @param length
     * @return
     */
    public String showLongFileSzie(Long length) {
        if (length >= 1048576) {
            return (length / 1048576) + "MB";
        } else if (length >= 1024) {
            return (length / 1024) + "KB";
        } else if (length < 1024) {
            return length + "B";
        } else {
            return "0KB";
        }
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            loadData();
    }

    @Override
    protected void onPermissionCancel(int requestCode) {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            FcUtils.showToast("没有权限读取文件");
    }

    public File getModelFilesDir() {
        File folderFile = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + ResourcesUtils.getString(R.string.app_name) + File.separator + "model");
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return folderFile;
    }


    private void downloadObj(D3Entity data) {
        showProcess("下载模型中...");
        showCheckView(new DialogCheckBuilder("下载提醒",
                "是否下载[" + data.getTitle() + "]所属模型文件？",
                "下载", "取消") {
            @Override
            public void confirm() {
                downloadFile(data.getFileName(), data.getUrl());
            }
        });
    }

    //下载文件
    private void downloadFile(String fileName, String url) {
        DownloadHelper helper = DownloadHelper.getInstance(url, fileName + ".zip");
        helper.setDownloadListener(new DownloadHelper.DownloadListener() {
            @Override
            public void onStart() {
                Log.e(TAG, "开始下载");
            }

            @Override
            public void onProgress(long soFarSize, long totalSize) {
                Log.e(TAG, "下载中：" + "soFarSize：" + soFarSize + "    totalSize：" + totalSize);

            }

            @Override
            public void onFinish(String fileFullPath, long totalSize) {
                helper.clear();
                Log.e(TAG, "下载完成：" + fileFullPath);
                unZipObj(new File(fileFullPath));
            }

            @Override
            public void onFailed() {
                Log.e(TAG, "下载失败");
                helper.clear();
            }
        });
        helper.start();
    }

    private void unZipObj(File file) {
        showProcess("解压模型中...");
        String folderPath =
                ResourcesUtils.getString(com.melvinhou.kami.R.string.app_name)
                        + "/model/" + file.getName();
        File folderFile = Environment.getExternalStoragePublicDirectory(folderPath.substring(0, folderPath.length() - 4));
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        unZip(file, folderFile.getAbsolutePath());
    }

    //解压文件
    private void unZip(File file, String path) {
        mDisposable.add(Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    FileOutputStream fos = null;
                    InputStream is = null;
                    try {
                        ZipFile zipFile = new ZipFile(file);
                        //输出文件夹
                        String outFolderName = path;
                        Enumeration e = zipFile.entries();
                        byte[] buffer = new byte[BUFF_SIZE];
                        ZipEntry entry;
                        File dstFile;
                        while (e.hasMoreElements()) {
                            entry = (ZipEntry) e.nextElement();
                            is = new BufferedInputStream(zipFile.getInputStream(entry));
                            dstFile = new File(outFolderName + File.separator + entry.getName());
                            //判断是否是文件夹
                            if (entry.isDirectory()) {
                                if (!dstFile.exists()) {
                                    dstFile.mkdirs();
                                }
                            } else {
                                fos = new FileOutputStream(dstFile);
                                int count = 0;
                                while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                                    fos.write(buffer, 0, count);
                                }
                            }
                        }
                        emitter.onNext(outFolderName);
                        file.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        emitter.onError(e);
                    } finally {
                        IOUtils.close(is);
                        IOUtils.close(fos);
                        emitter.onComplete();
                    }
                })
                .compose(IOUtils.setThread())
                .subscribe(filePath -> {
                    //重新加载
                    FcUtils.showToast("解压完成");
                    hideProcess();
                    loadData();
                }));
    }

    class MyAdapter extends RecyclerAdapter<D3Entity, MyHolder> {

        @Override
        public void bindData(MyHolder viewHolder, int position, D3Entity data) {
            viewHolder.update(data);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_d3;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            return new MyHolder(View);
        }
    }

    class MyHolder extends RecyclerHolder {

        TextView title, text, download;
        ImageView img;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_name);
            text = itemView.findViewById(R.id.tv_other);
            download = itemView.findViewById(R.id.tv_download);
            img = itemView.findViewById(R.id.iv_img);
        }

        public void update(D3Entity data) {
            title.setText(TextUtils.isEmpty(data.getTitle()) ? data.getFileName() : data.getTitle());
            text.setText(data.getExplain());
            Glide.with(FcUtils.getContext())
                    .load(data.getCover())
                    .apply(new RequestOptions()
                            .override(160, 240)
                            .placeholder(R.mipmap.andy)
                            .error(R.mipmap.andy)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(img);
            download.setText(data.isDownload() ? "已下载" : "未下载");
            download.setSelected(data.isDownload());
        }
    }

}
