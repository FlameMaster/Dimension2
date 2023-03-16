package com.melvinhou.medialibrary.music.util;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.music.model.FcMusicLibrary;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/2 0002 16:22
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class MusicUtil {

//    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI (content://media/external/audio/media)


    /**
     * 封面uri
     *
     * @return
     */
    public static String getAlbumArtUri(long albumId) {
        String[] projection = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART};
        String where = MediaStore.Audio.Albums._ID + "=" + albumId;
        String uri = null;
        try {
            Cursor cursorAlbum = FcUtils.getContext().getContentResolver()
                    .query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, where, null, null);
            if (cursorAlbum != null && cursorAlbum.moveToFirst()) {
                int index = cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                uri = cursorAlbum.getString(index);
                cursorAlbum.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri != null ? uri : getDeftAlbumArtUri();
    }

    /**
     * 根据专辑ID获取专辑封面图
     *
     * @param album_id 专辑ID
     * @return
     */
    public static Bitmap getCoverByAlbumId(long album_id) {
        //content://media/external/audio/albums/9
        Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, album_id);
        String[] projection = new String[]{MediaStore.Audio.Albums.ALBUM_ART};
        Cursor cur = null;
        String album_art = null;
        try {
            cur = FcUtils.getContext().getContentResolver()
                    .query(albumUri, projection, null, null, null);
            if (cur != null && cur.getCount() > 0 && cur.getColumnCount() > 0) {
                cur.moveToNext();
                album_art = cur.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) cur.close();
        }
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            bm = getDeftAlbumArt();
        }
        return bm;
    }


    /**
     * 封面uri
     *
     * @return
     */
    public static String getAlbumArtUri(String mediaId) {
        //"content://media/external/audio/media/"
        //MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + File.separator
        Uri uri = Uri.parse("content://media/external/audio/media/" + mediaId + "/albumart");
        //判断url是否存在
        boolean bool = false;
        if (null != uri) {
            try {
                InputStream inputStream = FcUtils.getContext().getContentResolver().openInputStream(uri);
                inputStream.close();
                bool = true;
            } catch (Exception e) {
            }
        }
        return bool ? uri.toString() : getDeftAlbumArtUri();
    }


    /**
     * 获取封面
     *
     * @param mediaId
     * @return
     */
    public static Bitmap getCoverByMediaId(String mediaId) {
        Bitmap bm = null;
        Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + File.separator + mediaId + "/albumart");
        ParcelFileDescriptor pfd = null;
        try {
            pfd = FcUtils.getContext().getContentResolver().openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (pfd != null) {
            FileDescriptor fd = pfd.getFileDescriptor();
            bm = BitmapFactory.decodeFileDescriptor(fd);
        }
        return bm != null ? bm : getDeftAlbumArt();
    }


    public static String getDeftAlbumArtUri() {
        return ResourcesUtils.getResourceUri(R.mipmap.default_cover).toString();
//        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
//                FcUtils.getContext().getPackageName() + "/mipmap-hdpi/default_cover.jpg";
    }

    public static Bitmap getDeftAlbumArt() {
        return BitmapFactory.decodeResource(FcUtils.getContext().getResources(), R.mipmap.default_cover);
    }

    private static long getAlbumId(String mediaId) {
        return FcMusicLibrary.instance().getAlbumId(mediaId);
    }

    private static String getMediaUrl(String mediaId) {
        return FcMusicLibrary.instance().getMusicUrl(mediaId);
    }


}
