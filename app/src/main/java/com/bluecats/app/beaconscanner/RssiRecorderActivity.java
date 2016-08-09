package com.bluecats.app.beaconscanner;

import com.bluecats.sdk.BlueCatsSDK;
import com.bluecats.sdk.BlueCatsSDKService;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RssiRecorderActivity extends AppCompatActivity {

    @BindView(R.id.btn_sdk)
    Button btn_sdk;

    @BindView(R.id.btn_sdk_stop)
    Button btn_sdk_stop;

    @Override
    protected void onPause() {
        super.onPause();
        BlueCatsSDK.didEnterBackground();
    }

    @Override
    protected void onResume() {
        BlueCatsSDK.didEnterForeground();
        super.onResume();
    }

    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi_recorder);
        ButterKnife.bind(this);
        btn_sdk.setEnabled(false);
        btn_sdk_stop.setEnabled(false);
        mHandler = new Handler();
        mHandler.post(autoCheck);
    }

    Runnable autoCheck = new Runnable() {
        @Override
        public void run() {
            if (!isMyServiceRunning(BlueCatsSDKRssiRecorderService.class)) {
                btn_sdk.setEnabled(true);
                btn_sdk_stop.setEnabled(false);
                return;
            }
            if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_NEVER_PURRED) {
                btn_sdk.setEnabled(true);
                btn_sdk_stop.setEnabled(false);
            } else if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_PURRING) {
                btn_sdk.setEnabled(false);
                btn_sdk_stop.setEnabled(true);
            } else if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_STOPPED_PURRING
                    || BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_PURRING_WITH_ERRORS) {
                btn_sdk.setEnabled(true);
                btn_sdk_stop.setEnabled(false);
            } else {
                mHandler.postDelayed(autoCheck, 1000);
            }
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.btn_sdk)
    protected void startRecording() {
        startService(new Intent(this, BlueCatsSDKRssiRecorderService.class));
        btn_sdk.setEnabled(false);
        btn_sdk_stop.setEnabled(false);
        mHandler.postDelayed(autoCheck, 2000);
        // IMPORTANT:
        // enable SDK running in foreground mode immediately after starting service.
        //  If startService() is called in OnCreated(), then don't need to call didEnterForeground().
        BlueCatsSDK.didEnterForeground();
    }

    @OnClick(R.id.btn_sdk_stop)
    protected void stopRecording() {
        if (BlueCatsSDK.getStatus() == BlueCatsSDK.BCStatus.BC_STATUS_PURRING) {
            stopService(new Intent(this, BlueCatsSDKRssiRecorderService.class));
            btn_sdk.setEnabled(false);
            btn_sdk_stop.setEnabled(false);
            mHandler.postDelayed(autoCheck, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(autoCheck);
        super.onDestroy();
    }

}
