# CirqueControlView
自定义可拖动圆环进度控制控件


前几天收到这么一个需求，本来以为挺简单的，没想到最后发现实现起来还是有点小麻烦的，在这里小小的总结一下。

先看看下面这张需求的样图：

![样图](https://raw.githubusercontent.com/Horrarndoo/imageAssets/master/cirque_control_view/demo.png)

然后在看一下最终实现的效果图，可能是gif录制软件的问题，有一些浮影，忽略就好了= = ：
![最终demo图](https://raw.githubusercontent.com/Horrarndoo/imageAssets/master/cirque_control_view/final.gif)

首先要分析一下最核心的地方，如何获取到滑动距离对应的弧长，看图：
![参考图](https://raw.githubusercontent.com/Horrarndoo/imageAssets/master/cirque_control_view/pic.png)

p1是手指按下的点，很明显要想知道当前进度弧边的值，就是要求出角d的值。
以p为圆心点，atan(b)=Math.atan((-p.y)/(-p.x));
所以角d的值为：Math.toDegrees(atan);
那么角b的值就得出来了，b=Math.toDegrees(atan) + mProgressOffest;
图中的圆可以分为四个象限，同理可以得出四个象限中求得弧长的方法：

```
    /**
     * 更新当前进度对应弧度
     *
     * @param x 按下x坐标点
     * @param y 按下y坐标点
     */
    private void updateCurrentAngle(float x, float y) {
        //根据坐标转换成对应的角度
        float pointX = x - mCenterX;
        float pointY = y - mCenterY;
        float tan_x;//根据左边点所在象限处理过后的x值
        float tan_y;//根据左边点所在象限处理过后的y值
        double atan;//所在象限弧边angle

        //01：第一象限-右上角区域
        if (pointX >= 0 && pointY <= 0) {
            tan_x = pointX;
            tan_y = pointY * (-1);
            atan = Math.atan(tan_x / tan_y);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + 90.f + mProgressOffest;
        }

        //02：第二象限-左上角区域
        if (pointX <= 0 && pointY <= 0) {
            tan_x = pointX * (-1);
            tan_y = pointY * (-1);
            atan = Math.atan(tan_y / tan_x);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + mProgressOffest;
        }

        //03：第三象限-左下角区域
        if (pointX <= 0 && pointY >= 0) {
            tan_x = pointX * (-1);
            tan_y = pointY;
            atan = Math.atan(tan_x / tan_y);//求弧边
            if ((int) Math.toDegrees(atan) >= (90.f - mProgressOffest)) {
                mCurrentAngle = (int) Math.toDegrees(atan) - (90.f - mProgressOffest);
            } else {
                mCurrentAngle = (int) Math.toDegrees(atan) + 270.f + mProgressOffest;
            }
        }

        //04：第四象限-右下角区域
        if (pointX >= 0 && pointY >= 0) {
            tan_x = pointX;
            tan_y = pointY;
            atan = Math.atan(tan_y / tan_x);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + 180.f + mProgressOffest;
        }
    }
```
获取手指按下的区域，避免误判断：

```
    /**
     * 按下时判断按下的点是否按在圆环范围内
     *
     * @param x x坐标点
     * @param y y坐标点
     */
    private boolean isTouchArc(float x, float y) {
        double d = getTouchRadius(x, y);
        return d >= mMinValidateTouchArcRadius && d <= mMaxValidateTouchArcRadius;
    }

    /**
     * 计算某点到圆点的距离
     *
     * @param x x坐标点
     * @param y y坐标点
     */
    private double getTouchRadius(float x, float y) {
        float cx = x - getWidth() / 2;
        float cy = y - getHeight() / 2;
        return Math.hypot(cx, cy);
    }
```
绘制bitmap；

```
    /**
     * 绘制小圆点bitmap
     *
     * @param canvas canvas
     */
    private void drawDragBitmap(Canvas canvas) {
        PointF progressPoint = ChartUtils.calcArcEndPointXY(mCenterX, mCenterY, mRadius,
                mCurrentAngle, 180.f - mProgressOffest);

        int left = (int) progressPoint.x - mDragBitmap.getWidth() / 2;
        int top = (int) progressPoint.y - mDragBitmap.getHeight() / 2;

        //        mBitmapRect = new Rect(left, top, left + mDragBitmap.getWidth(), top +
        //                mDragBitmap.getHeight());
        //
        //        canvas.drawBitmap(mDragBitmap,
        //                new Rect(0, 0, mDragBitmap.getWidth(), mDragBitmap.getHeight()),
        //                mBitmapRect, mBitmapPaint);
        //bitmap直接使用BitmapUtils中的缩放方法缩放，可以不用Rect进行缩放，也可以通过限定Rect来限定bitmap大小
        canvas.drawBitmap(mDragBitmap, left, top, mBitmapPaint);
    }
```
重写onTouchEvent事件；

```
@Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取点击位置的坐标
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchArc(x, y)) {
                    mTouchQuadrant = getTouchQuadrant(x, y);
                    mIsTouchOnArc = true;
                    updateCurrentAngle(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsTouchOnArc) {
                    updateCurrentAngle(x, y);
                    if (mOnCirqueProgressChangeListener != null)
                        mOnCirqueProgressChangeListener.onChange(mMinProgress, mMaxProgress,
                                Integer.parseInt(mText.replace("℃", "")));
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsTouchOnArc = false;
                mTouchQuadrant = 0;
                if (mOnCirqueProgressChangeListener != null)
                    mOnCirqueProgressChangeListener.onChangeEnd(mMinProgress, mMaxProgress,
                            Integer.parseInt(mText.replace("℃", "")));
                break;
        }

        invalidate();
        return true;
    }
```
到这里基本这个自定义控件也就实现完了。但是！是不是！忘了点！什么？没错，就是让我蛋疼不已的圆环上下限值判断。
由于手指滑动的时候，当前的angle值的范围是0-360，因此不可能简单的限定上下限。没有做任何判断的话，在起点处是可以随意滑动的，如下图所示：

![样图2](https://raw.githubusercontent.com/Horrarndoo/imageAssets/master/cirque_control_view/control.gif)

很明显这样是不行的，然后就是一阵鸡飞狗跳，简（ou）简（xin）单（li）单（xue）的一阵折腾之后，基本实现了要求，最后更新currentAngle的代码如下：

```
    /**
     * 更新当前进度对应弧度
     *
     * @param x 按下x坐标点
     * @param y 按下y坐标点
     */
    private void updateCurrentAngle(float x, float y) {
        //根据坐标转换成对应的角度
        float pointX = x - mCenterX;
        float pointY = y - mCenterY;
        float tan_x;//根据左边点所在象限处理过后的x值
        float tan_y;//根据左边点所在象限处理过后的y值
        double atan;//所在象限弧边angle

        //01：第一象限-右上角区域
        //保证dragBitmap在峰值的时候不会因为滑到这个象限更新currentAngle
        if (pointX >= 0 && pointY <= 0) {
            if (((mLastQuadrant == 3 && mLastAngle == 359.f)
                    || (mLastQuadrant == 3 && mLastAngle == 0.f))
                    && mTouchQuadrant != 1)
                return;

            tan_x = pointX;
            tan_y = pointY * (-1);
            atan = Math.atan(tan_x / tan_y);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + 90.f + mProgressOffest;
            mLastQuadrant = 1;
        }

        //02：第二象限-左上角区域
        if (pointX <= 0 && pointY <= 0) {
            if (((mLastQuadrant == 3 && mLastAngle == 359.f)
                    || (mLastQuadrant == 3 && mLastAngle == 0.f))
                    && mTouchQuadrant != 2) {
                return;
            }

            tan_x = pointX * (-1);
            tan_y = pointY * (-1);
            atan = Math.atan(tan_y / tan_x);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + mProgressOffest;
            mLastQuadrant = 2;
        }

        //03：第三象限-左下角区域
        if (pointX <= 0 && pointY >= 0) {
            tan_x = pointX * (-1);
            tan_y = pointY;
            atan = Math.atan(tan_x / tan_y);//求弧边
            if ((int) Math.toDegrees(atan) >= (90.f - mProgressOffest)) {
                mCurrentAngle = (int) Math.toDegrees(atan) - (90.f - mProgressOffest);
                if (mLastAngle >= 270.f) {
                    mCurrentAngle = 359.f;
                }
            } else {
                mCurrentAngle = (int) Math.toDegrees(atan) + 270.f + mProgressOffest;
                if (mLastAngle <= 90.f) {
                    mCurrentAngle = 0.f;
                }
            }
            mLastQuadrant = 3;
        }

        //04：第四象限-右下角区域
        //保证dragBitmap在峰值的时候不会因为滑到这个象限更新currentAngle
        if (pointX >= 0 && pointY >= 0) {
            if (((mLastQuadrant == 3 && mLastAngle == 359.f)
                    || (mLastQuadrant == 3 && mLastAngle == 0.f))
                    && mTouchQuadrant != 4)
                return;

            tan_x = pointX;
            tan_y = pointY;
            atan = Math.atan(tan_y / tan_x);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + 180.f + mProgressOffest;
            mLastQuadrant = 4;
        }
        mLastAngle = mCurrentAngle;
    }
```

其实做之前就真的觉得是挺简单的一个自定义控件，结果万万没想到因为最后这么一点代码折腾了半天。虽然最后这坨代码看着确实挺蛋疼的，但是暂时也想不到什么好的方法了，先这样吧。

最后贴上完整代码：[https://github.com/Horrarndoo/CirqueControlView](https://github.com/Horrarndoo/CirqueControlView)
