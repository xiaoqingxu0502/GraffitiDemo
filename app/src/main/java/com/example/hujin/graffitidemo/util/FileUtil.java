package com.example.hujin.graffitidemo.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * @Package com.example.hujin.graffitidemo.util
 * @Description: 文件工具类
 * @Author hujin
 * @Date 2017/6/2 上午11:02
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class FileUtil {
    /**
     * 获得磁盘缓存的路径,sd卡存在或sd卡不可被移除的时候，就用getExternalCacheDir，否则用getCacheDir
     * getExternalCacheDir()-> /sdcard/Android/data/<application package>/cache
     * getCacheDir() -> /data/data/<application package>/cache
     *
     * @param context
     * @param uniqueName 缓存文件夹名称，可以自定义。
     * @return
     */
    public static String cachePath(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()) {
            //低版本手机Environment.DIRECTORY_DOCUMENTS有问题
//            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
            cachePath = context.getExternalCacheDir().getPath();
        } else {
//            cachePath = context.getFilesDir().getPath();
            cachePath = context.getCacheDir().getPath();
        }
        if (TextUtils.isEmpty(cachePath)) {
            return null;
        }
        return cachePath + File.separator + uniqueName;
    }
}
