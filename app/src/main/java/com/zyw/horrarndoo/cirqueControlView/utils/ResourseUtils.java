package com.zyw.horrarndoo.cirqueControlView.utils;

import android.content.Context;
import android.support.annotation.ColorRes;

/**
 * Created by Horrarndoo on 2018/1/18.
 * <p>
 */

public class ResourseUtils {
    /**
     * 获取colors.xml资源文件颜色
     *
     * @param context context
     * @param id      资源文件id
     * @return 资源文件对应颜色值
     */
    public static int getColor(Context context, @ColorRes int id) {
        return context.getResources().getColor(id);
    }

    /**
     * 获取strings.xml资源文件字符串
     *
     * @param context context
     * @param id      资源文件id
     * @return 资源文件对应字符串
     */
    public static String getString(Context context, int id) {
        return context.getResources().getString(id);
    }
}
