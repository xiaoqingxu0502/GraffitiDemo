package com.example.hujin.graffitidemo.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Package com.example.hujin.graffitidemo.custom
 * @Description: 可缩放，可涂鸦的view
 * @Author hujin
 * @Date 2017/6/1 下午8:57
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class GraffitiView extends View {

    private Context mContext;
    /*图片状态*/
    private int mBimapStatus = 0;
    /*预览*/
    private final int STATUS_PREVIEW = 1;
    /*编辑*/
    private final int STATUS_EDIT = 2;

    /*控件的宽度*/
    private int mWidth;
    /*控件的高度*/
    private int mHeight;
    /*预览缩放比（初始化图片是，图片的压缩比*/
    private float mInitScale;
    /*用于对图片进行移动和缩放变换的矩阵*/
    private Matrix mMatrix = new Matrix();
    /*记录图片横向偏移值*/
    private float mTotalTranslateX;
    /*记录图片纵向偏移值*/
    private float mTotalTranslateY;
    /*记录图片总缩放比例*/
    private float mTotalScale;
    /*当前图片的宽*/
    private float mCurrentBitmapwWidth;
    /*预览图片的高*/
    private float mCurrentBitmapHeight;
    /*图片左上角X坐标*/
    private float mBitmapLeftTopX;
    /*图片左上角Y坐标*/
    private float mBitmapLeftTopY;
    /*图片右上角X坐标*/
    private float mBitmapRightTopX;
    /*图片左下角Y坐标*/
    private float mBitmapLetfBottomY;

    /*记录当前操作的状态*/
    private int mCurrentStatus = 0;
    /*初始化状态常量*/
    private static final int STATUS_INIT = 1;
    /*图片变化:移动和缩放*/
    private static final int STATUS_CHANGE = 2;
    /*涂鸦状态常量*/
    private static final int STATUS_HANDWRITING = 3;
    /*清除涂鸦*/
    private static final int STATUS_CLEAR = 4;
    /*撤销涂鸦*/
    private static final int STATUS_UNDO = 5;

    /*缩放控制器*/
    private ScaleGestureDetector mScaleDetector;
    /*图片被拖动的X距离*/
    private float mFocusX = 0.f;
    /*图片被拖动的Y距离*/
    private float mFocusY = 0.f;

    /*画笔*/
    private Paint mPaint;
    /*绘制涂鸦的原图*/
    private Bitmap mSourceBitmap;
    /*每一笔手写路径*/
    private Path mCurrentPath;
    /*每一笔路径的对象*/
    private GraffitiPath mGraffitiPath;
    /*保存涂鸦操作，撤销使用*/
    private List<GraffitiPath> mPathList = new CopyOnWriteArrayList<GraffitiPath>();
    /*涂鸦后的图片*/
    private Bitmap mGraffitiBitmap;
    /*用于绘制涂鸦后的图片*/
    private Canvas mBitmapCanvas;
    /*图片保存成功以后的回调*/
    private GraffitiListener mGraffitiListener;

    /*是否初始化绘制完成*/
    private boolean isInitDrawFinish = false;


    public GraffitiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCurrentStatus = STATUS_INIT;
        //关闭硬件加速
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }

    /**
     * 将待展示的图片设置进来。
     *
     * @param bitmap 待展示的Bitmap对象
     */
    public void setBitmap(Bitmap bitmap, int bitmapStaus, GraffitiListener graffitiListener) {
        mSourceBitmap = bitmap;
        if (mSourceBitmap == null) {
            throw new RuntimeException("bitmap is null");
        }
        mBimapStatus = bitmapStaus;
        if (mBimapStatus == STATUS_EDIT) {
            initPaint();
            initCanvas();
        }
        this.mGraffitiListener = graffitiListener;
        invalidate();
    }


    /**
     * @Description:初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        //画笔宽度：5f
        mPaint.setStrokeWidth(5f);
        //画笔颜色：红色
        mPaint.setColor(Color.RED);
        //画笔样式:线
        mPaint.setStyle(Paint.Style.STROKE);
        //消除锯齿
        mPaint.setAntiAlias(true);
        //设置笔刷的样式：圆
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    private void initCanvas() {
        if (mGraffitiBitmap != null) {
            mGraffitiBitmap.recycle();
        }
        mGraffitiBitmap = mSourceBitmap.copy(Bitmap.Config.RGB_565, true);
        mBitmapCanvas = new Canvas(mGraffitiBitmap);
    }

    //每次缩放倍数
    private float mScaledRatio;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mCurrentStatus == STATUS_CHANGE) {
                // 每次缩放倍数
                mScaledRatio = detector.getScaleFactor();
                mTotalScale *= mScaledRatio;
                // 控制图片的缩放范围
                mTotalScale = Math.max(mInitScale, Math.min(mTotalScale, mInitScale * 4));
                //mScaledRatio是用来在图片缩放时，计算位移的，如果图片没有缩放，mScaledRatio始终为1，避免错误计算。
                if (mTotalScale == mInitScale * 4) {
                    mScaledRatio = 1;
                }
            }
            return true;
        }
    }

    /*用于贝塞尔曲线绘制*/
    private float mDrawlastX;
    private float mDrawlastY;
    private float mTouchX;
    private float mTouchY;

    /*用于图片移动*/
    private int mLastPointerCount;
    private float mMoveLastX;
    private float mMoveLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInitDrawFinish) {
            return true;
        }
        mScaleDetector.onTouchEvent(event);
        float xTranslate = 0, yTranslate = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            xTranslate += event.getX(i);
            yTranslate += event.getY(i);
        }
        xTranslate = xTranslate / pointerCount;
        yTranslate = yTranslate / pointerCount;
        /**
         * 每当触摸点发生变化时，重置mLasX , mMoveLastY
         */
        if (pointerCount != mLastPointerCount) {
            mMoveLastX = xTranslate;
            mMoveLastY = yTranslate;
        }
        mLastPointerCount = pointerCount;

        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mBimapStatus == STATUS_EDIT) {
                    mGraffitiPath = new GraffitiPath();
                    if (x >= mBitmapLeftTopX && x <= mBitmapRightTopX && y >= mBitmapLeftTopY
                            && y < mBitmapLetfBottomY) {
                        mTouchX = mDrawlastX = x;
                        mTouchY = mDrawlastY = y;
                        mCurrentPath = new Path();
                        mCurrentPath.moveTo(transformX(event.getX()), transformY(event.getY()));
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //单指触摸时，如果时编辑模式，是画笔，如果是预览模式则是移动
                if (event.getPointerCount() == 1) {
                    if (mBimapStatus == STATUS_EDIT) {
                        mCurrentStatus = STATUS_HANDWRITING;
                        if (x >= mBitmapLeftTopX && x <= mBitmapRightTopX && y >= mBitmapLeftTopY
                                && y < mBitmapLetfBottomY) {
                            if (mCurrentPath != null) {
                                mDrawlastX = mTouchX;
                                mDrawlastY = mTouchY;
                                mTouchX = event.getX();
                                mTouchY = event.getY();
                                //贝塞尔曲线绘制:控制点为上一次touch位置，结束点为移动距离的一半。
                                mCurrentPath.quadTo(transformX(mDrawlastX), transformY(mDrawlastY),
                                        (transformX(mDrawlastX) + transformX(mTouchX)) / 2, (transformY
                                                (mDrawlastY) + transformY(mTouchY)) / 2);
                                mGraffitiPath.pathData.append(transformX(x) + ",");
                                mGraffitiPath.pathData.append(transformY(y) + ",");
                                mGraffitiPath.pathData.append(";");
                            }
                        }
                    } else if (mBimapStatus == STATUS_PREVIEW) {
                        mCurrentStatus = STATUS_CHANGE;
                    }
                }
                //双指为移动
                if (event.getPointerCount() == 2) {
                    centerPointBetweenFingers(event);
                    mCurrentStatus = STATUS_CHANGE;
                    mCurrentPath = null;
                    mGraffitiPath = null;

                }
                if (mCurrentStatus == STATUS_CHANGE) {
                    float dX = xTranslate - mMoveLastX;
                    float dY = yTranslate - mMoveLastY;
                    //缩放后的图片宽度大于控件宽度时
                    if (mCurrentBitmapwWidth > mWidth) {
                        //只有在图片可左右移动时，增加x
                        if ((dX > 0 && mBitmapLeftTopX < 0) || (dX < 0 && mBitmapRightTopX >
                                mWidth)) {
                            mFocusX = dX;
                        }
                    }
                    //缩放后的图片高度大于控件宽度时
                    if (mCurrentBitmapHeight > mHeight) {
                        //只有在图片可上下移动时，增加y
                        if ((dY > 0 && mBitmapLeftTopY < 0) || (dY < 0 && mBitmapLetfBottomY >
                                mHeight)) {
                            mFocusY = dY;
                        }
                    }
                    mMoveLastX = xTranslate;
                    mMoveLastY = yTranslate;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                if (mBimapStatus == STATUS_EDIT && mCurrentStatus == STATUS_HANDWRITING) {
                    //保存路径到集合中，撤销使用
                    if (mGraffitiPath != null && mGraffitiPath.pathData != null) {
                        int len = mGraffitiPath.pathData.length();
                        if (len > 0) {
                            mGraffitiPath.path = mCurrentPath;
                            mGraffitiPath.pathData.delete(len - 1, len);
                            mGraffitiPath.pathData.append("&");
                            mBitmapCanvas.drawPath(mCurrentPath, mPaint);
                            mPathList.add(mGraffitiPath);
                        }
                    }
                }
                break;
            default:
                break;

        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        switch (mCurrentStatus) {
            case STATUS_INIT:
                initBitmap(canvas);
                break;
            case STATUS_CHANGE:
                change(canvas);
                if (mBimapStatus == STATUS_EDIT) {
                    handWriting(canvas, STATUS_HANDWRITING);
                }
                break;
            case STATUS_HANDWRITING:
                handWriting(canvas, STATUS_HANDWRITING);
                break;
            case STATUS_CLEAR:
                handWriting(canvas, STATUS_CLEAR);
                break;
            case STATUS_UNDO:
                handWriting(canvas, STATUS_UNDO);
                break;
        }
        canvas.restore();

    }

    //避免float精度损失引起误差
    float tatalScale = .0f;

    /**
     * @Description:对图片进行缩放和移动。
     */
    private void change(Canvas canvas) {
        mMatrix.reset();
        // 将图片按总缩放比例进行缩放
        mMatrix.postScale(mTotalScale, mTotalScale);
        //图片变化后的宽度
        float scaledWidth = mSourceBitmap.getWidth() * mTotalScale;
        //图片变化后的高度
        float scaledHeight = mSourceBitmap.getHeight() * mTotalScale;
        //当前图片的宽度
        mCurrentBitmapwWidth = scaledWidth;
        //当前图片的高度
        mCurrentBitmapHeight = scaledHeight;
        // 缩放后对图片进行偏移，以保证缩放后中心点位置不变
        float translateX;
        float translateY;
        //缩放后的图片宽度小于的控件的宽度时，x基于控件中心缩放
        if (scaledWidth < mWidth) {
            translateX = (mWidth - scaledWidth) / 2f;
        } else {
            //推到过程：假设被放大的图片是一个矩形，左上角坐标为x0,y0,基点为x1,y1,图形被放大的倍数为q，求放大后的左上角坐标为x2,y2,现在我们要求这个x2,y2。根据图形可以得出公式：
            // [(x0 - x2) + (x1 - x0)] / (x1 -x0) = q，然后就可以求出坐标x2的值，同理可以求出y2。x2和y2即图片需要移动的距离。
            translateX = mTotalTranslateX * mScaledRatio + mCenterPointX * (1 - mScaledRatio) + mFocusX;
            //避免float的精度损失引起误差
            if (tatalScale == mTotalScale) {
                translateX = mTotalTranslateX + mFocusX;
            }
            // 进行边界检查，不允许将图片拖出边界
            if (translateX > 0) {
                //x方向上,左边界检查
                translateX = 0;
            } else if (scaledWidth - mWidth < Math.abs(translateX)) {
                //x方向上,右边界检查
                translateX = mWidth - scaledWidth;
            }

        }
        //缩放后的图片高度小于控件的高度时，y基于控件中心缩放
        if (scaledHeight < mHeight) {
            translateY = (mHeight - scaledHeight) / 2f;
        } else {
            translateY = mTotalTranslateY * mScaledRatio + mCenterPointY * (1 - mScaledRatio) + mFocusY;
            //避免float的精度损失引起误差
            if (tatalScale == mTotalScale) {
                translateY = mTotalTranslateY + mFocusY;
            }
            // 进行边界检查，不允许将图片拖出边界
            if (translateY > 0) {
                //y方向上，上边界检查
                translateY = 0;
            } else if (scaledHeight - mHeight < Math.abs(translateY)) {
                //y方向上，下边界检查
                translateY = mHeight - scaledHeight;
            }

        }
        mMatrix.postTranslate(translateX, translateY);
        mTotalTranslateX = translateX;
        mTotalTranslateY = translateY;
        //避免float精度损失引起误差
        tatalScale = mTotalScale;
        //绘制
        canvas.drawBitmap(mSourceBitmap, mMatrix, null);
        //截取
        canvas.clipRect(mTotalTranslateX, mTotalTranslateY, mTotalTranslateX +
                mCurrentBitmapwWidth, mTotalTranslateY + mCurrentBitmapHeight);
        computeBoundry(mTotalScale, mTotalTranslateX, mTotalTranslateY);

    }

    /**
     * @Description:初始化预览图，居中显示。
     */
    private void initBitmap(Canvas canvas) {
        if (mSourceBitmap != null) {
            //重置当前Matrix(将当前Matrix重置为单位矩阵)
            mMatrix.reset();
            //获取图片实际宽高
            int bitmapWidth = mSourceBitmap.getWidth();
            int bitmapHeight = mSourceBitmap.getHeight();
            // mWidth为控件宽，产品要求：将图片宽度充满，高度等比缩放。
            float ratio = mWidth / (bitmapWidth * 1.0f);
            mMatrix.postScale(ratio, ratio);
            // mHeight为控件高,在纵坐标方向上进行偏移，以保证图片居中显示
            float translateY = (mHeight - (bitmapHeight * ratio)) / 2f;
            mMatrix.postTranslate(0, translateY);
            //记录图片在矩阵上的纵向偏移值
            mTotalTranslateY = translateY;
            //记录图片在矩阵上的总缩放比例
            mTotalScale = mInitScale = ratio;
            //当前图片的宽
            mCurrentBitmapwWidth = bitmapWidth * mInitScale;
            //当前图片的高
            mCurrentBitmapHeight = bitmapHeight * mInitScale;
            //绘制图片
            canvas.drawBitmap(mSourceBitmap, mMatrix, null);
            //计算图片的四个顶点坐标,以便涂鸦时候的边界判断.
            computeBoundry(mTotalScale, mTotalTranslateX, mTotalTranslateY);
            isInitDrawFinish = true;
            if (mDrawFinish != null) {
                mDrawFinish.onDrawFinish();
            }
        }
    }


    /**
     * @Descripiton:手绘
     */
    private void handWriting(Canvas canvas, int Type) {
        mMatrix.reset();
        // 将图片按总缩放比例进行缩放
        mMatrix.postScale(mTotalScale, mTotalScale);
        // 缩放后对图片进行偏移，以保证缩放后中心点位置不变
        mMatrix.postTranslate(mTotalTranslateX, mTotalTranslateY);
        canvas.drawBitmap(mSourceBitmap, mMatrix, null);
        canvas.clipRect(mTotalTranslateX, mTotalTranslateY, mTotalTranslateX +
                mCurrentBitmapwWidth, mTotalTranslateY + mCurrentBitmapHeight);
        //给canvas必须设置matrix,不然canvas会按初始化的方式去绘制
        canvas.setMatrix(mMatrix);
        //线保留之前的画笔路径
        switch (Type) {
            case STATUS_HANDWRITING:
                for (GraffitiPath path : mPathList) {
                    canvas.drawPath(path.path, mPaint);
                }
                if (mCurrentPath != null) {
                    canvas.drawPath(mCurrentPath, mPaint);
                }
                break;
            case STATUS_UNDO:
                for (GraffitiPath path : mPathList) {
                    canvas.drawPath(path.path, mPaint);
                }
                break;
            case STATUS_CLEAR:
                //清空的时候就不再绘制了
                break;
            default:
                break;


        }
    }


    /**
     * @Descripiton:将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float transformX(float mTouchX) {
        return (mTouchX - mTotalTranslateX) / mTotalScale;
    }

    /**
     * @Descripiton:将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float transformY(float mTouchY) {
        return (mTouchY - mTotalTranslateY) / mTotalScale;
    }

    public static class GraffitiPath {
        public Path path;
        public StringBuilder pathData = new StringBuilder();

        @Override
        public String toString() {
            return "GraffitiPath{" +
                    "path=" + path +
                    ", pathData=" + pathData +
                    '}';
        }


    }

    /**
     * 记录两指同时放在屏幕上时，中心点的横坐标值
     */
    private float mCenterPointX;

    /**
     * 记录两指同时放在屏幕上时，中心点的纵坐标值
     */
    private float mCenterPointY;

    /**
     * 计算两个手指之间中心点的坐标。
     *
     * @param event
     */
    private void centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        mCenterPointX = (xPoint0 + xPoint1) / 2;
        mCenterPointY = (yPoint0 + yPoint1) / 2;

    }

    /**
     * @param totalScale      图片缩放倍数
     * @param totalTranslateX 图片在矩阵上的横向偏移值
     * @param totalTranslateY 图片在矩阵上的纵向偏移值
     * @Description:计算顶点坐标
     */
    private void computeBoundry(float totalScale, float totalTranslateX,
                                float totalTranslateY) {
        mBitmapLeftTopX = 0.f * totalScale;
        mBitmapLeftTopY = 0.f * totalScale;
        mBitmapLeftTopX = mBitmapLeftTopX + totalTranslateX;
        mBitmapLeftTopY = mBitmapLeftTopY + totalTranslateY;
        mBitmapRightTopX = mBitmapLeftTopX + mCurrentBitmapwWidth;
        mBitmapLetfBottomY = mBitmapLeftTopY + mCurrentBitmapHeight;

    }

    /*==========================API==========================*/
    /*==========================API==========================*/
    /*==========================API==========================*/

    /**
     * @Description:清空涂鸦
     */
    public void clear() {
        mCurrentStatus = STATUS_CLEAR;
        mCurrentPath = null;
        mPathList.clear();
        invalidate();
        initCanvas();
    }


    /**
     * @Description:撤销涂鸦
     */
    public void undo() {
        if (mPathList != null && mPathList.size() > 0) {
            mPathList.remove(mPathList.size() - 1);
            mCurrentStatus = STATUS_UNDO;
            invalidate();
            initCanvas();
            for (GraffitiPath path : mPathList) {
                mBitmapCanvas.drawPath(path.path, mPaint);
            }

        }
    }

    /**
     * @Description:恢复涂鸦
     */
    public void restorePath(List<GraffitiPath> graffitiPathList) {
        mPathList.addAll(graffitiPathList);
        mCurrentStatus = STATUS_UNDO;
        invalidate();
        initCanvas();
        for (GraffitiPath path : mPathList) {
            mBitmapCanvas.drawPath(path.path, mPaint);
        }

    }

    /**
     * @description:获取涂鸦路径
     */
    public List<GraffitiPath> getGraffitiData() {
        return mPathList;
    }

    /**
     * @description:保存图片
     */
    public void save() {
        if (mGraffitiListener != null) {
            mGraffitiListener.onSaved(mGraffitiBitmap);
        }

    }

    public interface GraffitiListener {
        void onSaved(Bitmap bitmap);
    }
    
    private DrawFinish mDrawFinish;

    public interface DrawFinish {
        void onDrawFinish();
    }

    public void setDrawFinish(DrawFinish drawFinish) {
        this.mDrawFinish = drawFinish;

    }

}
