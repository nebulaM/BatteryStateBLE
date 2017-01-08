
package com.github.batterystate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class BatteryStatusDisplay extends Fragment {
    private final static String TAG ="BatteryStatusDisplay";
    private TextView mTextBatteryCharge;
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

        mBatteryCharge.setData(0);
        mViewBatteryHealth.setData(0);



        return view;
    }

    protected void updateUI(int errorCode, int batteryLevel, int batteryHealth){

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
                mTextBatteryCharge.setText("Wrong Device");
                mTextBatteryHealth.setText("");
                break;
            case 2:
                mBatteryCharge.setData(0);
                mViewBatteryHealth.setData(0);
                mTextBatteryCharge.setText("Not Connected");
                mTextBatteryHealth.setText("");
                break;
            case 3:
                mBatteryCharge.setData(0);
                mViewBatteryHealth.setData(0);
                mTextBatteryCharge.setText("Connected");
                mTextBatteryHealth.setText("");
                break;
            default:
                break;
        }
    }

    private String bound(String in){
        if(Integer.parseInt(in)>100){
            return String.valueOf(100);
        }else if(Integer.parseInt(in)<0){
            return String.valueOf(0);
        }else {
            return in;
        }
    }
}
