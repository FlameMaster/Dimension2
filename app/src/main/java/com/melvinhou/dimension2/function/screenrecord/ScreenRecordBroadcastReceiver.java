package com.melvinhou.dimension2.function.screenrecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.melvinhou.kami.util.FcUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/3 22:41
 * <p>
 * = 分 类 说 明：用于通知录屏相关
 * ================================================
 */
public class ScreenRecordBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("screen_record_stop")) {
            Log.e("ScreenRecordBR","停止录制");
            //处理点击事件
            FcUtils.getContext().stopService(
                    new Intent(FcUtils.getContext(), ScreenRecordService.class));
        }
    }
}
