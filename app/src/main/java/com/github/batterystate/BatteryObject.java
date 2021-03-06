package com.github.batterystate;
import android.support.annotation.Keep;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * contains battery data to be sent to dynamodb
 */
@DynamoDBTable(tableName = "Battery")
@Keep public class BatteryObject {
    public final String TAG="BatteryObject";
    private String mSerialNum;
    private String mCharge="0";
    private String mHealth="0";
    private String mLastUpdate;
    private String mBatteryType;

    private String mCycle;
    private String mRepCap;

    public BatteryObject(){
    }

    @DynamoDBHashKey(attributeName = "serial-num")
    public String getSerialNum() {
        return mSerialNum;
    }
    public void setSerialNum(String serialNum){
        mSerialNum=serialNum;
    }
    @DynamoDBAttribute(attributeName = "Battery-Type")
    public String getBatteryType() {
        return mBatteryType;
    }
    public void setBatteryType(String batteryType){
        mBatteryType=batteryType;
    }



    @DynamoDBAttribute(attributeName = "Health")
    public String getHealth() {
        return mHealth;
    }
    public void setHealth(String health){
        mHealth=util.bound(health);
        Log.d(TAG,"@setHealth: battery health "+mHealth);
    }

    @DynamoDBAttribute(attributeName = "Charge")
    public String getCharge() {
        return mCharge;
    }
    public void setCharge(String charge){
        mCharge=util.bound(charge);
        Log.d(TAG,"@setCharge: battery charge "+mCharge);
    }

    @DynamoDBAttribute(attributeName = "Cycle")
    public String getCycle() {
        return mCycle;
    }
    public void setCycle(String Cycle){
        mCycle=Cycle;
        Log.d(TAG,"@setCycle: battery Cycle "+mCycle);
    }

    @DynamoDBAttribute(attributeName = "RepCap(Ah)")
    public String getRepCap() {
        return mRepCap;
    }
    public void setRepCap(String RepCap){
        mRepCap=RepCap;
        Log.d(TAG,"@setRepCap: battery RepCap "+mRepCap);
    }

    @DynamoDBAttribute(attributeName = "Last-Update")
    public String getLastUpdate() {
        return mLastUpdate;
    }
    public void setUpdate(String update){
        mLastUpdate=update;
    }

    /**
     *
     * @param data from BLE, format is errorCode,charge,health(,ip)
     *             only need charge and health
     *             errorCode 0 means no error
     */
    /*public void setBatteryData(String data){
        String[] dataSet = data.split(",");
        if(dataSet[0].equals("0")){
            setCharge(dataSet[1]);
            setHealth(dataSet[2]);
            setUpdate(df.format(c.getTime()));
            Log.d(TAG,"send to dynamodb: charge "+mCharge+" health "+mHealth+" at "+mLastUpdate);
        }
    }*/

}
