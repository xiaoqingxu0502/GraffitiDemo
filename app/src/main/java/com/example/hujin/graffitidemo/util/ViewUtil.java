package com.example.hujin.graffitidemo.util;

import android.text.TextUtils;
import android.widget.Toast;

import com.example.hujin.graffitidemo.MyApplication;

/**
 * @Package com.xes.jazhanghui.teacher.correct.model.datasupport
 * @Description: View工具类
 * @Author huji
 * @Date 2017/4/23 下午2:23
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class ViewUtil {
    /*吐司消息提醒*/
    public static void showMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(MyApplication.appContext, message, Toast.LENGTH_SHORT).show();
        }
    }
}
