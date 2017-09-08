package com.example.hujin.graffitidemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hujin.graffitidemo.R;
import com.example.hujin.graffitidemo.custom.GraffitiView;
import com.example.hujin.graffitidemo.manager.GraffitiManager;
import com.example.hujin.graffitidemo.util.ViewUtil;
import com.jakewharton.rxbinding2.view.RxView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static com.example.hujin.graffitidemo.manager.GraffitiManager.FILE_SAVE_PATH;

/**
 * @Package com.example.hujin.graffitidemo.activity
 * @Description: 涂鸦页面
 * @Author hujin
 * @Date 2017/6/2 上午9:53
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class GraffitiActivity extends AppCompatActivity {

    @BindView(R.id.gv_bitmap)
    GraffitiView mGvBitmap;
    @BindView(R.id.img_undo)
    ImageView mImgUndo;
    @BindView(R.id.img_clear)
    ImageView mImgClear;
    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.tv_save)
    TextView mTvSave;
    @BindView(R.id.rl_operation)
    RelativeLayout mRlOperation;

    private Bitmap mShowBitmap;
    private String mPicturePath;
    private int mType = 1;

    public static final String VIEW_MODE = "type";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graffiti);
        mType = getIntent().getIntExtra(VIEW_MODE, 1);
        ButterKnife.bind(this);
        initView();
        initListener();
    }

    private void initView() {
        mShowBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.timg);
        mGvBitmap.setBitmap(mShowBitmap, mType, new GraffitiView.GraffitiListener() {
            @Override
            public void onSaved(Bitmap bitmap) {
                if (bitmap == null) {
                    return;
                }
                mPicturePath = savePath(bitmap);
                finish(RESULT_OK);
            }
        });
        if (mType == 1) {
            mTvSave.setVisibility(View.GONE);
            mRlOperation.setVisibility(View.GONE);
        } else {
            mTvSave.setVisibility(View.VISIBLE);
            mRlOperation.setVisibility(View.VISIBLE);

        }
        if (GraffitiManager.creat().getGraffitiPath() != null && GraffitiManager
                .creat().getGraffitiPath().size() > 0) {
            if (mType == 1) {
                String cachePicture = getIntent().getStringExtra(FILE_SAVE_PATH);
                mShowBitmap = BitmapFactory.decodeFile(cachePicture);
                mGvBitmap.setBitmap(mShowBitmap, mType, new GraffitiView.GraffitiListener() {
                    @Override
                    public void onSaved(Bitmap bitmap) {

                    }
                });
            } else {
                mGvBitmap.setDrawFinish(new GraffitiView.DrawFinish() {
                    @Override
                    public void onDrawFinish() {
                        mGvBitmap.restorePath(GraffitiManager.creat().getGraffitiPath
                                ());
                    }
                });
            }
        }
    }

    private void initListener() {
        RxView.clicks(mImgBack)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        finish(RESULT_CANCELED);

                    }
                });
        RxView.clicks(mTvSave)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        if (mGvBitmap.getGraffitiData() != null && mGvBitmap.getGraffitiData()
                                .size() > 0) {
                            mGvBitmap.save();
                        } else {
                            finish(GraffitiManager.ResultCode.GRAFFITI_CLEAR);
                        }
                    }
                });
        RxView.clicks(mImgUndo)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        mGvBitmap.undo();

                    }
                });
        RxView.clicks(mImgClear)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        mGvBitmap.clear();
                    }
                });


    }

    private String savePath(Bitmap bitmap) {
        String savePath = GraffitiManager.creat().getSavePath();
        String pictureP = savePath + File.separator + "girl" + ".jpg";
        File pictureF = new File(pictureP);
        if (pictureF.exists()) {
            if (!pictureF.delete()) {
                ViewUtil.showMessage("保存失败，请重新保存");
            }
        }
        FileOutputStream outputStream = null;
        if (!pictureF.exists()) {
            try {
                if (pictureF.createNewFile()) {
                    outputStream = new FileOutputStream(pictureF);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                } else {
                    ViewUtil.showMessage("保存失败，请重新保存");
                }
            } catch (IOException e) {
                ViewUtil.showMessage("保存失败，请重新保存");
            }
        }
        GraffitiManager.creat().setGraffitiPath(mGvBitmap.getGraffitiData());
        return pictureP;
    }

    private void finish(int resultCode) {
        if (resultCode == GraffitiManager.ResultCode.GRAFFITI_CLEAR) {
            GraffitiManager.creat().clearGraffitiPath();
        } else if (resultCode == RESULT_OK) {
            GraffitiManager.creat().setGraffitiPath(mGvBitmap.getGraffitiData());
        }
        Intent intent = new Intent();
        intent.putExtra(FILE_SAVE_PATH, mPicturePath);
        setResult(resultCode, intent);
        super.finish();
    }

}
