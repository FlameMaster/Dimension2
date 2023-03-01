package com.melvinhou.kami.io;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.melvinhou.kami.BaseApplication;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.StringCompareUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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


//***********************************文件名和路径*********************************************//


    // 应用公共文件夹
    public static final String ROOT_NAME = Environment.getExternalStorageDirectory().getPath();
    private static String appDir = "";
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

    public static final String RECORD_DIR_SUFFIX = "/record/";
    public static final String RECORD_DOWNLOAD_DIR_SUFFIX = "/record/download/";
    public static final String VIDEO_DOWNLOAD_DIR_SUFFIX = "/video/download/";
    public static final String IMAGE_BASE_DIR_SUFFIX = "/image/";
    public static final String IMAGE_DOWNLOAD_DIR_SUFFIX = "/image/download/";
    public static final String MEDIA_DIR_SUFFIX = "/media/";
    public static final String FILE_DOWNLOAD_DIR_SUFFIX = "/file/download/";
    public static final String CRASH_LOG_DIR_SUFFIX = "/crash/";


    /**
     * 根据当前时间获取
     *
     * @return
     */
    public static String getFileNameForDate() {
//        "'fengchen'_yyyyMMdd_HHmmss"
        return new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(DateUtils.getCurrentTime());
    }


    /**
     * 默认存储目录
     *
     * @return
     */
    public static String getDefaultAppDir() {
        if (TextUtils.isEmpty(appDir)) {
            appDir = FcUtils.getContext().getFilesDir().getAbsolutePath();
        }
        return appDir;
    }

    /**
     * 获取文件存储路径
     *
     * @param path
     * @return
     */
    public static String getAppFileDir(String path) {
        return getDefaultAppDir() + path;
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

    /**
     * 获取uri的文件名
     *
     * @param uri
     * @return
     */
    public static String getRealFileName(final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = FcUtils.getContext().getContentResolver()
                    .query(uri, filePathColumn, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(filePathColumn[1]);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


//***********************************本地文件操作*********************************************//


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

    //获取缓存大小
    public static long getDiskCacheSize(long defaultSize) {
        long size = defaultSize;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            size += getFolderSize(new File(FcUtils.getContext().getExternalCacheDir().getPath()));
        }

        size += getFolderSize(new File(FcUtils.getContext().getCacheDir().getPath()));

        return size;
    }

    //删除缓存
    public static void clearDiskCache() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            deleteFolder(FcUtils.getContext().getExternalCacheDir().getPath(), false);
        }
        deleteFolder(FcUtils.getContext().getCacheDir().getPath(), false);
    }

    //复制文件
    public static void copyfile(File fromFile, File toFile, Boolean rewrite) {
        if (!fromFile.exists()) {
            return;
        }
        if (!fromFile.isFile()) {
            return;
        }
        if (!fromFile.canRead()) {
            return;
        }
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists() && rewrite) {
            toFile.delete();
        }
        //当文件不存时，canWrite一直返回的都是false
        if (!toFile.canWrite()) {
            FcUtils.showToast("不能够写将要复制的目标文件");
            return;
        }
        try {
            InputStream fosfrom = FcUtils.getContext().openFileInput(fromFile.getAbsolutePath());
            FileOutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c); //将内容写到新文件当中
            }
            fosfrom.close();
            fosto.close();
        } catch (Exception ex) {
            Log.e("readfile", ex.getMessage());
        }
    }

    //复制文件
    public static void copyfile(File fromFile, Uri toUri, Boolean rewrite) throws Exception {
        if (!fromFile.exists()) {
            return;
        }
        if (!fromFile.isFile()) {
            return;
        }
        if (!fromFile.canRead()) {
            return;
        }
        InputStream fosfrom = new FileInputStream(fromFile.getAbsolutePath());
        OutputStream osto = FcUtils.getContext().getContentResolver().openOutputStream(toUri);
        byte bt[] = new byte[1024];
        int c;
        while ((c = fosfrom.read(bt)) > 0) {
            osto.write(bt, 0, c); //将内容写到新文件当中
        }
        fosfrom.close();
        osto.close();
    }

    //复制文件
    public static void copyfile(Uri fromUri, File toFile, Boolean rewrite) {
        Log.e("文件复制", "in=" + fromUri + "\r\tout=" + toFile.getPath());
        if (fromUri == null) {
            return;
        }
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists() && rewrite) {
            toFile.delete();
        }
        //当文件不存时，canWrite一直返回的都是false
//        if (!toFile.canWrite()) {
//            FcUtils.runOnUiThread(() -> FcUtils.showToast("不能够写将要复制的目标文件"));
//            return;
//        }
        try {
            InputStream is = FcUtils.getContext().getContentResolver().openInputStream(fromUri);
            FileOutputStream fos = new FileOutputStream(toFile);
            byte buffer[] = new byte[1024];

            int count = 0;
            while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                fos.write(buffer, 0, count);
            }
            is.close();
            fos.close();
        } catch (Exception ex) {
            Log.e("readfile", ex.getMessage());
        }
    }


    /**
     * 删除指定目录下文件及目录
     *
     * @param filePath       路径
     * @param deleteThisPath 是否删除当前文件夹
     */
    public static void deleteFolder(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolder(files[i].getAbsolutePath(), true);
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


//***********************************文件下载*********************************************//


    /**
     * 文件下载
     *
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


    public static void openApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        FcUtils.getContext().startActivity(intent);
    }

    /**
     * 打开文件
     *
     * @param uri
     */
    public static void openFile(Uri uri) {
        String url = uri.toString();
        Intent intent = new Intent();
        if (StringCompareUtils.isApkFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else if (StringCompareUtils.isTxtFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "text/plain");
        } else if (StringCompareUtils.isImageFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "image/*");
        } else if (StringCompareUtils.isHtmlFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/html");
        } else if (StringCompareUtils.isVideoFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            intent.setDataAndType(uri, "video/*");
        } else if (StringCompareUtils.isAudioFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            intent.setDataAndType(uri, "audio/*");
        } else if (StringCompareUtils.isPdfFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "application/pdf");
        } else if (StringCompareUtils.isWordFile(url)) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, "application/msword");
        } else if (StringCompareUtils.isExcelFile(url)) {
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
        if (TextUtils.isEmpty(filePath)) return;
        openFile(new File(filePath));
    }


}
