package xin.ahza.clockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

//        mFloatingBtn = findViewById(R.id.floatingBtn);
//        mFloatingBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                moveTaskToBack(true);
////                Intent intent = new Intent(MainActivity.this, FloatingWindowService.class);
////                startService(intent);
//
////                moveTaskToBack(true);
//                Intent intentBind = new Intent(MainActivity.this, FloatingWindowService.class);
//                bindService(intentBind, mFloatingServiceConnection, Context.BIND_AUTO_CREATE);
//
//                Log.e("TAG------", "onClick: onCreate" +  mFloatingServiceBinder);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFloatingBtn = findViewById(R.id.floatingBtn);
        mFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
//                        Toast.makeText(MainActivity.this, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
                        new GlobalDialogSingle(MainActivity.this, "", "当前未获取悬浮窗权限", "去开启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                            }
                        }).show();
                    } else {
                        Intent intentBind = new Intent(MainActivity.this, FloatingWindowService.class);
                        bindService(intentBind, mFloatingServiceConnection, Context.BIND_AUTO_CREATE);

                        Log.e("TAG------", "onClick: onResume " +  mFloatingServiceBinder);
                    }
                }

                mFloatingServiceConnection.executeAfterServiceConnected(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("TAG------", "onClick: " +  mFloatingServiceBinder);
                        if (!mFloatingServiceBinder.getWindowStatus()) {
                            mFloatingServiceBinder.showWindow();
                        }
                    }
                });
            }
        });

        mRemoveBtn = findViewById(R.id.remove_btn);
        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingServiceConnection.executeAfterServiceConnected(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("TAG----", "onClick: remove " + mFloatingServiceBinder );
                        if (mFloatingServiceBinder.getWindowStatus()) {
                            mFloatingServiceBinder.hideWindows();
                        }
                    }
                });
            }
        });
    }

    public class FloatingServiceConnection implements ServiceConnection {
        private Context mContext = null;
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

        public void executeAfterServiceConnected(Runnable action) {
            if (connected) {
                action.run();
            } else {
                runnableArrayList.add(action);
            }
        }

    }

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