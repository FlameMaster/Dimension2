package com.melvinhou.kami.manager;

import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.StringUtils;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/1/21 18:21
 * <p>
 * = 分 类 说 明：检查弹窗数据体
 * ============================================================
 */
public abstract class DialogCheckBuilder {

    /*显示的文字*/
    private String titleText,explainText,confirmText,cancelText;

    public DialogCheckBuilder(String explainText){
        this("",explainText);
    }

    public DialogCheckBuilder(String titleText, String explainText){
        this(titleText,explainText,"确认","取消");
    }

    public DialogCheckBuilder(String titleText, String explainText, String confirmText, String cancelText){
        this.titleText=titleText;
        this.explainText=explainText;
        this.confirmText=confirmText;
        this.cancelText=cancelText;
    }

    /*确认*/
    public abstract void confirm();

    /*取消*/
    public void cancel(){
    }

    public String getTitleText() {
        return titleText;
    }

    public String getExplainText() {
        return explainText;
    }

    public String getConfirmText() {
        return confirmText;
    }

    public String getCancelText() {
        return cancelText;
    }

    public int getMinBottomWidth(){
        if (StringUtils.noNull(getCancelText()))
            return DimenUtils.dp2px(96);
        else
            return DimenUtils.dp2px(128);
    }


    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setExplainText(String explainText) {
        this.explainText = explainText;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }
}
