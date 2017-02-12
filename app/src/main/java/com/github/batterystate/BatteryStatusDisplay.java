
package com.github.batterystate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class BatteryStatusDisplay extends Fragment {
    private final static String TAG ="BatteryStatusDisplay";
    private TextView mTextBatteryCharge;
    private TextView mTextBatteryChargeTitle;
    private TextView mTextBatteryHealth;

    private DonutView mBatteryCharge;
    private DonutView mViewBatteryHealth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_status, container, false);
        mBatteryCharge = (DonutView) view.findViewById(R.id.ViewBatteryLevel);
        mViewBatteryHealth = (DonutView) view.findViewById(R.id.ViewBatteryHealth);
        mTextBatteryCharge = (TextView) view.findViewById(R.id.textViewBatLevelPercent);
        mTextBatteryHealth =(TextView) view.findViewById(R.id.textViewBatHealthPercent);

        mTextBatteryChargeTitle= (TextView) view.findViewById(R.id.textViewBatLevelTitle);

        return view;
    }

    protected void updateUI(int errorCode, int batteryLevel, int batteryHealth){
        if(errorCode==0){
            mTextBatteryCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,56);
            mTextBatteryChargeTitle.setText(getText(R.string.charge));
        }else{
            mTextBatteryCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            mTextBatteryChargeTitle.setText("");
        }
        Log.d(TAG,"@updateUI, level is "+batteryLevel+" health is "+batteryHealth);
        switch (errorCode){
            case 0:
                mBatteryCharge.setData(batteryLevel);
                mViewBatteryHealth.setData(batteryHealth);
                mTextBatteryCharge.setText(bound(Integer.toString(batteryLevel)));
                mTextBatteryHealth.setText(bound(Integer.toString(batteryHealth)));
                break;
            case 1:
                mBatteryCharge.setData(0);
                mViewBatteryHealth.setData(0);
                mTextBatteryCharge.setText(getText(R.string.wrong_device));
                mTextBatteryHealth.setText("");
                break;
            case 2:
                mBatteryCharge.setData(0);
                mViewBatteryHealth.setData(0);
                mTextBatteryCharge.setText(getText(R.string.no_connection));
                mTextBatteryHealth.setText("");
                break;
            case 3:
                mBatteryCharge.setData(0);
                mViewBatteryHealth.setData(0);
                mTextBatteryCharge.setText(getText(R.string.connected));
                mTextBatteryHealth.setText("");
                break;
            default:
                break;
        }
    }

    protected static String bound(String in){
        int data=Integer.parseInt(in);
        if(data<0){
            data+=256;
        }
        if(data>100){
            return String.valueOf(100);
        }else if(data<0){
            return String.valueOf(0);
        }else {
            return in;
        }
    }
}
