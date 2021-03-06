package com.melvinhou.dimension2.media.video;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActVideoBD;
import com.melvinhou.kami.adapter.BindingRecyclerAdapter;
import com.melvinhou.kami.mvvm.BindingActivity;
import com.melvinhou.kami.util.DeviceUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.rxjava.RxBusClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * ===========================================================
 * = ??? ????????? ???
 * <p>
 * = ??? ??? ??? ??????7416064@qq.com
 * <p>
 * = ??? ????????? ??? ??? ??? ??? ??? ??? ???
 * <p>
 * = ??? ??????2018/12/10 14:10
 * <p>
 * = ??? ??? ??? ???????????????????????????SurfaceView???????????????????????????
 * ============================================================
 */
public class VideoActivity extends BindingActivity<ActVideoBD> {

    //todo ??????????????????16:9????????????????????????????????????

    /*????????????*/
    public static final int SCREEN_DIRECTION_UNDEFINED = 404;
    /*?????????????????????*/
    private static final int[] SYSTEM_UI_FLAG_PORTRAIT = {
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION,
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    };
    /*?????????????????????*/
    private static final int[] SYSTEM_UI_FLAG_LANDSCAPE = {
                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE,
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    };


    String url = "https://yys.v.netease.com/2018/0725/ebefc466c32aa2c40aede8207956aae8qt.mp4";
    String url2 = "https://webstatic.bh3.com/video/bh3.com/pv/CG_OP_1800.mp4";
    String url3 = "http://ivi.bupt.edu.cn/hls/cctv5phd.m3u8";


    Intent mMediaPlayerIntent;
    MediaBinder mMediaBinder;
    MediaServiceConnection mMediaServiceConnection;

    /*????????????*/
    int mScreenDirection;
    /*??????????????????*/
    boolean isLandscape = false;
    //???????????????????????????
    SensorManager mSensorManager;
    Sensor mSensor;
    SensorEventListener mSensorEventListener;
    /*?????????????????????????????????*/
    ContentObserver mScreenRotateObserver;
    /*???????????????*/
    int mWindowFlag = 0;
    /*?????????????????????*/
    boolean isShowStableBar = false;
    /*?????????????????????????????????????????????*/
    Disposable mChangeOrientationDisposable, mProgressDisposable;

    @Override
    protected void initWindowUI() {
        int navigationColor = Color.TRANSPARENT;
        if (isLandscape) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LANDSCAPE[mWindowFlag]);
            navigationColor = ResourcesUtils.getColor(R.color.colorPrimary);
        } else
            getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_PORTRAIT[mWindowFlag]);
//        statusColor= 0x33000000;
        // ?????????
        getWindow().setStatusBarColor(0x33000000);
        // ???????????????
        getWindow().setNavigationBarColor(navigationColor);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video;
    }

    @Override
    protected void initView() {
        //????????????
        mScreenDirection = ResourcesUtils.getResources()
                .getConfiguration().orientation;
        isLandscape = ResourcesUtils.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        //SurfaceHolder???SurfaceView????????????callback?????????
        getViewDataBinding().tv.tvShow.getHolder().addCallback(callback);

        //??????????????????????????????
        initGravitySener();

        //????????????????????????
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.e("???????????????", "visibility=" + visibility);
            if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {//???????????????
                if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
                getViewDataBinding().tv.surfaceClick.setVisibility(View.VISIBLE);
                mChangeOrientationDisposable = Observable.timer(3, TimeUnit.SECONDS)
                        .compose(IOUtils.setThread())
                        .subscribe(aLong -> {
                            mWindowFlag = 1;
                            showSurfaceClick(null);
                        });
            } else {//???????????????
                if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
                getViewDataBinding().tv.surfaceClick.setVisibility(View.GONE);
            }
        });

        //??????????????????
        getViewDataBinding().tv.tvProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // ??????????????????????????????????????????
                if (!fromUser) return;
                // ??????????????????
                mMediaBinder.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //??????rxbus
