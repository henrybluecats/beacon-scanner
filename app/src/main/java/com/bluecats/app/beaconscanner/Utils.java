package com.bluecats.app.beaconscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by henrycheng on 4/08/2016.
 */
public class Utils {

    public static final int TYPE_NATIVE_SCANNER = 0x01;
    public static final int TYPE_SDK_SCANNER = 0x02;
    public static int getScanWindow(Context ctx, int typeScanner) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String key = ((typeScanner == TYPE_NATIVE_SCANNER)?"native":"sdk")+"_scan_window";
        String str = sp.getString(key, "3000");
        try {
            int value = Integer.valueOf(str);
            return value;
        } catch (Exception e) {
            //
        }
        return 3000;
    }

    public static int getScanInterval(Context ctx, int typeScanner) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String key = ((typeScanner == TYPE_NATIVE_SCANNER)?"native":"sdk")+"_scan_interval";
        String str = sp.getString(key, "700");
        try {
            int value = Integer.valueOf(str);
            return value;
        } catch (Exception e) {
            //
        }
        return 700;

    }
    public static boolean isSmoothingRSSI(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean str = sp.getBoolean("smooth_switch", true);
        return str;
    }

    public static int getScanMode(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String str = sp.getString("scanmode_list", "2");
        try {
            int value = Integer.valueOf(str);
            return value;
        } catch (Exception e) {

        }
        return 2;
    }

    public static String getBluetoothMacAddress(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sp.getString("native_scan_mac", "");
    }

    public static String getSerialNumber(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sp.getString("sdk_scan_serial_num", "");
    }

    public static boolean isLowPowerInForeground(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean str = sp.getBoolean("lowpower_switch", false);
        return str;
    }
}
