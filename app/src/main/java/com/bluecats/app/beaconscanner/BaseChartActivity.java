package com.bluecats.app.beaconscanner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.FormattedStringCache;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BaseChartActivity extends AppCompatActivity {
    private static final String TAG = "BaseChartActivity";
    LineChart mChart;
    Handler mHandler = new Handler();
    protected static final int UPDATE_INTERVAL = 1000;
    protected static final int MAX_LINES = 1;


    protected Map<String, Item> mAllItems = new HashMap<String, Item>();
    Runnable updateThread = new Runnable() {
        @Override
        public void run() {
            updateLogs();
            updateChart();
            mHandler.postDelayed(updateThread, UPDATE_INTERVAL);
        }
    };

    private void updateChart() {
        LineData data = mChart.getData();
        if (data != null) {
            for( int i = 0; i < data.getDataSetCount(); i++) {
                ILineDataSet set = data.getDataSetByIndex(i);
                set.addEntry(new Entry(data.getEntryCount(), getRssiValue(i)));
            }
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();

            mChart.setVisibleXRangeMaximum(30);

            mChart.moveViewToX(data.getEntryCount());
        }
    }

    private int getRssiValue(int idx) {
        synchronized (mAllItems) {
            for (Item item: mAllItems.values()) {
                if (item.idx == idx) {
                    int rssiVal = item.rssi;
                    if (idx == 0) {
                        item.rssi = 0;
                    }
                    return rssiVal;
                }
            }
            return 0;
        }
    }

    private void updateLogs() {
        synchronized (mAllItems) {

            for (Map.Entry<String, Item> entry: mAllItems.entrySet()) {
                Log.d(TAG, entry.getKey()+",,,"+entry.getValue().address+",,,"+entry.getValue().rssi);
//                entry.getValue().rssi = 0;
            }
        }
    }

    protected void setupChart() {
        mChart = (LineChart)findViewById(R.id.chart);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(true);

        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
//        l.setTypeface(mTfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
//        xl.setTypeface(mTfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setGranularity(1L);
//        xl.setValueFormatter(new AxisValueFormatter() {
//
//            private FormattedStringCache.Generic<Long, Date> mFormattedStringCache =
//                    new FormattedStringCache.Generic<>(new SimpleDateFormat("mm:ss.SSS"));
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                Long v = (long) value;
//                String txt = mFormattedStringCache.getFormattedValue(new Date(v), v);
//                Log.d(TAG, "time: "+txt);
//                return txt;
//            }
//
//            @Override
//            public int getDecimalDigits() {
//                return 0;
//            }
//        });

        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaxValue(20f);
        leftAxis.setAxisMinValue(-100f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    protected ILineDataSet createSet(int i, String name) {
        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(i==0?ColorTemplate.getHoloBlue():ColorTemplate.MATERIAL_COLORS[1]);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(i==0?ColorTemplate.getHoloBlue():ColorTemplate.MATERIAL_COLORS[1]);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(i==0);
        return set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupChart();

        mHandler.postDelayed(updateThread, UPDATE_INTERVAL);

    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(updateThread);
        super.onDestroy();
    }
}
