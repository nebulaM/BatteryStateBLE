package com.cadex.nebulaM.wheelchairpower;

import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cadex.nebulaM.wheelchairpower.customviews.ViewBatteryHealth;
import com.cadex.nebulaM.wheelchairpower.customviews.ViewBatteryLevel;

public class MainActivity extends AppCompatActivity {
    private TextView batteryLevelPercent;
    private int ts_count,ts_count2;
    private ViewFlipper viewFlipper;
    private  ViewBatteryLevel mViewBatteryLevel;
    private ViewBatteryHealth mViewBatteryHealth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_status);

        viewFlipper= (ViewFlipper) findViewById(R.id.viewFlipperMainActivity);
        mViewBatteryLevel =(ViewBatteryLevel)this.findViewById(R.id.ViewBatteryLevel);
        mViewBatteryHealth=(ViewBatteryHealth)this.findViewById(R.id.ViewBatteryHealth);
        batteryLevelPercent=(TextView) findViewById(R.id.textViewBatLevelPercent);
        ts_count=30;
        ts_count2=100;
        mViewBatteryLevel.setBatteryLevel(ts_count);
        mViewBatteryHealth.setBatteryHealth(ts_count2);
        batteryLevelPercent.setText(String.valueOf(ts_count)+"%");

        /*((Button) findViewById(R.id.tbBatteryLevel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                batteryLevel.setBatteryLevel(ts_count+1);
                ts_count++;
            }
        });*/

    }


    //this is a test, suppose to put updated battery level here
    public void displayBatteryLevel(View view) {
        if(ts_count>0)
            ts_count-=5;
        mViewBatteryLevel.setBatteryLevel(ts_count);
        batteryLevelPercent.setText(String.valueOf(ts_count)+"%");
    }
    public void displayBatteryLevel2(View view) {
        if(ts_count<100)
            ts_count+=5;
        mViewBatteryLevel.setBatteryLevel(ts_count);
        batteryLevelPercent.setText(String.valueOf(ts_count)+"%");
    }


    public void displayBatteryHealth(View view) {
        if(ts_count2>0)
            ts_count2-=5;
        mViewBatteryHealth.setBatteryHealth(ts_count2);

    }
    public void displayBatteryHealth2(View view) {
        if(ts_count2<100)
            ts_count2+=5;
        mViewBatteryHealth.setBatteryHealth(ts_count2);

    }




}
