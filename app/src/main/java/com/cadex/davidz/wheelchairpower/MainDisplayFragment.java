package com.cadex.nebulaM.wheelchairpower;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cadex.nebulaM.wheelchairpower.customviews.ViewBatteryHealth;
import com.cadex.nebulaM.wheelchairpower.customviews.ViewBatteryLevel;

/**
 * Created by nebulaM on 10/4/2016.
 */
public class MainDisplayFragment extends Fragment {
    private TextView mTextBatteryLevelPercent;
    private int mBatteryLevelData, mBatteryHealthData;

    private ViewBatteryLevel mViewBatteryLevel;
    private ViewBatteryHealth mViewBatteryHealth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_status, container, false);
        mViewBatteryLevel = (ViewBatteryLevel) view.findViewById(R.id.ViewBatteryLevel);
        mViewBatteryHealth = (ViewBatteryHealth) view.findViewById(R.id.ViewBatteryHealth);
        mTextBatteryLevelPercent = (TextView) view.findViewById(R.id.textViewBatLevelPercent);

        mViewBatteryLevel.setBatteryLevel(mBatteryLevelData);
        mViewBatteryHealth.setBatteryHealth(mBatteryHealthData);
        mTextBatteryLevelPercent.setText(String.valueOf(mBatteryLevelData) + "%");


        return view;
    }

    protected void updateUI(String inBatteryLevel){
        int batteryLevel=Integer.parseInt(inBatteryLevel);
        if(batteryLevel>100 || batteryLevel<0){
            mViewBatteryLevel.setBatteryLevel(0);
            mTextBatteryLevelPercent.setText("Wrong Device");
        }
        else {
            mViewBatteryLevel.setBatteryLevel(batteryLevel);
            mTextBatteryLevelPercent.setText(inBatteryLevel + "%");
        }
    }

}
