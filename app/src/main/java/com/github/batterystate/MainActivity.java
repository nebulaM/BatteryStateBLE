
package com.github.batterystate;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity{
    private final static String TAG = MainActivity.class.getSimpleName();
    //hardcode to our bluetooth server for now
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DEVICE_NAME="Battery";
    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BatteryStatusDisplay mBatteryDisplayFragment;
    private boolean mSubscribe=true;

    protected Toast mToast;

    protected static ThemeDialog mThemeDialog;
    private SharedPreferences mSP;
    public static final String SP_FILE_NAME ="BSSP";
    public static final String SP_KEY_THEME="SP_KEY_THEME";
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
                //mBatteryDisplayFragment.updateUI(3,"");
                //auto subscribe BLE channel
                mSubscribe=true;
                try{TimeUnit.SECONDS.sleep(1);}catch (InterruptedException e){e.printStackTrace();}
                subscribeBLE();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                //errorCode=2, not connected
                mBatteryDisplayFragment.updateUI(2,"");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //errorCode=0
                String dataIn=intent.getStringExtra(BluetoothLeService.EXTRA_DATA_SET);

                mBatteryDisplayFragment.updateUI(0,dataIn);

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
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
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
        mBatteryDisplayFragment.setButtonListener(new BatteryStatusDisplay.buttonListener() {
            @Override
            public void onClick(String action) {
                if(action.equals("readBLE")){
                    BluetoothGattCharacteristic characteristic=mBluetoothLeService.getGattCharacteristic(mBluetoothLeService.UUID_Battery_Service,mBluetoothLeService.UUID_Battery_Level_Percent);
                    if(characteristic==null){
                        //errorCode=1,wrong device
                        mBatteryDisplayFragment.updateUI(1,"");
                    }
                    else {
                        String ip=mBluetoothLeService.getIP();
                        if(ip!=null && !ip.equals("")){
                            mToast.setText(ip);
                            mToast.setDuration(Toast.LENGTH_LONG);
                            mToast.show();
                            mToast.setDuration(Toast.LENGTH_SHORT);
                        }
                        mBluetoothLeService.readCharacteristic(characteristic);

                    }
                }else if(action.equals("subBLE")){
                    subscribeBLE();
                }
            }
        });

        final View container=this.findViewById(R.id.frag_container);
        mSP=getSharedPreferences(MainActivity. SP_FILE_NAME, MODE_PRIVATE);
        setTheme(mSP.getInt(SP_KEY_THEME,0), container);



        mThemeDialog=new ThemeDialog();
        mThemeDialog.setOnCloseListener(new ThemeDialog.onCloseListener(){
            @Override
            public void onDialogClose(String tag, int parameter){
                mSP.edit().putInt(SP_KEY_THEME,parameter).apply();
                setTheme(parameter,container);
            }
        });


        getFragmentManager().beginTransaction().add(R.id.frag_container, mBatteryDisplayFragment).commit();

        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSubscribe=true;
                if(!subscribeBLE()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.connectFail)
                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        },5000);*/

    }

    private void setTheme(int themeID, View v){
        switch (themeID){
            case 0:
                v.setBackgroundResource(R.color.theme_0);
                break;
            case 1:
                v.setBackgroundResource(R.color.theme_1);
                break;
            case 2:
                v.setBackgroundResource(R.color.theme_2);
                break;
            case 3:
                v.setBackgroundResource(R.color.theme_3);
                break;
            default:
                break;
        }
    }

    private boolean subscribeBLE(){
        BluetoothGattCharacteristic characteristic=mBluetoothLeService.getGattCharacteristic(mBluetoothLeService.UUID_Battery_Service,mBluetoothLeService.UUID_Battery_Level_Percent);
        if(characteristic==null){
            //errorCode=1,wrong device
            mBatteryDisplayFragment.updateUI(1,"");
            return false;
        }
        else {
            if(mBluetoothLeService.setCharacteristicNotification(characteristic, mSubscribe)) {
                if (mSubscribe) {
                    mToast.setText(R.string.subscribe);
                    mToast.show();

                } else {
                    mToast.setText(R.string.unSubscribe);
                    mToast.show();
                }
                mSubscribe=!mSubscribe;
                return true;
            }else {
                if (mSubscribe) {
                    mToast.setText("Cannot subscribe, try again");
                    mToast.show();

                } else {
                    mToast.setText("Cannot un-subscribe, try again");
                    mToast.show();
                }
                mToast.show();
                return false;
            }
        }
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
                if(!mBluetoothLeService.connect(mDeviceAddress)){

                    mToast.setText("Device address is "+ mDeviceAddress+". "+getText(R.string.connectFailToast));
                    mToast.show();
                }
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
