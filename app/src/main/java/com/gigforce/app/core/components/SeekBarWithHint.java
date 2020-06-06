package com.gigforce.app.core.components;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.gigforce.app.R;

@SuppressLint("AppCompatCustomView")
public class SeekBarWithHint extends SeekBar {
    private Paint seekBarHintPaint;
    private int hintTextColor;
    private float hintTextSize;
    private SeekBarDependentCanvas otherView;

    public SeekBarWithHint(Context context) {
        super(context);
        init();
    }

    public SeekBarWithHint(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeekBarWithHint,
                0, 0);

        try {
            hintTextColor = a.getColor(R.styleable.SeekBarWithHint_hint_text_color, 0);
            hintTextSize = a.getDimension(R.styleable.SeekBarWithHint_hint_text_size, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    public SeekBarWithHint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public SeekBarWithHint(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeekBarWithHint,
                0, 0);

        try {
            hintTextColor = a.getColor(R.styleable.SeekBarWithHint_hint_text_color, 0);
            hintTextSize = a.getDimension(R.styleable.SeekBarWithHint_hint_text_size, 0);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        seekBarHintPaint = new TextPaint();
        seekBarHintPaint.setColor(getResources().getColor(R.color.black));
        seekBarHintPaint.setTextAlign(Paint.Align.CENTER);
        seekBarHintPaint.setTextSize(40);
    }

    public void setOtherView(SeekBarDependentCanvas otherView) {
        this.otherView = otherView;
    }

    int thumb_x = -1;
    int middle = -1;
    private Canvas canvas = null;
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(this.canvas==null && this.otherView!=null)this.canvas = this.otherView.getCanvas();
        if (this.otherView != null && this.canvas != null && thumb_x != -1 && middle != -1) {
            this.canvas.drawColor(Color.WHITE);
        }
        thumb_x = (int) (((double) this.getProgress() / this.getMax()) * (double) this.getWidth()) + 10;
        middle = getHeight() / 2 + 4;
        if (this.otherView != null && this.canvas != null) {
            String text = "";
            if(String.valueOf(getProgress()).equals("0")) {
                text = "  0 KM";
                this.canvas.drawText(text, thumb_x+25, middle, seekBarHintPaint);
            }
            else {
                text = String.valueOf(getProgress()) + " KM";
                this.canvas.drawText(text, thumb_x, middle, seekBarHintPaint);
            }
        }
    }
}
