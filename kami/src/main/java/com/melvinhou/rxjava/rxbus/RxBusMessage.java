package com.melvinhou.rxjava.rxbus;

import android.text.TextUtils;

import java.lang.annotation.Retention;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/4 09:36
 * <p>
 * = 分 类 说 明：rxbus使用的消息对象
 * ============================================================
 */
public class RxBusMessage {

    /**
     * @hide
     */
    @SuppressWarnings("WeakerAccess")
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Retention(SOURCE)
    @IntDef({OFFSCREEN_CLIENT_DEFAULT})
    @IntRange(from = 1)
    public @interface OffscreenClient {
    }

    //默认的公共发送
    public static final int OFFSCREEN_CLIENT_DEFAULT = -1;


    //类型指定,发送端和接收端，对接的key
    private String type;
    //指定对象的id
    private int clientId;
    private Object attach;

    public RxBusMessage(@OffscreenClient int clientId, @NonNull String type, Object attach) {
        this.clientId = clientId;
        this.type = type;
        this.attach = attach;
    }

    private RxBusMessage() {
    }

    public String getType() {
        return type;
    }

    public int getClientId() {
        return clientId;
    }

    public Object getAttach() {
        return attach;
    }

    private void setClientId(@OffscreenClient int clientId) {
        this.clientId = clientId;
    }

    private void setType(@NonNull String type) {
        this.type = type;
    }

    private void setAttach(Object attach) {
        this.attach = attach;
    }


    public static class Builder {

        private RxBusMessage message;

        public static Builder instance(@NonNull String type) {
            return new Builder(type);
        }

        private Builder(@NonNull String type) {
            message = new RxBusMessage();
            message.setType(type);
            message.setClientId(OFFSCREEN_CLIENT_DEFAULT);
        }

        /**
         * 初始化
         *
         * @return
         */
        public RxBusMessage build() {
            return message;
        }

        /**
         * 直接发送
         */
        public void post() {
            RxBus.instance().post(message);
        }

        public Builder client(@OffscreenClient int clientId) {
            message.setClientId(clientId);
            return this;
        }

        public Builder client(@NonNull String clientId) {
            if (TextUtils.isEmpty(clientId)) {
                throw new IllegalArgumentException(
                        "clientId not be null");
            }
            message.setClientId(RxBusClient.getClientId(clientId));
            return this;
        }

        public Builder attach(Object attach) {
            message.setAttach(attach);
            return this;
        }
    }


    public static class CommonType {
        public static final String DEFAULT = ":default";

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

    }
}
