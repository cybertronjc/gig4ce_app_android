package com.gigforce.app.core.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.gigforce.app.R;


public class CircularButtonAndroid extends ButtonLatto {
    private Paint paint;
    private boolean selected = false;

    public CircularButtonAndroid(Context context) {
        super(context);
        init();
    }

    public CircularButtonAndroid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularButtonAndroid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void init() {
        super.init();
        paint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {

        int x = getWidth();
        int y = getHeight();
        int radius;
        radius = x > y ? x / 4 : y / 4;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawPaint(paint);
        Paint paintRec = new Paint();
        paintRec.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x / 2, y / 2, 20, paint);
        if (selected) {

            paint.setColor(ContextCompat.getColor(getContext(), R.color.red));
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(x / 2, y / 2, radius, paint);
        }
        super.onDraw(canvas);
    }

    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
        invalidate();
    }
}
