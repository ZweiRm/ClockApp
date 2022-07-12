package xin.ahza.clockapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

//
// Created by skylar on 2022/4/19.
//
class TestView : View {
    private var mTimer: Timer? = null
    private val mCirclePaint: Paint = Paint()
    private val mPointerPaint: Paint = Paint()
    private val mTextPaint: Paint = Paint()

    private val mCirclePath: Path = Path()
    private val mHourPath: Path = Path()
    private val mMinutePath: Path = Path()
    private val mSecondPath: Path = Path()

    private lateinit var mPathMeasure: PathMeasure
    private lateinit var mSumPathEffect: SumPathEffect

    private var mViewWidth = 0
    private var mViewHeight = 0
    private var mCircleWidth = 6f
    private var mRadius = 0f
    private var mRectRadius = 20f
    private var mHoursDegree = 0f
    private var mMinutesDegree = 0f
    private var mSecondsDegree = 0f
    private var mCurrentTimeInSecond = 0L

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(
            context: Context, attrs: AttributeSet?,
            defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        mCirclePaint.color = Color.BLACK
        mCirclePaint.isAntiAlias = true
        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.strokeWidth = mCircleWidth

        mPointerPaint.color = Color.BLACK
        mPointerPaint.isAntiAlias = true
        mPointerPaint.style = Paint.Style.FILL

        mTextPaint.color = Color.BLACK
        mTextPaint.isAntiAlias = true
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = 40f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = measuredWidth - paddingLeft - paddingRight
        mViewHeight = measuredHeight - paddingTop - paddingBottom

        mRadius = mViewWidth / 2 - mCircleWidth
        mCirclePath.addCircle(0f, 0f, mRadius, Path.Direction.CW)

        mPathMeasure = PathMeasure(mCirclePath, false)
        val minutesShapePath = Path()
        val quarterShapePath = Path()
        minutesShapePath.addRect(0f, 0f, mRadius * 0.01f, mRadius * 0.06f, Path.Direction.CW)
        quarterShapePath.addRect(0f, 0f, mRadius * 0.02f, mRadius * 0.06f, Path.Direction.CW)
        val minutesDashPathEffect = PathDashPathEffect(
                minutesShapePath,
                mPathMeasure.length / 60,
                0f,
                PathDashPathEffect.Style.ROTATE
        )
        val quarterDashPathEffect = PathDashPathEffect(
                quarterShapePath,
                mPathMeasure.length / 12,
                0f,
                PathDashPathEffect.Style.ROTATE
        )
        mSumPathEffect = SumPathEffect(minutesDashPathEffect, quarterDashPathEffect)

        val hourPointerHeight = mRadius * 0.5f
        val hourPointerWidth = mRadius * 0.07f
        val hourRect = RectF(
                -hourPointerWidth / 2,
                -hourPointerHeight * 0.7f,
                hourPointerWidth / 2,
                hourPointerHeight * 0.3f
        )
        mHourPath.addRoundRect(hourRect, mRectRadius, mRectRadius, Path.Direction.CW)

        val minutePointerHeight = mRadius * 0.7f
        val minutePointerWidth = mRadius * 0.05f
        val minuteRect = RectF(
                -minutePointerWidth / 2,
                -minutePointerHeight * 0.8f,
                minutePointerWidth / 2,
                minutePointerHeight * 0.2f
        )
        mMinutePath.addRoundRect(minuteRect, mRectRadius, mRectRadius, Path.Direction.CW)

        val secondPointerHeight = mRadius * 0.9f
        val secondPointerWidth = mRadius * 0.03f
        val secondRect = RectF(
                -secondPointerWidth / 2,
                -secondPointerHeight * 0.8f,
                secondPointerWidth / 2,
                secondPointerHeight * 0.2f
        )
        mSecondPath.addRoundRect(secondRect, mRectRadius, mRectRadius, Path.Direction.CW)

        startAnimator()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }
        canvas.translate((mViewWidth / 2).toFloat(), (mViewHeight / 2).toFloat())

        //画圆盘
        mCirclePaint.pathEffect = null
        canvas.drawPath(mCirclePath, mCirclePaint)

        //画刻度
        mCirclePaint.pathEffect = mSumPathEffect
        canvas.drawPath(mCirclePath, mCirclePaint)

        //时分秒指针
        mPointerPaint.color = Color.BLACK

        canvas.save()
        canvas.rotate(mHoursDegree)
        canvas.drawPath(mHourPath, mPointerPaint)
        canvas.restore()

        canvas.save()
        canvas.rotate(mMinutesDegree)
        canvas.drawPath(mMinutePath, mPointerPaint)
        canvas.restore()

        canvas.save()
        canvas.rotate(mSecondsDegree)
        canvas.drawPath(mSecondPath, mPointerPaint)
        canvas.restore()

        //画中心点
        mPointerPaint.color = Color.WHITE
        canvas.drawCircle(0f, 0f, mRadius * 0.02f, mPointerPaint)
    }


    private fun startAnimator() {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)  //小时
        val minute = cal.get(Calendar.MINUTE)  //分
        val second = cal.get(Calendar.SECOND)  //秒
        mCurrentTimeInSecond = (hour * 60 * 60 + minute * 60 + second).toLong()

        if (mTimer == null) {
            mTimer = Timer()
        } else {
            mTimer?.cancel()
            mTimerTask.cancel()
        }
        mTimer?.schedule(mTimerTask, 0, 1000)
    }

    private var mTimerTask: TimerTask = object : TimerTask() {
        override fun run() {
            mCurrentTimeInSecond++
            computeDegree()
            invalidate()
        }
    }

    //12小时 00:00:00 ~ 12:00:00
    private fun computeDegree() {
        val secondsInOneRoll = 12 * 60 * 60
        val currentSeconds = mCurrentTimeInSecond % secondsInOneRoll

        var leftSeconds = currentSeconds
        val hours = currentSeconds / 60 / 60
        leftSeconds = currentSeconds - hours * 60 * 60
        val minutes = leftSeconds / 60
        leftSeconds -= minutes * 60
        val seconds = leftSeconds % 60

        mHoursDegree = hours * 30f
        mMinutesDegree = minutes * 6f
        mSecondsDegree = seconds * 6f

    }
}

