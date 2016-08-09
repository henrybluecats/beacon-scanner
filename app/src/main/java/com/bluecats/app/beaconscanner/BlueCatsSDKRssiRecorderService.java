package com.bluecats.app.beaconscanner;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BlueCatsSDK;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueCatsSDKRssiRecorderService extends Service {
    public BlueCatsSDKRssiRecorderService() {
    }
    private static final String APP_TOKEN = "9a04cd56-6a40-4e2e-b04e-2d0a4e352abf"; // "BC Data Testing" "Android Test"
    BCBeaconManager mBCBeaconManager;
    int scanWindow = 3000;
    int scanInterval = 700;
    boolean isSmoothRssi = true;
    String mSerialNumber = "B0005618";
    boolean isLowPowerMode = false;
    PrintWriter mWriter;
    boolean isBeaconEntered = true;
    int lastRssi = 0;
    final Object mObject = new Object();
    private static final String RECORDS_FOLDER="bcrssirecords";
    static SimpleDateFormat sdf = new SimpleDateFormat("MM.dd HH:mm:ss");
    @Override
    public void onCreate() {
        super.onCreate();
        mBCBeaconManager = new BCBeaconManager();
        scanWindow = Utils.getScanWindow(this, Utils.TYPE_SDK_SCANNER);
        scanInterval = Utils.getScanInterval(this, Utils.TYPE_SDK_SCANNER);
        isSmoothRssi = Utils.isSmoothingRSSI(this);
        isLowPowerMode = Utils.isLowPowerInForeground(this);

        String sn = Utils.getSerialNumber(this);
        if (sn.length()>0) {
            mSerialNumber = sn;
        }

        try {
            File file = new File("/sdcard/"+RECORDS_FOLDER);
            if (!file.exists()) {
                file.mkdir();
            }
            mWriter = new PrintWriter(new FileOutputStream("/sdcard/"+RECORDS_FOLDER+"/"+getFineFileName(getTimeString(System.currentTimeMillis()))+".csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getTimeString(long milli) {
        return sdf.format(new Date(milli));
    }
    private String getFineFileName(String value) {
        return value.replace(' ', '-').replace('.', '-').replace(':','-').replace('+','-');

    }
    @Override
    public void onDestroy() {
        mBCBeaconManager.unregisterCallback(mBeaconManagerCallback);
        BlueCatsSDK.stopPurring();
        try {
            mWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private String createRow(long time, int rssi, boolean beaconEntered) {
        StringBuilder sb = new StringBuilder();
        sb.append(time/1000).append(',');
        sb.append(getTimeString(time)).append(',');
        sb.append(rssi).append(',');
        sb.append(beaconEntered?10:-10).append('\n');
        return sb.toString();
    }

    BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback() {
        @Override
        public void didRangeBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                if (mSerialNumber.equalsIgnoreCase(beacon.getSerialNumber())) {
                    String row = null;
                    synchronized (mObject) {
                        isBeaconEntered = true;
                        lastRssi = beacon.getRSSI();
                        row = createRow(System.currentTimeMillis(), lastRssi, isBeaconEntered);

                    }
                    mWriter.append(row);
                    mWriter.flush();
                }
            }
        }

        @Override
        public void didExitBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                if (mSerialNumber.equalsIgnoreCase(beacon.getSerialNumber())) {
                    String row = null;
                    synchronized (mObject) {
                        isBeaconEntered = false;
                        row = createRow(System.currentTimeMillis(), lastRssi, isBeaconEntered);
                    }
                    mWriter.append(row);
                    mWriter.flush();
                }
            }
        }

        @Override
        public void didEnterBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                if (mSerialNumber.equalsIgnoreCase(beacon.getSerialNumber())) {
                    String row = null;
                    synchronized (mObject) {
                        isBeaconEntered = true;
                        row = createRow(System.currentTimeMillis(), lastRssi, isBeaconEntered);
                    }
                    mWriter.append(row);
                    mWriter.flush();
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    public void startRecording() {
        Map<String, String> options = new HashMap<>();
        options.put("BC_OPTION_SCAN_TIME", String.valueOf(scanWindow));
        options.put("BC_OPTION_TIME_BETWEEN_SCANS_HIGH_FREQUENCY", String.valueOf(scanInterval));

        options.put(BlueCatsSDK.BC_OPTION_CACHE_ALL_BEACONS_FOR_APP, "true");
        options.put(BlueCatsSDK.BC_OPTION_USE_RSSI_SMOOTHING, String.valueOf(isSmoothRssi));
        options.put("BC_OPTION_LOW_POWER_IN_FOREGROUND", String.valueOf(isLowPowerMode));
        BlueCatsSDK.setOptions(options);
        BlueCatsSDK.startPurringWithAppToken(this, APP_TOKEN);
        mBCBeaconManager.registerCallback(mBeaconManagerCallback);

    }

    class LocalBinder extends Binder {
        BlueCatsSDKRssiRecorderService getService() {
            return BlueCatsSDKRssiRecorderService.this;
        }
    }

    public IBinder mLocalService = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalService;
    }
}
