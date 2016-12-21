
package com.github.batteryState;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.batteryState.R;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    //hardcode to our bluetooth server for now
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BatteryStatusDisplay mBatteryDisplayFragment;
    private boolean mSubscribe=false;
    //an integer >0
    private final int REQUEST_ENABLE_BT=1;

    private String BLEDataIn;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
                mBatteryDisplayFragment.updateUI("3,0,0");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                //errorCode=2, not connected
                mBatteryDisplayFragment.updateUI("2,0,0");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //errorCode=0
                String dataIn=intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SET);
                int[] arr=new int[4];

                String[] dataSet = dataIn.split(",");
                if(dataSet.length>=7) {
                    for(int i=0;i<4;++i) {
                        int data= Integer.parseInt(dataSet[3+i]);
                        if(data<0){
                            data=256+data;
                        }
                        arr[i]=data;
                    }
                    Toast.makeText(getApplicationContext(),  arr[0] + "." + arr[1] + "." + arr[2] + "." + arr[3],
                            Toast.LENGTH_LONG).show();
                }
                mBatteryDisplayFragment.updateUI(dataIn);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // getActionBar().setTitle(mDeviceName);
        // getActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializes Bluetooth adapter.
       /* final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }*/
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mBatteryDisplayFragment = new BatteryStatusDisplay();
        getFragmentManager().beginTransaction().add(R.id.frag_container, mBatteryDisplayFragment).commit();

        ImageButton readBLE = (ImageButton) findViewById(R.id.readBLE);
        readBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic characteristic=mBluetoothLeService.getGattCharacteristic(mBluetoothLeService.UUID_Battery_Service,mBluetoothLeService.UUID_Battery_Level_Percent);
                if(characteristic==null){
                    //errorCode=1,wrong device
                    mBatteryDisplayFragment.updateUI("1,0,0");
                }
                else {
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
            }
        });

        ImageButton subscribeBLE = (ImageButton) findViewById(R.id.setNotification);
        subscribeBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic characteristic=mBluetoothLeService.getGattCharacteristic(mBluetoothLeService.UUID_Battery_Service,mBluetoothLeService.UUID_Battery_Level_Percent);
                if(characteristic==null){
                    //errorCode=1,wrong device
                    mBatteryDisplayFragment.updateUI("1,0,0");
                }
                else {
                    mBluetoothLeService.setCharacteristicNotification(characteristic, mSubscribe);
                    if(mSubscribe){
                        mSubscribe=false;
                    }
                    else{
                        mSubscribe=true;
                    }
                }

            }
        });
        //TODO:Resolve this crash
        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothGattCharacteristic characteristic=mBluetoothLeService.getGattCharacteristic(mBluetoothLeService.UUID_Battery_Service,mBluetoothLeService.UUID_Battery_Level_Percent);
                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
            }
        },1000);*/



    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.w(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothGattCharacteristic characteristic=mBluetoothLeService.getGattCharacteristic(mBluetoothLeService.UUID_Battery_Service,mBluetoothLeService.UUID_Battery_Level_Percent);
        if(characteristic!=null) {
            mBluetoothLeService.setCharacteristicNotification(characteristic, false);
        }
        // mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
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
        switch(item.getItemId()) {
            case R.id.menu_connect:
                if(mBluetoothLeService==null) {
                    Log.d(TAG, "mBluetoothLeService is null ");
                }
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
