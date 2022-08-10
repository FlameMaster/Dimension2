package com.melvinhou.dimension2.net;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/7/6 17:07
 * <p>
 * = 分 类 说 明：网络接口
 * ================================================
 */
public class HttpConstant {

    /*访问成功*/
    public static final int CODE_NORMAL = 200;
    /*系统内部错误*/
    public static final int CODE_ERR_SERVER = 500;
    /*参数错误，包括参数缺失、参数格式错误等*/
    public static final int CODE_ERR_PARAMETER = 400;
    /*需要登录*/
    public static final int CODE_ERR_LOGIN = 401;
    /*拒绝访问，权限不足*/
    public static final int CODE_ERR_PERMISSION = 403;
    /*异地登录*/
    public static final int CODE_OUT_LOGIN = -101;


//////////////////////////////////////全局/////////////////////////////////////////////////


    /*域名*/
    public static final String SERVER_URL = "http://web.gxgnt.top/";
    /*api地址*/
    public static final String SERVER_API = SERVER_URL+"mbapp/appexamination/";
    /*资源地址*/
    public static final String SERVER_RES = "https://otakuboy.oss-cn-beijing.aliyuncs.com/"+"Ciyuan2/app/";




}
