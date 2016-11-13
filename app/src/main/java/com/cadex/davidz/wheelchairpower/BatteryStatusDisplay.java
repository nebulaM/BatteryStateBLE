/*Copyright 2016 nebulaM 
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
    private TextView mTextBatteryHealthPercent;

    private ViewBatteryLevel mViewBatteryLevel;
    private ViewBatteryHealth mViewBatteryHealth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_status, container, false);
        mViewBatteryLevel = (ViewBatteryLevel) view.findViewById(R.id.ViewBatteryLevel);
        mViewBatteryHealth = (ViewBatteryHealth) view.findViewById(R.id.ViewBatteryHealth);
        mTextBatteryLevelPercent = (TextView) view.findViewById(R.id.textViewBatLevelPercent);
        mTextBatteryHealthPercent =(TextView) view.findViewById(R.id.textViewBatHealthPercent);

        mViewBatteryLevel.setBatteryLevel(0);
        mViewBatteryHealth.setBatteryHealth(0);



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
            mTextBatteryHealthPercent.setText(dataSet[2] + "%");
        }
        else if(errorCode==1){
            mViewBatteryLevel.setBatteryLevel(0);
            mViewBatteryHealth.setBatteryHealth(0);
            mTextBatteryLevelPercent.setText("Wrong Device");
            mTextBatteryHealthPercent.setText("");
        }
        else if(errorCode==2){
            mViewBatteryLevel.setBatteryLevel(0);
            mViewBatteryHealth.setBatteryHealth(0);
            mTextBatteryLevelPercent.setText("Not Connected");
            mTextBatteryHealthPercent.setText("");
        }
        else if(errorCode==3){
            mViewBatteryLevel.setBatteryLevel(0);
            mViewBatteryHealth.setBatteryHealth(0);
            mTextBatteryLevelPercent.setText("Connected");
            mTextBatteryHealthPercent.setText("");
            return;
        }

    }

}