//        RxBus.get().post(new EventMessage(VideoActivity.class.getName() + "onCreate"));

        //
        getViewDataBinding().other.list.setLayoutManager(new GridLayoutManager(FcUtils.getContext(), 4));
        mListAdaper = new ListAdaper();
        getViewDataBinding().other.list.setAdapter(mListAdaper);

        //???????????????
        int surce = getIntent().getIntExtra("dataSource", -1);
        String path = getIntent().getStringExtra("dataPath");
        if (surce >= 0 && StringUtils.noNull(path))
            initData(surce, path);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //?????????????????????
            back(null);
            return true;
        }
        return false;
    }

///////////////////////?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????///////////////////////

    /*??????*/
    public void back(View view) {
        if (isLandscape) changeTV(null);
        else finish();
    }

    /*????????????*/
    public void changePlay(View view) {
        if (mMediaBinder.isPlaying()) mMediaBinder.pause();
        else mMediaBinder.start();
    }

    /*????????????*/
    public void changeTV(View view) {
        if (mWindowFlag == 0) mWindowFlag = 1;
        else mWindowFlag = 0;
        StringBuffer buffer = new StringBuffer("H,");
        int marginRight;
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            buffer.append(16).append(":").append(9);
            marginRight = 0;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            int[] size = DimenUtils.getScreenSize();
            buffer.append(size[0]).append(":").append(size[1]);
            marginRight = DimenUtils.getNavigationHeight();
        }
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) getViewDataBinding().tv.tvShow.getLayoutParams();
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) getViewDataBinding().tv.tvChange.getLayoutParams();
        lp.dimensionRatio = buffer.toString();
        lp2.rightMargin = marginRight;
        getViewDataBinding().tv.tvShow.setLayoutParams(lp);
        getViewDataBinding().tv.tvChange.setLayoutParams(lp2);
    }

    /*??????/???????????????*/
    public void showSurfaceClick(View view) {
        Log.e("????????????", "JOJO???????????????");
        if (mWindowFlag == 0) {
            mWindowFlag = 1;
        } else mWindowFlag = 0;
        initWindowUI();
    }

