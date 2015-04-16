package com.devmobility.killerpresence.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.devmobility.killerpresence.util.BluetoothDeviceFoundListener;
import com.devmobility.killerpresence.util.BluetoothRssiListener;
import com.devmobility.killerpresence.util.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class BluetoothMgr extends BroadcastReceiver {

    private static final String TAG = BluetoothMgr.class.getSimpleName();

    private BluetoothDeviceFoundListener mBluetoothDeviceFoundListener;
    private BluetoothRssiListener mBluetoothRssiListener;
    private BluetoothService mBluetoothService;

    private TimerTask mReconnectTimerTask;
    private Timer mReconnectTimer;

    private Context mContext;
    private String mDeviceMac = "";

    private BluetoothAdapter mBluetoothAdapter;

    private boolean mReceiverRegistered = false;
    private boolean mServiceRegistered = false;

    private boolean mInitialized = false;

    public BluetoothMgr(Context context) {
        mContext = context;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "onServiceConnected in BluetoothMgr");
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (!mDeviceMac.isEmpty()) {
                mBluetoothService.connect(mDeviceMac);
                mBluetoothService.registerBluetoothRssiListenerCallback(mBluetoothRssiListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothService.registerBluetoothRssiListenerCallback(null);
            mBluetoothService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothService.ACTION_GATT_CONNECTED:
                    Log.d(TAG, "onReceive ACTION_GATT_CONNECTED");
                    if (mReconnectTimer != null) mReconnectTimer.cancel();
                    if (mReconnectTimerTask != null) mReconnectTimerTask.cancel();
                    if (mBluetoothDeviceFoundListener != null)
                        mBluetoothDeviceFoundListener.onConnected();
                    break;
                case BluetoothService.ACTION_GATT_DISCONNECTED:
                    if (mReconnectTimer != null) mReconnectTimer.cancel();
                    if (mReconnectTimerTask != null) mReconnectTimerTask.cancel();
                    if (mBluetoothDeviceFoundListener != null)
                        mBluetoothDeviceFoundListener.onDisconnected();
                    mReconnectTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            mBluetoothService.connect(mDeviceMac);
                        }
                    };
                    mReconnectTimer = new Timer();
                    mReconnectTimer.schedule(mReconnectTimerTask,
                            Constants.BTLE_RECONNECT_INITIAL_DELAY,
                            Constants.BTLE_RECONNECT_REPEAT_DELAY);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    if (mBluetoothDeviceFoundListener != null) {
                        BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        mBluetoothDeviceFoundListener.onFound(d);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    if (mBluetoothDeviceFoundListener != null) {
                        mBluetoothDeviceFoundListener.onActiveStateChanged(true);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (mBluetoothDeviceFoundListener != null) {
                        mBluetoothDeviceFoundListener.onActiveStateChanged(false);
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (d.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            Log.d(TAG, "BOND_BONDING");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Log.d(TAG, "BOND_BONDED");
                            ((CalibrateActivity) context).next();
                            if (mBluetoothService != null) {
                                Log.d(TAG, "mBluetoothService register and connect!!!");
                                mBluetoothService.registerBluetoothRssiListenerCallback(mBluetoothRssiListener);
                                SharedPreferences prefs = context.getSharedPreferences(Constants.APP_PREFS, 0);
                                String mac = prefs.getString(Constants.PREFS_BT_MAC_ADDRESS, "");
                                mBluetoothService.connect(mac);
                            }
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.d(TAG, "BOND_NONE");
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        return filter;
    }

    public void instantiate() {
        mInitialized = true;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mServiceRegistered) {
            Intent gattServiceIntent = new Intent(mContext, BluetoothService.class);
            mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            mServiceRegistered = true;
        }
        if (!mReceiverRegistered) {
            mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            mReceiverRegistered = true;
        }
    }

    public void destroy() {
        if (mReconnectTimer != null) mReconnectTimer.cancel();
        if (mReconnectTimerTask != null) mReconnectTimerTask.cancel();
        mInitialized = false;
        if (mReceiverRegistered)
            mContext.unregisterReceiver(mGattUpdateReceiver);
        mReceiverRegistered = false;
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter = null;
        }
        if (mServiceRegistered)
            mContext.unbindService(mServiceConnection);
        mServiceRegistered = false;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public boolean getInitialized() {
        return mInitialized;
    }
    public boolean isServiceRegistered() {
        return mServiceRegistered;
    }

    public boolean isReceiverRegistered() {
        return mReceiverRegistered;
    }

    public void setBluetoothDeviceFoundListener(BluetoothDeviceFoundListener listener) {
        this.mBluetoothDeviceFoundListener = listener;
    }
    public void setBluetoothRssiListener(BluetoothRssiListener listener) {
        this.mBluetoothRssiListener = listener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case BluetoothService.ACTION_GATT_CONNECTED:
                if (mReconnectTimer != null) mReconnectTimer.cancel();
                if (mReconnectTimerTask != null) mReconnectTimerTask.cancel();

                break;
            case BluetoothService.ACTION_GATT_DISCONNECTED:

                mReconnectTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        SharedPreferences prefs = context.getSharedPreferences(Constants.APP_PREFS, 0);
                        String mac = prefs.getString(Constants.PREFS_BT_MAC_ADDRESS, "");
                        mBluetoothService.connect(mac);
                    }
                };
                mReconnectTimer = new Timer();
                mReconnectTimer.schedule(mReconnectTimerTask,
                        Constants.BTLE_RECONNECT_INITIAL_DELAY,
                        Constants.BTLE_RECONNECT_REPEAT_DELAY);

                break;
        }
    }

    public void setMac(String mac) {
        mDeviceMac = mac;
    }
}
