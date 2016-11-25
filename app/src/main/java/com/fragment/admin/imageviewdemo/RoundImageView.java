package com.fragment.admin.imageviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * Created by admin on 2016/11/11.
 */
public class RoundImageView extends ImageView {
    private static final String TAG = "RoundImageView";
    private static final int cricleType = 0;
    private static final int roundType = 1;
    private int typeDfault;
    private int type;
    private int radious = 0;//圆角的大小，单位应该是dp
    private int borderWidth = 0;//边框的宽度
    private int borderColor;
    private int borderDefaultColor;

    private Paint mPaint;
    private Paint borderPaint;
    private RectF mRect;
    private RectF borderRect;

    private int mWidth;

    private BitmapShader mBitmapShader;
    boolean isFrast = true;

    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    /**
     * 初始化属性
     */
    private void init(AttributeSet attrs) {
        Log.e(TAG, "init");
        //默认的类型为roundType
        typeDfault = roundType;
        //设置borderColor默认为透明色
        borderDefaultColor = Color.parseColor("#00000000");
        //获取自定义属性
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        type = array.getInt(R.styleable.RoundImageView_type, typeDfault);
        radious = array.getDimensionPixelSize(R.styleable.RoundImageView_radius, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        0, getResources()
                                .getDisplayMetrics()));
        borderWidth = array.getDimensionPixelSize(R.styleable.RoundImageView_borderWidth, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        0, getResources()
                                .getDisplayMetrics()));//边框宽度
        borderColor = array.getColor(R.styleable.RoundImageView_borderColor, borderDefaultColor);
        array.recycle();
        mPaint = new Paint();
        mMatrix = new Matrix();
        mPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure");
        if (!isFrast) {
            return;
        }
        isFrast = false;
        int widthSize = getMeasuredWidth() +borderWidth*2 ;
        int heightSize = getMeasuredHeight() +borderWidth*2 ;
        Log.e(TAG, "widthSize:" + widthSize);
        Log.e(TAG, "heightSize:" + heightSize);

        Log.e(TAG, "getMeasuredWidth():" + getMeasuredWidth());
        Log.e(TAG, "getMeasuredHeight():" + getMeasuredHeight());
        if (type == cricleType) {
            //如果当前的type是cricleType，则要求宽高必须相同
            mWidth = Math.min(widthSize, heightSize);//判断宽高哪个更小一点
            radious = mWidth / 2;//当图片为圆形的时候radius必然等于mWidth的一半
            setMeasuredDimension(mWidth, mWidth);
        }
        else
        {
            setMeasuredDimension(widthSize, heightSize);
        }

    }

    /**
     * 设置BitmapShader
     */
    private void setBitmapShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        Bitmap bitmap = DrawableToBitmap(drawable);
        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);// Shader.TileMode.CLAMP表示拉伸

        //控制缩放比
        float scale = 1.0f;
        if (type == cricleType) {
            // 拿到bitmap宽或高的小值
            int bSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
            scale = mWidth * 1.0f / bSize;

        } else if (type == roundType) {
            // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
            scale = Math.max(getWidth() * 1.0f / bitmap.getWidth(), getHeight()
                    * 1.0f / bitmap.getHeight());
        }
        // shader的变换矩阵，我们这里主要用于放大或者缩小
        mMatrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        //给Paint设置Shader
        mPaint.setShader(mBitmapShader);
    }

    /**
     * 将Drawable转化为Bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap DrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            //如过drawable是BitmapDrawDrawable的子类的话
            BitmapDrawable bmpD = (BitmapDrawable) drawable;
            return bmpD.getBitmap();
        }
        int drawableHeight = drawable.getIntrinsicHeight();//获取drawable的高度
        int drawableWidth = drawable.getIntrinsicWidth();//获取drawable的宽度
        //构建Bitmap
        Bitmap bmp = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, drawableHeight, drawableWidth);
        drawable.draw(canvas);
        return bmp;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 圆角图片的范围
        if (type == roundType)
        {
            mRect = new RectF(0,0, getWidth()+borderWidth, getHeight()+borderWidth);
//            borderRect = new RectF(borderWidth/2, borderWidth/2, getWidth()+ borderWidth/2 , getHeight()+ borderWidth/2);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "onDraw");
        if (getDrawable() == null) {
            return;
        }
        setBitmapShader();
        if (type == roundType) {

//            canvas.drawRoundRect(borderRect, radious, radious, borderPaint);
            canvas.drawRoundRect(mRect, radious, radious, mPaint);
        } else {
            canvas.drawCircle(radious, radious, radious, mPaint);
        }
    }

    /**
     * 将dp转化为px的方法
     */
    private int dpTopx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
