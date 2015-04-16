package com.devmobility.killerpresence.bluetooth;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.devmobility.killerpresence.R;
import com.devmobility.killerpresence.util.BluetoothRssiListener;
import com.devmobility.killerpresence.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements BluetoothRssiListener{
    private final static String TAG = MainActivity.class.getSimpleName();

    /**
     * Simulate boot complete by ADB shelling into device and using:
     * <p/>
     * am broadcast -a android.intent.action.BOOT_COMPLETED
     */

    private TextView mDeviceName;
    private TextView mDeviceAddress;
    private TextView mRssiSignal;
    private TextView mConnectionState;
    private SeekBar mSeekBar;
    private ToggleButton mRssiToggle;

    private List<Integer> mRssiAverage = new ArrayList<>();

    private BluetoothMgr mBluetoothMgr;
    private boolean mConnected = false;

    private String mDeviceMac;
    private int mThresholdNear;
    private int mThresholdFar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupActionBar();
        setupScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectionState(false);
        launch();
    }

    private void launch() {
        SharedPreferences prefs = getSharedPreferences(Constants.APP_PREFS, 0);
        mDeviceMac = prefs.getString(Constants.PREFS_BT_MAC_ADDRESS, "");
        if (mDeviceMac.isEmpty()) {
            Intent calibrateActivity = new Intent(this, CalibrateActivity.class);
            startActivity(calibrateActivity);
        } else {
            // TODO: handle timing issue here
            mThresholdNear = prefs.getInt(Constants.PREFS_NEAR_THRESHOLD, 0);
            mThresholdFar = prefs.getInt(Constants.PREFS_FAR_THRESHOLD, 0);
            mBluetoothMgr = new BluetoothMgr(this);
            mBluetoothMgr.instantiate();
            mBluetoothMgr.setMac(mDeviceMac);
            mBluetoothMgr.setBluetoothRssiListener(this);
        }
    }

    private void setupActionBar() {
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(R.string.title_devices);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void setupScreen() {
        mDeviceAddress = (TextView) findViewById(R.id.device_address);
        mDeviceName = (TextView) findViewById(R.id.device_name);
        mRssiToggle = (ToggleButton) findViewById(R.id.toggle_rssi);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mRssiSignal = (TextView) findViewById(R.id.device_signal);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                return true;
            case R.id.menu_disconnect:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_test_launch:
                Intent intent = new Intent(Intent.ACTION_DEFAULT);
                intent.setComponent(new ComponentName(Constants.LAUNCH_PACKAGE_NAME,
                        Constants.LAUNCH_PACKAGE_NAME + Constants.LAUNCH_APP_NAME));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(boolean isActive) {
        mConnected = isActive;
        mRssiAverage.clear();
        mSeekBar.setProgress(0);
        if (isActive) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceName.setText(mBluetoothMgr.getBluetoothAdapter().getRemoteDevice(mDeviceMac).getName());
                    mDeviceAddress.setText(mBluetoothMgr.getBluetoothAdapter().getRemoteDevice(mDeviceMac).getAddress());
                    mConnectionState.setText(R.string.connected);
                    mRssiSignal.setText("[waiting for data]");
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceName.setText("n/a");
                    mDeviceAddress.setText("n/a");
                    mRssiSignal.setText("n/a");
                    mConnectionState.setText(R.string.connected);
                }
            });
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onRssiSignalInterrupted() {
        // TODO: reconnect here
    }

    @Override
    public void onRssiSignalUpdated(final int rssi) {
        updateConnectionState(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRssiSignal.setText(" " + rssi);
            }
        });

        mRssiAverage.add(rssi);
        if (mRssiAverage.size() == 31)
            mRssiAverage.remove(0);

        double progress = 0;
        for (int i : mRssiAverage) {
            progress += i;
        }
        progress = -4 * (progress/30);
        if (mRssiToggle.isChecked()) {
            //Log.d(TAG, "*** progress = " + progress + " ***");
            mSeekBar.setProgress(100 - (int) progress);
        }

        if (progress <= (double)mThresholdNear/100) {
            Log.d(TAG, "Near threshold reached!");
            // Currently opens the Gmail app.
            Intent intent = new Intent(Intent.ACTION_DEFAULT);
            intent.setComponent(new ComponentName(Constants.LAUNCH_PACKAGE_NAME,
                    Constants.LAUNCH_PACKAGE_NAME + Constants.LAUNCH_APP_NAME));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (progress >= (double)mThresholdFar/100) {
            Log.d(TAG, "Far threshold reached!");
            // onBackPressed();
        }

    }

}