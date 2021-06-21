package com.melvinhou.dimension2.media.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.VpVideoLiveBD;
import com.melvinhou.dimension2.pager.BasePager;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/18 4:49
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class VideoLivePager extends BasePager<VpVideoLiveBD> {

    private PopupWindow popupWindow;
    private EditText editView;

    @Override
    public int getLayoutID() {
        return R.layout.vp_video_live;
    }

    @Override
    protected void initData() {
        getBinding().bilibili.setOnClickListener(v -> showEditDialog("请输入房间号..."));
    }

    @Override
    public void onCreate(int position) {
        super.onCreate(position);
    }

    @Override
    public void updataEmptyState(int emptyState, String message) {

    }

    @Override
    public void refreshData(boolean isShowLoad) {

    }


    @Override
    public void onShow(int position) {
        super.onShow(position);

    }


    private void toLive(String url, String title) {
        Intent intent = new Intent(FcUtils.getContext(), VideoActivity2.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        intent.putExtra("mode", true);
        toActivity(intent);

        if (popupWindow != null && popupWindow.isShowing()) popupWindow.dismiss();
    }


    private void showEditDialog(String hint) {
        if (popupWindow == null)
            initPopupWindow();
        editView.setHint(hint);
        editView.setText("");
        popupWindow.showAtLocation(getRootView(), Gravity.CENTER, 0, 0);
    }


    private void initPopupWindow() {
        float width = getRootView().getWidth() - DimenUtils.dp2px(72);
        float height = width * 9f / 16f;
        popupWindow = new PopupWindow(View.inflate(FcUtils.getContext(), R.layout.dialog_open_link, null),
                (int) width, (int) height);
        popupWindow.setAnimationStyle(R.style.BottomDialogAnimation);
        // 设置PopupWindow是否能响应外部点击事件
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOnDismissListener(() -> {
        });
        popupWindow.getContentView().setBackgroundResource(R.drawable.bg_popup);

        editView = popupWindow.getContentView().findViewById(R.id.edit);
        popupWindow.getContentView().findViewById(R.id.edit).setOnClickListener(this::toLive);
    }

    @SuppressLint("CheckResult")
    private void toLive(View view) {
        String input = null;
        if (editView != null)
            input = editView.getText().toString();
        //哔哩哔哩
        if (StringUtils.noNull(input)) {
            final String api =
                    "https://api.live.bilibili.com/room/v1/Room/playUrl?cid="
                            + input
                            + "&platform=h5&otype=json&quality=4";
            Observable
                    .create((ObservableOnSubscribe<BilibiliLIveEntity>) emitter -> {
                        String json = getBilibiliLivePath(api);
                        Type type = new TypeToken<CYEntity<BilibiliLIveEntity>>() {
                        }.getType();
                        CYEntity<BilibiliLIveEntity> entity = new Gson().fromJson(json, type);
                        emitter.onNext(entity.getData());
                        emitter.onComplete();
                    })
                    .compose(IOUtils.setThread())
                    .subscribe(entity -> {
                        if (entity != null && entity.getDurl() != null && entity.getDurl().size() > 0)
                            toLive(entity.getDurl().get(0).getUrl(), "哔哩哔哩直播");
                    });
        }
    }

    private String getBilibiliLivePath(String path) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}
