package com.melvinhou.medialibrary.record;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/18 0018 14:04
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class ScreenRecordConstant {

    // 编码器参数
    public static final String MIME_TYPE = "video/avc"; // H.264 类型
    // 比特率
    public static final int BIT_RATE = 6000000;
    // 帧速率
    public static final int FRAME_RATE = 30; // 30 fps
    // I帧的帧率
    public static final int IFRAME_INTERVAL = 10; // 10 seconds between
    // 超时时限
    public static final int TIMEOUT_US = 10000;
}
