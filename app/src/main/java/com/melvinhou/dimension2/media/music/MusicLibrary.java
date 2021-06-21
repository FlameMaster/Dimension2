/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.melvinhou.dimension2.media.music;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.melvinhou.dimension2.BuildConfig;
import com.melvinhou.kami.util.ImageUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 * 提供本地数据,根据从客户端接收到的parentMediaId类型，返回数据
 */
public class MusicLibrary {

    private static final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    private static final HashMap<String, Long> albumRes = new HashMap<>();
    private static final HashMap<String, String> musicFileName = new HashMap<>();
    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    public static String getRoot() {
        return "root";
    }

    public static void clear() {
        music.clear();
        albumRes.clear();
        musicFileName.clear();
    }

    /**
     * 加载本地数据
     *
     * @param context
     * @return
     */
    public static boolean loadNativeData(Context context) {
        boolean state = false;
        try {
            clear();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                String id = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media._ID)); // 音乐id
                String title = cursor.getString((cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
                String artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
                String album = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM)); // 专辑
//            String genre = cursor.getString(cursor
//                    .getColumnIndex(MediaStore.Audio.Media.GENRE)); // 流派
                String displayName = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));//文件名称
                long albumId = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
                long size = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
                String url = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径

                int isMusic = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否为音乐
                if (isMusic != 0) { // 只把音乐添加到集合当中
                    createMediaMetadataCompat(
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
        } finally {
            return state;
        }
    }

    public static List<String> getMusicList() {
        List<String> list = new ArrayList<>(music.keySet());
        return list;
    }

    /**
     * 封面uri
     *
     * @return
     */
    public static String getAlbumArtUri(String mediaId) {
        if (TextUtils.isEmpty(mediaId)) {
//            return ContentUris.withAppendedId(albumArtUri, getAlbumRes(mediaId)).toString();
            return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    BuildConfig.APPLICATION_ID + "/mipmap-hdpi/fc.jpg";
        }
        return "content://media/external/audio/media/" + mediaId + "/albumart";
    }

    public static String getMusicFilename(String mediaId) {
        return musicFileName.containsKey(mediaId) ? musicFileName.get(mediaId) : null;
    }

    private static long getAlbumRes(String mediaId) {
        return albumRes.containsKey(mediaId) ? albumRes.get(mediaId) : 0;
    }



    /**
     * 获取封面
     * @param context
     * @param mediaId
     * @return
     */
    public static Bitmap getAlbumBitmap(Context context, String mediaId,
                                        int reqWidth, int reqHeight) {
        Bitmap bm = null;
        try {
            FileDescriptor fd = null;
            Uri uri = null;
            if (TextUtils.isEmpty(mediaId))
                uri = Uri.parse(
                        "content://media/external/audio/media/" + mediaId + "/albumart");
            else {
                long albumid = getAlbumRes(mediaId);
                uri = ContentUris.withAppendedId(albumArtUri, albumid);
            }
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) fd = pfd.getFileDescriptor();

            bm = ImageUtils.decodeBitmapFromFileDescriptor(fd, reqWidth, reqHeight);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return bm;
        }
    }

    /**
     * 获取封面
     * @param context
     * @param mediaId
     * @return
     */
    public static Bitmap getAlbumBitmap(Context context, String mediaId) {
        return getAlbumBitmap(context, mediaId, -1, -1);
    }

    /**
     * 获取音乐列表
     * @return
     */
    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : music.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    /**
     * 返回关于项目的元数据，如标题、艺术家等
     *
     * @param context
     * @param mediaId
     * @return
     */
    public static MediaMetadataCompat getMetadata(Context context, String mediaId) {
        MediaMetadataCompat metadataWithoutBitmap = music.get(mediaId);
//        Bitmap albumArt = getAlbumBitmap(context, mediaId);
        Bitmap albumArt = null;

        // 因为MediaMetadataCompat是不可变的，所以我们需要创建一个副本来设置相册图像.
        // 我们不会一开始就对所有项目都设置它，这样它们就不会占用不必要的内存.
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        for (String key :
                new String[]{
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        MediaMetadataCompat.METADATA_KEY_GENRE,
                        MediaMetadataCompat.METADATA_KEY_TITLE,
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI
                }) {
            builder.putString(key, metadataWithoutBitmap.getString(key));
        }
        builder.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        return builder.build();
    }

    /**
     * 添加数据
     *
     * @param mediaId
     * @param title           标题
     * @param artist          艺术家
     * @param album           专辑
     * @param genre           流派
     * @param duration        时长
     * @param durationUnit    时间单位
     * @param musicFilename   文件路径
     * @param albumArtResId   专辑封面id
     * @param albumArtResName 专辑封面名称
     */
    private static void createMediaMetadataCompat(
            String mediaId,
            String title,
            String artist,
            String album,
            String genre,
            long duration,
            TimeUnit durationUnit,
            String musicFilename,
            long albumArtResId,
            String albumArtResName) {
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
                                getAlbumArtUri(mediaId))
                        .putString(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                                getAlbumArtUri(mediaId))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .build());
        albumRes.put(mediaId, albumArtResId);
        musicFileName.put(mediaId, musicFilename);
    }
}