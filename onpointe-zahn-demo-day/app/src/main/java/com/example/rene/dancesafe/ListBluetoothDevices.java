package com.example.rene.dancesafe;

/*
    This class lists all nearby
    Bluetooth Low Energy Devices (BLE)
*/

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ListBluetoothDevices extends AppCompatActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter; // Adapter to display listView

    private BluetoothAdapter mBluetoothAdapter;       // Adapter to scan/stop scanning for BLE devices

    private boolean mScanning;                        // If true, start scanning for BLE, else don't

    private Handler mHandler;                         // Handle the Runnable (when scanning for BLE)

    private static final int REQUEST_ENABLE_BT = 1;   // To ask user permission to enable bluetooth

    private static final long SCAN_PERIOD = 10000;    // Scan for BLE devices for 10 seconds

    int deviceCount=0;
    Intent deviceControlIntent;



    // Check if BLE is supported
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bluetooth_devices);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        deviceControlIntent = new Intent(ListBluetoothDevices.this, DeviceControlActivity.class);
    }


    // 2 Options - Scan or Stop scanning
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }


    // Scan/Stop for new BLE devices based on chosen options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                //Toast.makeText(this, "Scan", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                //Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }


    // Request permission from user to enable bluetooth if disabled
    // List nearby BLE devices
    // Connect to nearby BLE devices on click
    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        ListView lv = (ListView) findViewById(R.id.list_view_bt);

        mLeDeviceListAdapter = new LeDeviceListAdapter(); // Initializes list view adapter.

        lv.setAdapter(mLeDeviceListAdapter);

        scanLeDevice(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(deviceCount == 0)
                {
                    BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                    if (device == null) return;
                    Toast.makeText(getApplicationContext(), device.getName()+"\n"+device.getAddress(), Toast.LENGTH_SHORT).show();
                    //deviceControlIntent = new Intent(ListBluetoothDevices.this, DeviceControlActivity.class);
                    deviceControlIntent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                    deviceControlIntent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }
                    deviceCount++;
                    Toast.makeText(getApplicationContext(), "Please Connect Second Device to proceed", Toast.LENGTH_SHORT).show();
                }
                else{
                    BluetoothDevice device2 = mLeDeviceListAdapter.getDevice(position);
                    if (device2 == null) return;
                    Toast.makeText(getApplicationContext(), device2.getName()+"\n"+device2.getAddress(), Toast.LENGTH_SHORT).show();
                    deviceControlIntent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME2, device2.getName());
                    deviceControlIntent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS2, device2.getAddress());
                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }
                    startActivity(deviceControlIntent);
                }
            }
        });
    }


    // Return to main activity if user chose not to enable bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // Stop scanning for new BLE devices and clear list
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }


    // If enable == true, run thread for 10 seconds to discover new BLE devices
    // If enable == false, stop scanning for new devices
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ListBluetoothDevices.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.device_name, null);
                viewHolder = new ViewHolder();
                //viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceInfo = (TextView) view.findViewById(R.id.bluetooth_text);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            if(device.getName() != null) {
                final String deviceName = (device.getName().equals("Bean") ? "OnPointe Device" : device.getName());
                final String deviceAddress = device.getAddress();

                if (deviceName != null && deviceName.length() > 0)
                    viewHolder.deviceInfo.setText(deviceName + "\n" + deviceAddress);
                else
                    viewHolder.deviceInfo.setText(R.string.unknown_device + "\n" + deviceAddress);
            }
            return view;
        }
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    // Class holding textView to display device's name and address
    static class ViewHolder {
        TextView deviceInfo;
    }


    // Navigation buttons
    public void home(View view) {
        Intent intent = new Intent(this, RecordPractice2.class);
        startActivity(intent);
    }

    public void bluetooth(View view) {
        Intent intent = new Intent(this, ListBluetoothDevices.class);
        startActivity(intent);
    }

    public void video_recorded(View view) {
        Intent intent = new Intent(this, column_play_back.class);
        startActivity(intent);
    }

    public void video_practiced(View view) {
        Intent intent = new Intent(this, column_practiced.class);
        startActivity(intent);
    }

    public void user_account(View view) {
        Intent intent = new Intent(this, user_account.class);
        startActivity(intent);
    }

}
