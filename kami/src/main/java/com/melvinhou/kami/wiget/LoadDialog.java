package com.melvinhou.kami.wiget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.melvinhou.kami.R;


public class LoadDialog extends ProgressDialog {

    private boolean isCancel = true;

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

    private void init(Context context) {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(isCancel);
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
            tv.setVisibility(View.VISIBLE);
        }

        if (!isShowing())
            show();
    }
}
