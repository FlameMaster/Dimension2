package com.melvinhou.medialibrary.music.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.music.util.MusicUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/2 0002 15:46
 * <p>
 * = 分 类 说 明：音乐播放元数据
 * ================================================
 */
public class FcMusicLibrary {

    //媒体列表
    private final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    //相册资源id
    private final HashMap<String, Long> albumRes = new HashMap<>();
    //文件名
    private final HashMap<String, String> urls = new HashMap<>();

    private static FcMusicLibrary mFcMusicModel;

    public static FcMusicLibrary instance() {
        if (mFcMusicModel == null)
            mFcMusicModel = new FcMusicLibrary();
        return mFcMusicModel;
    }

    private FcMusicLibrary() {

    }

    public boolean loadData(boolean isRefresh) {
        if (isRefresh || music.size() == 0)
            return loadNativeMusic(FcUtils.getContext());
        else return true;
    }

    /**
     * 加载本地数据
     *
     * @param context
     * @return
     */
    private boolean loadNativeMusic(Context context) {
        boolean state = false;
        try {
            clear();
            //Uri，指向external的database
            Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            //projection：选择的列; where：过滤条件; sortOrder：排序。
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE
            };
            String where
                    = "mime_type in ('audio/mpeg','audio/x-ms-wma') and bucket_display_name <> 'audio' and is_music > 0 ";
            String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
            Cursor cursor = context.getContentResolver()
                    .query(contentUri, null, null, null,
                            sortOrder);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media._ID)); // 音乐id
                String title = cursor.getString((cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))); // 音乐标题
                String artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 艺术家
                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // 专辑
//            String genre = cursor.getString(cursor
//                    .getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)); // 流派
                String displayName = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));//文件名称
                long albumId = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)); // 时长
                long size = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)); // 文件大小
                String url = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)); // 文件路径

                int isMusic = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)); // 是否为音乐
                if (isMusic != 0) { // 只把音乐添加到集合当中
                    putMediaMetadata(
                            id,
                            title,
                            artist,
                            album,
                            null,
                            duration,
                            TimeUnit.MILLISECONDS,
                            url,
                            albumId,
                            displayName);
                }
            }
            state = true;
        } catch (Exception | Error e) {
            e.printStackTrace();
        } finally {
            return state;
        }
    }

    private void clear() {
        music.clear();
        albumRes.clear();
        urls.clear();
    }


    /**
     * 添加元数据
     *
     * @param mediaId
     * @param title         标题
     * @param artist        艺术家
     * @param album         专辑
     * @param genre         流派
     * @param duration      时长
     * @param durationUnit  时间单位
     * @param url           文件路径
     * @param albumArtResId 专辑封面id
     * @param displayName   专辑封面名称
     */
    private void putMediaMetadata(
            String mediaId,
            String title,
            String artist,
            String album,
            String genre,
            long duration,
            TimeUnit durationUnit,
            String url,
            long albumArtResId,
            String displayName) {
        String albumUri = MusicUtil.getAlbumArtUri(mediaId);
        Bitmap albumArt =
//                MusicUtil.getCoverByAlbumId(albumArtResId);
//                MusicUtil.getCoverByMediaId(mediaId);
                ImageUtils.decodeBitmapFromUri(Uri.parse(albumUri));
        if (albumArt == null) {
            albumArt = ImageUtils.decodeBitmapResources(R.mipmap.default_cover, -1, -1);
        }

        music.put(
                mediaId,
                new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                                TimeUnit.MILLISECONDS.convert(duration, durationUnit))
                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putString(
                                MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                                albumUri)
                        .putString(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                                albumUri)
                        .putString(
                                MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                                url)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        //封面
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                        //封面icon
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, albumArt)
                        .build());
        albumRes.put(mediaId, albumArtResId);//66640 8
        urls.put(mediaId, url);
    }


    /**
     * 获取音乐列表
     *
     * @return
     */
    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : music.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    /**
     * 根据mediaId获取albumId
     *
     * @param mediaId
     * @return
     */
    public long getAlbumId(String mediaId) {
        if (StringUtils.isEmpty(mediaId))
            return 0;
        return albumRes.get(mediaId);
    }

    /**
     * 获取文件路径
     *
     * @param mediaId
     * @return
     */
    public String getMusicUrl(String mediaId) {
        if (StringUtils.isEmpty(mediaId))
            return null;
        return urls.get(mediaId);
    }

    /**
     * 获取媒体
     *
     * @param mediaId
     * @return
     */
    public MediaMetadataCompat getMetadata(String mediaId) {
        if (StringUtils.isEmpty(mediaId))
            return null;
        return music.get(mediaId);
    }


}
