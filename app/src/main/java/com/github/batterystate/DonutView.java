
package com.github.batterystate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.view.View;


public class DonutView extends View {
    private final String TAG="DonutView";
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

    private final int mColorFull;
    private final int mBGColorFull;

    private final int mColorMiddle;
    private final int mBGColorMiddle;

    private final int mColorEmpty;
    private final int mBGColorEmpty;

    private float smallCircleOffset;

    private double[][] frontColor;
    private double[][] backColor;

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
            mColorFull =a.getInteger(R.styleable.DonutView_donut_color_full,0x000000);
            mBGColorFull =a.getInteger(R.styleable.DonutView_donut_color_full_fade,0xffffff);
            mColorMiddle =a.getInteger(R.styleable.DonutView_donut_color_middle,0x000000);
            mBGColorMiddle =a.getInteger(R.styleable.DonutView_donut_color_middle_fade,0xffffff);
            mColorEmpty =a.getInteger(R.styleable.DonutView_donut_color_empty,0x000000);
            mBGColorEmpty =a.getInteger(R.styleable.DonutView_donut_color_empty_fade,0xffffff);

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
        mPaint.setColor(mColorFull);

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

        frontColor =new double[3][3];
        parseColor(frontColor,0,mColorEmpty);
        parseColor(frontColor,1,mColorMiddle);
        parseColor(frontColor,2,mColorFull);
        backColor =new double[3][3];
        parseColor(backColor,0,mBGColorEmpty);
        parseColor(backColor,1,mBGColorMiddle);
        parseColor(backColor,2,mBGColorFull);
    }

    private void parseColor(double[][] array, int index, int color){
        for(int i=0;i<3;i++){
            int thisColor=(color>>((2-i)*8))&0xFF;
            array[index][i]=thisColor;
        }
    }

    /**
     *
     * @param dataIn data to display, range 0-100
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


        if (mClockwise) {
            if (mData == 100.0f) {
                mPaint.setColor(mColorFull);
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.99f);
                canvas.drawCircle( mRadius,smallCircleOffset,mRadius*0.05f,mPaint);
            } else if (mData == 0.0f) {
                mPaint.setColor(mBGColorEmpty);
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.99f);
                canvas.drawCircle( mRadius,smallCircleOffset,mRadius*0.05f,mPaint);
            } else {
                int mColor=getColor(false);
                //Log.d(TAG,"@onDraw: mColor is"+ Integer.toHexString(mColor));
                mPaint.setColor(mColor);
                drawDonut(canvas, mPaint, mPath, 270.0f - mData * 3.60f, mData * 3.60f);
                int mBGColor=getColor(true);
                //Log.d(TAG,"@onDraw: mBGColor is"+ Integer.toHexString(mBGColor));
                mPaint.setColor(mBGColor);
                drawDonut(canvas, mPaint, mPath, 270.0f, (100 - mData) * 3.60f);
                mPaint.setColor(mColor);
                canvas.drawCircle(mRadius  - (float) Math.sin(Math.toRadians(  mData * 3.6f)) * (mRadius - smallCircleOffset), mRadius - (float) Math.cos(Math.toRadians( mData * 3.6f))* (mRadius - smallCircleOffset), mRadius * 0.05f, mPaint);
            }
        } else {//energy decreases counterclockwise
            if (mData == 100.0f) {
                mPaint.setColor(mColorFull);
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.9f);
                canvas.drawCircle( mRadius,smallCircleOffset,mRadius*0.05f,mPaint);
            } else if (mData == 0.0f) {
                mPaint.setColor(mBGColorEmpty);
                drawDonut(canvas, mPaint, mPath, 0.0f, 359.99f);
                canvas.drawCircle( mRadius,smallCircleOffset,mRadius*0.05f,mPaint);
            } else {
                int mColor=getColor(false);
                mPaint.setColor(mColor);
                drawDonut(canvas, mPaint, mPath, 270.0f, mData * 3.60f);
                int mBGColor=getColor(true);
                mPaint.setColor(mBGColor);
                drawDonut(canvas, mPaint, mPath, 270.0f + mData * 3.60f, (100.0f - mData) * 3.60f);
                mPaint.setColor(mColor);
                canvas.drawCircle(mRadius  - (float) Math.sin(Math.toRadians(  mData * 3.6f)) * (mRadius - smallCircleOffset), mRadius - (float) Math.cos(Math.toRadians( mData * 3.6f))* (mRadius - smallCircleOffset), mRadius * 0.05f, mPaint);
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

    /**
     *
     * @param isBGColor background color
     * @return a color in btw start color and end color, with respect to current data/50
     */
    protected int getColor(boolean isBGColor){
        double[] start, end;
        double[] color=new double[3];
        double regulate;
        if(mData<50.0f){
            if(!isBGColor) {
                start = frontColor[0];
                end = frontColor[1];
            }else {
                start = backColor[0];
                end = backColor[1];
            }
            regulate=mData;
        }else{
            if(!isBGColor) {
                start = frontColor[1];
                end = frontColor[2];
            }else {
                start = backColor[1];
                end = backColor[2];
            }
            regulate=mData-50.0f;
        }
        for(int i=0;i<3;i++){
            color[i]=start[i]+((end[i]-start[i])*regulate/50.0f);
            if(color[i]>255.0f){
                color[i]=255.0f;
            }else if(color[i]<0){
                color[i]=0;
            }
            //Log.d(TAG,"@onDraw: color "+i+" is "+color[i]);
        }

        //int colorReturn=((int)(Math.round(color[0]))<<16)+((int)(Math.round(color[1]))<<8)+(int)color[2];
        //Log.d(TAG,"@onDraw: color return "+Integer.toHexString(colorReturn));

        //add ff000000 for alpha
        return 0xff000000+((int)(Math.round(color[0]))<<16)+
                ((int)(Math.round(color[1]))<<8)+(int)color[2];
    }


    public int getDefaultColor(){
        return mBGColorEmpty;
    }
}
