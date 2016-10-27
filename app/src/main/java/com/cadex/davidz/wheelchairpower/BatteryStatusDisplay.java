package com.cadex.nebulaM.wheelchairpower;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cadex.nebulaM.wheelchairpower.customviews.ViewBatteryHealth;
import com.cadex.nebulaM.wheelchairpower.customviews.ViewBatteryLevel;

import static android.content.ContentValues.TAG;

/**
 * Created by nebulaM on 10/4/2016.
 */
public class BatteryStatusDisplay extends Fragment {
    private final static String TAG ="BatteryStatusDisplay";
    private TextView mTextBatteryLevelPercent;

    private ViewBatteryLevel mViewBatteryLevel;
    private ViewBatteryHealth mViewBatteryHealth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_status, container, false);
        mViewBatteryLevel = (ViewBatteryLevel) view.findViewById(R.id.ViewBatteryLevel);
        mViewBatteryHealth = (ViewBatteryHealth) view.findViewById(R.id.ViewBatteryHealth);
        mTextBatteryLevelPercent = (TextView) view.findViewById(R.id.textViewBatLevelPercent);

        mViewBatteryLevel.setBatteryLevel(0);
        mViewBatteryHealth.setBatteryHealth(0);
        mTextBatteryLevelPercent.setText(String.valueOf(0) + "%");


        return view;
    }

    protected void updateUI(String dataIn){
        Log.d(TAG,"input data string to battery UI:"+dataIn);
        String[] dataSet = dataIn.split(",");
        int errorCode=Integer.parseInt(dataSet[0]);
        int batteryLevel=Integer.parseInt(dataSet[1]);
        int batteryHealth=Integer.parseInt(dataSet[2]);
        if(errorCode==0){
            mViewBatteryLevel.setBatteryLevel(batteryLevel);
            mViewBatteryHealth.setBatteryHealth(batteryHealth);
            mTextBatteryLevelPercent.setText(dataSet[1] + "%");
        }
        else if(errorCode==1){
            mViewBatteryLevel.setBatteryLevel(0);
            mViewBatteryHealth.setBatteryHealth(0);
            mTextBatteryLevelPercent.setText("Wrong Device");
        }
        else if(errorCode==2){
            mViewBatteryLevel.setBatteryLevel(0);
            mViewBatteryHealth.setBatteryHealth(0);
            mTextBatteryLevelPercent.setText("Not Connected");
        }
        else if(errorCode==3){
            mViewBatteryLevel.setBatteryLevel(0);
            mViewBatteryHealth.setBatteryHealth(0);
            mTextBatteryLevelPercent.setText("Connected");
            return;
        }

    }

}
