package com.cadex.nebulaM.wheelchairpower.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import android.util.AttributeSet;
import android.view.View;


import com.cadex.nebulaM.wheelchairpower.R;

/**
 * Created by nebulaM on 9/16/2016.
 */
public class ViewBatteryLevel extends View {
    //in which direction battery level decreases
    private boolean mClockwise;
    //radius of the donut
    private float mRadius;
    private final float mDiameter;

    private float mBatteryLevel;

    private Paint mPaint;
    private Path mPath;

    RectF outerCircle;
    RectF innerCircle;
    RectF shadowRectF;

    private final int YellowGreen=0xFF9ACD32;
    private final int Khaki=0xFFF0E68C;
    private final int LightSalmon=0xFFFF9999;


    /**
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link } as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public ViewBatteryLevel(Context context, AttributeSet attrs) {
        super(context, attrs);

        // attrs contains the raw values for the XML attributes
        // that were specified in the layout, which don't include
        // attributes set by styles or themes, and which may have
        // unresolved references. Call obtainStyledAttributes()
        // to get the final values for each attribute.
        //
        // This call uses R.styleable.ViewBatteryLevel, which is an array of
        // the custom attributes that were declared in attrs.xml.
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ViewBatteryLevel,
                0, 0
        );

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.ViewBatteryLevel_* constants represent the index for
            // each custom attribute in the R.styleable.ViewBatteryLevel array.
            mClockwise = a.getBoolean(R.styleable.ViewBatteryLevel_clockwise, false);
            mRadius=a.getDimension(R.styleable.ViewBatteryLevel_radius,20.0f);
           
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }
        mDiameter=mRadius*2;
        init();
    }
    private void init() {

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mRadius / 14.0f);

        mPath = new Path();
        outerCircle = new RectF();
        innerCircle = new RectF();
        shadowRectF = new RectF();

        float startCord;

        startCord = .03f * mRadius;
        outerCircle.set(startCord, startCord, mDiameter-startCord, mDiameter-startCord);

        startCord = .3f * mRadius;
        innerCircle.set(startCord, startCord, mDiameter-startCord, mDiameter-startCord);

    }
    /**
     *
     * @param batteryLevel
     */
    public void setBatteryLevel(int batteryLevel){
        if(batteryLevel<0){
            mBatteryLevel=0.0f;
        }
        else if(batteryLevel>100){
            mBatteryLevel=100.0f;
        }
        else{
            mBatteryLevel=(float)batteryLevel;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) mDiameter;
        int desiredHeight = (int) mDiameter;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }else if (widthMode == MeasureSpec.AT_MOST) {
            //wrap content
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBatteryLevel>30){
            // green
            mPaint.setColor(0xff38e100);//, 0xff38e100);
        }
        else if(mBatteryLevel>15){
            // yellow
            mPaint.setColor(0xfff5c401);//,0xfff5c401);
        }
        else{
            //red
            mPaint.setColor(0xffb7161b);//,0xffb7161b);
        }

        if(mClockwise) {
            if(mBatteryLevel==100){
                drawDonut(canvas, mPaint,0.0f, 359.99f);
            }
            else {
                drawDonut(canvas, mPaint, 270.0f - mBatteryLevel * 3.60f, mBatteryLevel * 3.60f);
            }
        }
        else{
            if(mBatteryLevel==100){
                drawDonut(canvas, mPaint,0.0f, 359.9f);
            }
            else {
                drawDonut(canvas, mPaint, 270.0f, mBatteryLevel * 3.60f);
            }
        }
    }

    public void drawDonut(Canvas canvas, Paint paint, float start,float sweep){

        mPath.reset();
        mPath.arcTo(outerCircle, start, sweep, false);
        mPath.arcTo(innerCircle, start+sweep, -sweep, false);
        mPath.close();
        canvas.drawPath(mPath, paint);
    }


}
