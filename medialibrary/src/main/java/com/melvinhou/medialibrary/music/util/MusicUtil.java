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
     * 根据专辑ID获取专辑封面图
     *
     * @param album_id 专辑ID
     * @return
     */
    public static Bitmap getCoverByAlbumId(long album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = FcUtils.getContext().getContentResolver()
                .query(Uri.parse(mUriAlbums + "/" + album_id), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            bm = BitmapFactory.decodeResource(FcUtils.getContext().getResources(), R.drawable.default_cover);
        }
        return bm;
    }


    /**
     * 封面uri
     *
     * @return
     */
    public static String getAlbumArtUri(String mediaId) {
        if (TextUtils.isEmpty(mediaId)) {
//            return ContentUris.withAppendedId(albumArtUri, getAlbumRes(mediaId)).toString();
            return getDeftAlbumArtUri();
        }
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI+ File.separator + mediaId + "/albumart";
    }

    public static String getDeftAlbumArtUri(){
        return ResourcesUtils.getResourceUri(R.mipmap.default_cover).toString();
//        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
//                FcUtils.getContext().getPackageName() + "/mipmap-hdpi/default_cover.jpg";
    }

    private static long getAlbumId(String mediaId) {
        return FcMusicLibrary.instance().getAlbumId(mediaId);
    }

    private static String getMediaUrl(String mediaId) {
        return FcMusicLibrary.instance().getMusicUrl(mediaId);
    }


    /**
     * 获取封面
     *
     * @param mediaId
     * @return
     */
    public static Bitmap getCoverByMediaId(String mediaId) {
        Bitmap bm = null;
        try {
            FileDescriptor fd = null;
            Uri uri = null;
            if (TextUtils.isEmpty(mediaId))
                uri = Uri.parse( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI+ File.separator + mediaId + "/albumart");
            else {
                long albumid = FcMusicLibrary.instance().getAlbumId(mediaId);
                //获取专辑封面的Uri
                Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
                uri = ContentUris.withAppendedId(albumArtUri, albumid);
            }
            ParcelFileDescriptor pfd = FcUtils.getContext().getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) fd = pfd.getFileDescriptor();

            bm = BitmapFactory.decodeFileDescriptor(fd);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return bm;
        }
    }


}
