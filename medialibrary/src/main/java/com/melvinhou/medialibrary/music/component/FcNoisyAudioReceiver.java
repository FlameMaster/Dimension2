package com.melvinhou.medialibrary.music.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/2 0002 9:40
 * <p>
 * = 分 类 说 明：耳机拔出的监听，用于暂停播放
 * ================================================
 */
public abstract class FcNoisyAudioReceiver extends BroadcastReceiver {

    /**
     * 耳机拔出的意图
     */
    public static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            dispose();
        }
    }

    /**
     * 处理
     */
    abstract void dispose();


}
