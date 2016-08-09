package com.bluecats.app.beaconscanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstActivity extends AppCompatActivity {
    private static final int PERMISSION_RESPONSE = 0x100;
    private static final int PERMISSION_SDCARD_RESPONSE = 0x101;

    @BindView(R.id.native_scan) View btn1;
    @BindView(R.id.sdk_scan) View btn2;
    @BindView(R.id.sdk_rssi_rec) View btn3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        ButterKnife.bind(this);
        btn1.setEnabled(false);
        btn2.setEnabled(false);
        btn3.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int permissionCheck = ContextCompat.checkSelfPermission(FirstActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FirstActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_RESPONSE);
                    btn1.setEnabled(false);
                    btn2.setEnabled(false);
                } else {
                    btn1.setEnabled(true);
                    btn2.setEnabled(true);
                }
                permissionCheck = ContextCompat.checkSelfPermission(FirstActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FirstActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_SDCARD_RESPONSE);
                    btn3.setEnabled(false);
                } else {
                    btn3.setEnabled(true);
                }

            }
        }, 2000);
    }

    @OnClick(R.id.sdk_rssi_rec)
    protected void recordingSDK() {
        startActivity(new Intent(this, RssiRecorderActivity.class));
    }

    @OnClick(R.id.sdk_scan)
    protected void openSDKScanner(View v) {
        startActivity(new Intent(this, BlueCatsActivity.class));
    }

    @OnClick(R.id.native_scan)
    protected void openNativeScanner(View v) {
        startActivity(new Intent(this, MainActivity.class));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_RESPONSE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btn1.setEnabled(true);
                btn2.setEnabled(true);
            } else {
                Toast.makeText(this, "permission of location service denied.", Toast.LENGTH_LONG);
            }
        } else if (requestCode == PERMISSION_SDCARD_RESPONSE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btn3.setEnabled(true);
            } else {
                Toast.makeText(this, "permission of writing sdcard denied.", Toast.LENGTH_LONG);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return true;
    }

}
