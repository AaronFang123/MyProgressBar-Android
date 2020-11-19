package com.commodity.myviewdemo.CircleProgressBar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.commodity.myviewdemo.BuildConfig;
import com.commodity.myviewdemo.CircleProgressBar.utils.Constant;
import com.commodity.myviewdemo.CircleProgressBar.utils.MiscUtil;
import com.commodity.myviewdemo.R;


/**
 * Created by AaFaa
 * on 2020/11/19
 * in package com.commodity.lib_common_ui.CircleProgressBar
 * with project tradeplatform
 */
class CircleProgressBar extends View {
    public static final String TAG = CircleProgressBar.class.getSimpleName();
    private Context mContext;

    /**
     *  默认大小
     */
    private int mDefaultSize;
    /**
     *  是否开启抗锯齿
     */
    private boolean antiAlias;

    // 绘制提示

    private TextPaint mHintPaint;
    private CharSequence mHint;
    private int mHintColor;
    private float mHintSize;
    private float mHintOffset;

    // 绘制数值

    private TextPaint mValuePaint;
    private float mValue;
    private float mMaxValue;
    private float mValueOffset;
    private int mPrecision;
    private String mPrecisionFormat;
    private int mValueColor;
    private float mValueSize;

    //绘制单位
    private TextPaint mUnitPaint;
    private CharSequence mUnit;
    private int mUnitColor;
    private float mUnitSize;
    private float mUnitOffset;

    // 绘制圆弧

    private Paint mArcPaint;
    private float mArcWidth;
    private float mStartAngle, mSweepAngle;
    private RectF mRectF;

    /* 颜色渐变 */

    private SweepGradient mSweepGradient;
    private int[] mGradientColors = new int[]{Color.GREEN, Color.YELLOW, Color.RED};

    /**
     * 当前进度
     */
    private float mPercent;
    /**
     * 动画持续时间
     */
    private long mAnimateTime;
    /**
     * 属性动画
     */
    private ValueAnimator mAnimator;

    /* 绘制背景圆弧 */

    private Paint mBgArcPaint;
    /**
     * 背景圆弧颜色
     */
    private int mBgArcColor;

    /**
     * 背景圆弧宽度
     */
    private float mBgArcWidth;

    /* 圆心坐标，半径 */
    private Point mCenterPoint;
    private float mRadius;
    private float mTextOffsetPercentInRadius;

    public CircleProgressBar(Context context) {
        super(context);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultSize = MiscUtil.dipToPx(mContext, Constant.DEFAULT_VALUE);
        mAnimator = new ValueAnimator();
        // 画矩形
        mRectF = new RectF();
        mCenterPoint = new Point();
        initAttrs(attrs);
        initPaint();
        setValue(mValue);
    }

    private void setValue(float value) {
        if (value > mMaxValue) {
            value = mMaxValue;
        }
        float start = mPercent;
        float end = value / mMaxValue;
        startAnimator(start, end, mAnimateTime);
    }

