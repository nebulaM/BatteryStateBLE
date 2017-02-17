
package com.github.batterystate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class BatteryStatusDisplay extends Fragment {
    private final static String TAG ="BatteryStatusDisplay";
    private TextView mTextCharge;
    private TextView mTextChargeTitle;
    private TextView mTextHealth;
    private TextView mTextTTE;

    private DonutView mCharge;
    private DonutView mHealth;

    private Toast mToast;

    private String mIP;
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

        /*mHealth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!mToast.getView().isShown() && mIP !=null) {
                        mToast.setText(mIP);
                        mToast.show();
                }
            }
        });*/

        mTextCharge = (TextView) view.findViewById(R.id.textViewBatLevelPercent);
        mTextHealth =(TextView) view.findViewById(R.id.textViewBatHealthPercent);
        mTextTTE =(TextView) view.findViewById(R.id.textViewTTE);


        mTextChargeTitle = (TextView) view.findViewById(R.id.textViewBatLevelTitle);



        return view;
    }

    protected void updateUI(int errorCode, String in){
        if(errorCode==0){
            mTextCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP,56);
            mTextChargeTitle.setText(getText(R.string.charge));
        }else{
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

                int TTE=((int) ((Long.parseLong(dataSet[2])<<8)&0xFF00))+(int) Long.parseLong(dataSet[3]);
                /*if(dataSet.length>=7) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 4; ++i) {
                        int data = Integer.parseInt(dataSet[4 + i]);
                        if (data < 0) {
                            data = 256 + data;
                        }
                        sb.append(data);
                        sb.append('.');
                    }
                    sb.setLength(sb.length() - 1);
                    mIP =sb.toString();
                }*/
                Log.d(TAG,"@updateUI, level is "+batteryLevel+" health is "+batteryHealth+" TTE is "+TTE);

                mCharge.setData(batteryLevel);
                mHealth.setData(batteryHealth);
                mTextCharge.setText(bound(Integer.toString(batteryLevel)));
                mTextHealth.setText(bound(Integer.toString(batteryHealth)));
                if(TTE>60) {
                    mTextTTE.setText(String.valueOf(TTE/60) + " hr left");
                }else{
                    mTextTTE.setText(String.valueOf(TTE) + " min left");
                }
                break;
            case 1:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(getText(R.string.wrong_device));
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
                break;
            case 2:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(getText(R.string.no_connection));
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
                break;
            case 3:
                mCharge.setData(0);
                mHealth.setData(0);
                mTextCharge.setText(getText(R.string.connected));
                mTextHealth.setText("");
                mTextTTE.setText(R.string.TTE_Default);
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
