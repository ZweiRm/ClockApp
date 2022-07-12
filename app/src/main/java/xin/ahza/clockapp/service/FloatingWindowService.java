package xin.ahza.clockapp.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

import xin.ahza.clockapp.view.ClockViewWithHandler;

public class FloatingWindowService extends Service {
    private static final String TAG = "FloatingWindowService";

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private ClockViewWithHandler mHandlerClockView;
    private boolean flag = true;

    @Override
    public void onCreate() {
        super.onCreate();

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

        mHandlerClockView = new ClockViewWithHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (flag) {
            flag = false;
            mWindowManager.addView(mHandlerClockView, mLayoutParams);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandlerClockView.getParent() != null) {
            mWindowManager.removeView(mHandlerClockView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}