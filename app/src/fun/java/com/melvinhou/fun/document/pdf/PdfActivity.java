package com.melvinhou.fun.document.pdf;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.view.activities.BaseActivity2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/5/31 21:52
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class PdfActivity extends BaseActivity2 {

    static final String FILE_PDF_ROOT = "pdf";
    static final String FILE_NAME = "test.pdf";

    private RecyclerView mListView;
    private TextView mPageNumber;
    private SignatureImageView mTabletView;
    private ImageView mTabletBgView;
    private Group mTabletGroup;

    private String mUrl;
    private MyAdapter mAdapter;
    private int savePdfWidth, savePdfHeight, savePdfNumber;

    /**
     * 在assets文件夹中放入FILE_NAME文件
     * 然后直接启动
     */
    private PdfDocument mDocument;
    private PdfRenderer mRenderer;
    private ParcelFileDescriptor mDescriptor;
    private List<Bitmap> mBitmaps;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_pdf;
    }

    @Override
    protected void onDestroy() {
        //销毁页面的时候释放资源,习惯
        try {
            closeRenderer();
            closeDocument();
            deleteCacheFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_function, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signature:
                openTablet();
                return true;
            case R.id.save:
                try {
                    openDocument(FILE_NAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView() {
        mUrl = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(mUrl)) {
            mUrl = HttpConstant.SERVER_RES +"pdf/live2d.pdf";
            title = "Live2D Config Generator 使用说明";
        }
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(title);

        mListView = findViewById(R.id.list);
        mPageNumber = findViewById(R.id.page_number);
        mTabletView = findViewById(R.id.tablet);
        mTabletBgView = findViewById(R.id.tablet_background);
        mTabletGroup = findViewById(R.id.group_tablet);

//        Log.e("位置", "getExternalStorageDirectory=" + Environment.getExternalStorageDirectory().getPath());
//        Log.e("位置", "getDataDirectory=" + Environment.getDataDirectory().getPath());
//        Log.e("位置", "getDownloadCacheDirectory=" + Environment.getDownloadCacheDirectory().getPath());
//        Log.e("位置", "getRootDirectory=" + Environment.getRootDirectory().getPath());
//        Log.e("位置", "getFilesDir=" + FcUtils.getContext().getFilesDir().getPath());
//        Log.e("位置", "getCacheDir=" + FcUtils.getContext().getCacheDir().getPath());
//        Log.e("位置", "getDir=" + FcUtils.getContext().getDir("233", Context.MODE_PRIVATE).getPath());
//        Log.e("位置", "getExternalCacheDir=" + FcUtils.getContext().getExternalCacheDir().getPath());
    }

    @Override
    protected void initListener() {
        findViewById(R.id.tablet_back).setOnClickListener(v -> backward());
        findViewById(R.id.tablet_save).setOnClickListener(v -> saveSignature());
        //初始化ViewPager的适配器并绑定
        mAdapter = new MyAdapter();
        mListView.setLayoutManager(new LinearLayoutManager(
                FcUtils.getContext(), LinearLayoutManager.HORIZONTAL, false));
        new PagerSnapHelper().attachToRecyclerView(mListView);
        mListView.setAdapter(mAdapter);
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == recyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    View child = recyclerView.getChildAt(0);
                    if (child != null) {
                        int position = layoutManager.getPosition(child);
                        mPageNumber.setText((position + 1) + "/" + mAdapter.getItemCount());
                    }
                }
            }
        });

    }

    @Override
    protected void initData() {

    }

    /**
     * 打开签字版
     */
    private void openTablet() {
        mTabletGroup.setVisibility(View.VISIBLE);
        View currentChild = mListView.getChildAt(0);
        int currentPosition = mListView.getChildAdapterPosition(currentChild);
        Bitmap bitmap = mBitmaps.get(currentPosition);
        mTabletBgView.setImageBitmap(bitmap);
        //更新写字板大小
        int[] size = getDisplaySize();
        int width = size[0];
        int height = width * bitmap.getHeight() / bitmap.getWidth();
        ViewGroup.LayoutParams lp = mTabletView.getLayoutParams();
        lp.height = height;
        mTabletView.setLayoutParams(lp);
        mTabletView.startSignature();
    }

    /**
     *
     */
    @Override
    protected void onLoading() {
        showLoadingView(true);
        changeRequestState(RequestState.RUNNING);
        downloadPdf(mUrl, FILE_NAME, new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String fileName) {
                hideLoadingView();
                try {
                    openRender(fileName);
                } catch (IOException e) {
                    FcUtils.showToast("pdf打开失败");
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                changeRequestState(ResultState.FAILED);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void backward() {
        if (mTabletGroup.getVisibility() == View.VISIBLE) {
            mTabletView.startSignature();
            mTabletGroup.setVisibility(View.GONE);
        } else
            super.backward();
    }

    public File getCacheFile(String fileName) {
        return new File(getDir(FILE_PDF_ROOT, MODE_PRIVATE).getPath() + File.separator + fileName);
    }

    public File getOutPdfFile(String fileName) {
        File folderFile = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Dimension2" + File.separator + "pdf");
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        File file = new File(folderFile, "out_" + fileName);
        return file;
    }


////////////////////////////////////////pdf阅读//////////////////////////////////////////////

    /**
     * 下载文件到私有目录
     *
     * @param httpPath 网络地址
     * @param fileName 文件名
     * @param observer 观察者
     */
    private void downloadPdf(String httpPath, String fileName, Observer<String> observer) {
        Observable observable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            FileOutputStream fos = null;
            InputStream is = null;
            byte[] buf = new byte[1024 * 1000];
            try {
                URL url = new URL(httpPath);
                //初始化输出流
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                File file = getCacheFile(fileName);
                if (file.exists()) {
                    emitter.onNext(fileName);
                    return;
                }
                fos = new FileOutputStream(file);
                is = conn.getInputStream();
                //开始写入
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                //返回文件名
                emitter.onNext(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                emitter.onError(e);
            } finally {
                IOUtils.close(is);
                IOUtils.close(fos);
                emitter.onComplete();
            }
        }).compose(IOUtils.setThread());
        //判断是否有回调
        if (observer != null) observable.subscribe(observer);
        else observable.subscribe();
    }

    @SuppressLint({"NewApi", "CheckResult"})
    private void openRender(String fileName) throws IOException {
        File file = getCacheFile(fileName);
        //判断文件是否存在
        if (!file.exists()) {
            FcUtils.showToast("pdf打开失败");
            return;
        }
        //初始化PdfRender
        mDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (mDescriptor != null) {
            mRenderer = new PdfRenderer(mDescriptor);
        }
        //加载到图片
        if (mRenderer != null) {
            savePdfNumber = mRenderer.getPageCount();
            Observable.create((ObservableOnSubscribe<List<Bitmap>>) emitter -> {
                List<Bitmap> datas = new ArrayList<>();
                for (int position = 0; position < mRenderer.getPageCount(); position++) {
                    PdfRenderer.Page currentPage = mRenderer.openPage(position);
                    int width = currentPage.getWidth();
                    int height = currentPage.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    datas.add(bitmap);
                    //关闭当前Page对象
                    currentPage.close();
                }
                emitter.onNext(datas);
                emitter.onComplete();
            })
                    .compose(IOUtils.setThread())
                    .subscribe(datas -> {
                        if (datas != null && datas.size() > 0) {
                            mBitmaps = datas;
                            //刷新
                            if (mAdapter != null) mAdapter.notifyDataSetChanged();
                            //更新页码
                            mPageNumber.setText("1/" + mAdapter.getItemCount());
                        }
                    });
        }
    }

    @SuppressLint("NewApi")
    private void closeRenderer() throws IOException {
        if (mRenderer != null) mRenderer.close();
        if (mDescriptor != null) mDescriptor.close();
    }

    private void deleteCacheFile() {
        getCacheFile(FILE_NAME).delete();
    }

////////////////////////////////////////pdf存储//////////////////////////////////////////////


    @SuppressLint("NewApi")
    private void openDocument(String fileName) throws IOException {
        if (mBitmaps == null || mBitmaps.size() == 0) return;
        mDocument = new PdfDocument();
        Paint paint = new Paint();
        for (int i = 0; i < mBitmaps.size(); i++) {
            Bitmap bitmap = mBitmaps.get(i);
            savePdfWidth = bitmap.getWidth();
            savePdfHeight = bitmap.getHeight();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    savePdfWidth, savePdfHeight, savePdfNumber).create();
            PdfDocument.Page page = mDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
//            canvas.translate(0,-onePageHeight*i);
//            mListView.getChildAt(0).draw(canvas);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            mDocument.finishPage(page);
        }
        File file = getOutPdfFile(fileName);
        if (!file.exists()) {
            FileOutputStream outputStream = new FileOutputStream(file);
            mDocument.writeTo(outputStream);
            outputStream.close();
        } else {
            file.delete();
            FileOutputStream outputStream = new FileOutputStream(file);
            mDocument.writeTo(outputStream);
            outputStream.close();
        }
        mDocument.close();
        FcUtils.showToast("保存位置:"+file.getPath());
    }

    /**
     * 保存签名
     */
    private void saveSignature() {
        View currentChild = mListView.getChildAt(0);
        int currentPosition = mListView.getChildAdapterPosition(currentChild);
        Bitmap oldBitmap = mBitmaps.get(currentPosition);
        float scaling = (float) oldBitmap.getWidth() / (float) mTabletView.getWidth();
//        Bitmap bgBitmap = Bitmap.createBitmap(
//                mTabletView.getWidth(), mTabletView.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap newBitmap = ImageUtils.resizeBitmap(oldBitmap, 1f / scaling);
        //签名
        Canvas canvas = new Canvas(newBitmap);
        mTabletView.draw(canvas);
        //保存图片
        Bitmap finalBitmap = ImageUtils.resizeBitmap(newBitmap, scaling);
        if (finalBitmap != null) {
            mBitmaps.set(currentPosition, finalBitmap);
            mAdapter.notifyItemChanged(currentPosition);
            backward();
        }
    }

    @SuppressLint("NewApi")
    private void closeDocument() throws IOException {
        if (mDocument != null)
            mDocument.close();
    }


    /**
     * 获取屏幕宽高
     *
     * @return
     */
    private int[] getDisplaySize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
    }


    class MyAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(getItemView()) {
            };
        }

        private View getItemView() {
            ImageView imageView = new ImageView(FcUtils.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;
        }

        @SuppressLint("NewApi")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImageView img = (ImageView) holder.itemView;
            img.setImageBitmap(mBitmaps.get(position));
            //当前位置 int currentPosition;
        }

        /*
          public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
          ImageView img = (ImageView) holder.itemView;
          PdfRenderer.Page currentPage = mRenderer.openPage(position);
          if (savePdfWidth <= 0) {
          savePdfWidth = currentPage.getWidth();
          savePdfHeight = currentPage.getHeight();
          }
          int[] size = getDisplaySize();
          int width = size[0];
          int height = width * currentPage.getHeight() / currentPage.getWidth();
          Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
          currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
          img.setImageBitmap(bitmap);
          //关闭当前Page对象
          currentPage.close();
          }
         */

        @SuppressLint("NewApi")
        @Override
        public int getItemCount() {
            if (mBitmaps != null) return mBitmaps.size();
            else return 0;
        }
    }
}
