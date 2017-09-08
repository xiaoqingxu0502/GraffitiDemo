package com.example.hujin.graffitidemo.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hujin.graffitidemo.R;
import com.example.hujin.graffitidemo.manager.GraffitiManager;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static com.example.hujin.graffitidemo.activity.GraffitiActivity.VIEW_MODE;
import static com.example.hujin.graffitidemo.manager.GraffitiManager.FILE_SAVE_PATH;

/**
 * @Package com.example.hujin.graffitidemo.util
 * @Description: 主页面
 * @Author hujin
 * @Date 2017/6/2 上午11:03
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class MainActivity extends AppCompatActivity {


    Bitmap mShowBitmap;
    @BindView(R.id.img_show)
    ImageView mImgShow;
    @BindView(R.id.tv_preview)
    TextView mTvPreview;
    @BindView(R.id.tv_edit)
    TextView mTvEdit;
    /*涂鸦页面生成的缓存图片*/
    private String mCachePicturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initListener();
    }

    private void initView() {
        mShowBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.timg);
        mImgShow.setImageBitmap(mShowBitmap);
    }

    private void initListener() {
        RxView.clicks(mTvPreview)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        GraffitiManager.creat().creatGraffitiPath();
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt(VIEW_MODE,1);
                        if(!TextUtils.isEmpty(mCachePicturePath)){
                            bundle.putString(FILE_SAVE_PATH,mCachePicturePath);
                        }
                        intent.putExtras(bundle);
                        intent.setClass(MainActivity.this,GraffitiActivity.class);
                        startActivityForResult(intent, 1);
                    }
                });
        RxView.clicks(mTvEdit)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        GraffitiManager.creat().creatGraffitiPath();
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt(VIEW_MODE,2);
                        intent.putExtras(bundle);
                        intent.setClass(MainActivity.this,GraffitiActivity.class);
                        startActivityForResult(intent, 2);
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            mCachePicturePath = data.getStringExtra(FILE_SAVE_PATH);
            mShowBitmap = BitmapFactory.decodeFile(mCachePicturePath);
        }else if (resultCode == GraffitiManager.ResultCode.GRAFFITI_CLEAR){
            mShowBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.timg);
            mCachePicturePath = "";
        }
        mImgShow.setImageBitmap(mShowBitmap);

    }
}
