package xin.ahza.clockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

import xin.ahza.clockapp.service.FloatingWindowService;

public class MainActivity extends AppCompatActivity {
    private Button mFloatingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFloatingBtn = findViewById(R.id.floatingBtn);
        mFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                Intent intent = new Intent(MainActivity.this, FloatingWindowService.class);

                startService(intent);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isServiceRunning(this, FloatingWindowService.class.getName())) {
            Intent stopServiceIntent = new Intent(MainActivity.this, FloatingWindowService.class);
            stopService(stopServiceIntent);
            Log.e("TAG----RESUME----SERVICE", "onPostResume: isServiceRunning" + isServiceRunning(this, FloatingWindowService.class.getName()));
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