    /**
     * 设置动画效果
     * 我们只需要将初始值和结束值提供给ValueAnimator，
     * 并且告诉它动画所需运行的时长，
     * 那么ValueAnimator就会自动帮我们完成从初始值平滑地过渡到结束值这样的效果。
     *
     * @param start 开始的进度
     * @param end 结束的进度
     * @param animateTime 持续时间
     */
    private void startAnimator(float start, float end, final long animateTime) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animateTime);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mValue = mPercent * mMaxValue;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onAnimationUpdate: percent = " + mPercent
                            + ";currentAngle = " + (mSweepAngle * mPercent)
                            + ";value = " + mValue);
                }
                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mHintPaint = new TextPaint();
        // 设置抗锯齿,会消耗较大资源，绘制图形速度会变慢。
        mHintPaint.setAntiAlias(antiAlias);
        // 设置绘制文字大小
        mHintPaint.setTextSize(mHintSize);
        // 设置画笔颜色
        mHintPaint.setColor(mHintColor);
        // 从中间向两边绘制，不需要再次计算文字
        mHintPaint.setTextAlign(Paint.Align.CENTER);

        mValuePaint = new TextPaint();
        mValuePaint.setAntiAlias(antiAlias);
        mValuePaint.setTextSize(mValueSize);
        mValuePaint.setColor(mValueColor);
        // 设置Typeface对象，即字体风格，包括粗体，斜体以及衬线体，非衬线体等
        mValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mValuePaint.setTextAlign(Paint.Align.CENTER);

        mUnitPaint = new TextPaint();
        mUnitPaint.setAntiAlias(antiAlias);
        mUnitPaint.setTextSize(mUnitSize);
        mUnitPaint.setColor(mUnitColor);
        mUnitPaint.setTextAlign(Paint.Align.CENTER);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(antiAlias);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mArcPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔粗细
        mArcPaint.setStrokeWidth(mArcWidth);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND,或方形样式 Cap.SQUARE
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(antiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 初始化参数
     * @param attrs AttributeSet
     */
    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);

        antiAlias = typedArray.getBoolean(R.styleable.CircleProgressBar_antiAlias, Constant.ANTI_ALIAS);

        mHint = typedArray.getString(R.styleable.CircleProgressBar_hint);
        mHintColor = typedArray.getColor(R.styleable.CircleProgressBar_hintColor, Color.BLACK);
        mHintSize = typedArray.getDimension(R.styleable.CircleProgressBar_hintSize, Constant.DEFAULT_HINT_SIZE);

        mValue = typedArray.getFloat(R.styleable.CircleProgressBar_value, Constant.DEFAULT_VALUE);
        mMaxValue = typedArray.getFloat(R.styleable.CircleProgressBar_maxValue, Constant.DEFAULT_MAX_VALUE);
        //内容数值精度格式
        mPrecision = typedArray.getInt(R.styleable.CircleProgressBar_precision, 0);
        mPrecisionFormat = MiscUtil.getPrecisionFormat(mPrecision);
        mValueColor = typedArray.getColor(R.styleable.CircleProgressBar_valueColor, Color.BLACK);
        mValueSize = typedArray.getDimension(R.styleable.CircleProgressBar_valueSize, Constant.DEFAULT_VALUE_SIZE);

        mUnit = typedArray.getString(R.styleable.CircleProgressBar_unit);
        mUnitColor = typedArray.getColor(R.styleable.CircleProgressBar_unitColor, Color.BLACK);
        mUnitSize = typedArray.getDimension(R.styleable.CircleProgressBar_unitSize, Constant.DEFAULT_UNIT_SIZE);

        mArcWidth = typedArray.getDimension(R.styleable.CircleProgressBar_arcWidth, Constant.DEFAULT_ARC_WIDTH);
        mStartAngle = typedArray.getFloat(R.styleable.CircleProgressBar_startAngle, Constant.DEFAULT_START_ANGLE);
        mSweepAngle = typedArray.getFloat(R.styleable.CircleProgressBar_sweepAngle, Constant.DEFAULT_SWEEP_ANGLE);

        mBgArcColor = typedArray.getColor(R.styleable.CircleProgressBar_bgArcColor, Color.WHITE);
        mBgArcWidth = typedArray.getDimension(R.styleable.CircleProgressBar_bgArcWidth, Constant.DEFAULT_ARC_WIDTH);
        mTextOffsetPercentInRadius = typedArray.getFloat(R.styleable.CircleProgressBar_textOffsetPercentInRadius, 0.33f);

        //mPercent = typedArray.getFloat(R.styleable.CircleProgressBar_percent, 0);
        mAnimateTime = typedArray.getInt(R.styleable.CircleProgressBar_animTime, Constant.DEFAULT_ANIM_TIME);

        int gradientArcColors = typedArray.getResourceId(R.styleable.CircleProgressBar_arcColors, 0);
        if (gradientArcColors != 0) {
            try {
                int[] gradientColors = getResources().getIntArray(gradientArcColors);
                if (gradientColors.length == 0) {
                    //如果渐变色为数组为0，则尝试以单色读取色值
                    int color = getResources().getColor(gradientArcColors);
                    mGradientColors = new int[2];
                    mGradientColors[0] = color;
                    mGradientColors[1] = color;
                } else if (gradientColors.length == 1) {
                    //如果渐变数组只有一种颜色，默认设为两种相同颜色
                    mGradientColors = new int[2];
                    mGradientColors[0] = gradientColors[0];
                    mGradientColors[1] = gradientColors[0];
                } else {
                    mGradientColors = gradientColors;
                }
            } catch (Resources.NotFoundException e) {
                throw new Resources.NotFoundException("the give resource not found.");
            }
        }

        typedArray.recycle();
    }

    /**
     * 绘制文字内容
     *
     * @param canvas 画布
     */
    private void drawText(Canvas canvas) {
        // 计算文字宽度，由于Paint已设置为居中绘制，故此处不需要重新计算
        // float textWidth = mValuePaint.measureText(mValue.toString());
        // float x = mCenterPoint.x - textWidth / 2;
        canvas.drawText(String.format(mPrecisionFormat, mValue), mCenterPoint.x, mValueOffset, mValuePaint);

        if (mHint != null) {
            canvas.drawText(mHint.toString(), mCenterPoint.x, mHintOffset, mHintPaint);
        }

        if (mUnit != null) {
            canvas.drawText(mUnit.toString(), mCenterPoint.x, mUnitOffset, mUnitPaint);
        }
    }

    /**
     * 绘制圆弧背景
     *
     * @param canvas 画布
     */
    private void drawArc(Canvas canvas) {
        // 绘制背景圆弧
        // 从进度圆弧结束的地方开始重新绘制，优化性能
        canvas.save();
        float currentAngle = mSweepAngle * mPercent;
        canvas.rotate(mStartAngle, mCenterPoint.x, mCenterPoint.y);
        canvas.drawArc(mRectF, currentAngle, mSweepAngle - currentAngle + 2, false, mBgArcPaint);
        // 第一个参数 oval 为 RectF 类型，即圆弧显示区域
        // startAngle 和 sweepAngle  均为 float 类型，分别表示圆弧起始角度和圆弧度数
        // 3点钟方向为0度，顺时针递增
        // 如果 startAngle < 0 或者 > 360,则相当于 startAngle % 360
        // useCenter:如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形
        canvas.drawArc(mRectF, 2, currentAngle, false, mArcPaint);
        canvas.restore();
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public CharSequence getHint() {
        return mHint;
    }

    public void setHint(CharSequence hint) {
        mHint = hint;
    }

    public CharSequence getUnit() {
        return mUnit;
    }

    public void setUnit(CharSequence unit) {
        mUnit = unit;
    }

    public float getValue() {
        return mValue;
    }


    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue 最大值
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    /**
     * 获取精度
     *
     * @return 精度
     */
    public int getPrecision() {
        return mPrecision;
    }

    public void setPrecision(int precision) {
        mPrecision = precision;
        mPrecisionFormat = MiscUtil.getPrecisionFormat(precision);
    }

    public int[] getGradientColors() {
        return mGradientColors;
    }

    /**
     * 设置渐变
     *
     * @param gradientColors 描述渐变的颜色数组
     */
    public void setGradientColors(int[] gradientColors) {
        mGradientColors = gradientColors;
        updateArcPaint();
    }

    private void updateArcPaint() {
        // 设置渐变
        mSweepGradient = new SweepGradient(mCenterPoint.x, mCenterPoint.y, mGradientColors, null);
        mArcPaint.setShader(mSweepGradient);

    }

    public long getAnimTime() {
        return mAnimateTime;
    }

    public void setAnimTime(long animTime) {
        mAnimateTime = animTime;
    }

    /**
     * 重置
     */
    public void reset() {
        startAnimator(mPercent, 0.0f, 1000L);
    }

    private float getBaselineOffsetFromY(Paint paint) {
        return MiscUtil.measureTextHeight(paint) / 2;
    }


    /* Override Code */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, mDefaultSize),
                MiscUtil.measure(heightMeasureSpec, mDefaultSize));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawArc(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        //求圆弧和背景圆弧的最大宽度
        float maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        //求最小值作为实际值
        int minSize = Math.min(w - getPaddingLeft() - getPaddingRight() - 2 * (int) maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) maxArcWidth);
        //减去圆弧的宽度，否则会造成部分圆弧绘制在外围
        mRadius = minSize / 2;
        //获取圆的相关参数
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        //绘制圆弧的边界
        mRectF.left = mCenterPoint.x - mRadius - maxArcWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - maxArcWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + maxArcWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + maxArcWidth / 2;
        //计算文字绘制时的 baseline
        //由于文字的baseline、descent、ascent等属性只与textSize和typeface有关，所以此时可以直接计算
        //若value、hint、unit由同一个画笔绘制或者需要动态设置文字的大小，则需要在每次更新后再次计算
        mValueOffset = mCenterPoint.y + getBaselineOffsetFromY(mValuePaint);
        mHintOffset = mCenterPoint.y - mRadius * mTextOffsetPercentInRadius + getBaselineOffsetFromY(mHintPaint);
        mUnitOffset = mCenterPoint.y + mRadius * mTextOffsetPercentInRadius + getBaselineOffsetFromY(mUnitPaint);
        updateArcPaint();
        Log.d(TAG, "onSizeChanged: 控件大小 = " + "(" + w + ", " + h + ")"
                + "圆心坐标 = " + mCenterPoint.toString()
                + ";圆半径 = " + mRadius
                + ";圆的外接矩形 = " + mRectF.toString());
    }
}
