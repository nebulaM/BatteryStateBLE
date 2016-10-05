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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainDisplayFragment newFragment = new MainDisplayFragment();
        getFragmentManager().beginTransaction().replace(R.id.frag_container, newFragment).addToBackStack(null).commit();


        /*((Button) findViewById(R.id.tbBatteryLevel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                batteryLevel.setBatteryLevel(ts_count+1);
                ts_count++;
            }
        });*/

    }







}
