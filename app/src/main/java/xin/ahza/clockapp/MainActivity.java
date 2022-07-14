package xin.ahza.clockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import xin.ahza.clockapp.service.FloatingWindowService;
import xin.ahza.clockapp.util.GlobalDialogSingle;

public class MainActivity extends AppCompatActivity {
    private FloatingWindowService.FloatBinder mFloatingServiceBinder;
    private Button mFloatingBtn;
    private Button mRemoveBtn;
    private boolean hasBind = false;

    FloatingServiceConnection mFloatingServiceConnection = new FloatingServiceConnection(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 生成浮窗按钮
        mFloatingBtn = findViewById(R.id.floatingBtn);
        mFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {     // 权限检测
                    new GlobalDialogSingle(MainActivity.this, "", "当前未获取悬浮窗权限", "去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                        }
                    }).show();
                } else {    // 绑定服务
                    Intent intentBind = new Intent(MainActivity.this, FloatingWindowService.class);
                    bindService(intentBind, mFloatingServiceConnection, Context.BIND_AUTO_CREATE);
                }

                // 在绑定成功后利用 Binder 中提供的状态函数判断显示浮窗
                mFloatingServiceConnection.executeAfterServiceConnected(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFloatingServiceBinder.getWindowStatus()) {
                            mFloatingServiceBinder.showWindow();
                        }
                    }
                });
            }
        });

        // 关闭浮窗按钮
        mRemoveBtn = findViewById(R.id.remove_btn);
        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确保绑定存在时利用 Binder 提供的状态函数判断关闭浮窗
                mFloatingServiceConnection.executeAfterServiceConnected(new Runnable() {
                    @Override
                    public void run() {
                        if (mFloatingServiceBinder.getWindowStatus()) {
                            mFloatingServiceBinder.hideWindows();
                        }
                    }
                });
            }
        });
    }

    // 自定义 ServiceConnection
    public class FloatingServiceConnection implements ServiceConnection {
        private Context mContext;
        private ArrayList<Runnable> runnableArrayList;
        private boolean connected = false;

        public FloatingServiceConnection(Context mContext) {
            this.mContext = mContext;
            runnableArrayList = new ArrayList<>();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取服务的操作对象
            FloatingWindowService.FloatBinder binder = (FloatingWindowService.FloatBinder) service;
            binder.getService();

            mFloatingServiceBinder = (FloatingWindowService.FloatBinder) service;
            hasBind = true;

            connected = true;
            for (Runnable action : runnableArrayList) {
                action.run();
            }
            runnableArrayList.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBind = false;
        }

        // 绑定成功后执行函数
        // 通过设置一个 RunnableList 将未绑定时的操作保存起来，等待成功后再运行
        public void executeAfterServiceConnected(Runnable action) {
            if (connected) {    // 若已经绑定成功则直接运行
                action.run();
            } else {            // 若未绑定则存入 List
                runnableArrayList.add(action);
            }
        }

    }

    // 判断服务是否运行函数
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        Log.e("OnlineService：", className);
        for (int i = 0; i < serviceList.size(); i++) {
            Log.e("serviceName：", serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().contains(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}