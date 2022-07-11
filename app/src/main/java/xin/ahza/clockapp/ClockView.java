package xin.ahza.clockapp;

import static android.graphics.Paint.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SumPathEffect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ClockView extends View {
    private Timer mTimer;

    private final Paint mCirclePaint;
    private final Paint mPointPaint;
    private final Paint mTextPaint;

    private final Path mCirclePath;
    private final Path mHourPath;
    private final Path mMinutePath;
    private final Path mSecondPath;

    private PathMeasure mPathMeasure;
    private SumPathEffect mSumPathEffect;

    private float mViewWidth = 0;
    private float mViewHeight = 0;
    private final float mCircleWidth = 6;
    private float mRadius = 0;
    private final float mRectRadius = 20;
    private float mHourDegree = 0;
    private float mMinuteDegree = 0;
    private float mSecondDegree = 0;
    private float mTextDegree = 0;

    private long mCurrentTimeInSecond = 0;

    private String[] text = {"12", "3", "6", "9"};

    public ClockView(Context context) {
        super(context);

        mCirclePaint = new Paint();
        mPointPaint = new Paint();
        mTextPaint = new Paint();

        mCirclePath = new Path();
        mHourPath = new Path();
        mMinutePath = new Path();
        mSecondPath = new Path();

        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCirclePaint = new Paint();
        mPointPaint = new Paint();
        mTextPaint = new Paint();

        mCirclePath = new Path();
        mHourPath = new Path();
        mMinutePath = new Path();
        mSecondPath = new Path();

        init();
    }

    private void init() {
        mCirclePaint.setColor(Color.BLACK);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleWidth);

        mPointPaint.setColor(Color.BLACK);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Style.FILL);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setTextSize(100);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 获取 View 大小
        mViewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mViewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        // 创建圆形 Path
        mRadius = mViewWidth / 2 - mCircleWidth;
        mCirclePath.addCircle(0, 0, mRadius, Path.Direction.CW);

        // 创建刻度
        // addRect 设置每个刻度矩形
        // PathDashPathEffect 设置每隔一定间距按照圆 Path 旋转排布
        // SumPathEffect 合并分钟和小时刻度 Effect
        mPathMeasure = new PathMeasure(mCirclePath, false);
        Path minuteShapePath = new Path();
        Path quarterShapePath = new Path();
        minuteShapePath.addRect(0, 0, mRadius * 0.01F, mRadius * 0.05F, Path.Direction.CW);
        quarterShapePath.addRect(0, 0, mRadius * 0.02F, mRadius * 0.06F, Path.Direction.CW);
        PathDashPathEffect minuteDashPathEffect = new PathDashPathEffect(minuteShapePath, mPathMeasure.getLength() / 60, 0, PathDashPathEffect.Style.ROTATE);
        PathDashPathEffect quarterDashPathEffect = new PathDashPathEffect(quarterShapePath, mPathMeasure.getLength() / 12, 0, PathDashPathEffect.Style.ROTATE);
        mSumPathEffect = new SumPathEffect(minuteDashPathEffect, quarterDashPathEffect);

        // 创建小时指针矩形
        float hourPointerHeight = mRadius * 0.5F;
        float hourPointerWidth = mRadius * 0.06F;
        RectF hourRect = new RectF(-hourPointerWidth / 2, -hourPointerHeight * 0.7F, hourPointerWidth / 2, hourPointerHeight * 0.2F);
        mHourPath.addRoundRect(hourRect, mRectRadius, mRectRadius, Path.Direction.CW);

        // 创建分钟指针矩形
        float minutePointerHeight = mRadius * 0.8F;
        float minutePointerWidth = mRadius * 0.04F;
        RectF minuteRect = new RectF(-minutePointerWidth / 2, -minutePointerHeight * 0.8F, minutePointerWidth / 2, minutePointerHeight * 0.2F);
        mMinutePath.addRoundRect(minuteRect, mRectRadius, mRectRadius, Path.Direction.CW);

        // 创建秒钟指针矩形
        float secondPointerHeight = mRadius * 0.9F;
        float secondPointerWidth = mRadius * 0.02F;
        RectF secondRect = new RectF(-secondPointerWidth / 2, -secondPointerHeight * 0.8F, secondPointerWidth / 2, secondPointerHeight * 0.2F);
        mSecondPath.addRoundRect(secondRect, mRectRadius, mRectRadius, Path.Direction.CW);

        startAnimator();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas != null) {
            // 画布平移到屏幕中心
            canvas.translate(mViewWidth / 2, mViewHeight / 2);

            // 圆盘
            mCirclePaint.setPathEffect(null);
            canvas.drawPath(mCirclePath, mCirclePaint);

            // 刻度
            mCirclePaint.setPathEffect(mSumPathEffect);
            canvas.drawPath(mCirclePath, mCirclePaint);

            // 指针
            mPointPaint.setColor(Color.BLACK);

            canvas.save();
            canvas.rotate(mHourDegree);
            canvas.drawPath(mHourPath, mPointPaint);
            canvas.restore();

            canvas.save();
            canvas.rotate(mMinuteDegree);
            canvas.drawPath(mMinutePath, mPointPaint);
            canvas.restore();

            mPointPaint.setColor(Color.RED);
            canvas.save();
            canvas.rotate(mSecondDegree);
            canvas.drawPath(mSecondPath, mPointPaint);
            canvas.restore();

            // 中心
            mPointPaint.setColor(Color.WHITE);
            canvas.drawCircle(0, 0, mRadius * 0.02F, mPointPaint);

            // 文字
            float textLen = mTextPaint.measureText(text[0]);
            float textRadius = mRadius - 100;
            for (int i = 1; i <= 60; i++) {
                double sin = Math.sin(Math.toRadians(6 * i));
                double cos = Math.cos(Math.toRadians(6 * i));
                if (i % 5 == 0) {
                    float x1 = (float) (sin * textRadius) - textLen * 0.3F;
                    float y1 = - (float) (cos * textRadius) + textLen * 0.3F;
                    canvas.drawText(String.valueOf(i / 5), x1, y1, mTextPaint);
                }
            }
        }
    }

    private void startAnimator() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        mCurrentTimeInSecond = hour * 60 * 60 + minute * 60 + second;

        if (mTimer == null) {
            mTimer = new Timer();
        } else {
            mTimer.cancel();
            mTimerTask.cancel();
        }

        if (mTimer != null) {
            mTimer.schedule(mTimerTask, 0, 1000);
        }
    }

    private final TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            mCurrentTimeInSecond++;
            computeDegree();
            invalidate();
        }
    };

    private void computeDegree() {
        int secondsInOneRoll = 12 * 60 * 60;
        long currentSecond = mCurrentTimeInSecond % secondsInOneRoll;

        long leftSeconds;
        long hours = currentSecond / 60 / 60;
        leftSeconds = currentSecond - hours * 60 * 60;
        long minutes = leftSeconds / 60;
        leftSeconds -= minutes * 60;
        long seconds = leftSeconds % 60;

        mHourDegree = hours * 30F;
        mMinuteDegree = minutes * 6F;
        mSecondDegree = seconds * 6F;
    }
}
