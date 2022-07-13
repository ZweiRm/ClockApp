package xin.ahza.clockapp.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import xin.ahza.clockapp.MainActivity;
import xin.ahza.clockapp.view.ClockViewWithHandler;

public class FloatingWindowService extends Service {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private ClockViewWithHandler mHandlerClockView;

    private int mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY; // 开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
    private int mStartX, mStartY, mStopX, mStopY;   // 开始时的坐标和结束时的坐标（相对于自身控件的坐标）
    private boolean isMove; // 判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initFloating();
        initWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initFloating() {
        mHandlerClockView = new ClockViewWithHandler(this);
        mHandlerClockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FloatingWindowService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                mWindowManager.removeView(mHandlerClockView);
            }
        });
        mHandlerClockView.setOnTouchListener(new FloatingListener());
    }

    private void initWindow() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = 500;
        mLayoutParams.height = 500;
        mLayoutParams.gravity = Gravity.TOP | Gravity.START;
        mLayoutParams.x = 0;
        mLayoutParams.y = 300;

        mWindowManager.addView(mHandlerClockView, mLayoutParams);
    }

    private class FloatingListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isMove = false;
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrentX = (int) event.getRawX();
                    mTouchCurrentY = (int) event.getRawY();

                    mLayoutParams.x += mTouchCurrentX - mTouchStartX;
                    mLayoutParams.y += mTouchCurrentY - mTouchStartY;
                    mWindowManager.updateViewLayout(mHandlerClockView, mLayoutParams);

                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    break;
                case MotionEvent.ACTION_UP:
                    mStopX = (int) event.getX();
                    mStopY = (int) event.getY();

                    if (Math.abs(mStartX - mStopX) >= 1 || Math.abs(mStartY - mStopY) >= 1) {
                        isMove = true;
                    }
                    break;
                default:
                    break;
            }

            // 如果是移动事件不触发OnClick事件，防止移动的时候一放手形成点击事件
            return isMove;
        }
    }
}