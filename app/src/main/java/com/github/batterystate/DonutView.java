
package com.github.batteryState;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.view.View;


import com.github.batteryState.R;


public class DonutView extends View {
    //in which direction battery level decreases
    private boolean mClockwise;
    //radius of the donut
    private float mRadius;
    private final float mDiameter;

    private float mData;

    private Paint mPaint;
    private Path mPath;

    RectF outerCircle;
    RectF innerCircle;
    RectF shadowRectF;

    private int mColor;
    private int mLightColor;

    private float[] smallCircleCord=new float[2];
    private float smallCircleOffset;


    /**
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link } as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public DonutView(Context context, AttributeSet attrs) {
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
                R.styleable.DonutView,
                0, 0
        );

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.ViewBatteryLevel_* constants represent the index for
            // each custom attribute in the R.styleable.ViewBatteryLevel array.
            mClockwise = a.getBoolean(R.styleable.DonutView_clockwise,true);
            mRadius=a.getDimension(R.styleable.DonutView_radius,20.0f);
            mColor=a.getInteger(R.styleable.DonutView_donut_color,0x000000);
            mLightColor=a.getInteger(R.styleable.DonutView_donut_color_fade,0xffffff);
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
        mPaint.setColor(mColor);

        mPath = new Path();
        outerCircle = new RectF();
        innerCircle = new RectF();
        shadowRectF = new RectF();

        float outer=0.05f;
        float inner=0.07f;
        float startCordOut = outer * mRadius;
        outerCircle.set(startCordOut, startCordOut, mDiameter-startCordOut, mDiameter-startCordOut);

        float startCordIn = inner * mRadius;
        innerCircle.set(startCordIn, startCordIn, mDiameter-startCordIn, mDiameter-startCordIn);
        smallCircleOffset=((inner+outer)/2)*mRadius;
    }
    /**
     *
     * @param dataIn
     */
    public void setData(int dataIn){
        if(dataIn<0){
            mData =0.0f;
        }
        else if(dataIn>100){
            mData =100.0f;
        }
        else{
            mData =(float)dataIn;
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
        mPaint.setColor(mColor);

        if (mClockwise) {
            if (mData == 100.0f) {
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.99f);
                canvas.drawCircle( mRadius,smallCircleOffset,mRadius*0.05f,mPaint);
            } else if (mData == 0.0f) {
                mPaint.setColor(mLightColor);
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.99f);
            } else {

                drawDonut(canvas, mPaint, mPath, 270.0f - mData * 3.60f, mData * 3.60f);
                mPaint.setColor(mLightColor);
                drawDonut(canvas, mPaint, mPath, 270.0f, (100 - mData) * 3.60f);
                mPaint.setColor(mColor);
                canvas.drawCircle(mRadius  - (float) Math.sin(Math.toRadians(  mData * 3.6f)) * (mRadius - smallCircleOffset), mRadius - (float) Math.cos(Math.toRadians( mData * 3.6f))* (mRadius - smallCircleOffset), mRadius * 0.05f, mPaint);
            }
        } else {//energy decreases counterclockwise
            if (mData == 100.0f) {
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.9f);
            } else if (mData == 0.0f) {
                mPaint.setColor(mLightColor);
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.99f);
            } else {
                drawDonut(canvas, mPaint, mPath, 270.0f, mData * 3.60f);
                mPaint.setColor(mLightColor);
                drawDonut(canvas, mPaint, mPath, 270.0f + mData * 3.60f, (100.0f - mData) * 3.60f);
            }
        }
    }

    private void drawDonut(Canvas canvas, Paint paint, Path path, float start,float sweep){
        path.reset();
        path.arcTo(outerCircle, start, sweep, false);
        path.arcTo(innerCircle, start+sweep, -sweep, false);
        path.close();
        canvas.drawPath(path, paint);
    }
}
