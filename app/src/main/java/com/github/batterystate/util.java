package com.github.batterystate;

/**
 * local utils
 */

public class util {
    /**
     *
     * @param in string input to be regulated
     * @return string represents an int value btw 0-100
     */
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


    protected static int parseIntBound100(String in){
        int data=Integer.parseInt(in);
        if(data<0){
            data+=256;
        }
        if(data>100){
            return 100;
        }else if(data<0){
            return 0;
        }else {
            return data;
        }
    }

    /**
     *
     * @param upper 8-bit of a 16-bit number
     * @param lower 8-bit of a 16-bit number
     * @return integer, a 16-bit number represented by upper*256+lower
     */
    protected static int parseInt(String upper, String lower){
        int upper8=Integer.parseInt(upper);
        int lower8=Integer.parseInt(lower);
        if(upper8<0){
            upper8+=256;
        }
        if(lower8<0){
            lower8+=256;
        }
        return ((upper8<< 8) & 0xFF00) + lower8;
    }

    protected static int parseInt(byte upper, byte lower){
        int upper8=(int)upper;
        int lower8=(int)lower;
        if(upper8<0){
            upper8+=256;
        }
        if(lower8<0){
            lower8+=256;
        }
        return ((upper8<< 8) & 0xFF00) + lower8;
    }
}
