package com.melvinhou.model3d_sample.sample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.melvinhou.kami.bean.FcEntity;
import com.melvinhou.kami.io.DownloadHelper;
import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.net.RequestCallback;
import com.melvinhou.kami.tool.AssetsUtil;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.model3d_sample.R;
import com.melvinhou.model3d_sample.api.AssetsService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.MutableLiveData;
import de.javagl.obj.Obj;
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
 * = 时 间：2021/6/20 19:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class D3SampleModel extends BaseViewModel {
    private final String TAG = D3SampleModel.class.getName();

    public D3SampleModel(@NonNull Application application) {
        super(application);
    }

    MutableLiveData<List<D3SampleEntity>> list = new MutableLiveData<>();


    @SuppressLint("CheckResult")
    void loadListData() {
        AssetsUtil.loadData(
                        "sample_media_list.json",
                        D3SampleEntity.class, ArrayList.class)
                .subscribe(data -> {
                    Log.e("获取数据", "长度=${data?.data?.size ?: -1}");
                    list.postValue((List<D3SampleEntity>) data.getData());
                });
    }

    /**
     * 加载列表
     */
    void loadList() {
        Observable<FcEntity<ArrayList<D3SampleEntity>>> observable = AssetsService.instance.Api().getD3List();
        requestData(observable, new RequestCallback<ArrayList<D3SampleEntity>>() {
            @Override
            public void onSuceess(ArrayList<D3SampleEntity> data) {
                updateList(data);
            }
        });
    }

    /**
     * 更新数据
     *
     * @param datas 原始数据
     */
    private void updateList(ArrayList<D3SampleEntity> datas) {
        Map<String, D3SampleEntity> map = new ArrayMap<>();
        File dir = getModelFilesDir(null);
        // 处理目录
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            assert files != null;
            for (File file : files) {
                D3SampleEntity d3Entity = new D3SampleEntity();
                String fileName = file.getName();
                d3Entity.setFileName(fileName);
                d3Entity.setDirectoryPath(file.getAbsolutePath());
                d3Entity.setDownload(true);
                d3Entity.setExplain("文件大小：" + FileUtils.getFileSizeText(new File(file, fileName + ".obj").length()));
                map.put(fileName, d3Entity);
            }
        }
        for (D3SampleEntity entity : datas) {
            D3SampleEntity newEn = map.remove(entity.getFileName());
            if (newEn != null) {
                entity.setDirectoryPath(newEn.getDirectoryPath());
                entity.setExplain(newEn.getExplain());
                entity.setDownload(true);
            }
        }
        //剩余的本地目录
        for (String key : map.keySet()) {
            D3SampleEntity newEn = map.get(key);
            datas.add(newEn);
        }
        list.postValue(datas);
    }

    /**
     * 存储目录
     *
     * @param subFolderName 子目录
     * @return
     */
    public File getModelFilesDir(String subFolderName) {
        String folderPath =
                ResourcesUtils.getString(R.string.app_name) + File.separator + ".model";
        if (!TextUtils.isEmpty(subFolderName)) {
            folderPath = folderPath + File.separator + subFolderName;
        }
        File folderFile = Environment.getExternalStoragePublicDirectory(folderPath);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return folderFile;
    }


    /**
     * 下载模型文件
     *
     * @param fileName
     * @param url
     */
    void downloadModelFile(String fileName, String url, CallBack<File> callback) {
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
                callback.callback(new File(fileFullPath));
            }

            @Override
            public void onFailed() {
                Log.e(TAG, "下载失败");
                callback.callback(null);
                helper.clear();
            }
        });
        helper.start();
    }


    /**
     * 解压下载的模型包
     *
     * @param file
     * @param callback
     */
    void unZipObj(File file, CallBack<Boolean> callback) {
        isRequest.setValue(true);
        String fileName = file.getName();
        String path = getModelFilesDir(fileName.substring(0, fileName.length() - 4)).getAbsolutePath();
        //添加解压任务
        addDisposable(Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    FileOutputStream fos = null;
                    InputStream is = null;
                    int BUFF_SIZE = 1024 * 1024; // 1M Byte
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
                        emitter.onNext(null);
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
                    callback.callback(filePath != null);
                }));
    }
}
