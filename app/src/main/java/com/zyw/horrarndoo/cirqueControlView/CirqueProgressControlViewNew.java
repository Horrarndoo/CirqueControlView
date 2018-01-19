package com.zyw.horrarndoo.cirqueControlView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zyw.horrarndoo.cirqueControlView.utils.ChartUtils;

/**
 * Created by Horrarndoo on 2018/1/18.
 * <p>
 */
public class CirqueProgressControlViewNew extends View {
    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 圆环的颜色
     */
    private int roundColor;

    /**
     * 圆环进度的颜色
     */
    private int roundProgressColor;

    /**
     * 圆环的宽度
     */
    private float roundWidth;

    /**
     * 最大进度
     */
    private int max;

    /**
     * 当前进度
     */
    private int progress;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    private float textSize;

    /**
     * 点的半径
     */
    private float pointRadius;

    /**
     * 空心点的宽度
     */
    private float pointWidth;

    private Drawable mDragDrawable, mDragPressDrawable;

    public CirqueProgressControlViewNew(Context context) {
        this(context, null);
    }

    public CirqueProgressControlViewNew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirqueProgressControlViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();

        //获取自定义属性和默认值
        roundColor = Color.RED;
        roundProgressColor = Color.GREEN;
        roundWidth = 3;
        textColor = Color.GREEN;
        textSize = 30;
        max = 100;

        pointRadius = 3;
        pointWidth = 2;

        // 加载拖动图标
        mDragDrawable = getResources().getDrawable(R.drawable.ring_dot);// 圆点图片
        int thumbHalfheight = mDragDrawable.getIntrinsicHeight() / 2;
        int thumbHalfWidth = mDragDrawable.getIntrinsicWidth() / 2;
        mDragDrawable.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);

        mDragPressDrawable = getResources().getDrawable(R.drawable.ring_dot);// 圆点图片
        thumbHalfheight = mDragPressDrawable.getIntrinsicHeight() / 2;
        thumbHalfWidth = mDragPressDrawable.getIntrinsicWidth() / 2;
        mDragPressDrawable.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);
        paddingOuterThumb = thumbHalfheight;
    }


    @Override
    public void onDraw(Canvas canvas) {
        /**
         * 画最外层的大圆环
         */
        paint.setColor(roundColor); //设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); //设置空心
        paint.setStrokeWidth(roundWidth); //设置圆环的宽度
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawCircle(centerX, centerY, radius, paint); //画出圆环

        /**
         * 画文字
         */
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        String textTime = getTimeText(progress);
        float textWidth = paint.measureText(textTime);   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间

        canvas.drawText(textTime, centerX - textWidth / 2, centerY + textSize / 2, paint);

        /**
         * 画圆弧 ，画圆环的进度
         */
        paint.setStrokeWidth(roundWidth); //设置圆环的宽度
        paint.setColor(roundProgressColor);  //设置进度的颜色
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY +
                radius);  //用于定义的圆弧的形状和大小的界限

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval, 270, 360 * progress / max, false, paint);  //根据进度画圆弧

        // 画圆上的两个点
        paint.setStrokeWidth(pointWidth);
        PointF startPoint = ChartUtils.calcArcEndPointXY(centerX, centerY, radius, 0, 270);
        canvas.drawCircle(startPoint.x, startPoint.y, pointRadius, paint);

        PointF progressPoint = ChartUtils.calcArcEndPointXY(centerX, centerY, radius, 360 *
                progress / max, 270);
        // 画Thumb
        canvas.save();
        canvas.translate(progressPoint.x, progressPoint.y);
        if (downOnArc) {
            mDragPressDrawable.draw(canvas);
        } else {
            mDragDrawable.draw(canvas);
        }
        canvas.restore();
    }

    private boolean downOnArc = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchArc(x, y)) {
                    downOnArc = true;
                    updateArc(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downOnArc) {
                    updateArc(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                downOnArc = false;
                invalidate();
                if (changeListener != null) {
                    changeListener.onProgressChangeEnd(max, progress);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private int centerX, centerY;
    private int radius;
    private int paddingOuterThumb;

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        centerX = width / 2;
        centerY = height / 2;
        int minCenter = Math.min(centerX, centerY);

        radius = (int) (minCenter - roundWidth / 2 - paddingOuterThumb); //圆环的半径
        minValidateTouchArcRadius = (int) (radius - paddingOuterThumb * 1.5f);
        maxValidateTouchArcRadius = (int) (radius + paddingOuterThumb * 1.5f);
        super.onSizeChanged(width, height, oldw, oldh);
    }

    // 根据点的位置，更新进度
    private void updateArc(int x, int y) {
        int cx = x - getWidth() / 2;
        int cy = y - getHeight() / 2;
        // 计算角度，得出（-1->1）之间的数据，等同于（-180°->180°）
        double angle = Math.atan2(cy, cx) / Math.PI;
        // 将角度转换成（0->2）之间的值，然后加上90°的偏移量
        angle = ((2 + angle) % 2 + (90 / 180f)) % 2;
        // 用（0->2）之间的角度值乘以总进度，等于当前进度
        progress = (int) (angle * max / 2);
        if (changeListener != null) {
            changeListener.onProgressChange(max, progress);
        }
        invalidate();
    }

    private int minValidateTouchArcRadius; // 最小有效点击半径
    private int maxValidateTouchArcRadius; // 最大有效点击半径

    // 判断是否按在圆边上
    private boolean isTouchArc(int x, int y) {
        double d = getTouchRadius(x, y);
        if (d >= minValidateTouchArcRadius && d <= maxValidateTouchArcRadius) {
            return true;
        }
        return false;
    }

    // 计算某点到圆点的距离
    private double getTouchRadius(int x, int y) {
        int cx = x - getWidth() / 2;
        int cy = y - getHeight() / 2;
        return Math.hypot(cx, cy);
    }

    private String getTimeText(int progress) {
        int minute = progress / 60;
        int second = progress % 60;
        String result = (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second;
        return result;
    }

    public synchronized int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     *
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }

    public int getCricleColor() {
        return roundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }

    private OnProgressChangeListener changeListener;

    public void setChangeListener(OnProgressChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public interface OnProgressChangeListener {
        void onProgressChange(int duration, int progress);

        void onProgressChangeEnd(int duration, int progress);
    }
}
