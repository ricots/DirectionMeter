package com.blackbox.direction.meter.views;

/**
 * Created by Waleed on 25/08/2016.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;


import com.blackbox.direction.meter.R;
import com.blackbox.direction.meter.models.TargetLocation;

import java.util.Random;

public class LockView extends View {

    private String TAG = LockView.class.getSimpleName();

    private Paint mTextPaint, mContainer, mMainLinePaint, mSecondaryLinePaint, mTerciaryLinePaint, mMarkerPaint, drawPaint;

    private int mTextColor, mBackgroundColor, mLineColor;
    private float mDegrees, mTextSize, mRangeDegrees;

    float strokeSize;
    private boolean targetSet = false;
    private String targetName = "";
    private String targetURL = "";
    private String targetDistance = "";
    private Canvas canvas;
    private Context context;

    private TargetLocation targetList;

    Paint.FontMetrics fm;
    String randomStr;
    String[] array = {"#FF007F", "#FF0000", "#FF7F00", "#7FFF00", "#00FF00", "#007fff", "#0000FF", "#7F00FF"};

    public LockView(Context context) {
        super(context);
        this.context = context;
    }

    public LockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        strokeSize = getResources().getDimensionPixelSize(R.dimen.scale_stroke_size);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CompassView, 0, 0);

        mBackgroundColor = a.getColor(R.styleable.CompassView_backgroundColor, Color.TRANSPARENT);
        mLineColor = a.getColor(R.styleable.CompassView_lineColor, Color.GREEN);
        mTextColor = a.getColor(R.styleable.CompassView_textColor, Color.WHITE);
        mTextSize = a.getDimension(R.styleable.CompassView_textSize, 15 * getResources().getDisplayMetrics().scaledDensity);
        mDegrees = a.getFloat(R.styleable.CompassView_degrees, 120);
        mRangeDegrees = a.getFloat(R.styleable.CompassView_rangeDegrees, 180f);
        a.recycle();
        init();
    }


    private void setupPaint() {
        // Setup paint with color and stroke styles
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setShadowLayer(4, 2, 2, 0x80000000);
        drawPaint.setStrokeWidth(strokeSize);
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setSubpixelText(true);

        fm = new Paint.FontMetrics();
        mContainer = new Paint(Paint.ANTI_ALIAS_FLAG);
        mContainer.setTextAlign(Paint.Align.CENTER);
        mContainer.setColor(Color.BLACK);
        mContainer.setTextSize(18.0f);
        mContainer.getFontMetrics(fm);

        mMainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainLinePaint.setStrokeWidth(8f);

        mSecondaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondaryLinePaint.setStrokeWidth(6f);

        mTerciaryLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTerciaryLinePaint.setStrokeWidth(3f);

        mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setStyle(Paint.Style.FILL);

        setupPaint();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable("instanceState", super.onSaveInstanceState());
        b.putFloat("degrees", mDegrees);

        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle b = (Bundle) state;
            mDegrees = b.getFloat("degrees", 0);

            state = b.getParcelable("instanceState");
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int minWidth = (int) Math.floor(50 * getResources().getDisplayMetrics().density);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;

        } else {
            result = minWidth + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int minHeight = (int) Math.floor(30 * getResources().getDisplayMetrics().density);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;

        } else {
            result = minHeight + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        mMainLinePaint.setColor(mLineColor);
        mSecondaryLinePaint.setColor(mLineColor);
        mTerciaryLinePaint.setColor(mLineColor);

        mMarkerPaint.setColor(Color.RED);

        canvas.drawColor(mBackgroundColor);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        int unitHeight = (height - paddingTop - paddingBottom) / 24;

        float pixDeg = (width - paddingLeft - paddingRight) / mRangeDegrees;

        int minDegrees = Math.round(mDegrees - mRangeDegrees / 2), maxDegrees = Math.round(mDegrees
                + mRangeDegrees / 2);

        for (int i = -180; i < 540; i += 1) {
            if ((i >= minDegrees) && (i <= maxDegrees)) {

                TargetLocation tl = targetList;
                if (tl != null) {
                    if (i == tl.getBearing()) {

                        targetName = tl.getName();

                        targetURL = tl.getImage_url();
                        targetDistance = tl.getDistance();

                        float x = paddingLeft + pixDeg * (i - minDegrees);
                        float y = 5 * unitHeight + paddingTop + 130;

                        float startX = x;
                        float startY = height - paddingBottom - 270;
                        float stopX = paddingLeft + pixDeg * (i - minDegrees);
                        float stopY = y + 30;

                        float w = (mTextPaint.measureText(targetName)/2)+15;
                        float textSize = mTextPaint.getTextSize();
                       /* drawTextAndBreakLine(canvas, mTextPaint, x,
                                y, w, targetName);*/

                        mContainer.setColor(Color.parseColor(tl.getBgColor()));

                        targetList.setPaddingTop(90);
                        drawItems(canvas, x, y + targetList.getPaddingTop(), startX, startY, stopX, stopY + 220, w, textSize);
                    }
                }
            }
        }
    }

    public static void drawTextAndBreakLine(final Canvas canvas, final Paint paint,
                                            final float x, final float y, final float maxWidth, final String text) {
        String textToDisplay = text;
        String tempText = "";
        char[] chars;
        float textHeight = paint.descent() - paint.ascent();
        float lastY = y;
        int nextPos = 0;
        int lengthBeforeBreak = textToDisplay.length();
        do {
            lengthBeforeBreak = textToDisplay.length();
            chars = textToDisplay.toCharArray();
            nextPos = paint.breakText(chars, 0, chars.length, maxWidth, null);
            tempText = textToDisplay.substring(0, nextPos);
            textToDisplay = textToDisplay.substring(nextPos, textToDisplay.length());
            canvas.drawText(tempText, x, lastY, paint);
            lastY += textHeight;
        } while(nextPos < lengthBeforeBreak);
    }

    private void drawItems(Canvas canvas, float x, float y, float startX, float startY, float stopX, float stopY, float w, float textSize) {
        canvas.drawRect(x - w, y - textSize, x + w, y + 50, mContainer);
        canvas.drawText(targetName, x, y, mTextPaint);
        canvas.drawText(targetDistance, x, y + 40, mTextPaint);
    }


    public void setDegrees(float degrees) {
        mDegrees = degrees;
        invalidate();
        requestLayout();
    }

    public void setTarget(Context context, TargetLocation targetList) {
        this.context = context;
        targetSet = true;
        randomStr = array[new Random().nextInt(array.length)];
        targetList.setBgColor(randomStr);
        this.targetList = targetList;
        invalidate();
        requestLayout();
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidate();
        requestLayout();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        invalidate();
        requestLayout();
    }

    public void setTextSize(int size) {
        mTextSize = size;
        invalidate();
        requestLayout();
    }
}
