/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.batterystate;

import android.app.NotificationManager;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "0";
    public final static String EXTRA_DATA_SET = "0,0";

    public final static UUID UUID_Battery_Service=UUID.fromString(SampleGattAttributes.Battery_Service);

    public final static UUID UUID_Battery_Level_Percent =UUID.fromString(SampleGattAttributes.Battery_Level_Percent);

    private long mLastTimeNotify;
    private long mLastTimeSendToCloud;

    private final long NOTIFY_INTERVAL=3000002;

    private final long SEND_TO_CLOUD_PERIOD=600000;
    private boolean firstTime2Cloud;
    private BatteryObject mBatteryData2AWS;
    private DynamoDBMapper mapperAWSDB;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        // Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate(){
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                //TODO:Add Pool ID
                "", // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        ddbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        mapperAWSDB = new DynamoDBMapper(ddbClient);

    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if(UUID_Battery_Level_Percent.equals(characteristic.getUuid())){
            //Log.d(TAG, "battery data format UINT8.");
            byte[] dataSet=characteristic.getValue();
            //error from BLE server

            if(dataSet[0]>0){
                return;
            }
            Log.d(TAG, "Received battery percent: "+dataSet[7]);
            Log.d(TAG, "Received battery health: "+dataSet[8]);
            Log.d(TAG, "Received TTE/TTF: "+dataSet[9]);

            //length is data set plus error code
            StringBuilder sb=new StringBuilder();

            //1-6 unique ID not append to this sb, 7 level, 8 health, 9-10 TTE/TTF 11-12 current 13-14 volt
            for(int i=7;i<dataSet.length;i++){
                sb.append(dataSet[i]);
                sb.append(',');
            }
            sb.setLength(sb.length()-1);
            //Log.d(TAG,"@broadcastUpdate String is "+sb.toString());
            //battery level
            if(dataSet[7]<=80){
                if(System.currentTimeMillis()-mLastTimeNotify>NOTIFY_INTERVAL){
                    Log.d(TAG,"show Notification!");

                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            v.vibrate(800);
                        }
                    });
                    t.start();
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_low_battery)
                            .setContentTitle(getText(R.string.notify_low_bat_title))
                            .setContentText(getText(R.string.notify_low_bat_text));
                    NotificationManager mNotifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(1, mBuilder.build());

                    try{t.join();}catch (InterruptedException e){e.printStackTrace();}

                    mLastTimeNotify=System.currentTimeMillis();

                }
            }

            //send to cloud server
            if(isNetworkAvailable() &&
                    ((firstTime2Cloud&&System.currentTimeMillis()-mLastTimeSendToCloud>10000)
                            || (System.currentTimeMillis()-mLastTimeSendToCloud>SEND_TO_CLOUD_PERIOD))) {
                StringBuilder sbSerialNumber=new StringBuilder(6);
                for(int i=1;i<=6;i++){
                    sbSerialNumber.append(dataSet[i]);
                }
                final String serialNumber=sbSerialNumber.toString();
                final String charge=Integer.toString((int)dataSet[7]);
                final String health=Integer.toString((int)dataSet[8]);
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendToCloud(serialNumber,charge,health);
                    }
                });
                t.start();
                mLastTimeSendToCloud=System.currentTimeMillis();
                firstTime2Cloud=false;
            }

            intent.putExtra(EXTRA_DATA_SET, sb.toString());

        }
        else {
            // For all other profiles, writes the data formatted in HEX.
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
    protected static Calendar c = Calendar.getInstance();
    protected static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private void sendToCloud(String serialNumber, String charge, String health){
        if (mBatteryData2AWS == null) {
            mBatteryData2AWS = new BatteryObject();
        }
        try {
            mBatteryData2AWS.setSerialNum(serialNumber);
            mBatteryData2AWS.setCharge(charge);
            mBatteryData2AWS.setHealth(health);
            mBatteryData2AWS.setUpdate(df.format(c.getTime()));
            mapperAWSDB.save(mBatteryData2AWS);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

        //give some time to wait for battery data gets stable
        mLastTimeNotify=System.currentTimeMillis();
        mLastTimeSendToCloud=System.currentTimeMillis();
        firstTime2Cloud=true;

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
    public boolean connect(final String address) {
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

                //give some time to wait for battery data gets stable
                mLastTimeNotify=System.currentTimeMillis();
                mLastTimeSendToCloud=System.currentTimeMillis();
                firstTime2Cloud=true;

                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        ParcelUuid[] uuids=device.getUuids();
        Log.e(TAG,"Device name is "+ device.getName());
        //Log.e(TAG,"Device uuids "+ uuids.length);
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
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
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Battery Data
        if (UUID_Battery_Level_Percent.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

            Log.d(TAG,UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG).toString());


            if(enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (mBluetoothGatt.writeDescriptor(descriptor)) {
                    Log.d(TAG, "subscribed battery service");
                    return true;
                } else {
                    Log.e(TAG, "cannot subscribed battery service");
                    return false;
                }
            }
            else{
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                if (mBluetoothGatt.writeDescriptor(descriptor)) {
                    Log.d(TAG, "Un subscribed battery service");
                    return true;
                } else {
                    Log.e(TAG, "cannot un subscribed battery service");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public BluetoothGattService getGattService(UUID uuid){
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getService(uuid);
    }

    public BluetoothGattCharacteristic getGattCharacteristic(UUID service, UUID characteristic) {
        if (mBluetoothGatt == null){
            Log.e(TAG,"bluetoothGatt not found when attempt to getGattCharacteristic");
            return null;
        }
        BluetoothGattService Service = mBluetoothGatt.getService(service);
        if(Service==null){
            Log.e(TAG,"Service is "+service);
            Log.e(TAG,"Service not found when attempt to getGattCharacteristic");
            Log.e(TAG,"If you are sure that you connected to the proper device, then please restart bluetooth on your phone.");
            return null;
        }
        BluetoothGattCharacteristic charac = Service.getCharacteristic(characteristic);
        if (charac == null) {
            Log.e(TAG, "char not found when attempt to getGattCharacteristic");
            return null;
        }
        return charac;
    }
}