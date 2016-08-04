package com.bluecats.app.beaconscanner;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BlueCatsSDK;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueCatsActivity extends BaseChartActivity {

    private static final String TAG = "BlueCatsActivity";
    int scanWindow = 3000;
    int scanInterval = 700;
    boolean isSmoothRssi = true;
    String mSerialNumber = "B0005618";
    boolean isLowPowerMode = false;

    private static final String ENTER_EXIT = "Enter/Exit";
    private static final String APP_TOKEN = "9a04cd56-6a40-4e2e-b04e-2d0a4e352abf"; // "BC Data Testing" "Android Test"
    BCBeaconManager mBCBeaconManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBCBeaconManager = new BCBeaconManager();
        setTitle("BlueCatsSDK");

        scanWindow = Utils.getScanWindow(this, Utils.TYPE_SDK_SCANNER);
        scanInterval = Utils.getScanInterval(this, Utils.TYPE_SDK_SCANNER);
        isSmoothRssi = Utils.isSmoothingRSSI(this);
        isLowPowerMode = Utils.isLowPowerInForeground(this);

        String sn = Utils.getSerialNumber(this);
        if (sn.length()>0) {
            mSerialNumber = sn;
        }

        Toast.makeText(this, scanWindow+"s on, "+scanInterval+"s off. SN:"+mSerialNumber+",low power:"+isLowPowerMode, Toast.LENGTH_LONG).show();

        setupLineData();
        Map<String, String> options = new HashMap<>();
        options.put("BC_OPTION_SCAN_TIME", String.valueOf(scanWindow));
        options.put("BC_OPTION_TIME_BETWEEN_SCANS_HIGH_FREQUENCY", String.valueOf(scanInterval));

        options.put(BlueCatsSDK.BC_OPTION_CACHE_ALL_BEACONS_FOR_APP, "true");
        options.put(BlueCatsSDK.BC_OPTION_USE_RSSI_SMOOTHING, String.valueOf(isSmoothRssi));
        options.put("BC_OPTION_LOW_POWER_IN_FOREGROUND", String.valueOf(isLowPowerMode));
        BlueCatsSDK.setOptions(options);
        BlueCatsSDK.startPurringWithAppToken(this, APP_TOKEN);
        mBCBeaconManager.registerCallback(mBeaconManagerCallback);

        if (savedInstanceState != null) {
            int itemCnt = savedInstanceState.getInt("allitems_count", 0);
            if (itemCnt > 0) {
                for (int j = 0; j < itemCnt; j++) {
                    String str = savedInstanceState.getString("items_"+j);
                    String[] parts = str.split(",");
                    Item item = new Item();
                    item.idx = Integer.parseInt(parts[1]);
                    item.rssi = Integer.parseInt(parts[2]);
                    item.address = parts[3];
                    mAllItems.put(parts[0], item);
                }
            }
            int datasetCnt = savedInstanceState.getInt("dataset_count", 0);
            if (datasetCnt > 0) {
                LineData data = mChart.getData();
                for (int i=0;i < datasetCnt; i++) {
                    ILineDataSet set = data.getDataSetByIndex(i);
                    ArrayList<Entry> entrys = savedInstanceState.getParcelableArrayList("dataset_"+i);
                    for (Entry entry: entrys) {
                        set.addEntry(entry);
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LineData data = mChart.getData();
        int cnt = data.getDataSetCount();
        outState.putInt("dataset_count", cnt);
        for(int i=0; i<cnt; i++) {
            ILineDataSet set = data.getDataSetByIndex(i);
            ArrayList<Entry> entrys = new ArrayList<>(set.getEntryCount());
            for (int j=0;j<set.getEntryCount();j++) {
                entrys.add(set.getEntryForIndex(j));
            }
            outState.putParcelableArrayList("dataset_"+i,entrys);
        }

        outState.putInt("allitems_count", mAllItems.size());

        cnt = 0;
        for(Map.Entry<String, Item> mapEntry: mAllItems.entrySet()) {
            StringBuilder sb = new StringBuilder();
            Item item = mapEntry.getValue();
            sb.append(mapEntry.getKey());
            sb.append(',');
            sb.append(item.idx);
            sb.append(',');
            sb.append(item.rssi);
            sb.append(',');
            sb.append(item.address);
            outState.putString("items_"+cnt, sb.toString());
            cnt++;
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mBCBeaconManager.unregisterCallback(mBeaconManagerCallback);
        BlueCatsSDK.stopPurring();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        BlueCatsSDK.didEnterBackground();
        super.onPause();
    }

    protected void setupLineData() {
        Item item = new Item();
        item.idx = 0;
        mAllItems.put(mSerialNumber, item);
        LineData data = new LineData();
        data.addDataSet(createSet(0, mSerialNumber+","+scanWindow+","+scanInterval+","+isSmoothRssi+","+isLowPowerMode));

        item = new Item();
        item.idx = 1;
        mAllItems.put(ENTER_EXIT, item);
        data.addDataSet(createSet(1, ENTER_EXIT));
        mChart.setData(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BlueCatsSDK.didEnterForeground();

    }

    BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback() {
        @Override
        public void didRangeBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                if (mSerialNumber.equalsIgnoreCase(beacon.getSerialNumber())) {
                    synchronized (mAllItems) {
                        Item item = mAllItems.get(beacon.getSerialNumber());
                        if (item != null) {
                            item.address = beacon.getSerialNumber();
                            item.rssi = beacon.getRSSI();
                        }
                        item = mAllItems.get(ENTER_EXIT);
                        if (item != null) {
                            item.rssi = 10;
                        }
                    }
                }
            }
        }

        @Override
        public void didExitBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                if (mSerialNumber.equalsIgnoreCase(beacon.getSerialNumber())) {
                    synchronized (mAllItems) {
                        Item item = mAllItems.get(ENTER_EXIT);
                        if (item != null) {
                            item.rssi = -10;
                        }
                    }
                }
            }
        }

        @Override
        public void didEnterBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                if (mSerialNumber.equalsIgnoreCase(beacon.getSerialNumber())) {
                    synchronized (mAllItems) {
                        Item item = mAllItems.get(ENTER_EXIT);
                        if (item != null) {
                            item.rssi = 10;
                        }
                    }
                }
            }
        }
    };
}
