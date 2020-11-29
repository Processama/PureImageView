package com.example.pureimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PureImageView extends androidx.appcompat.widget.AppCompatImageView {
    /** 默认图片类型 */
    public static final int NORMAL_IMG = 0;
    /** 圆形图片类型 */
    public static final int CIRCLE_IMG = 1;
    /** 带圆角图片类型 */
    public static final int CORNER_IMG = 2;

    // ImageView的指定类型
    private final int mType;
    // 圆角(单位dp通过getDimension会乘以density)
    private final float mCornerRadius;
    // 边框宽度（单位dp通过getDimension会乘以density）
    private final float mBorderWidth;
    // 两倍边框宽度
    private final float mDoubleBorderWidth;
    // 一半边框宽度
    private final float mHalfBorderWidth;
    // 边框颜色
    private final int mBorderColor;

    // 画圆及圆角矩形的画笔
    private final Paint mPaintImage;
    // 图片着色器，在画布左上角开始绘制
    BitmapShader mImgBitmapShader;
    // 画边框的画笔
    private final Paint mPaintBorder;

    // 画圆角边框的矩形
    RectF mRectBorder;
    // 画圆角图片的矩形
    RectF mRectBitmap;

    public PureImageView(@NonNull Context context) {
        this(context, null);
    }

    public PureImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PureImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 自定义属性初始化
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PureImageView,
                defStyleAttr, 0);
        mType = typedArray.getInt(R.styleable.PureImageView_imgType, NORMAL_IMG);
        mCornerRadius = typedArray.getDimension(R.styleable.PureImageView_cornerRadius, 0f);
        mBorderWidth = typedArray.getDimension(R.styleable.PureImageView_borderWidth, 0f);
        mDoubleBorderWidth = mBorderWidth * 2;
        mHalfBorderWidth = mBorderWidth / 2;
        mBorderColor = typedArray.getColor(R.styleable.PureImageView_borderColor, Color.WHITE);
        // 回收
        typedArray.recycle();

        // 画笔初始化
        mPaintImage = new Paint();
        mPaintImage.setAntiAlias(true);
        mPaintBorder = new Paint();
        mPaintBorder.setAntiAlias(true);

        // 圆角矩形初始化
        mRectBorder = new RectF();
        mRectBitmap = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 为圆形ImageView时，宽高为两者最小值，形成正方形
        if (mType == CIRCLE_IMG) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int viewSize = Math.min(width, height);
            setMeasuredDimension(viewSize, viewSize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 默认ImageView走父类onDraw
        if (mType == NORMAL_IMG) {
            super.onDraw(canvas);
        } else {
            // 根据图片获取drawable并转成bitmap
            Bitmap imgBitmap = drawableToBitmap(getDrawable());
            // 为空直接return
            if (imgBitmap == null) {
                return;
            }
            // 获取View内容宽度
            int viewWidth = getWidth();
            // 获取View内容高度
            int viewHeight = getHeight();
            // 获取宽高中较小的值作为正方形边长
            int viewSize = Math.min(viewWidth, viewHeight);
            // 对于圆形或圆角需要重新缩放Bitmap
            Bitmap resizeImgBitmap = null;
            // 圆形缩放Bitmap
            if (mType == CIRCLE_IMG) {
                resizeImgBitmap = resizeBitmapForCircle(imgBitmap, viewSize);
            } else if (mType == CORNER_IMG) {
                // 圆角型缩放Bitmap
                resizeImgBitmap = resizeBitmapForCorner(imgBitmap, viewWidth, viewHeight);
            }
            if (resizeImgBitmap == null) {
                return;
            }
            // 缩放后宽高
            int imgWidth = resizeImgBitmap.getWidth();
            int imgHeight = resizeImgBitmap.getHeight();
            // 通过其他参数及缩放后Bitmap初始化画笔
            initPaint(resizeImgBitmap);
            // 圆形ImageView
            if (mType == CIRCLE_IMG) {
                // 画边框（实际上是一个实心圆，被内容覆盖）
                canvas.drawCircle((float) viewSize / 2, (float) viewSize / 2,
                        (float) (viewSize / 2 - mHalfBorderWidth), mPaintBorder);
                // 偏移边框宽度
                canvas.translate(mBorderWidth, mBorderWidth);
                // 画内容，因为BitmapShader是在画布左上角绘制的，如果不偏移，直接在中心画，
                // 得到的是部分内容及其延申（BitmapShader指定Clamp类型），偏移后，指定好
                // 圆心位置及半径取得的是全部内容
                canvas.drawCircle((float) imgWidth / 2, (float) imgWidth / 2,
                        (float)imgWidth / 2, mPaintImage);
            } else if (mType == CORNER_IMG) {
                // 边框及内容矩形并处理padding
                mRectBorder.set(mHalfBorderWidth + getPaddingLeft(),
                        mHalfBorderWidth + getPaddingTop(),
                        viewWidth - mHalfBorderWidth - getPaddingRight(),
                        viewHeight - mHalfBorderWidth - getPaddingBottom());
                mRectBitmap.set(getPaddingLeft(), getPaddingTop(),
                        imgWidth - getPaddingRight(), imgHeight - getPaddingBottom());
                // 画边框
                canvas.drawRoundRect(mRectBorder, mCornerRadius, mCornerRadius, mPaintBorder);
                // 偏移边框宽度
                canvas.translate(mBorderWidth, mBorderWidth);
                // 画内容矩形，偏移原因同圆形ImageView
                canvas.drawRoundRect(mRectBitmap, mCornerRadius, mCornerRadius, mPaintImage);
            }
        }
    }

    /**
     * 根据不同类型drawable转成bitmap
     *
     * @param drawable Drawable
     * @return Bitamap
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        // BitmapDrawable类型
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof ColorDrawable) {
            // ColorDrawable类型
            Rect rect = drawable.getBounds();
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;
            int color = ((ColorDrawable) drawable).getColor();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawARGB(Color.alpha(color), Color.red(color),
                    Color.green(color), Color.blue(color));
            return bitmap;
        } else {
            // 其他情况
            return null;
        }
    }

    /**
     * 圆形ImageView的Bitmap缩放，先取得bitmap的正方形bitmap，
     * 对正方形bitmap缩放至ImageView宽或高 - 两倍边框宽度
     *
     * @param bitmap image
     * @param viewSize ImageView的宽或高（正方形）
     * @return bitmap
     */
    private Bitmap resizeBitmapForCircle(Bitmap bitmap, int viewSize) {
        // 获取原bitmap宽高
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        int size = Math.min(imgWidth, imgHeight);
        int x = 0;
        int y = 0;
        // 要在中间取正方形，算出对应的x或y值
        if (imgWidth > imgHeight) {
            // 宽大于高，要取正方形，x值显然如下
            x = (imgWidth - imgHeight) / 2;
        } else {
            // 高大于宽，要取正方形，y值显然如下
            y = (imgHeight - imgWidth) / 2;
        }
        // 生成正方形bitmap
        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
        // 缩放正方形bitmap为ImageView宽或高 - 两倍边框宽的值
        float scale = (viewSize - mDoubleBorderWidth) / size;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(squareBitmap, 0, 0, size, size, matrix, true);
    }

    /**
     * 带圆角的ImageView缩放，直接ImageView宽高比缩放
     *
     * @param bitmap bitmap
     * @param viewWidth ImageView的宽
     * @param viewHeight ImageView的高
     * @return bitmap
     */
    private Bitmap resizeBitmapForCorner(Bitmap bitmap, int viewWidth, int viewHeight) {
        // 将原bitmap宽高缩放至ImageView宽高 - 两倍边框宽度
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        float widthScale = (viewWidth - mDoubleBorderWidth) / imgWidth;
        float heightScale = (viewHeight - mDoubleBorderWidth) / imgHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, heightScale);
        return Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, matrix, true);
    }

    /**
     * 初始化画笔
     *
     * @param resizeImgBitmap 缩放后bitmap
     */
    private void initPaint(Bitmap resizeImgBitmap) {
        // 边框画笔初始化
        mPaintBorder.setColor(mBorderColor);
        mPaintBorder.setStrokeWidth(mBorderWidth);
        mPaintBorder.setStyle(Paint.Style.FILL_AND_STROKE);
        // 内容画笔初始化
        mImgBitmapShader = new BitmapShader(resizeImgBitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        mPaintImage.setShader(mImgBitmapShader);
    }
}
