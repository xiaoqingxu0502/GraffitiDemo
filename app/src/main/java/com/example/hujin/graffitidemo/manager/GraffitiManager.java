package com.example.hujin.graffitidemo.manager;

import com.example.hujin.graffitidemo.MyApplication;
import com.example.hujin.graffitidemo.custom.GraffitiView;
import com.example.hujin.graffitidemo.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Package com.example.hujin.graffitidemo.manager
 * @Description: 涂鸦状体管理类
 * @Author hujin
 * @Date 2017/6/2 上午10:32
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class GraffitiManager {
    private static volatile GraffitiManager mInstance = null;

    /*图片保存路径*/
    public static final String FILE_SAVE_PATH = "file_save_path";

    private String mSavePath;

    private List<GraffitiView.GraffitiPath> mGraffitiPath = new ArrayList<>();

    private GraffitiManager(){}

    public static GraffitiManager creat() {
        if (mInstance == null) {
            synchronized (GraffitiManager.class) {
                if (mInstance == null) {
                    mInstance = new GraffitiManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * @Description: 创建涂鸦图片保存路径
     */
    public void creatGraffitiPath() {
        String root = FileUtil.cachePath(MyApplication.appContext, "task");
        File studentF = new File(root);
        if (!studentF.exists()) {
            studentF.mkdirs();
        }
        mSavePath = root;

    }

    public List<GraffitiView.GraffitiPath> getGraffitiPath() {

        return mGraffitiPath;
    }

    public void setGraffitiPath(List<GraffitiView.GraffitiPath> graffitiPath) {
        this.mGraffitiPath = graffitiPath;
    }

    public void clearGraffitiPath() {
        mGraffitiPath.clear();
    }

    public String getSavePath() {

        return mSavePath;
    }

    public static class ResultCode {

        public static final int GRAFFITI_CLEAR = 101;


    }
}
