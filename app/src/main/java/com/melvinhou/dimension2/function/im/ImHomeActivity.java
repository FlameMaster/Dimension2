package com.melvinhou.dimension2.function.im;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.db.SqlManager;
import com.melvinhou.dimension2.user.User;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.manager.ThreadManager;
import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.SharePrefUtil;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.BaseActivity;
import com.melvinhou.rxjava.RxBus;
import com.melvinhou.rxjava.RxMsgParameters;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
 * = 时 间：2021/6/16 16:12
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class ImHomeActivity extends BaseActivity {

    private ServerSocket mServerSocket;
    private Socket mSocket;

    private int mPort = 17432;
    private String mIp;

    private TextView mUserNameView, mLocalInformationView;
    private RecyclerView mListView;
    private MyAdapter mAdapter;


    /**
     * 等待被连接的Runnable
     */
    private Runnable mAwaitSocketAcceptRunnadle = () -> {
        try {
            //等待客户端的连接，Accept会阻塞，直到建立连接
            mSocket = mServerSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //启动消息接收线程
        startReader();
    };

    @Override
    protected int getLayoutID() {
        return R.layout.activity_im_home;
    }

    @Override
    protected void initView() {
        mUserNameView = findViewById(R.id.user_name);
        mLocalInformationView = findViewById(R.id.local_information);
        mListView = findViewById(R.id.list);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.add_friend).setOnClickListener(v -> addFriend());

        mAdapter = new MyAdapter();
        //配置适配器
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(new LinearLayoutManager(FcUtils.getContext()));
        mAdapter.setOnItemClickListener(this::onItemClick);
    }

    @Override
    protected void initData() {
        initSocketServer();
        initIPAddress();
        ImSqlManager.getIntance().imFriendDao().getAllFriends()
                .observe(this, imFriendEntities -> {
                    mAdapter.clearData();
                    mAdapter.addDatas(imFriendEntities);
                });
    }

    /**
     * 初始化ip
     */
    @SuppressLint("CheckResult")
    private void initIPAddress() {
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    String ip = IOUtils.getIPAddress();
                    emitter.onNext(ip);
                    emitter.onComplete();
                })
                .compose(IOUtils.setThread())
                .subscribe(entity -> {
                    if (entity != null) {
                        mIp = entity;
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("IP:").append(entity)
                                .append("\r\tPort:").append(mPort);
                        mLocalInformationView.setText(buffer);
                    }
                });
    }

    /**
     * 初始化socket服务器
     */
    private void initSocketServer() {

        try {
            mServerSocket = new ServerSocket(mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO 这是服务器断开连接重新初始化，本来需要一个空闲线程轮询的
        //启动服务线程
        ThreadManager.getThreadPool().execute(mAwaitSocketAcceptRunnadle);
    }

    @Override
    protected void onDestroy() {
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    /**
     * 条目点击
     *
     * @param myHolder
     * @param position
     * @param data
     */
    private void onItemClick(MyHolder myHolder, int position, ImFriendEntity data) {
        Intent intent = new Intent(ImHomeActivity.this, ImChatActivity.class);
        intent.putExtra("ip", data.getIp());
        intent.putExtra("port", data.getPort());
        intent.putExtra("userId", data.getUserId());
        startActivity(intent);
    }


    /**
     * 启动消息遍历器
     */
    private void startReader() {
        ThreadManager.getThreadPool().execute(() -> {
            DataInputStream reader;
            try {
                // 获取读取流
                reader = new DataInputStream(mSocket.getInputStream());
                while (mSocket != null) {
                    Log.w("IM服务器", "等待消息中...");
                    // 读取数据
                    String msg = reader.readUTF();
                    Log.w("IM服务器", "获取到客户端的信息:" + msg);
                    //告知客户端消息收到
//                    if (mCurrentSocket != null) {
//                        DataOutputStream writer = new DataOutputStream(mCurrentSocket.getOutputStream());
//                        writer.writeUTF(msg); // 写一个UTF-8的信息
//                    }
                    String ip = mSocket.getInetAddress().getHostAddress();
                    ImFriendEntity friendEntity = ImSqlManager.getImFriend(ip);
                    if (friendEntity != null) {
                        ImChatMessageEntity info = new ImChatMessageEntity();
                        long date = DateUtils.getNowTime();
                        info.setUuid(date);
                        info.setUserId(friendEntity.getUserId());
                        info.setMessage(msg);
                        info.setDate(StringUtils.formatDuration(date, "yyyyMMdd_HHmmss"));
                        RxBus.get().post(new EventMessage(EventMessage.EventType.ALL,
                                RxMsgParameters.IM_MESSAGE_RECEIVE, info));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 添加好友
     */
    private void addFriend() {
        Dialog dialog = new Dialog(ImHomeActivity.this, R.style.Dimension2Dialog);
        dialog.setContentView(R.layout.dialog_im_add_friend);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        //设置布局大小
        dialogWindow.setLayout(DimenUtils.getScreenSize()[0] - DimenUtils.dp2px(32),
                WindowManager.LayoutParams.WRAP_CONTENT);
        //设置整体大小包括外部半透明
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.width = DimenUtils.getScreenSize()[0] - DimenUtils.dp2px(16);
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        dialogWindow.setAttributes(params);
        //设置Dialog位置
        dialogWindow.setGravity(Gravity.CENTER);
        EditText edit_name = dialog.findViewById(R.id.edit_name);
        EditText edit_ip = dialog.findViewById(R.id.edit_ip);
        EditText edit_port = dialog.findViewById(R.id.edit_port);
        dialog.findViewById(R.id.submit).setOnClickListener(view -> {
            String name = edit_name.getText().toString();
            String ip = edit_ip.getText().toString();
            String port = edit_port.getText().toString();
            String photo = HttpConstant.SERVER_RES +"image/portrait/03.jpg";
            //非空判断
            if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
                ip = mIp;
                port = String.valueOf(mPort);
                name = "Your Phone";
                photo = HttpConstant.SERVER_RES +"image/portrait/01.jpg";
            }
            addFriend(name, photo, ip, Integer.valueOf(port));
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * 添加好友
     *
     * @param name
     * @param photo
     * @param ip
     * @param port
     */
    @SuppressLint("CheckResult")
    private void addFriend(String name, String photo, String ip, int port) {
        User user = new User();
        long id = SharePrefUtil.getLong("userNumber", 20000l);
        user.setUserId(id);
        user.setNickName(name);
        user.setPhoto(photo);
        SharePrefUtil.saveLong("userNumber", id + 1);
        ImFriendEntity info = new ImFriendEntity(id, ip, port);
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            try {
                SqlManager.addUser(FcUtils.getContext(), user);
                ImSqlManager.addImFriend(info);
                emitter.onNext(true);
            } finally {
                emitter.onComplete();
            }
        })
                .compose(IOUtils.setThread())
                .subscribe(value -> mAdapter.addData(info));
    }


    static class MyAdapter extends RecyclerAdapter<ImFriendEntity, MyHolder> {
        @Override
        public void bindData(MyHolder viewHolder, int position, ImFriendEntity data) {
            viewHolder.updateUI(data);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_im_friend;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            return new MyHolder(View);
        }
    }

    static class MyHolder extends RecyclerHolder {
        ImageView mPhoto;
        TextView mUserName, mInformation, mUnreadMessageCount;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mPhoto = itemView.findViewById(R.id.user_photo);
            mUserName = itemView.findViewById(R.id.user_name);
            mInformation = itemView.findViewById(R.id.user_information);
            mUnreadMessageCount = itemView.findViewById(R.id.unread_message_count);
        }

        @SuppressLint("CheckResult")
        public void updateUI(ImFriendEntity data) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("IP:\r\t").append(data.getIp())
                    .append("\r\tProt:\r\t").append(data.getPort());
            mInformation.setText(buffer);
            if (data.getUnreadCount() > 0) {
                mUnreadMessageCount.setVisibility(View.VISIBLE);
                mUnreadMessageCount.setText(data.getUnreadCount());
            } else mUnreadMessageCount.setVisibility(View.GONE);

            Observable.create((ObservableOnSubscribe<User>) emitter -> {
                try {
                    User user = SqlManager.findUser(FcUtils.getContext(),data.getUserId());
                    emitter.onNext(user);
                } finally {
                    emitter.onComplete();
                }
            })
                    .compose(IOUtils.setThread())
                    .subscribe(user -> {
                        if (user != null) {
                            mUserName.setText(user.getNickName());
                            Glide.with(FcUtils.getContext())
                                    .load(user.getPhoto())
                                    .into(mPhoto);
                        }
                    });
        }

    }
}
