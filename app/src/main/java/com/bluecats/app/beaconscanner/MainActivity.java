package com.bluecats.app.beaconscanner;

import com.github.mikephil.charting.data.LineData;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseChartActivity {

    private int SCAN_WINDOW = 6000;
    private int SCAN_INTERVAL = 700;
    private int SCAN_MODE = ScanSettings.SCAN_MODE_LOW_LATENCY;
    private String MAC_ADDRESS = "7D:AC:96:13:A6:39";
    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mScanner;
    private ScanCallback callbackScanner = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            boolean isAdded = false;
            if (!MAC_ADDRESS.equalsIgnoreCase(result.getDevice().getAddress())) {
                return;
            }
            synchronized (mAllItems) {
                Item item = mAllItems.get(result.getDevice().getAddress());
                if (item != null) {
                    item.rssi = result.getRssi();
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(MainActivity.this, "error "+ errorCode, Toast.LENGTH_LONG).show();
            scanThread.interrupt();
        }
    };
    Thread scanThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                startScan();
                try {
                    sleep(SCAN_WINDOW);
                } catch (InterruptedException e) {
                    stopScan();
                    break;
                }
                stopScan();
                try {
                    sleep(SCAN_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };

    protected void setupLineData() {
        Item item = new Item();
        item.address = MAC_ADDRESS;
        mAllItems.put(MAC_ADDRESS, item);
        LineData data =  new LineData();
        data.addDataSet(createSet(0, MAC_ADDRESS+","+SCAN_WINDOW+","+SCAN_INTERVAL+","+SCAN_MODE));
        mChart.setData(data);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = mBluetoothAdapter.getBluetoothLeScanner();

        SCAN_WINDOW = Utils.getScanWindow(this, Utils.TYPE_NATIVE_SCANNER);
        SCAN_INTERVAL = Utils.getScanInterval(this, Utils.TYPE_NATIVE_SCANNER);
        SCAN_MODE = Utils.getScanMode(this);
        String macaddress = Utils.getBluetoothMacAddress(this);
        if (macaddress.length()>0) {
            MAC_ADDRESS = macaddress;
        }
        Toast.makeText(this, SCAN_WINDOW+"s on,"+SCAN_INTERVAL+"s off."+"mode:"+SCAN_MODE+",Mac:"+MAC_ADDRESS, Toast.LENGTH_LONG).show();
        setupLineData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!scanThread.isAlive()) {
            scanThread.start();
        }
    }

    @Override
    protected void onPause() {
        scanThread.interrupt();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void startScan() {
        if(mScanner != null) {
            List<ScanFilter> filterList = new ArrayList<ScanFilter>();
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(SCAN_MODE)
                    .build();
            mScanner.startScan(filterList, settings, callbackScanner);
        }
    }

    protected void stopScan() {
        if(mScanner != null) {
            mScanner.stopScan(callbackScanner);
        }
    }

}
