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
    private TextView batteryLevelPercent;
    private int mBatteryLevelData, mBatteryHealthData;

    private ViewBatteryLevel mViewBatteryLevel;
    private ViewBatteryHealth mViewBatteryHealth;
    //set to false and delete useless buttons in battery_status.xml after finishing this part
    //take care of the value assigned to mBatteryLevelData and mBatteryHealthData
    private final boolean test=true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_status, container, false);
        mViewBatteryLevel = (ViewBatteryLevel) view.findViewById(R.id.ViewBatteryLevel);
        mViewBatteryHealth = (ViewBatteryHealth) view.findViewById(R.id.ViewBatteryHealth);
        batteryLevelPercent = (TextView) view.findViewById(R.id.textViewBatLevelPercent);
        if(test) {
            mBatteryLevelData = 30;
            mBatteryHealthData = 100;
        }
        mViewBatteryLevel.setBatteryLevel(mBatteryLevelData);
        mViewBatteryHealth.setBatteryHealth(mBatteryHealthData);
        batteryLevelPercent.setText(String.valueOf(mBatteryLevelData) + "%");
        //following are tests, suppose to put updated battery level here
        if(test) {
            Button displayBatteryLevel = (Button) view.findViewById(R.id.displayBatteryLevel);
            displayBatteryLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBatteryLevelData > 0)
                        mBatteryLevelData -= 5;
                    mViewBatteryLevel.setBatteryLevel(mBatteryLevelData);
                    batteryLevelPercent.setText(String.valueOf(mBatteryLevelData) + "%");
                }
            });

            Button displayBatteryLevel2 = (Button) view.findViewById(R.id.displayBatteryLevel2);
            displayBatteryLevel2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBatteryLevelData < 100)
                        mBatteryLevelData += 5;
                    mViewBatteryLevel.setBatteryLevel(mBatteryLevelData);
                    batteryLevelPercent.setText(String.valueOf(mBatteryLevelData) + "%");
                }
            });
            Button displayBatteryHealth = (Button) view.findViewById(R.id.displayBatteryHealth);
            displayBatteryHealth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBatteryHealthData > 0)
                        mBatteryHealthData -= 5;
                    mViewBatteryHealth.setBatteryHealth(mBatteryHealthData);
                }
            });

            Button displayBatteryHealth2 = (Button) view.findViewById(R.id.displayBatteryHealth2);
            displayBatteryHealth2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBatteryHealthData < 100)
                        mBatteryHealthData += 5;
                    mViewBatteryHealth.setBatteryHealth(mBatteryHealthData);
                }
            });
        }




        return view;
    }



}
