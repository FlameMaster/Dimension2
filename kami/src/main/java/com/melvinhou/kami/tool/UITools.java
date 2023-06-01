package com.melvinhou.kami.tool;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.melvinhou.kami.R;
import com.melvinhou.kami.util.FcUtils;

import androidx.annotation.GravityInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/13 0013 9:36
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class UITools {


    /**
     * 关闭软键盘
     *
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && activity.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                if (null != activity.getCurrentFocus()) {
                    activity.getCurrentFocus().clearFocus();
                }
            }
        }
    }


    /**
     * 关闭软键盘
     *
     * @param dialog
     */
    public static void hideSoftKeyboard(Dialog dialog) {
        if (dialog != null) {
            InputMethodManager imm = (InputMethodManager) dialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && dialog.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), 0);
                if (null != dialog.getCurrentFocus()) {
                    dialog.getCurrentFocus().clearFocus();
                }
            }
        }
    }

    /**
     * 隐藏软键盘(有输入框)
     *
     * @param editText
     */
    public static void hideSoftKeyboard(EditText editText) {
        if (editText != null) {
            InputMethodManager inputmanger = (InputMethodManager) editText.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputmanger.isActive())
                inputmanger.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 创建一个dialog
     *
     * @param activity
     * @param layoutResID
     * @param themeResID
     * @param gravity
     * @param animResID
     */
    public static AlertDialog createDialog(Activity activity,
                                           @LayoutRes int layoutResID, @StyleRes int themeResID,
                                           @GravityInt int gravity, @StyleRes int animResID) {
        AlertDialog dialog = new AlertDialog.Builder(activity, themeResID).create();
        dialog.setContentView(layoutResID);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                hideSoftKeyboard(activity);
            }
        });
        //窗口
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(null);
        window.setGravity(gravity);
        window.setWindowAnimations(animResID);
        //显示
        dialog.show();
        window.setContentView(layoutResID);//设置在show之后
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    public static AlertDialog createDialog(Activity activity,
                                           @LayoutRes int layoutResID,
                                           @GravityInt int gravity, @StyleRes int animResID) {
        return createDialog(activity, layoutResID, R.style.KamiDialog, gravity, animResID);
    }

    /**
     * 打开app设置
     */
    public static void toAppSetting() {
//        Html.fromHtml("<font color='#000000'>" + builder.getExplainText() + "</font>");
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", FcUtils.getContext().getPackageName(), null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        FcUtils.getContext().startActivity(intent);
    }
}
