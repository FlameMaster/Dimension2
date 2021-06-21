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

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * 提供从 {@link PlayerAdapter} (播放器) 到 {@link MusicService} 包含 {@link MediaSessionCompat}的状态更新的监听器.
 */
public abstract class PlaybackInfoListener {

    /**
     * 状态变化
     * @param state
     */
    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    /**
     * 播放完成
     */
    public void onPlaybackCompleted() {
    }
}