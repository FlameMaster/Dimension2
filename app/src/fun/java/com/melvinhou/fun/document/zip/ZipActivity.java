package com.melvinhou.fun.document.zip;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.activities.BaseActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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
 * = 时 间：2021/6/2 15:21
 * <p>
 * = 分 类 说 明：压缩文件相关
 * ================================================
 */
public class ZipActivity extends BaseActivity {

    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
    //  回调码
    private final static int FILE_RESULT_CODE_UNZIP = 250;
    private final static int FILE_RESULT_CODE_ZIP = 380;

    private TextView mZipPathView, mZipPath2View;
    private ListView mListView;
    private File mZipCacheFile;

    private ArrayAdapter<String> mListAdapter;
    //要压缩的文件列表
    private Uri[] mCdZipFiles;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_zip;
    }

    @Override
    protected void initView() {
        mZipPathView = findViewById(R.id.unzip_path);
        mZipPath2View = findViewById(R.id.zip_path);
        mListView = findViewById(R.id.zip_list);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.unzip_file_selector).setOnClickListener(v -> onZipFileSelect());
        findViewById(R.id.unzip_bt).setOnClickListener(v -> unZip());
        findViewById(R.id.zip_file_selector).setOnClickListener(v -> onFilesSelect());
        findViewById(R.id.zip_bt).setOnClickListener(v -> zipFiles());
        mListAdapter = new ArrayAdapter<>(FcUtils.getContext(), R.layout.item_zip_log, R.id.title);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    protected void initData() {

    }

    /**
     * 解压
     */
    @SuppressLint("CheckResult")
    private void unZip() {
        if (mZipCacheFile == null) {
            FcUtils.showToast("麻烦选择需要解压的文件");
            return;
        }
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    FileOutputStream fos = null;
                    InputStream is = null;
                    try {
                        ZipFile zipFile = new ZipFile(mZipCacheFile);
                        //输出文件夹
                        String outFolderName = getUnZipOutFile().getPath();
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
                    mZipPathView.setText("已解压,位置:" + filePath);
                    mZipPathView.setTextColor(ResourcesUtils.getColor(R.color.cyan));
                });
    }

    /**
     * 选择需要被解压的文件
     */
    private void onZipFileSelect() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_RESULT_CODE_UNZIP);
    }

    /**
     * 选择需要压缩的文件
     */
    private void onFilesSelect() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//文件浏览器
        intent.setType("*/*");//无类型限制
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//多选参数为true
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_RESULT_CODE_ZIP);
    }

    /**
     * 批量压缩文件（夹）
     *
     * @throws IOException 当压缩过程出错时抛出
     */
    @SuppressLint("CheckResult")
    public void zipFiles() {
        if (mCdZipFiles == null || mCdZipFiles.length <= 0) return;
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    //生成的压缩文件
                    File outzipFile = getZipOutFile(FileUtils.getFileNameForDate() + ".zip");
                    BufferedInputStream is = null;
                    ZipOutputStream zipout = null;
                    try {
                        zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                                outzipFile), BUFF_SIZE));
                        byte buffer[] = new byte[BUFF_SIZE];
                        //循环压缩
                        for (Uri uri : mCdZipFiles) {
                            is = new BufferedInputStream(
                                    getContentResolver().openInputStream(uri), BUFF_SIZE);
                            zipout.putNextEntry(new ZipEntry(getUriFileName(uri)));
                            int realLength;
                            while ((realLength = is.read(buffer)) != -1) {
                                zipout.write(buffer, 0, realLength);
                            }
                            zipout.flush();
                            zipout.closeEntry();
                        }
                        if (outzipFile.exists())
                            emitter.onNext(outzipFile.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.close(is);
                        IOUtils.close(zipout);
                        emitter.onComplete();
                    }
                })
                .compose(IOUtils.setThread())
                .subscribe(filePath -> {
                    mZipPath2View.setText("压缩完成,位置:" + filePath);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_RESULT_CODE_UNZIP && data.getData() != null) {
                //单选的
                updateZipFile(data.getData());
            } else if (requestCode == FILE_RESULT_CODE_ZIP) {
                if (data.getData() != null) {
                    updateWaitZipFile(new Uri[]{data.getData()});
                } else {
                    //多选的
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        Uri[] uris = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            uris[i] = item.getUri();
                        }
                        updateWaitZipFile(uris);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清空缓存文件
        FileUtils.clearDiskCache();
    }

    /**
     * 打开选中的uri
     * 复制一个到cache文件夹，返回的是cache文件夹的cope文件
     *
     * @param uri
     */
    @SuppressLint("CheckResult")
    private void updateZipFile(final Uri uri) {
        Observable
                .create((ObservableOnSubscribe<File>) emitter -> {
                    File file = null;
                    //ContentResolver.SCHEME_FILE/SCHEME_ANDROID_RESOURCE/SCHEME_CONTENT
                    String scheme = uri.getScheme();
                    InputStream is = null;
                    FileOutputStream fos = null;
                    try {
                        is = getContentResolver().openInputStream(uri);
                        if (is != null) {
                            byte[] buf = new byte[1024 * 1000];
                            file = getZipCacheFile(uri.getPath());
                            if (!file.exists()) {
                                fos = new FileOutputStream(file);
                                //开始写入
                                int len = 0;
                                while ((len = is.read(buf)) != -1) {
                                    fos.write(buf, 0, len);
                                }
                                fos.flush();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.close(is);
                        IOUtils.close(fos);
                        if (file != null) emitter.onNext(file);
                        emitter.onComplete();
                    }
                })
                .compose(IOUtils.setThread())
                .subscribe(file -> {
                    //判断是否能打开文件
                    if (file.exists()) {
                        mZipCacheFile = file;
                        mZipPathView.setText("待解压,位置:" + file.getPath());
                        mZipPathView.setTextColor(Color.BLACK);
                    }
                });
    }

    /**
     * 打开待压缩的文件
     *
     * @param uris
     */
    private void updateWaitZipFile(Uri[] uris) {
        mCdZipFiles = uris;
        mZipPath2View.setText("待压缩");
        //更新列表
        mListAdapter.clear();
        for (Uri uri : mCdZipFiles) {
            mListAdapter.add(uri.getPath());
            mListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取解压的文件
     *
     * @param fileName
     * @return
     */
    private File getZipCacheFile(String fileName) {
        String newName = StringUtils.md5(fileName);
        return new File(getExternalCacheDir(), newName != null ? newName : fileName);
    }

    /**
     * 获取解压后的文件夹
     *
     * @return
     */
    private File getUnZipOutFile() {
        String outFolderPath = FileUtils.getFileNameForDate();
        File folderFile = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Dimension2"
                + File.separator + "zip"
                + File.separator + outFolderPath);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return folderFile;
    }

    /**
     * 获取压缩后的文件目录
     *
     * @return
     */
    private File getZipOutFile(String fileName) {
        File folderFile = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Dimension2"
                + File.separator + "zip");
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        File file = new File(folderFile, fileName);
        return file;
    }


    /**
     * 获取文件名
     *
     * @param uri
     * @return
     */
    private static String getUriFileName(Uri uri) {
        String name = FileUtils.getFileNameForDate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Cursor cursor = FcUtils.getContext().getContentResolver()
                    .query(uri, null, null, null);
            cursor.moveToFirst();
            name = cursor.getString(cursor
                    .getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        return name;
    }


}