///////////////////////?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????///////////////////////

    RxBusClient mRxBusClient = new RxBusClient(VideoActivity.class.getName()) {
        @Override
        protected void onEvent(int type, String message, Object data) {
            if (StringUtils.noNull(message)) {
                Log.d("????????????:", "messge=" + message);
                if (message.contains("tv_play"))
                    onMediaPlay((Integer) data);
                else if (message.contains("tv_start"))
                    onMediaStart();
                else if (message.contains("tv_pause"))
                    onMediaPause();
                else if (message.contains("tv_stop"))
                    onMediaStop();
                else if (message.contains("tv_completion"))
                    onMediaCompletion((Integer) data);
                else if (message.contains("tv_loading")) {
                    getViewDataBinding().tv.tvLoadProgress.setVisibility((Boolean) data ? View.VISIBLE : View.GONE);
                    getViewDataBinding().tv.expired.setVisibility(View.GONE);
                } else if (message.contains("tv_expired")) {
                    getViewDataBinding().tv.tvLoadProgress.setVisibility(View.GONE);
                    getViewDataBinding().tv.expired.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private void play(int position) {
        //????????????
        if (position >= 0 &&
                mMediaBinder != null &&
                mMediaBinder.getMediaData(position) != null &&
                StringUtils.noNull(mMediaBinder.getMediaData(position).getUrl()))
            mMediaBinder.play(position);
    }

    /*????????????*/
    private void onMediaPlay(int position) {
        //????????????
        //?????????????????????
        getViewDataBinding().tv.tvProgressMaxText.setText(StringUtils.formatDuration(mMediaBinder.getDuration()));
        getViewDataBinding().tv.tvProgress.setMax(mMediaBinder.getDuration());
        getViewDataBinding().tv.tvTitle.setText(getViewDataBinding().getMovie().getMovies().get(position).getTitle());
        //??????title
        for (int i = 0; i < getViewDataBinding().other.list.getChildCount(); i++) {
            TextView tab = (TextView) getViewDataBinding().other.list.getChildAt(i);
            if (i == position) {
                tab.setBackgroundResource(R.drawable.bg_tab_h);
                tab.setTextColor(Color.WHITE);
            } else {
                tab.setBackgroundResource(R.drawable.bg_tab_n);
                tab.setTextColor(ResourcesUtils.getColor(R.color.colorPrimary));
            }
        }

        //?????????????????????
        if (mProgressDisposable != null) mProgressDisposable.dispose();
        mProgressDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(aLong -> {
//                    Log.e("apply", "aLong=" + aLong);
//                    return FcUtils.getNowTime();
                    return aLong;
                })
                .compose(IOUtils.setThread())
                .subscribe(residueTime -> {
                    //???????????????
                    updataProgress();
                });
    }

    /*??????*/
    private void onMediaStart() {
        getViewDataBinding().tv.tvProgressMaxText.setText(StringUtils.formatDuration(mMediaBinder.getDuration()));
        getViewDataBinding().tv.tvProgress.setMax(mMediaBinder.getDuration());
        getViewDataBinding().tv.tvPlay.setImageResource(R.drawable.ic_tv_pause);
    }

    /*??????*/
    private void onMediaPause() {
        getViewDataBinding().tv.tvPlay.setImageResource(R.drawable.ic_tv_play);
    }

    /*??????*/
    private void onMediaStop() {
        getViewDataBinding().tv.tvPlay.setImageResource(R.drawable.ic_tv_play);
    }

    /*????????????*/
    private void onMediaCompletion(int oosition) {
        mMediaBinder.next();
    }

    /*???????????????*/
    private void updataProgress() {
        int position = mMediaBinder.getCurrentPosition();
        getViewDataBinding().tv.tvProgress.setProgress(position);
        getViewDataBinding().tv.tvProgressText.setText(StringUtils.formatDuration(position));
    }

///////////////////////?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????///////////////////////

    /*????????????????????????*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
//        ????????????????????????????????????????????????????????????
//        onSaveInstanceState(Bundle outState)
//        ????????????????????????????????????????????????
//        onRestoreInstanceState(Bundle savedInstanceState)
        super.onConfigurationChanged(newConfig);
        initWindowUI();

        Log.e("???????????????", "onConfigurationChanged" + newConfig.orientation);
        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  //????????????
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaBinder != null) {
            Log.e("????????????", "onStart");
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribe(aLong -> {
                        //???????????????????????????
                        mMediaBinder.setDisplay(getViewDataBinding().tv.tvShow.getHolder());
                        mMediaBinder.start();
                    });
        }
    }

    @Override
    public void onResume() {
        initWindowUI();
        super.onResume();
        changeSensor();
        //?????????????????????????????????
        FcUtils.getContext().getContentResolver()
                .registerContentObserver(Settings.System
                                .getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                        false, mScreenRotateObserver);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaBinder != null) {
            Log.e("????????????", "onStop");
            mMediaBinder.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //??????rxbus
        mRxBusClient.unregister();
        if (mProgressDisposable != null) mProgressDisposable.dispose();
        //?????????????????????????????????
        FcUtils.getContext().getContentResolver()
                .unregisterContentObserver(mScreenRotateObserver);
        if (mMediaServiceConnection != null)
            unbindService(mMediaServiceConnection);
        if (mMediaPlayerIntent != null)
            stopService(mMediaPlayerIntent);
    }

    /*??????????????????*/
    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
        if (requestedOrientation == SCREEN_DIRECTION_UNDEFINED | requestedOrientation == mScreenDirection)
            return;
        super.setRequestedOrientation(requestedOrientation);
        mScreenDirection = requestedOrientation;
    }

    /*?????????????????????????????????*/
    private void initGravitySener() {
        // ????????????????????????
        mSensorManager = (SensorManager) FcUtils.getContext()
                .getSystemService(Context.SENSOR_SERVICE);
        // ?????????????????????
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mSensorEventListener = new SensorEventListener() {
            /*???????????????Sensor???????????????*/
            @Override
            public void onSensorChanged(SensorEvent event) {

                //????????????????????????
                if (Sensor.TYPE_GRAVITY != event.sensor.getType()) return;

                //??????xy??????????????????
                float[] values = event.values;
                float x = values[0];
                float y = values[1];
//                Log.e("?????????mmp","z="+values[2]);

                //???????????????????????????
                int newOrientation = SCREEN_DIRECTION_UNDEFINED;
                if (x < 4.5 && x >= -4.5 && y >= 4.5) {//??????
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else if (x >= 4.5 && y < 4.5 && y >= -4.5) {//??????
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else if (x <= -4.5 && y < 4.5 && y >= -4.5) {//????????????
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                } else if (x < 4.5 && x >= -4.5 && y < -4.5) {//????????????
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
//                if (x < 3.5 && x >= -3.5) {//????????????????????????????????????
//                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
//                } else if (y < 9 && y >= -9) {//????????????????????????????????????
//                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
//                }

//                Log.e("?????????????????????", "newOrientation=" + newOrientation + "____x=" + x + "____y=" + y);
                Log.e("?????????????????????", "newOrientation=" + newOrientation + "mScreenDirection=" + mScreenDirection);
                setRequestedOrientation(newOrientation);

            }

            /*???????????????Sensor?????????????????????*/
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        mScreenRotateObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                changeSensor();
            }
        };
    }

    /*????????????????????????????????????????????????*/
    protected void changeSensor() {
        //????????????????????????????????????????????????
        if (DeviceUtils.isOpenScreenRotate())
            mSensorManager.registerListener(mSensorEventListener, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        else
            mSensorManager.unregisterListener(mSensorEventListener);
    }

    /* ???????????????service*/
    protected void bindMediaService() {
        mMediaPlayerIntent = new Intent(FcUtils.getContext(), MediaPlayerService.class);
        startService(mMediaPlayerIntent);


        mMediaServiceConnection = new MediaServiceConnection();
        bindService(mMediaPlayerIntent, mMediaServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private class MediaServiceConnection implements ServiceConnection {
        /*??????service???????????????????????????*/
        @SuppressLint("CheckResult")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("????????????", "????????????" + new SimpleDateFormat("mm:ss:")
                    .format(new Date()));
            mMediaBinder = (MediaBinder) service;
            mMediaBinder.setMedias(getViewDataBinding().getMovie().getMovies());


            //????????????
            play(0);
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribe(aLong -> {
                        //???????????????????????????
                        mMediaBinder.setDisplay(getViewDataBinding().tv.tvShow.getHolder());
                    });

        }

        /*??????service?????????????????????????????????*/
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.e("?????????callback", "surfaceCreated???holder=" + holder);
            if (mMediaBinder != null)
                holder.setFixedSize(mMediaBinder.getVideoWidth(), mMediaBinder.getVideoHeight());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.e("??????callback", "surfaceChanged???holder=" + holder);
            Log.e("??????callback", "width=" + width + "||height=" + height);
            if (mMediaBinder != null) {
                float videoRatio = mMediaBinder.getVideoWidth() / mMediaBinder.getVideoHeight();
                float ratio = width / height;
                if (ratio > videoRatio) {//???????????????
                    width = (int) (height * videoRatio);
                } else if (ratio < videoRatio) {//???????????????
                    height = (int) (width / videoRatio);
                }
                holder.setFixedSize(width, height);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e("??????callback", "surfaceChanged???holder=" + holder);
        }
    };


///////////////////////????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????///////////////////////

    ListAdaper mListAdaper;

    @SuppressLint("CheckResult")
    private void initData(int surce, String path) {
        Observable.create((ObservableOnSubscribe<Movie>) emitter -> {
            Movie entity = LoadUtils.getData(surce, path,
                    new TypeToken<CYEntity<Movie>>() {
                    });
            emitter.onNext(entity);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(movie -> {
                    Log.d("videoAct", "???????????????:" + movie.getMovies().get(0).getTitle());
                    getViewDataBinding().setMovie(movie);

                    //?????????????????????
                    bindMediaService();
                    if (movie.getMovies() != null)
                        if (movie.getMovies().size() > 1) {
                            getViewDataBinding().other.list.setItemAnimator(new DefaultItemAnimator());
                            for (MediaModel model : movie.getMovies()) {
                                if (StringUtils.noNull(model.getUrl()))
//                                    addMediaTab(model);
                                    mListAdaper.datas.add(model);
                            }
                            mListAdaper.setItemClickListener((viewHolder, position, data) -> {
                                //????????????
                                if (mMediaBinder != null) {
                                    play(position);
                                }
                            });
                        } else if (movie.getMovies().size() == 1)
                            movie.getMovies().get(0).setTitle(movie.getTitle());
                });
        Observable.timer(500, TimeUnit.MILLISECONDS)
                .compose(IOUtils.setThread())
                .subscribe(aLong -> {
//                        Animation animation = new AlphaAnimation(1F, 0F);
//                        animation.setDuration(400);
//                        getViewDataBinding().other.shade.startAnimation(animation);
                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            getViewDataBinding().other.getRoot(), //???????????????View??????
                            getViewDataBinding().getRoot().getWidth() / 2,
                            getViewDataBinding().getRoot().getWidth(), //??????????????????
                            50, //????????????????????????
                            1920);
                    animator.setDuration(500);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.start();
                    getViewDataBinding().other.shade.setVisibility(View.GONE);
                });
    }

    /*????????????ui*/
    private void addMediaTab(MediaModel model) {
        TextView tv = new TextView(FcUtils.getContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
//        lp.width = (getViewDataBinding().other.list.getMeasuredWidth() - DimenUtils.dp2px(16))
//                / getViewDataBinding().other.list.getColumnCount()
//                - DimenUtils.dp2px(16);
        lp.height = DimenUtils.dp2px(48);
        lp.topMargin = DimenUtils.dp2px(6);
        lp.bottomMargin = DimenUtils.dp2px(6);
        lp.leftMargin = DimenUtils.dp2px(8);
        lp.rightMargin = DimenUtils.dp2px(8);
        tv.setLayoutParams(lp);
        tv.setPadding(DimenUtils.dp2px(8), DimenUtils.dp2px(6), DimenUtils.dp2px(8), DimenUtils.dp2px(6));
//        tv.setPadding(DimenUtils.dp2px(8), 0, DimenUtils.dp2px(8), 0);
        tv.setMaxLines(2);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        if (getViewDataBinding().other.list.getChildCount() == 0) {
            tv.setBackgroundResource(R.drawable.bg_tab_h);
            tv.setTextColor(Color.WHITE);
        } else {
            tv.setBackgroundResource(R.drawable.bg_tab_n);
            tv.setTextColor(ResourcesUtils.getColor(R.color.colorPrimary));
        }
        tv.setGravity(Gravity.TOP | Gravity.LEFT);
        tv.setText(model.getTitle());
        //????????????
        final int position = getViewDataBinding().other.list.getChildCount();
        getViewDataBinding().other.list.addView(tv);

        //????????????
        tv.setOnClickListener(v -> {
            //????????????
            if (mMediaBinder != null) {
                play(position);
            }
        });
    }

    public void nullClick(View view) {
    }

    class ListAdaper extends RecyclerView.Adapter<ListHolder> {

        List<MediaModel> datas;
        BindingRecyclerAdapter.OnItemClickListener itemClickListener;

        ListAdaper() {
            datas = new ArrayList<>();
        }

        public void setItemClickListener(BindingRecyclerAdapter.OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public ListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            TextView tv = new TextView(FcUtils.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT);
            lp.topMargin = DimenUtils.dp2px(6);
            lp.bottomMargin = DimenUtils.dp2px(6);
            lp.leftMargin = DimenUtils.dp2px(8);
            lp.rightMargin = DimenUtils.dp2px(8);
            tv.setLayoutParams(lp);
            tv.setPadding(DimenUtils.dp2px(8), DimenUtils.dp2px(6), DimenUtils.dp2px(8), DimenUtils.dp2px(6));
            tv.setMaxLines(2);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            if (getViewDataBinding().other.list.getChildCount() == 0) {
                tv.setBackgroundResource(R.drawable.bg_tab_h);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundResource(R.drawable.bg_tab_n);
                tv.setTextColor(ResourcesUtils.getColor(R.color.colorPrimary));
            }
            tv.setGravity(Gravity.TOP | Gravity.LEFT);

            return new ListHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ListHolder viewHolder, int i) {
            viewHolder.updata(i, datas.get(i));
            if (itemClickListener != null)
                viewHolder.itemView.setOnClickListener(v ->
                        itemClickListener.onItemClick(null, i, datas.get(i)));
        }

        @Override
        public int getItemCount() {
            if (datas == null) return 0;
            return datas.size();
        }
    }

    class ListHolder extends RecyclerView.ViewHolder {

        TextView title;

        public ListHolder(@NonNull TextView itemView) {
            super(itemView);
            setIsRecyclable(false);
            title = itemView;
        }

        public void updata(int position, MediaModel model) {
            title.setText(model.getTitle());
        }


    }
}
