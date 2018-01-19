package com.zyw.horrarndoo.cirqueControlView.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.DrawableRes;

/**
 * Created by Horrarndoo on 2018/1/18.
 * <p>
 */

public class BitmapUtils {
    /**
     * 获取drawable资源文件图片bitmap
     *
     * @param context context
     * @param id      资源文件id
     * @return 资源文件对应图片bitmap
     */
    public static Bitmap getBitmap(Context context, @DrawableRes int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    /**
     * 转换bitmap宽高
     *
     * @param bitmap    bitmap
     * @param newWidth  新的bitmap宽度
     * @param newHeight 新的bitmap高度
     * @return 转换宽高后的bitmap
     */
    public static Bitmap conversionBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap b = bitmap;
        int width = b.getWidth();
        int height = b.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
    }
}
