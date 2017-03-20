
package com.github.batterystate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class BatteryStatusDisplay extends Fragment {
    private final static String TAG ="BatteryStatusDisplay";
    interface buttonListener{
        void onClick(String action);
    }
    private buttonListener mButtonListener;
    private TextView mTextCharge;
    private TextView mTextChargeTitle;
    private TextView mTextHealth;
    private TextView mTextHealthTitle;
    private TextView mTextTTE;

    private DonutView mCharge;
    private DonutView mHealth;

    private int DISPLAY_CHARGE =0;
    private int DISPLAY_HEALTH =0;

    private String tmpIn="";

    private int testCharge=0;

    private final boolean TEST=false;

    //clean tmpIn in 5 seconds
    private final long tmpInTimeOut=5000;

    private long tmpInLastUpdate;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_status, container, false);
        mCharge = (DonutView) view.findViewById(R.id.ViewBatteryLevel);
        mHealth = (DonutView) view.findViewById(R.id.ViewBatteryHealth);


        mCharge.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DISPLAY_CHARGE = DISPLAY_CHARGE <2? DISPLAY_CHARGE +1:0;
                if(TEST) {
                    testCharge = testCharge < 100 ? testCharge + 1 : 0;
                }
                updateUI(99,tmpIn);
            }
        });

        mHealth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                DISPLAY_HEALTH=DISPLAY_HEALTH<2? DISPLAY_HEALTH+1:0;
                updateUI(99,tmpIn);
            }
        });

        mTextCharge = (TextView) view.findViewById(R.id.textViewBatLevelPercent);
        mTextHealth =(TextView) view.findViewById(R.id.textViewBatHealthPercent);
        mTextTTE =(TextView) view.findViewById(R.id.textViewTTE);


        mTextChargeTitle = (TextView) view.findViewById(R.id.textViewBatLevelTitle);
        mTextHealthTitle = (TextView) view.findViewById(R.id.textViewBatHealthTitle);
        ImageButton readBLE = (ImageButton) view.findViewById(R.id.readBLE);
        readBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonListener.onClick("readBLE");
            }
        });

        readBLE.setAlpha(0.0f);

        ImageButton subscribeBLEBtn = (ImageButton) view.findViewById(R.id.setNotification);
        subscribeBLEBtn.setAlpha(0.0f);

        subscribeBLEBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonListener.onClick("subBLE");
            }
        });

        ImageButton themeButton= (ImageButton) view.findViewById(R.id.setTheme);
        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mThemeDialog.show(getFragmentManager(), "dialog");
            }
        });

        return view;
    }

    /**
     * Update battery status UI
     * @param errorCode 0 means no error, 99 means update from touching screen
     * @param in input String of 8 or 12(w/ IP) bytes, batteryLevel, batteryHealth, TTE/F(2 bytes), current(2 bytes), volt(2 bytes), IP(4 bytes)
     */
    protected void updateUI(int errorCode, String in){
        if(TEST) {
            in = "10,75,0,9,15,152,48,30,35,35,35,35";
            errorCode = 0;
        }
        if(in==null || in.equals("")){
            errorCode=4;
        }
        if(errorCode==0) {
            tmpInLastUpdate= System.currentTimeMillis();
            tmpIn = in;
        }

        if(errorCode==99 && System.currentTimeMillis()-tmpInLastUpdate>tmpInTimeOut){
            errorCode=2;
        }

        System.out.println("String in : "+in);
        if(errorCode!=0 &&errorCode!=99){
            mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            mTextChargeTitle.setText("");
        }
        switch (errorCode){
            case 0: case 99:
                String[] dataSet = in.split(",");
                if(dataSet.length<12){
                    return;
                }
                int batteryLevel=util.parseIntBound100(dataSet[0]);
                int batteryHealth=util.parseIntBound100(dataSet[1]);

                if(TEST) {
                    batteryLevel = testCharge;
                }
                int TTEorF = util.parseInt(dataSet[2],dataSet[3]);
                int current= util.parseInt(dataSet[4],dataSet[5]);

                if((current>>15)==1){
                    current=current-65535;
                }
                //prevent from frequent flip btw full and empty
                if(batteryLevel>95 && Math.abs(current)<30){
                    mTextTTE.setText(R.string.full_battery);
                }
                else if(current<=0) {
                    //display in minute if time is less than 300 minutes
                    if(TTEorF>300) {
                        mTextTTE.setText(String.format("%01d "+ getText(R.string.hr_left),TTEorF/60));
                    }else{
                        mTextTTE.setText(String.format("%01d "+ getText(R.string.min_left),TTEorF));
                    }
                }else{
                    if(TTEorF>300) {
                        mTextTTE.setText(String.format("%01d "+ getText(R.string.hr_full),TTEorF/60));
                    }else{
                        mTextTTE.setText(String.format("%01d "+ getText(R.string.min_full),TTEorF));
                    }
                }

                Log.d(TAG,"@updateUI, level is "+batteryLevel+" health is "+batteryHealth+" TTE/F is "+TTEorF +" current is " +current);

                switch (DISPLAY_CHARGE) {
                    case 1:
                        int volt=util.parseInt(dataSet[6],dataSet[7]);
                        mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,42);
                        mTextChargeTitle.setText(getText(R.string.voltage));
                        if(volt>1000){
                            double v=volt/1000.0;
                            mTextCharge.setText(Double.toString(v)+" V");
                        }else{
                            mTextCharge.setText(Integer.toString(volt)+" mV");
                        }
                        break;
                    case 2:
                        mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,42);
                        mTextChargeTitle.setText(getText(R.string.current));
                        int absAmp=Math.abs(current);
                        if(absAmp>1000){
                            double a=absAmp/1000.0;
                            mTextCharge.setText(Double.toString(a)+" A");

                        }else{
                            mTextCharge.setText(Integer.toString(absAmp) + " mA");

                        }
                        break;
                    default:
                        mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,56);
                        mTextChargeTitle.setText(getText(R.string.charge));
                        mTextCharge.setText(util.bound(Integer.toString(batteryLevel)));
                        break;

                }
                switch (DISPLAY_HEALTH){
                    case 1:
                        int cycle=util.parseInt(dataSet[8],dataSet[9]);
                        mTextHealth.setTextSize(TypedValue.COMPLEX_UNIT_SP,56);
                        mTextHealth.setText(String.valueOf(cycle));
                        mTextHealthTitle.setText(getText(R.string.cycle));
                        break;
                    case 2:
                        double repCap=util.parseInt(dataSet[10],dataSet[11])/100.0;
                        mTextHealth.setTextSize(TypedValue.COMPLEX_UNIT_SP,42);
                        mTextHealth.setText(String.valueOf(repCap)+" Ah");
                        mTextHealthTitle.setText(R.string.capacity);
                        break;
                    default:
                        mTextHealth.setTextSize(TypedValue.COMPLEX_UNIT_SP,56);
                        mTextHealthTitle.setText(getText(R.string.health));
                        mTextHealth.setText(util.bound(Integer.toString(batteryHealth)));

                }
                mCharge.setData(batteryLevel);
                mHealth.setData(batteryHealth);

                setTextColor(true);
                break;

            case 2:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(getText(R.string.no_connection));
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
                setTextColor(false);
                break;
            case 3:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(getText(R.string.connected));
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
                setTextColor(false);
                break;
            case 4:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(R.string.wrong_data);
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
                setTextColor(false);
                break;
            default:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(getText(R.string.wrong_device));
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
                setTextColor(false);
                break;
        }
    }



    public void setButtonListener(BatteryStatusDisplay.buttonListener onClickListener){
        mButtonListener=onClickListener;
    }



    private void setTextColor(boolean connected){
        int chargeColor;
        int healthColor;
        if(!connected){
            chargeColor=mCharge.getDefaultColor();
            healthColor=mHealth.getDefaultColor();
        }else {
            chargeColor=mCharge.getColor(false);
            healthColor=mHealth.getColor(false);
        }
        setChargeTextColor(chargeColor);
        setHealthTextColor(healthColor);
    }

    private void setChargeTextColor(int color){
        //Log.d(TAG,"@setChargeTextColor: color is "+Integer.toHexString(color));
        mTextCharge.setTextColor(color);
        mTextChargeTitle.setTextColor(color);
        mTextTTE.setTextColor(color);
    }

    private void setHealthTextColor(int color){
        mTextHealth.setTextColor(color);
        mTextHealthTitle.setTextColor(color);
    }
}
