package com.devmobility.killerpresence.util;

public interface BluetoothRssiListener {
    void onRssiSignalUpdated(final int rssi);

    void onRssiSignalInterrupted();
}
