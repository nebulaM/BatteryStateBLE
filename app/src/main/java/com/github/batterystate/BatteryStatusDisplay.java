
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
import android.widget.Toast;

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

    private Toast mToast;

    private String mIP;

    private int DISPLAY=0;

    private String tmpIn="";


    private int testCharge=0;

    private final boolean TEST=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getActivity().getApplicationContext(),"",Toast.LENGTH_SHORT);
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
                DISPLAY=DISPLAY<1?DISPLAY+1:0;
                if(TEST) {
                    testCharge = testCharge < 100 ? testCharge + 1 : 0;
                }
                updateUI(0,tmpIn);
            }
        });

        mHealth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!mToast.getView().isShown() && mIP !=null) {
                        mToast.setText(mIP);
                        mToast.show();
                }
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

        ImageButton subscribeBLEBtn = (ImageButton) view.findViewById(R.id.setNotification);
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
     * @param errorCode 0 means no error
     * @param in input String of 8 or 12(w/ IP) bytes, batteryLevel, batteryHealth, TTE/F(2 bytes), current(2 bytes), volt(2 bytes), IP(4 bytes)
     */
    protected void updateUI(int errorCode, String in){
        if(TEST) {
            in = "10,10,10,10,0,995,10,10";
            errorCode = 0;
        }
        if(in==null || in.equals("")){
            return;
        }
        tmpIn=in;
        System.out.println("String in : "+in);
        if(errorCode!=0){
            mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            mTextChargeTitle.setText("");
        }
        switch (errorCode){
            case 0:
                String[] dataSet = in.split(",");
                int batteryLevel=Integer.parseInt(dataSet[0]);
                if(batteryLevel<0){
                    batteryLevel+=256;
                }
                int batteryHealth=Integer.parseInt(dataSet[1]);
                if(batteryHealth<0){
                    batteryHealth+=256;
                }
                if(TEST) {
                    batteryLevel = testCharge;
                }
                int current=((int) ((Long.parseLong(dataSet[4])<<8)&0xFF00))+(int) Long.parseLong(dataSet[5]);
                int TTEorF = ((int) ((Long.parseLong(dataSet[2]) << 8) & 0xFF00)) + (int) Long.parseLong(dataSet[3]);
                if((current>>15)==1){
                    current=current-65535;
                }
                if(current<=0) {
                    if(TTEorF>60) {
                        mTextTTE.setText(String.valueOf(TTEorF/60) + " hr left");
                    }else{
                        mTextTTE.setText(String.valueOf(TTEorF) + " min left");
                    }
                }else{
                    if(TTEorF>60) {
                        mTextTTE.setText(String.valueOf(TTEorF/60) + " hr to full");
                    }else{
                        mTextTTE.setText(String.valueOf(TTEorF) + " min to full");
                    }
                }
                if(dataSet.length==12) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 4; ++i) {
                        int data = Integer.parseInt(dataSet[8 + i]);
                        if (data < 0) {
                            data = 256 + data;
                        }
                        sb.append(data);
                        sb.append('.');
                    }
                    sb.setLength(sb.length() - 1);
                    mIP =sb.toString();
                    System.out.println("mIP: "+mIP);
                }
                Log.d(TAG,"@updateUI, level is "+batteryLevel+" health is "+batteryHealth+" TTE/F is "+TTEorF +" current is " +current);

                switch (DISPLAY) {
                    case 1:
                        mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,32);
                        mTextChargeTitle.setText(getText(R.string.current));
                        int absAmp=Math.abs(current);
                        if(absAmp>1000){
                            int amp=absAmp/1000;
                            int mAmp=absAmp-(amp*1000);
                            if(current>0) {
                                mTextCharge.setText(Integer.toString(amp) + "." + Integer.toString(mAmp) + " A");
                            }else{
                                mTextCharge.setText(Integer.toString(amp) + "." + Integer.toString(mAmp) + " A");
                            }
                        }else{
                            if(current>0) {
                                mTextCharge.setText(Integer.toString(absAmp) + " mA");
                            }else{
                                mTextCharge.setText(Integer.toString(absAmp) + " mA");
                            }
                        }
                        break;
                    default:
                        mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,56);
                        mTextChargeTitle.setText(getText(R.string.charge));
                        mTextCharge.setText(bound(Integer.toString(batteryLevel)));
                        break;

                }
                mCharge.setData(batteryLevel);
                mHealth.setData(batteryHealth);

                mTextHealth.setText(bound(Integer.toString(batteryHealth)));

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
