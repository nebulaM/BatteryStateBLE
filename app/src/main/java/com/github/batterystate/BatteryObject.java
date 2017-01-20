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



    public BatteryObject(String serialNum){
        setSerialNum(serialNum);
        //TODO:find a way to determine battery type
        setBatteryType("TEST");
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
        mHealth=health;
    }

    @DynamoDBAttribute(attributeName = "Charge")
    public String getCharge() {
        return mCharge;
    }
    public void setCharge(String charge){
        mCharge=charge;
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
