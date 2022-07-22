package com.melvinhou.dimension2;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/10 23:56
 * <p>
 * = 分 类 说 明：全局常量
 * ================================================
 */
public class GlobalParameters {

    //媒体列表直属条目分类
    public interface MediaLevel1Type {
        //图片
        int PICTURE = 100;
        //视频
        int VIDEO = 200;
        //音乐
        int MUSIC = 300;
        //动画
        int ANIMATOR = 400;
        //抖音
        int TIKTOK = 290;
    }

    //媒体列表2级条目分类
    public interface MediaLevel2Type {
        //输入地址打开
        int PICTURE_OPEN_INPUT = 120;
        //相册
        int PICTURE_ALBUM = 130;
        //插画
        int PICTURE_ILLUSTRATION = 132;
        //裁剪
        int PICTURE_CUTTING = 170;
        //相机
        int PICTURE_CAMERA = 180;
        //扫描
        int PICTURE_SCAN = 183;

        //老式播放器
        int VIDEO_SURFACE = 220;
        //新式播放器
        int VIDEO_TEXTURE = 210;
        //ijk播放器
        int VIDEO_IJK = 212;
        //直播
        int VIDEO_LIVE = 240;
        //电视
        int VIDEO_TV = 230;

        //音乐列表
        int MUSIC_LIST = 310;

        //属性动画
        int ANIMATOR_PROPERTY =420;
        //交互动画
        int ANIMATOR_INTERACTION =430;
        //svg动画
        int ANIMATOR_SVG =440;
        //三方动画
        int ANIMATOR_OTHER =410;
    }
}
