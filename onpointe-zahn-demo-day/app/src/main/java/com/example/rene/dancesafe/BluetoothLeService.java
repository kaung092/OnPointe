package com.example.rene.dancesafe;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;

    public static String mBluetoothDeviceAddress ="";
    public static String mBluetoothDeviceAddress2="";

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGatt mBluetoothGatt2;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;

    private static final int STATE_CONNECTING = 1;

    private static final int STATE_CONNECTED = 2;


    //First
    public final static String ACTION_GATT_CONNECTED_1 =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED_1";

    public final static String ACTION_GATT_DISCONNECTED_1 =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED_1";

    public final static String ACTION_GATT_SERVICES_DISCOVERED_1 =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED_1";

    public final static String ACTION_DATA_AVAILABLE_1 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_1";

    //Second
    public final static String ACTION_GATT_CONNECTED_2 =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED_2";

    public final static String ACTION_GATT_DISCONNECTED_2 =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED_2";

    public final static String ACTION_GATT_SERVICES_DISCOVERED_2 =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED_2";

    public final static String ACTION_DATA_AVAILABLE_2 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_2";




    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public final static UUID UUID_MYO_SERVICE = UUID.fromString(SampleGattAttributes.MYO_SERVICE);

    public final static UUID UUID_MYO_CHARACTERISTIC = UUID.fromString(SampleGattAttributes.MYO_CHARACTERISTIC);

    public final static UUID UUID_BEAN_SERVICE = UUID.fromString(SampleGattAttributes.BEAN_SERVICE);

    public final static UUID UUID_BEAN_CHARACTERISTIC = UUID.fromString(SampleGattAttributes.BEAN_CHARACTERISTIC);

    public final static UUID CONFIG_DESCRIPTOR = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        // Read BLE device's data
        private void readData(BluetoothGatt gatt) {

            BluetoothGattCharacteristic characteristic;

            characteristic = gatt.getService(UUID_BEAN_SERVICE).getCharacteristic(UUID_BEAN_CHARACTERISTIC);

            gatt.readCharacteristic(characteristic);

            Log.d("BEAN", "Enabling Data Collection ...");
        }

        // Notify app when data in BLE device has changed
        private void setNotifyNextData(BluetoothGatt gatt) {

            BluetoothGattCharacteristic characteristic;

            characteristic = gatt.getService(UUID_BEAN_SERVICE).getCharacteristic(UUID_BEAN_CHARACTERISTIC);

            // Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);

            // Enable remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);

            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            gatt.writeDescriptor(desc);
            Log.d("BEAN", "setNotifyNextData working");
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            String intentAction;
            String address = gatt.getDevice().getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                intentAction = (address.equals(mBluetoothDeviceAddress)? ACTION_GATT_CONNECTED_1:ACTION_GATT_CONNECTED_2);
                mConnectionState = STATE_CONNECTED;

                broadcastUpdate(intentAction);

                Log.i(TAG, "Connected to GATT server :" + address);
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices() + " and " + mBluetoothGatt2.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                //intentAction = ACTION_GATT_DISCONNECTED_1;
                intentAction = (address.equals(mBluetoothDeviceAddress)? ACTION_GATT_DISCONNECTED_1:ACTION_GATT_DISCONNECTED_2);
                mConnectionState = STATE_DISCONNECTED;

                Log.i(TAG, "Disconnected from GATT server address: " + address);

                broadcastUpdate(intentAction);
               // broadcastUpdate("com.example.bluetooth.le."+address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            String address = gatt.getDevice().getAddress();

            if (status == BluetoothGatt.GATT_SUCCESS) {

                String intentAction = (address==mBluetoothDeviceAddress)?ACTION_GATT_SERVICES_DISCOVERED_1:ACTION_GATT_SERVICES_DISCOVERED_2;
                broadcastUpdate(intentAction);
                readData(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String intentAction = (address==mBluetoothDeviceAddress)?ACTION_DATA_AVAILABLE_1:ACTION_DATA_AVAILABLE_2;
                //readData(gatt);
                broadcastUpdate(intentAction, characteristic);

                setNotifyNextData(gatt);

                Log.d("BEAN", "onCharacteristicRead working");
                Log.d("BEAN","Status: "+status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE_1, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String address = gatt.getDevice().getAddress();
            String intentAction = (address==mBluetoothDeviceAddress)?ACTION_DATA_AVAILABLE_1:ACTION_DATA_AVAILABLE_2;
            broadcastUpdate(intentAction, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {

            readData(gatt);
        }

    };


    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        Log.d("BEAN", "BroadcastUpdate is called");

        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {

            int flag = characteristic.getProperties();

            int format = -1;

            if ((flag & 0x01) != 0) {

                format = BluetoothGattCharacteristic.FORMAT_UINT16;

                Log.d(TAG, "Heart rate format UINT16.");
            } else {

                format = BluetoothGattCharacteristic.FORMAT_UINT8;

                Log.d(TAG, "Heart rate format UINT8.");
            }

            final int heartRate = characteristic.getIntValue(format, 1);

            Log.d(TAG, String.format("Received heart rate: %d", heartRate));

            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));

        } else if (UUID_MYO_CHARACTERISTIC.equals(characteristic.getUuid())) {

            Log.d(TAG, "BroadcastUpdate writing data BEAN");

            int flag = characteristic.getProperties();

            int format = -1;

            if ((flag & 0x01) != 0) {

                format = BluetoothGattCharacteristic.FORMAT_UINT16;

                Log.d(TAG, "MYO format UINT16.");
            } else {

                format = BluetoothGattCharacteristic.FORMAT_UINT8;

                Log.d(TAG, "MYO format UINT8.");
            }

            final int myo = characteristic.getIntValue(format, 1);

            Log.d(TAG, String.format("MYO data received: %d", myo));

            intent.putExtra(EXTRA_DATA, String.valueOf(myo));

        } else if (UUID_BEAN_CHARACTERISTIC.equals(characteristic.getUuid())) {

            int flag = characteristic.getProperties();

            int format = -1;

            if ((flag & 0x01) != 0) {

                format = BluetoothGattCharacteristic.FORMAT_UINT16;

                Log.d(TAG, "Heart rate format UINT16.");
            } else {

                format = BluetoothGattCharacteristic.FORMAT_UINT8;

                Log.d(TAG, "Heart rate format UINT8.");
            }

            final int bean = characteristic.getIntValue(format, 1);

            Log.d("Characteristic.getValue", String.format("Received heart rate:"+String.valueOf(bean) +" %d", bean));

            intent.putExtra(EXTRA_DATA, String.valueOf(bean));

        } else {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }

        sendBroadcast(intent);
    }


    public class LocalBinder extends Binder {

        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {

        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();

        return super.onUnbind(intent);
    }


    private final IBinder mBinder = new LocalBinder();


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {

            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManager == null) {

                Log.e(TAG, "Unable to initialize BluetoothManager.");

                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {

            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");

            return false;
        }

        return true;
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address, final String address2) {

        if (mBluetoothAdapter == null || address == null) {

            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");

            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {

            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

            if (mBluetoothGatt.connect()) {

                mConnectionState = STATE_CONNECTING;

                return true;
            } else {

                return false;
            }
        }
        if (mBluetoothDeviceAddress2 != null && address.equals(mBluetoothDeviceAddress2)
                && mBluetoothGatt2 != null) {

            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

            if (mBluetoothGatt2.connect()) {

                mConnectionState = STATE_CONNECTING;

                return true;
            } else {

                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        final BluetoothDevice device2 = mBluetoothAdapter.getRemoteDevice(address2);

        if (device == null ) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        if (device2 == null ) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothGatt2 = device2.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");

        mBluetoothDeviceAddress = address;
        mBluetoothDeviceAddress2 = address2;

        mConnectionState = STATE_CONNECTING;

        return true;
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {

        if (mBluetoothAdapter == null || mBluetoothGatt == null || mBluetoothGatt2 == null) {

            Log.w(TAG, "BluetoothAdapter not initialized");

            return;
        }

        mBluetoothGatt.disconnect();
        mBluetoothGatt2.disconnect();
    }


    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {

        if (mBluetoothGatt == null) {
            return;
        }
        if (mBluetoothGatt2 == null) {
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt2.close();
        mBluetoothGatt = null;
        mBluetoothGatt2 = null;

    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null || mBluetoothGatt2 == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }


        Log.d("BEAN", "readCharacteristic Original");

        mBluetoothGatt.readCharacteristic(characteristic);
        mBluetoothGatt2.readCharacteristic(characteristic);

    }

    /**
     * Request a write to a given {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to write to.
     */

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {

            Log.w(TAG, "BluetoothAdapter not initialized");

            return;
        }

        Log.d("BEAN","readCharacteristic Original");

        mBluetoothGatt.writeCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a given characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {

            Log.w(TAG, "BluetoothAdapter not initialized");

            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        mBluetoothGatt2.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            mBluetoothGatt.writeDescriptor(descriptor);
            mBluetoothGatt2.writeDescriptor(descriptor);
        }

        if (UUID_MYO_CHARACTERISTIC.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            mBluetoothGatt.writeDescriptor(descriptor);
            mBluetoothGatt2.writeDescriptor(descriptor);
        }

        if (UUID_BEAN_CHARACTERISTIC.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            mBluetoothGatt.writeDescriptor(descriptor);
            mBluetoothGatt2.writeDescriptor(descriptor);
        }
    }


    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {

        if (mBluetoothGatt == null || mBluetoothGatt2 == null) return null;

        List<BluetoothGattService> listService = mBluetoothGatt.getServices();
        listService.addAll(mBluetoothGatt2.getServices());

        return listService;
    }
}
