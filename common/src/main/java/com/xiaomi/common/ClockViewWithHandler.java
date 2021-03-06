package com.xiaomi.common;

import static android.graphics.Paint.Style;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private final Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            startAnimator();
        }
    };

    public ClockViewWithHandler(Context context) {
        this(context, null);
    }

    public ClockViewWithHandler(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockViewWithHandler(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e("TAG-----------", "onDetachedFromWindow: ");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // ?????? View ??????
        mViewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mViewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        // ???????????? Path
        mRadius = mViewWidth / 2 - mCircleWidth;
        mCirclePath.addCircle(0, 0, mRadius, Path.Direction.CW);

        // ????????????
        // addRect ????????????????????????
        // PathDashPathEffect ????????????????????????????????? Path ????????????
        // SumPathEffect ??????????????????????????? Effect
        mPathMeasure = new PathMeasure(mCirclePath, false);
        Path minuteShapePath = new Path();
        Path quarterShapePath = new Path();
        minuteShapePath.addRect(0, 0, mRadius * 0.01F, mRadius * 0.05F, Path.Direction.CW);
        quarterShapePath.addRect(0, 0, mRadius * 0.02F, mRadius * 0.06F, Path.Direction.CW);
        PathDashPathEffect minuteDashPathEffect = new PathDashPathEffect(minuteShapePath, mPathMeasure.getLength() / 60, 0, PathDashPathEffect.Style.ROTATE);
        PathDashPathEffect quarterDashPathEffect = new PathDashPathEffect(quarterShapePath, mPathMeasure.getLength() / 12, 0, PathDashPathEffect.Style.ROTATE);
        mSumPathEffect = new SumPathEffect(minuteDashPathEffect, quarterDashPathEffect);

        // ????????????????????????
        float hourPointerHeight = mRadius * 0.6F;
        float hourPointerWidth = mRadius * 0.06F;
        RectF hourRect = new RectF(-hourPointerWidth / 2, -hourPointerHeight * 0.7F, hourPointerWidth / 2, hourPointerHeight * 0.2F);
        mHourPath.addRoundRect(hourRect, mRectRadius, mRectRadius, Path.Direction.CW);

        // ????????????????????????
        float minutePointerHeight = mRadius * 0.8F;
        float minutePointerWidth = mRadius * 0.04F;
        RectF minuteRect = new RectF(-minutePointerWidth / 2, -minutePointerHeight * 0.8F, minutePointerWidth / 2, minutePointerHeight * 0.2F);
        mMinutePath.addRoundRect(minuteRect, mRectRadius, mRectRadius, Path.Direction.CW);

        // ????????????????????????
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
            // ???????????????????????????
            canvas.translate(mViewWidth / 2, mViewHeight / 2);

            // ??????
            canvas.drawCircle(0, 0, mRadius, mCircleFillPaint);
            mCirclePaint.setPathEffect(null);
            canvas.drawPath(mCirclePath, mCirclePaint);

            // ??????
            mCirclePaint.setPathEffect(mSumPathEffect);
            canvas.drawPath(mCirclePath, mCirclePaint);

            // ??????
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

            // ??????
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

            // ??????
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

        computeDegree();
        invalidate();

        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    // ??????????????????
    // ????????????????????????????????????????????????????????????????????????????????????
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
