package com.yw.FlowAuto.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import com.yw.FlowAuto.R;

/**
 * User: qii
 * Date: 12-11-15
 */

public class PerformanceTextView extends TextView {

    private boolean mMeasuredExactly = false;

    private boolean mBlockMeasurement = false;
    private Paint paint = new Paint();
    private boolean pressed = false;

    public PerformanceTextView(Context context) {
        super(context);
    }

    public PerformanceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PerformanceTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setBackgroundDrawable(Drawable drawable) {
        mBlockMeasurement = true;
//        super.setImageDrawable(drawable);
        mBlockMeasurement = false;
        super.setBackgroundDrawable(drawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (pressed) {
            canvas.drawColor(getResources().getColor(R.color.transparent_cover));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressed = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void requestLayout() {
        if (mBlockMeasurement && mMeasuredExactly) {
            // Ignore request

        } else {
            super.requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasuredExactly = isMeasuredExactly(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean isMeasuredExactly(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        return widthMeasureSpecMode == MeasureSpec.EXACTLY
                && heightMeasureSpecMode == MeasureSpec.EXACTLY;
    }
}