package com.melvinhou.kami.view.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.melvinhou.kami.R;


public class LoadDialog extends ProgressDialog {

    //true:点击外围和返回键消失
    private boolean isCancel = false;

    public LoadDialog(Context context) {
        super(context);
    }

    public LoadDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        init(getContext());
    }

    private void init(Context context) {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        //设置点击返回键不消失
        setCancelable(isCancel);
        //设置点击屏幕不消失
        setCanceledOnTouchOutside(isCancel);

        setContentView(R.layout.load_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    @Override
    public void show() {
        super.show();
    }

    public void show(String text) {
        TextView tv = findViewById(R.id.tv_load_dialog);
        if (tv != null) {
            tv.setText(text);
            tv.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        }

        if (!isShowing())
            show();
    }
}
