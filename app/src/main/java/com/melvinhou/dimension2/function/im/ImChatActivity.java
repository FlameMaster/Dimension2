package com.melvinhou.dimension2.function.im;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.Dimension2Application;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.db.SqlManager;
import com.melvinhou.dimension2.user.User;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.manager.ThreadManager;
import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.view.BaseActivity;
import com.melvinhou.rxjava.RxBus;
import com.melvinhou.rxjava.RxBusClient;
import com.melvinhou.rxjava.RxMsgParameters;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/16 20:41
 * <p>
 * = 分 类 说 明：聊天窗口頁面
 * ================================================
 */
public class ImChatActivity extends BaseActivity {

    private final String url_background = HttpConstant.SERVER_RES +"background/baskground001.jpg";


    /*RxBus的接收器*/
    private RxBusClient mRxBusClient;
    private ImChatModel mModel;
    // disposable 是订阅事件，可以用来取消订阅。防止在 activity 或者 fragment 销毁后仍然占用着内存，无法释放。
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private EditText mMessageInputView;
    private View mSendButton;
    private ImageView mListBackgroundView;
    private RecyclerView mListView;


    private int mPort;
    private String mIP;
    private long mCurrentUserId;

    private MyAdapter mAdapter;
    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;


    /*注册绑定rxbus*/
    private void bindRxBus() {
        mRxBusClient = new RxBusClient(ImChatActivity.this.getClass().getName()) {
            @Override
            protected void onEvent(int type, String message, Object data) {
                ImChatActivity.this.onEvent(type, message, data);
            }
        };
        //告诉别人我这里初始化了
        RxBus.get().post(new EventMessage(ImChatActivity.this.getClass().getName()
                + RxMsgParameters.ACTIVITY_LAUNCHED));
    }

