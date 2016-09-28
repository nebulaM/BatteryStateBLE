package com.cadex.nebulaM.wheelchairpower.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.cadex.nebulaM.wheelchairpower.R;

/**
 * Created by nebulaM on 9/17/2016.
 */
public class ViewBatteryHealth extends View{
    
    private float mHeight;
    private float mWidth;

    private Paint mPaint;

    RectF rectangle;

    private float mBatteryHealth;
    /**
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link } as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public ViewBatteryHealth(Context context, AttributeSet attrs) {
        super(context, attrs);

        // attrs contains the raw values for the XML attributes
        // that were specified in the layout, which don't include
        // attributes set by styles or themes, and which may have
        // unresolved references. Call obtainStyledAttributes()
        // to get the final values for each attribute.
        //
        // This call uses R.styleable.ViewbatteryHealth, which is an array of
        // the custom attributes that were declared in attrs.xml.
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ViewBatteryHealth,
                0, 0
        );

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.ViewbatteryHealth_* constants represent the index for
            // each custom attribute in the R.styleable.ViewbatteryHealth array.
            mWidth=a.getDimension(R.styleable.ViewBatteryHealth_width,20.0f);
            mHeight=a.getDimension(R.styleable.ViewBatteryHealth_height,20.0f);

        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }
        
        init();
    }

    private void init() {

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        rectangle=new RectF();
        rectangle.set(0,0,mWidth,mHeight);
    }

    public void setBatteryHealth(float batteryHealth){
        if(batteryHealth<0.0f){
            mBatteryHealth=0.0f;
        }
        else if(batteryHealth>100.0f){
            mBatteryHealth=100.0f;
        }
        else{
            mBatteryHealth=batteryHealth;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) mWidth;
        int desiredHeight = (int) mHeight;

        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize;
        }else if (widthMode == View.MeasureSpec.AT_MOST) {
            //wrap content
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBatteryHealth > 30) {
            // green
            mPaint.setColor(0xff3fff00);//, 0xff33d200);
        } else if (mBatteryHealth > 15) {
            // yellow
            mPaint.setColor(0xfff0f000);//, 0xfff5c401);
        } else {
            //red
            mPaint.setColor(0xffe66d56);//, 0xffb7161b);
        }
        canvas.drawRect(0,0,mBatteryHealth*mWidth/100.0f,mHeight,mPaint);
    }

    public void setGradient(int sColor, int eColor){
        mPaint.setShader(new LinearGradient(0, 0, mWidth, mHeight,
                new int[]{sColor,eColor},
                new float[]{.6f,.95f}, Shader.TileMode.CLAMP) );

    }
}
