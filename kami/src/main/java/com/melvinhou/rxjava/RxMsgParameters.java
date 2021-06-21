package com.melvinhou.rxjava;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/3 14:14
 * <p>
 * = 分 类 说 明：
 * ============================================================
 */
public class RxMsgParameters {

    /*活动页面已经启动的消息*/
    public static final String ACTIVITY_LAUNCHED = ":[Activity]onLaunched";
    /*关闭活动页面的消息*/
    public static final String ACTIVITY_FINISH = ":[Activity]finish";
    /*开启新活动页面的消息*/
    public static final String ACTIVITY_LAUNCH = ":[Activity]launch";


    /*数据刷新*/
    public static final String DATA_REFRESH = ":[Data]reFresh";
    /*网络连接改变*/
    public static final String NETWORK_CHANGE_LINK = ":[NetWork]changeLink";
    /*打开相册*/
    public static final String INTENT_OPEN_ALBUM = ":[Intent]openAlbum";
    /*打开文件选择器*/
    public static final String INTENT_OPEN_FILE = ":[Intent]openFileChooser";


    /*聊天的消息传送*/
    public static final String IM_MESSAGE_RECEIVE = ":[Message]receive";


    public interface ApplicationFrom {

        /*初始化输入器*/
        String UI_INIT_INPUT = ":{ApplicationFrom}[UI]initInput";

        /*删除附件*/
        String HTTP_ATTACH_DELETE = ":{ApplicationFrom}[HTTP]deleteAttach";
    }

    public interface Pager{

        /*初始化*/
        String PAGER_INIT =":{Pager}init";
    }

}
