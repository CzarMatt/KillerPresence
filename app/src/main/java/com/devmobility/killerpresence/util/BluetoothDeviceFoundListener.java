package com.devmobility.killerpresence.util;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceFoundListener {
    void onActiveStateChanged(boolean state);

    void onFound(BluetoothDevice d);

    void onConnected();

    void onDisconnected();
}
