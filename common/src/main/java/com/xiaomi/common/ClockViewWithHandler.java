package com.xiaomi.common;

import static android.graphics.Paint.Style;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SumPathEffect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class ClockViewWithHandler extends View {
    private final Paint mCirclePaint;
    private final Paint mCircleFillPaint;
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

    private long mCurrentTimeInSecond = 0;

    private final TimerHandler mHandler;

    private static final class TimerHandler extends Handler  {
        private WeakReference clockViewWeakReference;
        private TimerHandler(ClockViewWithHandler clockView) {
            clockViewWeakReference = new WeakReference<>(clockView);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            ClockViewWithHandler view = (ClockViewWithHandler) clockViewWeakReference.get();
            if (view != null) {
                view.mCurrentTimeInSecond++;
                view.computeDegree();
                view.invalidate();
                sendEmptyMessageDelayed(1, 1000);
            }
            super.handleMessage(msg);
        }
    }

    public ClockViewWithHandler(Context context) {
        this(context, null);
    }

    public ClockViewWithHandler(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockViewWithHandler(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHandler = new TimerHandler(this);

        mCirclePaint = new Paint();
        mPointPaint = new Paint();
        mTextPaint = new Paint();
        mCircleFillPaint = new Paint();

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

        mCircleFillPaint.setColor(Color.WHITE);
        mCircleFillPaint.setAntiAlias(true);

        mPointPaint.setColor(Color.BLACK);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Style.FILL);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Style.FILL);
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
        float hourPointerHeight = mRadius * 0.6F;
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
            canvas.drawCircle(0, 0, mRadius, mCircleFillPaint);
            mCirclePaint.setPathEffect(null);
            canvas.drawPath(mCirclePath, mCirclePaint);

            // 刻度
            mCirclePaint.setPathEffect(mSumPathEffect);
            canvas.drawPath(mCirclePath, mCirclePaint);

            // 文字
            float textLen = mTextPaint.measureText("12");
            float textRadius = mRadius * 0.8F;

            mTextPaint.setTextSize(mViewWidth * 0.1F);
            for (int i = 1; i <= 60; i++) {
                double sin = Math.sin(Math.toRadians(6 * i));
                double cos = Math.cos(Math.toRadians(6 * i));
                if (i % 5 == 0) {
                    float x1 = (float) (sin * textRadius) - textLen * 0.3F;
                    float y1 = - (float) (cos * textRadius) + textLen * 0.3F;
                    canvas.drawText(String.valueOf(i / 5), x1, y1, mTextPaint);
                }
            }

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
        }
    }

    private void startAnimator() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        mCurrentTimeInSecond = hour * 60 * 60 + minute * 60 + second;

        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    // 计算指针角度
    // 同时计算两套角度，整数角度用于秒针，浮点角度用于时针分针
    private void computeDegree() {
        int secondsInOneRoll = 12 * 60 * 60;
        long currentSecond = mCurrentTimeInSecond % secondsInOneRoll;

        long leftSeconds;
        long leftSecondsL;

        float hours = mCurrentTimeInSecond / 60F / 60F;
        long hoursL = currentSecond / 60 / 60;

        leftSeconds = currentSecond - hoursL * 60 * 60;
        leftSecondsL = leftSeconds;

        float minutes = leftSeconds / 60F;
        long minutesL = leftSecondsL / 60;

        leftSecondsL -= minutesL * 60;
        long seconds = leftSecondsL % 60;

        mHourDegree = hours * 30F;
        mMinuteDegree = minutes * 6F;
        mSecondDegree = seconds * 6F;
    }
}
