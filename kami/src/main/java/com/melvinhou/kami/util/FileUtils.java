package com.melvinhou.kami.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.melvinhou.kami.BaseApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import androidx.core.content.FileProvider;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/27 16:57
 * <p>
 * = 分 类 说 明：文件操作
 * ================================================
 */

public class FileUtils {

    // 应用公共文件夹
    public static final String ROOT_NAME = Environment.getExternalStorageDirectory().getPath();
    // 照片
    public static final String TYPE_PATH_IMAGE = "image";
    // 录像
    public static final String TYPE_PATH_VIDEO = "vidoe";
    // 缓存
    public static final String TYPE_PATH_CACHE = "cache";

    // jpg
    public static final String MEDIA_TYPE_JPEG = ".jpg";
    // png
    public static final String MEDIA_TYPE_PNG = ".png";
    // 录像
    public static final String MEDIA_TYPE_VIDEO = ".mp4";
    // app安装包
    public static final String MEDIA_TYPE_APK = ".apk";


    /**
     * 文件下载
     * @param urlStr
     * @param file
     * @return
     */
    public static boolean downloadFile(String urlStr, File file) {
        //初始化流
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            is = conn.getInputStream();
            fos = new FileOutputStream(file);
            byte[] buf = new byte[512];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(is);
            IOUtils.close(fos);
        }
        return false;

    }


    public static String getFileNameForDate(String prefix) {
        return prefix + getFileNameForDate();
    }

    /**
     * 根据当前时间获取
     *
     * @return
     */
    public static String getFileNameForDate() {
//        "'fengchen'_yyyyMMdd_HHmmss"
        return new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(DateUtils.getNowTime());
    }


    public static void openApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        FcUtils.getContext().startActivity(intent);
    }

    /**
     * 打开文件
     * @param uri
     */
    public static void openFile(Uri uri) {
        String url = uri.toString();
        Intent intent = new Intent();
        if (StringCompareUtils.isApkUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else if (StringCompareUtils.isTextUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "text/plain");
        } else if (StringCompareUtils.isImageUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "image/*");
        } else if (StringCompareUtils.isHtmlUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/html");
        } else if (StringCompareUtils.isVideoUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            intent.setDataAndType(uri, "video/*");
        } else if (StringCompareUtils.isAudioUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            intent.setDataAndType(uri, "audio/*");
        } else if (StringCompareUtils.isPdfUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "application/pdf");
        } else if (StringCompareUtils.isWordUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "application/msword");
        } else if (StringCompareUtils.isExcelUrl(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "file/*");
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            FcUtils.getContext().startActivity(intent);
        }
    }

    public static void openFile(File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            uri = FileProvider.getUriForFile(FcUtils.getContext(),
                    BaseApplication.getInstance().getApplicationInfo().packageName + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        openFile(uri);
    }

    public static void openFile(String filePath) {
        if (TextUtils.isEmpty(filePath))return;
        openFile(new File(filePath));
    }



    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file) {

        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }


    /**
     * 删除指定目录下文件及目录
     *
     * @param filePath       路径
     * @param deleteThisPath 是否删除当前文件夹
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    //获取缓存路径
    public static File getDiskCacheDir(String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = FcUtils.getContext().getExternalCacheDir().getPath();
        } else {
            cachePath = FcUtils.getContext().getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }


    //获取缓存大小
    public static long getDiskCacheSize(long defaultSize) {
        long size = defaultSize;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            size+=getFolderSize(new File(FcUtils.getContext().getExternalCacheDir().getPath()));
        }

        size+=getFolderSize(new File(FcUtils.getContext().getCacheDir().getPath()));

        return size;
    }

    //删除缓存
    public static void clearDiskCache() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            deleteFolderFile(FcUtils.getContext().getExternalCacheDir().getPath(),false);
        }
        deleteFolderFile(FcUtils.getContext().getCacheDir().getPath(),false);
    }

}
