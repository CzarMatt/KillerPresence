package com.devmobility.killerpresence.bluetooth;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.devmobility.killerpresence.R;
import com.devmobility.killerpresence.util.BluetoothDeviceFoundListener;
import com.devmobility.killerpresence.util.BluetoothDeviceListAdapter;
import com.devmobility.killerpresence.util.Constants;

public class DeviceListFragment extends Fragment implements BluetoothDeviceFoundListener {

    private static final String TAG = DeviceListFragment.class.getSimpleName();

    private BluetoothMgr mBluetoothMgr;
    private BluetoothDeviceListAdapter mDeviceListAdapter;

    private boolean mScanning;

    public DeviceListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.devicelist_frag, container, false);

        ActionBar ab = getActivity().getActionBar();
        if (ab != null)
            ab.setTitle(R.string.title_calibrate);

        mBluetoothMgr = ((CalibrateActivity) getActivity()).getBluetoothMgr();
        mBluetoothMgr.setBluetoothDeviceFoundListener(this);

        setHasOptionsMenu(true);

        if (mBluetoothMgr.getBluetoothAdapter() == null) {
            TextView tv = (TextView) view.findViewById(R.id.textview_devices);
            tv.setText(getResources().getString(R.string.error_bt_not_supported));
        } else {
            if (!mBluetoothMgr.getBluetoothAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            } else {
                ListView deviceList = (ListView) view.findViewById(R.id.listview_devices);
                mDeviceListAdapter = new BluetoothDeviceListAdapter(getActivity());
                deviceList.setAdapter(mDeviceListAdapter);
                deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mBluetoothMgr.getBluetoothAdapter().cancelDiscovery();
                        BluetoothDevice device = mDeviceListAdapter.getDevice(position);
                        Editor editor = getActivity().getSharedPreferences(Constants.APP_PREFS, 0).edit();
                        editor.putString(Constants.PREFS_BT_MAC_ADDRESS, device.getAddress());
                        editor.apply();
                        device.createBond();
                    }
                });
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothMgr == null) {
            mBluetoothMgr = ((CalibrateActivity) getActivity()).getBluetoothMgr();
        }
        if(!mBluetoothMgr.getInitialized()) {
            mBluetoothMgr.instantiate();
        }
        mBluetoothMgr.setBluetoothDeviceFoundListener(this);
        discoverDevices();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ((CalibrateActivity) getActivity()).disableRightArrow();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calibrate_discover, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.progress);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                discoverDevices();
                break;
            case R.id.menu_stop:
                mScanning = false;
                getActivity().invalidateOptionsMenu();
                BluetoothMgr bluetoothMgr = ((CalibrateActivity) getActivity()).getBluetoothMgr();
                bluetoothMgr.getBluetoothAdapter().cancelDiscovery();
                break;
        }
        return true;
    }

    private void discoverDevices() {
        mScanning = true;
        getActivity().invalidateOptionsMenu();
        if (mDeviceListAdapter != null)
            mDeviceListAdapter.clear();
        mBluetoothMgr.getBluetoothAdapter().startDiscovery();
    }

    @Override
    public void onActiveStateChanged(boolean state) {
        mScanning = state;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onFound(BluetoothDevice d) {
        if (mDeviceListAdapter == null)
            mDeviceListAdapter = new BluetoothDeviceListAdapter(getActivity());
        mDeviceListAdapter.addDevice(d);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onConnected() {
        // TODO
    }

    @Override
    public void onDisconnected() {
        // TODO
    }

}