    /**
     * 接受消息
     *
     * @param type
     * @param message
     * @param data
     */
    private void onEvent(int type, String message, Object data) {
        if (message.contains(RxMsgParameters.IM_MESSAGE_RECEIVE)
                && data instanceof ImChatMessageEntity) {//收到消息
            ImChatMessageEntity info = (ImChatMessageEntity) data;
            //判断是否是当前的聊天对象
            if (info.getUserId() != mCurrentUserId) return;
            Log.w("收到服务器的消息", info.getMessage());
            FcUtils.runOnUIThread(() -> {
                mAdapter.addData(info);
                //TODO 这里应该放在服务端使用
                //数据库更新
                mModel.putChatMessage(info);
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void back() {
        //finish()不会执行动画所以使用finishAfterTransition()
        finishAfterTransition();
    }

    /**
     * 系统按键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }
        return false;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_im_chat;
    }

    @Override
    protected void initView() {
        mListView = findViewById(R.id.list);
        mMessageInputView = findViewById(R.id.edit_message);
        mSendButton = findViewById(R.id.submit);
        mListBackgroundView = findViewById(R.id.list_background);
        //背景
        Glide.with(FcUtils.getContext())
                .load(url_background)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(mListBackgroundView);
        //标题
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("聊天");
    }

    @Override
    protected void initListener() {
        mSendButton.setOnClickListener(v -> send());
        //配置适配器
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(new LinearLayoutManager(FcUtils.getContext()));

        //软键盘监听
        mMessageInputView.setOnEditorActionListener((v, actionId, event) -> {
            //判断是否是“send”键
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                //隐藏软键盘
                InputMethodManager imm = (InputMethodManager) v
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(
                            v.getApplicationWindowToken(), 0);
                }
                send();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void initData() {
        //绑定rxbus
        bindRxBus();

        //初始化数据
        mCurrentUserId = getIntent().getLongExtra("userId", 10000);
        mIP = getIntent().getStringExtra("ip");
        mPort = getIntent().getIntExtra("port", 17432);
        //model
        mModel = new ViewModelProvider(this).get(ImChatModel.class);
        mModel.onCreate(mCurrentUserId);

        //用户数据
        mDisposable.add(Observable
                .create((ObservableOnSubscribe<User>) emitter -> {
                    try {
                        User user = SqlManager.findUser(FcUtils.getContext(), mCurrentUserId);
                        emitter.onNext(user);
                    } finally {
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    if (user != null) {
                        getSupportActionBar().setTitle(user.getNickName());
                        mAdapter.setCurrentUser(user);
                    }
                }));
        //连接socket服务器
        connect2ServerSocket(mIP, mPort);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //拉取本地历史记录数据
        mDisposable.add(mModel.getAllChatHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mAdapter.clearData();
                    mAdapter.addDatas(list);
                    mListView.scrollToPosition(mAdapter.getItemCount()-1);
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消订阅。防止在 activity 或者 fragment 销毁后仍然占用着内存，无法释放。
        mDisposable.clear();
    }

    @Override
    protected void onDestroy() {
        mRxBusClient.unregister();
        super.onDestroy();
    }

    /**
     * 连接通讯服务器
     *
     * @param ip
     * @param port
     * @return
     */
    @SuppressLint("CheckResult")
    private void connect2ServerSocket(@NonNull final String ip, @NonNull final int port) {
        Observable
                .create((ObservableOnSubscribe<Socket>) emitter -> {
                    try {
                        Socket socket = new Socket(ip, port);
                        emitter.onNext(socket);
                    } catch (Exception e) {
                        e.printStackTrace();
                        FcUtils.showToast("无法连接对方,请稍后再试");
                    } finally {
                        emitter.onComplete();
                    }
                })
                .compose(IOUtils.setThread())
                .subscribe(socket -> {
                    mSocket = socket;
                    if (socket != null) {
                        mOutStream = socket.getOutputStream();
                        mInStream = socket.getInputStream();
                        //启动轮询
                        startReader();
                    } else {
                        mOutStream = null;
                        mInStream = null;
                    }
                });
    }


    /**
     * 发送消息
     */
    private void send() {
        final String message = mMessageInputView.getText().toString();
        if (message.length() == 0) {
            FcUtils.showToast("消息不能为空");
            startActivity(new Intent(FcUtils.getContext(), ImChatActivity.class));
            return;
        }
        sendMessage(message);
    }

    /**
     * 像服务器发送消息
     *
     * @param message
     */
    @SuppressLint("CheckResult")
    private void sendMessage(@NonNull String message) {
        if (mOutStream == null) return;
        //添加到自己发送的消息
        Observable<Boolean> observable1 = Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    try {
                        mAdapter.addData(mModel.createChatMessage(mCurrentUserId, message));
                        mListView.scrollToPosition(mAdapter.getItemCount()-1);
                        emitter.onNext(true);
                    } finally {
                        emitter.onComplete();
                    }
                })
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .observeOn(AndroidSchedulers.mainThread())
                ;
        Observable<Boolean> observable2 = Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    try {
                        DataOutputStream writer = new DataOutputStream(mOutStream);
                        writer.writeUTF(message);
                        emitter.onNext(true);
                    } finally {
                        emitter.onComplete();
                    }
                })
                .compose(IOUtils.setThread());
        //添加进数据库
        mModel.putChatMessage(mModel.createChatMessage(10000l, message))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        Observable
                .zip(observable1, observable2, (value1, value12) -> value1 && value12)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    //清空输入框
                    mMessageInputView.getText().clear();
                });
    }

    /**
     * 启动消息遍历器
     */
    private void startReader() {
        ThreadManager.getThreadPool().execute(() -> {
            DataInputStream reader;
            try {
                // 获取读取流
                reader = new DataInputStream(mInStream);
                while (mInStream != null) {
//                    Log.w("IM服务器", "等待消息中...");
                    // 读取数据
                    String msg = reader.readUTF();
//                    Log.w("IM服务器", "获取到客户端的信息:" + msg);
                    //告知客户端消息收到
//                    if (mCurrentSocket != null) {
//                        DataOutputStream writer = new DataOutputStream(mOutputStream);
//                        writer.writeUTF(msg); // 写一个UTF-8的信息
//                    }
                    FcUtils.runOnUIThread(() -> {
                        mAdapter.addData(mModel.createChatMessage(mCurrentUserId, msg));
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    static class MyAdapter extends RecyclerAdapter<ImChatMessageEntity, MyHolder> {

        /**
         * 当前聊天对象
         */
        private User mCurrentUser;

        public void setCurrentUser(User user) {
            mCurrentUser = user;
        }

        @Override
        public void bindData(MyHolder viewHolder, int position, ImChatMessageEntity data) {
            boolean isCurrentUser = data.getUserId() != mCurrentUser.getUserId();
            String photoUrl = null;
            if (isCurrentUser) photoUrl = mCurrentUser.getPhoto();
            viewHolder.updateUI(data, isCurrentUser, photoUrl);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_im_message;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            return new MyHolder(View);
        }
    }

    static class MyHolder extends RecyclerHolder {
        private ImageView mPeoplePhotoView, mUserPhotoView;
        private TextView mMessageView, mDateView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mPeoplePhotoView = itemView.findViewById(R.id.people_photo);
            mUserPhotoView = itemView.findViewById(R.id.user_photo);
            mMessageView = itemView.findViewById(R.id.message);
            mDateView = itemView.findViewById(R.id.date);
        }

        public void updateUI(ImChatMessageEntity data, boolean isCurrentUser, String photoUrl) {
            mMessageView.setText(data.getMessage());
            mDateView.setText(data.getDate());
            mPeoplePhotoView.setVisibility(isCurrentUser ? View.VISIBLE : View.INVISIBLE);
            mUserPhotoView.setVisibility(isCurrentUser ? View.INVISIBLE : View.VISIBLE);
            if (isCurrentUser)
                Glide.with(FcUtils.getContext())
                        .load(photoUrl)
                        .into(mPeoplePhotoView);
        }

    }
}
